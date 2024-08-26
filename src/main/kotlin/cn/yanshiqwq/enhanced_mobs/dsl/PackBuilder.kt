package cn.yanshiqwq.enhanced_mobs.dsl

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.EnhancedMobType
import cn.yanshiqwq.enhanced_mobs.Pack
import cn.yanshiqwq.enhanced_mobs.manager.PackManager
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mob

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.PackBuilder
 *
 * @author yanshiqwq
 * @since 2024/8/20 下午4:41
 */

/**
 * 用于构建数据包实例的构建器类
 *
 * @param id 该包的唯一标识符
 * @param description 该包的描述信息
 */
class PackBuilder(
    private val id: String,
    private val description: String
) {
    companion object {
        /**
         * 创建并注册一个新的数据包实例
         *
         * @param id 该包的唯一标识符
         * @param description 该包的描述信息
         * @param block 用于配置 `PackBuilder` 的配置块
         */
        fun pack(id: String, description: String, block: PackBuilder.() -> Unit) {
            val builder = PackBuilder(id, description)
            block.invoke(builder)
            val pack = builder.build()
            PackManager.register(pack)
        }
    }

    private val types: HashSet<EnhancedMobType> = hashSetOf()

    /**
     * 构建并返回一个数据包实例
     *
     * @return 配置好的实例
     * @throws NullPointerException 如果包的 id 为空，则抛出异常
     */
    fun build(): Pack {
        if (id == "") throw NullPointerException("Pack id must not be empty")
        return Pack(id, description, types)
    }

    /**
     * 添加一个怪物类型到包中
     *
     * @param type 要添加的怪物类型
     * @param id 该怪物类型的唯一标识符
     * @param block 用于配置怪物实例
     */
    fun type(type: EntityType, id: String, block: Mob.(EnhancedMob) -> Unit) =
        types.add(EnhancedMobType(id, type, block))
}
