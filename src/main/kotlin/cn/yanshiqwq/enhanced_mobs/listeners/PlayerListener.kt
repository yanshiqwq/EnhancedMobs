package cn.yanshiqwq.enhanced_mobs.listeners

import cn.yanshiqwq.enhanced_mobs.Utils.addModifierSafe
import cn.yanshiqwq.enhanced_mobs.api.MobApi.effect
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.attribute.Attribute.*
import org.bukkit.attribute.AttributeModifier
import org.bukkit.attribute.AttributeModifier.Operation.*
import org.bukkit.entity.Fireball
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import java.util.*
import kotlin.random.Random

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.listeners.PlayerListener
 *
 * @author yanshiqwq
 * @since 2024/7/16 下午6:40
 */
class PlayerListener: Listener {
    companion object {
        private val uuid: UUID = UUID.fromString("7e993d80-af92-40ed-9097-101b28ae76ca")
        private val healthModifier = AttributeModifier(uuid, "Player spawn bonus", 20.0, ADD_NUMBER)
        private val toughnessModifier = AttributeModifier(uuid, "Player spawn bonus", 8.0, ADD_NUMBER)
        fun removeRespawnModifier(players: List<Player>) {
            players.forEach {
                it.getAttribute(GENERIC_MAX_HEALTH)?.removeModifier(healthModifier)
                it.getAttribute(GENERIC_ARMOR_TOUGHNESS)?.removeModifier(toughnessModifier)
            }
        }
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        event.player.run {
            // 添加重生增益
            getAttribute(GENERIC_MAX_HEALTH)?.addModifierSafe(healthModifier)
            getAttribute(GENERIC_ARMOR_TOUGHNESS)?.addModifierSafe(toughnessModifier)
            effect(PotionEffectType.NIGHT_VISION, 0, 5 * 60 * 20)
        }
    }

    @EventHandler
    fun onArmorDamage(event: PlayerItemDamageEvent) {
        val armorSlots = listOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
        if (event.item.type.equipmentSlot in armorSlots && Random.nextDouble() >= 0.6) {
            event.isCancelled = true
        }
        if (event.item.type == Material.SHIELD && Random.nextDouble() >= 0.5) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onUsingFireCharge(event: PlayerInteractEvent) {
        if (!event.action.isRightClick || event.material != Material.FIRE_CHARGE) return

        val player = event.player
        player.launchProjectile(Fireball::class.java).apply {
            velocity = player.location.direction.multiply(2)
            shooter = player
            yield = 2.5F
        }

        if (player.gameMode == GameMode.CREATIVE) return
        if (event.item != null)
            event.item!!.subtract(1)
        else
            player.inventory.removeItem(ItemStack(Material.FIRE_CHARGE, 1))
    }
}