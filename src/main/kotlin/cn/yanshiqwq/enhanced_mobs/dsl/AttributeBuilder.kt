package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.attribute.Attributable
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import java.util.UUID

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.BaseAttributeBuilder
 *
 * @author yanshiqwq
 * @since 2024/8/19 下午10:33
 */

class AttributeBuilder {
    var health: Double? = null
    var damage: Double? = null
    var speed: Double? = null
    var knockback: Double? = null
    var knockbackResistance: Double? = null
    var armor: Double? = null
    var armorToughness: Double? = null
    var luck: Double? = null

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

    fun setBase(entity: Attributable) = map().forEach { (attribute, value) ->
        entity.getAttribute(attribute)?.baseValue = value!!
    }

    fun addModifiers(name: String, operation: AttributeModifier.Operation, entity: Attributable, uuid: UUID? = null) = map().forEach { (attribute, value) ->
        val modifier = if (uuid != null)
            AttributeModifier(uuid, name, value!!, operation)
        else
            AttributeModifier(name, value!!, operation)
        entity.getAttribute(attribute)?.addModifier(modifier)
    }
}