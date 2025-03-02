package cn.yanshiqwq.enhanced_mobs.dsl

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.Utils.canCrit
import cn.yanshiqwq.enhanced_mobs.Utils.isAxe
import cn.yanshiqwq.enhanced_mobs.Utils.isPickaxe
import cn.yanshiqwq.enhanced_mobs.Utils.isShovel
import cn.yanshiqwq.enhanced_mobs.Utils.isSword
import cn.yanshiqwq.enhanced_mobs.Utils.look
import cn.yanshiqwq.enhanced_mobs.Utils.onDamage
import cn.yanshiqwq.enhanced_mobs.Utils.onDeath
import cn.yanshiqwq.enhanced_mobs.Utils.sound
import cn.yanshiqwq.enhanced_mobs.Utils.stun
import cn.yanshiqwq.enhanced_mobs.Utils.unstun
import cn.yanshiqwq.enhanced_mobs.dsl.SkillApi.ToughnessMechanism.ToughnessReducer.Operation.*
import cn.yanshiqwq.enhanced_mobs.dsl.TimerBuilder.Companion.onTimer
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.block.Block
import org.bukkit.damage.DamageSource
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.function.submit
import taboolib.common.platform.function.unregisterListener
import taboolib.common.platform.service.PlatformExecutor
import java.io.Closeable

object SkillApi {
    abstract class BaseSkillExecutor(protected val entity: Mob) : Closeable {
        // 统一管理事件监听器和定时任务
        private val listeners = hashSetOf<ProxyListener>()
        private val timers = hashSetOf<PlatformExecutor.PlatformTask>()

        // 公共资源释放逻辑
        protected fun registerDeathHandler() {
            listeners.add(entity.onDeath {
                this@BaseSkillExecutor.close()
                close()
            })
        }

        // 统一关闭方法
        override fun close() {
            listeners.forEach { unregisterListener(it) }
            timers.forEach { it.cancel() }
            listeners.clear()
            timers.clear()
        }

        // 工具方法
        protected fun addListener(listener: ProxyListener) {
            listeners.add(listener)
        }

        protected fun addTimer(task: PlatformExecutor.PlatformTask) {
            timers.add(task)
        }

        abstract fun build()
    }

    // 韧性条机制
    fun EnhancedMob.toughness(builder: ToughnessMechanism.() -> Unit = {}): ToughnessMechanism {
        toughnessInstance.apply(builder).build()
        return toughnessInstance
    }

    data class ToughnessConfig(
        // 最大韧性值
        var maxToughness: Double,
        // 每秒自然恢复量
        var regenRate: Double,
        // 恢复间隔
        var regenPeriod: Long,
        // 击破状态持续时间
        var breakDuration: Long
    )

    @Suppress("unused")
    class ToughnessMechanism(mob: EnhancedMob): BaseSkillExecutor(mob.entity!!) {
        init {
            // Default multiplier
            addReducer("cooldown_multiplier", MULTIPLY_TOTAL) { event ->
                (event.damager as? Player)?.attackCooldown?.toDouble() ?: 0.0
            }
            addReducer("crit_multiplier", MULTIPLY_TOTAL) { event ->
                if ((event.damager as? Player)?.canCrit() == true) 0.5 else 0.0
            }
            addReducer("weapon_base", ADD_BASE, weaponType(
                { it: Material -> it.isShovel() } to 0.5,
                { it: Material -> it == Material.SHIELD } to 0.5,
                { it: Material -> it.isSword() } to 1.0,
                { it: Material -> it == Material.TRIDENT } to 1.0,
                { it: Material -> it.isPickaxe() } to 1.0,
                { it: Material -> it.isAxe() } to 2.0,
                { it: Material -> it == Material.MACE } to 2.5,
            ))
        }
        val config = ToughnessConfig(12.0, 1.0, 5, 50)
        fun config(
            maxToughness: Double? = null,
            regenRate: Double? = null,
            regenPeriod: Long? = null,
            breakDuration: Long? = null
        ) {
            maxToughness?.let { config.maxToughness = it }
            regenRate?.let { config.regenRate = it }
            regenPeriod?.let { config.regenPeriod = it }
            breakDuration?.let { config.breakDuration = it }
        }

