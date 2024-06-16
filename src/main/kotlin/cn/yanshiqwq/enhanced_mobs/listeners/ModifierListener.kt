package cn.yanshiqwq.enhanced_mobs.listeners

import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType


/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.listeners.ModifierListener
 *
 * @author yanshiqwq
 * @since 2024/6/8 07:01
 */
class ModifierListener : Listener {
    @EventHandler
    fun onArrowDamage(event: EntityDamageByEntityEvent) {
        if (event.damager !is Arrow) return

        val arrow = event.damager as Arrow
        val damager = arrow.shooter as LivingEntity
        if (damager is Player) return
        val level = damager.equipment?.itemInMainHand?.enchantments?.get(Enchantment.ARROW_DAMAGE) ?: 0
        event.damage = (damager.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.value ?: 0.0) * (1 + level * 0.25)
    }

    @EventHandler
    fun onUsingFireCharge(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_AIR || event.material != Material.FIRE_CHARGE) return

        val player = event.player
        player.launchProjectile(Fireball::class.java).apply {
            velocity = player.location.direction.multiply(2)
            shooter = player
            yield = 2.0F
        }

        if (player.gameMode == GameMode.CREATIVE) return
        if (event.item != null)
            event.item!!.subtract(1)
        else
            player.inventory.removeItem(ItemStack(Material.FIRE_CHARGE, 1))
    }

    @EventHandler
    fun onEntitySpawn(event: EntitySpawnEvent) {
        val entity = event.entity
        if (entity !is Mob) return

        entity.applyVariantBoost()
        entity.health = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: return
    }

    private fun Mob.applyVariantBoost() {
        when (this) {
            is Stray, is WitherSkeleton, is Husk, is Drowned -> {
                getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 28.0
                getAttribute(Attribute.GENERIC_ARMOR)?.baseValue = 4.0
                getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.baseValue = 4.0
                getAttribute(Attribute.GENERIC_FOLLOW_RANGE)?.baseValue = 24.0
            }

            is Creeper -> {
                if (!isPowered) return
                getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 24.0
                getAttribute(Attribute.GENERIC_ARMOR)?.baseValue = 6.0
                getAttribute(Attribute.GENERIC_FOLLOW_RANGE)?.baseValue = 24.0
                getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE)?.baseValue = 0.35
            }

            is Giant -> {
                getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 196.0
                getAttribute(Attribute.GENERIC_ARMOR)?.baseValue = 6.0
                getAttribute(Attribute.GENERIC_FOLLOW_RANGE)?.baseValue = 128.0
                getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.baseValue = 0.35
                getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.baseValue = 9.0
                getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE)?.baseValue = 0.85
                getAttribute(Attribute.ZOMBIE_SPAWN_REINFORCEMENTS)?.baseValue = 0.35
                addPotionEffect(PotionEffect(PotionEffectType.JUMP, Int.MAX_VALUE, 3, true, false))
            }

            is Piglin -> {
                getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 24.0
                getAttribute(Attribute.GENERIC_ARMOR)?.baseValue = 2.0
                isImmuneToZombification = true
            }

            is PiglinBrute -> {
                getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 72.0
                getAttribute(Attribute.GENERIC_ARMOR)?.baseValue = 4.0
                getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE)?.baseValue = 0.15
                isImmuneToZombification = true
            }
        }
    }
}