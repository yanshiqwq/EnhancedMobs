package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.Main.Companion.INSTANCE
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.Attribute.*
import org.bukkit.attribute.AttributeModifier
import org.bukkit.attribute.AttributeModifier.Operation
import org.bukkit.attribute.AttributeModifier.Operation.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*
import kotlin.math.floor
import kotlin.math.ln


/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.EnhancedMob
 *
 * @author yanshiqwq
 * @since 2024/6/7 05:29
 */

class Arrow: Listener {
    @EventHandler
    fun onArrowDamage(event: EntityDamageByEntityEvent) {
        if (event.damager is Arrow) {
            val arrow = event.damager as Arrow
            val damager = arrow.shooter as LivingEntity
            if (damager is Player) return
            val level = damager.equipment?.itemInMainHand?.enchantments?.get(Enchantment.ARROW_DAMAGE) ?: 0
            event.damage = (damager.getAttribute(GENERIC_ATTACK_DAMAGE)?.value ?: 0.0) * (1 + level * 0.25)
        }
    }
}

interface RecordFactor {
    fun value(base: Double): Number
}
data class DoubleRecordFactor(val formula: (Double) -> Double, private val range: ClosedFloatingPointRange<Double>? = null): RecordFactor {
    override fun value(base: Double): Double {
        return if (range == null) formula(base) else formula(base).coerceIn(range)
    }
}
data class IntRecordFactor(val formula: (Double) -> Double, private val range: IntRange? = null): RecordFactor {
    override fun value(base: Double): Int {
        return if (range == null) floor(formula(base)).toInt() else floor(formula(base)).toInt().coerceIn(range)
    }
}

data class AttributeRecordFactor(val operation: Operation, val factor: DoubleRecordFactor){
    fun getModifier(multiplier: Double, uuid: UUID, name: String): AttributeModifier {
        val amount = factor.value(multiplier)
        return AttributeModifier(uuid, name, amount, operation)
    }
}
data class AttributeRecord(val attribute: Map<Attribute, AttributeRecordFactor>){
    fun apply(entity: LivingEntity, multiplier: Double, uuid: UUID, name: String){
        attribute.forEach {
            val attribute = it.key
            val factor = it.value
            entity.getAttribute(attribute)!!.addModifier(factor.getModifier(multiplier, uuid, name))
        }
    }
}

data class EnchantRecord(val enchant: Map<Enchantment, IntRecordFactor>){
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

abstract class EnhancedMob(private val multiplier: Double, val entity: LivingEntity){
    companion object {
        val key = NamespacedKey(INSTANCE!!, "multiplier")
    }
    init {
        entity.persistentDataContainer.set(key, PersistentDataType.DOUBLE, multiplier)
    }
    fun initAttribute(record: AttributeRecord){
        val attributeUUID: UUID = UUID.fromString("a8d0bc44-1534-43f0-a594-f74c7c91bc59")
        val attributeName = "EnhancedMob Spawn Boost"
        record.apply(this.entity, this.multiplier, attributeUUID, attributeName)
    }
    fun initEquipment(material: Material, slot: EquipmentSlot){
        this.entity.equipment?.setItem(slot, ItemStack(material))
    }
    fun initEnchant(slot: EquipmentSlot, record: EnchantRecord){
        record.apply(this.entity, multiplier, slot)
    }
}

class EnhancedMobs {
    companion object {
        val logFormula: (Double) -> (Double) -> Double = { scale ->
            {
                when (it) {
                    in 0.0..Double.MAX_VALUE -> scale * ln(it + 1.0)
                    else -> it
                }
            }
        }
        val map = mapOf<String, (Double, LivingEntity) -> Unit>(
            "ZOMBIE" to {k,v -> Zombie(k,v)},
            "SKELETON" to {k,v -> Skeleton(k,v)},
            "SKELETON_VARIANT" to {k,v -> SkeletonVariant(k,v)},
            "SPIDER" to {k,v -> Spider(k,v)},
            "CREEPER" to {k,v -> Creeper(k,v)},
            "GENERIC" to {k,v -> Generic(k,v)}
        )
    }

