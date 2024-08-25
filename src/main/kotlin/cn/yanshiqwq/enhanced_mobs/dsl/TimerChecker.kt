package cn.yanshiqwq.enhanced_mobs.dsl

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.TimerChecker
 *
 * @author yanshiqwq
 * @since 2024/8/26 上午12:23
 */
abstract class TimerChecker: EntityConditionHandler {
    override var runAfterEntityDead: Boolean = false
    override val condition = arrayListOf<Boolean>()
    
    override fun judge(condition: Boolean) {
        this.condition.add(condition)
    }
    
    fun checkCondition(): Boolean {
        condition.add(!hasCooldown() || checkCooldown())
        return condition.all { it }
    }
}