package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.EnhancedMob.type
import org.bukkit.event.entity.EntityDamageByEntityEvent
import taboolib.common.platform.Plugin
import taboolib.common.platform.command.command
import taboolib.common.platform.function.info

object Main : Plugin() {
    override fun onEnable() {
        command(
            name = "enhancedmobs",
            aliases = listOf("em")
        ) {

        }
        type("zombie_bloody_energized") {
            name("嗜血僵尸-势能")
            base {
                health = 20.0
                damage = 2.0
                speed = 0.23
            }
            func {
                listen<EntityDamageByEntityEvent> {

                }
            }
        }
        info("Successfully running enhanced_mobs!")
    }
}