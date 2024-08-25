package cn.yanshiqwq.enhanced_mobs.event

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.manager.MobManager
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEntityEvent
import taboolib.common.platform.function.registerBukkitListener
import taboolib.common.platform.function.server

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.event.PlayerInteractEnhancedMobEvent
 *
 * @author yanshiqwq
 * @since 2024/8/24 下午8:31
 */
class PlayerInteractEnhancedMobEvent(
    player: Player,
    val mob: EnhancedMob
): PlayerInteractEntityEvent(player, mob.entity) {
    companion object {
        init {
            registerBukkitListener(PlayerInteractEntityEvent::class.java) {
                val mob = MobManager.get(it.rightClicked.uniqueId)
                if (mob != null) server<Server>().pluginManager.callEvent(PlayerInteractEnhancedMobEvent(it.player, mob))
            }
        }
    }
}