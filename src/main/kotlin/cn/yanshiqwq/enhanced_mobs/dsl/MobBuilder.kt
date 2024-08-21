package cn.yanshiqwq.enhanced_mobs.dsl

import cn.yanshiqwq.enhanced_mobs.Util
import kotlinx.coroutines.Runnable
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.attribute.Attributable
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import taboolib.common.platform.service.PlatformExecutor
import taboolib.platform.BukkitPlugin

open class MobBuilder(val mob: Mob) {
    // 修改名字
    fun name(name: String) = name(mob, name)
    fun name(entity: Entity, name: String) {
        entity.customName = name
    }

    // 修改基础属性
    fun base(entity: Attributable = mob, block: AttributeBuilder.() -> Unit) = AttributeBuilder().apply(block).setBase(entity)

    // 修改物品
    fun equip(entity: LivingEntity = mob, block: EquipmentBuilder.() -> Unit) = EquipmentBuilder().apply(block).build(entity)

    // 修改生物属性
    inline fun <reified T: Mob> property(entity: T, crossinline block: T.() -> Unit) {
        if (this !is T) throw IllegalArgumentException("Illegal EntityType: ${mob.type}")
        entity.block()
    }
    fun property(block: Mob.() -> Unit) = property(mob, block)

    // 添加药水效果
    fun effect(type: PotionEffectType, entity: LivingEntity = mob, block: PotionEffectBuilder.() -> Unit) {
        val builder = PotionEffectBuilder(type)
        block.invoke(builder)
        entity.addPotionEffect(builder.build())
    }

    // 添加指定药水类型的所有效果
    fun potion(entity: LivingEntity, type: PotionType, block: PotionTypeBuilder.() -> Unit) {
        val effect = PotionTypeBuilder(type.potionEffects).apply(block).build()
        entity.addPotionEffects(effect)
    }
    fun potion(type: PotionType, block: PotionTypeBuilder.() -> Unit) = potion(mob, type, block)

    // 声音
    fun sound(loc: Location, sound: Sound, category: SoundCategory, volume: Float, pitch: Float) = loc.world?.playSound(loc, sound, category, volume, pitch)
    fun sound(sound: Sound, pitch: Float, volume: Float = 1.0F) =
        sound(mob.location, sound, SoundCategory.HOSTILE, volume, pitch)

    // 监听器
    fun onDamage(params: ListenerBuilder<EntityDamageByEntityEvent>.() -> Unit) =
        ListenerBuilder(EntityDamageByEntityEvent::class.java)
            .apply(params)
            .build(mob)

    fun onAttack(params: ListenerBuilder<EntityDamageByEntityEvent>.() -> Unit) =
        ListenerBuilder(EntityDamageByEntityEvent::class.java)
            .apply(params)
            .build(mob) { damager }

    fun onDeath(params: ListenerBuilder<EntityDeathEvent>.() -> Unit) =
        ListenerBuilder(EntityDeathEvent::class.java)
            .apply(params)
            .build(mob)

    // 计时器
    fun onTimer(params: TimerBuilder.() -> Unit): PlatformExecutor.PlatformTask {
        val builder = TimerBuilder()
        params.invoke(builder)
        return builder.build()
    }

    fun delay(delay: Long = 0, async: Boolean = false, block: Runnable) = Bukkit.getScheduler().run {
        if (async)
            runTaskLater(BukkitPlugin.getInstance(), block, delay)
        else
            runTaskLaterAsynchronously(BukkitPlugin.getInstance(), block, delay)
    }

    // 条件
    fun <T> byChance(chance: Double): T.() -> Boolean = { Util.chance(chance) }
    fun <T> allOf(vararg bool: Boolean): T.() -> Boolean = { bool.all { it } }
    fun <T> anyOf(vararg bool: Boolean): T.() -> Boolean = { bool.any { it } }
    fun Mob.hasTarget() = target != null
    fun distance(entity: Entity, target: Entity): Double = entity.location.distance(target.location)
    fun ICooldown.cooldown(time: Long) = cooldown(mob.world.gameTime, time)

    // Skill
    fun placeBlock(
        delay: Long,
        type: Material,
        onPlace: () -> Unit,
        onRemove: () -> Unit,
        removeOnDeath: Boolean = true
    ) {
        val block = mob.location.block
        block.type = type
        onPlace.invoke()
        fun removeFire() {
            if (block.type != type) return
            onRemove.invoke()
            block.type = Material.AIR
        }
        delay(delay) { removeFire() }
        if (removeOnDeath) onDeath { removeFire() }
    }
}