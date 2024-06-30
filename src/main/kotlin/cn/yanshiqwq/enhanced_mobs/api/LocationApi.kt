package cn.yanshiqwq.enhanced_mobs.api

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.util.Vector

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.script.Api.Location
 *
 * @author yanshiqwq
 * @since 2024/6/30 下午4:06
 */
object LocationApi {
    inline fun <reified T : Entity> Location.spawnEntity(type: EntityType, function: T.() -> Unit) {
        val entity = this.world.spawnEntity(this, type, CreatureSpawnEvent.SpawnReason.REINFORCEMENTS)
        if (type.entityClass == T::class.java)
            function.invoke(entity as T)
        else
            throw IllegalArgumentException("The generic type variable does not match the provided type: $type")
    }
    fun Location.placeBlock(type: Material) {
        this.block.run {
            if (!isReplaceable) return
            setType(type, true)
        }
    }
    fun Location.playSound(sound: Sound, volume: Float, pitch: Float) = world.playSound(this, sound, volume, pitch)
    fun Location.spawnParticle(
        particle: Particle,
        count: Int,
        size: Vector,
        speed: Double = 0.0,
        data: Any? = null,
        offset: Vector = Vector(0,1,0)
    ) {
        val loc = clone().add(offset)
        if (data != null) {
            world.spawnParticle(particle, loc, count, size.x, size.y, size.z, speed, data)
            return
        }
        world.spawnParticle(particle, loc, count, size.x, size.y, size.z, speed)
    }
}