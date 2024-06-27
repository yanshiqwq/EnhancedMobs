package cn.yanshiqwq.enhanced_mobs.managers

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import org.bukkit.entity.EntityType
import kotlin.random.Random

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.managers.MobManager
 *
 * @author yanshiqwq
 * @since 2024/6/8 15:39
 */

class TypeManager {
    companion object {
        fun getRandomTypeKey(type: EntityType): TypeKey? {
            val map = instance!!.weightManager.getWeightMap()
            val mobTypeList = map.keys.flatten()
            if (!mobTypeList.contains(type)) return null
            val weightMap = map.values
                .flatMap { it.entries }
                .associate { it.toPair() }
                .toMutableMap()
            val totalWeight = weightMap.values.sum()
            var randomValue = Random.nextInt(totalWeight)

            for ((typeId, weight) in weightMap) {
                randomValue -= weight
                if (randomValue < 0) {
                    return typeId
                }
            }

            return TypeKey("vanilla", "generic")
        }
    }

    data class MobType(val typeKey: TypeKey, val function: EnhancedMob.() -> Unit)

    data class TypeKey(val packId: String, val typeId: String) {
        constructor(id: String) : this(id.split(".")[0], id.split(".")[1])

        fun value(): String {
            return "$packId.$typeId"
        }
    }

    private val typeMap: MutableList<MobType> = mutableListOf()
    fun loadTypes(pack: PackManager.Pack) {
        pack.typeMap.forEach { (typeKey, function) ->
            instance!!.logger.info("\tLoading mobType: ${typeKey.typeId}")
            typeMap.add(MobType(typeKey, function))
        }
    }

    fun hasTypeId(key: TypeKey): Boolean = !typeMap.none { it.typeKey == key }

    fun getType(key: TypeKey) = typeMap.first { it.typeKey == key }

    fun listTypeKeys(): List<TypeKey> = typeMap.map { it.typeKey }
}