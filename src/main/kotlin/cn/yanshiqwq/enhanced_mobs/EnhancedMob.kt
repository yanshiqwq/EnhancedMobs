package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.Utils.heal
import cn.yanshiqwq.enhanced_mobs.Utils.initEquipment
import cn.yanshiqwq.enhanced_mobs.data.Record
import cn.yanshiqwq.enhanced_mobs.managers.MobTypeManager
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitTask
import java.lang.NullPointerException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.EnhancedMob
 *
 * @author yanshiqwq
 * @since 2024/6/7 05:29
 */
class EnhancedMob(val multiplier: Double, val entity: Mob) {
    companion object {
        val boostTypeKey = NamespacedKey(instance!!, "boost_type")
        val multiplierKey = NamespacedKey(instance!!, "multiplier")
        data class Listener(val eventClass: KClass<out Event>, val function: (Event) -> Unit)
        fun Mob.asEnhancedMob(multiplier: Double, boostTypeId: MobTypeManager.TypeId): EnhancedMob? {
            val mob = try {
                EnhancedMob(multiplier, this).apply { initBoost(boostTypeId) }
            } catch (e: NullPointerException) { return null }
            heal()
            return mob
        }
    }

    init {
        entity.persistentDataContainer.set(multiplierKey, PersistentDataType.DOUBLE, multiplier)
        instance!!.mobManager!!.register(entity.uniqueId, this)
    }

    val listeners: ArrayList<Listener> = arrayListOf()
    private val tasks: MutableMap<UUID, BukkitTask> = mutableMapOf()

    fun initBoost(boostTypeId: MobTypeManager.TypeId) {
        entity.persistentDataContainer.set(boostTypeKey, PersistentDataType.STRING, boostTypeId.value())
        instance!!.mobTypeManager.queryTypeFunction(boostTypeId)?.invoke(this)
    }

    fun initAttribute(record: Record.AttributeRecord) {
        val attributeUUID: UUID = UUID.fromString("a8d0bc44-1534-43f0-a594-f74c7c91bc59")
        val attributeName = "EnhancedMob Spawn Boost"
        record.apply(entity, multiplier, attributeUUID, attributeName)
    }

    fun initEnchant(slot: EquipmentSlot, record: Record.EnchantRecord) {
        record.apply(entity, multiplier, slot)
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Event> initListener(noinline function: (T) -> Unit): Listener {
        val listener = Listener(T::class, function as (Event) -> Unit)
        this.listeners.add(listener)
        return listener
    }

    fun initRangeItemPeriodTask(
        distance: Double,
        before: Material,
        after: Material? = null,
        slot: EquipmentSlot = EquipmentSlot.OFF_HAND,
        period: Long = 20L,
        hasLineOfSight: Boolean = true,
        id: UUID = UUID.randomUUID(),
        function: EnhancedMob.(LivingEntity) -> Boolean
    ): BukkitTask? {
        entity.initEquipment(slot, before)
        val task: EnhancedMob.() -> Boolean = lambda@{
            val target = entity.target ?: return@lambda false
            if (target.location.distance(entity.location) > distance) return@lambda false
            if (entity.equipment.getItem(slot).type != before) return@lambda false
            if (hasLineOfSight && !target.hasLineOfSight(entity)) return@lambda false
            if (!this.function(target)) return@lambda false
            if (after != null) entity.initEquipment(slot, after)
            return@lambda true
        }
        return initPeriodTask(period, id = id, function = task)
    }

    fun initRangeItemPeriodTask(
        distance: Double,
        before: ItemStack,
        after: Material = Material.AIR,
        slot: EquipmentSlot = EquipmentSlot.OFF_HAND,
        period: Long = 20L,
        hasLineOfSight: Boolean = true,
        id: UUID = UUID.randomUUID(),
        function: EnhancedMob.(LivingEntity) -> Boolean
    ): BukkitTask? {
        val task: EnhancedMob.(LivingEntity) -> Boolean = lambda@{
            if (entity.equipment.getItem(slot).type == after) {
                cancelTask(id)
                return@lambda false
            }
            val target = entity.target ?: return@lambda false
            if (!this.function(target)) return@lambda false
            entity.equipment.getItem(slot).add(-1)
            return@lambda true
        }
        return initRangeItemPeriodTask(distance, before.type, null, slot, period, hasLineOfSight, id, task)
    }

    fun initRangeItemDisposableTask(
        distance: Double,
        before: Material,
        after: Material = Material.AIR,
        slot: EquipmentSlot = EquipmentSlot.OFF_HAND,
        hasLineOfSight: Boolean = true,
        id: UUID = UUID.randomUUID(),
        function: EnhancedMob.(LivingEntity) -> Boolean
    ): BukkitTask? {
        entity.initEquipment(slot, before)
        val task: EnhancedMob.() -> Boolean = lambda@{
            val target = entity.target ?: return@lambda false
            if (target.location.distance(entity.location) > distance) return@lambda false
            if (hasLineOfSight && !target.hasLineOfSight(entity)) return@lambda false
            if (!this.function(target)) return@lambda false
            entity.initEquipment(slot, after)
            return@lambda true
        }
        return initDisposableTask(id = id, function = task)
    }

    private fun initDisposableTask(period: Long = 20L, delay: Long = 0L, id: UUID = UUID.randomUUID(), function: EnhancedMob.() -> Boolean): BukkitTask? {
        val func = Runnable {
            if (!this.function()) return@Runnable
            cancelTask(id)
        }
        tasks[id] = Bukkit.getScheduler().runTaskTimer(instance!!, func, delay, period)
        return tasks[id]
    }

    private fun initPeriodTask(period: Long = 20L, delay: Long = 0L, id: UUID = UUID.randomUUID(), function: EnhancedMob.() -> Boolean): BukkitTask? {
        val func = Runnable { this.function() }
        tasks[id] = Bukkit.getScheduler().runTaskTimer(instance!!, func, delay, period)
        initListener<EntityDeathEvent> {
            if(it.entity != entity) return@initListener
            cancelTask(id)
        }
        return tasks[id]
    }

    fun initDelayTask(delay: Long = 20L, id: UUID = UUID.randomUUID(), function: EnhancedMob.() -> Unit): BukkitTask? {
        val func = Runnable { this.function() }
        tasks[id] = Bukkit.getScheduler().runTaskLater(instance!!, func, delay)
        initListener<EntityDeathEvent> {
            if(it.entity != entity) return@initListener
            cancelTask(id)
        }
        return tasks[id]
    }

    fun cancelTask(id: UUID) {
        tasks[id]?.cancel()
    }
}