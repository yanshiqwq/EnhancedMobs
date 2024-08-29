package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.Location
import org.bukkit.Particle
import taboolib.common.util.Vector

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.ParticleBuilder
 *
 * @author yanshiqwq
 * @since 2024/8/29 下午11:59
 */
data class ParticleBuilder(
    val particle: Particle,
    var count: Int = 32,
    var speed: Double = 0.0
) {
    var offset = Vector(0, 0, 0)
    var spread = Vector(0, 0, 0)
    var data: Any? = null
    
    fun vertical(amount: Double) {
        spread.y = amount
    }
    fun horizontal(amount: Double) {
        spread.x = amount
        spread.z = amount
    }
    
    fun build(loc: Location) = if (data == null)
        loc.world?.spawnParticle(particle, loc, count, offset.x, offset.y, offset.z, speed)
    else
        loc.world?.spawnParticle(particle, loc, count, offset.x, offset.y, offset.z, speed, data)
}