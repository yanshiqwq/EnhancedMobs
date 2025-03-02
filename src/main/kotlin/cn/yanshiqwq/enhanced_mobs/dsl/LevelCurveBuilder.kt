package cn.yanshiqwq.enhanced_mobs.dsl

import cn.yanshiqwq.enhanced_mobs.LevelCurve
import org.bukkit.attribute.Attribute

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.LevelCurveBuilder
 *
 * @author yanshiqwq
 * @since 2024/10/2 下午7:16
 */
class LevelCurveBuilder(
    private val id: String
) {
    companion object {
        data class ExpressionContext(val base: Double, val level: Int)
        fun levelCurve(id: String, block: LevelCurveBuilder.() -> Unit): Pair<String, LevelCurve> {
            val builder = LevelCurveBuilder(id)
            block.invoke(builder)
            return builder.build()
        }
    }
    private val map = hashMapOf<Attribute, Expression>()
    fun maxHealth(expression: Expression) = map.put(Attribute.MAX_HEALTH, expression)
    fun followRange(expression: Expression) = map.put(Attribute.FOLLOW_RANGE, expression)
    fun knockbackResistance(expression: Expression) = map.put(Attribute.KNOCKBACK_RESISTANCE, expression)
    fun movementSpeed(expression: Expression) = map.put(Attribute.MOVEMENT_SPEED, expression)
    fun flyingSpeed(expression: Expression) = map.put(Attribute.FLYING_SPEED, expression)
    fun attackDamage(expression: Expression) = map.put(Attribute.ATTACK_DAMAGE, expression)
    fun attackKnockback(expression: Expression) = map.put(Attribute.ATTACK_KNOCKBACK, expression)
    fun attackSpeed(expression: Expression) = map.put(Attribute.ATTACK_SPEED, expression)
    fun armor(expression: Expression) = map.put(Attribute.ARMOR, expression)
    fun armorToughness(expression: Expression) = map.put(Attribute.ARMOR_TOUGHNESS, expression)
    fun luck(expression: Expression) = map.put(Attribute.LUCK, expression)
    fun maxAbsorption(expression: Expression) = map.put(Attribute.MAX_ABSORPTION, expression)
    fun spawnReinforcements(expression: Expression) = map.put(Attribute.SPAWN_REINFORCEMENTS, expression)
    
    fun build(): Pair<String, LevelCurve> {
        return id to LevelCurve(map)
    }
}
typealias Expression = LevelCurveBuilder.Companion.ExpressionContext.() -> Double