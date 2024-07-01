package cn.yanshiqwq.enhanced_mobs.api

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.api.MobApi.item
import org.bukkit.Bukkit
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
        fun onCancel(block: Runnable) { onCancel = block }
        fun cancel() = onCancel.run()
    }

    fun EnhancedMob.timerTask(taskId: TaskId = TaskId(), delay: Long = 0L, setup: Runnable, run: Runnable): TaskId {
        setup.run()
        taskId.onCancel(setup)
        return task(taskId, delay, run)
    }
    fun EnhancedMob.task(taskId: TaskId = TaskId(), delay: Long = 0L, block: Runnable): TaskId {
        cancelTask(taskId)
        val func = getRunnable { block.run() }
        return addTask(taskId, Bukkit.getScheduler().runTaskLater(instance!!, func, delay))
    }
    fun EnhancedMob.task(taskId: TaskId = TaskId(), delay: Long = 0L, period: Long, block: Runnable): TaskId {
        cancelTask(taskId)
        val func = getRunnable { block.run() }
        return addTask(taskId, Bukkit.getScheduler().runTaskTimer(instance!!, func, delay, period))
    }

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
    fun EnhancedMob.itemTask(
        distance: Double,
        before: ItemStack,
        after: ItemStack? = null,
        hasLineOfSight: Boolean = true,
        slot: EquipmentSlot = EquipmentSlot.OFF_HAND,
        block: (LivingEntity) -> Unit
    ) {
        item(slot, ItemStack(before))
        val task = mobItemTask(distance, before, hasLineOfSight, slot) {
            block(it)
            if (after != null) item(slot, ItemStack(after))
        }
        Bukkit.getScheduler().runTask(instance!!, task)
    }
    fun EnhancedMob.itemTask(
        distance: Double,
        delay: Long = 0L,
        period: Long,
        before: ItemStack,
        after: ItemStack? = null,
        hasLineOfSight: Boolean = true,
        slot: EquipmentSlot = EquipmentSlot.OFF_HAND,
        block: (LivingEntity) -> Unit
    ) {
        item(slot, ItemStack(before))
        val task = mobItemTask(distance, before, hasLineOfSight, slot) {
            block(it)
            if (after != null) item(slot, ItemStack(after))
        }
        Bukkit.getScheduler().runTaskTimer(instance!!, task, delay, period)
    }
    private inline fun EnhancedMob.mobItemTask(
        distance: Double,
        before: ItemStack,
        hasLineOfSight: Boolean,
        slot: EquipmentSlot = EquipmentSlot.OFF_HAND,
        crossinline block: EnhancedMob.(LivingEntity) -> Unit
    ): Runnable {
        return Runnable {
            val target = entity.target ?: return@Runnable
            if (target.location.distance(entity.location) > distance) return@Runnable
            if (entity.isDead) return@Runnable
            if (entity.equipment.getItem(slot).type != before.type) return@Runnable
            if (hasLineOfSight && !target.hasLineOfSight(entity)) return@Runnable
            block.invoke(this, entity.target!!)
        }
    }
}