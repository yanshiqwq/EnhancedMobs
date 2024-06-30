package cn.yanshiqwq.enhanced_mobs.script

import cn.yanshiqwq.enhanced_mobs.dsl.MobDslBuilder.pack
import cn.yanshiqwq.enhanced_mobs.data.Record.IntFactor
import cn.yanshiqwq.enhanced_mobs.data.Record.DoubleFactor
import cn.yanshiqwq.enhanced_mobs.data.Record.logFormula
import cn.yanshiqwq.enhanced_mobs.managers.PackManager
import cn.yanshiqwq.enhanced_mobs.api.MobApi.attribute
import cn.yanshiqwq.enhanced_mobs.api.MobApi.item
import cn.yanshiqwq.enhanced_mobs.api.MobApi.property
import org.bukkit.Material
import org.bukkit.attribute.Attribute.*
import org.bukkit.attribute.AttributeModifier.Operation.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Creeper
import org.bukkit.inventory.EquipmentSlot

object VanillaPack {
    fun get(): PackManager.Pack = pack("vanilla") {
        type("zombie") {
            attribute(GENERIC_MAX_HEALTH, MULTIPLY_SCALAR_1, DoubleFactor { 0.52 * it })
            attribute(GENERIC_MOVEMENT_SPEED, MULTIPLY_SCALAR_1, logFormula(0.2))
            attribute(GENERIC_ATTACK_DAMAGE, MULTIPLY_SCALAR_1, logFormula(2.0))
            attribute(GENERIC_KNOCKBACK_RESISTANCE, ADD_NUMBER, DoubleFactor { 0.045 * it })
        }
        type("skeleton") {
            attribute(GENERIC_MAX_HEALTH, MULTIPLY_SCALAR_1, DoubleFactor { 0.44 * it })
            attribute(GENERIC_ATTACK_DAMAGE, MULTIPLY_SCALAR_1, logFormula(2.0))
            attribute(GENERIC_MOVEMENT_SPEED, MULTIPLY_SCALAR_1, logFormula(0.2))
            item(EquipmentSlot.HAND, Material.BOW) {
                enchant(Enchantment.ARROW_KNOCKBACK, logFormula(2.0).asIntFactor())
                enchant(Enchantment.ARROW_FIRE, IntFactor { if (it >= 2) 1.0 else 0.0 })
            }
        }
        type("spider") {
            attribute(GENERIC_MAX_HEALTH, MULTIPLY_SCALAR_1, DoubleFactor { 0.6 * it })
            attribute(GENERIC_MOVEMENT_SPEED, MULTIPLY_SCALAR_1, logFormula(0.25))
            attribute(GENERIC_ATTACK_DAMAGE, MULTIPLY_SCALAR_1, logFormula(1.8))
        }
        type("creeper") {
            attribute(GENERIC_MAX_HEALTH, MULTIPLY_SCALAR_1, DoubleFactor { 0.44 * it })
            attribute(GENERIC_MOVEMENT_SPEED, MULTIPLY_SCALAR_1, logFormula(0.25))
            attribute(GENERIC_KNOCKBACK_RESISTANCE, MULTIPLY_SCALAR_1, DoubleFactor { 0.035 * it })
            property<Creeper> {
                maxFuseTicks = IntFactor(15..32767) { 30 - 2 * it }.value(multiplier)
                explosionRadius = IntFactor(0..32) { 3 * it + 3 }.value(multiplier)
            }
        }
        type("witch") {

        }
        type("fallback") {
            attribute(GENERIC_MAX_HEALTH, MULTIPLY_SCALAR_1, DoubleFactor { 0.44 * it })
            attribute(GENERIC_MOVEMENT_SPEED, MULTIPLY_SCALAR_1, logFormula(0.2))
            attribute(GENERIC_ATTACK_DAMAGE, MULTIPLY_SCALAR_1, logFormula(1.5))
        }
    }
}