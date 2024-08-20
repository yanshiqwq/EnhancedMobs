package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.PotionEffectBuilder
 *
 * @author yanshiqwq
 * @since 2024/8/20 下午3:39
 */
data class PotionEffectBuilder(
    var type: PotionEffectType,
    var duration: Int = 20,
    var amplifier: Int = 0,
    var ambient: Boolean = false,
    var particles: Boolean = true,
    var icon: Boolean = true
) {
    constructor(effect: PotionEffect) : this(effect.type, effect.duration, effect.amplifier, effect.isAmbient, effect.hasParticles(), effect.hasIcon())
    fun setInfinite(): PotionEffectBuilder {
        duration = PotionEffect.INFINITE_DURATION
        return this
    }
    fun build() = PotionEffect(type, duration, amplifier, ambient, particles, icon)
}