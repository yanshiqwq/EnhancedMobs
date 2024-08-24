package cn.yanshiqwq.enhanced_mobs.dsl

import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.DelayBuilder
 *
 * @author yanshiqwq
 * @since 2024/8/24 下午3:39
 * @param now 是否立即执行任务
 * @param async 是否异步执行任务
 * @param delay 任务的初始延迟时间，单位为刻
 */
open class DelayBuilder(
    val delay: Long = 0,
    val now: Boolean = false,
    val async: Boolean = false
) : ExecutionHandler<PlatformExecutor.PlatformTask>, ConditionHandler<Unit> {
    override var executor: PlatformExecutor.PlatformTask.() -> Unit = {}
    override var condition: Unit.() -> Boolean = { true }
    override var cooldown: CooldownTimer? = null

    /**
     * 构建并提交任务至 TabooLib
     *
     * @return 提交的任务
     */
    open fun build() = submit(now, async, delay, 0, executor)
}