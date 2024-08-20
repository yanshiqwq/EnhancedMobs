package cn.yanshiqwq.enhanced_mobs.dsl

import cn.yanshiqwq.enhanced_mobs.EnhancedMobType
import cn.yanshiqwq.enhanced_mobs.api.MobApi
import org.bukkit.entity.EntityType
import java.util.*

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.PackBuilder
 *
 * @author yanshiqwq
 * @since 2024/8/20 下午4:41
 */
abstract class PackBuilder {
    abstract val id: String
    abstract val description: String
    private val types: ArrayList<EnhancedMobType> = arrayListOf()

    fun build(): List<EnhancedMobType> = types
    fun type(type: EntityType, id: String, block: MobApi.() -> Unit) {
        val builder = MobTypeBuilder(id, type)
        builder.func { block() }
        types.plus(builder.build())
    }
}