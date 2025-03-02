package cn.yanshiqwq.enhanced_mobs.manager

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import java.util.*

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.manager.MobManager
 *
 * @author yanshiqwq
 * @since 2024/8/19 下午 5:26
 */
object MobManager: HashSetManager<EnhancedMob>() {
    fun get(uuid: UUID) = entries.find { it.entity?.uniqueId == uuid }
    fun get(range: IntRange) = entries.filter { it.level in range }
    fun has(uuid: UUID) = get(uuid) != null
}