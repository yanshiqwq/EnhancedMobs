package cn.yanshiqwq.enhanced_mobs.manager

import cn.yanshiqwq.enhanced_mobs.EnhancedMobType
import org.bukkit.entity.EntityType

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.manager.MobTypeManager
 *
 * @author yanshiqwq
 * @since 2024/8/22 下午 5:04
 */
object MobTypeManager: HashMapManager<String, EnhancedMobType>() {
    fun get(type: EntityType) = entries.values.first { it.type == type }
}