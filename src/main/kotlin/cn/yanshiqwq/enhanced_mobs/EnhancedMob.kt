package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.dsl.MobBuilder

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.EnhancedMobType
 *
 * @author yanshiqwq
 * @since 2024/8/19 下午9:01
 */
object EnhancedMob {
    fun type(id: String, block: MobBuilder.() -> Unit) {
        val builder = MobBuilder(id)
        block.invoke(builder)
    }
}