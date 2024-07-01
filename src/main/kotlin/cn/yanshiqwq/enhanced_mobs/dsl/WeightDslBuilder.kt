package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.entity.EntityType
import kotlin.random.Random

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.WeightDslBuilder
 *
 * @author yanshiqwq
 * @since 2024/6/30 上午2:16
 */
class WeightDslBuilder {
    class WeightList<T : Any>(private val weights: MutableList<Weight<T>> = mutableListOf()) {
        constructor(map: Map<T, Int>): this(map.entries.map { Weight<T>(it.key, it.value) }.toMutableList())

        data class Weight<T>(val key: T, val weight: Int)
        fun weight(key: T, weight: Int) = weights.add(Weight(key, weight))
        fun weight(vararg entry: Pair<T, Int>) = entry.toMap().forEach {
            weight(it.key, it.value)
        }

        fun getRandomByWeightList(): T {
            val totalWeight = weights.sumOf { it.weight }
            if (totalWeight <= 0) throw IllegalStateException("totalWeight must be positive")
            val rand = Random.nextInt(totalWeight)
            var cumulativeWeight = 0
            for ((key, weight) in weights) {
                cumulativeWeight += weight
                if (rand <= cumulativeWeight) {
                    return key
                }
            }
            throw IllegalStateException("Unknown error in getRandomByWeightList()")
        }

        fun toMap() = weights.associate { it.weight to it.key }
    }

    data class WeightMapGroup(
        val types: List<EntityType>,
        val weightList: WeightList<String>
    )

    fun loadWeightMap(block: EntityPropertiesBuilder.() -> Unit): List<WeightMapGroup> {
        val builder = EntityPropertiesBuilder()
        builder.block()
        return builder.properties.toList()
    }

    class EntityPropertiesBuilder {
        val properties = mutableListOf<WeightMapGroup>()
        fun entity(vararg types: EntityType, block: WeightList<String>.() -> Unit) {
            val props = WeightList<String>()
            props.block()
            properties.add(WeightMapGroup(types.asList(), props))
        }
    }
}