package cn.yanshiqwq.enhanced_mobs.dsl

import cn.yanshiqwq.enhanced_mobs.EnhancedMobType
import cn.yanshiqwq.enhanced_mobs.Pack
import org.bukkit.entity.EntityType
import java.util.*

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.PackBuilder
 *
 * @author yanshiqwq
 * @since 2024/8/20 下午4:41
 */
class PackBuilder {
    companion object {
        fun pack(block: PackBuilder.() -> Unit): Pack {
            val builder = PackBuilder()
            block.invoke(builder)
            return builder.build()
        }
    }
    private var id: String = ""
    private var description: String = ""
    private val types: ArrayList<EnhancedMobType> = arrayListOf()

    fun id(id: String) { this.id = id }
    fun description(description: String) { this.description = description }
    fun build(): Pack {
        if (id == "") throw NullPointerException("Pack id must not be empty")
        return Pack(id, description, types)
    }
    fun type(type: EntityType, id: String, block: MobBuilder.() -> Unit) {
        val builder = MobTypeBuilder(id, type)
        builder.func { block() }
        types.plus(builder.build())
    }
}