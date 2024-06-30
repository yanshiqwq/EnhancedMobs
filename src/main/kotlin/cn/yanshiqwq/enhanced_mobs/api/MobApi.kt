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
    fun potionItem(type: Material, potion: PotionType): ItemStack {
        val item = ItemStack(type)
        (item.itemMeta as? PotionMeta)?.basePotionType = potion
        return item
    }
    fun fireworkItem(): ItemStack {
        val itemStack = ItemStack(Material.FIREWORK_ROCKET)
        val meta = itemStack.itemMeta as FireworkMeta

        meta.power = 3 // 设置火箭的飞行持续时间

        // 添加一个爆炸效果
        val effect = FireworkEffect.builder()
            .flicker(true)
            .withColor(Color.fromRGB(11743532)) // 设置颜色
            .with(FireworkEffect.Type.BURST) // 设置爆炸形状
            .trail(true)
            .build()

        meta.addEffect(effect)

        itemStack.itemMeta = meta

        return itemStack
    }
    fun EnhancedMob.item(slot: EquipmentSlot, type: Material, block: SlotBuilder.() -> Unit = {}) = item(slot, ItemStack(type), block)
    fun EnhancedMob.item(slot: EquipmentSlot, item: ItemStack, block: SlotBuilder.() -> Unit = {}) {
        val builder = SlotBuilder(slot, item)
        builder.build().invoke(this)
        block.invoke(builder)
    }

    inline fun <reified T: Mob> EnhancedMob.property(crossinline block: T.() -> Unit) {
        if (entity !is T) throw IllegalArgumentException("Illegal EntityType: ${entity.type}")
        entity.block()
    }
}