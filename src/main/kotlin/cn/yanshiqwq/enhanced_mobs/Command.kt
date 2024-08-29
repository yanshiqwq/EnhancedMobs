package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.manager.MobTypeManager
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.*

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.Command
 *
 * @author yanshiqwq
 * @since 2024/8/22 下午 9:04
 */
@CommandHeader("enhancedmobs", ["em"])
object Command {
    @CommandBody(permission = "enhanced_mobs.spawn")
    val spawn = subCommand {
        dynamic("type") {
            suggest {
                MobTypeManager.get().map { it.id }
            }
            int("level",
                suggest = listOf("7", "16", "24", "33", "45", "56", "67", "75", "82", "85", "87", "92", "95")
            ) {
                exec<Player> {
                    val location = sender.location
                    val type = MobTypeManager.get(ctx["type"])
                               ?: throw NullPointerException("TypeId not found: ${ctx["type"]}")
                    val level = ctx.int("level")
                    type.spawn(location, level)
                }
            }
        }
    }
    
    @CommandBody
    val help = subCommand {
        exec<CommandSender> {
            sender.sendMessage("&7Usage: &b/enhancedmobs spawn &a<type> <level>")
        }
    }
}