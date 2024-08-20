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
data class PotionTypeBuilder(private val effects: MutableList<PotionEffect>) {
    fun effect(type: PotionEffectType, block: PotionEffectBuilder.() -> Unit) {
        val index = effects.indexOfLast { it.type == type }
        if (index != -1) {
            val builder = PotionEffectBuilder(type)
            block(builder)
            effects[index] = builder.build()
        }
    }
    fun build() = effects.toList()
}