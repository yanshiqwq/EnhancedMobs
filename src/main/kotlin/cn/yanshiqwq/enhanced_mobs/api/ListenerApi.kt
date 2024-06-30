package cn.yanshiqwq.enhanced_mobs.api

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import org.bukkit.entity.*
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityResurrectEvent
import org.bukkit.event.entity.EntityShootBowEvent
import kotlin.reflect.KClass

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.script.Api.ListenerApi
 *
 * @author yanshiqwq
 * @since 2024/6/30 下午4:07
 */
object ListenerApi {
    data class Listener(val eventClass: KClass<out Event>, val function: (Event) -> Unit)
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T: Event> EnhancedMob.listener(noinline block: (T) -> Unit) {
        if (entity.isDead) return
        val listener = Listener(T::class, block as (Event) -> Unit)
        listeners.add(listener)
    }

    data class ArrowDamageEventDsl(
        val arrow: Arrow,
        val entity: LivingEntity,
    )
    inline fun EnhancedMob.onArrowDamage(crossinline block: ArrowDamageEventDsl.(event: EntityDamageByEntityEvent) -> Unit) {
        listener<EntityDamageByEntityEvent> {
            if (it.damager !is Arrow || it.entity !is LivingEntity) return@listener
            if (it.entity is Player && (it.entity as Player).isBlocking) return@listener
            val arrow = it.damager as Arrow
            if (arrow.shooter != entity) return@listener
            val entity = it.entity as LivingEntity

            val dsl = ArrowDamageEventDsl(arrow, entity)
            dsl.block(it)
        }
    }

    inline fun EnhancedMob.onBowShoot(crossinline block: EntityShootBowEvent.() -> Unit) {
        listener<EntityShootBowEvent> {
            if (it.entity != entity) return@listener
            it.block()
        }
    }

    data class AttackEventDsl(val target: LivingEntity)
    inline fun EnhancedMob.onAttack(crossinline block: AttackEventDsl.(event: EntityDamageByEntityEvent) -> Unit) {
        listener<EntityDamageByEntityEvent> {
            if (it.damager != entity) return@listener
            if (it.entity !is LivingEntity) return@listener

            val dsl = AttackEventDsl(it.entity as LivingEntity)
            dsl.block(it)
        }
    }

    data class PreDamageEventDsl(val attacker: LivingEntity)
    inline fun EnhancedMob.onPreDamage(crossinline block: PreDamageEventDsl.(event: EntityDamageByEntityEvent) -> Unit) {
        listener<EntityDamageByEntityEvent> {
            if (it.damager !is LivingEntity) return@listener
            if (it.entity != entity) return@listener

            val dsl = PreDamageEventDsl(it.damager as LivingEntity)
            dsl.block(it)
        }
    }

    inline fun EnhancedMob.onDeath(crossinline block: EntityDeathEvent.() -> Unit) {
        listener<EntityDeathEvent> {
            if (it.entity != entity) return@listener
            it.block()
        }
    }

    inline fun EnhancedMob.onResurrect(crossinline block: EntityResurrectEvent.() -> Unit) {
        listener<EntityResurrectEvent> {
            if (!it.isCancelled) return@listener
            if (it.entity != entity) return@listener
            it.block()
        }
    }
}