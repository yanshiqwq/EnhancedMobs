package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.entity.EntityType

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.WeightDslBuilder
 *
 * @author yanshiqwq
 * @since 2024/6/30 上午2:16
 */
class WeightDslBuilder {
    data class WeightMap(
        val types: List<EntityType>,
        val weight: Map<String, Int>
    )

    fun loadWeightMap(block: EntityPropertiesBuilder.() -> Unit): List<WeightMap> {
        val builder = EntityPropertiesBuilder()
        builder.block()
        return builder.properties.toList()
    }

    class EntityPropertiesBuilder {
        val properties = mutableListOf<WeightMap>()
        fun entity(vararg types: EntityType, block: MobWeights.() -> Unit) {
            val props = MobWeights()
            props.block()
            properties.add(WeightMap(types.asList(), props.weights))
        }
    }

    class MobWeights {
        val weights = mutableMapOf<String, Int>()

        fun weights(block: MutableMap<String, Int>.() -> Unit) {
            weights.block()
        }
    }
}