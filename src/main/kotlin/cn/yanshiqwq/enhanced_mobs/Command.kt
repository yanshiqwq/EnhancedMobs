package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.TypeBoost.Companion.customBoost
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.CreatureSpawnEvent
import java.util.*


/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.Command
 *
 * @author yanshiqwq
 * @since 2024/6/2 23:52
 */


class Command : CommandExecutor {
    private val prefix = Component.text("[EnhancedMobs] ", NamedTextColor.GRAY)
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        // 检查命令发送者是否是玩家
        if (sender !is Player) {
            sender.sendMessage(prefix.append(Component.text("只有玩家可以执行此命令！", NamedTextColor.RED)))
            return true
        }

        // 检查命令参数数量
        if (args != null) {
            if (args.size < 3) {
                sender.sendMessage(prefix.append(Component.text("用法: /enhancedmobs spawn [怪物类型] [怪物强度]", NamedTextColor.GREEN)))
                return true
            }
        }

        // 解析怪物类型参数
        val typeArg = args?.get(1) ?: return false
        val entityType: EntityType = try {
            EntityType.valueOf(typeArg.uppercase(Locale.getDefault()))
        } catch (e: IllegalArgumentException) {
            sender.sendMessage(prefix.append(Component.text("无效的怪物类型！", NamedTextColor.RED)))
            return true
        }

        // 解析倍数参数
        val multiplier = try {
            args[2].toDouble()
        } catch (e: NumberFormatException) {
            sender.sendMessage(prefix.append(Component.text("无效的怪物强度！")))
            return true
        }

        // 生成实体
        val entity: Entity = sender.world.spawnEntity(sender.location, entityType, CreatureSpawnEvent.SpawnReason.CUSTOM)
        customBoost(entity as LivingEntity, multiplier)
        val percent = String.format("${if (multiplier >= 1) "+" else ""}%.2f", multiplier * 100)
        sender.sendMessage(prefix
            .append(Component.text("已生成 ${entityType.name}$ ("))
            .append(Component.text(percent, NamedTextColor.AQUA))
            .append(Component.text("%).", NamedTextColor.GRAY)))
        return true
    }
}


class CommandTabCompleter : TabCompleter {
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        val completions: MutableList<String> = ArrayList()
        if (args.size == 1) {
            completions.add("spawn")
        } else if (args.size == 2 && args[0].equals("spawn", ignoreCase = true)) {
            for (entityType in EntityType.entries) {
                completions.add(entityType.name)
            }
        } else if (args.size == 3 && args[0].equals("spawn", ignoreCase = true)) {
            completions.run {
                add("-1.0")
                add("0.0")
                add("1.0")
            }
        }
        return completions
    }
}

