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
import cn.yanshiqwq.enhanced_mobs.api.ListenerApi.onPreDamage
import cn.yanshiqwq.enhanced_mobs.api.ListenerApi.onResurrect
import cn.yanshiqwq.enhanced_mobs.api.LocationApi.placeBlock
import cn.yanshiqwq.enhanced_mobs.api.LocationApi.playSound
import cn.yanshiqwq.enhanced_mobs.api.LocationApi.spawnEntity
import cn.yanshiqwq.enhanced_mobs.api.LocationApi.spawnParticle
import cn.yanshiqwq.enhanced_mobs.api.MobApi.effect
import cn.yanshiqwq.enhanced_mobs.api.MobApi.attribute
import cn.yanshiqwq.enhanced_mobs.api.MobApi.baseAttribute
import cn.yanshiqwq.enhanced_mobs.api.MobApi.freeze
import cn.yanshiqwq.enhanced_mobs.api.MobApi.glowing
import cn.yanshiqwq.enhanced_mobs.api.MobApi.item
import cn.yanshiqwq.enhanced_mobs.api.MobApi.knockBack
import cn.yanshiqwq.enhanced_mobs.api.MobApi.reduceAir
import cn.yanshiqwq.enhanced_mobs.api.TaskApi
import cn.yanshiqwq.enhanced_mobs.api.TaskApi.TaskId
import cn.yanshiqwq.enhanced_mobs.api.TaskApi.itemTask
import cn.yanshiqwq.enhanced_mobs.api.TaskApi.tick
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

