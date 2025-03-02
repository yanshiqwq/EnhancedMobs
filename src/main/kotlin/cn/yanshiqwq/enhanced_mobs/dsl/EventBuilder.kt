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
 * 构建事件处理逻辑
 */
abstract class EventBuilder: EntityHandler, CooldownHandler {
    override var runAfterEntityDead: Boolean = false
    override var cooldown: CooldownTimer? = null
    
    protected inline fun <T, R> checkAndExecute(
        entity: LivingEntity,
        context: T,
        value: R,
        executor: T.(R) -> Unit,
        canceller: () -> Unit
    ) = checkAndExecute(entity, canceller) {
        executor.invoke(context, value)
    }
    
    protected inline fun <T> checkAndExecute(
        entity: LivingEntity,
        context: T,
        executor: T.() -> Unit,
        canceller: () -> Unit
    ) = checkAndExecute(entity, canceller) {
        executor.invoke(context)
    }
    
    /**
     * 检查条件并执行事件
     *
     * @param entity 事件相关的实体
     * @param executor 事件执行器
     * @param canceller 事件取消器
     */
    protected inline fun checkAndExecute(
        entity: LivingEntity,
        canceller: () -> Unit,
        executor: () -> Unit
    ) {
        // 实体检测不通过则取消事件
        if (!checkIfValid(entity) || checkIfDead(entity)) {
            canceller.invoke()
            return
        }
        
        // 未冷却完毕则不执行
        if (!checkIfCooldownFinished()) return
        
        executor.invoke()
    }
    
    @Suppress("UNCHECKED_CAST")
    companion object {
        fun <T: Entity> EntityDamageByEntityEvent.damager(block: T.() -> Boolean) =
            damager<T>().run(block)
        fun <T: Entity> EntityDamageByEntityEvent.damager() =
            damager as T
    }
}