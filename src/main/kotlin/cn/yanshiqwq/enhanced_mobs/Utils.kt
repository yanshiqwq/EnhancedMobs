package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeInstance
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.*
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Team
import org.bukkit.util.Vector

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.Utils
 *
 * @author yanshiqwq
 * @since 2024/6/11 00:16
 */
@Suppress("unused")
object Utils {
    fun getTeam(teamName: String): Team? {
        return instance!!.server.scoreboardManager.mainScoreboard.runCatching {
            getTeam(teamName) ?: registerNewTeam(teamName)
        }.getOrNull()
    }

    fun AttributeInstance.addModifierSafe(modifier: AttributeModifier){
        if (modifiers.contains(modifier)) return
        addModifier(modifier)
    }

    fun Boolean.Companion.all(vararg booleans: Boolean): Boolean {
        return booleans.all { it }
    }
    fun Boolean.Companion.any(vararg booleans: Boolean): Boolean {
        return booleans.any { it }
    }

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

    fun Entity.isOnFire() = this.fireTicks > 0

    fun Mob.equip(slot: EquipmentSlot, material: Material) {
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

    fun Location.getNearestPlayer(): Player? {
        // 获取世界中的所有玩家
        val players = Bukkit.getOnlinePlayers()

        // 初始化最近的玩家和最小距离
        var nearestPlayer: Player? = null
        var minDistance = Double.MAX_VALUE

        // 遍历所有玩家，找到距离怪物最近的玩家
        for (player in players) {
            val distance = player.location.distance(this)
            if (distance < minDistance) {
                minDistance = distance
                nearestPlayer = player
            }
        }

        return nearestPlayer
    }

    inline fun <reified T : Entity> Location.spawnEntity(type: EntityType, function: T.() -> Unit) {
        val entity = this.world.spawnEntity(this, type, CreatureSpawnEvent.SpawnReason.REINFORCEMENTS)
        if (type.entityClass == T::class.java)
            function.invoke(entity as T)
        else
            throw IllegalArgumentException("The generic type variable does not match the provided type: $type")
    }
}