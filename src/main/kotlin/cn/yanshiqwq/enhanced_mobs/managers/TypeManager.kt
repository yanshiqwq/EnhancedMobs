package cn.yanshiqwq.enhanced_mobs.managers

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.dsl.WeightDslBuilder
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
        fun getRandomTypeKey(entityType: EntityType, weightMapList: List<WeightDslBuilder.WeightMap>): TypeKey {
            val weightMap = weightMapList.filter { entityType in it.types }.map { it.weight }
            if (weightMap.isEmpty()) throw IllegalArgumentException("MatchedKeys is empty")

            // 计算总权重
            val totalWeight = weightMap.sumOf { it.values.sum() }

            // 随机选择一个 id
            val rand = Random.nextInt(totalWeight)
            val mergedMap = weightMap
                .flatMap { it.entries }
                .associate { it.key to it.value }

            return selectWeightedKey(mergedMap, rand)
        }

        private fun selectWeightedKey(weights: Map<String, Int>, rand: Int): TypeKey {
            var cumulativeWeight = 0
            for ((key, weight) in weights) {
                cumulativeWeight += weight
                if (rand <= cumulativeWeight) {
                    return TypeKey(key)
                }
            }
            return TypeKey("vanilla", "fallback")
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