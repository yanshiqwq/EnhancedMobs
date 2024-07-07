package cn.yanshiqwq.enhanced_mobs.managers

import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.script.SubPack
import cn.yanshiqwq.enhanced_mobs.script.MainPack

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

    data class Pack(val id: String, val type: PackType, val typeMap: List<TypeManager.MobType> = listOf())
    enum class PackType {
        MAIN, SUB
    }

    private val packs = mutableListOf<Pack>()

    fun loadPacks() {
        register(MainPack.get())
        register(SubPack.get())
    }

    fun implement(pack: Pack, typeId: String): TypeManager.MobType {
        return pack.typeMap.first { it.typeKey.typeId == typeId }
    }

    private fun register(pack: Pack) {
        packs.add(pack)
        instance!!.logger.info("Registering pack: ${pack.id} (${pack.typeMap.size} types) ...")
        instance!!.typeManager.loadTypes(pack)
    }

    fun getPacks(type: PackType) = packs.filter { it.type == type }
    fun getPack(key: TypeManager.TypeKey) = packs.first { it.id == key.packId }
}
