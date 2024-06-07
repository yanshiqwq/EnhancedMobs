package cn.yanshiqwq.enhanced_mobs

import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EquipmentSlot
import kotlin.math.ln

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.EnhancedMobs
 *
 * @author yanshiqwq
 * @since 2024/6/8 07:01
 */
class MobManager {
    private val map: MutableMap<String, (EnhancedMob) -> Unit> = mutableMapOf()
    fun register(id: String, func: (EnhancedMob) -> Unit){
        map[id] = func
    }
    fun create(id: String, multiplier: Double, entity: LivingEntity){
        map[id]?.let { it(EnhancedMob(multiplier, entity)) }
    }
    fun query(id: String): ((EnhancedMob) -> Unit)? {
        return map[id]
    }
    fun list(): MutableSet<String> {
        return map.keys
    }
}

class Mobs {
    companion object {
        val logFormula: (Double) -> (Double) -> Double = { scale ->
            {
                when (it) {
                    in 0.0..Double.MAX_VALUE -> scale * ln(it + 1.0)
                    else -> it
                }
            }
        }
        fun zombie(entity: EnhancedMob){
            entity.apply {
                initAttribute(AttributeRecord(mapOf(
                    Attribute.GENERIC_MAX_HEALTH to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor({1.4 * it})),
                    Attribute.GENERIC_MOVEMENT_SPEED to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(0.25))),
                    Attribute.GENERIC_ARMOR to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor({0.35 * it}, -1.0..5.0)),
                    Attribute.GENERIC_ATTACK_DAMAGE to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(1.0))),
                    Attribute.GENERIC_KNOCKBACK_RESISTANCE to AttributeRecordFactor(AttributeModifier.Operation.ADD_NUMBER, DoubleRecordFactor({0.065 * it})),
                    Attribute.GENERIC_FOLLOW_RANGE to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor({0.15 * it}))
                )))
            }
        }

        fun skeleton(entity: EnhancedMob){
            entity.apply {
                initAttribute(AttributeRecord(mapOf(
                    Attribute.GENERIC_MAX_HEALTH to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor({0.65 * it})),
                    Attribute.GENERIC_ATTACK_DAMAGE to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(3.0))),
                    Attribute.GENERIC_MOVEMENT_SPEED to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(0.25)))
                )))
                initEnchant(
                    EquipmentSlot.HAND, EnchantRecord(mapOf(
                        Enchantment.ARROW_KNOCKBACK to IntRecordFactor({0.5 * it}),
                        Enchantment.ARROW_FIRE to IntRecordFactor({0.5 * it})
                    )))
            }
        }

        fun skeletonVariant(entity: EnhancedMob){
            entity.apply {
                initAttribute(AttributeRecord(mapOf(
                    Attribute.GENERIC_MAX_HEALTH to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor({0.4 * it})),
                    Attribute.GENERIC_MOVEMENT_SPEED to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(0.25))),
                    Attribute.GENERIC_ATTACK_DAMAGE to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(1.0)))
                )))
                initEquipment(Material.IRON_SWORD, EquipmentSlot.HAND)
            }
        }

        fun spider(entity: EnhancedMob){
            entity.apply {
                initAttribute(AttributeRecord(mapOf(
                    Attribute.GENERIC_MAX_HEALTH to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor({2.0 * it})),
                    Attribute.GENERIC_MOVEMENT_SPEED to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(0.25))),
                    Attribute.GENERIC_ATTACK_DAMAGE to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(1.0)))
                )))
            }
        }

        fun creeper(entity: EnhancedMob){
            entity.apply {
                initAttribute(AttributeRecord(mapOf(
                    Attribute.GENERIC_MAX_HEALTH to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor({0.35 * it})),
                    Attribute.GENERIC_MOVEMENT_SPEED to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(0.25))),
                    Attribute.GENERIC_KNOCKBACK_RESISTANCE to AttributeRecordFactor(AttributeModifier.Operation.ADD_NUMBER, DoubleRecordFactor({0.065 * it}))
                )))
            }
            val multiplier = entity.multiplier
            (entity as org.bukkit.entity.Creeper).apply {
                maxFuseTicks = IntRecordFactor({-2 * it + 30}, 15..32767).value(multiplier)
                explosionRadius = IntRecordFactor({3 * it + 3}, 0..32).value(multiplier)
            }
        }

        fun generic(entity: EnhancedMob){
            entity.apply {
                initAttribute(AttributeRecord(mapOf(
                    Attribute.GENERIC_MAX_HEALTH to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor({it})),
                    Attribute.GENERIC_MOVEMENT_SPEED to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(0.25))),
                    Attribute.GENERIC_ATTACK_DAMAGE to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(1.0)))
                )))
            }
        }
    }
}