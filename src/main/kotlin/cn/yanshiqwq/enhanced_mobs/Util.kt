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
//    fun <T, R> wrap(original: (T) -> R, wrapper: (T) -> T): (T) -> R {
//        return { input: T ->
//            // 在调用原始函数之前对输入进行包装
//            val wrapped = wrapper(input)
//            // 调用原始函数
//            original(wrapped)
//        }
//    }
    fun chance(chance: Double, random: Random = Random) = (random.nextDouble() > chance)
    fun chance(chance: Double, seed: Long, random: Random = Random(seed)) = chance(chance, random)
    fun chance(chance: Double, seed: Int, random: Random = Random(seed)) = chance(chance, random)
}