package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.dsl.MobApi.distanceFromTarget
import cn.yanshiqwq.enhanced_mobs.dsl.MobApi.effect
import cn.yanshiqwq.enhanced_mobs.dsl.MobApi.equip
import cn.yanshiqwq.enhanced_mobs.dsl.MobApi.inAir
import cn.yanshiqwq.enhanced_mobs.dsl.MobApi.inLiquid
import cn.yanshiqwq.enhanced_mobs.dsl.MobApi.onAttack
import cn.yanshiqwq.enhanced_mobs.dsl.MobApi.onLiquid
import cn.yanshiqwq.enhanced_mobs.dsl.MobApi.sound
import cn.yanshiqwq.enhanced_mobs.dsl.MobApi.stun
import cn.yanshiqwq.enhanced_mobs.dsl.MobApi.target
import cn.yanshiqwq.enhanced_mobs.dsl.PackBuilder.Companion.pack
import cn.yanshiqwq.enhanced_mobs.dsl.SkillApi.onShield
import cn.yanshiqwq.enhanced_mobs.dsl.SkillApi.placeBlock
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.potion.PotionEffectType
import taboolib.type.BukkitEquipment

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.PackBuiltIn
 *
 * @author yanshiqwq
 * @since 2024/8/29 下午11:37
 */
object PackConfig {
    val default = pack("enhancedmobs", "built-in pack") {
        type(EntityType.ZOMBIE, "zombie_bloody_energized") {
            name("嗜血僵尸·势能")
            base {
                health = 20.0
                damage = 2.0
                speed = 0.23
            }
            mechanic {
                onAttack {
                    execute {
                        effect(PotionEffectType.INCREASE_DAMAGE) {
                            duration = 20
                        }
                        effect(PotionEffectType.SPEED) {
                            duration = 20
                        }
                    }
                }
            }
        }
        
        type(EntityType.ZOMBIE, "zombie_miner_igniter") {
            name("矿工僵尸·烧灼")
            base {
                health = 20.0
                damage = 2.0
                speed = 0.24
            }
            equip {
                head(Material.IRON_HELMET)
                offHand(Material.FLINT_AND_STEEL)
            }
            mechanic {
                placeBlock(40, Material.FIRE) {
                    judge {
                        allOf(
                            distanceFromTarget { it <= 3.5 },
                            target {
                                allOf(inAir(), !inLiquid(), !onLiquid())
                            }
                        )
                    }
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
        
        type(EntityType.ZOMBIE, "zombie_miner_lava") {
            name("矿工僵尸·熔火")
            base {
                health = 20.0
                damage = 2.0
                speed = 0.24
            }
            equip {
                head(Material.IRON_HELMET)
                offHand(Material.LAVA_BUCKET)
            }
            mechanic {
                placeBlock(50, Material.LAVA) {
                    judge {
                        allOf(
                            distanceFromTarget { it <= 3.5 },
                            target {
                                allOf(inAir(), !inLiquid(), !onLiquid())
                            }
                        )
                    }
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
                        effect(PotionEffectType.SLOW) {
                            duration = 100
                        }
                        stun(20)
                        cancel()
                    }
                }
            }
        }
        
        type(EntityType.ZOMBIE, "zombie_guard_shield") {
            name("护卫僵尸-坚守")
            base {
                health = 24.0
                damage = 3.0
                speed = 0.216
            }
            equip {
                offHand(Material.SHIELD)
            }
            mechanic {
                onShield(50, -0.65, 0.15, 0.15)
            }
        }
    }
}