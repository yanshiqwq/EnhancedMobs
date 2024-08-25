package cn.yanshiqwq.enhanced_mobs.dsl

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.ConditionHandler
 *
 * @author yanshiqwq
 * @since 2024/8/26 上午12:01
 */

interface ConditionHandler {
    /**
     * 条件函数，定义了在上下文对象上的条件检查逻辑
     */
    val condition: Any
    fun judge(condition: Boolean)
}