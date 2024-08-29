package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageByEntityEvent

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.EventBuilder
 *
 * @author yanshiqwq
 * @since 2024/8/26 下午 7:47
 */
/**
 * 抽象类，用于构建事件处理逻辑
 *
 * @param T 事件处理逻辑的类型
 */
abstract class EventBuilder<T>: EntityHandler<T>, ConditionHandler<T>, CooldownHandler {
    override var runAfterEntityDead: Boolean = false
    override var cooldown: CooldownTimer? = null
    override val conditions = hashSetOf<T.() -> Boolean>()
    
    /**
     * 条件判断成功时要执行的操作
     */
    protected var executor: T.() -> Unit = {}
    
    /**
     * 条件判断失败时要执行的操作
     */
    protected var failed: T.() -> Unit = {}
    
    /**
     * 检查条件并执行事件
     *
     * @param entity 事件相关的实体
     * @param context 事件上下文
     * @param onCancel 取消事件时的回调
     */
    protected fun checkAndExecute(
        entity: LivingEntity,
        context: T,
        onCancel: () -> Unit
    ) {
        // 实体检测不通过则取消事件
        if (!checkIfValid(entity) || checkIfDead(entity)) {
            onCancel.invoke()
            return
        }
        
        if (!checkIfCooldownFinished()) return
        
        if (!checkCondition(context)) {
            failed.invoke(context)
        } else {
            executor.invoke(context)
        }
    }
    
    @Suppress("UNCHECKED_CAST")
    companion object {
        fun <T: Entity> EntityDamageByEntityEvent.damager(block: T.() -> Boolean) =
            damager<T>().run(block)
        fun <T: Entity> EntityDamageByEntityEvent.damager() =
            damager as T
    }
}