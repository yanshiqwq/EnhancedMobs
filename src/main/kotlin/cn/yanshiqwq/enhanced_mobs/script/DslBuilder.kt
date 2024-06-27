package cn.yanshiqwq.enhanced_mobs.script

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.EnhancedMob.Companion.attributeName
import cn.yanshiqwq.enhanced_mobs.EnhancedMob.Companion.attributeUUID
import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.data.*
import cn.yanshiqwq.enhanced_mobs.managers.PackManager
import cn.yanshiqwq.enhanced_mobs.managers.TypeManager
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import org.bukkit.event.Event
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import java.util.*
import kotlin.reflect.KClass

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.script.DslBuilder
 *
 * @author yanshiqwq
 * @since 2024/6/25 22:15
 */
object DslBuilder {
    fun pack(name: String, block: PackBuilder.() -> Unit): PackManager.Pack {
        val builder = PackBuilder(name)
        builder.block()
        return builder.build()
    }

    class PackBuilder(private val packId: String) {
        private val types = mutableListOf<TypeManager.MobType>()

        fun type(typeId: String, block: TypeBuilder.() -> Unit) {
            val builder = TypeBuilder(TypeManager.TypeKey(packId, typeId))
            builder.block()
            types.add(builder.build())
        }

        fun type(typeId: String, impl: String, block: TypeBuilder.(EnhancedMob) -> Unit) {
            val typeKey = TypeManager.TypeKey(impl)
            val implInstance = instance!!.typeManager.getType(typeKey)
            val builder = TypeBuilder(TypeManager.TypeKey(packId, typeId))

            types.add(TypeManager.MobType(TypeManager.TypeKey(packId, typeId)) {
                implInstance.function.invoke(this)
                builder.block(this)
            })
        }

        fun build(): PackManager.Pack {
            return PackManager.Pack(packId, types)
        }
    }

    class TypeBuilder(private val typeKey: TypeManager.TypeKey) {
        val functions = mutableListOf<EnhancedMob.() -> Unit>()

        private val attributes = mutableListOf<Record.AttributeRecord>()
        fun attribute(attribute: Attribute, operation: AttributeModifier.Operation, factor: Record.DoubleFactor) {
            attributes.add(Record.AttributeRecord(attribute, Record.AttributeFactor(operation, factor)))
        }

        fun item(slot: EquipmentSlot, type: Material, block: SlotBuilder.() -> Unit = {}) = item(slot, ItemStack(type), block)
        fun item(slot: EquipmentSlot, item: ItemStack? = null, block: SlotBuilder.() -> Unit = {}) {
            val builder = SlotBuilder(slot, item)
            block.invoke(builder)
            functions.add(builder.build())
        }

        private val tasks = mutableMapOf<String, BukkitTask>()
        data class TaskId(val id: String = UUID.randomUUID().toString()) {
            private var switch: () -> Unit = {}
            fun onSwitch(block: () -> Unit) { switch = block }
            fun switch() = switch.invoke()
        }



        data class Listener(val eventClass: KClass<out Event>, val function: (Event) -> Unit)
        val listeners: ArrayList<Listener> = arrayListOf()

        @Suppress("UNCHECKED_CAST")
        inline fun <reified T: Event> listener(noinline block: (T) -> Unit) = functions.add {
            if (entity.isDead) return@add
            val listener = Listener(T::class, block as (Event) -> Unit)
            listeners.add(listener)
        }

        fun run(block: EnhancedMob.() -> Unit) {
            functions.add(block)
        }

        inline fun <reified T: Mob> property(crossinline block: T.(EnhancedMob) -> Unit) {
            functions.add {
                if (entity !is T) throw IllegalArgumentException("Illegal EntityType: ${entity.type}")
                entity.block(this)
            }
        }

