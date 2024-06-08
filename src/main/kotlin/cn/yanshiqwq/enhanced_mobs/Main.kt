package cn.yanshiqwq.enhanced_mobs

import org.bukkit.Bukkit.getLogger
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldSaveEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger

class Main : JavaPlugin(), Listener {
    val mobTypeManager: MobTypeManager = MobTypeManager()
    val mobManager: MobManager = MobManager(mobDataPath)

    override fun onEnable() {
        instance = this

        server.pluginManager.registerEvents(LevelEntity(), this)
        server.pluginManager.registerEvents(Spawn(), this)
        server.pluginManager.registerEvents(ArrowModifier(), this)
        getCommand("enhancedmobs")!!.setExecutor(Command())
        getCommand("enhancedmobs")!!.tabCompleter = CommandTabCompleter()
        mobTypeManager.loadPacks(vanillaPack, extendPack)

        logger.info("Plugin enabled")
    }

    override fun onDisable() {
        instance = null
        logger.info("Plugin disabled")
    }

    @EventHandler
    fun onAutoSave(event: WorldSaveEvent) {
        mobManager.save(mobDataPath)
    }

    companion object {
        private const val mobDataPath = "mobs.dat"
        val logger: Logger = getLogger()
        var instance: Main? = null
    }
}
