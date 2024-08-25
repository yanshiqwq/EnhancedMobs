package cn.yanshiqwq.enhanced_mobs.dsl

class DelayBuilder(
    delay: Long,
    now: Boolean = false,
    async: Boolean = false
): TimerBuilder(0, delay, now, async)
