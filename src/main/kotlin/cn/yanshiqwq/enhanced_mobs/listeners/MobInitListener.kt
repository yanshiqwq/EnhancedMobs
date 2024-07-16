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
import cn.yanshiqwq.enhanced_mobs.api.MobApi.effect
import cn.yanshiqwq.enhanced_mobs.data.LootTable.applyLootTableX
import cn.yanshiqwq.enhanced_mobs.managers.TypeManager
import cn.yanshiqwq.enhanced_mobs.managers.TypeManager.Companion.getRandomTypeKey
import cn.yanshiqwq.enhanced_mobs.script.Config
import cn.yanshiqwq.enhanced_mobs.script.Config.applyVariantBoost
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
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.server.ServerLoadEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffectType
import java.util.*

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.listeners.SpawnListener
 *
 * @author yanshiqwq
 * @since 2024/6/15 18:09
 */
class MobInitListener: Listener {
    enum class MobTeam(val id: String) {
        STRENGTH("enhancedmobs.strength"),
        ENHANCED("enhancedmobs.enhanced"),
        BOSS("enhancedmobs.boss")
    }

    @EventHandler
    fun onServerLoad(event: ServerLoadEvent){
        instance!!.logger.info("Initializing teams (${event.type}) ...")
        getTeam(MobTeam.STRENGTH.id)?.color(NamedTextColor.AQUA)
        getTeam(MobTeam.ENHANCED.id)?.color(NamedTextColor.LIGHT_PURPLE)
        getTeam(MobTeam.BOSS.id)?.color(NamedTextColor.RED)

    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent){
        event.player.run {
            // 添加重生增益
            val uuid = UUID.fromString("7e993d80-af92-40ed-9097-101b28ae76ca")
            val healthModifier = AttributeModifier(uuid, "Player spawn bonus", 20.0, ADD_NUMBER)
            val toughnessModifier = AttributeModifier(uuid, "Player spawn bonus", 8.0, ADD_NUMBER)
            getAttribute(GENERIC_MAX_HEALTH)?.addModifierSafe(healthModifier)
            getAttribute(GENERIC_ARMOR_TOUGHNESS)?.addModifierSafe(toughnessModifier)
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

            fun Entity.loadTypeKey(key: NamespacedKey): TypeManager.TypeKey? {
                return try {
                    this.persistentDataContainer.get(key, PersistentDataType.STRING)?.let { TypeManager.TypeKey(it) }
                } catch (e: IndexOutOfBoundsException) {
                    null
                }
            }

            val mainBoostTypeKey = loadTypeKey(EnhancedMob.mainKey) ?: return
            val subBoostTypeKey = loadTypeKey(EnhancedMob.subKey)

            val mob = asEnhancedMob(multiplier, mainBoostTypeKey, subBoostTypeKey)

            instance!!.mobManager.register(event.entity.uniqueId, mob)
        }
    }

    @EventHandler
    fun onEntitySpawn(event: EntitySpawnEvent) {
        val entity = event.entity as? Mob ?: return
        if (!entity.isEnhancedMob()) entity.initEnhancedMob()
    }

    @EventHandler
    fun onEntityTarget(event: EntityTargetEvent) {
        val entity = event.entity as? Mob ?: return
        if (!entity.isEnhancedMob()) entity.initEnhancedMob()
    }

    private fun Mob.initEnhancedMob() {
        // 获取与玩家等级相关的乘数
        val playerLevelKey = NamespacedKey(instance!!, "nearest_player_level")
        val playerLevel = location.getNearestPlayer()?.level ?: 0
        persistentDataContainer.set(playerLevelKey, PersistentDataType.INTEGER, playerLevel)

        // 获取与难度相关的乘数
        val worldDifficultyMultiplier = (world.difficulty.ordinal + 1) / 4 // 和平=0.25, 简单=0.5, 普通=0.75, 困难=1

        // 获取最终乘数
        val multiplier = when (playerLevel) {
            in 0..10 -> -0.1 // 5 级
            in 10..20 -> 0.125 // 16 级
            in 20..30 -> 0.4 // 24 级
            in 30..40 -> 0.75 // 33 级
            in 40..50 -> 1.35 // 45 级
            in 50..60 -> 2.0 // 56 级
            in 60..70 -> 2.75 // 67 级
            in 70..80 -> 3.4 // 75 级
            in 80..Int.MAX_VALUE -> 4.1 // 82 级
            else -> 0.0
        } * worldDifficultyMultiplier

        // 设为 EnhancedMob
        val mainBoostTypeKey = Config.getMainTypeKey(type)
        val subBoostTypeKey = Config.getWeightMap()?.let{
            getRandomTypeKey(type, it)
        }
        if (entitySpawnReason == CreatureSpawnEvent.SpawnReason.CUSTOM) return // 忽略 /enhancedmobs 生成的情况
        val mob = asEnhancedMob(multiplier, mainBoostTypeKey, subBoostTypeKey)
        instance!!.mobManager.register(uniqueId, mob)

        // 根据变种修改属性
        mob.applyVariantBoost()

        // 修改 LootTable
        applyLootTableX(multiplier)

        // 血量回满
        percentHeal(1.0)
    }
}