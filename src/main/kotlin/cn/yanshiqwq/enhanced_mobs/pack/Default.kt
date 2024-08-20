package cn.yanshiqwq.enhanced_mobs.pack

import cn.yanshiqwq.enhanced_mobs.dsl.PackBuilder
import org.bukkit.Material
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
class Default(
    override val id: String = "default",
    override val description: String = "built-in pack"
): PackBuilder() {
    init {
        type(EntityType.ZOMBIE, "zombie_bloody_energized") {
            name("嗜血僵尸-势能")
            base {
                health = 20.0
                damage = 2.0
                speed = 0.23
            }
            onDamage(byChance(0.65)) {
                effect(PotionEffectType.INCREASE_DAMAGE) {
                    duration = 20
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
            var cooldown = false
            onTimer(40, condition = Boolean.all(
                mob.hasTarget(),
                distance(mob, mob.target!!) <= 5
            )) {
                if (cooldown) return@onTimer
                mob.target!!.location.block.run {
                    type = Material.FIRE
                    cooldown = true
                    fun removeFire() {
                        if (type !in arrayOf(Material.FIRE, Material.SOUL_FIRE)) return
                        cooldown = false
                        type = Material.AIR
                    }
                    delay(40) { removeFire() }
                    onDeath { removeFire() }
                }
            }
        }
    }
}