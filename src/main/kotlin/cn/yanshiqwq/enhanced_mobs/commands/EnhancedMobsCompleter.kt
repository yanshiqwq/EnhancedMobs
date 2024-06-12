package cn.yanshiqwq.enhanced_mobs.commands

import cn.yanshiqwq.enhanced_mobs.Main
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.util.ArrayList

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.commands.EnhancedMobsCompleter
 *
 * @author yanshiqwq
 * @since 2024/6/11 00:18
 */
class EnhancedMobsCompleter : TabCompleter {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String> {
        val completions: MutableList<String> = ArrayList()
        if (args.size == 1) {
            completions.add("spawn")
        } else if (args[0].equals("spawn", ignoreCase = true)) {
            when (args.size) {
                2 -> {
                    for (type in EntityType.entries.filter { it.isSpawnable }) {
                        if (type.name.startsWith(args[1], ignoreCase = true)) {
                            completions.add(type.name)
                        }
                    }
                } // 怪物类型
                3 -> {
                    val input = args[2].uppercase().split(".")
                    val packId = input[0]
                    val mobTypeManager = Main.instance!!.mobTypeManager
                    val packNames = mobTypeManager.listPackIds()
                    val matchingPackages = packNames.filter { it.startsWith(packId) }
                    matchingPackages.addFirst("DEFAULT")
                    return if (input.size == 1 || "DEFAULT".startsWith(input[0])) {
                        matchingPackages
                    } else {
                        mobTypeManager.listTypeIds(packId).map { it.value() }
                    }
                } // 强化类型
                4 -> completions.addAll(arrayOf("-0.5", "0.0", "1.0", "2.0")) // 怪物强度
                in 5..7 -> {
                    if (sender is Player) { // 玩家坐标
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