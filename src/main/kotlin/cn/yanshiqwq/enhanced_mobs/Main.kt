package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.config.ConfigV1
import cn.yanshiqwq.enhanced_mobs.manager.LevelCurveManager
import cn.yanshiqwq.enhanced_mobs.manager.PackManager
import cn.yanshiqwq.enhanced_mobs.ui.BossBarManager
import org.bukkit.event.entity.EntityTargetEvent
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info
import taboolib.common.platform.function.registerBukkitListener
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile

object Main: Plugin() {
    // 预加载配置文件
    @Config("config.yml")
    lateinit var configFile: ConfigFile
    
    override fun onEnable() {
        if (ConfigV1.loadBuiltinPacks) {
            // 加载内置怪物类型
            PackManager.register(Initialize.pack)
            // 加载默认等级曲线
            LevelCurveManager.register(Initialize.levelCurve)
        }

        // 在怪物锁定目标时尝试加载为 EnhancedMob
        registerBukkitListener(EntityTargetEvent::class.java) {
            EnhancedMob.load(it.entity)
        }

        BossBarManager.load()
        
        info("Successfully running EnhancedMobs!")
    }
    
    override fun onDisable() {
        info("Successfully disabled EnhancedMobs!")
    }
}