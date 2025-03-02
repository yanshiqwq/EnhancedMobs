package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.entity.LivingEntity
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.DelayBuilder
 *
 * @author yanshiqwq
 * @since 2024/8/24 下午 3:39
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
): EventBuilder() {
    companion object {
        /**
         * 创建一个 TabooLib 计时器任务
         *
         * @see TimerBuilder
         * @return 返回创建的 TabooLib 计时器任务
         */
        fun LivingEntity.onTimer(
            period: Long,
            cooldown: Long? = null,
            block: PlatformExecutor.PlatformTask.() -> Unit
        ): PlatformExecutor.PlatformTask {
            val builder = TimerBuilder(period)
            if (cooldown != null) builder.setCooldown(cooldown, world)
            return builder.build(this, block)
        }
    }
    
    /**
     * 构建并提交任务
     *
     * @param entity 任务关联的实体
     * @return TabooLib 任务
     */
    fun build(entity: LivingEntity, block: PlatformExecutor.PlatformTask.() -> Unit): PlatformExecutor.PlatformTask {
        val taskBlock: PlatformExecutor.PlatformTask.() -> Unit = {
            checkAndExecute(entity, this, block) { cancel() }
        }
        return submit(now, async, period, delay, taskBlock)
    }
}