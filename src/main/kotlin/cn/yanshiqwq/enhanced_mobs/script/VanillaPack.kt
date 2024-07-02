package cn.yanshiqwq.enhanced_mobs.script

import cn.yanshiqwq.enhanced_mobs.Utils.byChance
import cn.yanshiqwq.enhanced_mobs.Utils.weightList
import cn.yanshiqwq.enhanced_mobs.api.ListenerApi.onPotionThrown
import cn.yanshiqwq.enhanced_mobs.dsl.MobDslBuilder.pack
import cn.yanshiqwq.enhanced_mobs.data.Record.IntFactor
import cn.yanshiqwq.enhanced_mobs.data.Record.DoubleFactor
import cn.yanshiqwq.enhanced_mobs.data.Record.logFormula
import cn.yanshiqwq.enhanced_mobs.managers.PackManager
import cn.yanshiqwq.enhanced_mobs.api.MobApi.attribute
import cn.yanshiqwq.enhanced_mobs.api.MobApi.fireworkItem
import cn.yanshiqwq.enhanced_mobs.api.MobApi.item
import cn.yanshiqwq.enhanced_mobs.api.MobApi.potionItem
import cn.yanshiqwq.enhanced_mobs.api.MobApi.property
import cn.yanshiqwq.enhanced_mobs.data.Tags
import org.bukkit.Material
import org.bukkit.attribute.Attribute.*
import org.bukkit.attribute.AttributeModifier.Operation.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Creeper
import org.bukkit.entity.EntityType
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType

