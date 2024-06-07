package cn.yanshiqwq.enhanced_mobs

import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    override fun onEnable() {
        INSTANCE = this
        LOGGER = logger
        server.pluginManager.registerEvents(LevelEntity(), this)
        server.pluginManager.registerEvents(Spawn(), this)
        server.pluginManager.registerEvents(Arrow(), this)
        getCommand("enhancedmobs")!!.setExecutor(Command())
        getCommand("enhancedmobs")!!.tabCompleter = CommandTabCompleter()
        logger.info("Plugin enabled")
    }

    override fun onDisable() {
        logger.info("Plugin disabled")
        INSTANCE = null
    }

    companion object {
        var INSTANCE: Plugin? = null
        var LOGGER: java.util.logging.Logger? = null
    }
}
