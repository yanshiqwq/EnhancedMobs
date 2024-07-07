package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.dsl.WeightDslBuilder.WeightList
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeInstance
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.*
import org.bukkit.scoreboard.Team
import kotlin.random.Random

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.Utils
 *
 * @author yanshiqwq
 * @since 2024/6/11 00:16
 */
@Suppress("unused")
object Utils {
    fun <T: Any> weightList(block: WeightList<T>.() -> Unit): WeightList<T> {
        val weightList = WeightList<T>()
        weightList.block()
        return weightList
    }

    fun <T: Any> T.byChance(chance: Double) = if (Random.nextDouble() <= chance) this else null
    fun chance(chance: Double) = if (Random.nextDouble() <= chance) true else false

    fun getTeam(teamName: String): Team? = instance!!.server.scoreboardManager.mainScoreboard.runCatching {
        getTeam(teamName) ?: registerNewTeam(teamName)
    }.getOrNull()

    fun AttributeInstance.addModifierSafe(modifier: AttributeModifier) = try {
        addModifier(modifier)
    } catch (_: IllegalArgumentException) {}

    fun Boolean.Companion.all(vararg booleans: Boolean): Boolean = booleans.all { it }
    fun Boolean.Companion.any(vararg booleans: Boolean): Boolean = booleans.any { it }

    fun LivingEntity.percentHeal(percent: Double = 1.0) {
        health = getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value * percent
    }

    fun LivingEntity.addModifier(attribute: Attribute, modifier: AttributeModifier) =
        getAttribute(attribute)?.addModifier(modifier)
    fun LivingEntity.removeModifier(attribute: Attribute, modifier: AttributeModifier?) =
        modifier?.let { getAttribute(attribute)?.removeModifier(it) }

    fun Material.isAxe(): Boolean = this.name.endsWith("_AXE")
    fun Material.isSword(): Boolean = this.name.endsWith("_SWORD")
    fun Material.isShovel(): Boolean = this.name.endsWith("_SHOVEL")
    fun Material.isPickaxe(): Boolean = this.name.endsWith("_PICKAXE")
    fun Material.isHoe(): Boolean = this.name.endsWith("_HOE")

    fun Location.getNearestPlayer(): Player? {
        // 获取世界中的所有玩家
        val players = Bukkit.getOnlinePlayers()
            .filter { it.world == world }

        // 初始化最近的玩家和最小距离
        var nearestPlayer: Player? = null
        var minDistance = Double.MAX_VALUE

        // 遍历所有玩家，找到距离怪物最近的玩家
        for (player in players) {
            val distance = player.location.distance(this)
            if (distance < minDistance) {
                minDistance = distance
                nearestPlayer = player
            }
        }
        return nearestPlayer
    }
}