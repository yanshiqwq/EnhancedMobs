package cn.yanshiqwq.enhanced_mobs.dsl

import org.bukkit.*
import org.bukkit.attribute.Attributable
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier.Operation
import org.bukkit.entity.*
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import taboolib.common.util.Vector
import taboolib.platform.util.groundBlock
import taboolib.platform.util.setEquipment
import taboolib.type.BukkitEquipment
import java.util.*
import kotlin.random.Random

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.MobApi
 * 
 * @author yanshiqwq
 * @since 2024/8/28 下午 7:07
 */
object MobApi {
    /**
     * 修改实体的自定义名称
     *
     * @param name 要设置的新名称
     */
    fun LivingEntity.name(name: String) {
        customName = name
    }
    
    /**
     * 为实体添加伤害监听器
     *
     * @param params 配置监听器的参数
     */
    inline fun LivingEntity.onDamage(params: ListenerBuilder<EntityDamageByEntityEvent>.() -> Unit) =
        ListenerBuilder(EntityDamageByEntityEvent::class.java)
            .apply(params)
            .build(this)
    
    /**
     * 为实体添加攻击监听器
     *
     * @param params 配置监听器的参数
     */
    inline fun LivingEntity.onAttack(params: ListenerBuilder<EntityDamageByEntityEvent>.() -> Unit) =
        ListenerBuilder(EntityDamageByEntityEvent::class.java)
            .apply(params)
            .build(this) {damager}
    
    /**
     * 为实体添加死亡监听器
     * 默认在实体死亡时也可以执行，请手动在 executor 中添加 close() 关闭监听器
     *
     * @param params 配置监听器的参数
     */
    inline fun LivingEntity.onDeath(params: ListenerBuilder<EntityDeathEvent>.() -> Unit) =
        ListenerBuilder(EntityDeathEvent::class.java)
            .apply {runAfterEntityDead = true}
            .apply(params)
            .build(this)
    
    /**
     * 创建一个 TabooLib 计时器任务
     *
     * @param params 配置计时器的参数
     * @see TimerBuilder
     * @return 返回创建的 TabooLib 计时器任务
     */
    inline fun LivingEntity.onTimer(
        period: Long,
        cooldown: Long? = null,
        params: TimerBuilder.() -> Unit
    ) {
        val builder = TimerBuilder(period)
        if (cooldown != null) builder.setCooldown(cooldown, world)
        params.invoke(builder)
        return builder.build(this)
    }
    
    /**
     * 创建一个 TabooLib 延迟任务
     *
     * @param params 配置计时器的参数
     * @see TimerBuilder
     * @return 返回创建的 TabooLib 计时器任务
     */
    inline fun LivingEntity.delay(
        delay: Long,
        params: DelayBuilder.() -> Unit
    ) {
        val builder = DelayBuilder(delay)
        params.invoke(builder)
        return builder.build(this)
    }
    
    /**
     * 修改实体的装备
     *
     * @param block 用于配置装备的构建块
     */
    inline fun LivingEntity.equip(block: EquipmentBuilder.() -> Unit) =
        EquipmentBuilder().apply(block).apply(this)
    fun LivingEntity.equip(slot: BukkitEquipment, item: ItemStack) = setEquipment(slot, item)
    fun LivingEntity.equip(slot: BukkitEquipment, type: Material) = equip(slot, ItemStack(type))
    
    /**
     * 添加药水效果到实体
     *
     * @param type 药水效果的类型
     * @param block 用于配置药水效果的构建块
     */
    inline fun LivingEntity.effect(type: PotionEffectType, block: PotionEffectBuilder.() -> Unit = {}) {
        val builder = PotionEffectBuilder(type)
        block.invoke(builder)
        addPotionEffect(builder.build())
    }
    
    /**
     * 添加指定药水类型的所有效果到实体
     *
     * @param type 药水类型
     * @param block 用于配置药水效果的构建块
     */
    inline fun LivingEntity.potion(type: PotionType, block: PotionTypeBuilder.() -> Unit = {}) {
        val effects = PotionTypeBuilder(type.potionEffects).apply(block).build()
        addPotionEffects(effects)
    }
    
