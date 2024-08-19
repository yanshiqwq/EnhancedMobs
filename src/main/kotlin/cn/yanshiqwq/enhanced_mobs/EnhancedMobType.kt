package cn.yanshiqwq.enhanced_mobs

import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mob
import java.util.*

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.EnhancedMobType
 *
 * @author yanshiqwq
 * @since 2024/8/19 下午9:16
 */
class EnhancedMobType(
    private val id: String,
    private val type: EntityType = EntityType.ZOMBIE,
    private val block: (Mob) -> Unit = {}
) {
    fun spawn(loc: Location): UUID {
        val world = loc.world ?: throw NullPointerException("The specified location's world is null.")
        val entity = world.spawnEntity(loc, type) as? Mob
            ?: throw IllegalArgumentException("The provided type must be a subclass of Mob.")
        block.invoke(entity)
        return entity.uniqueId
    }
}