package cn.yanshiqwq.enhanced_mobs.api

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.api.MobApi.item
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import java.util.*

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.script.Api.TaskApi
 *
 * @author yanshiqwq
 * @since 2024/6/30 下午4:08
 */
object TaskApi {
    data class TaskId(val id: String = UUID.randomUUID().toString()) {
        private var onCancel: Runnable = Runnable {}
        fun onCancel(block: Runnable) {
            onCancel = block
        }

        fun cancel() = onCancel.run()
    }

    enum class TimerTaskOnCancel {
        SETUP, RUN
    }

    fun EnhancedMob.timerTask(
        taskId: TaskId = TaskId(),
        delay: Long = 0L,
        onCancel: TimerTaskOnCancel = TimerTaskOnCancel.RUN,
        setup: Runnable,
        run: Runnable
    ): TaskId {
        if (tasks.containsKey(taskId)) return taskId
        setup.run()
        when (onCancel) {
            TimerTaskOnCancel.SETUP -> taskId.onCancel(setup)
            TimerTaskOnCancel.RUN -> taskId.onCancel(run)
        }
        return task(taskId, delay, run)
    }

    fun EnhancedMob.task(taskId: TaskId = TaskId(), delay: Long = 0L, block: Runnable): TaskId {
        if (hasTask(taskId)) return taskId
        return addTask(taskId, Bukkit.getScheduler().runTaskLater(instance!!, getRunnable(block), delay))
    }

    fun EnhancedMob.task(taskId: TaskId = TaskId(), delay: Long = 0L, period: Long, block: Runnable): TaskId {
        if (hasTask(taskId)) return taskId
        return addTask(taskId, Bukkit.getScheduler().runTaskTimer(instance!!, getRunnable(block), delay, period))
    }

    fun EnhancedMob.tick(taskId: TaskId = TaskId(), delay: Long = 0L, block: Runnable): TaskId =
        task(taskId, delay, 1L, getRunnable(block))

    private fun EnhancedMob.getRunnable(block: Runnable): Runnable {
        return Runnable {
            if (entity.isDead) return@Runnable
            block.run()
        }
    }
    private fun EnhancedMob.addTask(taskId: TaskId, task: BukkitTask): TaskId {
        tasks[taskId] = task
        return taskId
    }
    fun EnhancedMob.cancelTask(taskId: TaskId) {
        tasks[taskId]?.run {
            taskId.cancel()
            cancel()
        }
    }
    fun EnhancedMob.hasTask(taskId: TaskId) = tasks.containsKey(taskId)
    enum class TaskType {
        DISPOSABLE, PERIOD
    }
    data class ItemTaskDsl(
        var taskId: TaskId?,
        val distance: ClosedFloatingPointRange<Double>,
        val delay: Long,
        val period: Long,
        val before: ItemStack,
        val after: ItemStack?,
        val hasLineOfSight: Boolean,
        val slot: EquipmentSlot
    ) {
        fun cancelTask() = taskId?.cancel()
    }
    fun EnhancedMob.itemTask(
        type: TaskType,
        distance: ClosedFloatingPointRange<Double>,
        delay: Long = 0L,
        period: Long = 10L,
        before: ItemStack,
        after: ItemStack? = null,
        cancel: ItemStack = ItemStack(Material.AIR),
        hasLineOfSight: Boolean = true,
        slot: EquipmentSlot = EquipmentSlot.OFF_HAND,
        block: ItemTaskDsl.(LivingEntity) -> Unit
    ) {
        item(slot, ItemStack(before))
        val dsl = ItemTaskDsl(null, distance, delay, period, before, after, hasLineOfSight, slot)
        val task = Runnable {
            val target = entity.target ?: return@Runnable
            if (target.world != entity.world) return@Runnable
            if (target.location.distance(entity.location) !in distance) return@Runnable
            if (entity.isDead) return@Runnable
            if (entity.equipment.getItem(slot).type != before.type) return@Runnable
            if (hasLineOfSight && !target.hasLineOfSight(entity)) return@Runnable
            if (type == TaskType.DISPOSABLE || entity.equipment.getItem(slot) == cancel) dsl.taskId?.cancel()
            block(dsl, target)
            if (after != null) item(slot, ItemStack(after))
        }
        dsl.taskId = task(period = period, block = task)
    }
}