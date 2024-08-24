package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.manager.MobTypeManager
import org.bukkit.command.CommandSender
import taboolib.common.platform.command.*
import taboolib.platform.util.toBukkitLocation

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.Command
 *
 * @author yanshiqwq
 * @since 2024/8/22 下午9:04
 */
@CommandHeader("enhancedmobs", ["em"])
object Command {
    @CommandBody(permission = "enhanced_mobs.spawn")
    val spawn = subCommand {
        dynamic("type") {
            suggest {
                MobTypeManager.get().map { it.id }
            }
            int("level") {
                suggest {
                    listOf("7", "16", "24", "33", "45", "56", "67", "75", "82", "88", "92", "95")
                }
                execute<CommandSender> { _, context, _ ->
                    val location = context.player().location.toBukkitLocation()
                    val type = MobTypeManager.get(context["type"]) ?: throw NullPointerException("TypeId not found: ${context["type"]}")
                    val level = context.int("level")
                    EnhancedMobType.spawn(type, location, level)
                }
            }
        }
    }
    @CommandBody
    val help = simpleCommand("help") { sender, _ ->
        sender.sendMessage("Usage: /enhancedmobs spawn <type> <level>")
    }
}