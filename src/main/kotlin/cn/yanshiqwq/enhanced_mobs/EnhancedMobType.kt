package cn.yanshiqwq.enhanced_mobs

import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mob

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.EnhancedMobType
 *
 * @author yanshiqwq
 * @since 2024/8/19 下午 9:16
 */
/**
 * 怪物类型
 *
 * @property id 怪物类型的唯一标识符
 * @property type 生物类型
 * @see EntityType
 * @property block 用于在创建生物时配置其属性的配置块
 */
data class EnhancedMobType(
    val id: String,
    val type: EntityType,
    val block: Mob.(EnhancedMob) -> Unit
) {
    companion object {
        /**
         * 在指定位置生成一个本插件的怪物
         *
         * @param loc 生物生成的位置
         * @param level 生物的等级
         * @return 一个怪物类型实例，表示生成的生物
         * @throws NullPointerException 如果指定位置的世界不存在
         * @throws ClassCastException 如果要生成的实体不是生物类型
         */
        fun spawn(enhancedType: EnhancedMobType, loc: Location, level: Int): EnhancedMob {
            val world = loc.world ?: throw NullPointerException("The specified location's world is null: $loc")
            val entity = world.spawnEntity(loc, enhancedType.type) as? Mob
                         ?: throw ClassCastException("The provided type must be a subclass of Mob: $enhancedType.type")
            return EnhancedMob.build(entity, enhancedType, level)
        }
    }
}