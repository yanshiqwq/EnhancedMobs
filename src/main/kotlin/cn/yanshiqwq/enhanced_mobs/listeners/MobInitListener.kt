package cn.yanshiqwq.enhanced_mobs.listeners

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.EnhancedMob.Companion.asEnhancedMob
import cn.yanshiqwq.enhanced_mobs.EnhancedMob.Companion.hasEnhancedMobData
import cn.yanshiqwq.enhanced_mobs.EnhancedMob.Companion.isEnhancedMob
import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.Utils.getNearestPlayer
import cn.yanshiqwq.enhanced_mobs.Utils.getTeam
import cn.yanshiqwq.enhanced_mobs.Utils.percentHeal
import cn.yanshiqwq.enhanced_mobs.data.LootTable.applyLootTableX
import cn.yanshiqwq.enhanced_mobs.managers.TypeManager
import cn.yanshiqwq.enhanced_mobs.managers.TypeManager.Companion.getRandomTypeKey
import cn.yanshiqwq.enhanced_mobs.script.Config
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.NamespacedKey
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.event.server.ServerLoadEvent
import org.bukkit.persistence.PersistentDataType

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
    fun onEnhancedMobReload(event: EntityTargetLivingEntityEvent){
        val entity = event.entity as? Mob ?: return
        entity.run {
            // 仅在有 EnhancedMob 数据且实体未标记于 manager 时继续执行
            if (isEnhancedMob()) return
            if (!hasEnhancedMobData()) {
                initEnhancedMob()
                return
            }

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

    private fun Mob.initEnhancedMob() {
        // 获取与玩家等级相关的乘数
        val playerLevelKey = NamespacedKey(instance!!, "nearest_player_level")
        val playerLevel = location.getNearestPlayer()?.level ?: 0
        persistentDataContainer.set(playerLevelKey, PersistentDataType.INTEGER, playerLevel)

        // 获取与难度相关的乘数
        val worldDifficultyMultiplier = (world.difficulty.ordinal + 1) / 4 // 和平=0.25, 简单=0.5, 普通=0.75, 困难=1

        // 获取最终乘数
        val multiplier = when (playerLevel) {
            in 0..10 -> -0.2 // 8 级
            in 10..20 -> 0.25 // 16 级
            in 20..30 -> 0.55 // 23 级
            in 30..40 -> 1.0 // 35 级
            in 40..50 -> 1.4 // 45 级
            in 50..60 -> 1.85 // 56 级
            in 60..80 -> 2.4 // 64 级
            in 80..90 -> 3.5 // 75 级
            in 90..Int.MAX_VALUE -> 4.2 // 82 级
            else -> 0.0
        } * worldDifficultyMultiplier

        // 设为 EnhancedMob
        val mainBoostTypeKey = Config.getMainTypeKey(type)
        val subBoostTypeKey = getRandomTypeKey(type, Config.getWeightMap())
        if (entitySpawnReason == CreatureSpawnEvent.SpawnReason.CUSTOM) return // 忽略 /enhancedmobs 生成的情况
        val mob = asEnhancedMob(multiplier, mainBoostTypeKey, subBoostTypeKey)
        instance!!.mobManager.register(uniqueId, mob)

        // 修改 LootTable
        applyLootTableX(multiplier)

        // 血量回满
        percentHeal(1.0)
    }
}