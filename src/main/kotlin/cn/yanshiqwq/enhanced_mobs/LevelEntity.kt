package cn.yanshiqwq.enhanced_mobs

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.persistence.PersistentDataType
import kotlin.math.ln
import kotlin.math.max

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.EntityTargetListener
 *
 * @author yanshiqwq
 * @since 2024/5/31 00:02
 */

class LevelEntity: Listener {
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
        if (entity !is LivingEntity || entity.spawnCategory != SpawnCategory.MONSTER) return
        val name = levelEntity(entity)
        entity.customName(name)
        entity.isCustomNameVisible = true
    }

    private var delay = false
    @EventHandler
    fun onPlayerQuery(event: PlayerInteractEntityEvent) {
        delay = !delay
        if (delay) { return } // ??
        val player = event.player
        val entity = event.rightClicked
        val level = levelEntity(entity as LivingEntity)
        val distanceBoost = entity.persistentDataContainer.get(NamespacedKey(Main.INSTANCE!!, "distance_boost"), PersistentDataType.DOUBLE) ?: 0.0

        val distanceComponent = Component.text(" (+${String.format("%.2f", distanceBoost * 100)}%)", NamedTextColor.GRAY)
        val healthComponent = splitter.append(Component.text("❤: ${String.format("%.3f", entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value)}", NamedTextColor.RED))
        val armorComponent = splitter.append(Component.text("🛡: ${String.format("%.3f", entity.getAttribute(Attribute.GENERIC_ARMOR)!!.value)}"))
        val attackComponent = splitter.append(Component.text("🗡: ${String.format("%.3f", getDamage(entity))}", NamedTextColor.YELLOW))
        val speedComponent = splitter.append(Component.text("⚡: ${String.format("%.3f", entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)!!.value)}", NamedTextColor.AQUA))
        player.sendMessage(level.append(distanceComponent).append(healthComponent).append(armorComponent).append(attackComponent).append(speedComponent))
    }

    private fun levelEntity(entity: LivingEntity): TextComponent {
        val level = getCommonLevel(entity)
        val typeKey = entity.type.translationKey()
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

    private fun getCommonLevel(entity: LivingEntity): Int {
        val level = getFactorHealth(entity) * getFactorDamage(entity) * getFactorSpeed(entity) * getFactorAge(entity)
        return level.toInt()
    }

    private fun getFactorHealth(entity: LivingEntity): Double {
        val health = (entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 1.0) / getFactorArmor(entity)
        return if (health <= 20)
            health / 20 + 1
        else
            4 * ln(health + 1) - 10.17
    }

    private fun getFactorArmor(entity: LivingEntity): Double {
        val armor = entity.getAttribute(Attribute.GENERIC_ARMOR)?.value ?: 0.0
        return max(1.0 - 0.04 * armor, 0.2)
    }

    private fun getFactorSpeed(entity: LivingEntity): Double {
        return entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)!!.run {
            (value / baseValue - 1) * 0.35 + 1
        }
    }

    private fun getDamage(entity: LivingEntity): Double {
        return when (entity) {
            is Skeleton -> {
                var damage = entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.value ?: 1.0
                var knockback = 0
                var flame = 1.0
                val mainHand = entity.equipment.itemInMainHand
                if (mainHand.type in arrayOf(Material.BOW, Material.CROSSBOW)) {
                    val power = mainHand.enchantments[Enchantment.ARROW_DAMAGE] ?: 0
                    damage = 4.5 * (1 + 0.15 * power)
                    knockback = mainHand.enchantments[Enchantment.ARROW_KNOCKBACK] ?: 0
                    flame = if ((mainHand.enchantments[Enchantment.ARROW_FIRE]?: 0) >= 1) 1.25 else 1.0
                }
                damage * flame * (1 + 0.15 * knockback) * 0.85
            }
            is Creeper -> {
                val factorFuse = 1 - (entity.maxFuseTicks - 40) / 240
                entity.explosionRadius * factorFuse * 1.15
            }
            else -> entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.value ?: 1.0
        }
    }

    private fun getFactorDamage(entity: LivingEntity): Double {
        val damage= getDamage(entity)
        return if (damage >= 3)
            7 * ln(damage + 1) - 7.7
        else
            damage / 3 + 1
    }

    private fun getFactorAge(entity: LivingEntity): Double {
        return if (entity is Ageable && !entity.isAdult) 1.35 else 1.0
    }
}