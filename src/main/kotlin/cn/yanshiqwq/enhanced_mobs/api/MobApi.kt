package cn.yanshiqwq.enhanced_mobs.api

import cn.yanshiqwq.enhanced_mobs.Util
import cn.yanshiqwq.enhanced_mobs.dsl.AttributeBuilder
import cn.yanshiqwq.enhanced_mobs.dsl.EquipmentBuilder
import cn.yanshiqwq.enhanced_mobs.dsl.PotionEffectBuilder
import cn.yanshiqwq.enhanced_mobs.dsl.PotionTypeBuilder
import kotlinx.coroutines.Runnable
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.attribute.Attributable
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityEvent
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.function.registerBukkitListener
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import taboolib.platform.BukkitPlugin

open class MobApi(val mob: Mob) {
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

    class ListenerParams {
        var priority: EventPriority = EventPriority.NORMAL
        var ignoreCancelled: Boolean = true
        var runAfterEntityDead: Boolean = false
    }

    // 声音
    fun sound(loc: Location) = Sound.BLOCK_FIRE_EXTINGUISH // TODO

    // 实体事件监听器
    inline fun <reified T: EntityEvent> listen(
        crossinline condition: T.() -> Boolean = { true },
        params: ListenerParams.() -> Unit = {},
        crossinline criteria: T.() -> Entity = { entity },
        crossinline block: T.() -> Unit
    ) {
        val builder = ListenerParams()
        params.invoke(builder)
        registerBukkitListener(T::class.java, builder.priority, builder.ignoreCancelled) { event ->
            // 检查一般条件
            if (!condition.invoke(event)) return@registerBukkitListener

            // 检查实体相关条件
            val entity = criteria.invoke(event) as? Mob ?: return@registerBukkitListener
            if (entity.uniqueId != mob.uniqueId) return@registerBukkitListener

            // 检查实体是否死亡
            if (entity.isDead && !builder.runAfterEntityDead) return@registerBukkitListener

            // 执行块
            block.invoke(event)
        }
    }

    // alias
    inline fun onDamage(
        crossinline condition: EntityDamageByEntityEvent.() -> Boolean = { true },
        params: ListenerParams.() -> Unit = {},
        crossinline block: EntityDamageByEntityEvent.() -> Unit
    ) = listen<EntityDamageByEntityEvent>(condition, params, { entity }, block)

    inline fun onAttack(
        crossinline condition: EntityDamageByEntityEvent.() -> Boolean = { true },
        params: ListenerParams.() -> Unit = {},
        crossinline block: EntityDamageByEntityEvent.() -> Unit
    ) = listen<EntityDamageByEntityEvent>(condition, params, { damager }, block)

    inline fun onDeath(
        crossinline condition: EntityDeathEvent.() -> Boolean = { true },
        params: ListenerParams.() -> Unit = {},
        crossinline block: EntityDeathEvent.() -> Unit
    ) = listen<EntityDeathEvent>(condition, params, { entity }, block)

    // 计时器
    fun onTimer(period: Long = 20, delay: Long = 0, async: Boolean = false, condition: Boolean, block: PlatformExecutor.PlatformTask.() -> Unit) =
        submit(false, async, delay, period) {
            if(condition) this.block()
        }

    fun delay(delay: Long = 0, async: Boolean = false, block: Runnable) = Bukkit.getScheduler().run {
        if (async)
            runTaskLater(BukkitPlugin.getInstance(), block, delay)
        else
            runTaskLaterAsynchronously(BukkitPlugin.getInstance(), block, delay)
    }


    // 条件
    fun <T> byChance(chance: Double): T.() -> Boolean = {
        Util.chance(chance)
    }

    fun Boolean.Companion.all(vararg bool: Boolean): Boolean = bool.all { it }
    fun Boolean.Companion.any(vararg bool: Boolean): Boolean = bool.any { it }
    fun Mob.hasTarget() = target != null
    fun distance(entity: Entity, target: Entity): Double = entity.location.distance(target.location)
}