        // 韧性操作相关
        private var toughness = config.maxToughness
        fun getToughness() = toughness
        fun reduceToughness(amount: Double, event: EntityDamageByEntityEvent? = null) {
            if (isBroken) return
            toughness = (toughness - amount).coerceIn(0.0, config.maxToughness)
            onReduce(toughness, null)
            checkBreakState(event)
        }
        fun restoreToughness(amount: Double) {
            if (toughness == config.maxToughness) return
            toughness = (toughness + amount).coerceIn(0.0, config.maxToughness)
            onRegen(toughness)
        }

        // 状态参数
        private var isBroken = false
        fun isBroken() = isBroken

        // 任务
        private var regenTask: PlatformExecutor.PlatformTask? = null
        private var breakTask: PlatformExecutor.PlatformTask? = null

        // 事件回调
        private var onReduce: Closeable.(Double, EntityDamageByEntityEvent?) -> Unit = { _, _ -> }
        private var onRegen: Closeable.(Double) -> Unit = {}
        private var onBreakStart: () -> Unit = {}
        private var onBreakEnd: () -> Unit = {}
        private var onBlock: Closeable.(EntityDamageByEntityEvent) -> Unit = {}
        private var onBroken: Closeable.(EntityDamageByEntityEvent) -> Unit = {}

        // 韧性扣除值修正器
        private var toughnessReducers: HashSet<ToughnessReducer> = hashSetOf()
        fun addReducer(id: String, type: ToughnessReducer.Operation, formula: (EntityDamageByEntityEvent) -> Double) {
            toughnessReducers.add(ToughnessReducer(id, type, formula))
        }
        fun removeReducer(id: String) {
            toughnessReducers.removeIf { it.id == id }
        }
        fun damageType(vararg rules: Pair<(DamageSource) -> Boolean, Double>): (EntityDamageByEntityEvent) -> Double = { event ->
            rules.find { it.first.invoke(event.damageSource) }?.second ?: 0.0
        }
        fun weaponType(vararg rules: Pair<(Material) -> Boolean, Double>): (EntityDamageByEntityEvent) -> Double = { event ->
            val weapon = (event.damager as? LivingEntity)?.equipment?.itemInMainHand?.type ?: Material.AIR
            rules.find { it.first.invoke(weapon) }?.second ?: 0.0
        }
        data class ToughnessReducer(
            val id: String,
            val type: Operation,
            val formula: (EntityDamageByEntityEvent) -> Double
        ) {
            /**
             * 定义韧性扣除计算中不同类型的修正操作。
             *
             * 每个操作对应不同的计算阶段和调整方式，执行顺序为：
             * 1. [ADD_BASE] → 2. [MULTIPLY_BASE] → 3. [ADD_VALUE] → 4. [MULTIPLY_TOTAL]
             */
            enum class Operation {
                /**
                 * **基础值加法修正**
                 * - 作用阶段: 初始基础值（base = 1）的加法调整
                 * - 计算方式: 所有修正值相加，叠加到基础值上
                 * - 公式体现: `(1 + sum(ADD_BASE))`
                 * - 说人话：白值属性
                 * - 示例:
                 *   ```
                 *   ADD_BASE 修正值: 0.2, 0.3 → 总和 0.5
                 *   调整后基础值: 1 + 0.5 = 1.5
                 *   ```
                 */
                ADD_BASE,

                /**
                 * **基础值乘法修正**
                 * - 作用阶段: 对调整后的基础值进行百分比增幅
                 * - 计算方式: 所有修正值相加后作为百分比乘数 `(1 + sum)`
                 * - 公式体现: `(adjusted_base * (1 + sum(MULTIPLY_BASE)))`
                 * - 说人话：增伤区
                 * - 示例:
                 *   ```
                 *   MULTIPLY_BASE 修正值: 0.1, 0.2 → 总和 0.3
                 *   调整后值: 1.5 * (1 + 0.3) = 1.95
                 *   ```
                 */
                MULTIPLY_BASE,

                /**
                 * **最终值加法修正**
                 * - 作用阶段: 在基础值调整后的结果上直接叠加
                 * - 计算方式: 所有修正值相加，直接加到中间结果
                 * - 公式体现: `(previous_result + sum(ADD_VALUE))`
                 * - 说人话：小生命、小防御、小攻击
                 * - 示例:
                 *   ```
                 *   ADD_VALUE 修正值: 0.5 → 总和 0.5
                 *   调整后值: 1.95 + 0.5 = 2.45
                 *   ```
                 */
                ADD_VALUE,

                /**
                 * **全局乘法修正**
                 * - 作用阶段: 对最终结果进行百分比增幅
                 * - 计算方式: 所有修正值作为百分比乘数 `(1 + MULTIPLY_TOTAL)` 连续相乘
                 * - 公式体现: `(previous_result * product(MULTIPLY_TOTAL + 1))`
                 * - 说人话：独立乘区
                 * - 示例:
                 *   ```
                 *   MULTIPLY_TOTAL 修正值: 0.8, 0.9 → 乘积 (0.8 + 1) * (0.9 + 1) = 3.42
                 *   最终结果: 2.00 * 3.42 = 6.84
                 *   ```
                 * - 注意: 空集合默认乘积为 1.0
                 */
                MULTIPLY_TOTAL
            }
        }

