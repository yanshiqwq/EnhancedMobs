package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.event.PlayerInteractEnhancedMobEvent
import org.bukkit.attribute.Attribute
import taboolib.common.platform.function.registerBukkitListener
import taboolib.module.chat.ComponentText
import taboolib.module.chat.StandardColors

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.PlayerListener
 *
 * @author yanshiqwq
 * @since 2024/8/24 下午8:26
 */
object PlayerListener {
    init {
        registerBukkitListener(PlayerInteractEnhancedMobEvent::class.java) {
            val player = it.player
            val mob = it.mob.entity
            if (it.isCancelled || !it.player.isSneaking) return@registerBukkitListener
            
            val splitter = ComponentText.of(" | ").color(StandardColors.WHITE).newLine()
            
            val healthComponent = splitter.append(
                ComponentText.of(
                    "❤: ${
                        "%.3f".format(mob.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value)
                    }"
                ).color(StandardColors.RED)
            )
            val armorComponent = splitter.append(
                ComponentText.of(
                    "🛡: ${
                        "%.3f".format(mob.getAttribute(Attribute.GENERIC_ARMOR)!!.value)
                    }"
                ).color(StandardColors.GRAY)
            )
            val attackComponent = splitter.append(
                ComponentText.of(
                    "🗡: ${
                        "%.3f".format(mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.value)
                    }",
                ).color(StandardColors.YELLOW)
            )
            val speedComponent = splitter.append(
                ComponentText.of(
                    "⚡: ${
                        "%.3f".format(mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)!!.value)
                    }"
                ).color(StandardColors.AQUA)
            )
            val component = ComponentText.of(mob.customName!!)
                .append(healthComponent)
                .append(armorComponent)
                .append(attackComponent)
                .append(speedComponent)
            player.sendMessage(component.toLegacyText())
        }
    }
}