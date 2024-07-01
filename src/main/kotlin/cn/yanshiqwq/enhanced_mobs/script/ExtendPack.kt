package cn.yanshiqwq.enhanced_mobs.script

import cn.yanshiqwq.enhanced_mobs.data.Record.DoubleFactor
import cn.yanshiqwq.enhanced_mobs.data.Record.logFormula
import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.Utils.addModifier
import cn.yanshiqwq.enhanced_mobs.Utils.addModifierSafe
import cn.yanshiqwq.enhanced_mobs.Utils.percentHeal
import cn.yanshiqwq.enhanced_mobs.Utils.isAxe
import cn.yanshiqwq.enhanced_mobs.Utils.removeModifier
import cn.yanshiqwq.enhanced_mobs.managers.PackManager
import cn.yanshiqwq.enhanced_mobs.api.ListenerApi.onArrowDamage
import cn.yanshiqwq.enhanced_mobs.api.ListenerApi.onAttack
import cn.yanshiqwq.enhanced_mobs.api.ListenerApi.onBowShoot
import cn.yanshiqwq.enhanced_mobs.api.ListenerApi.onDeath
import cn.yanshiqwq.enhanced_mobs.api.ListenerApi.onMove
import cn.yanshiqwq.enhanced_mobs.api.ListenerApi.onPreDamage
import cn.yanshiqwq.enhanced_mobs.api.ListenerApi.onResurrect
import cn.yanshiqwq.enhanced_mobs.api.LocationApi.placeBlock
import cn.yanshiqwq.enhanced_mobs.api.LocationApi.playSound
import cn.yanshiqwq.enhanced_mobs.api.LocationApi.spawnEntity
import cn.yanshiqwq.enhanced_mobs.api.LocationApi.spawnParticle
import cn.yanshiqwq.enhanced_mobs.api.MobApi.effect
import cn.yanshiqwq.enhanced_mobs.api.MobApi.attribute
import cn.yanshiqwq.enhanced_mobs.api.MobApi.glowing
import cn.yanshiqwq.enhanced_mobs.api.MobApi.item
import cn.yanshiqwq.enhanced_mobs.api.MobApi.knockBack
import cn.yanshiqwq.enhanced_mobs.api.MobApi.reduceAir
import cn.yanshiqwq.enhanced_mobs.api.TaskApi.TaskId
import cn.yanshiqwq.enhanced_mobs.api.TaskApi.itemTask
import cn.yanshiqwq.enhanced_mobs.api.TaskApi.task
import cn.yanshiqwq.enhanced_mobs.api.TaskApi.timerTask
import cn.yanshiqwq.enhanced_mobs.dsl.MobDslBuilder.pack
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.attribute.Attribute.*
import org.bukkit.attribute.AttributeModifier
import org.bukkit.attribute.AttributeModifier.Operation.*
import org.bukkit.util.Vector
import java.util.*
import kotlin.random.Random
import kotlin.random.nextInt

