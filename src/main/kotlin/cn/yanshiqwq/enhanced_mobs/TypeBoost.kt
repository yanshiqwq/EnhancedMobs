package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.Ratio.Companion.creeper
import cn.yanshiqwq.enhanced_mobs.Ratio.Companion.skeleton
import cn.yanshiqwq.enhanced_mobs.Ratio.Companion.skeletonEnchant
import cn.yanshiqwq.enhanced_mobs.Ratio.Companion.skeletonVariant
import cn.yanshiqwq.enhanced_mobs.Ratio.Companion.spider
import cn.yanshiqwq.enhanced_mobs.Ratio.Companion.zombie
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.attribute.AttributeModifier.Operation
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.random.Random

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.TypeBoost
 *
 * @author yanshiqwq
 * @since 2024/6/2 09:28
 */
class TypeBoost {
    companion object {
        private const val attributeName = "EnhanceMobs Type Boost"
        private val uuid: UUID = UUID.fromString("1502d80e-3bf4-452f-adda-8d8af5b9bb2a")

        val randomList = arrayOf(
            EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK, EntityType.DROWNED, EntityType.ZOMBIFIED_PIGLIN,
            EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON,
            EntityType.CREEPER, EntityType.SPIDER, EntityType.CAVE_SPIDER
        )

        fun boost(entity: LivingEntity, multiplier: Double) {
            try {
                when (entity) {
                    is Zombie -> zombie.addAttributes(entity, multiplier)
                    is AbstractSkeleton ->
                        if (Random.nextDouble() >= 0.3) {
                            skeleton.addAttributes(entity, multiplier)
                            skeletonEnchant.addBoost(entity, multiplier)
                        } else {
                            skeletonVariant.addAttributes(entity, multiplier)
                            entity.equipment.setItemInMainHand(ItemStack(Material.IRON_SWORD))
                        }
                    is Spider -> spider.addAttributes(entity, multiplier)
                    is Creeper -> {
                        creeper.addAttributes(entity, multiplier)
                        creeper(entity, multiplier)
                    }
                }
            } catch (ignored: IllegalArgumentException) {}
        }

        fun distanceBoost(entity: LivingEntity, multiplier: Double) {
            try {
                when (entity) {
                    is Zombie -> zombie.addDistanceBoost(entity, multiplier)
                    is AbstractSkeleton ->
                        if (Random.nextDouble() >= 0.3) {
                            skeleton.addDistanceBoost(entity, multiplier)
                            skeletonEnchant.addBoost(entity, multiplier)
                        } else {
                            skeletonVariant.addDistanceBoost(entity, multiplier)
                            entity.equipment.setItemInMainHand(ItemStack(Material.IRON_SWORD))
                        }
                    is Spider -> spider.addDistanceBoost(entity, multiplier)
                    is Creeper -> {
                        creeper.addDistanceBoost(entity, multiplier)
                        creeper(entity, multiplier)
                    }
                }
            } catch (ignored: IllegalArgumentException) {}
        }

        fun customBoost(entity: LivingEntity, multiplier: Double) {
            try {
                when (entity) {
                    is Zombie -> zombie.addCustomBoost(entity, multiplier)
                    is AbstractSkeleton ->
                        if (Random.nextDouble() >= 0.3) {
                            skeleton.addCustomBoost(entity, multiplier)
                            skeletonEnchant.addBoost(entity, multiplier)
                        } else {
                            skeletonVariant.addCustomBoost(entity, multiplier)
                            entity.equipment.setItemInMainHand(ItemStack(Material.IRON_SWORD))
                        }
                    is Spider -> spider.addCustomBoost(entity, multiplier)
                    is Creeper -> {
                        creeper.addCustomBoost(entity, multiplier)
                        creeper(entity, multiplier)
                    }
                }
            } catch (ignored: IllegalArgumentException) {}
        }
        private fun creeper(entity: Creeper, multiplier: Double) {
            entity.run {
                maxFuseTicks = (-2 * multiplier + 30).toInt().coerceAtLeast(15)
                explosionRadius = (3 * multiplier + 3).toInt().coerceIn(0, 32)
            }
        }
        fun LivingEntity.addAttribute(attribute: Attribute, amount: Double, operation: Operation){
            this.getAttribute(attribute)!!.addModifier(AttributeModifier(uuid, attributeName, amount, operation))
        }
    }
}