        fun task(taskId: TaskId = TaskId(), delay: Long = 0L, block: Runnable): TaskId {
            functions.add {
                cancelTask(taskId)
                taskId.switch()
                val func = mobTask(this) { block.run() }
                addTask(taskId, Bukkit.getScheduler().runTaskLater(instance!!, func, delay))
            }
            return taskId
        }
        fun task(taskId: TaskId = TaskId(), delay: Long = 0L, period: Long, block: Runnable): TaskId {
            functions.add {
                cancelTask(taskId)
                taskId.switch()
                val func = mobTask(this) { block.run() }
                addTask(taskId, Bukkit.getScheduler().runTaskTimer(instance!!, func, delay, period))
            }
            return taskId
        }

        private fun mobTask(mob: EnhancedMob, block: Runnable): Runnable {
            return Runnable {
                if (mob.entity.isDead) return@Runnable
                block.run()
            }
        }
        private fun addTask(taskId: TaskId, task: BukkitTask) { this@TypeBuilder.tasks[taskId.id] = task }
        fun cancelTask(taskId: TaskId) {
            this@TypeBuilder.tasks[taskId.id].run {
                if (this == null) return@run
                taskId.switch()
                cancel()
            }
        }

        fun itemTask(
            distance: Double,
            before: ItemStack,
            after: ItemStack? = null,
            hasLineOfSight: Boolean = true,
            slot: EquipmentSlot = EquipmentSlot.OFF_HAND,
            block: EnhancedMob.(LivingEntity) -> Unit
        ) {
            functions.add {
                val task = mobItemTask(this, distance, before, hasLineOfSight, slot) {
                    this.block(it)
                    if (after != null) entity.equipment.setItem(slot, ItemStack(after))
                }
                Bukkit.getScheduler().runTask(instance!!, task)
            }
        }
        fun itemTask(
            distance: Double,
            delay: Long = 0L,
            period: Long,
            before: ItemStack,
            after: ItemStack? = null,
            hasLineOfSight: Boolean = true,
            slot: EquipmentSlot = EquipmentSlot.OFF_HAND,
            block: EnhancedMob.(LivingEntity) -> Unit
        ) {
            functions.add {
                val task = mobItemTask(this, distance, before, hasLineOfSight, slot) {
                    this.block(it)
                    if (after != null) entity.equipment.setItem(slot, ItemStack(after))
                }
                Bukkit.getScheduler().runTaskTimer(instance!!, task, delay, period)
            }
        }
        private inline fun mobItemTask(
            mob: EnhancedMob,
            distance: Double,
            before: ItemStack,
            hasLineOfSight: Boolean,
            slot: EquipmentSlot = EquipmentSlot.OFF_HAND,
            crossinline block: EnhancedMob.(LivingEntity) -> Unit
        ): Runnable {
            return Runnable {
                val target = mob.entity.target ?: return@Runnable
                if (target.location.distance(mob.entity.location) > distance) return@Runnable
                if (mob.entity.isDead) return@Runnable
                if (mob.entity.equipment.getItem(slot).type != before.type) return@Runnable
                if (hasLineOfSight && !target.hasLineOfSight(mob.entity)) return@Runnable
                block.invoke(mob, mob.entity.target!!)
            }
        }

        fun build(): TypeManager.MobType {
            return TypeManager.MobType(typeKey) {
                attributes.forEach { it.apply(entity, multiplier, attributeUUID, attributeName) }
                functions.forEach { it.invoke(this) }
            }
        }
    }

    class SlotBuilder(private val slot: EquipmentSlot, private val item: ItemStack? = null) {
        private val enchants = mutableListOf<Record.EnchantRecord>()

        fun item(type: Material) = item?.withType(type)

        fun enchant(enchant: Enchantment, factor: Record.IntFactor) = enchants.add(Record.EnchantRecord(enchant, factor))

        fun build(): EnhancedMob.() -> Unit {
            return {
                if (item != null) entity.equipment.setItem(slot, item)
                enchants.forEach { it.apply(this.entity, this.multiplier, slot) }
            }
        }
    }
}