        override fun build() {
            // 注册伤害监听
            addListener(entity.onDamage(EventPriority.LOW) { event ->
                if (isBroken) onBroken.invoke(this, event)
                else onBlock.invoke(this, event)

                // 计算韧性扣除
                val (addBase, multiplyBase, addValue, multiplyTotal) = toughnessReducers
                    .fold(Quadruple(0.0, 0.0, 0.0, 1.0)) { current, reducer ->
                        when (reducer.type) {
                            ToughnessReducer.Operation.ADD_BASE -> current.copy(
                                addBase = current.addBase + reducer.formula(event)
                            )
                            ToughnessReducer.Operation.MULTIPLY_BASE -> current.copy(
                                multiplyBase = current.multiplyBase + reducer.formula(event)
                            )
                            ToughnessReducer.Operation.ADD_VALUE -> current.copy(
                                addValue = current.addValue + reducer.formula(event)
                            )
                            ToughnessReducer.Operation.MULTIPLY_TOTAL -> current.copy(
                                multiplyTotal = current.multiplyTotal * (reducer.formula(event) + 1.0)
                            )
                        }
                    }

                val reduceAmount = ((1 + addBase) * (1 + multiplyBase) + addValue) * multiplyTotal
                reduceToughness(reduceAmount, event)
                onReduce(this, toughness, event)
            })

            // 初始自然恢复
            startRegen()
            registerDeathHandler()
        }

        // 辅助数据类
        private data class Quadruple(
            val addBase: Double,
            val multiplyBase: Double,
            val addValue: Double,
            val multiplyTotal: Double
        )

        // 弱点击破！
        private fun enterBreakState(event: EntityDamageByEntityEvent? = null) {
            isBroken = true

            // 若为有来源削韧，当次伤害提升50%
            if (event != null)
                event.damage *= 1.5

            // 诶你怎么似了
            entity.stun()
            entity.look(pitch = 45.0F) // 调整视角
            entity.sound(Sound.BLOCK_AMETHYST_CLUSTER_BREAK, SoundCategory.HOSTILE, 1.0F, 1.0F)

            // 清除恢复任务
            regenTask?.cancel()

            // 启动击破状态计时
            breakTask = submit(delay = config.breakDuration) {
                exitBreakState()
            }

            // 触发回调
            onBreakStart()
        }

        private fun exitBreakState() {
            isBroken = false
            toughness = config.maxToughness
            entity.unstun()
            onBreakEnd()
            startRegen()
        }

        private fun startRegen() {
            regenTask?.cancel()
            regenTask = submit(period = config.regenPeriod) {
                if (!isBroken && toughness < config.maxToughness) {
                    toughness = (toughness + config.regenRate * (config.regenPeriod * 0.05)).coerceIn(0.0, config.maxToughness)
                    onRegen(toughness)
                }
            }
            addTimer(regenTask!!)
        }

        /** DSL */
        fun onReduce(block: Closeable.(Double, EntityDamageByEntityEvent?) -> Unit) { onReduce = block }
        fun onRegen(block: Closeable.(Double) -> Unit) { onRegen = block }
        fun onBreakStart(block: () -> Unit) { onBreakStart = block }
        fun onBreakEnd(block: () -> Unit) { onBreakEnd = block }
        fun onBlock(block: Closeable.(EntityDamageByEntityEvent) -> Unit) { onBlock = block }
        fun onBroken(block: Closeable.(EntityDamageByEntityEvent) -> Unit) { onBroken = block }

