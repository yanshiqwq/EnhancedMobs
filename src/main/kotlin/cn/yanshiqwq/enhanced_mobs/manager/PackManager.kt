package cn.yanshiqwq.enhanced_mobs.manager

import cn.yanshiqwq.enhanced_mobs.EnhancedMobType
import cn.yanshiqwq.enhanced_mobs.Pack
import taboolib.common.platform.function.info

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
        info("Loading Pack \"${pack.id}\" ... (${pack.types.size} types)")
        packs.add(pack)
        pack.types.forEach {
            MobTypeManager.register(it)
            info("  - $it")
        }
        info("Pack \"${pack.id}\" loaded!")
    }
    fun unregister(pack: Pack) {
        packs.remove(pack)
        MobTypeManager.unregister(pack.types)
    }
    fun get(id: String) = packs.find { it.id == id }
    fun get(type: EnhancedMobType) = packs.find { it.types.contains(type) }
}