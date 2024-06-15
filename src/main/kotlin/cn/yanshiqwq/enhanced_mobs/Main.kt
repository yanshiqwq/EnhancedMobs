package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.commands.EnhancedMobsExecutor
import cn.yanshiqwq.enhanced_mobs.commands.EnhancedMobsCompleter
import cn.yanshiqwq.enhanced_mobs.data.BuiltInPacks
import cn.yanshiqwq.enhanced_mobs.listeners.EntityLevelListener
import cn.yanshiqwq.enhanced_mobs.listeners.MobEventListener
import cn.yanshiqwq.enhanced_mobs.listeners.ModifierListener
import cn.yanshiqwq.enhanced_mobs.listeners.SpawnListener
import cn.yanshiqwq.enhanced_mobs.managers.MobManager
import cn.yanshiqwq.enhanced_mobs.managers.MobTypeManager
import org.bukkit.Bukkit.getLogger
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.Main
 *
 * @author yanshiqwq
 * @since 2024/5/30 23:12
 */
class Main : JavaPlugin() {
    companion object {
        const val prefix = "[EnhancedMobs]"
        var instance: Main? = null
        val logger: Logger = getLogger()
    }

    val mobTypeManager: MobTypeManager = MobTypeManager()
    var mobManager: MobManager? = null

    override fun onEnable() {
        instance = this
        mobManager = MobManager()
        mobTypeManager.loadPacks(BuiltInPacks.vanillaPack, BuiltInPacks.extendPack)

        server.pluginManager.run {
            registerEvents(EntityLevelListener(), instance!!)
            registerEvents(ModifierListener(), instance!!)
            registerEvents(MobEventListener(), instance!!)
            registerEvents(SpawnListener(), instance!!)
        }

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
}
