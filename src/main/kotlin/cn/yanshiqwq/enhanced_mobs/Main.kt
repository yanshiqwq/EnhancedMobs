package cn.yanshiqwq.enhanced_mobs

import org.bukkit.Bukkit.getLogger
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger


class Main : JavaPlugin() {
    val mobTypeManager: MobTypeManager = MobTypeManager()
    var mobManager: MobManager? = null

    override fun onEnable() {
        instance = this
        mobManager = MobManager()
        mobTypeManager.loadPacks(vanillaPack, extendPack)

        server.pluginManager.registerEvents(LevelEntity(), this)
        server.pluginManager.registerEvents(Spawn(), this)
        server.pluginManager.registerEvents(Modifier(), this)
        server.pluginManager.registerEvents(MobEventListener(), this)

        getCommand("enhancedmobs")!!.apply {
            setExecutor(EnhancedMobsCommand())
            tabCompleter = EnhancedMobsTabCompleter()
        }

        logger.info("Plugin enabled")
    }

    override fun onDisable() {
        instance = null
        logger.info("Plugin disabled")
    }

    companion object {
        val logger: Logger = getLogger()
        const val prefix = "[EnhancedMobs]"
        var instance: Main? = null
    }
}