    class Zombie(multiplier: Double, entity: LivingEntity): EnhancedMob(multiplier, entity){
        init {
            this.apply {
                initAttribute(AttributeRecord(mapOf(
                    GENERIC_MAX_HEALTH to AttributeRecordFactor(MULTIPLY_SCALAR_1, DoubleRecordFactor({1.4 * it})),
                    GENERIC_MOVEMENT_SPEED to AttributeRecordFactor(MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(0.25))),
                    GENERIC_ARMOR to AttributeRecordFactor(MULTIPLY_SCALAR_1, DoubleRecordFactor({0.35 * it}, -1.0..5.0)),
                    GENERIC_ATTACK_DAMAGE to AttributeRecordFactor(MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(1.0))),
                    GENERIC_KNOCKBACK_RESISTANCE to AttributeRecordFactor(ADD_NUMBER, DoubleRecordFactor({0.065 * it})),
                    GENERIC_FOLLOW_RANGE to AttributeRecordFactor(MULTIPLY_SCALAR_1, DoubleRecordFactor({0.15 * it}))
                )))
            }
        }
    }

    class Skeleton(multiplier: Double, entity: LivingEntity): EnhancedMob(multiplier, entity){
        init {
            this.apply {
                initAttribute(AttributeRecord(mapOf(
                    GENERIC_MAX_HEALTH to AttributeRecordFactor(MULTIPLY_SCALAR_1, DoubleRecordFactor({0.4 * it})),
                    GENERIC_ATTACK_DAMAGE to AttributeRecordFactor(MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(3.0))),
                    GENERIC_MOVEMENT_SPEED to AttributeRecordFactor(MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(0.25)))
                )))
                initEnchant(EquipmentSlot.HAND, EnchantRecord(mapOf(
                    Enchantment.ARROW_KNOCKBACK to IntRecordFactor({0.5 * it}),
                    Enchantment.ARROW_FIRE to IntRecordFactor({0.5 * it})
                )))
            }
        }
    }

    class SkeletonVariant(multiplier: Double, entity: LivingEntity): EnhancedMob(multiplier, entity){
        init {
            this.apply {
                initAttribute(AttributeRecord(mapOf(
                    GENERIC_MAX_HEALTH to AttributeRecordFactor(MULTIPLY_SCALAR_1, DoubleRecordFactor({0.4 * it})),
                    GENERIC_MOVEMENT_SPEED to AttributeRecordFactor(MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(0.25))),
                    GENERIC_ATTACK_DAMAGE to AttributeRecordFactor(MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(1.0)))
                )))
                initEquipment(Material.IRON_SWORD, EquipmentSlot.HAND)
            }
        }
    }

    class Spider(multiplier: Double, entity: LivingEntity): EnhancedMob(multiplier, entity){
        init {
            this.apply {
                initAttribute(AttributeRecord(mapOf(
                    GENERIC_MAX_HEALTH to AttributeRecordFactor(MULTIPLY_SCALAR_1, DoubleRecordFactor({2.0 * it})),
                    GENERIC_MOVEMENT_SPEED to AttributeRecordFactor(MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(0.25))),
                    GENERIC_ATTACK_DAMAGE to AttributeRecordFactor(MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(1.0)))
                )))
            }
        }
    }

    class Creeper(multiplier: Double, entity: LivingEntity): EnhancedMob(multiplier, entity){
        init {
            this.apply {
                initAttribute(AttributeRecord(mapOf(
                    GENERIC_MAX_HEALTH to AttributeRecordFactor(MULTIPLY_SCALAR_1, DoubleRecordFactor({0.35 * it})),
                    GENERIC_MOVEMENT_SPEED to AttributeRecordFactor(MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(0.25))),
                    GENERIC_KNOCKBACK_RESISTANCE to AttributeRecordFactor(ADD_NUMBER, DoubleRecordFactor({0.065 * it}))
                )))
            }
            (entity as org.bukkit.entity.Creeper).apply {
                maxFuseTicks = IntRecordFactor({-2 * it + 30}, 15..32767).value(multiplier)
                explosionRadius = IntRecordFactor({3 * it + 3}, 0..32).value(multiplier)
            }
        }
    }

    class Generic(multiplier: Double, entity: LivingEntity): EnhancedMob(multiplier, entity){
        init {
            this.apply {
                initAttribute(AttributeRecord(mapOf(
                    GENERIC_MAX_HEALTH to AttributeRecordFactor(MULTIPLY_SCALAR_1, DoubleRecordFactor({it})),
                    GENERIC_MOVEMENT_SPEED to AttributeRecordFactor(MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(0.25))),
                    GENERIC_ATTACK_DAMAGE to AttributeRecordFactor(MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(1.0)))
                )))
            }
        }
    }
}