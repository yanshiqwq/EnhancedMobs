package cn.yanshiqwq.enhanced_mobs.event

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import org.bukkit.Location
import taboolib.platform.type.BukkitProxyEvent

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.event.EnhancedMobSpawnEvent
 *
 * @author yanshiqwq
 * @since 2024/8/24 下午5:03
 */
data class EnhancedMobSpawnEvent(
    val mob: EnhancedMob,
    val location: Location
): BukkitProxyEvent()