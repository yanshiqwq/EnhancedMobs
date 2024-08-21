package cn.yanshiqwq.enhanced_mobs.dsl

import cn.yanshiqwq.enhanced_mobs.EnhancedMobType
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mob

typealias Skill = MobBuilder.(Mob) -> Unit
class MobTypeBuilder(
    private val id: String,
    private val type: EntityType
) {
    private val blocks = arrayListOf<Skill>()

    fun func(block: Skill) = blocks.add(block)

    fun build() = EnhancedMobType(id, type) {
        blocks.forEach { block ->
            val builder = MobBuilder(it)
            block.invoke(builder, it)
        }
    }
}