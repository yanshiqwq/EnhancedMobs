package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeInstance
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.*
import org.bukkit.scoreboard.Team

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.Utils
 *
 * @author yanshiqwq
 * @since 2024/6/11 00:16
 */
@Suppress("unused")
object Utils {
    fun getTeam(teamName: String): Team? = instance!!.server.scoreboardManager.mainScoreboard.runCatching {
            getTeam(teamName) ?: registerNewTeam(teamName)
        }.getOrNull()

    fun AttributeInstance.addModifierSafe(modifier: AttributeModifier){
        if (modifiers.contains(modifier)) return
        addModifier(modifier)
    }

    fun Boolean.Companion.all(vararg booleans: Boolean): Boolean = booleans.all { it }
    fun Boolean.Companion.any(vararg booleans: Boolean): Boolean = booleans.any { it }

    fun LivingEntity.percentHeal(percent: Double = 1.0) {
        if (isDead) return
        health = getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value * percent
    }

    fun Entity.isOnFire() = this.fireTicks > 0

    fun Material.isAxe(): Boolean = this.name.endsWith("_AXE")
    fun Material.isSword(): Boolean = this.name.endsWith("_SWORD")
    fun Material.isShovel(): Boolean = this.name.endsWith("_SHOVEL")
    fun Material.isPickaxe(): Boolean = this.name.endsWith("_PICKAXE")
    fun Material.isHoe(): Boolean = this.name.endsWith("_HOE")

    fun Location.getNearestPlayer(): Player? {
        // 获取世界中的所有玩家
        val players = Bukkit.getOnlinePlayers()

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