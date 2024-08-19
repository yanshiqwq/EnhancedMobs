package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.Bukkit
import org.bukkit.entity.Mob
import org.bukkit.event.Event
import org.bukkit.scheduler.BukkitRunnable
import taboolib.common.platform.function.registerBukkitListener
import taboolib.common.platform.event.EventPriority
import taboolib.platform.BukkitPlugin

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.SkillBuilder
 *
 * @author yanshiqwq
 * @since 2024/8/19 下午9:26
 */
class SkillApi(val entity: Mob) {
    inline fun <reified T: Mob> property(crossinline block: T.() -> Unit) {
        if (entity !is T) throw IllegalArgumentException("Illegal EntityType: ${entity.type}")
        entity.block()
    }

    inline fun <reified T : Event> listen(
        priority: EventPriority = EventPriority.NORMAL,
        ignoreCancelled: Boolean = true,
        runAfterEntityDead: Boolean = false,
        crossinline block: (T) -> Unit
    ) = registerBukkitListener(T::class.java, priority, ignoreCancelled) {
        if (entity.isDead && !runAfterEntityDead) return@registerBukkitListener
        block(it)
    }

    fun timer(interval: Long = 20L, delay: Long = 0L, async: Boolean = false, task: BukkitRunnable) = Bukkit.getServer().scheduler.run {
        if (async) {
            task.runTaskTimerAsynchronously(BukkitPlugin.getInstance(), delay, interval)
        } else {
            task.runTaskTimer(BukkitPlugin.getInstance(), delay, interval)
        }
    }

    fun delay(delay: Long = 0L, task: BukkitRunnable, async: Boolean = false) = Bukkit.getServer().scheduler.run {
        if (async) {
            task.runTaskLaterAsynchronously(BukkitPlugin.getInstance(), delay)
        } else {
            task.runTaskLater(BukkitPlugin.getInstance(), delay)
        }
    }
}