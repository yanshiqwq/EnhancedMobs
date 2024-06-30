package cn.yanshiqwq.enhanced_mobs.api

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
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
        private var switch: () -> Unit = {}
        fun onSwitch(block: () -> Unit) { switch = block }
        fun switch() = switch.invoke()
    }
    fun EnhancedMob.task(taskId: TaskId = TaskId(), delay: Long = 0L, block: Runnable): TaskId {
        cancelTask(taskId)
        taskId.switch()
        val func = getRunnable(this) { block.run() }
        addTask(taskId, Bukkit.getScheduler().runTaskLater(instance!!, func, delay))
        return taskId
    }
    fun EnhancedMob.task(taskId: TaskId = TaskId(), delay: Long = 0L, period: Long, block: Runnable): TaskId {
        cancelTask(taskId)
        taskId.switch()
        val func = getRunnable(this) { block.run() }
        addTask(taskId, Bukkit.getScheduler().runTaskTimer(instance!!, func, delay, period))
        return taskId
    }

    private fun getRunnable(mob: EnhancedMob, block: Runnable): Runnable {
        return Runnable {
            if (mob.entity.isDead) return@Runnable
            block.run()
        }
    }
    private fun EnhancedMob.addTask(taskId: TaskId, task: BukkitTask) { tasks[taskId.id] = task }
    fun EnhancedMob.cancelTask(taskId: TaskId) {
        tasks[taskId.id].run {
            if (this == null) return@run
            taskId.switch()
            cancel()
        }
    }
    fun EnhancedMob.itemTask(
        distance: Double,
        before: ItemStack,
        after: ItemStack? = null,
        hasLineOfSight: Boolean = true,
        slot: EquipmentSlot = EquipmentSlot.OFF_HAND,
        block: EnhancedMob.(LivingEntity) -> Unit
    ) {
        val task = mobItemTask(distance, before, hasLineOfSight, slot) {
            this.block(it)
            if (after != null) entity.equipment.setItem(slot, ItemStack(after))
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
        block: EnhancedMob.(LivingEntity) -> Unit
    ) {
        val task = mobItemTask(distance, before, hasLineOfSight, slot) {
            this.block(it)
            if (after != null) entity.equipment.setItem(slot, ItemStack(after))
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