    /**
     * 添加一组效果到指定实体
     */
    inline fun LivingEntity.potion(block: PotionTypeBuilder.() -> Unit) {
        val effects = PotionTypeBuilder().apply(block).build()
        addPotionEffects(effects)
    }
    
    /**
     * 在指定位置播放声音
     *
     * @param sound 要播放的声音
     * @param category 声音的类别
     * @param volume 播放声音的音量
     * @param pitch 播放声音的音调
     */
    fun Location.sound(sound: Sound, category: SoundCategory, pitch: Float = 1.0F, volume: Float) =
        world?.playSound(this, sound, category, volume, pitch)
    
    fun Entity.sound(sound: Sound, category: SoundCategory, pitch: Float = 1.0F, volume: Float = 1.0F) =
        location.sound(sound, category, volume, pitch)
    
    /**
     * 在实体的位置播放指定的声音
     *
     * @param sound 要播放的声音
     * @param pitch 播放声音的音调
     * @param volume 播放声音的音量
     */
    fun Entity.sound(sound: Sound, pitch: Float = 1.0F, volume: Float = 1.0F) =
        sound(sound, SoundCategory.HOSTILE, volume, pitch)
    
    fun <T> Location.particle(particle: Particle, count: Int, speed: Double = 0.0, offset: Vector = Vector(0,0,0), data: T) =
        world?.spawnParticle<T>(particle, this, count, offset.x, offset.y, offset.z, speed, data)
    fun Location.particle(particle: Particle, count: Int, speed: Double = 0.0, offset: Vector = Vector(0,0,0)) =
        world?.spawnParticle(particle, this, count, offset.x, offset.y, offset.z, speed)
    
    fun Entity.particle(
        particle: Particle,
        count: Int = 32,
        speed: Double = 0.0,
        block: ParticleBuilder.() -> Unit
    ) {
        val builder = ParticleBuilder(particle, count, speed)
        block.invoke(builder)
        builder.build(location)
    }
    
    fun Mob.look(yaw: Float? = null, pitch: Float? = null) {
        setRotation(yaw ?: location.yaw, pitch ?: location.pitch)
    }
    fun Mob.rotate(yawOffset: Float = 0.0F, pitchOffset: Float = 0.0F) =
        setRotation(location.yaw + yawOffset, location.pitch + pitchOffset)
    
    /**
     * 检查目标是否满足给定条件
     * @param default 目标不存在时的默认返回值，默认值等效于 hasTarget()
     * @param condition 用于检查目标的条件
     * @return 如果目标不存在，返回默认值；否则，返回条件函数对目标的检查结果
     */
    inline fun Mob.target(default: Boolean = false, condition: LivingEntity.() -> Boolean): Boolean =
        if (target == null) default
        else condition.invoke(target!!)
    
    /**
     * 计算两个实体之间的距离
     * @param entity 要计算距离的实体
     * @return 当前实体与指定实体之间的距离
     */
    fun Entity.distance(entity: Entity): Double = location.distance(entity.location)
    
    /**
     * 判断该生物与目标之间的距离是否满足给定条件
     * @param condition 用于检查距离的条件函数
     * @return 如果目标不存在，返回 false；否则，返回条件函数对距离的检查结果
     */
    inline fun Mob.distanceFromTarget(condition: (Double) -> Boolean): Boolean {
        val targetDistance = target?.let {distance(it) } ?: return false
        return condition(targetDistance)
    }
    
    /**
     * 判断实体是否位于指定的方块类型上
     * @param type 要检查的方块类型
     * @return 如果实体所在位置的方块类型在给定类型中，返回 true；否则，返回 false
     */
    fun Entity.inBlock(vararg type: Material) = type.contains(location.block.type)
    
    /**
     * 判断实体是否在液体中
     * @return 如果实体所在位置的方块是液体，返回 true；否则，返回 false
     */
    fun Entity.inLiquid() = location.block.isLiquid
    
    /**
     * 判断实体是否在空气中
     * @return 如果实体所在位置的方块是空气或其他空气类型，返回 true；否则，返回 false
     */
    fun Entity.inAir()= inBlock(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR)
    
    /**
     * 判断实体是否在指定的方块类型上方
     * @param type 要检查的方块类型
     * @return 如果实体下方的方块类型在给定类型中，返回 true；否则，返回 false
     */
    fun Entity.onBlock(vararg type: Material) =
        type.contains(groundBlock.type)
    