object VanillaPack: PackManager.PackObj {
    override fun get(): PackManager.Pack = pack("vanilla") {
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
                enchant(Enchantment.ARROW_KNOCKBACK, logFormula(1.0).asIntFactor())
            }
        }
        type("spider") {
            attribute(GENERIC_MAX_HEALTH, MULTIPLY_SCALAR_1, DoubleFactor { 0.6 * it })
            attribute(GENERIC_MOVEMENT_SPEED, MULTIPLY_SCALAR_1, logFormula(0.3))
            attribute(GENERIC_ATTACK_DAMAGE, MULTIPLY_SCALAR_1, logFormula(1.8))
        }
        type("creeper") {
            attribute(GENERIC_MAX_HEALTH, MULTIPLY_SCALAR_1, DoubleFactor { 0.44 * it })
            attribute(GENERIC_MOVEMENT_SPEED, MULTIPLY_SCALAR_1, logFormula(0.2))
            attribute(GENERIC_KNOCKBACK_RESISTANCE, MULTIPLY_SCALAR_1, DoubleFactor { 0.035 * it })
            property<Creeper> {
                maxFuseTicks = IntFactor(15..32767) { 30 - 2 * it }.value(multiplier)
                explosionRadius = IntFactor(0..32) { 3 * it + 3 }.value(multiplier)
                isPowered = true.byChance(0.065) ?: false
            }
        }
        type("witch") {
            attribute(GENERIC_MAX_HEALTH, MULTIPLY_SCALAR_1, DoubleFactor { 0.35 * it })
            attribute(GENERIC_MOVEMENT_SPEED, MULTIPLY_SCALAR_1, logFormula(0.3))
            onPotionThrown {
                weightList {
                    if (multiplier >= 1.0) {
                        when (entity.type) {
                            EntityType.PLAYER -> {
                                weight(effect(PotionEffectType.UNLUCK,
                                    IntFactor(0..300 * 20) { it * 30 * 20.0 }, 2), 1)
                                weight(effect(PotionEffectType.BAD_OMEN,
                                    IntFactor(0..300 * 20) { it * 30 * 20.0 }, 3), 1)
                                weight(effect(PotionEffectType.SLOW_DIGGING,
                                    IntFactor(0..180 * 20) { it * 30 * 20.0 }, 0), 1)
                                weight(effect(PotionEffectType.HUNGER,
                                    IntFactor(0..180 * 20) { it * 12 * 20.0 }, 3), 3)
                            }
                            in Tags.Entity.undeads ->
                                weight(potion(PotionType.INSTANT_HEAL), 5)
                            !in Tags.Entity.undeads ->
                                weight(potion(PotionType.STRONG_HARMING), 5)
                            else -> {}
                        }
                        weight(effect(PotionEffectType.GLOWING,
                            IntFactor(0..300 * 20) { it * 30 * 20.0 }, 0), 1)
                    }
                    if (multiplier >= 2.0) {
                        when (entity.type) {
                            EntityType.PLAYER -> {
                                weight(effect(PotionEffectType.SLOW_DIGGING,
                                    IntFactor(0..30 * 20) { it * 5 * 20.0 }, 2), 1)
                            }
                            in Tags.Entity.undeads ->
                                weight(potion(PotionType.STRONG_HEALING), 5)
                            !in Tags.Entity.undeads -> {
                                weight(potion(PotionType.STRONG_POISON), 1)
                                weight(potion(PotionType.LONG_POISON), 1)
                            }
                            else -> {}
                        }
                        weight(potion(PotionType.STRONG_SLOWNESS), 1)
                        weight(potion(PotionType.LONG_SLOWNESS), 1)
                        weight(potion(PotionType.LONG_WEAKNESS), 1)
                        weight(effect(PotionEffectType.JUMP,
                            IntFactor(0..60 * 20) { it * 12 * 20.0 }, 254), 3)
                        weight(effect(PotionEffectType.WITHER,
                            IntFactor(0..60 * 20) { it * 7 * 20.0 }, 1), 2)
                        weight(effect(PotionEffectType.WITHER,
                            IntFactor(0..90 * 20) { it * 12 * 20.0 }, 0), 2)
                    }
                    if (multiplier >= 3.0) {
                        when (entity.type) {
                            EntityType.PLAYER -> {
                                weight(effect(PotionEffectType.CONFUSION,
                                    IntFactor(0..30 * 20) { it * 7 * 20.0 }, 0), 1)
                                weight(effect(PotionEffectType.BLINDNESS,
                                    IntFactor(0..30 * 20) { it * 12 * 20.0 }, 0), 5)
                            }
                            else -> {}
                        }
                        weight(potion(PotionType.LONG_TURTLE_MASTER), 1)
                        weight(effect(PotionEffectType.LEVITATION,
                            IntFactor(0..30 * 20) { it * 5 * 20.0 }, 0), 1)
                    }
                }.getRandomByWeightList().byChance(
                    DoubleFactor(0.0..0.5) { 0.15 + it * 0.1 }.value(multiplier)
                )?.run()
                lingering().byChance(
                        DoubleFactor(0.0..0.5) { 0.15 + it * 0.05 }.value(multiplier)
                )?.run()
            }
        }
        type("pillager") {
            attribute(GENERIC_MAX_HEALTH, MULTIPLY_SCALAR_1, DoubleFactor { 0.5 * it })
            attribute(GENERIC_MOVEMENT_SPEED, MULTIPLY_SCALAR_1, logFormula(0.2))
            attribute(GENERIC_ATTACK_DAMAGE, MULTIPLY_SCALAR_1, logFormula(2.0))
            weightList {
                weight(Runnable { item(EquipmentSlot.OFF_HAND, potionItem(Material.TIPPED_ARROW, PotionType.WEAKNESS)) }, 2)
                weight(Runnable { item(EquipmentSlot.OFF_HAND, ItemStack(Material.SPECTRAL_ARROW, 64)) }, 1)
                weight(Runnable { item(EquipmentSlot.OFF_HAND, fireworkItem()) }, 2)
                if (multiplier >= 1.0) {
                    weight(Runnable { item(EquipmentSlot.OFF_HAND, potionItem(Material.TIPPED_ARROW, PotionType.STRONG_SLOWNESS)) }, 1)
                    weight(Runnable { item(EquipmentSlot.OFF_HAND, potionItem(Material.TIPPED_ARROW, PotionType.INSTANT_DAMAGE)) }, 1)
                }
            }.getRandomByWeightList().byChance(
                DoubleFactor(0.0..0.85) { 0.2 * it }.value(multiplier))?.run()
        }
        type("fallback") {
            attribute(GENERIC_MAX_HEALTH, MULTIPLY_SCALAR_1, DoubleFactor { 0.44 * it })
            attribute(GENERIC_MOVEMENT_SPEED, MULTIPLY_SCALAR_1, logFormula(0.2))
            attribute(GENERIC_ATTACK_DAMAGE, MULTIPLY_SCALAR_1, logFormula(1.5))
        }
    }
}