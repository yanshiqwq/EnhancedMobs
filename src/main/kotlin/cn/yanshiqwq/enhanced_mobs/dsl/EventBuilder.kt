package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.entity.LivingEntity

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.EventBuilder
 *
 * @author yanshiqwq
 * @since 2024/8/26 下午7:47
 */
/**
 * 抽象类，用于构建事件处理逻辑
 *
 * @param T 事件处理逻辑的类型
 */
abstract class EventBuilder<T> : EntityChecker<T>, ConditionHandler<T>, ExecutionHandler<T>, CooldownHandler {
    override var runAfterEntityDead: Boolean = false
    override var cooldown: CooldownTimer? = null
    override val conditions = hashSetOf<(T) -> Boolean>()
    abstract override var executor: T.() -> Unit
    
    /**
     * 检查条件并执行事件
     *
     * @param entity 事件相关的实体
     * @param context 事件上下文
     * @param action 事件执行的逻辑
     * @param onCancel 取消事件时的回调
     */
    protected fun checkAndExecute(
        entity: LivingEntity,
        context: T,
        action: T.() -> Unit,
        onCancel: () -> Unit
    ) {
        // 实体检测不通过则取消事件
        if (!checkIfValid(entity) || checkIfDead(entity)) {
            onCancel()
            return
        }
        
        // 条件检测不通过则取消执行
        if (!checkIfCooldownFinished() || !checkCondition(context)) return
        
        action.invoke(context)
    }
}