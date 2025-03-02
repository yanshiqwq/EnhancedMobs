package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.Utils.allOf
import cn.yanshiqwq.enhanced_mobs.Utils.clear
import cn.yanshiqwq.enhanced_mobs.Utils.createNamespacedKey
import cn.yanshiqwq.enhanced_mobs.Utils.dash
import cn.yanshiqwq.enhanced_mobs.Utils.distanceFromTarget
import cn.yanshiqwq.enhanced_mobs.Utils.effect
import cn.yanshiqwq.enhanced_mobs.Utils.equip
import cn.yanshiqwq.enhanced_mobs.Utils.inAir
import cn.yanshiqwq.enhanced_mobs.Utils.inLiquid
import cn.yanshiqwq.enhanced_mobs.Utils.isAxe
import cn.yanshiqwq.enhanced_mobs.Utils.isOnFire
import cn.yanshiqwq.enhanced_mobs.Utils.knockBack
import cn.yanshiqwq.enhanced_mobs.Utils.look
import cn.yanshiqwq.enhanced_mobs.Utils.onAttack
import cn.yanshiqwq.enhanced_mobs.Utils.onLiquid
import cn.yanshiqwq.enhanced_mobs.Utils.particle
import cn.yanshiqwq.enhanced_mobs.Utils.require
import cn.yanshiqwq.enhanced_mobs.Utils.sound
import cn.yanshiqwq.enhanced_mobs.Utils.spawnTNTPrimed
import cn.yanshiqwq.enhanced_mobs.Utils.stun
import cn.yanshiqwq.enhanced_mobs.Utils.target
import cn.yanshiqwq.enhanced_mobs.config.ConfigV1.levelRange
import cn.yanshiqwq.enhanced_mobs.dsl.AttributeBuilder.Companion.modifier
import cn.yanshiqwq.enhanced_mobs.dsl.AttributeBuilder.Companion.removeModifier
import cn.yanshiqwq.enhanced_mobs.dsl.LevelCurveBuilder.Companion.levelCurve
import cn.yanshiqwq.enhanced_mobs.dsl.PackBuilder.Companion.pack
import cn.yanshiqwq.enhanced_mobs.dsl.SkillApi.ToughnessMechanism.ToughnessReducer
import cn.yanshiqwq.enhanced_mobs.dsl.SkillApi.absorptionShield
import cn.yanshiqwq.enhanced_mobs.dsl.SkillApi.placeBlock
import cn.yanshiqwq.enhanced_mobs.dsl.SkillApi.toughness
import cn.yanshiqwq.enhanced_mobs.dsl.SkillApi.toughnessShield
import cn.yanshiqwq.enhanced_mobs.dsl.TimerBuilder.Companion.onTimer
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.attribute.AttributeModifier.Operation
import org.bukkit.entity.EntityType
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.potion.PotionEffectType
import taboolib.platform.util.buildItem
import taboolib.type.BukkitEquipment
import kotlin.math.pow

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.BuiltInPack
 *
 * @author yanshiqwq
 * @since 2024/8/29 下午 11:37
 */
