package cn.yanshiqwq.enhanced_mobs.managers

import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.script.ExtendPack
import cn.yanshiqwq.enhanced_mobs.script.VanillaPack

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.managers.PackManager
 *
 * @author yanshiqwq
 * @since 2024/6/23 15:59
 */

class PackManager {
    interface PackObj {
        fun get(): Pack
    }

    data class Pack(val id: String, val typeMap: List<TypeManager.MobType> = listOf())

    private val packs = mutableListOf<Pack>()

    fun loadPacks() {
        register(VanillaPack.get())
        register(ExtendPack.get())
    }

    fun implement(pack: Pack, typeId: String): TypeManager.MobType {
        return pack.typeMap.first { it.typeKey.typeId == typeId }
    }

    private fun register(pack: Pack) {
        packs.add(pack)
        instance!!.logger.info("Registering pack: ${pack.id} (${pack.typeMap.size} types) ...")
        instance!!.typeManager.loadTypes(pack)
    }
}
