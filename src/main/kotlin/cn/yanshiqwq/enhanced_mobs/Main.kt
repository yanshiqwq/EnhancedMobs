package cn.yanshiqwq.enhanced_mobs

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
        info("Successfully running enhanced_mobs!")
    }
}