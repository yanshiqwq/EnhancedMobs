package cn.yanshiqwq.enhanced_mobs

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.Pack
 *
 * @author yanshiqwq
 * @since 2024/8/21 下午 9:06
 */
/**
 * 数据包
 *
 * @property id 数据包的唯一标识符
 * @property description 数据包的描述信息
 * @property types 包含在数据包中的怪物类型集合
 */
data class Pack(
    val id: String,
    val description: String,
    val types: HashSet<EnhancedMobType> = hashSetOf()
)