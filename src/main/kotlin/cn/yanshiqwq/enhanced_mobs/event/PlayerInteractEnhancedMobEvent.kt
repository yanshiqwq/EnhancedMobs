package cn.yanshiqwq.enhanced_mobs.event

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.manager.MobManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEntityEvent
import taboolib.common.platform.function.registerBukkitListener

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.event.PlayerInteractEnhancedMobEvent
 *
 * @author yanshiqwq
 * @since 2024/8/24 下午 8:31
 */
class PlayerInteractEnhancedMobEvent(
    player: Player,
    val mob: EnhancedMob
): PlayerInteractEntityEvent(player, mob.entity) {
    companion object {
        init {
            registerBukkitListener(PlayerInteractEntityEvent::class.java) {
                val mob = MobManager.get(it.rightClicked.uniqueId) ?: return@registerBukkitListener
                val event = PlayerInteractEnhancedMobEvent(it.player, mob)
                Bukkit.getServer().pluginManager.callEvent(event)
            }
        }
    }
}