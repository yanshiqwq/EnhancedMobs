package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.entity.LivingEntity

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.Condition
 *
 * @author yanshiqwq
 * @since 2024/8/21 下午6:52
 */

/**
 * 用于处理实体条件
 *
 * @param T 上下文的类型
 */
abstract class EntityConditionHandler<T> {
    /**
     * 是否在实体死亡后继续运行
     */
    var runAfterEntityDead: Boolean = false
    
    /**
     * 存储条件
     */
    val conditions = hashSetOf<(T) -> Boolean>()
    
    /**
     * 添加单个条件
     *
     * @param condition 要添加的条件
     * @return 是否成功添加条件
     */
    fun judge(condition: (T) -> Boolean) = conditions.add(condition)
    
    /**
     * 判断所有条件是否都满足
     *
     * @param condition 要判断的条件数组
     */
    fun judgeAll(vararg condition: Boolean) = judge { condition.all { it } }
    
    /**
     * 判断任意条件是否满足
     *
     * @param condition 要判断的条件数组
     */
    fun judgeAny(vararg condition: Boolean) = judge { condition.any { it } }
    
    /**
     * 检查实体是否死亡
     *
     * @param entity 要检查的实体
     * @return 实体是否死亡
     */
    fun checkIfDead(entity: LivingEntity) = !runAfterEntityDead && entity.isDead
    
    /**
     * 检查实体是否有效
     *
     * @param entity 要检查的实体
     * @return 实体是否有效
     */
    fun checkIfValid(entity: LivingEntity) = entity.isValid
    
    /**
     * 检查条件是否满足
     *
     * @param context 上下文对象
     * @return 所有条件是否都满足
     */
    fun checkCondition(context: T): Boolean = conditions.all { it.invoke(context) }
}
