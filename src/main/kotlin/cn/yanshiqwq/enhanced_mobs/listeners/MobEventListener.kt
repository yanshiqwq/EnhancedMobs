package cn.yanshiqwq.enhanced_mobs.listeners

import cn.yanshiqwq.enhanced_mobs.EnhancedMob.Companion.isEnhancedMob
import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.api.TaskApi.cancelTask
import com.destroystokyo.paper.event.entity.EntityPathfindEvent
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import io.papermc.paper.event.entity.EntityToggleSitEvent
import org.bukkit.attribute.Attribute.GENERIC_ATTACK_DAMAGE
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.*
import org.checkerframework.checker.units.qual.t
import java.util.*

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.listeners.MobEventListener
 *
 * @author yanshiqwq
 * @since 2024/6/7 05:29
 */

class MobEventListener : Listener {
    @EventHandler
    fun onMobArrowDamage(event: EntityDamageByEntityEvent) {
        val arrow = event.damager as? Arrow ?: return
        val damager = arrow.shooter as? LivingEntity ?: return
        if (damager is Player || !damager.isEnhancedMob()) return
        val level = damager.equipment?.itemInMainHand?.enchantments?.get(Enchantment.ARROW_DAMAGE) ?: 0
        event.damage = 1.5 * (damager.getAttribute(GENERIC_ATTACK_DAMAGE)?.value ?: 0.0) * (1 + level * 0.25)
    }

    @EventHandler
    fun onEnhancedMobRemove(event: EntityRemoveFromWorldEvent){
        val mob = instance!!.mobManager.get(event.entity.uniqueId)
        if (mob == null) {
            removeEnhancedMob(event.entity.uniqueId)
            return
        }
        mob.tasks.forEach { mob.cancelTask(it.key) }
    }

    private fun removeEnhancedMob(uuid: UUID) = instance!!.mobManager.remove(uuid)

    // JB BUKKIT API
//    @EventHandler
//    fun onEntityEvent(event: EntityEvent) { triggerListeners(event) }

    private fun triggerListeners(event: Event) {
        try {
            for (mob in instance!!.mobManager.map().values) {
                val start = System.nanoTime()
                for (it in mob.listeners) {
                    if (it.eventClass != event::class) continue
                    it.function.invoke(event)
                }
                val time = (System.nanoTime() - start) / 1000000.0
                if (time >= 1) instance!!.logger.warning("Mob ${mob.entity.name} (${mob.entity.uniqueId}) took ${time}ms !")
            }
        } catch (_: ConcurrentModificationException) {
        } catch (_: IllegalArgumentException) {
        } catch (e: Exception) {
            instance!!.logger.warning("An unexpected exception occurred while triggering skill")
            e.printStackTrace()
        }
    }

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