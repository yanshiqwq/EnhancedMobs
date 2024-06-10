package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.Main.Companion.logger
import com.destroystokyo.paper.event.entity.EntityPathfindEvent
import io.papermc.paper.event.entity.EntityToggleSitEvent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.*
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask
import java.util.*
import kotlin.reflect.KClass


/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.EnhancedMob
 *
 * @author yanshiqwq
 * @since 2024/6/7 05:29
 */

class MobEventListener : Listener {
    // JB BUKKIT API
//    @EventHandler
//    fun onEntityEvent(event: EntityEvent) { triggerListeners(event) }

    @EventHandler
    fun onEvent(event: EntityCombustEvent) {
        triggerListeners(event)
    }

    @EventHandler
    fun onEvent(event: EntityDamageEvent) {
        triggerListeners(event)
    }

    @EventHandler
    fun onEvent(event: EntityDeathEvent) {
        triggerListeners(event)
    }

    @EventHandler
    fun onEvent(event: EntityEnterBlockEvent) {
        triggerListeners(event)
    }

    @EventHandler
    fun onEvent(event: EntityExplodeEvent) {
        triggerListeners(event)
    }

    @EventHandler
    fun onEvent(event: EntityInteractEvent) {
        triggerListeners(event)
    }

    @EventHandler
    fun onEvent(event: EntityPickupItemEvent) {
        triggerListeners(event)
    }

    @EventHandler
    fun onEvent(event: EntityPotionEffectEvent) {
        triggerListeners(event)
    }

    @EventHandler
    fun onEvent(event: EntityRegainHealthEvent) {
        triggerListeners(event)
    }

    @EventHandler
    fun onEvent(event: EntityResurrectEvent) {
        triggerListeners(event)
    }

    @EventHandler
    fun onEvent(event: EntityShootBowEvent) {
        triggerListeners(event)
    }

    @EventHandler
    fun onEvent(event: EntitySpawnEvent) {
        triggerListeners(event)
    }

    @EventHandler
    fun onEvent(event: EntityTargetEvent) {
        triggerListeners(event)
    }

    @EventHandler
    fun onEvent(event: EntityTeleportEvent) {
        triggerListeners(event)
    }

    @EventHandler
    fun onEvent(event: EntityToggleGlideEvent) {
        triggerListeners(event)
    }

    @EventHandler
    fun onEvent(event: EntityToggleSwimEvent) {
        triggerListeners(event)
    }

    @EventHandler
    fun onEvent(event: EntityToggleSitEvent) {
        triggerListeners(event)
    }

    @EventHandler
    fun onEvent(event: ExplosionPrimeEvent) {
        triggerListeners(event)
    }

    @EventHandler
    fun onEvent(event: PiglinBarterEvent) {
        triggerListeners(event)
    }

    @EventHandler
    fun onEvent(event: PigZombieAngerEvent) {
        triggerListeners(event)
    }

    @EventHandler
    fun onEvent(event: ProjectileLaunchEvent) {
        triggerListeners(event)
    }

    @EventHandler
    fun onEvent(event: ProjectileHitEvent) {
        triggerListeners(event)
    }

    @EventHandler
    fun onEvent(event: SlimeSplitEvent) {
        triggerListeners(event)
    }

    @EventHandler
    fun onEvent(event: EntityAirChangeEvent) {
        triggerListeners(event)
    }

    @EventHandler
    fun onEvent(event: EntityPathfindEvent) {
        triggerListeners(event)
    }

    private fun triggerListeners(event: Event) {
        try {
            for (mob in instance?.mobManager?.map()?.values ?: emptyList()) {
                for (it in mob.listeners) {
                    if (it.key != event::class) continue
                    it.value(event)
                }
            }
        } catch (e: Exception) {
            logger.warning("An unexpected exception occurred while triggering skill.")
            e.printStackTrace()
        }
    }
}

class EnhancedMob(val multiplier: Double, val entity: Mob) {
    companion object {
        val multiplierKey = NamespacedKey(instance!!, "multiplier")
    }

    init {
        entity.persistentDataContainer.set(multiplierKey, PersistentDataType.DOUBLE, multiplier)
        instance!!.mobManager!!.register(entity.uniqueId, this)
    }

    val listeners = mutableMapOf<KClass<out Event>, (Event) -> Unit>()

    fun initAttribute(record: AttributeRecord) {
        val attributeUUID: UUID = UUID.fromString("a8d0bc44-1534-43f0-a594-f74c7c91bc59")
        val attributeName = "EnhancedMob Spawn Boost"
        record.apply(this.entity, this.multiplier, attributeUUID, attributeName)
    }

    fun initEquipment(slot: EquipmentSlot, material: Material) {
        this.entity.equipment.setItem(slot, ItemStack(material))
    }

    fun initEnchant(slot: EquipmentSlot, record: EnchantRecord) {
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

fun Location.placeBlock(type: Material) {
    this.block.run {
        if (!isReplaceable) return
        setType(type, true)
    }
}

inline fun <reified T : Entity> Location.spawnEntity(type: EntityType, function: T.() -> Unit) {
    val entity = this.world.spawnEntity(this, type, CreatureSpawnEvent.SpawnReason.REINFORCEMENTS)
    if (type.entityClass == T::class.java)
        (entity as T).function()
    else
        throw IllegalArgumentException("The generic type variable does not match the provided type: $type")
}
