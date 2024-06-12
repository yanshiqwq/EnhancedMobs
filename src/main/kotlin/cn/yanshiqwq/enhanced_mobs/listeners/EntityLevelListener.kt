package cn.yanshiqwq.enhanced_mobs.listeners

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.persistence.PersistentDataType
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.listeners.EntityLevelListener
 *
 * @author yanshiqwq
 * @since 2024/5/31 00:02
 */

class EntityLevelListener : Listener {
    // debug
//    @EventHandler
//    fun onPlayerDamage(event: EntityDamageByEntityEvent) {
//        if (event.entity !is Player) return
//        val player = event.entity as Player
//        val entity = when (event.damager) {
//            is Projectile -> (event.damager as Projectile).shooter!! as LivingEntity
//            is Attributable -> event.damager as LivingEntity
//            else -> return
//        }
//        val component = (entity.customName() ?: Component.text(entity.name))
//            .append(Component.text(" 对 ", NamedTextColor.GRAY))
//            .append(Component.text(player.name, NamedTextColor.YELLOW))
//            .append(Component.text(" 造成了 ", NamedTextColor.GRAY))
//            .append(Component.text(String.format("%.3f",event.finalDamage), NamedTextColor.RED))
//            .append(Component.text(" 点伤害"))
//            .append(Component.newline())
//        player.sendMessage(component)
//    }

    private val splitter = Component.text(" | ", NamedTextColor.GRAY)

    @EventHandler
    fun onEntityTarget(event: EntityTargetEvent) {
        val entity = event.entity
        if (entity !is LivingEntity) return
        entity.customName(entity.getLeveledNameComponent())
        entity.isCustomNameVisible = true
    }

    private var delay = false

    @EventHandler
    fun onPlayerQuery(event: PlayerInteractEntityEvent) {
        delay = !delay
        if (delay) {
            return
        } // ??
        val player = event.player
        val entity = event.rightClicked
        if (entity !is LivingEntity) return

        val level = entity.getLeveledNameComponent()
        val multiplier = entity.persistentDataContainer.get(EnhancedMob.multiplierKey, PersistentDataType.DOUBLE) ?: 0.0

        val multiplierComponent = Component.text(
            " (${
                if (multiplier >= 0) "+" else ""
            }${
                "%.2f".format(multiplier * 100)
            }%)",
            NamedTextColor.GRAY
        )
        val healthComponent = splitter.append(
            Component.text(
                "❤: ${
                    "%.3f".format(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value)
                }", NamedTextColor.RED
            )
        )
        val armorComponent = splitter.append(
            Component.text(
                "🛡: ${
                    "%.3f".format(entity.getAttribute(Attribute.GENERIC_ARMOR)!!.value)
                }"
            )
        )
        val attackComponent =
            splitter.append(Component.text("🗡: ${String.format("%.3f", entity.getDamage())}", NamedTextColor.YELLOW))
        val speedComponent = splitter.append(
            Component.text(
                "⚡: ${
                    "%.3f".format(entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)!!.value)
                }", NamedTextColor.AQUA
            )
        )
        player.sendMessage(
            level.append(multiplierComponent).append(healthComponent).append(armorComponent).append(attackComponent)
                .append(speedComponent)
        )
    }

    private fun LivingEntity.getLeveledNameComponent(): TextComponent {
        val level = getCommonLevel()
        val typeKey = type.translationKey()
        val levelColor = when (level) {
            in 10..19 -> NamedTextColor.GREEN
            in 20..29 -> NamedTextColor.YELLOW
            in 30..39 -> NamedTextColor.RED
            in 40..59 -> NamedTextColor.DARK_PURPLE
            in 60..Int.MAX_VALUE -> NamedTextColor.DARK_RED
            else -> NamedTextColor.GRAY
        }
        val nameColor = if (level >= 10) NamedTextColor.WHITE else NamedTextColor.GRAY

        return Component.text("[", NamedTextColor.GRAY)
            .append(Component.text("Lv.$level", levelColor))
            .append(Component.text("] ", NamedTextColor.GRAY))
            .append(Component.translatable(typeKey, nameColor))
    }

    private fun LivingEntity.getCommonLevel(): Int {
        val level = getFactorHealth() * getFactorDamage() * getFactorSpeed() * getFactorAge()
        return floor(level).toInt()
    }

    private fun LivingEntity.getFactorHealth(): Double {
        val health = (getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 0.0) / getFactorArmor()
        return if (health <= 20)
            health / 20 + 1
        else
            2 * ln(health + 1) - 4.09
    }

    private fun LivingEntity.getFactorArmor(): Double {
        val armor = getAttribute(Attribute.GENERIC_ARMOR)?.value ?: 0.0
        return max(1.0 - 0.04 * armor, 0.2)
    }

    private fun LivingEntity.getFactorSpeed(): Double {
        return getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)!!.run {
            (value / baseValue - 1) * 0.35 + 1
        }
    }

    private fun LivingEntity.getDamage(): Double {
        return when (this) {
            is WitherSkeleton, is AbstractSkeleton -> {
                var damage = getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.value ?: 1.0
                var knockback = 0
                var flame = 1.0
                val mainHand = equipment?.itemInMainHand
                    ?: return getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.value ?: 0.0
                if (mainHand.type in arrayOf(Material.BOW, Material.CROSSBOW)) {
                    val power = mainHand.enchantments[Enchantment.ARROW_DAMAGE] ?: 0
                    damage = 4.5 * (1 + 0.15 * power)
                    knockback = mainHand.enchantments[Enchantment.ARROW_KNOCKBACK] ?: 0
                    flame = if ((mainHand.enchantments[Enchantment.ARROW_FIRE] ?: 0) >= 1) 1.25 else 1.0
                }
                damage * flame * (1 + 0.15 * knockback) * 0.85
            }

            is Creeper -> {
                val factorFuse = 1 - (maxFuseTicks - 40) / 240
                explosionRadius * factorFuse * 1.15
            }

            else -> getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.value ?: 1.0
        }
    }

    private fun LivingEntity.getFactorDamage(): Double {
        val damage = getDamage()
        return if (damage >= 3)
            7 * ln(damage + 1) - 7.7
        else
            damage / 3 + 1
    }

    private fun LivingEntity.getFactorAge(): Double {
        return if (this is Ageable && !isAdult) 1.35 else 1.0
    }
}