package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.entity.LivingEntity

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.Condition
 *
 * @author yanshiqwq
 * @since 2024/8/21 下午 6:52
 */
/**
 * 用于检查实体条件的接口
 *
 * @param T 上下文的类型
 */
interface EntityChecker<T> {
    /**
     * 是否在实体死亡后继续运行
     */
    var runAfterEntityDead: Boolean
    
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
}