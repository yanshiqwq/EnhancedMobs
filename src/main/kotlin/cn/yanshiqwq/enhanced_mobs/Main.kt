package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.config.ConfigV1
import org.bukkit.event.entity.EntityTargetEvent
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info
import taboolib.common.platform.function.registerBukkitListener
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile

object Main : Plugin() {
    const val PREFIX: String = "EnhancedMobs |"

    // 预加载配置文件
    @Config("config.yml")
    lateinit var configFile: ConfigFile

    override fun onEnable() {
        // 加载内置怪物类型
        if (ConfigV1.loadBuiltinPacks) BuiltInPack.load()

        // 加载 EnhancedMob
        registerBukkitListener(EntityTargetEvent::class.java) {
            EnhancedMob.tryLoad(it.entity)
        }
        info("Successfully running EnhancedMobs!")
    }

    override fun onDisable() {
        info("Successfully disabled EnhancedMobs!")
    }
}