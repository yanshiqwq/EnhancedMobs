package cn.yanshiqwq.enhanced_mobs.listeners

import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.api.TaskApi.cancelTask
import com.destroystokyo.paper.event.entity.EntityPathfindEvent
import io.papermc.paper.event.entity.EntityToggleSitEvent
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.*
import java.util.*
import kotlin.ConcurrentModificationException

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.listeners.MobEventListener
 *
 * @author yanshiqwq
 * @since 2024/6/7 05:29
 */

class MobEventListener : Listener {
    private fun triggerListeners(event: Event) {
        try {
            for (mob in instance!!.mobManager.map().values) {
                for (it in mob.listeners) {
                    if (it.eventClass != event::class) continue
                    it.function.invoke(event)
                }
            }
        } catch (_: ConcurrentModificationException) {
        } catch (e: Exception) {
            instance!!.logger.warning("An unexpected exception occurred while triggering skill.")
            e.printStackTrace()
        }
    }

    @EventHandler
    fun onEnhancedMobDeath(event: EntityDeathEvent){
        val mob = instance!!.mobManager.get(event.entity.uniqueId)
        if (mob == null) {
            removeEnhancedMob(event.entity.uniqueId)
            return
        }
        mob.tasks.forEach { mob.cancelTask(it.key) }
    }

    fun removeEnhancedMob(uuid: UUID) = instance!!.mobManager.remove(uuid)

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
}