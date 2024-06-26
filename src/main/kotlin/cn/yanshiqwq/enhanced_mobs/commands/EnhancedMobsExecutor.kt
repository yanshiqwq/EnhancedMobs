package cn.yanshiqwq.enhanced_mobs.commands

import cn.yanshiqwq.enhanced_mobs.EnhancedMob.Companion.asEnhancedMob
import cn.yanshiqwq.enhanced_mobs.Main
import cn.yanshiqwq.enhanced_mobs.data.Tags
import cn.yanshiqwq.enhanced_mobs.managers.TypeManager
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
    private val prefix = Component.text(Main.prefix + " ", NamedTextColor.GRAY)
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        // 检查命令发送者是否是玩家
        if (sender !is Player) {
            sender.sendMessage(prefix.append(Component.text("只有玩家可以执行此命令！", NamedTextColor.RED)))
            return true
        }

        // 检查命令参数数量
        if (args == null || args.size < 3) {
            sender.sendMessage(prefix.append(Component.text("用法: /enhancedmobs spawn <怪物类型> <强化类型> [怪物强度] [坐标]", NamedTextColor.GREEN)))
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
        val boostTypeArg = args[2].lowercase(Locale.getDefault())
        val boostTypeKey =
            if (boostTypeArg != "default") TypeManager.TypeKey(boostTypeArg)
            else getDefaultBoostId(entityType)
        if (!Main.instance!!.typeManager.hasTypeId(boostTypeKey)) {
            sender.sendMessage(prefix.append(Component.text("无效的强化类型 \"$boostTypeArg\" ！", NamedTextColor.RED)))
            return true
        }

        // 解析倍数参数
        val multiplier = if (args.size >= 4) {
            val multiplierArg = args[3]
            try {
                multiplierArg.toDouble()
            } catch (e: NumberFormatException) {
                sender.sendMessage(prefix.append(Component.text("无效的怪物强度 \"$multiplierArg\" ！", NamedTextColor.RED)))
                return true
            }
        } else 0.0


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
        entity.asEnhancedMob(multiplier, boostTypeKey)

        val percent = String.format("${if (multiplier >= 0.0) "+" else ""}%.2f", multiplier * 100)
        sender.sendMessage(prefix
            .append(Component.text("已生成 ${entityType.name} ", NamedTextColor.GREEN))
            .append(Component.text("($boostTypeArg ", NamedTextColor.GRAY))
            .append(Component.text(percent, NamedTextColor.AQUA))
            .append(Component.text("%) 于 $location.", NamedTextColor.GRAY))
        )
        return true
    }
    private fun getDefaultBoostId(entityType: EntityType): TypeManager.TypeKey {
        return when (entityType) {
            in Tags.Entity.zombies -> TypeManager.TypeKey("vanilla", "zombie")
            in Tags.Entity.skeletons -> TypeManager.TypeKey("vanilla", "skeleton")
            in Tags.Entity.spiders -> TypeManager.TypeKey("vanilla", "spider")
            in Tags.Entity.creepers -> TypeManager.TypeKey("vanilla", "creeper")
            else -> TypeManager.TypeKey("vanilla", "generic")
        }
    }
}