package cn.yanshiqwq.enhanced_mobs

import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.VariantBoost
 *
 * @author yanshiqwq
 * @since 2024/6/2 20:29
 */
class VariantBoost {
    companion object {
        fun stray(entity: Stray) {
            entity.run {
                getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue = 28.0
                getAttribute(Attribute.GENERIC_ARMOR)!!.baseValue = 6.0
                getAttribute(Attribute.GENERIC_FOLLOW_RANGE)!!.baseValue = 24.0
            }
        }
        fun husk(entity: Husk) {
            entity.run {
                getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue = 28.0
                getAttribute(Attribute.GENERIC_ARMOR)!!.baseValue = 6.0
                getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.baseValue = 4.0
            }
        }
        fun drowned(entity: Drowned) {
            entity.run {
                getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue = 28.0
                getAttribute(Attribute.GENERIC_ARMOR)!!.baseValue = 6.0
                getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.baseValue = 4.0
            }
        }

        fun poweredCreeper(entity: Creeper) {
            entity.run {
                getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue = 24.0
                getAttribute(Attribute.GENERIC_ARMOR)!!.baseValue = 6.0
                getAttribute(Attribute.GENERIC_FOLLOW_RANGE)!!.baseValue = 24.0
                getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE)!!.baseValue = 0.35
            }
        }

        fun giant(entity: Giant) {
            entity.run {
                getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue = 196.0
                getAttribute(Attribute.GENERIC_ARMOR)!!.baseValue = 6.0
                getAttribute(Attribute.GENERIC_FOLLOW_RANGE)!!.baseValue = 128.0
                getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)!!.baseValue = 0.35
                getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.baseValue = 9.0
                getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE)!!.baseValue = 0.85
                getAttribute(Attribute.ZOMBIE_SPAWN_REINFORCEMENTS)!!.baseValue = 0.35
                addPotionEffect(PotionEffect(PotionEffectType.JUMP, Int.MAX_VALUE, 3, true, false))
            }
        }

        fun piglin(entity: PiglinAbstract) {
            entity.run {
                getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue = 24.0
                getAttribute(Attribute.GENERIC_ARMOR)!!.baseValue = 6.0
                getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE)!!.baseValue = 0.35
                isImmuneToZombification = true
            }
        }
    }
}