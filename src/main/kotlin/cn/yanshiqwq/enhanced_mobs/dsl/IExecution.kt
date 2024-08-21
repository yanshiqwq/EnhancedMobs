package cn.yanshiqwq.enhanced_mobs.dsl

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.IExecution
 *
 * @author yanshiqwq
 * @since 2024/8/21 下午7:13
 */
interface IExecution<T> {
    var executor: T.() -> Unit
}