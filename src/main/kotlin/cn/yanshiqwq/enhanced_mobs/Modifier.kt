package cn.yanshiqwq.enhanced_mobs

import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Arrow
import org.bukkit.entity.Fireball
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.event.block.Action
import org.bukkit.inventory.EquipmentSlot
import kotlin.random.Random


/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.Arrow
 *
 * @author yanshiqwq
 * @since 2024/6/8 07:01
 */
class Modifier: Listener {
    @EventHandler
    fun onArrowDamage(event: EntityDamageByEntityEvent) {
        if (event.damager is Arrow) {
            val arrow = event.damager as Arrow
            val damager = arrow.shooter as LivingEntity
            if (damager is Player) return
            val level = damager.equipment?.itemInMainHand?.enchantments?.get(Enchantment.ARROW_DAMAGE) ?: 0
            event.damage = (damager.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.value ?: 0.0) * (1 + level * 0.25)
        }
    }

    @EventHandler
    fun onUsingFireCharge(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_AIR || event.material != Material.FIRE_CHARGE) return
        val player = event.player
        val fireball = player.launchProjectile(Fireball::class.java)
        fireball.velocity = player.location.direction.multiply(1.85)
        fireball.shooter = player
        fireball.yield = 1.85F

        if (player.gameMode == GameMode.CREATIVE) return
        if (player.equipment.itemInMainHand.type == Material.FIRE_CHARGE)
            player.damageItemStack(EquipmentSlot.HAND, 1)
        else
            player.inventory.removeItem(ItemStack(Material.FIRE_CHARGE, 1))
    }

    @EventHandler
    fun onFireballDamage(event: EntityDamageByEntityEvent) {
        if (event.damager is Fireball) {
            val fireball = event.damager as Fireball
            val entity = event.entity
            if (entity is Player) return
            if (fireball.yield >= 1.5 && Random.nextDouble() >= 0.85) {
                entity.fireTicks += 100
            }
        }
    }
}