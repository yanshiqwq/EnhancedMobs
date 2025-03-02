package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.function.registerBukkitListener
import java.io.Closeable

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.ListenerBuilder
 *
 * @author yanshiqwq
 * @since 2024/8/21 下午 7:27
 */
/**
 * 用于构建和注册实体事件监听器
 *
 * @param T 实体事件类型
 * @property eventClass 事件的类类型
 * @property priority 事件监听器的优先级
 * @property ignoreCancelled 是否忽略已取消的事件
 * @sample EntityEventListenerBuilder(EntityDeathEvent::class.java)
 */
class EntityEventListenerBuilder<T: EntityEvent>(
    private val eventClass: Class<T>,
    private val priority: EventPriority = EventPriority.NORMAL,
    private val ignoreCancelled: Boolean = true
): EventBuilder() {
    /**
     * 构建并注册事件监听器
     *
     * @param entity 需要监听的实体
     * @param criteria 用于确定事件中哪个对象为要监听的实体，默认为 EntityEvent.entity
     * @see EntityEvent.entity
     * @return 注册的 TabooLib 事件监听器代理
     */
    fun build(
        entity: LivingEntity,
        block: Closeable.(T) -> Unit,
        criteria: T.() -> Entity = { this.entity }
    ) = registerBukkitListener(eventClass, priority, ignoreCancelled) { event ->
        // 检查事件实体是否与当前实体相同
        val criteriaEntity = criteria.invoke(event)
        if (criteriaEntity.uniqueId != entity.uniqueId) return@registerBukkitListener
        checkAndExecute(entity, this, event, block) { close() }
    }
}