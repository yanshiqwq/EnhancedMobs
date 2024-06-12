@file:JvmName("PackKt")

package cn.yanshiqwq.enhanced_mobs.data

import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.Utils.isAxe
import cn.yanshiqwq.enhanced_mobs.Utils.placeBlock
import cn.yanshiqwq.enhanced_mobs.Utils.spawnEntity
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityResurrectEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.ln
import kotlin.random.Random

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.data.BuiltInPacks
 *
 * @author yanshiqwq
 * @since 2024/6/8 07:01
 */

object BuiltInPacks {
    val logFormula: (Double) -> (Double) -> Double = { scale ->
        {
            when (it) {
                in 0.0..Double.MAX_VALUE -> scale * ln(it + 1.0)
                else -> it
            }
        }
    }
    val vanillaPack = Pack("VANILLA", mapOf(
        "ZOMBIE" to {
            initAttribute(
                Record.AttributeRecord(
                    mapOf(
                        Attribute.GENERIC_MAX_HEALTH to Record.AttributeFactor(
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                            Record.DoubleFactor({ 1.4 * it })
                        ),
                        Attribute.GENERIC_MOVEMENT_SPEED to Record.AttributeFactor(
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                            Record.DoubleFactor(logFormula(0.25))
                        ),
                        Attribute.GENERIC_ARMOR to Record.AttributeFactor(
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                            Record.DoubleFactor({ 0.35 * it }, -1.0..5.0)
                        ),
                        Attribute.GENERIC_ATTACK_DAMAGE to Record.AttributeFactor(
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                            Record.DoubleFactor(logFormula(1.0))
                        ),
                        Attribute.GENERIC_KNOCKBACK_RESISTANCE to Record.AttributeFactor(
                            AttributeModifier.Operation.ADD_NUMBER,
                            Record.DoubleFactor({ 0.065 * it })
                        ),
                        Attribute.GENERIC_FOLLOW_RANGE to Record.AttributeFactor(
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                            Record.DoubleFactor({ 0.15 * it })
                        )
                    )
                )
            )
        },

        "SKELETON" to {
            initAttribute(
                Record.AttributeRecord(
                    mapOf(
                        Attribute.GENERIC_MAX_HEALTH to Record.AttributeFactor(
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                            Record.DoubleFactor({ 0.65 * it })
                        ),
                        Attribute.GENERIC_ATTACK_DAMAGE to Record.AttributeFactor(
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                            Record.DoubleFactor(logFormula(3.0))
                        ),
                        Attribute.GENERIC_MOVEMENT_SPEED to Record.AttributeFactor(
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                            Record.DoubleFactor(logFormula(0.25))
                        )
                    )
                )
            )
            initEnchant(
                EquipmentSlot.HAND, Record.EnchantRecord(
                    mapOf(
                        Enchantment.ARROW_KNOCKBACK to Record.IntFactor({ 0.5 * it }),
                        Enchantment.ARROW_FIRE to Record.IntFactor({ 0.5 * it })
                    )
                )
            )
        },

        "SPIDER" to {
            initAttribute(
                Record.AttributeRecord(
                    mapOf(
                        Attribute.GENERIC_MAX_HEALTH to Record.AttributeFactor(
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                            Record.DoubleFactor({ 2.0 * it })
                        ),
                        Attribute.GENERIC_MOVEMENT_SPEED to Record.AttributeFactor(
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                            Record.DoubleFactor(logFormula(0.25))
                        ),
                        Attribute.GENERIC_ATTACK_DAMAGE to Record.AttributeFactor(
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                            Record.DoubleFactor(logFormula(1.0))
                        )
                    )
                )
            )
        },

        "CREEPER" to {
            if (entity !is Creeper) throw IllegalArgumentException("Illegal EntityType: ${entity.type}")
            initAttribute(
                Record.AttributeRecord(
                    mapOf(
                        Attribute.GENERIC_MAX_HEALTH to Record.AttributeFactor(
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                            Record.DoubleFactor({ 0.35 * it })
                        ),
                        Attribute.GENERIC_MOVEMENT_SPEED to Record.AttributeFactor(
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                            Record.DoubleFactor(logFormula(0.25))
                        ),
                        Attribute.GENERIC_KNOCKBACK_RESISTANCE to Record.AttributeFactor(
                            AttributeModifier.Operation.ADD_NUMBER,
                            Record.DoubleFactor({ 0.065 * it })
                        )
                    )
                )
            )
            entity.apply {
                maxFuseTicks = Record.IntFactor({ -2 * it + 30 }, 15..32767).value(multiplier)
                explosionRadius = Record.IntFactor({ 3 * it + 3 }, 0..32).value(multiplier)
            }
        },

        "GENERIC" to {
            initAttribute(
                Record.AttributeRecord(
                    mapOf(
                        Attribute.GENERIC_MAX_HEALTH to Record.AttributeFactor(
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                            Record.DoubleFactor({ it })
                        ),
                        Attribute.GENERIC_MOVEMENT_SPEED to Record.AttributeFactor(
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                            Record.DoubleFactor(logFormula(0.25))
                        ),
                        Attribute.GENERIC_ATTACK_DAMAGE to Record.AttributeFactor(
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                            Record.DoubleFactor(logFormula(1.0))
                        )
                    )
                )
            )
        }
    ))

