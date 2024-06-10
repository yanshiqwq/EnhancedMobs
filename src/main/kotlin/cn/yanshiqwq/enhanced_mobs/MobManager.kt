package cn.yanshiqwq.enhanced_mobs

import org.bukkit.entity.Mob
import java.util.*

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.EnhancedMobManager
 *
 * @author yanshiqwq
 * @since 2024/6/8 15:40
 */

class MobManager() {
    private val map: MutableMap<UUID, EnhancedMob> = mutableMapOf()

    fun register(uuid: UUID, entity: EnhancedMob){
        map[uuid] = entity
    }
    fun remove(uuid: UUID){
        map.remove(uuid)
    }
    fun map(): MutableMap<UUID, EnhancedMob> {
        return map
    }
    fun get(uuid: UUID): EnhancedMob? {
        return map[uuid]
    }
    fun get(entity: Mob): EnhancedMob? {
        map.values.forEach {
            if (it.entity == entity) return it
        }
        return null
    }
}