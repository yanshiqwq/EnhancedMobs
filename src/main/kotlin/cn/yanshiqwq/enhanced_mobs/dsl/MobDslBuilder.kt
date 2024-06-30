package cn.yanshiqwq.enhanced_mobs.dsl

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.data.*
import cn.yanshiqwq.enhanced_mobs.managers.PackManager
import cn.yanshiqwq.enhanced_mobs.managers.TypeManager
import org.bukkit.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.script.DslBuilder
 *
 * @author yanshiqwq
 * @since 2024/6/25 22:15
 */
object MobDslBuilder {
    fun pack(name: String, block: PackBuilder.() -> Unit): PackManager.Pack {
        val builder = PackBuilder(name)
        builder.block()
        return builder.build()
    }

    class PackBuilder(private val packId: String) {
        private val types = mutableListOf<TypeManager.MobType>()

        fun type(typeId: String, block: EnhancedMob.() -> Unit) {
            val builder = TypeBuilder(TypeManager.TypeKey(packId, typeId))
            types.add(builder.build() {
                this.block()
            })
        }

        fun type(typeId: String, impl: String, block: EnhancedMob.() -> Unit) {
            val type = instance!!.typeManager.getType(TypeManager.TypeKey(impl))
            type(typeId, type, block)
        }

        fun type(typeId: String, impl: TypeManager.MobType, block: EnhancedMob.() -> Unit) {
            types.add(TypeManager.MobType(TypeManager.TypeKey(packId, typeId)) {
                impl.function.invoke(this)
                this.block()
            })
        }

        fun build(): PackManager.Pack {
            return PackManager.Pack(packId, types)
        }
    }

    class TypeBuilder(private val typeKey: TypeManager.TypeKey) {
        fun build(block: EnhancedMob.() -> Unit): TypeManager.MobType {
            return TypeManager.MobType(typeKey) { block() }
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