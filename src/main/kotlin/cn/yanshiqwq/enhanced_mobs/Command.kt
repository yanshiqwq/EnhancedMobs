package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.manager.LevelCurveManager
import cn.yanshiqwq.enhanced_mobs.manager.MobTypeManager
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.*
import taboolib.common.platform.command.component.ExecuteContext

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
                MobTypeManager.entries.keys.toList()
            }
            int("level") {
                dynamic("levelCurve") {
                    suggest {
                        LevelCurveManager.entries.keys.toList()
                    }
                    exec<Player> {
                        val level = ctx.int("level")
                        val levelCurve = ctx["levelCurve"]
                        spawnMob(level, levelCurve)
                    }
                }
                exec<Player> {
                    val level = ctx.int("level")
                    spawnMob(level, "default")
                }
            }
            exec<Player> {
                spawnMob(64, "default")
            }
        }
    }
    private fun ExecuteContext<Player>.spawnMob(level: Int, levelCurve: String) {
        val location = sender.location
        val typeId = ctx["type"]
        val type = MobTypeManager.getValue(typeId)
        type.spawn(location, level, levelCurve)
    }
    
    @CommandBody
    val help = subCommand {
        exec<CommandSender> {
            sender.sendMessage("&7Usage: &b/enhancedmobs spawn &a<type> [level]")
        }
    }
}