package cn.yanshiqwq.enhanced_mobs.manager

import cn.yanshiqwq.enhanced_mobs.EnhancedMobType
import cn.yanshiqwq.enhanced_mobs.Pack

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.manager.PackManager
 *
 * @author yanshiqwq
 * @since 2024/8/22 下午5:04
 */
object PackManager {
    private val packs: HashSet<Pack> = hashSetOf()
    fun register(pack: Pack) {
        packs.add(pack)
        MobTypeManager.register(pack.types)
    }
    fun unregister(pack: Pack) {
        packs.remove(pack)
        MobTypeManager.unregister(pack.types)
    }
    fun get(id: String) = packs.find { it.id == id }
    fun get(type: EnhancedMobType) = packs.find { it.types.contains(type) }
}