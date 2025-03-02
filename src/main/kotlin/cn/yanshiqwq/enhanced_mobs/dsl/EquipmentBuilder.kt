package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import taboolib.platform.util.ItemBuilder
import taboolib.platform.util.buildItem
import taboolib.platform.util.setEquipment
import taboolib.type.BukkitEquipment
import taboolib.type.BukkitEquipment.*

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
data class EquipmentBuilder(
    private val map: MutableMap<BukkitEquipment, Pair<ItemStack?, Double?>> = mutableMapOf()
) {
    private fun slot(
        slot: BukkitEquipment,
        item: ItemStack,
        dropChance: Double = DEFAULT_DROP_CHANCE,
        block: ItemStack.() -> Unit = {}
    ) {
        block(item)
        map[slot] = item to dropChance
    }

    private fun slot(
        slot: BukkitEquipment,
        type: Material,
        dropChance: Double = DEFAULT_DROP_CHANCE,
        block: ItemBuilder.() -> Unit = {}
    ) {
        map[slot] = buildItem(type, block) to dropChance
    }

    companion object {
        const val DEFAULT_DROP_CHANCE = 0.085
    }

    fun head(type: Material, dropChance: Double = DEFAULT_DROP_CHANCE, block: ItemBuilder.() -> Unit = {}) =
        slot(HEAD, type, dropChance, block)

    fun chest(type: Material, dropChance: Double = DEFAULT_DROP_CHANCE, block: ItemBuilder.() -> Unit = {}) =
        slot(CHEST, type, dropChance, block)

    fun legs(type: Material, dropChance: Double = DEFAULT_DROP_CHANCE, block: ItemBuilder.() -> Unit = {}) =
        slot(LEGS, type, dropChance, block)

    fun feet(type: Material, dropChance: Double = DEFAULT_DROP_CHANCE, block: ItemBuilder.() -> Unit = {}) =
        slot(FEET, type, dropChance, block)

    fun hand(type: Material, dropChance: Double = DEFAULT_DROP_CHANCE, block: ItemBuilder.() -> Unit = {}) =
        slot(HAND, type, dropChance, block)

    fun offHand(type: Material, dropChance: Double = DEFAULT_DROP_CHANCE, block: ItemBuilder.() -> Unit = {}) =
        slot(OFF_HAND, type, dropChance, block)

    fun head(item: ItemStack, dropChance: Double = DEFAULT_DROP_CHANCE, block: ItemStack.() -> Unit = {}) =
        slot(HEAD, item, dropChance, block)

    fun chest(item: ItemStack, dropChance: Double = DEFAULT_DROP_CHANCE, block: ItemStack.() -> Unit = {}) =
        slot(CHEST, item, dropChance, block)

    fun legs(item: ItemStack, dropChance: Double = DEFAULT_DROP_CHANCE, block: ItemStack.() -> Unit = {}) =
        slot(LEGS, item, dropChance, block)

    fun feet(item: ItemStack, dropChance: Double = DEFAULT_DROP_CHANCE, block: ItemStack.() -> Unit = {}) =
        slot(FEET, item, dropChance, block)

    fun hand(item: ItemStack, dropChance: Double = DEFAULT_DROP_CHANCE, block: ItemStack.() -> Unit = {}) =
        slot(HAND, item, dropChance, block)

    fun offHand(item: ItemStack, dropChance: Double = DEFAULT_DROP_CHANCE, block: ItemStack.() -> Unit = {}) =
        slot(OFF_HAND, item, dropChance, block)

    /**
     * 将设置好的装备应用到指定的实体上
     *
     * @param entity 要应用装备的实体
     */
    fun apply(entity: LivingEntity) = map
        .filterValues { (item, chance) -> item != null && chance != null }
        .runCatching {
            map { (slot, pair) ->
                val item = pair.first
                val chance = pair.second
                entity.setEquipment(slot, item!!)
                entity.setDropChance(slot, chance!!.toFloat())
            }
        }.isSuccess

    private fun LivingEntity.setDropChance(slot: BukkitEquipment, value: Float) = equipment?.run {
        when (slot) {
            HAND -> itemInMainHandDropChance = value
            OFF_HAND -> itemInOffHandDropChance = value
            FEET -> bootsDropChance = value
            LEGS -> leggingsDropChance = value
            CHEST -> chestplateDropChance = value
            HEAD -> helmetDropChance = value
        }
    }

    private fun LivingEntity.getDropChance(slot: BukkitEquipment) = equipment?.run {
        when (slot) {
            HAND -> itemInMainHandDropChance
            OFF_HAND -> itemInOffHandDropChance
            FEET -> bootsDropChance
            LEGS -> leggingsDropChance
            CHEST -> chestplateDropChance
            HEAD -> helmetDropChance
        }
    }
}