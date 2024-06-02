package cn.yanshiqwq.enhanced_mobs

import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    private val prefix = "[enhanced_mobs]"
    override fun onEnable() {
        INSTANCE = this
        LOGGER = logger
        server.pluginManager.registerEvents(EntityTargetListener(), this)
        logger.info("$prefix Plugin enabled")
    }

    override fun onDisable() {
        logger.info("$prefix Plugin disabled")
        INSTANCE = null
    }

    companion object {
        var INSTANCE: Plugin? = null
        var LOGGER: java.util.logging.Logger? = null
    }
}
