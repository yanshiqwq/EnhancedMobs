package cn.yanshiqwq.enhanced_mobs.dsl

import taboolib.common.platform.function.submit

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.TimerBuilder
 *
 * @author yanshiqwq
 * @since 2024/8/21 下午7:25
 */

/**
 * 用于构建定时任务的构建器类
 *
 * @property now 是否立即执行任务
 * @property async 是否异步执行任务
 * @property delay 任务的初始延迟时间
 * @property period 任务的执行周期，单位为刻
 * @property execute 定义任务的执行逻辑
 */
class TimerBuilder(private val period: Long): DelayBuilder() {
    override fun build() = submit(now, async, delay, period, executor)
}
