package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.entity.Mob
import taboolib.common.platform.function.info
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
open class TimerBuilder(
    private val period: Long,
    private val delay: Long = 0,
    private val now: Boolean = false,
    private val async: Boolean = false
) : ExecutionHandler<PlatformExecutor.PlatformTask>, TimerChecker() {
    override var executor: PlatformExecutor.PlatformTask.() -> Unit = {}
    override var cooldown: CooldownTimer? = null
    private lateinit var task: PlatformExecutor.PlatformTask
    fun cancel() = task.cancel()

    /**
     * 构建并提交任务至 TabooLib
     *
     * @return 提交的任务
     */
    fun build(mob: Mob) {
        task = submit(now, async, delay, period) {
            if (checkIfDead(mob)) {
                info("Cancelling task for dead mob ${mob.uniqueId}")
                cancel()
                return@submit
            }
            if (!checkCondition()) {
                info("Condition check failed for mob ${mob.uniqueId}")
                info(cooldown)
                return@submit
            }
            executor.invoke(this)
        }
    }
}