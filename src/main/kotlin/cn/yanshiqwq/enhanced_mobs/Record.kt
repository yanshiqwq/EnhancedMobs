package cn.yanshiqwq.enhanced_mobs

import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EquipmentSlot
import java.util.*
import kotlin.math.floor

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.Record
 *
 * @author yanshiqwq
 * @since 2024/6/8 07:02
 */

object Record {
    interface Factor {
        fun value(base: Double): Number
    }

    data class DoubleFactor(
        val formula: (Double) -> Double,
        private val range: ClosedFloatingPointRange<Double>? = null
    ) : Factor {
        override fun value(base: Double): Double {
            return if (range == null) formula(base) else formula(base).coerceIn(range)
        }
    }

    data class IntFactor(val formula: (Double) -> Double, private val range: IntRange? = null) : Factor {
        override fun value(base: Double): Int {
            return if (range == null) floor(formula(base)).toInt() else floor(formula(base)).toInt().coerceIn(range)
        }
    }

    data class AttributeFactor(val operation: AttributeModifier.Operation, val factor: DoubleFactor) {
        fun getModifier(multiplier: Double, uuid: UUID, name: String): AttributeModifier {
            val amount = factor.value(multiplier)
            return AttributeModifier(uuid, name, amount, operation)
        }
    }

    data class AttributeRecord(val attribute: Map<Attribute, AttributeFactor>) {
        fun apply(entity: LivingEntity, multiplier: Double, uuid: UUID, name: String) {
            attribute.forEach {
                val attribute = it.key
                val factor = it.value
                entity.getAttribute(attribute)!!.addModifier(factor.getModifier(multiplier, uuid, name))
            }
        }
    }

    data class EnchantRecord(val enchant: Map<Enchantment, IntFactor>) {
        fun apply(entity: LivingEntity, multiplier: Double, slot: EquipmentSlot) {
            enchant.forEach {
                val enchant = it.key
                val equipment = entity.equipment ?: return
                val item = equipment.getItem(slot)
                if (!enchant.canEnchantItem(item)) return

                val base = item.itemMeta.enchants[enchant] ?: 0
                val factor = it.value
                val level = base + factor.value(multiplier)
                if (level < 1) return

                val meta = item.itemMeta
                meta.addEnchant(enchant, level, true)
                item.itemMeta = meta
                equipment.setItem(slot, item)
            }
        }
    }
}