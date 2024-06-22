package cn.yanshiqwq.enhanced_mobs.data

import org.bukkit.entity.EntityType

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.data.Tags
 *
 * @author yanshiqwq
 * @since 2024/6/22 17:53
 */
object Tags {
    object Entity {
        val zombies = listOf(EntityType.ZOMBIE_VILLAGER, EntityType.ZOMBIE, EntityType.HUSK, EntityType.DROWNED)
        val skeletons = listOf(EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON)
        val spiders = listOf(EntityType.SPIDER, EntityType.CAVE_SPIDER)
        val creepers = listOf(EntityType.CREEPER)
    }
}