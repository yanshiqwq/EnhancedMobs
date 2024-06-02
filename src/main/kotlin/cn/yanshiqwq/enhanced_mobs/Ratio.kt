package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.TypeBoost.Companion.addAttribute
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.Attribute.*
import org.bukkit.attribute.AttributeModifier
import org.bukkit.attribute.AttributeModifier.Operation
import org.bukkit.attribute.AttributeModifier.Operation.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.Enchantment.*
import org.bukkit.entity.LivingEntity
import java.util.*

/**
 * enhanced_mobs
 * .MonsterRatio
 *
 * @author yanshiqwq
 * @since 2024/6/3 01:45
 */

class Ratio {
    companion object {
        data class Record(val attribute: Map<Attribute, Triple<Double, Double, Operation>>){
            fun addAttributes(entity: LivingEntity, multiplier: Double){
                attribute.forEach {
                    entity.addAttribute(it.key, it.value.first * multiplier + it.value.second, it.value.third)
                }
            }
            fun addDistanceBoost(entity: LivingEntity, multiplier: Double) {
                addScalar(entity, multiplier, UUID.fromString("a7dda658-a797-4f8b-aa3b-6c5e3cecfcca"), "Distance Boost")
            }
            fun addCustomBoost(entity: LivingEntity, multiplier: Double) {
                addScalar(entity, multiplier, UUID.fromString("f315fc78-28e4-40bc-bae3-fb3680766008"), "Custom Boost")
            }
            private fun addScalar(entity: LivingEntity, multiplier: Double, uuid: UUID, name: String) {
                attribute.forEach {
                    entity.getAttribute(it.key)!!
                        .addModifier(AttributeModifier(
                            uuid, name, it.value.first * multiplier, ADD_SCALAR
                        ))
                }
            }
        }
        data class RecordEnchant(val enchant: Map<Enchantment, Pair<Double, Double>>){
            fun addBoost(entity: LivingEntity, multiplier: Double) {
                enchant.forEach {
                    if (entity.equipment == null) return
                    val level = (entity.equipment!!.itemInMainHand.enchantments[it.key] ?: 0) + (it.value.first * multiplier - it.value.second).toInt()
                    if (level < 1) return
                    entity.equipment!!.itemInMainHand.itemMeta.addEnchant(it.key, level, true)
                }
            }
        }
        val zombie = Record(mapOf(
            GENERIC_MAX_HEALTH to Triple(1.2, 0.2, MULTIPLY_SCALAR_1),
            GENERIC_MOVEMENT_SPEED to Triple(0.05, 0.1, MULTIPLY_SCALAR_1),
            GENERIC_ATTACK_DAMAGE to Triple(1.0, 1.0, MULTIPLY_SCALAR_1),
            GENERIC_KNOCKBACK_RESISTANCE to Triple(0.15, -0.15, ADD_NUMBER),
            GENERIC_FOLLOW_RANGE to Triple(0.175, 0.0, MULTIPLY_SCALAR_1)
        ))
        val skeleton = Record(mapOf(
            GENERIC_MAX_HEALTH to Triple(0.5, 0.6, MULTIPLY_SCALAR_1),
            GENERIC_MOVEMENT_SPEED to Triple(0.1, 0.08, MULTIPLY_SCALAR_1)
        ))
        val skeletonVariant = Record(mapOf(
            GENERIC_MAX_HEALTH to Triple(0.5, 0.6, MULTIPLY_SCALAR_1),
            GENERIC_MOVEMENT_SPEED to Triple(0.1, 0.08, MULTIPLY_SCALAR_1),
            GENERIC_ATTACK_DAMAGE to Triple(1.0, -0.5, MULTIPLY_SCALAR_1)
        ))
        val skeletonEnchant = RecordEnchant(mapOf(
            ARROW_DAMAGE to Pair(2.5, 0.0),
            ARROW_KNOCKBACK to Pair(0.5, 0.0),
            ARROW_FIRE to Pair(0.5, 0.0)
        ))
        val spider = Record(mapOf(
            GENERIC_MAX_HEALTH to Triple(2.5, -1.5, MULTIPLY_SCALAR_1),
            GENERIC_MOVEMENT_SPEED to Triple(0.07, 0.21, MULTIPLY_SCALAR_1),
            GENERIC_ATTACK_DAMAGE to Triple(0.8, 1.2, MULTIPLY_SCALAR_1)
        ))
        val creeper = Record(mapOf(
            GENERIC_MAX_HEALTH to Triple(0.5, 0.6, MULTIPLY_SCALAR_1),
            GENERIC_MOVEMENT_SPEED to Triple(0.07, 0.21, MULTIPLY_SCALAR_1),
            GENERIC_KNOCKBACK_RESISTANCE to Triple(0.2, -0.05, MULTIPLY_SCALAR_1)
        ))
    }
}