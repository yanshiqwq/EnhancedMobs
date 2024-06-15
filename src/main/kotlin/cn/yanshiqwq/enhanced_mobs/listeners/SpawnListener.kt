package cn.yanshiqwq.enhanced_mobs.listeners

import cn.yanshiqwq.enhanced_mobs.EnhancedMob.Companion.asEnhancedMob
import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.managers.MobTypeManager.Companion.getRandomTypeId
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Mob
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntitySpawnEvent
import java.lang.Exception
import kotlin.random.Random

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.listeners.SpawnListener
 *
 * @author yanshiqwq
 * @since 2024/6/15 18:09
 */
class SpawnListener: Listener {
    init {
        instance!!.server.scoreboardManager.mainScoreboard.runCatching {
            (getTeam("strength") ?: registerNewTeam("strength")).color(NamedTextColor.AQUA)
            (getTeam("enhanced") ?: registerNewTeam("enhanced")).color(NamedTextColor.LIGHT_PURPLE)
            (getTeam("boss") ?: registerNewTeam("boss")).color(NamedTextColor.RED)
        }
    }
    @EventHandler
    fun onMobSpawn(event: EntitySpawnEvent){
        if (event.entity !is Mob || event.isCancelled) return
        val entity = event.entity as Mob
        val multiplier = when(Random.nextInt(100)) {
            in 0..90 -> 0.0
            in 90..95 -> 1.0
            in 95 .. 98 -> 2.0
            else -> 3.0
        }
        if (event.entity.entitySpawnReason != CreatureSpawnEvent.SpawnReason.CUSTOM) {
            val mob = try {
                entity.asEnhancedMob(multiplier, getRandomTypeId(entity.type)) ?: return
            } catch (ignored: Exception) { return }
            instance!!.mobManager?.register(entity.uniqueId, mob)
        }
        val teamName = when (multiplier) {
            in 0.5 .. 1.0 -> "strength"
            in 1.0 .. 3.0 -> "enhanced"
            in 3.0..114514.0 -> "boss"
            else -> return
        }
        instance!!.server.scoreboardManager.mainScoreboard.getTeam(teamName)?.addEntity(entity)
        entity.isGlowing = true
    }
}