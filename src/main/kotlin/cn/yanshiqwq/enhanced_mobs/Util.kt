package cn.yanshiqwq.enhanced_mobs

import kotlin.random.Random

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.EnhancedMobType
 *
 * @author yanshiqwq
 * @since 2024/8/19 下午9:01
 */
object Util {
    fun chance(chance: Double, random: Random = Random) = (random.nextDouble() > chance)
    fun chance(chance: Double, seed: Long, random: Random = Random(seed)) = chance(chance, random)
    fun chance(chance: Double, seed: Int, random: Random = Random(seed)) = chance(chance, random)
}