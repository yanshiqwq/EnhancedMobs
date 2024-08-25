package cn.yanshiqwq.enhanced_mobs.config

import cn.yanshiqwq.enhanced_mobs.Main
import com.github.keelar.exprk.Expressions
import org.bukkit.attribute.Attributable
import org.bukkit.attribute.Attribute
import taboolib.common.platform.function.info
import taboolib.module.configuration.ConfigFile

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.config.ConfigV1
 *
 * @author yanshiqwq
 * @since 2024/8/22 下午9:08
 */

object ConfigV1: AbstractConfig(Main.configFile) {
    private val config: ConfigFile = Main.configFile
    var loadBuiltinPacks: Boolean = config.getBoolean("loadBuiltinPacks", true)
    var levelRange: IntRange = runCatching {
        val rangeStr = config.getString("levelRange")!!
        val start = rangeStr.substringBefore("..").toInt()
        val end = rangeStr.substringAfter("..").toInt()
        return@runCatching start..end
    }.getOrDefault(1..99)
    var customName: String = config.getString("customName", "&7[%sLv.%d&7] %s")!!
    val levelFormula: LevelFormula by lazy {
        LevelFormula(
            config.getString("levelFormula.maxHealth") ?: "base * 1.025 ^ (level - 20)",
            config.getString("levelFormula.armor") ?: "base + level * 0.08",
            config.getString("levelFormula.attackDamage") ?: "base * 1.025 ^ (level - 30)",
            config.getString("levelFormula.movementSpeed") ?: """
                if (level <= 65) then
                    base
                else if (level <= 77) then
                    1.1 * base
                else if (level <= 85) then
                    1.2 * base
                else
                    1.32 * base
            """.trimIndent(),
            config.getString("levelFormula.attackKnockback") ?: """
                if (level <= 65) then
                    base
                else if (level <= 77) then
                    base + 0.35
                else if (level <= 85) then
                    base + 0.85
                else
                    base + 1.5
            """.trimIndent(),
            config.getString("levelFormula.knockbackResistance") ?: "base + level * 0.35"
        )
    }
    
    data class LevelFormula(
        val maxHealth: String,
        val armor: String,
        val attackDamage: String,
        val movementSpeed: String,
        val attackKnockback: String,
        val knockbackResistance: String
    ) {
        fun apply(entity: Attributable, level: Int) = mapOf(
            Attribute.GENERIC_MAX_HEALTH to maxHealth,
            Attribute.GENERIC_ATTACK_DAMAGE to attackDamage,
            Attribute.GENERIC_MOVEMENT_SPEED to movementSpeed,
            Attribute.GENERIC_KNOCKBACK_RESISTANCE to knockbackResistance
        ).forEach { (attribute, expression) ->
            entity.getAttribute(attribute)?.run {
                baseValue = Expressions()
                    .define("base", baseValue)
                    .define("level", level.toBigDecimal())
                    .eval(expression)
                    .toDouble()
            }
        }
    }

    init {
        info("Config initialized!")
        info("  - configVersion = $configVersion")
        info("  - loadBuiltInPacks = $loadBuiltinPacks")
        info("  - levelRange = \"$levelRange\"")
        info("  - customName = \"$customName\"")
    }
}

