package cn.yanshiqwq.enhanced_mobs.manager

import cn.yanshiqwq.enhanced_mobs.EnhancedMobType
import org.bukkit.entity.EntityType

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.manager.MobTypeManager
 *
 * @author yanshiqwq
 * @since 2024/8/22 下午5:04
 */
object MobTypeManager {
    private val types: HashSet<EnhancedMobType> = hashSetOf()
    fun register(type: EnhancedMobType) = types.add(type)
    fun register(type: HashSet<EnhancedMobType>) = types.addAll(type)
    fun unregister(type: EnhancedMobType) = types.remove(type)
    fun unregister(type: HashSet<EnhancedMobType>) = types.removeAll(type.toSet())
    fun get() = types
    fun get(id: String) = types.find { it.id == id }
    fun get(type: EntityType) = types.find { it.type == type }
}