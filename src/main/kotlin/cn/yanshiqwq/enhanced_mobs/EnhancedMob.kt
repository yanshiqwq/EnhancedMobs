package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.config.ConfigV1
import cn.yanshiqwq.enhanced_mobs.event.EnhancedMobSpawnEvent
import cn.yanshiqwq.enhanced_mobs.manager.MobManager
import cn.yanshiqwq.enhanced_mobs.manager.MobTypeManager
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.Mob
import org.bukkit.persistence.PersistentDataType
import taboolib.module.chat.colored
import taboolib.platform.BukkitPlugin
import java.util.UUID

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.EnhancedMob
 *
 * @author yanshiqwq
 * @since 2024/8/21 下午10:34
 */

/**
 * 本插件特有的怪物实例
 */
class EnhancedMob(
    uuid: UUID,
    val type: EnhancedMobType,
    val level: Int
) {
    /**
     * 此 `EnhancedMob` 对应的怪物实体
     *
     * @return 若实体死亡或被移除则为null
     */
    val mob = Bukkit.getEntity(uuid) as Mob

    companion object {
        /**
         * 使用给定的怪物实体、类型和等级构造一个 `EnhancedMob`
         *
         * @param entity 代表怪物实体
         * @param type 怪物类型
         * @param level 怪物等级
         */
        fun build(entity: Mob, type: EnhancedMobType, level: Int) = EnhancedMob(entity.uniqueId, type, level).apply {
            // 持久化数据
            mob.persistentDataContainer.apply {
                set(MobDataKey.TYPE_ID, PersistentDataType.STRING, type.id)
                set(MobDataKey.LEVEL, PersistentDataType.INTEGER, level)
            }

            // 应用类型代码块
            type.block.invoke(mob)

            // 应用等级曲线
            ConfigV1.levelFormula.apply(mob, level)

            // 修改显示名称
            val color = when (level) {
                in 10..70 -> "&a"
                in 70..80 -> "&e"
                in 80..90 -> "&5"
                in 90..ConfigV1.levelRange.last -> "&c"
                else -> "&7"
            }
            mob.customName = ConfigV1.customName.format(color, level, mob.customName).colored()

            // 触发 EnhancedMob 生成事件
            EnhancedMobSpawnEvent(this, mob.location).call()
        }

        /**
         * 用于在持久数据容器中存储怪物类型ID的键
         */
        private val MobDataKey = object {
            val TYPE_ID = NamespacedKey(BukkitPlugin.getInstance(), "type_id")
            val LEVEL = NamespacedKey(BukkitPlugin.getInstance(), "level")
        }

        /**
         * 尝试将实体加载为 `EnhancedMob`
         * 用于插件重启后重新加载数据
         *
         * @param entity 需要加载的实体
         */
        fun tryLoad(entity: Entity) {
            val mob = entity as? Mob ?: return
            val container = mob.persistentDataContainer
            val id = container.get(MobDataKey.TYPE_ID, PersistentDataType.STRING) ?: return
            val type = MobTypeManager.get(id) ?: return
            val level = container.get(MobDataKey.LEVEL, PersistentDataType.INTEGER) ?: return
            MobManager.add(build(mob, type, level))
        }
    }
}