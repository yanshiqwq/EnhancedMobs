package cn.yanshiqwq.enhanced_mobs.dsl

/**
 * 用于创建延迟任务的构建器
 *
 * @param delay 延迟时间，单位为刻
 * @param now 是否立即执行
 * @param async 是否异步执行
 */
class DelayBuilder(
    delay: Long,
    now: Boolean = false,
    async: Boolean = false
) : TimerBuilder(0, delay, now, async)