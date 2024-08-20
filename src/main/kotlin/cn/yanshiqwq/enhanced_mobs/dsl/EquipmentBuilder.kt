package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.EquipmentBuilder
 *
 * @author yanshiqwq
 * @since 2024/8/19 下午11:06
 */

class EquipmentBuilder {
    var head: ItemStack? = null
    var chest: ItemStack? = null
    var legs: ItemStack? = null
    var feet: ItemStack? = null
    var hand: ItemStack? = null
    var offHand: ItemStack? = null

    fun build(entity: LivingEntity) {
        mapOf(
            EquipmentSlot.HEAD to head,
            EquipmentSlot.CHEST to chest,
            EquipmentSlot.LEGS to legs,
            EquipmentSlot.FEET to feet,
            EquipmentSlot.HAND to hand,
            EquipmentSlot.OFF_HAND to offHand
        ).filterValues { it != null }
            .forEach { (slot, item) ->
                entity.equipment?.setItem(slot, item)
            }
    }
}