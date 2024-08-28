package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.PotionEffectBuilder
 *
 * @author yanshiqwq
 * @since 2024/8/20 下午 3:39
 */
/**
 * 用于构建状态效果
 *
 * @param type 状态效果的类型
 * @param duration 状态效果的持续时间，单位为刻
 * @param amplifier 状态效果的增幅等级
 * @param ambient 是否为信标效果
 * @param particles 是否显示粒子
 * @param icon 是否显示效果图标
 */
data class PotionEffectBuilder(
    var type: PotionEffectType,
    var duration: Int = 20,
    var amplifier: Int = 0,
    var ambient: Boolean = false,
    var particles: Boolean = true,
    var icon: Boolean = true
) {
    /**
     * 使用状态效果构建
     *
     * @param effect 要用于构建器的药水效果实例
     */
    constructor(effect: PotionEffect): this(
        effect.type,
        effect.duration,
        effect.amplifier,
        effect.isAmbient,
        effect.hasParticles(),
        effect.hasIcon()
    )
    
    /**
     * 将状态效果的持续时间设置为无限
     * @see PotionEffect.INFINITE_DURATION
     * @see PotionEffect.isInfinite
     *
     * @return 当前构建器实例，以便链式调用
     */
    fun setInfinite(): PotionEffectBuilder {
        duration = PotionEffect.INFINITE_DURATION
        return this
    }
    
    /**
     * 构建并返回一个状态效果实例
     *
     * @return 配置好的状态效果实例
     */
    fun build() = PotionEffect(type, duration, amplifier, ambient, particles, icon)
}