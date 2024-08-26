package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.config.ConfigV1
import cn.yanshiqwq.enhanced_mobs.event.PlayerInteractEnhancedMobEvent
import cn.yanshiqwq.enhanced_mobs.manager.MobManager
import org.bukkit.attribute.Attribute
import org.bukkit.event.entity.EntityTargetEvent
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info
import taboolib.common.platform.function.registerBukkitListener
import taboolib.module.chat.ComponentText
import taboolib.module.chat.StandardColors
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile

object Main : Plugin() {
    // 预加载配置文件
    @Config("config.yml")
    lateinit var configFile: ConfigFile

    override fun onEnable() {
        // 加载内置怪物类型
        if (ConfigV1.loadBuiltinPacks) BuiltInPack.load()

        // 加载 EnhancedMob
        registerBukkitListener(EntityTargetEvent::class.java) {
            if (MobManager.has(it.entity.uniqueId)) return@registerBukkitListener
            EnhancedMob.tryLoad(it.entity)
        }
        
        // 注册玩家查询实体属性事件
        // 潜行时右键实体即可触发
        registerBukkitListener(PlayerInteractEnhancedMobEvent::class.java) {
            val player = it.player
            val mob = it.mob.entity
            if (it.isCancelled || !it.player.isSneaking) return@registerBukkitListener
            
            val splitter = ComponentText.of(" | ").color(StandardColors.WHITE).newLine()
            
            val healthComponent = splitter.append(
                ComponentText.of(
                    "❤: ${
                        "%.3f".format(mob.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value)
                    }"
                ).color(StandardColors.RED)
            )
            val armorComponent = splitter.append(
                ComponentText.of(
                    "🛡: ${
                        "%.3f".format(mob.getAttribute(Attribute.GENERIC_ARMOR)!!.value)
                    }"
                ).color(StandardColors.GRAY)
            )
            val attackComponent = splitter.append(
                ComponentText.of(
                    "🗡: ${
                        "%.3f".format(mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.value)
                    }",
                ).color(StandardColors.YELLOW)
            )
            val speedComponent = splitter.append(
                ComponentText.of(
                    "⚡: ${
                        "%.3f".format(mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)!!.value)
                    }"
                ).color(StandardColors.AQUA)
            )
            val component = ComponentText.of(mob.customName!!)
                .append(healthComponent)
                .append(armorComponent)
                .append(attackComponent)
                .append(speedComponent)
            player.sendMessage(component.toLegacyText())
        }
        info("Successfully running EnhancedMobs!")
    }

    override fun onDisable() {
        info("Successfully disabled EnhancedMobs!")
    }
}