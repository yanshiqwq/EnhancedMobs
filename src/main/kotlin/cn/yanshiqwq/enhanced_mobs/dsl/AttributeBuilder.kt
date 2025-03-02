package cn.yanshiqwq.enhanced_mobs.dsl

import cn.yanshiqwq.enhanced_mobs.Utils
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.attribute.Attributable
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeInstance
import org.bukkit.attribute.AttributeModifier
import org.bukkit.attribute.AttributeModifier.Operation
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.BaseAttributeBuilder
 *
 * @author yanshiqwq
 * @since 2024/8/19 下午 10:33
 */
/**
 * 用于构建和设置实体的属性
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
data class AttributeBuilder(
    private val map: MutableMap<Attribute, Double?> = mutableMapOf()
) {
    fun maxHealth(value: Double) = map.set(Attribute.MAX_HEALTH, value)
    fun followRange(value: Double) = map.set(Attribute.FOLLOW_RANGE, value)
    fun knockbackResistance(value: Double) = map.set(Attribute.KNOCKBACK_RESISTANCE, value)
    fun movementSpeed(value: Double) = map.set(Attribute.MOVEMENT_SPEED, value)
    fun flyingSpeed(value: Double) = map.set(Attribute.FLYING_SPEED, value)
    fun attackDamage(value: Double) = map.set(Attribute.ATTACK_DAMAGE, value)
    fun attackKnockback(value: Double) = map.set(Attribute.ATTACK_KNOCKBACK, value)
    fun armor(value: Double) = map.set(Attribute.ARMOR, value)
    fun armorToughness(value: Double) = map.set(Attribute.ARMOR_TOUGHNESS, value)
    fun luck(value: Double) = map.set(Attribute.LUCK, value)
    fun maxAbsorption(value: Double) = map.set(Attribute.MAX_ABSORPTION, value)
    fun spawnReinforcements(value: Double) = map.set(Attribute.SPAWN_REINFORCEMENTS, value)
    
    /**
     * 将属性应用于实体的基础值
     *
     * @param entity 要设置属性的实体
     */
    fun applyAsBase(entity: Attributable) = map.forEach { (attribute, value) ->
        entity.getAttribute(attribute)?.baseValue = value!!
    }
    
    /**
     * 添加属性修饰器
     * @see AttributeModifier
     *
     * @param operation 修饰器的操作类型
     * @param entity 要添加修饰器的实体
     */
    fun applyAsModifier(
        key: NamespacedKey,
        operation: Operation,
        entity: Attributable,
    ) = map.forEach { (attribute, value) ->
        val modifier = AttributeModifier(key, value!!, operation, EquipmentSlotGroup.ANY)
        entity.getAttribute(attribute)?.addModifierSafe(modifier)
    }
    
    fun applyAsModifier(
        key: NamespacedKey,
        operation: Operation,
        meta: ItemMeta,
    ) = map.forEach { (attribute, value) ->
        val modifier = AttributeModifier(key, value!!, operation, EquipmentSlotGroup.ANY)
        meta.addAttributeModifier(attribute, modifier)
    }
    
    companion object {
        /**
         * 修改实体的基础属性
         *
         * @param block 用于配置属性的构建块
         */
        inline fun LivingEntity.base(block: AttributeBuilder.() -> Unit) =
            AttributeBuilder().apply(block).applyAsBase(this)
        
        /**
         * 为实体附加属性修饰器
         *
         * @param operation 修饰器的操作类型
         * @param block 用于配置属性的构建块
         */
        inline fun Attributable.modifier(
            operation: Operation,
            key: NamespacedKey = Utils.createNamespacedKey("default_modifier"),
            block: AttributeBuilder.() -> Unit
        )= AttributeBuilder().apply(block).run {
            applyAsModifier(key, operation, this@modifier)
        }
        
        inline fun Attributable.attribute(
            attribute: Attribute,
            block: AttributeInstance.() -> Unit
        ) = getAttribute(attribute)?.block() ?: Unit
        
        fun AttributeInstance.hasModifier(modifier: AttributeModifier) =
            modifiers.contains(modifier)
        
        fun AttributeInstance.removeModifier(condition: (AttributeModifier) -> Boolean) =
            modifiers.filter(condition).forEach { removeModifier(it) }

        fun Attributable.removeModifier(key: NamespacedKey) =
            removeModifierByCondition { it.key == key }
        
        fun AttributeInstance.addModifierSafe(modifier: AttributeModifier) =
            runCatching { addModifier(modifier) }
        
        fun AttributeInstance.modifier(
            operation: Operation,
            amount: Double = 0.0,
            key: NamespacedKey,
            slot: EquipmentSlotGroup = EquipmentSlotGroup.ANY
        ) = addModifierSafe(AttributeModifier(key, amount, operation, slot))
    
        inline fun ItemStack.modifier(
            operation: Operation,
            key: NamespacedKey = Utils.createNamespacedKey("modifier"),
            block: AttributeBuilder.() -> Unit
        ): ItemStack {
            AttributeBuilder().apply(block).run {
                if (itemMeta == null) return@run
                applyAsModifier(key, operation, itemMeta!!)
            }
            return this
        }
        
        fun Attributable.listAttribute() = Registry.ATTRIBUTE.map { attribute ->
            getAttribute(attribute)
        }

        private fun Attributable.removeModifierByCondition(condition: (AttributeModifier) -> Boolean) =
            listAttribute().forEach { attribute ->
                attribute?.modifiers?.removeIf(condition)
            }
    }
}