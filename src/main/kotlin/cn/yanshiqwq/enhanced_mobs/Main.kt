package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.commands.EnhancedMobsCompleter
import cn.yanshiqwq.enhanced_mobs.commands.EnhancedMobsExecutor
import cn.yanshiqwq.enhanced_mobs.listeners.EntityLevelListener
import cn.yanshiqwq.enhanced_mobs.listeners.MobEventListener
import cn.yanshiqwq.enhanced_mobs.listeners.ModifierListener
import cn.yanshiqwq.enhanced_mobs.listeners.SpawnListener
import cn.yanshiqwq.enhanced_mobs.managers.MobManager
import cn.yanshiqwq.enhanced_mobs.managers.TypeManager
import cn.yanshiqwq.enhanced_mobs.managers.PackManager
import cn.yanshiqwq.enhanced_mobs.managers.WeightManager
import org.bukkit.plugin.PluginLogger
import org.bukkit.plugin.java.JavaPlugin
import kotlin.io.path.Path


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
    }

    val logger = PluginLogger(this)
    val packManager = PackManager()
    val typeManager = TypeManager()
    val mobManager = MobManager()
    val weightManager = WeightManager(config)
    override fun onEnable() {
        instance = this

        saveDefaultConfig() // 确保配置文件存在，如果不存在则创建默认配置文件
        packManager.loadPacks(Path(dataFolder.path, "packs"))

        server.pluginManager.run {
            registerEvents(EntityLevelListener(), instance!!)
            registerEvents(ModifierListener(), instance!!)
            registerEvents(MobEventListener(), instance!!)
            registerEvents(SpawnListener(), instance!!)
        }

        getCommand("enhancedmobs")!!.run {
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
