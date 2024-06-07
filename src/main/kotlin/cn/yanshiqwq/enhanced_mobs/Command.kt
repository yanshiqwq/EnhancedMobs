package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.Boost.Companion.randomList
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
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
                sender.sendMessage(prefix.append(Component.text("用法: /enhancedmobs spawn <怪物类型> <强化类型> <怪物强度> [坐标]", NamedTextColor.GREEN)))
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

        val boostType = try {
            EnhancedMobs.map[typeArg.uppercase(Locale.getDefault())]!!
        } catch (e: IllegalArgumentException) {
            sender.sendMessage(prefix.append(Component.text("无效的强化类型！", NamedTextColor.RED)))
            return true
        }

        // 解析倍数参数
        val multiplier = try {
            args[3].toDouble()
        } catch (e: NumberFormatException) {
            sender.sendMessage(prefix.append(Component.text("无效的怪物强度！", NamedTextColor.RED)))
            return true
        }

        // 解析坐标参数
        val location: Location = if (args.size >= 7) {
            try {
                val x = args[4].toDouble()
                val y = args[5].toDouble()
                val z = args[6].toDouble()
                Location(sender.world, x, y, z)
            } catch (e: NumberFormatException) {
                sender.sendMessage(prefix.append(Component.text("坐标格式不正确！", NamedTextColor.RED)))
                return true
            }
        } else sender.location.toCenterLocation()

        // 生成实体
        val entity: LivingEntity = sender.world.spawnEntity(location, entityType, CreatureSpawnEvent.SpawnReason.CUSTOM) as LivingEntity
        boostType(multiplier, entity)
        entity.health = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value

        val percent = String.format("${if (multiplier >= 0.0) "+" else ""}%.2f", multiplier * 100)
        sender.sendMessage(prefix
            .append(Component.text("已生成 ${entityType.name}$ ", NamedTextColor.GREEN))
            .append(Component.text("(", NamedTextColor.GRAY))
            .append(Component.text(percent, NamedTextColor.AQUA))
            .append(Component.text("%) 于 (${location.x}, ${location.y}, ${location.z}).", NamedTextColor.GRAY)))
        return true
    }
}


class CommandTabCompleter : TabCompleter {
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        val completions: MutableList<String> = ArrayList()
        if (args.size == 1) {
            completions.add("spawn")
        } else if (args[0].equals("spawn", ignoreCase = true)) {
            when (args.size) {
                2 -> for (type in randomList) completions.add(type.name) // 自动补全怪物类型
                3 -> for (type in EnhancedMobs.map) completions.add(type.key)
                4 -> completions.addAll(arrayOf("-0.5", "0.0", "0.5", "1.0", "2.0")) // 自动补全怪物强度
                in 5..7 -> {
                    if (sender is Player) { // 自动补全玩家坐标
                        val location = sender.location.toCenterLocation()
                        when (args.size) {
                            5 -> completions.add(location.x.toString())
                            6 -> completions.add(location.y.toString())
                            7 -> completions.add(location.z.toString())
                        }
                    }
                }
            }
        }
        return completions
    }
}


