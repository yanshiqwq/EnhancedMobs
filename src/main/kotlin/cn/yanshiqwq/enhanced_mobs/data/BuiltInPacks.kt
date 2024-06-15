@file:JvmName("PackKt")

package cn.yanshiqwq.enhanced_mobs.data

import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.Utils.applyEffect
import cn.yanshiqwq.enhanced_mobs.Utils.heal
import cn.yanshiqwq.enhanced_mobs.Utils.initEquipment
import cn.yanshiqwq.enhanced_mobs.Utils.isAxe
import cn.yanshiqwq.enhanced_mobs.Utils.placeBlock
import cn.yanshiqwq.enhanced_mobs.Utils.playSound
import cn.yanshiqwq.enhanced_mobs.Utils.setMotionMultiplier
import cn.yanshiqwq.enhanced_mobs.Utils.spawnEntity
import cn.yanshiqwq.enhanced_mobs.Utils.spawnParticle
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityResurrectEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import java.util.*
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
    val vanillaPack = Pack("vanilla", mapOf(
        "zombie" to {
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
                        Attribute.GENERIC_ATTACK_DAMAGE to Record.AttributeFactor(
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                            Record.DoubleFactor({ it })
                        ),
                        Attribute.GENERIC_KNOCKBACK_RESISTANCE to Record.AttributeFactor(
                            AttributeModifier.Operation.ADD_NUMBER,
                            Record.DoubleFactor({ 0.065 * it })
                        )
                    )
                )
            )
        },

        "skeleton" to {
            initAttribute(
                Record.AttributeRecord(
                    mapOf(
                        Attribute.GENERIC_MAX_HEALTH to Record.AttributeFactor(
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                            Record.DoubleFactor({ 0.65 * it })
                        ),
                        Attribute.GENERIC_ATTACK_DAMAGE to Record.AttributeFactor(
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                            Record.DoubleFactor({ 3 * it })
                        ),
                        Attribute.GENERIC_MOVEMENT_SPEED to Record.AttributeFactor(
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                            Record.DoubleFactor(logFormula(0.20))
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

        "spider" to {
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
                            Record.DoubleFactor({ 1.5 * it })
                        )
                    )
                )
            )
        },

        "creeper" to {
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
                            Record.DoubleFactor({ 0.035 * it })
                        )
                    )
                )
            )
            entity.apply {
                maxFuseTicks = Record.IntFactor({ -2 * it + 30 }, 15..32767).value(multiplier)
                explosionRadius = Record.IntFactor({ 3 * it + 3 }, 0..32).value(multiplier)
            }
        },

        "generic" to {
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
                            Record.DoubleFactor({ it })
                        )
                    )
                )
            )
        }
    ))

    val extendPack = Pack("extend", mapOf(
        "skeleton_iron_sword" to {
            initAttribute(
                Record.AttributeRecord(
                    mapOf(
                        Attribute.GENERIC_MAX_HEALTH to Record.AttributeFactor(
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                            Record.DoubleFactor({ 0.65 * it })
                        ),
                        Attribute.GENERIC_ATTACK_DAMAGE to Record.AttributeFactor(
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                            Record.DoubleFactor(logFormula(0.8))
                        ),
                        Attribute.GENERIC_MOVEMENT_SPEED to Record.AttributeFactor(
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                            Record.DoubleFactor(logFormula(0.1))
                        )
                    )
                )
            )
            entity.initEquipment(EquipmentSlot.HAND, Material.IRON_SWORD)
        },
        "zombie_strength_cloud" to vanillaPack.implement("zombie") {
            initListener<EntityDeathEvent> {
                if (it.entity != entity) return@initListener
                entity.location.spawnEntity<AreaEffectCloud>(EntityType.AREA_EFFECT_CLOUD) {
                    color = PotionEffectType.INCREASE_DAMAGE.color
                    duration = 50
                    radius = 2.0F
                    addCustomEffect(PotionEffect(PotionEffectType.INCREASE_DAMAGE, 120, 1), true)
                }
            }
        },
        "zombie_totem" to vanillaPack.implement("zombie") {
            entity.initEquipment(EquipmentSlot.OFF_HAND, Material.TOTEM_OF_UNDYING)
            initListener<EntityResurrectEvent> {
                if ((it.entity != entity) || it.isCancelled) return@initListener
                entity.heal(0.5)
                entity.applyEffect(PotionEffectType.SPEED, 0)
            }
        },
        "zombie_tnt" to vanillaPack.implement("zombie") {
            initRangeItemDisposableTask(6.0, Material.TNT) {
                it.location.run {
                    spawnEntity<TNTPrimed>(EntityType.PRIMED_TNT) {
                        fuseTicks = 40
                        source = entity
                        location.direction.multiply(1.4)
                    }
                    playSound(Sound.BLOCK_GRASS_PLACE, 1.0F, 0.75F)
                    playSound(Sound.ENTITY_TNT_PRIMED, 1.0F, 1.0F)
                }
                return@initRangeItemDisposableTask true
            }
        },
        "zombie_lava" to vanillaPack.implement("zombie") {
           initRangeItemPeriodTask(5.0, Material.LAVA_BUCKET, Material.BUCKET) {
                if (it.isInWaterOrBubbleColumn || it.isInLava) return@initRangeItemPeriodTask false
                it.location.run {
                    placeBlock(Material.LAVA)
                    playSound(Sound.ITEM_BUCKET_EMPTY_LAVA, 1.0F, 1.0F)
                    initDelayTask(100L) {
                        if (block.type != Material.LAVA) return@initDelayTask
                        placeBlock(Material.AIR)
                        entity.initEquipment(EquipmentSlot.OFF_HAND, Material.LAVA_BUCKET)
                    }
                }
                entity.applyEffect(PotionEffectType.FIRE_RESISTANCE, 0, 72 * 20)
                return@initRangeItemPeriodTask true
            }
        },
        "zombie_fire_charge" to vanillaPack.implement("zombie") {
            initRangeItemPeriodTask(8.0, ItemStack(Material.FIRE_CHARGE, 4)) {
                if (it.isInWaterOrRainOrBubbleColumn || it.isInLava) return@initRangeItemPeriodTask false
                entity.location.playSound(Sound.ITEM_FIRECHARGE_USE, 1.0F, 1.0F)
                entity.launchProjectile(Fireball::class.java).apply {
                    velocity = location.direction.multiply(1.2)
                    shooter = entity
                    yield = (1 + multiplier * 0.5).coerceIn(0.0..16.0).toFloat()
                }
                entity.applyEffect(PotionEffectType.DAMAGE_RESISTANCE, 0, 12 * 20)
                entity.applyEffect(PotionEffectType.FIRE_RESISTANCE, 0, 12 * 20)
                return@initRangeItemPeriodTask true
            }
        },
        "zombie_flint_and_steel" to vanillaPack.implement("zombie") {
            initRangeItemPeriodTask(3.0, Material.FLINT_AND_STEEL, period = 50L) {
                if (it.isInWaterOrRainOrBubbleColumn || it.isInLava) return@initRangeItemPeriodTask false
                it.location.run {
                    placeBlock(Material.FIRE)
                    playSound(Sound.ITEM_FLINTANDSTEEL_USE, 1.0F, 1.0F)
                    initDelayTask(40L) { if (block.type == Material.FIRE) placeBlock(Material.AIR) }
                }
                entity.applyEffect(PotionEffectType.FIRE_RESISTANCE, 0, 12 * 20)
                return@initRangeItemPeriodTask true
            }
        },
        "zombie_ender_pearl" to vanillaPack.implement("zombie") {
            initRangeItemDisposableTask(16.0, Material.ENDER_PEARL) {
                if (entity.isInLava) return@initRangeItemDisposableTask false
                if (entity.health <= 5.0) return@initRangeItemDisposableTask false
                entity.teleport(it)
                it.damage(0.0, entity)
                it.location.run {
                    playSound(Sound.ENTITY_ENDER_PEARL_THROW, 1.0F, 1.0F)
                    playSound(Sound.ENTITY_GENERIC_SMALL_FALL, 1.0F, 1.0F)
                    spawnParticle(Particle.PORTAL, 64, Vector(0.0, 0.0, 0.0),0.6)
                }
                entity.damage(5.0)
                return@initRangeItemDisposableTask true
            }
        },
        "zombie_shield" to vanillaPack.implement("zombie") {
            val itemType = Material.SHIELD
            entity.initEquipment(EquipmentSlot.OFF_HAND, itemType)
            initListener<EntityDamageByEntityEvent> {
                if (it.entity != entity || entity.isDead) return@initListener

                val shield = entity.equipment.itemInOffHand
                if (shield.type != itemType) return@initListener

                val shieldKey = NamespacedKey(instance!!, "shield")

                val shieldTaskId = UUID.randomUUID()

                if (it.damager !is LivingEntity) return@initListener
                val damager = it.damager as LivingEntity
                if (damager.equipment == null) return@initListener

                if (!entity.hasAI()) return@initListener
                if (entity.persistentDataContainer.get(shieldKey, PersistentDataType.BOOLEAN) == true) {
                    if (damager.equipment!!.itemInMainHand.type.isAxe()) {
                        entity.persistentDataContainer.set(shieldKey, PersistentDataType.BOOLEAN, false)
                        entity.setAI(false)
                        entity.damage(it.damage * 0.5, damager)
                        initDelayTask(40L) { entity.setAI(true) }

                        val modifier = AttributeModifier("Break shield bonus", -0.65, AttributeModifier.Operation.MULTIPLY_SCALAR_1)
                        entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.addModifier(modifier)
                        initDelayTask(100L) { entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.removeModifier(modifier) }

                        entity.location.playSound(Sound.ITEM_SHIELD_BREAK, 1.0F, 1.0F)
                        cancelTask(shieldTaskId)

                        entity.location.run {
                            spawnParticle(Particle.ITEM_CRACK,72, Vector(0.3, 0.5, 0.3), 0.0, ItemStack(Material.SHIELD))
                            spawnParticle(Particle.CRIT_MAGIC,64, Vector(0.3, 0.5, 0.3), 0.5)
                        }
                    } else {
                        damager.setMotionMultiplier(-0.5)
                        entity.location.playSound(Sound.ITEM_SHIELD_BLOCK, 1.0F, 1.0F)
                        it.isCancelled = true
                    }
                    return@initListener
                }
                if (Random.nextDouble() <= (0.35 * (multiplier + 1)).coerceIn(0.0, 0.85)) {
                    cancelTask(shieldTaskId)
                    entity.persistentDataContainer.set(shieldKey, PersistentDataType.BOOLEAN, true)
                    entity.location.spawnParticle(Particle.SPELL_INSTANT, 64, Vector(0.3, 0.5, 0.3), 1.0)
                    initDelayTask(100L, shieldTaskId) {
                        entity.persistentDataContainer.set(shieldKey, PersistentDataType.BOOLEAN, false)
                    }
                }
            }
        }
    ))
}