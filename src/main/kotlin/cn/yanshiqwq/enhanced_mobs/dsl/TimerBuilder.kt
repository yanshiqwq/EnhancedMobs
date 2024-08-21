package cn.yanshiqwq.enhanced_mobs.dsl

import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.TimerBuilder
 *
 * @author yanshiqwq
 * @since 2024/8/21 下午7:25
 */
class TimerBuilder : IExecution<PlatformExecutor.PlatformTask>, Condition<Unit>() {
    var now: Boolean = false
    var async: Boolean = false
    var delay: Long = 0
    var period: Long = 20
    override var executor: PlatformExecutor.PlatformTask.() -> Unit = {}
    fun build() = submit(now, async, delay, period, executor)
}