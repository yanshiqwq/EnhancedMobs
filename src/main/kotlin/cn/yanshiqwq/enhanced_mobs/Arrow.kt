package cn.yanshiqwq.enhanced_mobs

import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.Arrow
 *
 * @author yanshiqwq
 * @since 2024/6/8 07:01
 */
class Arrow: Listener {
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
}