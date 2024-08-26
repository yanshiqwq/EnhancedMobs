package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.entity.LivingEntity
import taboolib.common.platform.function.info

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
abstract class EventBuilder<T> : ExecutionHandler<T>, CooldownHandler, EntityConditionHandler<T>() {
    /**
     * 定义事件执行的具体逻辑
     */
    abstract override var executor: T.() -> Unit
    
    /**
     * 定义事件的冷却时间
     */
    abstract override var cooldown: CooldownTimer?
    
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
        if (!checkIfValid(entity)) {
            info("Cancelling task for invalid mob ${entity.uniqueId}")
            onCancel()
            return
        }
        
        if (checkIfDead(entity)) {
            info("Cancelling task for dead mob ${entity.uniqueId}")
            onCancel()
            return
        }
        
        if (!checkIfCooldownFinished()) {
            info("Cooldown check failed for mob ${entity.uniqueId}")
            return
        }
        
        if (!checkCondition(context)) {
            info("Condition check failed for mob ${entity.uniqueId}")
            return
        }
        
        action.invoke(context)
    }
}