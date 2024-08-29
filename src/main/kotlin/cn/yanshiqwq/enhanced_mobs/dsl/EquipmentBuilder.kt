package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import taboolib.platform.util.setEquipment
import taboolib.type.BukkitEquipment

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.EquipmentBuilder
 *
 * @author yanshiqwq
 * @since 2024/8/19 下午 11:06
 */
/**
 * 用于构建并设置实体的装备
 */
class EquipmentBuilder {
    /**
     * 头部装备
     */
    var head: ItemStack? = null
    
    /**
     * 设置头部装备
     *
     * @param type 装备的材料类型
     */
    fun head(type: Material) {
        head = ItemStack(type)
    }
    
    /**
     * 胸部装备
     */
    var chest: ItemStack? = null
    
    /**
     * 设置胸部装备
     *
     * @param type 装备的材料类型
     */
    fun chest(type: Material) {
        chest = ItemStack(type)
    }
    
    /**
     * 腿部装备
     */
    var legs: ItemStack? = null
    
    /**
     * 设置腿部装备
     *
     * @param type 装备的材料类型
     */
    fun legs(type: Material) {
        legs = ItemStack(type)
    }
    
    /**
     * 脚部装备
     */
    var feet: ItemStack? = null
    
    /**
     * 设置脚部装备
     *
     * @param type 装备的材料类型
     */
    fun feet(type: Material) {
        feet = ItemStack(type)
    }
    
    /**
     * 主手装备
     */
    var hand: ItemStack? = null
    
    /**
     * 设置主手装备
     *
     * @param type 装备的材料类型
     */
    fun hand(type: Material) {
        hand = ItemStack(type)
    }
    
    /**
     * 副手装备
     */
    var offHand: ItemStack? = null
    
    /**
     * 设置副手装备
     *
     * @param type 装备的材料类型
     */
    fun offHand(type: Material) {
        offHand = ItemStack(type)
    }
    
    /**
     * 将设置好的装备应用到指定的实体上
     *
     * @param entity 要应用装备的实体
     */
    fun apply(entity: LivingEntity) {
        mapOf(
            BukkitEquipment.HEAD to head,
            BukkitEquipment.CHEST to chest,
            BukkitEquipment.LEGS to legs,
            BukkitEquipment.FEET to feet,
            BukkitEquipment.HAND to hand,
            BukkitEquipment.OFF_HAND to offHand
        ).filterValues { it != null }
            .forEach { (slot, item) ->
                entity.setEquipment(slot, item!!)
            }
    }
}