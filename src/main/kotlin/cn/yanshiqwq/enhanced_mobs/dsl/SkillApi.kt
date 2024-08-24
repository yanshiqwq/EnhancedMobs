package cn.yanshiqwq.enhanced_mobs.dsl

import cn.yanshiqwq.enhanced_mobs.dsl.GenericApi.delay
import cn.yanshiqwq.enhanced_mobs.dsl.GenericApi.onDeath
import org.bukkit.Material
import org.bukkit.entity.Mob

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.SkillApi
 *
 * @author yanshiqwq
 * @since 2024/8/24 下午6:23
 */
object SkillApi {
    /**
     * 在目标的位置放置一个方块，并在一定时间后或实体死亡时移除该方块
     *
     * @param delay 延迟时间，单位为 `Tick`，在该时间后移除方块
     * @param type 要放置的方块的类型
     * @param removeOnDeath 是否在实体死亡时移除方块
     */
    fun Mob.placeBlock(delay: Long = 0, type: Material = Material.AIR, removeOnDeath: Boolean = true, blockPlacer: BlockPlacer.() -> Unit) {
        val placer = BlockPlacer(this, delay, type, removeOnDeath)
        blockPlacer(placer)
        placer.execute()
    }
    class BlockPlacer(
        private val mob: Mob,
        private val delay: Long,
        private val type: Material,
        private val removeOnDeath: Boolean
    ) {
        /**
         * 修改放置方块后的回调函数
         */
        fun onPlace(block: () -> Unit) { onPlace = block }
        private var onPlace: (() -> Unit) = {}


        /**
         * 修改移除方块后的回调函数
         */
        fun onRemove(block: () -> Unit) { onRemove = block }
        private var onRemove: (() -> Unit) = {}

        /**
         * 修改方块移除失败后的回调函数
         */
        fun onFailedRemove(block: () -> Unit) { onFailedRemove = block }
        private var onFailedRemove: (() -> Unit) = {}


        fun execute() {
            val target = mob.target ?: return
            val block = target.location.block
            block.type = type
            onPlace.invoke()

            delay(delay) {
                if (block.type != type) {
                    onFailedRemove.invoke()
                    return@delay
                }
                onRemove.invoke()
                block.type = Material.AIR
            }

            if (removeOnDeath) {
                mob.onDeath {
                    if (block.type != type) return@onDeath
                    block.type = Material.AIR
                }
            }
        }
    }
}