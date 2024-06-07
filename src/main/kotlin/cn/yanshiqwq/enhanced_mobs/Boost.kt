package cn.yanshiqwq.enhanced_mobs

import org.bukkit.entity.*
import java.lang.IllegalArgumentException
import kotlin.random.Random

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.Boost
 *
 * @author yanshiqwq
 * @since 2024/6/2 09:28
 */
class Boost {
    companion object {
        val randomList = arrayOf(
            EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK, EntityType.DROWNED, EntityType.ZOMBIFIED_PIGLIN, EntityType.GIANT,
            EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON,
            EntityType.CREEPER, EntityType.SPIDER, EntityType.CAVE_SPIDER
        )
        fun applyBoost(entity: LivingEntity, multiplier: Double) {
            try {
                when (entity) {
                    is Zombie -> Mobs.Zombie(multiplier, entity)
                    is WitherSkeleton, is AbstractSkeleton -> {
                        if (Random.nextDouble() >= 0.3) Mobs.Skeleton(multiplier, entity)
                        else Mobs.SkeletonVariant(multiplier, entity)
                    }
                    is Spider -> Mobs.Spider(multiplier, entity)
                    is Creeper -> Mobs.Creeper(multiplier, entity)
                    else -> Mobs.Generic(multiplier, entity)
                }
            } catch (ignored: IllegalArgumentException) {}
        }
    }
}