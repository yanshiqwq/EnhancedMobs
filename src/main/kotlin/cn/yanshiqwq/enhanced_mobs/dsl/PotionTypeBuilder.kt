package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.PotionTypeBuilder
 *
 * @author yanshiqwq
 * @since 2024/8/20 下午4:04
 */
/**
 * 用于构建状态效果组的构建器类
 *
 * @param effects 要管理的药水效果列表
 */
data class PotionTypeBuilder(private val effects: MutableList<PotionEffect>) {
    /**
     * 为指定的药水效果类型配置效果。
     *
     * @param type 状态效果的类型
     * @param block 用于配置状态效果的 DSL 函数
     */
    fun effect(type: PotionEffectType, block: PotionEffectBuilder.() -> Unit) {
        val index = effects.indexOfLast { it.type == type }
        if (index != -1) {
            val builder = PotionEffectBuilder(effects[index])
            block(builder)
            effects[index] = builder.build()
        }
    }

    /**
     * 构建并返回药水效果列表
     *
     * @return 配置好的状态效果列表
     */
    fun build() = effects
}
