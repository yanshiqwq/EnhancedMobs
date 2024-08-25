package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.event.Event

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.EventChecker
 *
 * @author yanshiqwq
 * @since 2024/8/26 上午12:13
 */
abstract class EventChecker<T: Event>: EntityConditionHandler {
    override var runAfterEntityDead: Boolean = false
    override val condition = hashSetOf<(T) -> Boolean>(
        { !hasCooldown() || checkCooldown() }
    )
    
    override fun judge(condition: Boolean) {
        this.condition.add { condition }
    }
    
    fun judge(block: (T) -> Boolean) = condition.add(block)
    fun checkCondition(event: T) = condition.all {
        it.invoke(event)
    }
}