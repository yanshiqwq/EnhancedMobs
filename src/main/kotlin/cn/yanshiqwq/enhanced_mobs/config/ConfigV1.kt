package cn.yanshiqwq.enhanced_mobs.config

import cn.yanshiqwq.enhanced_mobs.Main
import taboolib.common.platform.function.info
import taboolib.module.chat.colored
import taboolib.module.configuration.ConfigFile

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.config.ConfigV1
 *
 * @author yanshiqwq
 * @since 2024/8/22 下午 9:08
 */
object ConfigV1 {
    private val config: ConfigFile = Main.configFile
    val loadBuiltinPacks: Boolean = config.getBoolean("load_built_in_packs", true)
    val levelFormula: String = config.getString(
        "level_formula",
        "{vertical_coeff} * sqrt(abs({y} - 64)) + {horizontal_coeff} * ln(sqrt(pow({x}, 2) + pow({z}, 2)) + 1)"
    )!!
    val coeff: Pair<Int, Int> = Pair(
        config.getInt("coeff.vertical", 6),
        config.getInt("coeff.horizontal", 2)
    )
    val levelRange: IntRange = runCatching {
        val start = config.getInt("level_range.min", 1)
        val end = config.getInt("level_range.max", 95)
        return@runCatching start .. end
    }.getOrDefault(1 .. 95)
    val namePrefix: String = config.getString(
        "name_prefix",
        "&7[%sLv.%d&7] "
    )!!
    
    init {
        info("Config initialized!")
        info("  - load_built_in_packs = $loadBuiltinPacks")
        info("  - level_range = \"$levelRange\"")
        info("  - name_prefix = \"${namePrefix.colored()}\"")
    }
}