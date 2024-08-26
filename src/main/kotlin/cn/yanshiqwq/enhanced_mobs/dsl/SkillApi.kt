package cn.yanshiqwq.enhanced_mobs.dsl

import cn.yanshiqwq.enhanced_mobs.dsl.GenericApi.delay
import cn.yanshiqwq.enhanced_mobs.dsl.GenericApi.onDeath
import org.bukkit.Material
import org.bukkit.entity.Mob
import taboolib.common.platform.service.PlatformExecutor

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
     * @param delay 延迟时间，单位为刻，在该时间后移除方块
     * @param type 要放置的方块的类型
     * @param removeOnDeath 是否在实体死亡时移除方块
     */
    fun Mob.placeBlock(delay: Long = 0, type: Material = Material.AIR, removeOnDeath: Boolean = true, block: BlockPlacer.() -> Unit) {
        val placer = BlockPlacer(this, delay, type, removeOnDeath)
        block.invoke(placer)
        placer.run()
    }
    class BlockPlacer(
        private val mob: Mob,
        private val delay: Long,
        private val type: Material,
        private val removeOnDeath: Boolean
    ) {
        /**
         * 设置放置方块后的回调函数
         */
        fun onPlace(block: () -> Unit) { onPlace = block }
        private var onPlace: () -> Unit = {}


        /**
         * 设置移除方块后的回调函数
         */
        fun onRemove(block: PlatformExecutor.PlatformTask.() -> Unit) { onRemove = block }
        private var onRemove: PlatformExecutor.PlatformTask.() -> Unit = {}

        /**
         * 设置方块移除失败后的回调函数
         */
        fun onFailedRemove(block: PlatformExecutor.PlatformTask.() -> Unit) { onFailedRemove = block }
        private var onFailedRemove: PlatformExecutor.PlatformTask.() -> Unit = {}
        
        fun run() {
            val target = mob.target ?: return
            val block = target.location.block
            block.type = type
            onPlace.invoke()
            println("Block placed: ${block.type} ${block.location}")
            mob.delay(delay) {
                execute {
                    if (block.type != type) {
                        onFailedRemove.invoke(this)
                        println("Failed to remove block: ${block.type} ${block.location}")
                        cancel()
                        return@execute
                    }
                    onRemove.invoke(this)
                    block.type = Material.AIR
                    println("Block removed: ${block.type} ${block.location}")
                }
            }
            
            if (removeOnDeath) {
                mob.onDeath {
                    judgeAll(
                        block.type == type
                    )
                    execute {
                        block.type = Material.AIR
                        println("Block removed on death: ${block.type} ${block.location}")
                    }
                }
            }
        }
    }
}