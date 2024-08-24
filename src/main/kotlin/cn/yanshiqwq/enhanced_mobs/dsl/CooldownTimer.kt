package cn.yanshiqwq.enhanced_mobs.dsl

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.Cooldown
 *
 * @author yanshiqwq
 * @since 2024/8/21 下午8:08
 */

/**
 * 冷却时间机制
 *
 * @param time 冷却时间，单位为刻
 */
class CooldownTimer(private var time: Long) {
    /**
     * 上次操作的时间戳，初始值为 `-1`，表示未操作过
     * 单位为刻
     */
    private var lastActionTime: Long = -1

    /**
     * 更新上次操作时间戳为当前游戏时间
     *
     * @param gameTime 当前游戏时间，单位为刻
     * @see org.bukkit.World.getGameTime
     */
    private fun update(gameTime: Long) {
        lastActionTime = gameTime
    }

    /**
     * 检查是否已超过冷却时间，并更新上次操作时间
     *
     * @param gameTime 当前游戏时间，单位为单位为刻
     * @see org.bukkit.World.getGameTime
     * @return 如果冷却时间已过，则返回 `true` 并更新上次操作时间；否则返回 `false`
     */
    fun checkAndUpdate(gameTime: Long): Boolean =
        if (gameTime - lastActionTime > time) {
            update(gameTime)
            true
        } else false
}
