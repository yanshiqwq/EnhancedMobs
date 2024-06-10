package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mob
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

class EnhancedMobsCommand : CommandExecutor {
    private val prefix = Component.text(Main.prefix + " ", NamedTextColor.GRAY)
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        // 检查命令发送者是否是玩家
        if (sender !is Player) {
            sender.sendMessage(prefix.append(Component.text("只有玩家可以执行此命令！", NamedTextColor.RED)))
            return true
        }

        // 检查命令参数数量
        if (args == null || args.size < 4) {
            sender.sendMessage(prefix.append(Component.text("用法: /enhancedmobs spawn <怪物类型> <强化类型> <怪物强度> [坐标]", NamedTextColor.GREEN)))
            return true
        }

        // 解析怪物类型参数
        val entityTypeArg = args[1].uppercase(Locale.getDefault())
        val entityType: EntityType = try {
            EntityType.valueOf(entityTypeArg)
        } catch (e: IllegalArgumentException) {
            sender.sendMessage(prefix.append(Component.text("无效的怪物类型 \"$entityTypeArg\" ！", NamedTextColor.RED)))
            return true
        }

        // 解析强化类型参数
        val boostTypeArg = args[2].uppercase(Locale.getDefault())
        val boostTypeFunc = try {
            instance!!.mobTypeManager.queryTypeFunc(
                if (boostTypeArg != "DEFAULT") TypeId(boostTypeArg)
                else defaultBoost(entityType)
            ) ?: throw Exception()
        } catch (e: Exception) {
            sender.sendMessage(prefix.append(Component.text("无效的强化类型 \"$boostTypeArg\" ！", NamedTextColor.RED)))
            return true
        }

        // 解析倍数参数
        val multiplierArg = args[3]
        val multiplier = try {
            multiplierArg.toDouble()
        } catch (e: NumberFormatException) {
            sender.sendMessage(prefix.append(Component.text("无效的怪物强度 \"$multiplierArg\" ！", NamedTextColor.RED)))
            return true
        }

        // 解析坐标参数
        val location: Location = if (args.size >= 7) {
            val xArg = args[4]
            val yArg = args[5]
            val zArg = args[6]
            try {
                val x = xArg.toDouble()
                val y = yArg.toDouble()
                val z = zArg.toDouble()
                Location(sender.world, x, y, z)
            } catch (e: NumberFormatException) {
                sender.sendMessage(prefix.append(Component.text("坐标 ($xArg, $yArg, $zArg) 格式错误！", NamedTextColor.RED)))
                return true
            }
        } else sender.location.toCenterLocation()

        // 生成实体
        val entity: Mob = sender.world.spawnEntity(location, entityType, CreatureSpawnEvent.SpawnReason.CUSTOM) as Mob
        boostTypeFunc(EnhancedMob(multiplier, entity))
        entity.health = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value


        val percent = String.format("${if (multiplier >= 0.0) "+" else ""}%.2f", multiplier * 100)
        sender.sendMessage(prefix
            .append(Component.text("已生成 ${entityType.name} ", NamedTextColor.GREEN))
            .append(Component.text("(${boostTypeArg} ", NamedTextColor.GRAY))
            .append(Component.text(percent, NamedTextColor.AQUA))
            .append(Component.text("%) 于 (${location.x}, ${location.y}, ${location.z}).", NamedTextColor.GRAY)))
        return true
    }
    private fun defaultBoost(entityType: EntityType): TypeId {
        return when (entityType) {
            in arrayOf(EntityType.ZOMBIE_VILLAGER, EntityType.ZOMBIE, EntityType.HUSK, EntityType.DROWNED) -> TypeId("VANILLA","ZOMBIE")
            in arrayOf(EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON) -> TypeId("VANILLA","SKELETON")
            in arrayOf(EntityType.SPIDER, EntityType.CAVE_SPIDER) -> TypeId("VANILLA","SPIDER")
            EntityType.CREEPER -> TypeId("VANILLA","CREEPER")
            else -> TypeId("VANILLA","GENERIC")
        }
    }
}


class EnhancedMobsTabCompleter : TabCompleter {
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        val completions: MutableList<String> = ArrayList()
        if (args.size == 1) {
            completions.add("spawn")
        } else if (args[0].equals("spawn", ignoreCase = true)) {
            when (args.size) {
                2 -> {
                    for (type in EntityType.entries.filter { it.isSpawnable }){
                        if (type.name.startsWith(args[1], ignoreCase = true)){
                            completions.add(type.name)
                        }
                    }
                } // 怪物类型
                3 -> {
                    val input = args[2].uppercase().split(".")
                    val packId = input[0]
                    val mobTypeManager = instance!!.mobTypeManager
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