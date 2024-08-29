package cn.yanshiqwq.enhanced_mobs.dsl

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.ContextConditionHandler
 *
 * @author yanshiqwq
 * @since 2024/8/28 下午 1:16
 */
/**
 * 有上下文的条件接口
 * @param T 上下文的类型
 */
interface ConditionHandler<T> {
    /**
     * 存储条件
     */
    val conditions: MutableCollection<T.() -> Boolean>
    
    /**
     * 判断所有条件是否都满足
     *
     * @param condition 要判断的条件数组
     */
    fun allOf(vararg condition: Boolean) = condition.all { it }
    
    /**
     * 判断任意条件是否满足
     *
     * @param condition 要判断的条件数组
     */
    fun anyOf(vararg condition: Boolean) = condition.any { it }
    
    /**
     * 检查条件是否满足
     *
     * @param context 上下文对象
     * @return 所有条件是否都满足
     */
    fun checkCondition(context: T): Boolean = conditions.all { it.invoke(context) }
}