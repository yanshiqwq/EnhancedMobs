package cn.yanshiqwq.enhanced_mobs.managers

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import org.bukkit.entity.Mob
import java.util.*

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.EnhancedMobManager
 *
 * @author yanshiqwq
 * @since 2024/6/8 15:40
 */

@Suppress("unused", "unused")
class MobManager {
    private val map: MutableMap<UUID, EnhancedMob> = mutableMapOf()

    fun map(): MutableMap<UUID, EnhancedMob> = map

    fun register(uuid: UUID, entity: EnhancedMob) = map.put(uuid, entity)
    fun remove(uuid: UUID) = map.remove(uuid)

    fun get(uuid: UUID): EnhancedMob? = map[uuid]
    fun get(entity: Mob): EnhancedMob? = map.values.find { it.entity == entity }

    fun has(uuid: UUID): Boolean = get(uuid) != null
    fun has(entity: Mob): Boolean = get(entity) != null
}