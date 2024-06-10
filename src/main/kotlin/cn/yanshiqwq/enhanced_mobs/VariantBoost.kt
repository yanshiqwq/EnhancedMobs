package cn.yanshiqwq.enhanced_mobs

import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.*
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.VariantBoost
 *
 * @author yanshiqwq
 * @since 2024/6/2 20:29
 */
object VariantBoost {
    fun apply(entity: LivingEntity) {
        entity.apply {
            when (entity) {
                is Stray, is WitherSkeleton, is Husk, is Drowned -> {
                    getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 28.0
                    getAttribute(Attribute.GENERIC_ARMOR)?.baseValue = 4.0
                    getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.baseValue = 4.0
                    getAttribute(Attribute.GENERIC_FOLLOW_RANGE)?.baseValue = 24.0
                }

                is Creeper -> {
                    getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 24.0
                    getAttribute(Attribute.GENERIC_ARMOR)?.baseValue = 6.0
                    getAttribute(Attribute.GENERIC_FOLLOW_RANGE)?.baseValue = 24.0
                    getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE)?.baseValue = 0.35
                }

                is Giant -> {
                    getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 196.0
                    getAttribute(Attribute.GENERIC_ARMOR)?.baseValue = 6.0
                    getAttribute(Attribute.GENERIC_FOLLOW_RANGE)?.baseValue = 128.0
                    getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.baseValue = 0.35
                    getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.baseValue = 9.0
                    getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE)?.baseValue = 0.85
                    getAttribute(Attribute.ZOMBIE_SPAWN_REINFORCEMENTS)?.baseValue = 0.35
                    addPotionEffect(PotionEffect(PotionEffectType.JUMP, Int.MAX_VALUE, 3, true, false))
                }

                is PiglinAbstract -> {
                    getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 24.0
                    getAttribute(Attribute.GENERIC_ARMOR)?.baseValue = 6.0
                    getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE)?.baseValue = 0.35
                    entity.isImmuneToZombification = true
                    if (entity is PiglinBrute) getAttribute(Attribute.GENERIC_MAX_HEALTH)?.addModifier(
                        AttributeModifier(
                            UUID.fromString("ac610ed8-7a70-4eb7-a9c9-caff4f249a3e"),
                            "Piglin brute boost",
                            1.0,
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1
                        )
                    )
                }
            }
        }
    }
}