package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*


/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.EnhancedMob
 *
 * @author yanshiqwq
 * @since 2024/6/7 05:29
 */

class EnhancedMob(val multiplier: Double, val entity: LivingEntity){
    companion object {
        val key = NamespacedKey(instance!!, "multiplier")
    }
    init {
        entity.persistentDataContainer.set(key, PersistentDataType.DOUBLE, multiplier)
        instance!!.mobManager.register(entity.uniqueId, this)
    }

    fun initAttribute(record: AttributeRecord){
        val attributeUUID: UUID = UUID.fromString("a8d0bc44-1534-43f0-a594-f74c7c91bc59")
        val attributeName = "EnhancedMob Spawn Boost"
        record.apply(this.entity, this.multiplier, attributeUUID, attributeName)
    }
    fun initEquipment(slot: EquipmentSlot, item: ItemStack){
        this.entity.equipment?.setItem(slot, item)
    }
    fun initEquipment(slot: EquipmentSlot, material: Material){
        this.entity.equipment?.setItem(slot, ItemStack(material))
    }
    fun initEnchant(slot: EquipmentSlot, record: EnchantRecord){
        record.apply(this.entity, multiplier, slot)
    }
}