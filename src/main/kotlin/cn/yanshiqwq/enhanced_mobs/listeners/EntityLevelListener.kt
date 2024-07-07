package cn.yanshiqwq.enhanced_mobs.listeners

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.Utils.getTeam
import cn.yanshiqwq.enhanced_mobs.managers.TypeManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.meta.FireworkMeta
import org.bukkit.persistence.PersistentDataType
import kotlin.math.floor
import kotlin.math.ln

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
    private val levelKey = NamespacedKey(instance!!, "level")

    @EventHandler(priority = EventPriority.LOWEST)
    fun onEntityTarget(event: EntityTargetEvent) {
        val entity = event.entity as? Mob ?: return
        if (entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.value == 0.0) return
        val level = entity.getCommonLevel()
        entity.persistentDataContainer.set(levelKey, PersistentDataType.INTEGER, level)
        entity.customName(entity.getLeveledNameComponent(level))
        entity.isCustomNameVisible = true
    }

    private var delay = false

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerQuery(event: PlayerInteractEntityEvent) {
        delay = !delay
        if (delay) return // ???
        val player = event.player
        val entity = event.rightClicked
        if (entity !is LivingEntity) return

        val level = entity.persistentDataContainer.get(levelKey, PersistentDataType.INTEGER) ?: return
        val levelComponent = entity.getLeveledNameComponent(level)
        val mainBoostType = entity.persistentDataContainer.get(EnhancedMob.mainKey, PersistentDataType.STRING) ?: TypeManager.TypeKey.mainDefault.value()
        val subBoostType = entity.persistentDataContainer.get(EnhancedMob.subKey, PersistentDataType.STRING) ?: TypeManager.TypeKey.subDefault.value()
        val multiplier = entity.persistentDataContainer.get(EnhancedMob.multiplierKey, PersistentDataType.DOUBLE) ?: 0.0

        val multiplierComponent = Component.text(
            " ($mainBoostType:$subBoostType) ${
                if (multiplier >= 0) "+" else ""
            }${
                "%.2f".format(multiplier * 100)
            }%",
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
        val component = levelComponent
            .append(multiplierComponent)
            .append(healthComponent)
            .append(armorComponent)
            .append(attackComponent)
            .append(speedComponent)
        player.sendMessage(component)
    }

    private fun LivingEntity.getLeveledNameComponent(level: Int): TextComponent {
        // 设定等级颜色
        val levelColor = when (level) {
            in 10..50 -> NamedTextColor.GREEN
            in 50..70 -> NamedTextColor.YELLOW
            in 70..80 -> NamedTextColor.RED
            in 80..90 -> NamedTextColor.DARK_PURPLE
            in 90..Int.MAX_VALUE -> NamedTextColor.DARK_RED
            else -> NamedTextColor.GRAY
        }
        val nameColor = if (level >= 10) NamedTextColor.WHITE else NamedTextColor.GRAY

        // 设定发光颜色
        val teamName = when (level) {
            in 85..90 -> InitListener.MobTeam.STRENGTH.id
            in 90..95 -> InitListener.MobTeam.ENHANCED.id
            in 95..Int.MAX_VALUE -> InitListener.MobTeam.BOSS.id
            else -> null
        }
        if (teamName != null) {
            getTeam(teamName)?.addEntity(this)
            isGlowing = true
        }

        // 返回等级信息
        val typeKey = type.translationKey()
        return Component.text("[", NamedTextColor.GRAY)
            .append(Component.text("Lv.$level", levelColor))
            .append(Component.text("] ", NamedTextColor.GRAY))
            .append(Component.translatable(typeKey, nameColor))
    }

    private fun LivingEntity.getCommonLevel(): Int {
        val level = getFactorHealth() * getFactorDamage() * getFactorSpeed()
        return floor(level).toInt()
    }

    private fun LivingEntity.getFactorHealth(): Double {
        val health = (getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 0.0) / getFactorArmor()
        return if (health > 20)
            2 * ln(health + 1) - 4
        else
            health / 20 + 1
    }

    private fun LivingEntity.getFactorArmor(): Double {
        val armor = getAttribute(Attribute.GENERIC_ARMOR)?.value ?: 0.0
        return 1.0 - 0.02 * armor
    }

    private fun LivingEntity.getFactorSpeed(): Double {
        val baseSpeed = getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)!!.baseValue
        val speed = getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)!!.value
        return 1 + speed / (baseSpeed + 0.65)
    }

    private fun LivingEntity.getDamage(): Double {
        val baseDamage = getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.value ?: 0.0
        val mainHand = equipment?.itemInMainHand
            ?: return getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.value ?: 0.0

        val damage = when (mainHand.type) {
            Material.BOW -> {
                val power = mainHand.enchantments[Enchantment.ARROW_DAMAGE] ?: 0
                val knockback = mainHand.enchantments[Enchantment.ARROW_KNOCKBACK] ?: 0
                val flame = mainHand.enchantments[Enchantment.ARROW_FIRE] ?: 0

                1 + 1.18 * baseDamage * (1 + 0.25 * power) * (1 + 0.15 * knockback) + if (flame >= 1) 5.0 else 0.0
            }
            Material.CROSSBOW -> {
                val quickCharge = mainHand.enchantments[Enchantment.QUICK_CHARGE] ?: 0
                val multishot = mainHand.enchantments[Enchantment.MULTISHOT] ?: 0
                val piercing = mainHand.enchantments[Enchantment.PIERCING] ?: 0

                val offHand = equipment?.itemInOffHand
                val firework = if (offHand != null && offHand.type == Material.FIREWORK_ROCKET) {
                    val meta = offHand.itemMeta as FireworkMeta
                    val count = meta.effects.count()
                    if (count == 0) 0.0 else 5.5 + (count - 1) * 1.5 - baseDamage
                } else 0.0
                1 + baseDamage * (1 + if (multishot >= 1) 0.35 else 0.0) * (0.6 + 0.4 * quickCharge) * (1.5 + piercing * 0.05) + firework
            }
            else -> baseDamage
        }
        return when (this) {
            is Skeleton -> damage + 1.0
            is Creeper -> {
                val factorFuse = 1 - (maxFuseTicks - 40) / 240
                explosionRadius * factorFuse * 1.2
            }
            is CaveSpider -> damage + 3.0
            is Spider -> damage + 1.0
            is Witch -> damage + 6.0
            is Pillager -> damage * 0.6

            else -> damage
        }
    }

    private fun LivingEntity.getFactorDamage(): Double {
        val damage = getDamage()
        return if (damage > 3)
            7 * ln(damage) - 3.7
        else
            damage + 1
    }
}