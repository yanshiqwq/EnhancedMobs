package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.attribute.*
import java.util.*

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
class AttributeBuilder {
    /**
     * 实体的生命上限
     * @see Attribute.GENERIC_MAX_HEALTH
     */
    var health: Double? = null
    
    /**
     * 实体的攻击力
     * @see Attribute.GENERIC_ATTACK_DAMAGE
     */
    var damage: Double? = null
    
    /**
     * 实体的移动速度
     * @see Attribute.GENERIC_MOVEMENT_SPEED
     */
    var speed: Double? = null
    
    /**
     * 实体的攻击击退值
     * @see Attribute.GENERIC_ATTACK_KNOCKBACK
     */
    var knockback: Double? = null
    
    /**
     * 实体的击退抗性
     * @see Attribute.GENERIC_KNOCKBACK_RESISTANCE
     */
    var knockbackResistance: Double? = null
    
    /**
     * 实体的护甲值
     * @see Attribute.GENERIC_ARMOR
     */
    var armor: Double? = null
    
    /**
     * 实体的护甲韧性
     * @see Attribute.GENERIC_ARMOR_TOUGHNESS
     */
    var armorToughness: Double? = null
    
    /**
     * 实体的幸运值（保留字段）
     * @see Attribute.GENERIC_LUCK
     */
    var luck: Double? = null
    
    /**
     * 生成当前属性的映射
     *
     * @return 包含所有设置的属性及其值的映射
     */
    private fun map(): Map<Attribute, Double?> = mapOf(
        Attribute.GENERIC_MAX_HEALTH to health,
        Attribute.GENERIC_ATTACK_DAMAGE to damage,
        Attribute.GENERIC_MOVEMENT_SPEED to speed,
        Attribute.GENERIC_ATTACK_KNOCKBACK to knockback,
        Attribute.GENERIC_KNOCKBACK_RESISTANCE to knockbackResistance,
        Attribute.GENERIC_ARMOR to armor,
        Attribute.GENERIC_ARMOR_TOUGHNESS to armorToughness,
        Attribute.GENERIC_LUCK to luck
    ).filterValues { it != null }
    
    /**
     * 将属性应用于实体的基础值
     *
     * @param entity 要设置属性的实体
     */
    fun applyAsBase(entity: Attributable) = map().forEach { (attribute, value) ->
        entity.getAttribute(attribute)?.baseValue = value!!
    }
    
    /**
     * 为指定实体的属性添加属性修饰器
     * @see AttributeModifier
     *
     * @param name 修饰器的名称
     * @param operation 修饰器的操作类型
     * @param entity 要添加修饰器的实体
     * @param uuid 修饰器的 UUID
     */
    fun applyAsModifier(
        name: String,
        operation: AttributeModifier.Operation,
        entity: Attributable,
        uuid: UUID? = null
    ) = map().forEach { (attribute, value) ->
        val modifier = if (uuid != null)
            AttributeModifier(uuid, name, value!!, operation)
        else
            AttributeModifier(name, value!!, operation)
        entity.getAttribute(attribute)?.addModifier(modifier)
    }
    
    companion object {
        fun removeModifier(name: String, entity: Attributable) = Attribute.entries.forEach { attribute ->
            entity.getAttribute(attribute)?.let { attr ->
                attr.modifiers.removeIf { it.name == name }
            }
        }
        
        fun removeModifier(uuid: UUID, entity: Attributable) = Attribute.entries.forEach { attribute ->
            entity.getAttribute(attribute)?.let { attr ->
                attr.modifiers.removeIf { it.uniqueId == uuid }
            }
        }
    }
}