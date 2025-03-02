package cn.yanshiqwq.enhanced_mobs.manager

import cn.yanshiqwq.enhanced_mobs.LevelCurve
import org.bukkit.attribute.Attributable

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.manager.LevelCurveManager
 *
 * @author yanshiqwq
 * @since 2024/10/2 下午6:15
 */
object LevelCurveManager: HashMapManager<String, LevelCurve>() {
    fun Attributable.applyLevelCurve(id: String, level: Int) =
        entries.filterKeys { it == id }
            .firstNotNullOf { it.value }
            .applyTo(this, level)
}