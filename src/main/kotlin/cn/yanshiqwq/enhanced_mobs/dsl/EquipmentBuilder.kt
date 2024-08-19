package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.EquipmentBuilder
 *
 * @author yanshiqwq
 * @since 2024/8/19 下午11:06
 */

class EquipmentBuilder: IBuilder<EquipmentSlot, ItemStack?> {
    var head: ItemStack? = null
    var chest: ItemStack? = null
    var legs: ItemStack? = null
    var feet: ItemStack? = null
    var hand: ItemStack? = null
    var offHand: ItemStack? = null

    override fun build() = mapOf(
        EquipmentSlot.HEAD to head,
        EquipmentSlot.CHEST to chest,
        EquipmentSlot.LEGS to legs,
        EquipmentSlot.FEET to feet,
        EquipmentSlot.HAND to hand,
        EquipmentSlot.OFF_HAND to offHand
    )
}