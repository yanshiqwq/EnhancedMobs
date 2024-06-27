package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.Utils.heal
import cn.yanshiqwq.enhanced_mobs.managers.TypeManager
import cn.yanshiqwq.enhanced_mobs.script.DslBuilder
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.Mob
import org.bukkit.event.Event
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
    data class Listener(val eventClass: KClass<out Event>, val function: (Event) -> Unit)
    companion object {
        val attributeUUID: UUID = UUID.fromString("a8d0bc44-1534-43f0-a594-f74c7c91bc59")
        const val attributeName = "EnhancedMob Spawn Boost"

        val boostTypeKey = NamespacedKey(instance!!, "boost_type")
        val multiplierKey = NamespacedKey(instance!!, "multiplier")

        fun Entity.isEnhancedMob(): Boolean {
            if (this !is Mob) return false
            return instance!!.mobManager.has(this)
        }
        fun Entity.hasEnhancedMobData(): Boolean {
            if (this !is Mob) return false
            val container = persistentDataContainer
            return container.has(multiplierKey) && container.has(boostTypeKey)
        }
        fun Mob.asEnhancedMob(multiplier: Double, boostTypeKey: TypeManager.TypeKey, isReload: Boolean = true): EnhancedMob? {
            val mob = try {
                EnhancedMob(multiplier, this).apply { applyBoost(boostTypeKey) }
            } catch (e: NullPointerException) { return null }
            instance!!.mobManager.register(this.uniqueId, mob)
            if (isReload) heal()
            return mob
        }
    }

    init {
        entity.persistentDataContainer.set(multiplierKey, PersistentDataType.DOUBLE, multiplier)
        instance!!.mobManager.register(entity.uniqueId, this)
    }

    val listeners: ArrayList<Listener> = arrayListOf()

    fun applyBoost(boostTypeKey: TypeManager.TypeKey) {
        entity.persistentDataContainer.set(EnhancedMob.boostTypeKey, PersistentDataType.STRING, boostTypeKey.value())
        instance!!.typeManager.getType(boostTypeKey).function.invoke(this)
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Event> initListener(noinline function: (T) -> Unit): Listener {
        val listener = Listener(T::class, function as (Event) -> Unit)
        this.listeners.add(listener)
        return listener
    }

    private inline fun task(crossinline action: EnhancedMob.() -> Unit): EnhancedMob.() -> Boolean {
        return lambda@{
            if (entity.isDead) return@lambda false
            action()
            return@lambda true
        }
    }

    fun delayTask(init: DslBuilder.TypeBuilder.TaskDsl.() -> Unit): BukkitTask {
        val dsl = DslBuilder.TypeBuilder.TaskDsl().apply(init)
        val delay = dsl.delay
        val task = task { dsl.function }
        val func = Runnable { task.invoke(this@EnhancedMob) }
        return Bukkit.getScheduler().runTaskLater(instance!!, func, delay)
    }

    fun periodTask(init: DslBuilder.TypeBuilder.TaskDsl.() -> Unit): BukkitTask {
        val dsl = DslBuilder.TypeBuilder.TaskDsl().apply(init)
        val delay = dsl.delay
        val period = dsl.period
        val task = task { dsl.function }
        val func = Runnable { task.invoke(this) }
        return Bukkit.getScheduler().runTaskTimer(instance!!, func, delay, period)
    }

    private inline fun itemTask(dsl: DslBuilder.TypeBuilder.ItemTaskDsl, crossinline action: EnhancedMob.() -> Unit = {}): EnhancedMob.() -> Boolean {
        return lambda@{
            val target = entity.target ?: return@lambda false
            if (target.location.distance(entity.location) > dsl.distance) return@lambda false
            if (entity.equipment.getItem(dsl.slot).type != dsl.before.type) return@lambda false
            if (dsl.hasLineOfSight && !target.hasLineOfSight(entity)) return@lambda false
            if (!dsl.function.invoke(this, target)) return@lambda false
            task(action)
            return@lambda true
        }
    }

    fun disposableItemTask(init: DslBuilder.TypeBuilder.ItemTaskDsl.() -> Unit): BukkitTask {
        val dsl = DslBuilder.TypeBuilder.ItemTaskDsl().apply(init)
        val task = itemTask(dsl) { entity.equipment.setItem(dsl.slot, ItemStack(dsl.after)) }
        val func = Runnable { task.invoke(this) }
        return Bukkit.getScheduler().runTask(instance!!, func)
    }

    fun periodItemTask(init: DslBuilder.TypeBuilder.ItemTaskDsl.() -> Unit): BukkitTask {
        val dsl = DslBuilder.TypeBuilder.ItemTaskDsl().apply(init)
        val delay = dsl.delay
        val period = dsl.period
        val task = itemTask(dsl) { entity.equipment.setItem(dsl.slot, ItemStack(dsl.after)) }
        val func = Runnable { task.invoke(this) }
        return Bukkit.getScheduler().runTaskTimer(instance!!, func, delay, period)
    }
}