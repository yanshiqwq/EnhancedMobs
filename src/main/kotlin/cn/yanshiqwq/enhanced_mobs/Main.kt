package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.commands.EnhancedMobsExecutor
import cn.yanshiqwq.enhanced_mobs.commands.EnhancedMobsCompleter
import cn.yanshiqwq.enhanced_mobs.config.Packs
import cn.yanshiqwq.enhanced_mobs.listeners.EntityLevelListener
import cn.yanshiqwq.enhanced_mobs.listeners.MobEventListener
import cn.yanshiqwq.enhanced_mobs.listeners.ModifierListener
import cn.yanshiqwq.enhanced_mobs.listeners.SpawnListener
import cn.yanshiqwq.enhanced_mobs.managers.MobManager
import cn.yanshiqwq.enhanced_mobs.managers.MobTypeManager
import org.bukkit.Bukkit.getLogger
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger


class Main : JavaPlugin() {
    val mobTypeManager: MobTypeManager = MobTypeManager()
    var mobManager: MobManager? = null

    override fun onEnable() {
        instance = this
        mobManager = MobManager()
        mobTypeManager.loadPacks(Packs.vanillaPack, Packs.extendPack)

        server.pluginManager.registerEvents(EntityLevelListener(), this)
        server.pluginManager.registerEvents(SpawnListener(), this)
        server.pluginManager.registerEvents(ModifierListener(), this)
        server.pluginManager.registerEvents(MobEventListener(), this)

        getCommand("enhancedmobs")!!.apply {
            setExecutor(EnhancedMobsExecutor())
            tabCompleter = EnhancedMobsCompleter()
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
