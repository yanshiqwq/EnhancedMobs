package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.dsl.AttributeBuilder.Companion.attribute
import cn.yanshiqwq.enhanced_mobs.dsl.AttributeBuilder.Companion.modifier
import cn.yanshiqwq.enhanced_mobs.dsl.AttributeBuilder.Companion.removeModifier
import cn.yanshiqwq.enhanced_mobs.dsl.Expression
import cn.yanshiqwq.enhanced_mobs.dsl.LevelCurveBuilder
import org.bukkit.attribute.Attributable
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier.Operation.MULTIPLY_SCALAR_1

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.LevelCurve
 *
 * @author yanshiqwq
 * @since 2024/10/2 下午6:13
 */
data class LevelCurve(val map: Map<Attribute, Expression>) {
    companion object {
        private val MODIFIER_KEY = Utils.createNamespacedKey("level_boost")
        private val LEADER_ZOMBIE_MODIFIER_NAME = arrayOf("minecraft:leader_zombie_bonus", "Leader zombie bonus")
    }
    fun applyTo(entity: Attributable, level: Int) =
        map.forEach { (attribute, expression) ->
            // 添加属性修饰器
            entity.attribute(attribute) {
                val context = LevelCurveBuilder.Companion.ExpressionContext(baseValue, level)
                val finalValue = expression.invoke(context)
                val multiplier = (finalValue / baseValue) - 1
                modifier(MULTIPLY_SCALAR_1, multiplier, MODIFIER_KEY)
            }
            
            // 领头僵尸
            // @see https://zh.minecraft.wiki/w/%E5%B1%9E%E6%80%A7/%E5%83%B5%E5%B0%B8%E5%A2%9E%E6%8F%B4
            entity.attribute(Attribute.MAX_HEALTH) {
                removeModifier { it.name in LEADER_ZOMBIE_MODIFIER_NAME }
            }
        }
}