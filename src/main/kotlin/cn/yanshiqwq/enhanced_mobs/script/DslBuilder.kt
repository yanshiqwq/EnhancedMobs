package cn.yanshiqwq.enhanced_mobs.script

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.EnhancedMob.Companion.attributeName
import cn.yanshiqwq.enhanced_mobs.EnhancedMob.Companion.attributeUUID
import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.data.*
import cn.yanshiqwq.enhanced_mobs.managers.PackManager
import cn.yanshiqwq.enhanced_mobs.managers.TypeManager
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Event
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask

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

        fun type(typeId: String, implementation: String, block: TypeBuilder.(EnhancedMob) -> Unit) {
            TypeId(typeId).implement(TypeManager.TypeKey(implementation), block)
        }

        data class TypeId(val id: String)
        private fun TypeId.implement(typeKey: TypeManager.TypeKey, block: TypeBuilder.(EnhancedMob) -> Unit) {
            val implementation = instance!!.typeManager.getType(typeKey)
            val builder = TypeBuilder(TypeManager.TypeKey(packId, this.id))
            types.add(TypeManager.MobType(TypeManager.TypeKey(packId, this.id)){
                implementation.function.invoke(this)
                block.invoke(builder, this)
            })
        }

        fun build(): PackManager.Pack {
            return PackManager.Pack(packId, types)
        }
    }

    class TypeBuilder(private val typeKey: TypeManager.TypeKey) {
        private val attributes = mutableListOf<Record.AttributeRecord>()
        val functions = mutableListOf<EnhancedMob.() -> Unit>()
        val tasks = mutableMapOf<String, BukkitTask>()

        fun attribute(attribute: Attribute, operation: AttributeModifier.Operation, factor: Record.DoubleFactor) {
            attributes.add(Record.AttributeRecord(attribute, Record.AttributeFactor(operation, factor)))
        }

        fun item(slot: EquipmentSlot, type: Material, block: SlotBuilder.() -> Unit = {}) = item(slot, ItemStack(type), block)

        fun item(slot: EquipmentSlot, item: ItemStack? = null, block: SlotBuilder.() -> Unit = {}) {
            val builder = SlotBuilder(slot, item)
            block.invoke(builder)
            functions.add(builder.build())
        }

        inline fun <reified T: Event> listener(noinline block: (T) -> Unit) = functions.add { initListener<T>(block) }

        abstract class AbstractTaskDsl {
            var id: String = "DEFAULT"
            var delay: Long = 0
            var period: Long = 0
        }

        open class TaskDsl: AbstractTaskDsl() {
            var function: EnhancedMob.(LivingEntity?) -> Boolean = { true }
        }

        class ItemTaskDsl: AbstractTaskDsl() {
            var function: EnhancedMob.(LivingEntity) -> Boolean = { true }
            var distance: Double = 0.0
                set(value) {
                    if (value < 0.0) throw IllegalArgumentException("Distance cannot be less than zero")
                    field = value
                }

            var before: ItemStack = ItemStack(Material.AIR)
            var after: ItemStack = ItemStack(Material.AIR)
            var slot: EquipmentSlot = EquipmentSlot.OFF_HAND
            var hasLineOfSight: Boolean = true
        }

        fun run(block: EnhancedMob.() -> Unit) {
            functions.add(block)
        }

        fun task(init: TaskDsl.() -> Unit) {
            val dsl = TaskDsl().apply(init)

            functions.add {
                cancelTask(dsl.id)
                if (dsl.delay > 0) {
                    this@TypeBuilder.tasks[dsl.id] = delayTask(init)
                }
                if (dsl.period > 0) {
                    this@TypeBuilder.tasks[dsl.id] = periodTask(init)
                }
                throw IllegalArgumentException("Unknown task type")
            }
        }

        fun cancelTask(id: String) = this@TypeBuilder.tasks[id]?.cancel()

        fun itemTask(init: ItemTaskDsl.() -> Unit) {
            val dsl = ItemTaskDsl().apply(init)

            functions.add {
                if (entity.target == null) return@add
                if (dsl.period > 0) {
                    this@TypeBuilder.tasks[dsl.id] = periodItemTask(init)
                    return@add
                }
                this@TypeBuilder.tasks[dsl.id] = disposableItemTask(init)
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

        fun enchant(enchant: Enchantment, factor: Record.IntFactor) {
            enchants.add(Record.EnchantRecord(enchant, factor))
        }

        fun build(): EnhancedMob.() -> Unit {
            return {
                if (item != null) entity.equipment.setItem(slot, item)
                enchants.forEach { it.apply(this.entity, this.multiplier, slot) }
            }
        }
    }
}