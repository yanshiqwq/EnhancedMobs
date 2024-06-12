package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
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
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask
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
        data class Listener(val eventClass: KClass<out Event>, val function: (Event) -> Boolean)
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
        record.apply(this.entity, this.multiplier, attributeUUID, attributeName)
    }

    fun initEquipment(slot: EquipmentSlot, material: Material) {
        this.entity.equipment.setItem(slot, ItemStack(material))
    }

    fun initEnchant(slot: EquipmentSlot, record: Record.EnchantRecord) {
        record.apply(this.entity, multiplier, slot)
    }

    fun addEffect(
        effectType: PotionEffectType,
        amplifier: Int = 0,
        duration: Int = Int.MAX_VALUE,
        particle: Boolean = false,
        ambient: Boolean = true
    ) {
        this.entity.addPotionEffect(PotionEffect(effectType, duration, amplifier, ambient, particle))
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Event> initListener(noinline function: (T) -> Boolean) {
        this.listeners.add(Listener(T::class, function as (Event) -> Boolean))
    }

    fun initRangeUsingItemTask(
        distance: Double,
        before: Material,
        disposable: Boolean = true,
        after: Material = Material.AIR,
        slot: EquipmentSlot = EquipmentSlot.OFF_HAND,
        hasLineOfSight: Boolean = true,
        function: EnhancedMob.(LivingEntity) -> Boolean
    ) {
        initEquipment(slot, before)
        val task: EnhancedMob.() -> Boolean = lambda@{
            val target = entity.target ?: return@lambda false
            if (target.location.distance(entity.location) > distance) return@lambda false
            if (hasLineOfSight && !target.hasLineOfSight(entity)) return@lambda false

            if (!this.function(target)) return@lambda false
            if (disposable) initEquipment(slot, after)

            return@lambda true
        }
        if (disposable)
            initDisposableTask(function = task)
        else
            initPeriodTask(function = task)
    }

    fun initDisposableTask(taskUUID: UUID = UUID.randomUUID(), function: EnhancedMob.() -> Boolean) {
        val func = Runnable {
            if (!this.function()) return@Runnable
            tasks[taskUUID]?.cancel()
        }
        tasks[taskUUID] = Bukkit.getScheduler().runTaskTimer(instance!!, func, 0L, 20L)
    }

    fun initPeriodTask(taskUUID: UUID = UUID.randomUUID(), function: EnhancedMob.() -> Boolean, delay: Long = 0L, period: Long = 20L): UUID {
        tasks[taskUUID] = Bukkit.getScheduler().runTaskTimer(instance!!, Runnable { this.function() }, delay, period)
        initListener<EntityDeathEvent> {
            if(it.entity != entity) return@initListener false
            tasks[taskUUID]?.cancel() ?: return@initListener false
            return@initListener true
        }
        return taskUUID
    }
}