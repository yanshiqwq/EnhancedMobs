package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.Main.Companion.INSTANCE
import cn.yanshiqwq.enhanced_mobs.TypeBoost.Companion.boost
import cn.yanshiqwq.enhanced_mobs.TypeBoost.Companion.distanceBoost
import cn.yanshiqwq.enhanced_mobs.TypeBoost.Companion.randomList
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.persistence.PersistentDataType
import kotlin.math.ln
import kotlin.random.Random

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

        when (entity) {
            is Husk -> VariantBoost.husk(entity)
            is Drowned -> VariantBoost.drowned(entity)
            is Giant -> VariantBoost.giant(entity)

            is PiglinAbstract -> VariantBoost.piglin(entity)
            is Stray -> VariantBoost.stray(entity)
            is Creeper -> if (entity.isPowered) VariantBoost.poweredCreeper(entity)
        }

        distanceBoost(entity)
        typeBoost(entity)

        entity.health = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
    }

    companion object {
        fun typeBoost(entity: LivingEntity){
            if (randomList.all { entity.type != it }) return
            val type = Random.nextDouble()
            val isStrength = 0.085
            val isEnhance = 0.035
            val multiplier = when (type) {
                in 0.0..isStrength -> 1.0
                in isStrength..isStrength + isEnhance -> 1.4
                else -> return
            }
            boost(entity, multiplier)
            entity.isGlowing = true
        }
        fun distanceBoost(entity: LivingEntity){
            val loc = entity.location
            val value = when (val distance = loc.subtract(loc.world.spawnLocation).length()) {
                in 0.0..1500.0 -> 1 + distance / 6000
                else -> 0.155 + 0.15 * ln(distance)
            } - 1
            entity.persistentDataContainer.set(NamespacedKey(INSTANCE!!, "distance_boost"), PersistentDataType.DOUBLE, value)
            distanceBoost(entity, value)
        }
    }
}