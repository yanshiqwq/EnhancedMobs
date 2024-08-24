package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.entity.Mob

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

    /**
     * 用于在计时器和监听器中作为冷却时间的条件，同时设定冷却时间
     * @see TimerBuilder
     * @see ListenerBuilder
     *
     * @param time 冷却时间，单位为刻
     * @return 冷却时间是否结束
     */
    fun Mob.cooldown(time: Long): Boolean {
        if (cooldown == null) cooldown = CooldownTimer(time)
        return cooldown!!.checkAndUpdate(world.gameTime)
    }
}
