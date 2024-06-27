package cn.yanshiqwq.enhanced_mobs.listeners

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.EnhancedMob.Companion.asEnhancedMob
import cn.yanshiqwq.enhanced_mobs.EnhancedMob.Companion.isEnhancedMob
import cn.yanshiqwq.enhanced_mobs.EnhancedMob.Companion.hasEnhancedMobData
import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.Utils.addModifierSafe
import cn.yanshiqwq.enhanced_mobs.Utils.applyEffect
import cn.yanshiqwq.enhanced_mobs.Utils.getNearestPlayer
import cn.yanshiqwq.enhanced_mobs.Utils.getTeam
import cn.yanshiqwq.enhanced_mobs.data.LootTable.applyLootTableX
import cn.yanshiqwq.enhanced_mobs.managers.TypeManager
import cn.yanshiqwq.enhanced_mobs.managers.TypeManager.Companion.getRandomTypeKey
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
    enum class MobTeam(val id: String) {
        STRENGTH("enhancedmobs.strength"),
        ENHANCED("enhancedmobs.enhanced"),
        BOSS("enhancedmobs.boss")
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent){
        getTeam(MobTeam.STRENGTH.id)?.color(NamedTextColor.AQUA)
        getTeam(MobTeam.ENHANCED.id)?.color(NamedTextColor.LIGHT_PURPLE)
        getTeam(MobTeam.BOSS.id)?.color(NamedTextColor.RED)
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent){
        event.player.run {
            // 添加重生增益
            val uuid = UUID.fromString("7e993d80-af92-40ed-9097-101b28ae76ca")
            val modifier = AttributeModifier(uuid, "Player spawn bonus", 20.0, Operation.ADD_NUMBER)
            getAttribute(Attribute.GENERIC_MAX_HEALTH)?.addModifierSafe(modifier)

            applyEffect(PotionEffectType.DAMAGE_RESISTANCE, 0, 5 * 60 * 20)
            applyEffect(PotionEffectType.NIGHT_VISION, 0, 5 * 60 * 20)
        }
    }

    @EventHandler
    fun onEnhancedMobLoad(event: EntityTargetLivingEntityEvent){
        val entity = event.entity
        if (entity !is Mob) return
        entity.run {
            // 获取 container
            val container = persistentDataContainer

            // 仅在有 EnhancedMob 数据且实体未标记于 manager 时继续执行
            if (!entity.hasEnhancedMobData() || entity.isEnhancedMob()) return

            // 从 container 中恢复数据并添加至 manager
            val multiplier = container.get(EnhancedMob.multiplierKey, PersistentDataType.DOUBLE) ?: return
            val boostType = try {
                val typeIdString = container.get(EnhancedMob.boostTypeKey, PersistentDataType.STRING) ?: return
                TypeManager.TypeKey(typeIdString)
            } catch (e: IndexOutOfBoundsException) { return }
            val mob = (event.entity as Mob).asEnhancedMob(multiplier, boostType, false) ?: return
            instance!!.mobManager.register(event.entity.uniqueId, mob)
        }
    }

    @EventHandler
    fun onMobSpawn(event: EntitySpawnEvent){
        val entity = event.entity
        if (entity !is Mob) return
        entity.run {
            // 获取与玩家等级相关的 multiplier
            val playerLevelKey = NamespacedKey(instance!!, "nearest_player_level")
            val playerLevel = location.getNearestPlayer()?.level?.coerceIn(0..233) ?: 0
            val playerLevelMultiplier = 0.12 * ln(300 - playerLevel.toDouble())
            persistentDataContainer.set(playerLevelKey, PersistentDataType.INTEGER, playerLevel)

            // 获取与难度相关的 multiplier
            val worldDifficultyMultiplier = 1 + (3 - event.entity.world.difficulty.ordinal) * 0.5

            // 获取最终 multiplier
            val multiplier = when (val weight = Random.nextDouble().pow(worldDifficultyMultiplier)) {
                in 0.0..playerLevelMultiplier -> 0.0
                in playerLevelMultiplier .. 1.0 -> 0.65 * (playerLevel + 15) * (weight - playerLevelMultiplier).pow(2)
                else -> 0.0 // ?
            }

            // 设为 EnhancedMob
            val boostTypeKey = getRandomTypeKey(type) ?: return
            if (event.entity.entitySpawnReason != CreatureSpawnEvent.SpawnReason.CUSTOM) {
                val enhancedMob = try {
                    asEnhancedMob(multiplier, boostTypeKey) ?: return
                } catch (ignored: Exception) { return }
                instance!!.mobManager.register(uniqueId, enhancedMob)
            }

            // 修改 LootTable
            applyLootTableX(multiplier)
        }
    }
}