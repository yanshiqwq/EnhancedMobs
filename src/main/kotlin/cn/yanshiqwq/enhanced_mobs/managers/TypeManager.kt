package cn.yanshiqwq.enhanced_mobs.managers

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.dsl.WeightDslBuilder
import org.bukkit.entity.EntityType

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.managers.MobManager
 *
 * @author yanshiqwq
 * @since 2024/6/8 15:39
 */
class TypeManager {
    companion object {
        fun getRandomTypeKey(entityType: EntityType, weightMapGroupList: List<WeightDslBuilder.WeightMapGroup>): TypeKey? {
            val weightMap = weightMapGroupList
                .filter { entityType in it.types }
                .ifEmpty { return null }
                .flatMap { it.weightList.toMap().entries }
                .associate { it.value to it.key }
            val weightList = WeightDslBuilder.WeightList(weightMap)
            return TypeKey(weightList.getRandomByWeightList())
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
            instance!!.logger.info("- Loading mobType: ${typeKey.value()}")
            typeMap.add(MobType(typeKey, function))
        }
    }

    fun hasTypeId(key: TypeKey): Boolean = !typeMap.none { it.typeKey == key }

    fun getType(key: TypeKey): MobType =
        typeMap.firstOrNull { it.typeKey == key }
            ?: typeMap.first { it.typeKey == TypeKey("vanilla", "fallback") }

    fun listTypeKeys(): List<TypeKey> = typeMap.map { it.typeKey }
}