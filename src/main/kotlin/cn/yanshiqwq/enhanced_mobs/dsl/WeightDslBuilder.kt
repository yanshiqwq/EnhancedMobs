package cn.yanshiqwq.enhanced_mobs.dsl

import cn.yanshiqwq.enhanced_mobs.Utils.chance
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
    class WeightedRandom<T> private constructor(private var items: MutableMap<T, Int>) {
        companion object {
            fun <T> create(items: Map<T, Int>): WeightedRandom<T> = WeightedRandom(items.toMutableMap())
            fun <T> create(): WeightedRandom<T> = WeightedRandom(mutableMapOf())
        }

        private var totalWeight: Int = calculateTotalWeight()

        private fun calculateTotalWeight(): Int {
            return items.toList().sumOf { it.second }
        }

        fun addItem(item: T, weight: Int) {
            items[item] = weight
            totalWeight += weight
        }

        fun nextItem(): T {
            if (items.isEmpty()) throw IllegalStateException("Cannot select from an empty list")

            var randomValue = Random.nextInt(totalWeight) + 1
            for ((item, weight) in items) {
                randomValue -= weight
                if (randomValue <= 0) {
                    return item
                }
            }

            throw IllegalStateException("Should never reach here")
        }

         fun map() = items.toMap()
    }


    data class WeightMapGroup(
        val types: List<EntityType>,
        val weightList: WeightedRandom<String>
    )

    fun loadWeightMap(block: EntityPropertiesBuilder.() -> Unit): List<WeightMapGroup> {
        val builder = EntityPropertiesBuilder()
        builder.block()
        return builder.properties.toList()
    }

    class EntityPropertiesBuilder {
        val properties = mutableListOf<WeightMapGroup>()
        fun entity(types: List<EntityType>, items: Map<String, Int>, chance: Double = 0.08) =
            if (chance(chance))
                properties.add(WeightMapGroup(types, WeightedRandom.create(items)))
            else null
    }
}