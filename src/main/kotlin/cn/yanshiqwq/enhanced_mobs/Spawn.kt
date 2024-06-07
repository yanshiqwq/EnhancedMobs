package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.VariantBoost.Companion.applyVariantBoost
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntitySpawnEvent

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.Spawn
 *
 * @author yanshiqwq
 * @since 2024/6/2 09:13
 */

class Spawn: Listener {
    @EventHandler
    fun onEntitySpawn(event: EntitySpawnEvent){
        val entity = event.entity
        if (entity !is LivingEntity) return

        applyVariantBoost(entity)

//        distanceBoost(entity)
//        typeBoost(entity)

        entity.health = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: return
    }

//    companion object {
//        fun typeBoost(entity: LivingEntity){
//            if (randomList.all { entity.type != it }) return
//            val type = Random.nextDouble()
//            val isStrength = 0.085
//            val isEnhance = 0.035
//            val multiplier = when (type) {
//                in 0.0..isStrength -> 1.0
//                in isStrength..isStrength + isEnhance -> 1.4
//                else -> return
//            }
//            applyBoost(entity, multiplier, Boost.BoostType.TYPE)
//            entity.isGlowing = true
//        }
//        fun distanceBoost(entity: LivingEntity){
//            val loc = entity.location
//            val value = when (val distance = loc.subtract(loc.world.spawnLocation).length()) {
//                in 0.0..1500.0 -> 1 + distance / 6000
//                else -> 0.155 + 0.15 * ln(distance)
//            } - 1
//            entity.persistentDataContainer.set(NamespacedKey(INSTANCE!!, "distance_boost"), PersistentDataType.DOUBLE, value)
//            applyBoost(entity, value, Boost.BoostType.DISTANCE)
//        }
//    }
}