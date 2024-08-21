package cn.yanshiqwq.enhanced_mobs.dsl

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.ICooldown
 *
 * @author yanshiqwq
 * @since 2024/8/21 下午8:33
 */
abstract class ICooldown {
    private var cooldown: Cooldown? = null
    fun cooldown(gameTime: Long, time: Long): Boolean {
        if (cooldown == null) cooldown = Cooldown(time)
        return cooldown!!.check(gameTime)
    }
}