        private fun checkBreakState(event: EntityDamageByEntityEvent? = null) {
            when {
                !isBroken() && toughness <= 0 -> enterBreakState(event)
                isBroken() && toughness <= 0 -> Unit // Do nothing
                !isBroken() && toughness > 0 -> startRegen()
                isBroken() && toughness > 0 -> exitBreakState()
            }
        }
    }

    fun Mob.absorptionShield(
        refreshPeriod: Long,
        shieldAmount: Double,
        builder: AbsorptionShieldExecutor.() -> Unit
    ) = AbsorptionShieldExecutor(refreshPeriod, shieldAmount, this).apply(builder).build()

    @Suppress("unused")
    class AbsorptionShieldExecutor(
        private val refreshPeriod: Long,
        private val shieldAmount: Double,
        entity: Mob
    ) : BaseSkillExecutor(entity) {
        private var onBlock: () -> Unit = {}
        private var onBroken: () -> Unit = {}
        private var onRecover: () -> Unit = {}

        fun onBlock(block: () -> Unit) { onBlock = block }
        fun onBroken(block: () -> Unit) { onBroken = block }
        fun onRecover(block: () -> Unit) { onRecover = block }

        override fun build() {
            // 事件监听
            addListener(entity.onDamage {
                if (entity.absorptionAmount > 0) onBlock() else onBroken()
            })

            // 定时刷新护盾
            addTimer(entity.onTimer(refreshPeriod) {
                entity.absorptionAmount = shieldAmount
                onRecover()
            })

            // 注册死亡清理
            registerDeathHandler()
        }
    }

    fun Mob.placeBlock(
        cooldown: Long,
        type: Material,
        duration: Long = 10,
        builder: PlaceBlockExecutor.() -> Unit
    ) = PlaceBlockExecutor(type, duration, cooldown, this).apply(builder).build()

    class PlaceBlockExecutor(
        private val blockType: Material,
        private val duration: Long,
        private val cooldown: Long?,
        entity: Mob
    ) : BaseSkillExecutor(entity) {

        private var onPlace: PlatformExecutor.PlatformTask.() -> Unit = {}
        private var onRemove: PlatformExecutor.PlatformTask.() -> Unit = {}
        private var onFailed: PlatformExecutor.PlatformTask.() -> Unit = {}
        private var currentBlock: Block? = null

        fun onPlace(block: PlatformExecutor.PlatformTask.() -> Unit) { onPlace = block }
        fun onRemove(block: PlatformExecutor.PlatformTask.() -> Unit) { onRemove = block }
        fun onFailed(block: PlatformExecutor.PlatformTask.() -> Unit) { onFailed = block }

        override fun build() {
            addTimer(entity.onTimer(10, cooldown) {
                val targetBlock = entity.target?.location?.block?.takeIf { it.type.isAir } ?: run {
                    onFailed.invoke(this)
                    return@onTimer
                }

                currentBlock = targetBlock.apply {
                    type = blockType
                    onPlace.invoke(this@onTimer)
                }

                addTimer(submit(delay = duration) {
                    currentBlock?.takeIf { it.type == blockType }?.run {
                        type = Material.AIR
                        onRemove.invoke(this@submit)
                    }
                    currentBlock = null
                })
            })
            registerDeathHandler()
        }

        override fun close() {
            super.close()
            currentBlock?.takeIf { it.type == blockType }?.type = Material.AIR
        }
    }

    fun EnhancedMob.toughnessShield(
        blockMultiplier: Double,
        breakMultiplier: Double,
    ) = ToughnessShield(blockMultiplier, breakMultiplier, this).build()

    class ToughnessShield(
        private val blockMultiplier: Double,
        private val breakMultiplier: Double,
        private val mob: EnhancedMob
    ) : BaseSkillExecutor(mob.entity!!) {
        override fun build() {
            addListener(entity.onDamage(EventPriority.HIGH) { event ->
                if (mob.toughnessInstance.isBroken()) {
                    event.damage *= (1 + breakMultiplier)
                } else {
                    event.damage *= (1 + blockMultiplier)
                }
            })
            registerDeathHandler()
        }
    }
}