@file:Suppress("unused")

package cn.yanshiqwq.enhanced_mobs.api

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.data.Record
import io.papermc.paper.event.entity.EntityMoveEvent
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.event.Event
import org.bukkit.event.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
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

    data class ProjectileDamageEventDsl(
        val projectile: Projectile,
        val target: LivingEntity,
    )
    inline fun EnhancedMob.onProjectileDamage(crossinline block: ProjectileDamageEventDsl.(event: EntityDamageByEntityEvent) -> Unit) {
        listener<EntityDamageByEntityEvent> {
            if ((it.entity as? Player)?.isBlocking == true) return@listener
            val projectile = it.damager as? Projectile ?: return@listener
            if (projectile.shooter != entity) return@listener
            val target = it.entity as? LivingEntity ?: return@listener
            val dsl = ProjectileDamageEventDsl(projectile, target)
            dsl.block(it)
        }
    }

    data class ArrowDamageEventDsl(
        val arrow: Arrow,
        val target: LivingEntity,
    )
    inline fun EnhancedMob.onArrowDamage(crossinline block: ArrowDamageEventDsl.(event: EntityDamageByEntityEvent) -> Unit) {
        listener<EntityDamageByEntityEvent> {
            if (it.damager !is Arrow || it.entity !is LivingEntity)
            if (it.entity is Player && (it.entity as Player).isBlocking) return@listener // TODO 测试it.isCancelled是否与此行效果一致
            val arrow = it.damager as? Arrow ?: return@listener
            if (arrow.shooter != entity) return@listener
            val target = it.entity as? LivingEntity ?: return@listener
            val dsl = ArrowDamageEventDsl(arrow, target)
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
            val dsl = AttackEventDsl(it.entity as? LivingEntity ?: return@listener)
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

    data class PotionThrownDsl(
        private val mob: EnhancedMob,
        val potion: ThrownPotion,
        val target: LivingEntity
    ) {
        fun effect(type: PotionEffectType, duration: Record.IntFactor, amplifier: Int): Runnable = Runnable {
            val meta = potion.potionMeta
            meta.apply {
                basePotionType = PotionType.UNCRAFTABLE
                clearCustomEffects()
                addCustomEffect(PotionEffect(type, duration.value(mob.multiplier), amplifier), true)
                color = type.color
            }
            potion.potionMeta = meta
        }
        fun potion(type: PotionType) = Runnable {
            val meta = potion.potionMeta
            meta.basePotionType = type
            potion.potionMeta = meta
        }
        fun lingering() = Runnable {
            val meta = potion.item.itemMeta
            val item = ItemStack(Material.LINGERING_POTION)
            item.setItemMeta(meta)
            potion.item = item
        }
        fun splash() = Runnable {
            val meta = potion.item.itemMeta
            val item = ItemStack(Material.SPLASH_POTION)
            item.setItemMeta(meta)
            potion.item = item
        }
    }
    inline fun EnhancedMob.onPotionThrown(crossinline block: PotionThrownDsl.(ProjectileLaunchEvent) -> Unit) {
        listener<ProjectileLaunchEvent> {
            val potion = it.entity as? ThrownPotion ?: return@listener
            val entity = potion.shooter as? Mob ?: return@listener
            if (entity != this.entity) return@listener
            val target = entity.target ?: return@listener
            val dsl = PotionThrownDsl(this, potion, target)
            instance!!.logger.info(potion.potionMeta.toString())
            dsl.block(it)
        }
    }

    inline fun EnhancedMob.onMove(crossinline block: EntityMoveEvent.() -> Unit) {
        listener<EntityMoveEvent> {
            if (it.entity != entity) return@listener
            it.block()
        }
    }
}