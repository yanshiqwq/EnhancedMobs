package cn.yanshiqwq.enhanced_mobs.api

import cn.yanshiqwq.enhanced_mobs.Pack

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.api.IPackApi
 *
 * @author yanshiqwq
 * @since 2024/8/21 下午9:04
 */
interface IPluginApi {
    fun getPack(id: String): Pack?
    fun registerPack(pack: Pack)
}