@file:Suppress("MemberVisibilityCanBePrivate")

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
import org.bukkit.inventory.meta.FireworkMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.script.Api.MobApi
 *
 * @author yanshiqwq
 * @since 2024/6/30 下午3:59
 */
object MobApi {
    fun Entity.knockBack(multiplier: Double): Unit = setMotionMultiplier(-multiplier)
    fun Entity.setMotionMultiplier(multiplier: Double) {
        velocity = location.direction.multiply(multiplier)
    }
    fun LivingEntity.effect(
        effectType: PotionEffectType,
        amplifier: Int = 0,
        duration: Int = Int.MAX_VALUE,
        particle: Boolean = true,
        ambient: Boolean = false
    ) = addPotionEffect(PotionEffect(effectType, duration, amplifier, ambient, particle))

    fun LivingEntity.addAir(amount: Int, range: IntRange = -20..300) {
        remainingAir = (remainingAir + amount).coerceIn(range)
    }
    fun LivingEntity.reduceAir(amount: Double) = reduceAir(amount.toInt())
    fun LivingEntity.reduceAir(amount: Int) = addAir(-amount)

    fun LivingEntity.freeze(ticks: Int, range: IntRange = 140..440) {
        freezeTicks = (freezeTicks + ticks).coerceIn(range)
    }

    fun LivingEntity.fire(ticks: Int, range: IntRange = -20..300) {
        fireTicks = (fireTicks + ticks).coerceIn(range)
    }

    fun EnhancedMob.baseAttribute(attribute: Attribute, base: Double) {
        entity.getAttribute(attribute)?.baseValue = base
    }
    fun EnhancedMob.attribute(attribute: Attribute, operation: AttributeModifier.Operation, factor: Double) =
        this.attribute(attribute, operation, Record.DoubleFactor(formula = {factor}))
    fun EnhancedMob.attribute(attribute: Attribute, operation: AttributeModifier.Operation, factor: Record.DoubleFactor) =
        Record.AttributeRecord(attribute, Record.AttributeFactor(operation, factor)).apply(this.entity, multiplier, attributeUUID, attributeName)

    fun potionItem(type: Material, potion: PotionType, count: Int = 64): ItemStack {
        val item = ItemStack(type, count)
        val meta = item.itemMeta as? PotionMeta ?: return item
        meta.basePotionType = potion
        item.setItemMeta(meta)
        return item
    }
    fun fireworkItem(): ItemStack {
        val itemStack = ItemStack(Material.FIREWORK_ROCKET)
        val meta = itemStack.itemMeta as FireworkMeta
        meta.power = 1 // 设置火箭的飞行持续时间
        // 添加一个爆炸效果
        val effect = FireworkEffect.builder()
            .flicker(true)
            .withColor(Color.WHITE) // 设置颜色
            .with(FireworkEffect.Type.BURST) // 设置爆炸形状
            .build()
        meta.addEffect(effect)
        itemStack.itemMeta = meta
        return itemStack
    }
    fun EnhancedMob.item(slot: EquipmentSlot, type: Material, count: Int = 1, dropChance: Float = 0.085F, block: SlotBuilder.() -> Unit = {}) {
        val item = ItemStack(type, count)
        item(slot, item, block)
        entity.equipment.setDropChance(slot, dropChance)
    }
    fun EnhancedMob.item(slot: EquipmentSlot, item: ItemStack, block: SlotBuilder.() -> Unit = {}) {
        val builder = SlotBuilder(slot, item)
        block.invoke(builder)
        builder.build().invoke(this)
    }

    fun EnhancedMob.glowing() { entity.isGlowing = true }

    inline fun <reified T: Mob> EnhancedMob.property(crossinline block: T.() -> Unit) {
        if (entity !is T) throw IllegalArgumentException("Illegal EntityType: ${entity.type}")
        entity.block()
    }
}