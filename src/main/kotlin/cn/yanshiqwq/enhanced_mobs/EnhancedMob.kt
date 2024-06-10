package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import org.bukkit.event.Event
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask
import java.util.*
import kotlin.reflect.KClass


class EnhancedMob(val multiplier: Double, val entity: Mob) {
    companion object {
        val multiplierKey = NamespacedKey(instance!!, "multiplier")
    }

    init {
        entity.persistentDataContainer.set(multiplierKey, PersistentDataType.DOUBLE, multiplier)
        instance!!.mobManager!!.register(entity.uniqueId, this)
    }

    val listeners = mutableMapOf<KClass<out Event>, (Event) -> Unit>()

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
    inline fun <reified T : Event> initListener(noinline function: (T) -> Unit) {
        this.listeners[T::class] = function as (Event) -> Unit
    }

    fun periodRangeItem(
        distance: Double,
        before: Material,
        after: Material = Material.AIR,
        slot: EquipmentSlot = EquipmentSlot.OFF_HAND,
        shouldSee: Boolean = true,
        function: EnhancedMob.(LivingEntity) -> Unit
    ) {
        initEquipment(slot, before)
        var task: BukkitTask? = null
        val func = Runnable {
            if (entity.equipment.itemInOffHand.type != before) {
                task?.cancel()
                return@Runnable
            }
            val target = entity.target ?: return@Runnable
            if (target.location.distance(entity.location) > distance) return@Runnable
            if (shouldSee && !target.hasLineOfSight(entity)) return@Runnable
            this.function(target)
            initEquipment(slot, after)
        }
        task = Bukkit.getScheduler().runTaskTimer(instance!!, func, 0L, 20L)
    }
}