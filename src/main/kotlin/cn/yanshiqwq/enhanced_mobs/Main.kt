package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.commands.EnhancedMobsCompleter
import cn.yanshiqwq.enhanced_mobs.commands.EnhancedMobsExecutor
import cn.yanshiqwq.enhanced_mobs.listeners.*
import cn.yanshiqwq.enhanced_mobs.managers.MobManager
import cn.yanshiqwq.enhanced_mobs.managers.PackManager
import cn.yanshiqwq.enhanced_mobs.managers.TypeManager
import org.bukkit.plugin.java.JavaPlugin


/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.Main
 *
 * @author yanshiqwq
 * @since 2024/5/30 23:12
 */
class Main : JavaPlugin() {
    companion object {
        const val PREFIX = "[EnhancedMobs]"
        var instance: Main? = null
    }

    val packManager = PackManager()
    val typeManager = TypeManager()
    val mobManager = MobManager()
    override fun onEnable() {
        instance = this

        saveDefaultConfig() // 确保配置文件存在，如果不存在则创建默认配置文件
        packManager.loadPacks()

        server.pluginManager.run {
            registerEvents(EntityLevelListener(), instance!!)
            registerEvents(PlayerListener(), instance!!)
            registerEvents(MobEventListener(), instance!!)
            registerEvents(MobInitListener(), instance!!)
        }

        getCommand("enhancedmobs")!!.run {
            setExecutor(EnhancedMobsExecutor())
            tabCompleter = EnhancedMobsCompleter()
        }

        PlayerListener.addPlayerModifier(instance!!.server.onlinePlayers.toList())

        logger.info("Plugin enabled")
    }

    override fun onDisable() {
        PlayerListener.removePlayerModifier(instance!!.server.onlinePlayers.toList())
        instance = null
        logger.info("Plugin disabled")
    }
}
