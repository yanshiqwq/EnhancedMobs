package cn.yanshiqwq.enhanced_mobs.dsl

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.Cooldown
 *
 * @author yanshiqwq
 * @since 2024/8/21 下午8:08
 */
class Cooldown(private var time: Long) {
    // 记录上次操作的时间
    private var lastActionTime: Long = -1
    private fun update(gameTime: Long) {
        lastActionTime = gameTime
    }
    fun check(gameTime: Long): Boolean =
        if(gameTime - lastActionTime > time) {
            update(gameTime)
            true
        } else false
}