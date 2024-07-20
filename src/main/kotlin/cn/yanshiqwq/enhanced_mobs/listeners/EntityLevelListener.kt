package cn.yanshiqwq.enhanced_mobs.listeners

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.EnhancedMob.Companion.isEnhancedMob
import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.Utils.getTeam
import cn.yanshiqwq.enhanced_mobs.managers.TypeManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.persistence.PersistentDataType

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

    companion object {
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
                in 85..90 -> MobInitListener.MobTeam.STRENGTH.id
                in 90..95 -> MobInitListener.MobTeam.ENHANCED.id
                in 95..Int.MAX_VALUE -> MobInitListener.MobTeam.BOSS.id
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

        fun EnhancedMob.getCommonLevel() = when (multiplier) {
            in -1.0..0.0 -> 10 * multiplier + 10
            in 0.0..2.0 -> 25 * multiplier + 10
            in 2.0..EnhancedMob.MULTIPLIER_MAX_VALUE -> 10 * multiplier + 40
            else -> 0.0
        }.toInt().coerceIn(0..100)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onEntityTarget(event: EntityTargetEvent) {
        val mob = (if (event.entity.isEnhancedMob()) instance!!.mobManager.get(event.entity as Mob) else return) ?: return
        if (mob.entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.value == 0.0) return
        val level = mob.getCommonLevel()
        mob.entity.persistentDataContainer.set(levelKey, PersistentDataType.INTEGER, level)
        mob.entity.customName(mob.entity.getLeveledNameComponent(level))
        mob.entity.isCustomNameVisible = true
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
        val attackComponent = splitter.append(
            Component.text(
                "🗡: ${
                    "%.3f".format(entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.value)
                }"
            )
        )
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
}