    val extendPack = Pack("EXTEND", mapOf(
        "SKELETON_IRON_SWORD" to vanillaPack.implement("SKELETON") {
            initEquipment(EquipmentSlot.HAND, Material.IRON_SWORD)
        },
        "ZOMBIE_STRENGTH_CLOUD" to vanillaPack.implement("ZOMBIE") {
            initListener<EntityDeathEvent> {
                entity.location.spawnEntity<AreaEffectCloud>(EntityType.AREA_EFFECT_CLOUD) {
                    color = PotionEffectType.INCREASE_DAMAGE.color
                    duration = 50
                    radius = 2.0F
                    addCustomEffect(PotionEffect(PotionEffectType.INCREASE_DAMAGE, 120, 1), true)
                }
                return@initListener true
            }
        },
        "ZOMBIE_TOTEM" to vanillaPack.implement("ZOMBIE") {
            initEquipment(EquipmentSlot.OFF_HAND, Material.TOTEM_OF_UNDYING)
            initListener<EntityResurrectEvent> {
                if (it.isCancelled) return@initListener false
                val maxHealth = it.entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: return@initListener false
                it.entity.health = maxHealth * 0.5
                it.entity.addPotionEffect(PotionEffect(PotionEffectType.INCREASE_DAMAGE, Int.MAX_VALUE, 0))
                return@initListener true
            }
        },
        "ZOMBIE_TNT" to vanillaPack.implement("ZOMBIE") {
            initRangeUsingItemTask(6.0, Material.TNT) {
                it.location.spawnEntity<TNTPrimed>(EntityType.PRIMED_TNT) {
                    fuseTicks = 40
                    source = entity
                    location.direction.multiply(1.4)
                }
                it.world.run {
                    playSound(it.location, Sound.BLOCK_GRASS_PLACE, 1.0F, 0.75F)
                    playSound(it.location, Sound.ENTITY_TNT_PRIMED, 1.0F, 1.0F)
                }
                return@initRangeUsingItemTask true
            }
        },
        "ZOMBIE_LAVA" to vanillaPack.implement("ZOMBIE") {
            initRangeUsingItemTask(5.0, Material.LAVA_BUCKET, true, Material.BUCKET) {
                if (it.isInWaterOrBubbleColumn || it.isInLava) return@initRangeUsingItemTask false
                it.location.placeBlock(Material.LAVA)
                it.world.playSound(it.location, Sound.ITEM_BUCKET_EMPTY_LAVA, 1.0F, 1.0F)
                addEffect(PotionEffectType.FIRE_RESISTANCE, 0, 1200, true)
                return@initRangeUsingItemTask true
            }
        },
        "ZOMBIE_FLINT_AND_STEEL" to vanillaPack.implement("ZOMBIE") {
            initRangeUsingItemTask(5.0, Material.FLINT_AND_STEEL, false) {
                if (it.isInWaterOrBubbleColumn || it.isInLava) return@initRangeUsingItemTask false
                it.location.placeBlock(Material.FIRE)
                it.world.playSound(it.location, Sound.ITEM_FLINTANDSTEEL_USE, 1.0F, 1.0F)
                return@initRangeUsingItemTask true
            }
        },
        "ZOMBIE_ENDER_PEARL" to vanillaPack.implement("ZOMBIE") {
            initRangeUsingItemTask(16.0, Material.ENDER_PEARL) {
                entity.teleport(it)
                it.damage(0.0, entity)
                it.world.run {
                    playSound(it.location, Sound.ENTITY_ENDER_PEARL_THROW, 1.0F, 1.0F)
                    playSound(it.location, Sound.ENTITY_GENERIC_SMALL_FALL, 1.0F, 1.0F)
                    spawnParticle(Particle.PORTAL, it.location.apply { y += 1 }, 64, 0.0, 0.0, 0.0, 0.6)
                }
                val event = EntityDamageEvent(entity, DamageCause.FALL, 5.0)
                event.callEvent()
                return@initRangeUsingItemTask true
            }
        },
        "ZOMBIE_SHIELD" to vanillaPack.implement("ZOMBIE") {
            val itemType = Material.SHIELD
            initEquipment(EquipmentSlot.OFF_HAND, itemType)
            initListener<EntityDamageByEntityEvent> {
                if (it.entity != entity) return@initListener false
                if (entity.isDead) return@initListener false

                val shield = entity.equipment.itemInOffHand
                if (shield.type != itemType) return@initListener false

                val damager = it.damager as LivingEntity
                if (entity.isGlowing) {
                    if (damager.equipment!!.itemInMainHand.type.isAxe()) {
                        breakShield(entity, damager)
                        addEffect(PotionEffectType.SLOW, 4, 20, true)
                        it.damage *= 2
                    } else {
                        shieldBlock(damager)
                        it.isCancelled = true
                    }
                } else if (Random.nextDouble() <= (0.15 * (multiplier + 1)).coerceIn(0.0, 0.65)) {
                    holdShield(entity)
                    Bukkit.getScheduler().runTaskLater(instance!!, Runnable {
                        if (entity.isGlowing) breakShield(entity, damager)
                    }, 60L)
                }
                return@initListener true
            }
        }
    ))

    private fun holdShield(entity: Mob) {
        entity.isGlowing = true
    }

    private fun shieldBlock(damager: Entity) {
        damager.velocity = damager.location.direction.multiply(-0.35)
        damager.world.playSound(damager.location, Sound.ITEM_SHIELD_BLOCK, 1.0F, 1.0F)
    }

    private fun breakShield(entity: Mob, damager: Entity) {
        entity.isGlowing = false
        val particleLocation = entity.location.apply { y += 1 }
        damager.world.run {
            playSound(damager.location, Sound.ITEM_SHIELD_BREAK, 1.0F, 1.0F)
            spawnParticle(Particle.ITEM_CRACK, particleLocation, 72, 0.3, 0.5, 0.3, 0.0, ItemStack(Material.SHIELD))
        }
    }
}