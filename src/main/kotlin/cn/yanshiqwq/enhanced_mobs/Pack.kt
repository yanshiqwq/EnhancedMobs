package cn.yanshiqwq.enhanced_mobs

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.Pack
 *
 * @author yanshiqwq
 * @since 2024/8/21 下午9:06
 */
data class Pack(
    val id: String,
    val description: String,
    val types: HashSet<EnhancedMobType> = hashSetOf()
)