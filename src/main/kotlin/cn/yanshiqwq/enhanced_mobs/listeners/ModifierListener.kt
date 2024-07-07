package cn.yanshiqwq.enhanced_mobs.listeners

import cn.yanshiqwq.enhanced_mobs.EnhancedMob.Companion.isEnhancedMob
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.attribute.Attribute.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack


/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.listeners.ModifierListener
 *
 * @author yanshiqwq
 * @since 2024/6/8 07:01
 */
class ModifierListener : Listener {
    @EventHandler
    fun onEnhancedMobArrowDamage(event: EntityDamageByEntityEvent) {
        if (event.damager !is Arrow) return

        val arrow = event.damager as Arrow
        val damager = arrow.shooter as LivingEntity
        if (damager is Player || !damager.isEnhancedMob()) return
        val level = damager.equipment?.itemInMainHand?.enchantments?.get(Enchantment.ARROW_DAMAGE) ?: 0
        event.damage = 1.5 * (damager.getAttribute(GENERIC_ATTACK_DAMAGE)?.value ?: 0.0) * (1 + level * 0.25)
    }

    @EventHandler
    fun onUsingFireCharge(event: PlayerInteractEvent) {
        if (!event.action.isRightClick || event.material != Material.FIRE_CHARGE) return

        val player = event.player
        player.launchProjectile(Fireball::class.java).apply {
            velocity = player.location.direction.multiply(2)
            shooter = player
            yield = 2.4F
        }

        if (player.gameMode == GameMode.CREATIVE) return
        if (event.item != null)
            event.item!!.subtract(1)
        else
            player.inventory.removeItem(ItemStack(Material.FIRE_CHARGE, 1))
    }
}