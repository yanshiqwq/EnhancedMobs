package cn.yanshiqwq.enhanced_mobs.listeners

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.EnhancedMob.Companion.asEnhancedMob
import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.managers.MobTypeManager
import cn.yanshiqwq.enhanced_mobs.managers.MobTypeManager.Companion.getRandomTypeId
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Mob
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.persistence.PersistentDataType
import java.lang.Exception
import java.lang.IndexOutOfBoundsException
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
    fun onEnhancedMobLoad(event: EntityTargetLivingEntityEvent){
        event.entity.persistentDataContainer.run {
            if (!has(EnhancedMob.multiplierKey) || !has(EnhancedMob.boostTypeKey) || event.entity !is Mob) return // 之前不是EnhancedMob
            if (instance!!.mobManager?.get(this as Mob) != null) return // 现在已经是EnhancedMob了
            val multiplier = get(EnhancedMob.multiplierKey, PersistentDataType.DOUBLE) ?: return
            val boostType = try {
                val typeIdString = get(EnhancedMob.boostTypeKey, PersistentDataType.STRING) ?: return
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
        val multiplier = when (val weight = Random.nextDouble()) {
            in 0.0..0.75 -> 0.0
            in 0.75 .. 0.9 -> 6 * weight - 4.5
            in 0.9 .. 0.98 -> 13.75 * weight - 11.475
            in 0.99 .. 1.0 -> 50 * weight - 17.0
            else -> return // ?
        }
        if (event.entity.entitySpawnReason != CreatureSpawnEvent.SpawnReason.CUSTOM) {
            val mob = try {
                entity.asEnhancedMob(multiplier, getRandomTypeId(entity.type)) ?: return
            } catch (ignored: Exception) { return }
            instance!!.mobManager?.register(entity.uniqueId, mob)
        }
        val teamName = when (multiplier) {
            in 0.75 .. 1.5 -> "strength"
            in 1.5 .. 2.5 -> "enhanced"
            in 2.5..114514.0 -> "boss"
            else -> return
        }
        instance!!.server.scoreboardManager.mainScoreboard.getTeam(teamName)?.addEntity(entity)
        entity.isGlowing = true
    }
}