package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.Main.Companion.logger
import org.bukkit.entity.Mob

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.MobManager
 *
 * @author yanshiqwq
 * @since 2024/6/8 15:39
 */

data class TypeId(val pack: String, val mob: String) {
    constructor(id: String) : this(id.split(".")[0], id.split(".")[1])

    fun value(): String {
        return "$pack.$mob"
    }
}

class MobTypeManager {
    private val packMap: MutableMap<String, Set<String>> = mutableMapOf()
    private val typeMap: MutableMap<TypeId, (EnhancedMob) -> Unit> = mutableMapOf()
    fun loadPacks(vararg packs: Pack) {
        for (pack in packs) {
            logger.info("Loading pack: ${pack.id}")
            packMap[pack.id] = pack.typeMap.keys
            for (mob in pack.typeMap) {
                logger.info("   Loading mobType: ${mob.key}")
                typeMap[TypeId(pack.id, mob.key)] = mob.value
            }
        }
    }

    fun create(id: TypeId, multiplier: Double, entity: Mob) {
        typeMap[id]?.let { it(EnhancedMob(multiplier, entity)) }
    }

    fun queryTypeFunc(id: TypeId): ((EnhancedMob) -> Unit)? {
        return typeMap[id]
    }

    fun listPackIds(): MutableSet<String> {
        return packMap.keys
    }

    fun listTypeIds(packId: String): ArrayList<TypeId> {
        val typeNames = packMap[packId]?.toTypedArray() ?: return arrayListOf()
        val typeIds = ArrayList<TypeId>()
        typeNames.forEach {
            typeIds.add(TypeId(packId, it))
        }
        return typeIds
    }
}