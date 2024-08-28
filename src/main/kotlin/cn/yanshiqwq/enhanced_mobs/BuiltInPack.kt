package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.dsl.GenericApi.base
import cn.yanshiqwq.enhanced_mobs.dsl.GenericApi.distanceFromTarget
import cn.yanshiqwq.enhanced_mobs.dsl.GenericApi.effect
import cn.yanshiqwq.enhanced_mobs.dsl.GenericApi.equip
import cn.yanshiqwq.enhanced_mobs.dsl.GenericApi.inAir
import cn.yanshiqwq.enhanced_mobs.dsl.GenericApi.inLiquid
import cn.yanshiqwq.enhanced_mobs.dsl.GenericApi.name
import cn.yanshiqwq.enhanced_mobs.dsl.GenericApi.onAttack
import cn.yanshiqwq.enhanced_mobs.dsl.GenericApi.onLiquid
import cn.yanshiqwq.enhanced_mobs.dsl.GenericApi.onTimer
import cn.yanshiqwq.enhanced_mobs.dsl.GenericApi.sound
import cn.yanshiqwq.enhanced_mobs.dsl.GenericApi.target
import cn.yanshiqwq.enhanced_mobs.dsl.PackBuilder.Companion.pack
import cn.yanshiqwq.enhanced_mobs.dsl.SkillApi.placeBlock
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffectType

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.pack.Default
 *
 * @author yanshiqwq
 * @since 2024/8/20 下午4:37
 */
object BuiltInPack {
    fun load() = pack("enhancedmobs", "built-in pack") {
        type(EntityType.ZOMBIE, "zombie_bloody_energized") {
            name("嗜血僵尸·势能")
            base {
                health = 20.0
                damage = 2.0
                speed = 0.23
            }
            onAttack {
                execute {
                    effect(PotionEffectType.INCREASE_DAMAGE) {
                        duration = 20
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
            onTimer(40, 80) {
                judge {
                    allOf(
                        distanceFromTarget { it <= 3.5 },
                        target {
                            allOf(inAir(), !inLiquid(), !onLiquid())
                        }
                    )
                }
                execute {
                    placeBlock(40, Material.FIRE) {
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
            onTimer(40, 120) {
                judge {
                    allOf(
                        distanceFromTarget { it <= 3.5 },
                        target {
                            allOf(inAir(), !inLiquid(), !onLiquid())
                        }
                    )
                }
                execute {
                    placeBlock(50, Material.LAVA) {
                        onPlace {
                            effect(PotionEffectType.FIRE_RESISTANCE) {
                                duration = 400
                            }
                            sound(Sound.ITEM_BUCKET_EMPTY_LAVA)
                            equip(EquipmentSlot.OFF_HAND, Material.BUCKET)
                        }
                        onRemove {
                            sound(Sound.ITEM_BUCKET_FILL_LAVA)
                            equip(EquipmentSlot.OFF_HAND, Material.LAVA_BUCKET)
                        }
                        onFailedRemove {
                            effect(PotionEffectType.SLOW) {
                                duration = 100
                            }
                            cancel()
                        }
                    }
                }
            }
        }
    }
}