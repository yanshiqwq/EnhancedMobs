package cn.yanshiqwq.enhanced_mobs.dsl

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.IExecution
 *
 * @author yanshiqwq
 * @since 2024/8/21 下午7:13
 */

/**
 * 有上下文的可执行操作接口
 *
 * @param T 操作的上下文类型
 */
interface ExecutionHandler<T> {
    /**
     * 要执行的操作
     *
     * @see ListenerBuilder
     * @see TimerBuilder
     */
    var executor: T.() -> Unit
    
    /**
     * 设定执行的操作
     *
     * @param block 要执行的操作
     */
    fun execute(block: T.() -> Unit) { executor = block }
}