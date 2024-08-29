package cn.yanshiqwq.enhanced_mobs.dsl

import cn.yanshiqwq.enhanced_mobs.dsl.EventBuilder.Companion.damager
import cn.yanshiqwq.enhanced_mobs.dsl.MobApi.delay
import cn.yanshiqwq.enhanced_mobs.dsl.MobApi.effect
import cn.yanshiqwq.enhanced_mobs.dsl.MobApi.equip
import cn.yanshiqwq.enhanced_mobs.dsl.MobApi.isAxe
import cn.yanshiqwq.enhanced_mobs.dsl.MobApi.isFullCharge
import cn.yanshiqwq.enhanced_mobs.dsl.MobApi.look
import cn.yanshiqwq.enhanced_mobs.dsl.MobApi.onDamage
import cn.yanshiqwq.enhanced_mobs.dsl.MobApi.onDeath
import cn.yanshiqwq.enhanced_mobs.dsl.MobApi.particle
import cn.yanshiqwq.enhanced_mobs.dsl.MobApi.sound
import cn.yanshiqwq.enhanced_mobs.dsl.MobApi.stun
import org.bukkit.*
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Mob
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.service.PlatformExecutor
import taboolib.platform.util.getEquipment
import taboolib.type.BukkitEquipment

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.SkillApi
 *
 * @author yanshiqwq
 * @since 2024/8/24 下午 6:23
 */
object SkillApi {
    fun Mob.onShield(
        breakDuration: Long,
        preMultiplier: Double,
        postMultiplier: Double,
        breakMultiplier: Double
    ) {
        var isShieldBroken = false
        onDamage {
            judge {
                !isShieldBroken
            }
            execute {
                if (!damager<HumanEntity> {
                    getEquipment(BukkitEquipment.HAND)?.isAxe() == true &&
                    isFullCharge()
                }) {
                    sound(Sound.ITEM_SHIELD_BLOCK, SoundCategory.HOSTILE)
                    damage *= (1 + preMultiplier)
                    return@execute
                }
                
                damage *= (1 + breakMultiplier)
                
                stun(breakDuration)
                look(pitch = 60.0F)
                particle(Particle.ITEM_CRACK, 32, 0.1) {
                    horizontal(0.2)
                    vertical(0.6)
                    data = ItemStack(Material.SHIELD)
                }
                effect(PotionEffectType.CONFUSION) {
                    duration = breakDuration.toInt()
                }
                
                isShieldBroken = true
                sound(Sound.ITEM_SHIELD_BREAK, SoundCategory.HOSTILE)
                equip(BukkitEquipment.OFF_HAND, Material.AIR)
                delay(breakDuration) {
                    execute {
                        isShieldBroken = false
                        
                        sound(Sound.ITEM_ARMOR_EQUIP_GENERIC, SoundCategory.HOSTILE)
                        equip(BukkitEquipment.OFF_HAND, Material.SHIELD)
                    }
                }
            }
            failed {
                damage *= (1 + postMultiplier)
            }
        }
    }
    
    /**
     * 在目标的位置放置一个方块，并在一定时间后或实体死亡时移除该方块
     *
     * @param delay 延迟时间，单位为刻，在该时间后移除方块
     * @param type 要放置的方块的类型
     * @param removeOnDeath 是否在实体死亡时移除方块
     */
    fun Mob.placeBlock(
        delay: Long = 0,
        type: Material = Material.AIR,
        removeOnDeath: Boolean = true,
        init: BlockPlacer.() -> Unit
    ) = BlockPlacer(delay, type, removeOnDeath).apply(init).build(this)
    
    class BlockPlacer(
        private val period: Long,
        private val blockType: Material,
        private val removeOnDeath: Boolean
    ) : TimerBuilder(period) {
        
        private var onPlace: PlatformExecutor.PlatformTask.() -> Unit = {}
        private var onRemove: PlatformExecutor.PlatformTask.() -> Unit = {}
        private var onFailed: PlatformExecutor.PlatformTask.() -> Unit = {}
        
        fun onPlace(block: PlatformExecutor.PlatformTask.() -> Unit) { onPlace = block }
        fun onRemove(block: PlatformExecutor.PlatformTask.() -> Unit) { onRemove = block }
        fun onFailed(block: PlatformExecutor.PlatformTask.() -> Unit) { onFailed = block }
        
        fun build(entity: Mob) {
            execute {
                val target = entity.target ?: return@execute
                val block = target.location.block
                block.type = blockType
                onPlace.invoke(this)
                
                entity.delay(period) {
                    judge {
                        block.type == blockType
                    }
                    execute {
                        onRemove.invoke(this)
                        block.type = Material.AIR
                    }
                    failed {
                        onFailed.invoke(this)
                    }
                }
                
                if (removeOnDeath) {
                    entity.onDeath {
                        judge {
                            block.type == blockType
                        }
                        execute {
                            block.type = Material.AIR
                            close()
                        }
                    }
                }
            }
            super.build(entity)
        }
    }
}