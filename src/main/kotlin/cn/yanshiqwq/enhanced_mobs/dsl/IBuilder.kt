package cn.yanshiqwq.enhanced_mobs.dsl

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.IBuilder
 *
 * @author yanshiqwq
 * @since 2024/8/19 下午11:26
 */
interface IBuilder<K, V> {
    fun build(): Map<K, V>
}