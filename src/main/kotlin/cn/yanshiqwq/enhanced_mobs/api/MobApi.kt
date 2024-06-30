package cn.yanshiqwq.enhanced_mobs.api

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.EnhancedMob.Companion.attributeName
import cn.yanshiqwq.enhanced_mobs.EnhancedMob.Companion.attributeUUID
import cn.yanshiqwq.enhanced_mobs.data.Record
import cn.yanshiqwq.enhanced_mobs.dsl.MobDslBuilder.SlotBuilder
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.*
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.script.Api.MobApi
 *
 * @author yanshiqwq
 * @since 2024/6/30 下午3:59
 */
object MobApi {
    fun LivingEntity.knockBack(strength: Double, directionX: Double = 0.0, directionZ: Double = 0.0): Unit = knockback(strength, directionX, directionZ)
    fun Entity.setMotionMultiplier(multiplier: Double) {
        velocity = location.direction.multiply(multiplier)
    }
    fun LivingEntity.effect(
        effectType: PotionEffectType,
        amplifier: Int = 0,
        duration: Int = Int.MAX_VALUE,
        particle: Boolean = true,
        ambient: Boolean = true
    ) = addPotionEffect(PotionEffect(effectType, duration, amplifier, ambient, particle))

    fun LivingEntity.addAir(amount: Int) {
        remainingAir = (remainingAir + amount).coerceAtLeast(0)
    }
    fun LivingEntity.reduceAir(amount: Int) = addAir(-amount)

    fun EnhancedMob.attribute(attribute: Attribute, operation: AttributeModifier.Operation, factor: Double) {
        this.attribute(attribute, operation, Record.DoubleFactor(formula = {factor}))
    }
    fun EnhancedMob.attribute(attribute: Attribute, operation: AttributeModifier.Operation, factor: Record.DoubleFactor) =
        Record.AttributeRecord(attribute, Record.AttributeFactor(operation, factor)).apply(this.entity, multiplier, attributeUUID, attributeName)

    fun item(slot: EquipmentSlot, type: Material, block: SlotBuilder.() -> Unit = {}) = item(slot, ItemStack(type), block)
    fun item(slot: EquipmentSlot, item: ItemStack? = null, block: SlotBuilder.() -> Unit = {}) {
        val builder = SlotBuilder(slot, item)
        block.invoke(builder)
        builder.build()
    }

    inline fun <reified T: Mob> EnhancedMob.property(crossinline block: T.() -> Unit) {
        if (entity !is T) throw IllegalArgumentException("Illegal EntityType: ${entity.type}")
        entity.block()
    }
}