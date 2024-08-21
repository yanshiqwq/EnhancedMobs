package cn.yanshiqwq.enhanced_mobs.pack

import cn.yanshiqwq.enhanced_mobs.dsl.PackBuilder.Companion.pack
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.pack.Default
 *
 * @author yanshiqwq
 * @since 2024/8/20 下午4:37
 */
class Default {
    init {
        // TODO
        val builtInPack = pack {
            id("example")
            description("built-in pack")
            type(EntityType.ZOMBIE, "zombie_bloody_energized") {
                name("嗜血僵尸-势能")
                base {
                    health = 20.0
                    damage = 2.0
                    speed = 0.23
                }
                onDamage{
                    condition = byChance(0.65)
                    executor = {
                        effect(PotionEffectType.INCREASE_DAMAGE) {
                            duration = 20
                        }
                    }
                }
            }
            type(EntityType.ZOMBIE, "zombie_miner_igniter") {
                name("矿工僵尸-烧灼")
                base {
                    health = 24.0
                    damage = 2.0
                    speed = 0.24
                }
                equip {
                    head = ItemStack(Material.IRON_HELMET)
                    offHand = ItemStack(Material.FLINT_AND_STEEL)
                }
                onTimer {
                    period = 40
                    condition = allOf(
                        mob.hasTarget(),
                        distance(mob, mob.target!!) <= 5,
                        cooldown(80)
                    )
                    executor = {
                        placeBlock(
                            delay = 40L,
                            type = Material.FIRE,
                            onPlace = {
                                sound(Sound.ITEM_FLINTANDSTEEL_USE, pitch = 1.0F)
                            },
                            onRemove = {
                                sound(Sound.BLOCK_FIRE_EXTINGUISH, pitch = 1.0F)
                            }
                        )
                    }
                }
            }
        }
    }
}