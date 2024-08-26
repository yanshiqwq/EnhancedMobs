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
 * 用于处理冷却时间的接口
 *
 * @see CooldownTimer
 */
interface CooldownHandler {
    /**
     * 当前的冷却时间对象
     */
    var cooldown: CooldownTimer?
    
    /**
     * 检查是否存在冷却时间
     *
     * @return 存在冷却时间时返回 true
     */
    fun hasCooldown() = cooldown != null
    
    /**
     * 检查冷却时间是否已更新
     *
     * @return 冷却时间已更新时返回 true
     */
    fun checkCooldown() = cooldown?.checkAndUpdate() == true
    
    /**
     * 检查是否冷却完毕
     *
     * @return 无冷却时间或冷却完毕时返回 true
     */
    fun checkIfCooldownFinished() = !hasCooldown() || checkCooldown()
    
    /**
     * 设置冷却时间
     *
     * @param time 冷却时间长度
     * @param world 用于获取 gameTime 的世界
     * @see World.getGameTime
     */
    fun setCooldown(time: Long, world: World) {
        cooldown = CooldownTimer(time, world)
    }
}