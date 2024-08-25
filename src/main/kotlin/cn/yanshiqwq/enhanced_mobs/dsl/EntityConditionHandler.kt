package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.entity.LivingEntity

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.Condition
 *
 * @author yanshiqwq
 * @since 2024/8/21 下午6:52
 */

interface EntityConditionHandler: ConditionHandler, CooldownHandler {
    /**
     * 是否在实体死亡后仍然执行监听器
     */
    var runAfterEntityDead: Boolean
    
    fun checkIfDead(entity: LivingEntity) = !runAfterEntityDead && entity.isDead // 检查死亡
    fun judgeAll(vararg conditions: Boolean) = judge(conditions.all { it })
    fun judgeAny(vararg conditions: Boolean) = judge(conditions.any { it })
}