object SubPack: PackManager.PackObj {
    override fun get(): PackManager.Pack = pack("extend", PackManager.PackType.SUB) {
        type("frost") {
            onBowShoot { projectile.isGlowing = true }
            onArrowDamage {
                val tick = (100 + multiplier * 40).coerceIn(0.0..140.0 + 7 * 40.0)
                target.freeze(tick.toInt())
            }
        }
        type("iron_sword") {
            item(EquipmentSlot.HAND, Material.IRON_SWORD, dropChance = 0F) {
                val meta = item.itemMeta ?: return@item
                val modifier = AttributeModifier(
                    UUID.fromString("8ee84e93-7c40-4159-8089-57a32907c432"), "Random spawn bonus", 2.0, ADD_NUMBER)
                meta.addAttributeModifier(GENERIC_ATTACK_DAMAGE, modifier)
                item.itemMeta = meta
            }
            attribute(GENERIC_MAX_HEALTH, MULTIPLY_SCALAR_1, DoubleFactor { 0.65 * it })
            attribute(GENERIC_MOVEMENT_SPEED, MULTIPLY_SCALAR_1, logFormula(0.1))
        }

        type("spider_cobweb") {
            itemTask(
                type = TaskApi.TaskType.PERIOD,
                distance = 0.0..5.0,
                before = ItemStack(Material.COBWEB)
            ) {
                it.location.run {
                    if (block.isLiquid || !block.isReplaceable) return@itemTask
                    timerTask(
                        taskId = TaskId("cobweb"),
                        delay = 100L,
                        setup = {
                            placeBlock(Material.COBWEB)
                            playSound(Sound.BLOCK_STONE_PLACE, 1.0F, 1.0F)
                        },
                        run = {
                            if (block.type != Material.COBWEB) return@timerTask
                            block.breakNaturally()
                            playSound(Sound.BLOCK_STONE_BREAK, 1.0F, 1.0F)
                        }
                    )
                }
            }
            tick {
                val modifier = AttributeModifier(
                    UUID.fromString("a5c065e0-2688-4ecf-99d5-637ed0bc17ef"),
                    "Cobweb Speed Boost",
                    8.0,
                    MULTIPLY_SCALAR_1
                )
                if (entity.location.block.type == Material.COBWEB) {
                    entity.addModifier(GENERIC_MOVEMENT_SPEED, modifier)
                } else {
                    entity.removeModifier(GENERIC_MOVEMENT_SPEED, modifier)
                }
            }
        }

        type("reduce_air") {
            onAttack {
                target.reduceAir(60 + multiplier * 5)
            }
        }
        type("shield") {
            item(EquipmentSlot.OFF_HAND, Material.SHIELD, dropChance = 0.085F)
            onPreDamage {
                val shieldKey = NamespacedKey(instance!!, "shield")
                val armorModifier = AttributeModifier(UUID.fromString("1a4d8dc1-bd9d-4663-a6f0-0ec079fd4ef3"), "Shield bonus", 20.0, ADD_NUMBER)
                if (entity.persistentDataContainer.get(shieldKey, PersistentDataType.BOOLEAN) == false)
                    return@onPreDamage
                if (attacker.equipment!!.itemInMainHand.type.isAxe()) {
                    // 清除 AI
                    timerTask(
                        delay = 30,
                        setup = { entity.setAI(false) },
                        run = { entity.setAI(true) }
                    )

                    // 攻击力和移动速度降低 85%, 破盾
                    val modifier = AttributeModifier("Break shield bonus", -0.85, MULTIPLY_SCALAR_1)
                    timerTask(
                        delay = 100,
                        setup = {
                            entity.getAttribute(GENERIC_ATTACK_DAMAGE)?.addModifierSafe(modifier)
                            entity.getAttribute(GENERIC_MOVEMENT_SPEED)?.addModifierSafe(modifier)
                            entity.getAttribute(GENERIC_ARMOR)?.removeModifier(armorModifier)
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
                            entity.getAttribute(GENERIC_MOVEMENT_SPEED)?.removeModifier(modifier)
                            entity.getAttribute(GENERIC_ARMOR)?.addModifierSafe(armorModifier)
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
                    if (attacker is Player && attacker.gameMode == GameMode.CREATIVE) return@onPreDamage
                    attacker.knockBack(0.65)
                    entity.location.playSound(Sound.ITEM_SHIELD_BLOCK, 1.0F, 1.0F)
                    entity.getAttribute(GENERIC_ARMOR)?.addModifier(armorModifier)
                }
            }
        }
        type("zombie_leader") {
            item(EquipmentSlot.HAND, Material.IRON_SWORD)
            item(EquipmentSlot.HEAD, Material.IRON_HELMET)
            baseAttribute(GENERIC_MAX_HEALTH, 48.0)
            baseAttribute(ZOMBIE_SPAWN_REINFORCEMENTS, DoubleFactor(0.0..1.0) { 0.5 + 0.1 * it }.value(multiplier))
            glowing()
            onAttack {
                target.effect(PotionEffectType.WEAKNESS, 0, 7 * 20)
            }
        }
        type("cobweb") {
            itemTask(
                type = TaskApi.TaskType.PERIOD,
                distance = 0.0..5.0,
                before = ItemStack(Material.COBWEB, 3)
            ) {
                it.location.run {
                    if (block.isLiquid || !block.isReplaceable) return@itemTask
                    timerTask(
                        taskId = TaskId("cobweb"),
                        delay = 120L,
                        setup = {
                            placeBlock(Material.COBWEB)
                            playSound(Sound.BLOCK_STONE_PLACE, 1.0F, 1.0F)
                        },
                        run = {
                            if (block.type != Material.COBWEB) return@timerTask
                            block.breakNaturally()
                            playSound(Sound.BLOCK_STONE_BREAK, 1.0F, 1.0F)
                            entity.equipment.getItem(slot).subtract(1)
                        }
                    )
                }

            }
            tick {
                val modifier = AttributeModifier(
                    UUID.fromString("a5c065e0-2688-4ecf-99d5-637ed0bc17ef"),
                    "Cobweb Speed Boost",
                    8.0,
                    MULTIPLY_SCALAR_1
                )
                if (entity.location.block.type == Material.COBWEB) {
                    entity.addModifier(GENERIC_MOVEMENT_SPEED, modifier)
                } else {
                    entity.removeModifier(GENERIC_MOVEMENT_SPEED, modifier)
                }
            }
        }
        type("strength_cloud") {
            onDeath {
                entity.location.spawnEntity<AreaEffectCloud>(EntityType.AREA_EFFECT_CLOUD) {
                    color = PotionEffectType.INCREASE_DAMAGE.color
                    duration = 120
                    radius = 2.0F
                    addCustomEffect(PotionEffect(PotionEffectType.INCREASE_DAMAGE, 120, 1), true)
                }
            }
        }
        type("totem") { // TODO fix percentHeal
            item(EquipmentSlot.OFF_HAND, Material.TOTEM_OF_UNDYING)
            onResurrect {
                entity.percentHeal(0.5)
                entity.effect(PotionEffectType.SPEED, 0)
                entity.effect(PotionEffectType.INCREASE_DAMAGE, 0)
            }
        }
        type("tnt") {
            itemTask(
                type = TaskApi.TaskType.DISPOSABLE,
                distance = 0.0..7.0,
                before = ItemStack(Material.TNT),
                after = ItemStack(Material.AIR)
            ) {
                it.location.run {
                    spawnEntity<TNTPrimed>(EntityType.PRIMED_TNT) {
                        fuseTicks = 40
                        source = entity
                    }
                    playSound(Sound.BLOCK_GRASS_PLACE, 1.0F, 0.75F)
                    playSound(Sound.ENTITY_TNT_PRIMED, 1.0F, 1.0F)
                }
                entity.effect(PotionEffectType.DAMAGE_RESISTANCE, 1, 50)
            }
        }
        type("anvil") {
            itemTask(
                type = TaskApi.TaskType.DISPOSABLE,
                distance = 0.0..5.0,
                before = ItemStack(Material.DAMAGED_ANVIL),
                after = ItemStack(Material.AIR)
            ) {
                val anvilData = it.server.createBlockData(Material.DAMAGED_ANVIL)
                it.location.apply { y += 3 }.run {
                    if (!block.canPlace(anvilData)) return@itemTask
                    spawnEntity<FallingBlock>(EntityType.FALLING_BLOCK) {
                        setHurtEntities(true)
                        cancelDrop = true
                        dropItem = false
                        damagePerBlock = (6 + multiplier * 2).toFloat()
                        blockData = anvilData
                    }
                    playSound(Sound.BLOCK_ANVIL_PLACE, 1.0F, 1.0F)
                    entity.effect(PotionEffectType.DAMAGE_RESISTANCE, 1, 50)
                }
            }
        }
        type("lava") {
            itemTask(
                type = TaskApi.TaskType.PERIOD,
                distance = 0.0..5.0,
                before = ItemStack(Material.LAVA_BUCKET),
                after = ItemStack(Material.BUCKET)
            ) {
                it.location.run {
                    if (block.isLiquid || !block.canPlace(it.server.createBlockData(Material.LAVA))) return@itemTask
                    timerTask(
                        delay = 10 * 20L,
                        setup = {
                            placeBlock(Material.LAVA)
                            playSound(Sound.ITEM_BUCKET_EMPTY_LAVA, 1.0F, 1.0F)
                            it.fireTicks = -100
                            entity.effect(PotionEffectType.FIRE_RESISTANCE, 0, 72 * 20)
                        },
                        run = {
                            if (block.type != Material.LAVA) {
                                cancelTask()
                                return@timerTask
                            }
                            placeBlock(Material.AIR)
                            playSound(Sound.ITEM_BUCKET_FILL_LAVA, 1.0F, 1.0F)
                            item(EquipmentSlot.OFF_HAND, Material.LAVA_BUCKET)
                        }
                    )
                }

            }
        }
        type("fire_charge") {
            val powerFormula = DoubleFactor(0.0..16.0) {1 + it * 0.5}
            itemTask(
                type = TaskApi.TaskType.PERIOD,
                distance = 3.0..16.0,
                before = ItemStack(Material.FIRE_CHARGE, 5),
                period = 80L
            ) {
                entity.location.playSound(Sound.ITEM_FIRECHARGE_USE, 1.0F, 1.0F)
                entity.launchProjectile(Fireball::class.java).apply {
                    velocity = entity.location.direction.normalize().multiply(0.65)
                    yield = powerFormula.value(multiplier).toFloat()
                    setIsIncendiary(true)
                }
                entity.effect(PotionEffectType.DAMAGE_RESISTANCE, 0, 12 * 20)
                entity.effect(PotionEffectType.FIRE_RESISTANCE, 0, 12 * 20)
                entity.equipment.getItem(slot).subtract(1)
            }
        }
        type("flint_and_steel") {
            itemTask(
                type = TaskApi.TaskType.PERIOD,
                distance = 0.0..5.0,
                before = ItemStack(Material.FLINT_AND_STEEL),
                after = ItemStack(Material.FLINT)
            ) {
                it.location.run {
                    if (block.isLiquid || it.isInRain) return@itemTask
                    if(!block.canPlace(Bukkit.createBlockData(Material.FIRE))) return@itemTask
                    timerTask(
                        delay = 60L,
                        setup = {
                            placeBlock(Material.FIRE)
                            playSound(Sound.ITEM_FLINTANDSTEEL_USE, 1.0F, 1.0F)
                            entity.effect(PotionEffectType.FIRE_RESISTANCE, 0, 12 * 20)
                        },
                        run = {
                            if (block.type != Material.FIRE) return@timerTask
                            placeBlock(Material.AIR)
                            playSound(Sound.BLOCK_FIRE_EXTINGUISH, 0.5F, 2.0F)
                            item(EquipmentSlot.OFF_HAND, Material.FLINT_AND_STEEL)
                        }
                    )
                }
            }
        }
        type("ender_pearl") {
            itemTask(
                type = TaskApi.TaskType.DISPOSABLE,
                distance = 3.0..16.0,
                before = ItemStack(Material.ENDER_PEARL),
                after = ItemStack(Material.AIR)
            ) {
                if (it.isInLava) return@itemTask
                if (entity.health <= 5.0) return@itemTask
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