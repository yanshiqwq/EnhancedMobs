package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.Material
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

/**
 * 用于构建并设置实体的装备
 */
class EquipmentBuilder {
    /**
     * 头部装备
     */
    var head: ItemStack? = null
    fun head(type: Material) { head = ItemStack(type) }

    /**
     * 胸部装备
     */
    var chest: ItemStack? = null
    fun chest(type: Material) { chest = ItemStack(type) }

    /**
     * 腿部装备
     */
    var legs: ItemStack? = null
    fun legs(type: Material) { legs = ItemStack(type) }

    /**
     * 脚部装备
     */
    var feet: ItemStack? = null
    fun feet(type: Material) { feet = ItemStack(type) }

    /**
     * 主手装备
     */
    var hand: ItemStack? = null
    fun hand(type: Material) { hand = ItemStack(type) }

    /**
     * 副手装备
     */
    var offHand: ItemStack? = null
    fun offHand(type: Material) { offHand = ItemStack(type) }

    /**
     * 将设置好的装备应用到指定的实体上
     *
     * @param entity 要应用装备的实体
     */
    fun apply(entity: LivingEntity) {
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
