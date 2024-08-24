package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.event.PlayerInteractEnhancedMobEvent
import taboolib.common.platform.function.registerBukkitListener

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.PlayerListener
 *
 * @author yanshiqwq
 * @since 2024/8/24 下午8:26
 */
object PlayerListener {
    init {
        // TODO
        registerBukkitListener(PlayerInteractEnhancedMobEvent::class.java) {
            val player = it.player
            val mob = it.mob.mob
            if (it.isCancelled || !it.player.isSneaking) return@registerBukkitListener
            
            val splitter = " | "
            
//            val levelComponent = Component.text(
//                " ($boostType) ${
//                    if (multiplier >= 0) "+" else ""
//                }${
//                    "%.2f".format(multiplier * 100)
//                }%",
//                NamedTextColor.GRAY
//            )
//            val healthComponent = splitter.append(
//                Component.text(
//                    "❤: ${
//                        "%.3f".format(mob.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value)
//                    }", NamedTextColor.RED
//                )
//            )
//            val armorComponent = splitter.append(
//                Component.text(
//                    "🛡: ${
//                        "%.3f".format(mob.getAttribute(Attribute.GENERIC_ARMOR)!!.value)
//                    }"
//                )
//            )
//            val attackComponent =
//                splitter.append(Component.text("🗡: ${String.format("%.3f", mob.getDamage())}", NamedTextColor.YELLOW))
//            val speedComponent = splitter.append(
//                Component.text(
//                    "⚡: ${
//                        "%.3f".format(mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)!!.value)
//                    }", NamedTextColor.AQUA
//                )
//            )
//            val component = mob.mob.customName!!.colored()
//                .plus(healthComponent)
//                .plus(armorComponent)
//                .plus(attackComponent)
//                .plus(speedComponent)
//            player.sendMessage(component)
        }
    }
}