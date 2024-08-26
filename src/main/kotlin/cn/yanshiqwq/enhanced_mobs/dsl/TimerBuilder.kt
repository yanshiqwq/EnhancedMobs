package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.entity.LivingEntity
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.DelayBuilder
 *
 * @author yanshiqwq
 * @since 2024/8/24 下午3:39
 *
 */

/**
 * 用于构建和执行定时任务
 *
 * @param period 任务的周期，单位为刻
 * @param delay 任务的初始延迟时间，单位为刻
 * @param now 是否立即执行任务
 * @param async 是否异步执行任务
 */
open class TimerBuilder(
    private val period: Long,
    private val delay: Long = 0,
    private val now: Boolean = false,
    private val async: Boolean = false
) : EventBuilder<PlatformExecutor.PlatformTask>() {
    /**
     * 任务执行的具体逻辑
     */
    override var executor: PlatformExecutor.PlatformTask.() -> Unit = {}
    
    /**
     * 任务的冷却计时器
     */
    override var cooldown: CooldownTimer? = null
    
    /**
     * 构建并提交任务
     *
     * @param entity 任务关联的实体
     * @return TabooLib 任务
     */
    fun build(entity: LivingEntity) = submit(now, async, delay, period) {
        checkAndExecute(entity, this, executor) {
            cancel()
        }
    }
}