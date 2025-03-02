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
    var count: Int,
    var speed: Double = 0.0,
    var offset: Vector = Vector(0, 0, 0),
    var spread: Vector = Vector(0, 0, 0)
) {
    fun vertical(amount: Double) {
        spread.y = amount
    }
    
    fun horizontal(amount: Double) {
        spread.x = amount
        spread.z = amount
    }
    
    var data: Any? = null
    
    fun build(loc: Location) = loc.world?.run {
        if (data == null)
            spawnParticle(particle, loc, count, offset.x, offset.y, offset.z, speed)
        else
            spawnParticle(particle, loc, count, offset.x, offset.y, offset.z, speed, data)
    }
}