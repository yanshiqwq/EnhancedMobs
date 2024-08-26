package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.World

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.Cooldown
 *
 * @author yanshiqwq
 * @since 2024/8/21 下午8:08
 */

/**
 * 冷却时间计时器
 *
 * @param time 冷却时间，单位为刻
 */
class CooldownTimer(private val time: Long, private val world: World) {
    private fun getGameTime() = world.gameTime
    
    /**
     * 上次操作的时间戳，初始值为 `-1`，表示未操作过
     * 单位为刻
     */
    private var lastActionTime: Long = -1
    
    /**
     * 检查是否已超过冷却时间，并更新上次操作时间
     *
     * @return 如果冷却时间已过，则返回 `true` 并更新上次操作时间；否则返回 `false`
     */
    fun checkAndUpdate(): Boolean =
        if (getGameTime() - lastActionTime >= time) {
            lastActionTime = getGameTime()
            true
        } else false
    
    override fun toString() = "CooldownTimer(time=$time, remaining=${getGameTime() - lastActionTime})"
}
