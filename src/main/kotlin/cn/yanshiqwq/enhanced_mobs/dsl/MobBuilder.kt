package cn.yanshiqwq.enhanced_mobs.dsl

import cn.yanshiqwq.enhanced_mobs.EnhancedMobType
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mob

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.MobBuilder
 *
 * @author yanshiqwq
 * @since 2024/8/19 下午6:45
 */

typealias Skill = SkillApi.(Mob) -> Unit
class MobBuilder(
    private val id: String,
    private val type: EntityType = EntityType.ZOMBIE
) {
    private val blocks = arrayListOf<Skill>()

    fun name(name: String) = blocks.add {
        it.customName = name
    }

    fun func(block: Skill) = blocks.add(block)

    fun base(block: BaseAttributeBuilder.() -> Unit) = BaseAttributeBuilder().apply(block).build()
        .filterValues { it != null }
        .forEach { (attribute, value) ->
            blocks.add {
                it.getAttribute(attribute)?.baseValue = value!!
            }
        }

    fun equip(block: EquipmentBuilder.() -> Unit) = EquipmentBuilder().apply(block).build()
        .filterValues { it != null }
        .forEach { (slot, item) ->
            blocks.add {
                it.equipment?.setItem(slot, item)
            }
        }

    fun build() = EnhancedMobType(id, type) {
        blocks.forEach { block ->
            val builder = SkillApi(it)
            block.invoke(builder, it)
        }
    }
}
