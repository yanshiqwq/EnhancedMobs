package cn.yanshiqwq.enhanced_mobs

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.event.entity.CreatureSpawnEvent

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.Utils
 *
 * @author yanshiqwq
 * @since 2024/6/11 00:16
 */
object Utils {
    fun Location.placeBlock(type: Material) {
        this.block.run {
            if (!isReplaceable) return
            setType(type, true)
        }
    }

    fun Material.isAxe(): Boolean {
        return this in arrayOf(
            Material.WOODEN_AXE,
            Material.STONE_AXE,
            Material.GOLDEN_AXE,
            Material.IRON_AXE,
            Material.DIAMOND_AXE,
            Material.NETHERITE_AXE
        )
    }

    inline fun <reified T : Entity> Location.spawnEntity(type: EntityType, function: T.() -> Unit) {
        val entity = this.world.spawnEntity(this, type, CreatureSpawnEvent.SpawnReason.REINFORCEMENTS)
        if (type.entityClass == T::class.java)
            (entity as T).function()
        else
            throw IllegalArgumentException("The generic type variable does not match the provided type: $type")
    }
}