object Initialize {
    fun color(level: Int) = when (level) {
        in 10 .. 70 -> "&a"
        in 70 .. 80 -> "&e"
        in 80 .. 90 -> "&5"
        in 90 .. levelRange.last -> "&c"
        else -> "&7"
    }
    val levelCurve = levelCurve("default") {
        maxHealth {
            base * 0.85 * (Math.E.pow(0.0225 * level) - 1)
        }
        attackDamage {
            base + 1.15 * (Math.E.pow(0.0225 * level) - 1)
        }
        knockbackResistance {
            base + 0.3 * (level / 95).toDouble().pow(2)
        }
        movementSpeed {
            when (level) {
                in levelRange.first ..65 -> base
                in 65..77 -> 1.1 * base
                in 77..85 -> 1.2 * base
                in 85.. levelRange.last -> 1.32 * base
                else -> base
            }
        }
        attackKnockback {
            when (level) {
                in levelRange.first ..65 -> base
                in 65..77 -> base + 0.35
                in 77..85 -> base + 0.85
                in 85.. levelRange.last -> base + 1.2
                else -> base
            }
        }
        followRange {
            base * (1 + level / 64)
        }
    }
    val pack = pack("enhancedmobs", "built-in pack") {
        type(EntityType.ZOMBIE, "zombie_bloody") {
            base {
                maxHealth(20.0)
                attackDamage(2.0)
                movementSpeed(0.23)
            }
            mechanic {
                onAttack {
                    effect(PotionEffectType.STRENGTH) {
                        duration = 20
                    }
                    effect(PotionEffectType.SPEED) {
                        duration = 20
                    }
                }
            }
        }
        
        type(EntityType.ZOMBIE, "zombie_igniter") {
            base {
                maxHealth(20.0)
                attackDamage(2.0)
                movementSpeed(0.23)
            }
            equip {
                offHand(Material.FLINT_AND_STEEL)
            }
            mechanic {
                onAttack {
                    require (target { isOnFire() })
                    it.damage *= 1.15
                }
                placeBlock(50, Material.FIRE) {
                    require(
                        distanceFromTarget(0.0..3.5),
                        target {
                            allOf(inAir(), !inLiquid(), !onLiquid())
                        }
                    )
                    onPlace {
                        effect(PotionEffectType.FIRE_RESISTANCE) {
                            duration = 300
                        }
                        sound(Sound.ITEM_FLINTANDSTEEL_USE)
                    }
                    onRemove {
                        sound(Sound.BLOCK_FIRE_EXTINGUISH, pitch = 2.0F)
                    }
                }
            }
        }
        
        type(EntityType.ZOMBIE, "zombie_lava") {
            base {
                maxHealth(20.0)
                attackDamage(2.0)
                movementSpeed(0.23)
            }
            equip {
                offHand(Material.LAVA_BUCKET)
            }
            mechanic {
                placeBlock(50, Material.LAVA) {
                    require(
                        distanceFromTarget(0.0..3.5),
                        target {
                            allOf(inAir(), !inLiquid(), !onLiquid())
                        }
                    )
                    onPlace {
                        effect(PotionEffectType.FIRE_RESISTANCE) {
                            duration = 400
                        }
                        sound(Sound.ITEM_BUCKET_EMPTY_LAVA)
                        equip(BukkitEquipment.OFF_HAND, Material.BUCKET)
                    }
                    onRemove {
                        sound(Sound.ITEM_BUCKET_FILL_LAVA)
                        equip(BukkitEquipment.OFF_HAND, Material.LAVA_BUCKET)
                    }
                    onFailed {
                        effect(PotionEffectType.SLOWNESS) {
                            duration = 100
                        }
                        stun(20)
                        cancel()
                    }
                }
            }
        }
        
        type(EntityType.ZOMBIE, "zombie_tnt") {
            base {
                maxHealth(20.0)
                attackDamage(2.0)
                movementSpeed(0.216)
            }
            equip {
                offHand(Material.TNT)
            }
            mechanic {
                onTimer(50) {
                    require(
                        distanceFromTarget(0.0..3.0),
                        target {
                            inAir()
                        }
                    )
                    clear(BukkitEquipment.OFF_HAND)
                    effect(PotionEffectType.RESISTANCE) {
                        duration = 50
                        amplifier = 2
                    }
                    target?.location?.run {
                        spawnTNTPrimed {
                            fuseTicks = 50
                            source = this@mechanic
                        }
                        sound(Sound.BLOCK_GRASS_PLACE)
                        sound(Sound.ENTITY_TNT_PRIMED)
                    }
                    cancel()
                }
            }
        }
        
        type(EntityType.ZOMBIE, "zombie_shield") {
            base {
                maxHealth(20.0)
                attackDamage(2.0)
                movementSpeed(0.216)
            }
            equip {
                offHand(Material.SHIELD)
            }
            mechanic {
                toughnessShield(-0.65, 0.50)
                toughness {
                    config(maxToughness = 16.0)
                    addReducer("axe_multiplier", ToughnessReducer.Operation.MULTIPLY_BASE, weaponType(
                        { it: Material -> it.isAxe() } to 0.5,
                        { it: Material -> !it.isAxe() } to -0.65
                    ))
                    onBlock {
                        sound(Sound.ITEM_SHIELD_BLOCK, SoundCategory.HOSTILE) // 发出盾牌格挡声音
                    }
                    onBreakStart {
                        clear(BukkitEquipment.OFF_HAND) // 移除盾牌
                        sound(Sound.ITEM_SHIELD_BREAK, SoundCategory.HOSTILE)  // 发出盾牌破坏声音
                        particle(Particle.ITEM, 32) { // 盾牌破坏粒子
                            speed = 0.1
                            data = buildItem(Material.SHIELD)
                            horizontal(0.2)
                            vertical(0.6)
                        }
                        effect(PotionEffectType.NAUSEA) { // 反胃效果
                            duration = config.breakDuration.toInt()
                        }
                    }
                    onBreakEnd {
                        equip(BukkitEquipment.OFF_HAND, Material.SHIELD)  // 重新装备盾牌
                        sound(Sound.ITEM_ARMOR_EQUIP_GENERIC, SoundCategory.HOSTILE)  // 发出装备声音
                    }
                }
            }
        }
        
        type(EntityType.ZOMBIE, "zombie_assault") {
            base {
                maxHealth(20.0)
                attackDamage(2.0)
                movementSpeed(0.288)
            }
            equip {
                offHand(Material.ENDER_PEARL)
            }
            mechanic {
                onTimer(100) {
                    val target = target ?: return@onTimer
                    require(
                        distanceFromTarget(0.0..16.0),
                        health > 5.0
                    ) {
                        look(target)
                        dash(0.05)
                    }

                    sound(Sound.ENTITY_ENDER_PEARL_THROW, SoundCategory.HOSTILE) // 发出投掷末影珍珠声音

                    teleport(target.location, PlayerTeleportEvent.TeleportCause.ENDER_PEARL) // 传送至目标
                    knockBack(0.05)

                    sound(Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE) // 发出传送声音
                    particle(Particle.PORTAL, 32) { // 传送粒子
                        speed = 1.0
                        horizontal(0.5)
                        vertical(1.0)
                    }

                    damage(5.0)
                    clear(BukkitEquipment.OFF_HAND)
                    cancel()
                }
            }
        }
        type(EntityType.SKELETON, "skeleton_guard") {
            base {
                maxHealth(20.0)
                attackDamage(2.0)
                movementSpeed(0.3)
            }
            equip {
                hand(buildItem(Material.WOODEN_SWORD)) {
                    modifier(Operation.ADD_NUMBER) {
                        attackDamage(0.0)
                    }
                }
            }
            mechanic {
                absorptionShield(200, 4.0) {
                    val key = createNamespacedKey("absorption_shield")
                    onBlock {
                        modifier(Operation.ADD_NUMBER, key) {
                            knockbackResistance(0.85)
                        }
                    }
                    onBroken {
                        removeModifier(key)
                    }
                }
            }
        }
    }
}