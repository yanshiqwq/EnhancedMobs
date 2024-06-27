package cn.yanshiqwq.enhanced_mobs.data

import cn.yanshiqwq.enhanced_mobs.Utils.addModifierSafe
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EquipmentSlot
import java.util.*
import kotlin.math.floor
import kotlin.math.ln

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.data.Record
 *
 * @author yanshiqwq
 * @since 2024/6/8 07:02
 */

object Record {
    interface Factor {
        fun value(base: Double): Number
    }

    fun logFormula(scale: Double, range: ClosedFloatingPointRange<Double>? = null) = DoubleFactor(range) {
        if (it in 0.0..Double.MAX_VALUE) scale * ln(it + 1.0)
        else it
    }

    data class DoubleFactor(private val range: ClosedFloatingPointRange<Double>? = null, val formula: (Double) -> Double) : Factor {
        override fun value(base: Double): Double {
            return if (range == null)
                formula(base)
            else
                formula(base).coerceIn(range)
        }
        fun asIntFactor(): IntFactor {
            return if (range == null)
                IntFactor(formula = formula)
            else
                IntFactor(range.start.toInt()..range.endInclusive.toInt(), formula)
        }
    }

    data class IntFactor(private val range: IntRange? = null, val formula: (Double) -> Double) : Factor {
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

    data class AttributeRecord(val attribute: Attribute, val factor: AttributeFactor) {
        fun apply(entity: LivingEntity, multiplier: Double, uuid: UUID, name: String) {
            entity.getAttribute(attribute)?.addModifierSafe(factor.getModifier(multiplier, uuid, name))
        }
    }

    data class EnchantRecord(val enchant: Enchantment, val factor: IntFactor) {
        fun apply(entity: LivingEntity, multiplier: Double, slot: EquipmentSlot) {
            val equipment = entity.equipment ?: return
            val item = equipment.getItem(slot)
            if (!enchant.canEnchantItem(item)) return

            val base = item.itemMeta.enchants[enchant] ?: 0
            val level = base + factor.value(multiplier)
            if (level < 1) return

            val meta = item.itemMeta
            meta.addEnchant(enchant, level, true)
            item.itemMeta = meta
            equipment.setItem(slot, item)
        }
    }
}