package cn.yanshiqwq.enhanced_mobs.script

import cn.yanshiqwq.enhanced_mobs.dsl.WeightDslBuilder
import org.bukkit.entity.EntityType

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.script.Config
 *
 * @author yanshiqwq
 * @since 2024/6/30 下午4:17
 */
object Config {
    fun getWeightMapList(): List<WeightDslBuilder.WeightMapGroup> = WeightDslBuilder().loadWeightMap {
        entity(EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK, EntityType.DROWNED, EntityType.ZOMBIFIED_PIGLIN) {
            weight (
                "vanilla.zombie" to 80,
                "extend.zombie_strength_cloud" to 2,
                "extend.zombie_totem" to 2,
                "extend.zombie_tnt" to 2,
                "extend.zombie_lava" to 2,
                "extend.zombie_flint_and_steel" to 2,
                "extend.zombie_ender_pearl" to 2,
                "extend.zombie_shield" to 2
            )
        }
        entity(EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON) {
            weight (
                "vanilla.skeleton" to 80,
                "extend.skeleton_iron_sword" to 10
            )
        }
        entity(EntityType.SPIDER, EntityType.CAVE_SPIDER) {
            weight (
                "vanilla.spider" to 80
            )
        }
        entity(EntityType.CREEPER) {
            weight (
                "vanilla.creeper" to 80
            )
        }
    }
}