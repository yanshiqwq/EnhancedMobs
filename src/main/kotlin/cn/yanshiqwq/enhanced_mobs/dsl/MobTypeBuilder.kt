package cn.yanshiqwq.enhanced_mobs.dsl

import cn.yanshiqwq.enhanced_mobs.EnhancedMobType
import cn.yanshiqwq.enhanced_mobs.api.MobApi
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mob

typealias Skill = MobApi.(Mob) -> Unit
class MobTypeBuilder(
    private val id: String,
    private val type: EntityType
) {
    private val blocks = arrayListOf<Skill>()

    fun func(block: Skill) = blocks.add(block)

    fun build() = EnhancedMobType(id, type) {
        blocks.forEach { block ->
            val builder = MobApi(it)
            block.invoke(builder, it)
        }
    }
}