package cn.yanshiqwq.enhanced_mobs.config

import taboolib.module.configuration.ConfigFile

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.config.Config
 *
 * @author yanshiqwq
 * @since 2024/8/23 下午 5:21
 */
abstract class AbstractConfig(config: ConfigFile) {
    val configVersion: Int = config.getInt("configVersion", 1)
}