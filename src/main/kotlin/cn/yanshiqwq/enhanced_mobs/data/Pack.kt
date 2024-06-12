package cn.yanshiqwq.enhanced_mobs.data

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.Main

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.data.Pack
 *
 * @author yanshiqwq
 * @since 2024/6/11 00:19
 */
data class Pack(val id: String, val typeMap: Map<String, EnhancedMob.() -> Unit>) {
    fun implement(id: String, function: EnhancedMob.() -> Unit): EnhancedMob.() -> Unit {
        return {
            typeMap[id]?.let { it(this) } ?: Main.logger.warning("${Main.prefix} Cannot implement type $id.")
            function(this)
        }
    }
}