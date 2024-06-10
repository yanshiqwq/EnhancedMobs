package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.Main.Companion.logger
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.event.entity.*
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.ln
import kotlin.random.Random

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.EnhancedMobs
 *
 * @author yanshiqwq
 * @since 2024/6/8 07:01
 */

val logFormula: (Double) -> (Double) -> Double = { scale ->
    {
        when (it) {
            in 0.0..Double.MAX_VALUE -> scale * ln(it + 1.0)
            else -> it
        }
    }
}

data class Pack(val id: String, val typeMap: Map<String, EnhancedMob.() -> Unit>){
    fun implement(id: String, function: EnhancedMob.() -> Unit): EnhancedMob.() -> Unit {
        return {
            typeMap[id]?.let { it(this) } ?: logger.warning("${Main.prefix} Cannot implement type $id.")
            function(this)
        }
    }
}

val vanillaPack = Pack("VANILLA", mapOf(
    "ZOMBIE" to {
        initAttribute(AttributeRecord(mapOf(
            Attribute.GENERIC_MAX_HEALTH to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor({1.4 * it})),
            Attribute.GENERIC_MOVEMENT_SPEED to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(0.25))),
            Attribute.GENERIC_ARMOR to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor({0.35 * it}, -1.0..5.0)),
            Attribute.GENERIC_ATTACK_DAMAGE to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(1.0))),
            Attribute.GENERIC_KNOCKBACK_RESISTANCE to AttributeRecordFactor(AttributeModifier.Operation.ADD_NUMBER, DoubleRecordFactor({0.065 * it})),
            Attribute.GENERIC_FOLLOW_RANGE to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor({0.15 * it}))
        )))
    },

    "SKELETON" to {
        initAttribute(AttributeRecord(mapOf(
            Attribute.GENERIC_MAX_HEALTH to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor({0.65 * it})),
            Attribute.GENERIC_ATTACK_DAMAGE to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(3.0))),
            Attribute.GENERIC_MOVEMENT_SPEED to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(0.25)))
        )))
        initEnchant(EquipmentSlot.HAND, EnchantRecord(mapOf(
            Enchantment.ARROW_KNOCKBACK to IntRecordFactor({0.5 * it}),
            Enchantment.ARROW_FIRE to IntRecordFactor({0.5 * it})
        )))
    },

    "SPIDER" to {
        initAttribute(AttributeRecord(mapOf(
            Attribute.GENERIC_MAX_HEALTH to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor({2.0 * it})),
            Attribute.GENERIC_MOVEMENT_SPEED to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(0.25))),
            Attribute.GENERIC_ATTACK_DAMAGE to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(1.0)))
        )))
    },

    "CREEPER" to {
        if (entity !is Creeper) throw IllegalArgumentException("Illegal EntityType: ${entity.type}")
        initAttribute(AttributeRecord(mapOf(
            Attribute.GENERIC_MAX_HEALTH to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor({0.35 * it})),
            Attribute.GENERIC_MOVEMENT_SPEED to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(0.25))),
            Attribute.GENERIC_KNOCKBACK_RESISTANCE to AttributeRecordFactor(AttributeModifier.Operation.ADD_NUMBER, DoubleRecordFactor({0.065 * it}))
        )))
        entity.apply {
            maxFuseTicks = IntRecordFactor({-2 * it + 30}, 15..32767).value(multiplier)
            explosionRadius = IntRecordFactor({3 * it + 3}, 0..32).value(multiplier)
        }
    },

    "GENERIC" to {
        initAttribute(AttributeRecord(mapOf(
            Attribute.GENERIC_MAX_HEALTH to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor({it})),
            Attribute.GENERIC_MOVEMENT_SPEED to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(0.25))),
            Attribute.GENERIC_ATTACK_DAMAGE to AttributeRecordFactor(AttributeModifier.Operation.MULTIPLY_SCALAR_1, DoubleRecordFactor(logFormula(1.0)))
        )))
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
        }
    },
    "ZOMBIE_TOTEM" to vanillaPack.implement("ZOMBIE") {
        initEquipment(EquipmentSlot.OFF_HAND, Material.TOTEM_OF_UNDYING)
        initListener<EntityResurrectEvent> {
            if (it.hand == null) return@initListener
            val maxHealth = it.entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: return@initListener
            it.entity.health = maxHealth * 0.5
            it.entity.addPotionEffect(PotionEffect(PotionEffectType.INCREASE_DAMAGE, Int.MAX_VALUE, 0))
        }
    },
    "ZOMBIE_TNT" to vanillaPack.implement("ZOMBIE") {
        periodRangeItem(4.0, Material.TNT) {
            it.location.spawnEntity<TNTPrimed>(EntityType.PRIMED_TNT){
                fuseTicks = 30
                source = entity
                location.direction.multiply(1.4)
            }
            it.world.run {
                playSound(it.location, Sound.BLOCK_GRASS_PLACE, 1.0F, 0.75F)
                playSound(it.location, Sound.ENTITY_TNT_PRIMED, 1.0F, 1.0F)
            }
        }
    },
    "ZOMBIE_LAVA" to vanillaPack.implement("ZOMBIE") {
        periodRangeItem(3.0, Material.LAVA_BUCKET, Material.BUCKET) {
            if (it.isInWaterOrBubbleColumn || it.isInLava) return@periodRangeItem
            it.location.placeBlock(Material.LAVA)
            it.world.playSound(it.location, Sound.ITEM_BUCKET_EMPTY_LAVA, 1.0F, 1.0F)
            addEffect(PotionEffectType.FIRE_RESISTANCE, 0, 1200, true)
        }
    },
    "ZOMBIE_FLINT_AND_STEEL" to vanillaPack.implement("ZOMBIE") {
        periodRangeItem(4.0, Material.FLINT_AND_STEEL) {
            if (it.isInWaterOrBubbleColumn || it.isInLava) return@periodRangeItem
            it.location.placeBlock(Material.FIRE)
            it.world.playSound(it.location, Sound.ITEM_FLINTANDSTEEL_USE, 1.0F, 1.0F)
            entity.equipment.setItemInOffHand(ItemStack(Material.AIR))
        }
    },
    "ZOMBIE_ENDER_PEARL" to vanillaPack.implement("ZOMBIE") {
        periodRangeItem(16.0, Material.ENDER_PEARL) {
            entity.teleport(it)
            it.damage(0.0, entity)
            it.world.run {
                playSound(it.location, Sound.ENTITY_ENDER_PEARL_THROW, 1.0F, 1.0F)
                playSound(it.location, Sound.ENTITY_GENERIC_SMALL_FALL, 1.0F, 1.0F)
            }
            entity.lastDamageCause = EntityDamageEvent(entity, DamageCause.FALL, 3.0)
            entity.equipment.setItemInOffHand(ItemStack(Material.AIR))
        }
    },
    "ZOMBIE_SHIELD" to vanillaPack.implement("ZOMBIE") {
        val itemType = Material.SHIELD
        initEquipment(EquipmentSlot.OFF_HAND, itemType)
        initListener<EntityDamageByEntityEvent> {
            if (it.entity != entity) return@initListener
            if (entity.isDead) return@initListener

            val shield = entity.equipment.itemInOffHand
            if (shield.type != itemType) return@initListener

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
            } else if (Random.nextDouble() <= (0.15 * (multiplier + 1)).coerceIn(0.0, 0.65)){
                holdShield(entity)
                Bukkit.getScheduler().runTaskLater(instance!!, Runnable {
                    if (entity.isGlowing) breakShield(entity, damager)
                }, 60L)
            }
        }
    }
))

fun holdShield(entity: Mob){
    entity.isGlowing = true
}

fun shieldBlock(damager: Entity){
    damager.velocity = damager.location.direction.multiply(-0.35)
    damager.world.playSound(damager.location, Sound.ITEM_SHIELD_BLOCK, 1.0F, 1.0F)
}

fun breakShield(entity: Mob, damager: Entity){
    entity.isGlowing = false
    val particleLocation = entity.location.apply { y += 1 }
    damager.world.run {
        playSound(damager.location, Sound.ITEM_SHIELD_BREAK, 1.0F, 1.0F)
        spawnParticle(Particle.ITEM_CRACK, particleLocation, 72, 0.3, 0.5, 0.3, 0.0, ItemStack(Material.SHIELD))
    }
}

fun Material.isAxe(): Boolean{
    return this in arrayOf(Material.WOODEN_AXE, Material.STONE_AXE, Material.GOLDEN_AXE, Material.IRON_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE)
}