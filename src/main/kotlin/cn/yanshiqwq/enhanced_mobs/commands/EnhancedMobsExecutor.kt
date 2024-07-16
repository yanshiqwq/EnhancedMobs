package cn.yanshiqwq.enhanced_mobs.commands

import cn.yanshiqwq.enhanced_mobs.EnhancedMob.Companion.asEnhancedMob
import cn.yanshiqwq.enhanced_mobs.Main
import cn.yanshiqwq.enhanced_mobs.managers.PackManager
import cn.yanshiqwq.enhanced_mobs.managers.TypeManager
import cn.yanshiqwq.enhanced_mobs.script.Config.getMainTypeKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.event.entity.CreatureSpawnEvent
import java.util.*


/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.command.EnhancedMobsExecutor
 *
 * @author yanshiqwq
 * @since 2024/6/2 23:52
 */

class EnhancedMobsExecutor : CommandExecutor {
    private val prefix = Component.text(Main.PREFIX + " ", NamedTextColor.GRAY)
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        // 检查命令发送者是否是玩家
        if (sender !is Player) {
            sender.sendMessage(prefix.append(Component.text("只有玩家可以执行此命令！", NamedTextColor.RED)))
            return true
        }

        // 检查命令参数数量
        if (args == null || args.size < 3) {
            sender.sendMessage(prefix.append(Component.text("用法: /enhancedmobs spawn <怪物类型> <主强化类型> <次强化类型> [怪物强度] [坐标]", NamedTextColor.GREEN)))
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

        // 解析主强化类型参数
        val mainBoostTypeArg = args[2].lowercase(Locale.getDefault())
        val mainBoostTypeKey =
            if (mainBoostTypeArg != "default") TypeManager.TypeKey(mainBoostTypeArg)
            else getMainTypeKey(entityType)
        if (mainBoostTypeKey.pack().type != PackManager.PackType.MAIN) {
            sender.sendMessage(prefix.append(Component.text("无效的主强化类型 \"$mainBoostTypeArg\" ！", NamedTextColor.RED)))
            return true
        }

        // 解析次强化类型参数
        val subBoostTypeArg = args[3].lowercase(Locale.getDefault())
        val subBoostTypeKey =
            if (subBoostTypeArg != "none") TypeManager.TypeKey(subBoostTypeArg)
            else null
        if (subBoostTypeKey != null) {
            if (subBoostTypeKey.pack().type != PackManager.PackType.SUB) {
                sender.sendMessage(prefix.append(Component.text("无效的次强化类型 \"$subBoostTypeArg\" ！", NamedTextColor.RED)))
                return true
            }
        }

        // 解析倍数参数
        val multiplier = if (args.size >= 5) {
            val multiplierArg = args[4]
            try {
                multiplierArg.toDouble()
            } catch (e: NumberFormatException) {
                sender.sendMessage(prefix.append(Component.text("无效的怪物强度 \"$multiplierArg\" ！", NamedTextColor.RED)))
                return true
            }
        } else 0.0

        // 解析坐标参数
        val location: Location = if (args.size >= 8) {
            val xArg = args[5]
            val yArg = args[6]
            val zArg = args[7]
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
        entity.asEnhancedMob(multiplier, mainBoostTypeKey, subBoostTypeKey)

        val percent = String.format("${if (multiplier >= 0.0) "+" else ""}%.2f", multiplier * 100)
        sender.sendMessage(prefix
            .append(Component.text("已生成 ${entityType.name} ", NamedTextColor.GREEN))
            .append(Component.text("($mainBoostTypeArg:$subBoostTypeArg ", NamedTextColor.GRAY))
            .append(Component.text(percent, NamedTextColor.AQUA))
            .append(Component.text("%) 于 $location.", NamedTextColor.GRAY))
        )
        return true
    }
}