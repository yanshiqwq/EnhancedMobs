package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.manager.MobTypeManager
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.int
import taboolib.common.platform.command.subCommand

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
            suggestion<CommandSender> { _, _ ->
                MobTypeManager.get().map { it.id }
            }
            int("level") {
                suggestion<CommandSender> { _, _ ->
                    listOf("7", "16", "24", "33", "45", "56", "67", "75", "82", "88", "92", "95")
                }
                execute<Player> { sender, context, _ ->
                    val location = sender.location
                    val type = MobTypeManager.get(context["type"]) ?: throw NullPointerException("TypeId not found: ${context["type"]}")
                    val level = context.int("level")
                    EnhancedMobType.spawn(type, location, level)
                }
            }
        }
    }
    @CommandBody
    val help = subCommand {
        execute<CommandSender> { sender, _, _ ->
            sender.sendMessage("Usage: /enhancedmobs spawn <type> <level>")
        }
    }
}