object ExtendPack: PackManager.PackObj {
    private val manager = instance!!.packManager
    override fun get(): PackManager.Pack = pack("extend") {
        type("skeleton_frost", manager.implement(VanillaPack.get(), "skeleton")) {
            onBowShoot { projectile.isGlowing = true }
            onArrowDamage { target.freezeTicks = 140 + Random.nextInt(3..5) * 40 } // 冻结 6~10 秒
        }
        type("skeleton_iron_sword", manager.implement(VanillaPack.get(), "skeleton")) {
            item(EquipmentSlot.HAND, Material.IRON_SWORD)
            attribute(GENERIC_MAX_HEALTH, MULTIPLY_SCALAR_1, DoubleFactor { 0.65 * it })
            attribute(GENERIC_ATTACK_DAMAGE, MULTIPLY_SCALAR_1, logFormula(0.8))
            attribute(GENERIC_MOVEMENT_SPEED, MULTIPLY_SCALAR_1, logFormula(0.1))
        }

        type("spider_cobweb", manager.implement(VanillaPack.get(), "spider")) {
            itemTask(
                distance = 5.0,
                before = ItemStack(Material.COBWEB),
                period = (80 - multiplier * 10).toLong().coerceIn(50L..100L)
            ) {
                val block = it.location.block
                if (block.isLiquid || !block.isReplaceable) return@itemTask
                it.location.run {
                    placeBlock(Material.COBWEB)
                    playSound(Sound.ENTITY_SPIDER_AMBIENT, 1.0F, 2.0F)
                    task(taskId = TaskId("break_cobweb"), delay = 100L) {
                        if (block.type != Material.COBWEB) return@task
                        placeBlock(Material.AIR)
                        playSound(Sound.ENTITY_SPIDER_STEP, 1.0F, 2.0F)
                        item(EquipmentSlot.OFF_HAND, Material.COBWEB)
                    }
                }
            }
        }

        type("zombie_reduce_air", manager.implement(VanillaPack.get(), "zombie")) {
            onAttack { target.reduceAir(60 + multiplier * 5) }
        }
        type("zombie_shield", manager.implement(VanillaPack.get(), "zombie")) {
            item(EquipmentSlot.OFF_HAND, Material.SHIELD)
            onPreDamage {
                val shieldKey = NamespacedKey(instance!!, "shield")
                if (attacker.equipment!!.itemInMainHand.type.isAxe()) {
                    // 清除 AI
                    timerTask(
                        delay = 40,
                        setup = { entity.setAI(false) },
                        run = { entity.setAI(true) }
                    )

                    // 攻击力降低 85%, 破盾
                    val modifier = AttributeModifier("Break shield bonus", -0.85, MULTIPLY_SCALAR_1)
                    timerTask(delay = 100,
                        setup = {
                            entity.getAttribute(GENERIC_ATTACK_DAMAGE)?.addModifierSafe(modifier)
                            entity.persistentDataContainer.set(shieldKey, PersistentDataType.BOOLEAN, false)
                            entity.location.run {
                                playSound(Sound.ITEM_SHIELD_BREAK, 1.0F, 1.0F)
                                spawnParticle(Particle.ITEM_CRACK, 72, Vector(0.3, 0.5, 0.3), 0.0, ItemStack(Material.SHIELD))
                                spawnParticle(Particle.CRIT_MAGIC, 64, Vector(0.3, 0.5, 0.3), 0.5)
                            }
                            item(EquipmentSlot.OFF_HAND, Material.AIR)
                        },
                        run = {
                            entity.getAttribute(GENERIC_ATTACK_DAMAGE)?.removeModifier(modifier)
                            entity.persistentDataContainer.set(shieldKey, PersistentDataType.BOOLEAN, true)
                            entity.location.run {
                                playSound(Sound.ITEM_ARMOR_EQUIP_IRON, 1.0F, 1.0F)
                                spawnParticle(Particle.SPELL_INSTANT, 64, Vector(0.3, 0.5, 0.3), 1.0)
                            }
                            item(EquipmentSlot.OFF_HAND, Material.SHIELD)
                        }
                    )
                } else {
                    // 格挡攻击
                    attacker.knockBack(0.65)
                    entity.location.playSound(Sound.ITEM_SHIELD_BLOCK, 1.0F, 1.0F)
                    it.isCancelled = true
                }
            }
        }
        type("zombie_leader", manager.implement(VanillaPack.get(), "zombie")) {
            item(EquipmentSlot.HAND, Material.IRON_SWORD)
            item(EquipmentSlot.HEAD, Material.IRON_HELMET)
            attribute(ZOMBIE_SPAWN_REINFORCEMENTS, ADD_NUMBER, DoubleFactor(0.0..1.0) { 0.5 + 0.05 * it })
            glowing()
            onAttack { target.effect(PotionEffectType.WEAKNESS, 0, 12 * 20) }
        }
        type("zombie_cobweb", manager.implement(VanillaPack.get(), "zombie")) {
            itemTask(
                distance = 5.0,
                before = ItemStack(Material.COBWEB, 8),
                period = (100 - multiplier * 10).toLong().coerceIn(60L..120L)
            ) {
                val location = it.location.clone()
                val block = location.block
                if (block.isLiquid || !block.isReplaceable) return@itemTask
                location.run {
                    placeBlock(Material.COBWEB)
                    playSound(Sound.BLOCK_STONE_PLACE, 1.0F, 1.0F)
                    task(taskId = TaskId("break_cobweb"), delay = 50L) {
                        if (block.type != Material.COBWEB) return@task
                        playSound(Sound.BLOCK_STONE_BREAK, 1.0F, 1.0F)
                        item(EquipmentSlot.OFF_HAND, Material.COBWEB)
                    }
                }
                entity.equipment.itemInOffHand.subtract(1)
            }
            onMove {
                val modifier = AttributeModifier(
                    UUID.fromString("a5c065e0-2688-4ecf-99d5-637ed0bc17ef"),
                    "Cobweb Speed Boost",
                    8.0,
                    MULTIPLY_SCALAR_1
                )
                if (entity.location.block.type == Material.COBWEB)
                    entity.addModifier(GENERIC_MOVEMENT_SPEED, modifier)
                else
                    entity.removeModifier(GENERIC_MOVEMENT_SPEED, modifier)
            }
        }
        type("zombie_strength_cloud", manager.implement(VanillaPack.get(), "zombie")) {
            onDeath {
                entity.location.spawnEntity<AreaEffectCloud>(EntityType.AREA_EFFECT_CLOUD) {
                    color = PotionEffectType.INCREASE_DAMAGE.color
                    duration = 120
                    radius = 2.0F
                    addCustomEffect(PotionEffect(PotionEffectType.INCREASE_DAMAGE, 120, 1), true)
                }
            }
        }
        type("zombie_totem", manager.implement(VanillaPack.get(), "zombie")) {
            item(EquipmentSlot.OFF_HAND, Material.TOTEM_OF_UNDYING)
            onResurrect {
                entity.percentHeal(0.5)
                entity.effect(PotionEffectType.SPEED, 0)
                entity.effect(PotionEffectType.INCREASE_DAMAGE, 0)
            }
        }
        type("zombie_tnt", manager.implement(VanillaPack.get(), "zombie")) {
            itemTask(
                distance = 6.0,
                before = ItemStack(Material.TNT),
                after = ItemStack(Material.AIR)
            ) {
                val location = it.location
                location.run {
                    spawnEntity<TNTPrimed>(EntityType.PRIMED_TNT) {
                        fuseTicks = 40
                        source = entity
                        velocity = location.subtract(entity.location).toVector()
                    }
                    playSound(Sound.BLOCK_GRASS_PLACE, 1.0F, 0.75F)
                    playSound(Sound.ENTITY_TNT_PRIMED, 1.0F, 1.0F)
                }
                entity.effect(PotionEffectType.DAMAGE_RESISTANCE, 1, 50)
            }
        }
        type("zombie_anvil", manager.implement(VanillaPack.get(), "zombie")) {
            itemTask(
                distance = 5.0,
                before = ItemStack(Material.DAMAGED_ANVIL),
                after = ItemStack(Material.AIR)
            ) {
                val location = it.location.clone().apply { y += 3 }
                val anvilData = it.server.createBlockData(Material.DAMAGED_ANVIL)
                if (!location.block.canPlace(anvilData)) return@itemTask
                location.run {
                    spawnEntity<FallingBlock>(EntityType.FALLING_BLOCK) {
                        setHurtEntities(true)
                        cancelDrop = true
                        dropItem = false
                        damagePerBlock = (6 + multiplier * 1.5).toFloat()
                        blockData = anvilData
                    }
                    playSound(Sound.BLOCK_ANVIL_PLACE, 1.0F, 1.0F)
                    entity.effect(PotionEffectType.DAMAGE_RESISTANCE, 1, 50)
                }
            }
        }
        type("zombie_lava", manager.implement(VanillaPack.get(), "zombie")) {
            itemTask(
                distance = 5.0,
                before = ItemStack(Material.LAVA_BUCKET),
                after = ItemStack(Material.BUCKET),
                period = 60L
            ) {
                val location = it.location.clone()
                val block = location.block
                if (block.isLiquid || !block.canPlace(it.server.createBlockData(Material.LAVA))) return@itemTask
                location.run {
                    placeBlock(Material.LAVA)
                    playSound(Sound.ITEM_BUCKET_EMPTY_LAVA, 1.0F, 1.0F)
                    task(delay = 100L) {
                        if (block.type != Material.LAVA) return@task
                        placeBlock(Material.AIR)
                        playSound(Sound.ITEM_BUCKET_FILL_LAVA, 1.0F, 1.0F)
                        item(EquipmentSlot.OFF_HAND, Material.LAVA_BUCKET)
                    }
                }
                entity.effect(PotionEffectType.FIRE_RESISTANCE, 0, 72 * 20)
            }
        }
        type("zombie_fire_charge", manager.implement(VanillaPack.get(), "zombie")) {
            val powerFormula = DoubleFactor(0.0..16.0) {1 + it * 0.5}
            itemTask(
                distance = 16.0,
                before = ItemStack(Material.FIRE_CHARGE, 4),
                period = 60L
            ) {
                entity.location.playSound(Sound.ITEM_FIRECHARGE_USE, 1.0F, 1.0F)
                entity.launchProjectile(Fireball::class.java).apply {
                    velocity = entity.location.direction.normalize().multiply(0.65)
                    shooter = entity
                    yield = powerFormula.value(multiplier).toFloat()
                }
                entity.effect(PotionEffectType.DAMAGE_RESISTANCE, 0, 12 * 20)
                entity.effect(PotionEffectType.FIRE_RESISTANCE, 0, 12 * 20)
                entity.equipment.itemInOffHand.subtract(1)
            }
        }
        type("zombie_flint_and_steel", manager.implement(VanillaPack.get(), "zombie")) {
            itemTask(
                distance = 5.0,
                before = ItemStack(Material.FLINT_AND_STEEL),
                period = 60L
            ) {
                val location = it.location.clone()
                location.run {
                    if (block.isLiquid || it.isInRain) return@itemTask
                    if(!block.canPlace(Bukkit.createBlockData(Material.FIRE))) return@itemTask
                    timerTask(
                        delay = 40L,
                        setup = {
                            placeBlock(Material.FIRE)
                            playSound(Sound.ITEM_FLINTANDSTEEL_USE, 1.0F, 1.0F)
                        },
                        run = Runnable {
                            if (block.type != Material.FIRE) return@Runnable
                            placeBlock(Material.AIR)
                            playSound(Sound.BLOCK_FIRE_EXTINGUISH, 0.5F, 2.0F)
                        }
                    )
                }
                entity.effect(PotionEffectType.FIRE_RESISTANCE, 0, 12 * 20)
            }
        }
        type("zombie_ender_pearl", manager.implement(VanillaPack.get(), "zombie")) {
            itemTask(
                distance = 24.0,
                before = ItemStack(Material.ENDER_PEARL),
                after = ItemStack(Material.AIR)
            ) {
                if (entity.isInLava || entity.health <= 5.0) return@itemTask
                entity.location.playSound(Sound.ENTITY_ENDER_PEARL_THROW, 1.0F, 1.0F)
                entity.teleport(it)
                it.damage(0.0, entity)
                it.location.run {
                    playSound(Sound.ENTITY_GENERIC_BIG_FALL, 1.0F, 1.0F)
                    spawnParticle(Particle.PORTAL, 64, Vector(0.0, 0.0, 0.0),0.6)
                }
                entity.damage(5.0)
            }
        }
    }
}