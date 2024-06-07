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
                    is Zombie -> Main.mobManager!!.create("ZOMBIE", multiplier, entity)
                    is WitherSkeleton, is AbstractSkeleton -> {
                        if (Random.nextDouble() >= 0.3) Main.mobManager!!.create("SKELETON", multiplier, entity)
                        else Main.mobManager!!.create("SKElETON_VARIANT", multiplier, entity)
                    }
                    is Spider -> Main.mobManager!!.create("SPIDER", multiplier, entity)
                    is Creeper -> Main.mobManager!!.create("CREEPER", multiplier, entity)
                    else -> Main.mobManager!!.create("GENERIC", multiplier, entity)
                }
            } catch (ignored: IllegalArgumentException) {}
        }
    }
}