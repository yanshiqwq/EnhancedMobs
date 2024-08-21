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
class ListenerBuilder<T: EntityEvent>(
    private val eventClass: Class<T>
): IExecution<T>, Condition<T>() {
    var priority: EventPriority = EventPriority.NORMAL
    var ignoreCancelled: Boolean = true
    var runAfterEntityDead: Boolean = false
    override var executor: T.() -> Unit = {}
    fun build(
        mob: Mob,
        criteria: T.() -> Entity = { entity }
    ): ProxyListener = registerBukkitListener(eventClass, priority, ignoreCancelled) { event ->
        // 检查一般条件
        if (!check(event)) return@registerBukkitListener

        // 检查实体相关条件
        val entity = criteria.invoke(event) as? Mob ?: return@registerBukkitListener
        if (entity.uniqueId != mob.uniqueId) return@registerBukkitListener

        // 检查实体是否死亡
        if (entity.isDead && !runAfterEntityDead) return@registerBukkitListener

        // 执行块
        executor.invoke(event)
    }
}