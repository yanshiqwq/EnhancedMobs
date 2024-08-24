package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.entity.Entity
import org.bukkit.entity.Mob
import org.bukkit.event.entity.EntityEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.function.registerBukkitListener

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.ListenerBuilder
 *
 * @author yanshiqwq
 * @since 2024/8/21 下午7:27
 */

/**
 * 用于构建和注册实体事件监听器的构建器类
 *
 * @param T 实体事件类型
 * @property eventClass 事件的类类型
 * @sample ListenerBuilder(EntityDeathEvent::class.java)
 */
class ListenerBuilder<T: EntityEvent>(val eventClass: Class<T>) : ExecutionHandler<T>, ConditionHandler<T> {

    /**
     * 事件监听器的优先级
     */
    var priority: EventPriority = EventPriority.NORMAL

    /**
     * 是否忽略已取消的事件
     */
    var ignoreCancelled: Boolean = true

    /**
     * 是否在实体死亡后仍然执行监听器
     */
    var runAfterEntityDead: Boolean = false

    override var executor: T.() -> Unit = {}
    override var condition: T.() -> Boolean = { true }
    override var cooldown: CooldownTimer? = null

    /**
     * 构建并注册事件监听器。
     *
     * @param entity 需要监听的实体
     * @param criteria 用于确定事件中哪个对象为要监听的实体，默认为 EntityEvent.entity
     * @see EntityEvent.entity
     * @return 注册的 TabooLib 事件监听器代理
     */
    inline fun build(
        entity: Entity,
        crossinline criteria: T.() -> Entity = { entity }
    ): ProxyListener = registerBukkitListener(eventClass, priority, ignoreCancelled) { event ->
        // 检查一般条件
        if (!check(event)) return@registerBukkitListener

        // 检查实体相关条件
        val criteriaEntity = criteria.invoke(event) as? Mob ?: return@registerBukkitListener
        if (criteriaEntity.uniqueId != entity.uniqueId) return@registerBukkitListener

        // 检查实体是否死亡
        if (entity.isDead && !runAfterEntityDead) return@registerBukkitListener

        // 执行块
        executor.invoke(event)
    }
}
