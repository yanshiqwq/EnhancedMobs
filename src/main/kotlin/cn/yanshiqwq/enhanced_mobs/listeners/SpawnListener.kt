package cn.yanshiqwq.enhanced_mobs.listeners

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.EnhancedMob.Companion.asEnhancedMob
import cn.yanshiqwq.enhanced_mobs.EnhancedMob.Companion.hasEnhancedMobData
import cn.yanshiqwq.enhanced_mobs.EnhancedMob.Companion.isEnhancedMob
import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.Utils.addModifierSafe
import cn.yanshiqwq.enhanced_mobs.Utils.getNearestPlayer
import cn.yanshiqwq.enhanced_mobs.Utils.getTeam
import cn.yanshiqwq.enhanced_mobs.Utils.percentHeal
import cn.yanshiqwq.enhanced_mobs.api.MobApi.attribute
import cn.yanshiqwq.enhanced_mobs.api.MobApi.effect
import cn.yanshiqwq.enhanced_mobs.data.LootTable.applyLootTableX
import cn.yanshiqwq.enhanced_mobs.managers.TypeManager
import cn.yanshiqwq.enhanced_mobs.managers.TypeManager.Companion.getRandomTypeKey
import cn.yanshiqwq.enhanced_mobs.script.Config
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute.*
import org.bukkit.attribute.AttributeModifier
import org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.server.ServerLoadEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
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
    fun onServerLoad(event: ServerLoadEvent){
        getTeam(MobTeam.STRENGTH.id)?.color(NamedTextColor.AQUA)
        getTeam(MobTeam.ENHANCED.id)?.color(NamedTextColor.LIGHT_PURPLE)
        getTeam(MobTeam.BOSS.id)?.color(NamedTextColor.RED)
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent){
        event.player.run {
            // 添加重生增益
            val uuid = UUID.fromString("7e993d80-af92-40ed-9097-101b28ae76ca")
            val modifier = AttributeModifier(uuid, "Player spawn bonus", 20.0, ADD_NUMBER)
            getAttribute(GENERIC_MAX_HEALTH)?.addModifierSafe(modifier)
            effect(PotionEffectType.DAMAGE_RESISTANCE, 0, 5 * 60 * 20)
            effect(PotionEffectType.NIGHT_VISION, 0, 5 * 60 * 20)
        }
    }

    @EventHandler
    fun onEnhancedMobLoad(event: EntityTargetLivingEntityEvent){
        val entity = event.entity as? Mob ?: return
        entity.run {
            // 仅在有 EnhancedMob 数据且实体未标记于 manager 时继续执行
            if (!hasEnhancedMobData() || isEnhancedMob()) return

            // 从 container 中恢复数据并添加至 manager
            val multiplier = persistentDataContainer.get(EnhancedMob.multiplierKey, PersistentDataType.DOUBLE) ?: return
            val boostType = try {
                val typeIdString = persistentDataContainer.get(EnhancedMob.boostTypeKey, PersistentDataType.STRING) ?: return
                TypeManager.TypeKey(typeIdString)
            } catch (e: IndexOutOfBoundsException) { return }
            val mob = asEnhancedMob(multiplier, boostType, false) ?: return
            instance!!.mobManager.register(event.entity.uniqueId, mob)
        }
    }

    @EventHandler
    fun onMobSpawn(event: EntitySpawnEvent){
        val entity = event.entity as? Mob ?: return
        entity.run {
            // 获取与玩家等级相关的乘数
            val playerLevelKey = NamespacedKey(instance!!, "nearest_player_level")
            val playerLevel = location.getNearestPlayer()?.level?.coerceIn(0..233) ?: 0
            val playerLevelMultiplier = 0.12 * ln(300 - playerLevel.toDouble()) // 神秘公式
            persistentDataContainer.set(playerLevelKey, PersistentDataType.INTEGER, playerLevel)

            // 获取与难度相关的指数
            val worldDifficultyExponent = 1 + (3 - event.entity.world.difficulty.ordinal) * 0.5 // 和平=2.5, 简单=2, 普通=1.5, 困难=1

            // 获取最终乘数
            val multiplier = when (val weight = Random.nextDouble().pow(worldDifficultyExponent)) {
                in 0.0..playerLevelMultiplier -> 0.0
                in playerLevelMultiplier .. 1.0 -> 0.65 * (playerLevel + 15) * (weight - playerLevelMultiplier).pow(2) // 神秘公式^2
                else -> 0.0 // ?
            }

            // 设为 EnhancedMob
            val boostTypeKey = getRandomTypeKey(type, Config.weightMap) ?: return
            if (event.entity.entitySpawnReason == CreatureSpawnEvent.SpawnReason.CUSTOM) return // 忽略插件指令生成情况
            val mob = asEnhancedMob(multiplier, boostTypeKey) ?: return
            instance!!.mobManager.register(uniqueId, mob)

            // 根据变种修改属性
            mob.applyVariantBoost()

            // 修改 LootTable
            applyLootTableX(multiplier)

            // 血量回满
            percentHeal(1.0)
        }
    }

    private fun EnhancedMob.applyVariantBoost() {
        when (this.entity) {
            is Stray, is WitherSkeleton -> {
                attribute(GENERIC_MAX_HEALTH, ADD_NUMBER, 4.0)
                attribute(GENERIC_ARMOR, ADD_NUMBER, 2.0)
                attribute(GENERIC_ATTACK_DAMAGE, ADD_NUMBER, 0.5)
                attribute(GENERIC_FOLLOW_RANGE, ADD_NUMBER, 8.0)
            }

            is Husk, is Drowned -> {
                attribute(GENERIC_MAX_HEALTH, ADD_NUMBER, 4.0)
                attribute(GENERIC_ARMOR, ADD_NUMBER, 2.0)
                attribute(GENERIC_ATTACK_DAMAGE, ADD_NUMBER, 1.0)
                attribute(GENERIC_FOLLOW_RANGE, ADD_NUMBER, 8.0)
            }

            is Creeper -> if (entity.isPowered) {
                attribute(GENERIC_MAX_HEALTH, ADD_NUMBER, 4.0)
                attribute(GENERIC_ARMOR, ADD_NUMBER, 5.0)
                attribute(GENERIC_FOLLOW_RANGE, ADD_NUMBER, 8.0)
                attribute(GENERIC_KNOCKBACK_RESISTANCE, ADD_NUMBER, 0.35)
            }

            is Giant -> {
                attribute(GENERIC_MAX_HEALTH, ADD_NUMBER, 96.0)
                attribute(GENERIC_ARMOR, ADD_NUMBER, 6.0)
                attribute(GENERIC_ATTACK_DAMAGE, ADD_NUMBER, -37.0)
                attribute(GENERIC_MOVEMENT_SPEED, ADD_NUMBER, -0.15)
                attribute(GENERIC_FOLLOW_RANGE, ADD_NUMBER, 32.0)
                attribute(GENERIC_KNOCKBACK_RESISTANCE, ADD_NUMBER, 0.65)
                attribute(ZOMBIE_SPAWN_REINFORCEMENTS, ADD_NUMBER, 0.55)
                entity.addPotionEffect(PotionEffect(PotionEffectType.JUMP, Int.MAX_VALUE, 3, true, false))
            }

            is Piglin -> {
                attribute(GENERIC_MAX_HEALTH, ADD_NUMBER, 8.0)
                attribute(GENERIC_ARMOR, ADD_NUMBER, 4.0)
                entity.isImmuneToZombification = true
            }

            is PiglinBrute -> {
                attribute(GENERIC_MAX_HEALTH, ADD_NUMBER, 22.0)
                attribute(GENERIC_ARMOR, ADD_NUMBER, 6.0)
                attribute(GENERIC_ATTACK_KNOCKBACK, ADD_NUMBER, 0.35)
                attribute(GENERIC_KNOCKBACK_RESISTANCE, ADD_NUMBER, 0.35)
                entity.isImmuneToZombification = true
            }
        }
    }
}