    /**
     * 判断实体是否在液体上方
     * @return 如果实体下方的方块是液体，返回 true；否则，返回 false
     */
    fun Entity.onLiquid() = groundBlock.isLiquid
    
    /**
     * 移动位置坐标
     * @param x X 轴的偏移量
     * @param y Y 轴的偏移量
     * @param z Z 轴的偏移量
     * @return 移动后的新位置
     */
    fun Location.move(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0) = clone().apply {
        this.x += x
        this.y += y
        this.z += z
    }
    
    /**
     * 根据给定的概率计算随机事件是否发生
     *
     * @param chance 发生的概率，范围在 0 到 1 之间
     * @param random 用于生成随机数的 Random 对象
     * @return 如果随机数小于给定概率，则返回 true，表示事件发生
     */
    fun chance(chance: Double, random: Random = Random) = random.nextDouble() < chance
    
    /**
     * 根据给定的概率和种子计算随机事件是否发生
     *
     * @param chance 发生的概率，范围在 0 到 1 之间
     * @param seed 用于初始化 Random 对象的种子
     * @param random 用于生成随机数的 Random 对象，使用给定的种子初始化
     * @return 如果随机数小于给定概率，则返回 true，表示事件发生
     */
    fun chance(chance: Double, seed: Long, random: Random = Random(seed)) = chance(chance, random)
    
    /**
     * 根据给定的概率和种子计算随机事件是否发生
     *
     * @param chance 发生的概率，范围在 0 到 1 之间
     * @param seed 用于初始化 Random 对象的种子
     * @param random 用于生成随机数的 Random 对象，使用给定的种子初始化
     * @return 如果随机数小于给定概率，则返回 true，表示事件发生
     */
    fun chance(chance: Double, seed: Int, random: Random = Random(seed)) = chance(chance, random)
    
    /**
     * 修改实体的基础属性
     *
     * @param block 用于配置属性的构建块
     */
    inline fun LivingEntity.base(block: AttributeBuilder.() -> Unit) =
        AttributeBuilder().apply(block).applyAsBase(this)
    
    /**
     * 为实体附加属性修饰器
     *
     * @param name 修饰器的名称
     * @param operation 修饰器的操作类型
     * @param uuid 可选的修饰器 UUID
     * @param block 用于配置属性的 DSL 构建块
     */
    inline fun Attributable.addModifier(
        name: String,
        operation: Operation,
        uuid: UUID? = null,
        block: AttributeBuilder.() -> Unit
    )= AttributeBuilder().apply(block).run {
        if (uuid == null) applyAsModifier(name, operation, this@addModifier)
        else applyAsModifier(name, operation, this@addModifier, uuid)
    }
    
    /**
     * 为实体移除属性修饰器
     *
     * @param name 要移除的修饰器的名称
     */
    fun Attributable.removeModifier(name: String) = AttributeBuilder.removeModifier(name, this)
    
    /**
     * 为实体移除属性修饰器
     *
     * @param uuid 要移除的修饰器的 UUID
     */
    fun Attributable.removeModifier(uuid: UUID) = AttributeBuilder.removeModifier(uuid, this)
    
    fun HumanEntity.isFullCharge() = attackCooldown >= 1.0
    
    fun LivingEntity.heal() = percentHeal(1.0)
    
    fun LivingEntity.percentHeal(percent: Double) {
        health = getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value * percent
    }
    
    fun Mob.stun(time: Long) {
        isAware = false
        delay(time) {
            execute {
                isAware = true
            }
        }
    }
    
    fun ItemStack.isSword() = type.toString().endsWith("_SWORD")
    fun ItemStack.isAxe() = type.toString().endsWith("_AXE")
    fun ItemStack.isPickaxe() = type.toString().endsWith("_PICKAXE")
    fun ItemStack.isShovel() = type.toString().endsWith("_SHOVEL")
    fun ItemStack.isHelmet() = type.toString().endsWith("_HELMET")
    fun ItemStack.isChestPlate() = type.toString().endsWith("_CHESTPLATE")
    fun ItemStack.isLeggings() = type.toString().endsWith("_LEGGINGS")
    fun ItemStack.isBoots() = type.toString().endsWith("_BOOTS")
}