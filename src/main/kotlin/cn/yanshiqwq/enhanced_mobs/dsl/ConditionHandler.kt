package cn.yanshiqwq.enhanced_mobs.dsl

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.Condition
 *
 * @author yanshiqwq
 * @since 2024/8/21 下午6:52
 */

/**
 * 能够在特定上下文中进行检查的条件类
 *
 * @param T 作为条件检查时的上下文类型
 */
interface ConditionHandler<T>: CooldownHandler {
    /**
     * 条件函数，定义了在上下文对象上的条件检查逻辑
     * 默认条件始终为 `true`。
     */
    var condition: T.() -> Boolean

    fun judge(block: T.() -> Boolean) { this.condition = block }

    // 无需上下文的判断条件
    fun judge(condition: Boolean) { this.condition = { condition } }
    fun judgeAll(vararg conditions: Boolean) = judge(conditions.all { it })
    fun judgeAny(vararg conditions: Boolean) = judge(conditions.all { it })
    /**
     * 检查在给定上下文中的条件是否成立
     *
     * @param context 用于检查条件的上下文对象
     * @return 条件是否在上下文中成立
     */
    fun check(context: T) = condition.invoke(context)
}
