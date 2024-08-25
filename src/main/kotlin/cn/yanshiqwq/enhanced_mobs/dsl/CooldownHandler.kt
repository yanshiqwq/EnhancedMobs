package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.World

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.ICooldown
 *
 * @author yanshiqwq
 * @since 2024/8/21 下午8:33
 */

/**
 * 处理冷却时间机制
 * @see CooldownTimer
 */
interface CooldownHandler {
    /**
     * 当前的冷却时间对象
     */
    var cooldown: CooldownTimer?
    fun hasCooldown() = cooldown != null
    fun checkCooldown() = cooldown?.checkAndUpdate() == true
    fun setCooldown(time: Long, world: World) {
        cooldown = CooldownTimer(time, world)
    }
}
