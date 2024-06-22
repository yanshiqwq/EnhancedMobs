package cn.yanshiqwq.enhanced_mobs.listeners

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.EnhancedMob.Companion.asEnhancedMob
import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.Utils.getNearestPlayer
import cn.yanshiqwq.enhanced_mobs.data.LootTable
import cn.yanshiqwq.enhanced_mobs.managers.MobTypeManager
import cn.yanshiqwq.enhanced_mobs.managers.MobTypeManager.Companion.getRandomTypeId
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.attribute.AttributeModifier.Operation
import org.bukkit.entity.Mob
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.lang.Exception
import java.lang.IndexOutOfBoundsException
import java.util.*
import kotlin.math.ln
import kotlin.math.pow
import kotlin.random.Random

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.listeners.SpawnListener
 *
 * @author yanshiqwq
 * @since 2024/6/15 18:09
 */
class SpawnListener: Listener {
    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent){
        instance!!.server.scoreboardManager.mainScoreboard.run {
            (getTeam("strength") ?: registerNewTeam("strength")).color(NamedTextColor.AQUA)
            (getTeam("enhanced") ?: registerNewTeam("enhanced")).color(NamedTextColor.LIGHT_PURPLE)
            (getTeam("boss") ?: registerNewTeam("boss")).color(NamedTextColor.RED)
        }
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent){
        event.player.run {
            val uuid = UUID.fromString("7e993d80-af92-40ed-9097-101b28ae76ca")
            val modifier = AttributeModifier(uuid, "Player spawn bonus", 20.0, Operation.ADD_NUMBER)
            if (getAttribute(Attribute.GENERIC_MAX_HEALTH)?.modifiers?.contains(modifier) == true) getAttribute(Attribute.GENERIC_MAX_HEALTH)?.addTransientModifier(modifier)
            addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, 300 * 20, 0, true, false, false))
        }
    }

    @EventHandler
    fun onEnhancedMobLoad(event: EntityTargetLivingEntityEvent){
        if (event.entity !is Mob) return
        event.entity.run {
            val container = persistentDataContainer
            if (!container.has(EnhancedMob.multiplierKey) || !container.has(EnhancedMob.boostTypeKey)) return // 之前不是EnhancedMob
            if (instance!!.mobManager?.get(this as Mob) != null) return // 现在已经是EnhancedMob了
            val multiplier = container.get(EnhancedMob.multiplierKey, PersistentDataType.DOUBLE) ?: return
            val boostType = try {
                val typeIdString = container.get(EnhancedMob.boostTypeKey, PersistentDataType.STRING) ?: return
                MobTypeManager.TypeId(typeIdString)
            } catch (e: IndexOutOfBoundsException) { return }
            val mob = (event.entity as Mob).asEnhancedMob(multiplier, boostType, false) ?: return
            instance!!.mobManager?.register(event.entity.uniqueId, mob)
        }
    }

    @EventHandler
    fun onMobSpawn(event: EntitySpawnEvent){
        if (event.entity !is Mob || event.isCancelled) return
        val entity = event.entity as Mob
        val playerLevel = entity.location.getNearestPlayer()?.level?.coerceIn(0..233) ?: 0
        val playerLevelMultiplier = 0.12 * ln(300 - playerLevel.toDouble())
        val worldDifficultyModifier = 1 + (3 - event.entity.world.difficulty.ordinal) * 0.5
        val multiplier = when (val weight = Random.nextDouble().pow(worldDifficultyModifier)) {
            in 0.0..playerLevelMultiplier -> 0.0
            in playerLevelMultiplier .. 1.0 -> 0.65 * (playerLevel + 15) * (weight - playerLevelMultiplier).pow(2)
            else -> 0.0 // ?
        }

        val playerLevelKey = NamespacedKey(instance!!, "nearest_player_level")
        entity.persistentDataContainer.set(playerLevelKey, PersistentDataType.INTEGER, playerLevel)

        if (event.entity.entitySpawnReason != CreatureSpawnEvent.SpawnReason.CUSTOM) {
            val mob = try {
                entity.asEnhancedMob(multiplier, getRandomTypeId(entity.type)) ?: return
            } catch (ignored: Exception) { return }
            instance!!.mobManager?.register(entity.uniqueId, mob)
        }

        val teamName = when (multiplier) {
            in 1.0 .. 2.0 -> "strength"
            in 2.0 .. 3.0 -> "enhanced"
            in 3.0..114514.0 -> "boss"
            else -> return
        }
        instance!!.server.scoreboardManager.mainScoreboard.getTeam(teamName)?.addEntity(entity)

        LootTable.apply(entity, multiplier)
        entity.isGlowing = true
    }
}