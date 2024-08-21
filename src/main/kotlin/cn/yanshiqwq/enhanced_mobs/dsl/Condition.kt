package cn.yanshiqwq.enhanced_mobs.dsl

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.Condition
 *
 * @author yanshiqwq
 * @since 2024/8/21 下午6:52
 */
abstract class Condition<T>: ICooldown() {
    var condition: T.() -> Boolean = { true }
    fun check(context: T) = condition.invoke(context)
}
