package cn.yanshiqwq.enhanced_mobs.manager

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import java.util.*

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.manager.MobManager
 *
 * @author yanshiqwq
 * @since 2024/8/19 下午5:26
 */
object MobManager {
    private val mobs: HashSet<EnhancedMob> = hashSetOf()
    fun add(mob: EnhancedMob) = mobs.add(mob)
    fun remove(mob: EnhancedMob) = mobs.remove(mob)
    fun get(uuid: UUID) = mobs.find { it.entity.uniqueId == uuid }
    fun get(id: String) = mobs.filter { it.type.id == id }
    fun get(range: IntRange) = mobs.filter { it.level in range }
    fun get() = mobs
    fun has(uuid: UUID) = get(uuid) != null
}