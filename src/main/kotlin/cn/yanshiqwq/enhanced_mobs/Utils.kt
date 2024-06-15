package cn.yanshiqwq.enhanced_mobs

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

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

    fun Location.playSound(sound: Sound, volume: Float, pitch: Float) {
        this.world.playSound(this, sound, volume, pitch)
    }

    fun Location.spawnParticle(
        particle: Particle,
        count: Int,
        size: Vector,
        speed: Double = 0.0,
        data: Any? = null,
        offset: Vector = Vector(0,1,0)
    ) {
        val loc = this.clone().add(offset)
        if (data != null) {
            this.world.spawnParticle(particle, loc, count, size.x, size.y, size.z, speed, data)
            return
        }
        this.world.spawnParticle(particle, loc, count, size.x, size.y, size.z, speed)
    }

    fun Entity.setMotionMultiplier(multiplier: Double){
        velocity = location.direction.multiply(multiplier)
    }

    fun LivingEntity.heal(percent: Double = 1.0) {
        if (isDead) return
        health = getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value * percent
    }

    fun Mob.initEquipment(slot: EquipmentSlot, material: Material) {
        equipment.setItem(slot, ItemStack(material))
    }

    fun LivingEntity.applyEffect(
        effectType: PotionEffectType,
        amplifier: Int = 0,
        duration: Int = Int.MAX_VALUE,
        particle: Boolean = true,
        ambient: Boolean = true
    ) {
       addPotionEffect(PotionEffect(effectType, duration, amplifier, ambient, particle))
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