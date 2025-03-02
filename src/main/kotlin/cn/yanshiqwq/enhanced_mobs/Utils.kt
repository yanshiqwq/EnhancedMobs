package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.dsl.*
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.block.Block
import org.bukkit.entity.*
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.FireworkMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.function.registerBukkitListener
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import taboolib.common.util.Vector
import taboolib.platform.BukkitPlugin
import taboolib.platform.util.*
import taboolib.type.BukkitEquipment
import java.io.Closeable
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.dsl.MobApi
 * 
 * @author yanshiqwq
 * @since 2024/8/28 下午 7:07
 */
@Suppress("MemberVisibilityCanBePrivate")
object Utils {
    /**
     * 修改实体的自定义名称
     *
     * @param name 要设置的新名称
     */
    fun LivingEntity.name(name: String) {
        customName = name
    }

    /**
     * 为实体添加事件监听器
     */
    inline fun <reified T: EntityEvent> LivingEntity.listen(priority: EventPriority = EventPriority.NORMAL, noinline criteria: T.() -> Entity, noinline block: Closeable.(T) -> Unit) =
        EntityEventListenerBuilder(T::class.java, priority)
            .build(this, block, criteria)
    
    /**
     * 为实体添加伤害监听器
     */
    fun LivingEntity.onDamage(priority: EventPriority = EventPriority.NORMAL, block: Closeable.(EntityDamageByEntityEvent) -> Unit) =
        EntityEventListenerBuilder(EntityDamageByEntityEvent::class.java, priority)
            .build(this, block)
    
    /**
     * 为实体添加攻击监听器
     */
    fun LivingEntity.onAttack(block: Closeable.(EntityDamageByEntityEvent) -> Unit) =
        EntityEventListenerBuilder(EntityDamageByEntityEvent::class.java)
            .build(this, block) {damager}
    
    /**
     * 为实体添加死亡监听器
     */
    fun LivingEntity.onDeath(block: Closeable.(EntityDeathEvent) -> Unit) =
        EntityEventListenerBuilder(EntityDeathEvent::class.java)
            .apply {runAfterEntityDead = true}
            .build(this, {
                block.invoke(this, it)
                close()
            })
    
    /**
     * 修改实体的装备
     *
     * @param block 用于配置装备的构建块
     */
    inline fun LivingEntity.equip(block: EquipmentBuilder.() -> Unit) =
        EquipmentBuilder().apply(block).apply(this)
    
    fun LivingEntity.equip(slot: BukkitEquipment, item: ItemStack) = setEquipment(slot, item)
    fun LivingEntity.equip(slot: BukkitEquipment, type: Material) = equip(slot, ItemStack(type))
    fun LivingEntity.clear(slot: BukkitEquipment) = equip(slot, Material.AIR)
    
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
        submit { // playSound 需要在主线程执行
            world?.playSound(this@sound, sound, category, volume, pitch)
        }
    
    fun Entity.sound(sound: Sound, category: SoundCategory, pitch: Float = 1.0F, volume: Float = 1.0F) =
        location.sound (sound, category, volume, pitch)
    
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
        count: Int,
        block: ParticleBuilder.() -> Unit
    ) {
        val builder = ParticleBuilder(particle, count)
        block.invoke(builder)
        builder.build(location)
    }
    
    fun Mob.look(entity: Entity) = look(entity.location)
    fun Mob.look(location: Location) {
        val selfLoc = this.location.toVector()
        val targetLoc = location.toVector()
        val direction = targetLoc.subtract(selfLoc)
        teleport(location.setDirection(direction))
    }
    fun Mob.look(yaw: Float? = null, pitch: Float? = null) {
        setRotation(yaw ?: location.yaw, pitch ?: location.pitch)
    }
    fun Mob.rotate(yawOffset: Float = 0.0F, pitchOffset: Float = 0.0F) =
        setRotation(location.yaw + yawOffset, location.pitch + pitchOffset)
    
    /**
     * 检查目标是否满足给定条件
     * @param condition 用于检查目标的条件
     * @return 如果目标不存在，返回默认值；否则，返回条件函数对目标的检查结果
     */
    inline fun Mob.target(default: Boolean = false, condition: LivingEntity.() -> Boolean): Boolean =
        target?.let{ condition.invoke(it) } ?: default
    
    /**
     * 计算两个实体之间的距离
     * @param entity 要计算距离的实体
     * @return 当前实体与指定实体之间的距离
     */
    fun Entity.distance(entity: Entity): Double = location.distance(entity.location)
    
    fun Mob.distanceFromTarget(range: ClosedFloatingPointRange<Double>): Boolean {
        val targetDistance = target?.let { distance(it) } ?: return false
        return targetDistance in range
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

    fun allOf(vararg condition: Boolean) = condition.all { it }

    fun anyOf(vararg condition: Boolean) = condition.any { it }

    inline fun require(vararg condition: Boolean, beforeReturn: () -> Unit = {}) {
        if (!condition.all { it }){
            beforeReturn.invoke()
            return
        }
    }

    fun HumanEntity.attackCharged(percent: Double) = attackCooldown >= percent
    
    fun LivingEntity.heal() = percentHeal(1.0)
    
    fun LivingEntity.percentHeal(percent: Double) {
        health = getAttribute(Attribute.MAX_HEALTH)!!.value * percent
    }
    
    fun Mob.stun(time: Long): PlatformExecutor.PlatformTask {
        effect(PotionEffectType.SLOWNESS) {
            duration = time.toInt()
            amplifier = 255
        }
        stun()
        return submit(delay = time) {
            unstun()
        }
    }

    fun Mob.stun() {
        isAware = false
        setAI(false)
        submit(delay = 1L) {
            setAI(true)
        }
    }
    fun Mob.unstun() { isAware = true }
    
    fun LivingEntity.freeze(time: Long) {
        setAI(false)
        submit(delay = time){
            setAI(true)
        }
    }
    
    fun Entity.taunt(radius: Double) = world.getNearbyEntities(location, radius, radius, radius).forEach {
        (it as? Mob)?.attack(this)
    }
    
    fun potionItem(type: Material, potion: PotionType, count: Int = 1, block: ItemBuilder.() -> Unit): ItemStack =
        buildItem(type){
            amount = count
            block.invoke(this)
        }.modifyMeta<PotionMeta> {
            basePotionType = potion
        }
    
    fun fireworkItem(count: Int = 1, block: FireworkEffect.Builder.() -> Unit = {
        flicker(true)
        withColor(Color.fromRGB(11743532)) // 设置颜色
        with(FireworkEffect.Type.BURST) // 设置爆炸形状
        trail(true)
    }): ItemStack =
        buildItem(Material.FIREWORK_ROCKET) {
            amount = count
        }.modifyMeta<FireworkMeta> {
            power = 3
            val builder = FireworkEffect.builder()
            block.invoke(builder)
            addEffect(builder.build())
        }
    
    fun Entity.dash(force: Double): Unit = setMotionMultiplier(force)
    fun Entity.knockBack(force: Double): Unit = setMotionMultiplier(-force)
    private fun Entity.setMotionMultiplier(multiplier: Double) {
        velocity = location.direction.multiply(multiplier)
    }
    
    fun Material.isSword() = toString().endsWith("_SWORD")
    fun Material.isAxe() = toString().endsWith("_AXE")
    fun Material.isPickaxe() = toString().endsWith("_PICKAXE")
    fun Material.isShovel() = toString().endsWith("_SHOVEL")
    fun Material.isHelmet() = toString().endsWith("_HELMET")
    fun Material.isChestPlate() = toString().endsWith("_CHESTPLATE")
    fun Material.isLeggings() = toString().endsWith("_LEGGINGS")
    fun Material.isBoots() = toString().endsWith("_BOOTS")
    
    fun LivingEntity.isOnFire() = fireTicks > 0
    fun LivingEntity.isFreeze() = freezeTicks > 0

    fun Player.canCrit() = fallDistance > 0 && !(this as LivingEntity).isOnGround

    fun Block?.byType(type: Material, block: Block.() -> Unit) = takeIf { type != this?.type }?.run(block) ?: Unit
    fun <T: Entity> Entity?.byType(clazz: Class<T>, block: Entity.() -> Unit) = takeIf { this?.javaClass == clazz }?.run(block)
    
    fun <T: Entity> Location.spawn(clazz: Class<T>, block: T.() -> Unit) = world?.spawn(this, clazz, block)
    
    fun Location.spawnTNTPrimed(breakBlock: Boolean = false, block: TNTPrimed.() -> Unit) = spawn(TNTPrimed::class.java) {
        // 仿照原版 TNT 点燃逻辑
        // @see https://zh.minecraft.wiki/w/TNT#%E8%A1%8C%E4%B8%BA
        val angle = Random.nextDouble() * 2 * Math.PI // 随机角度
        val velocityX = 0.02 * cos(angle) // 水平方向上的X分量
        val velocityZ = 0.02 * sin(angle) // 水平方向上的Z分量
        
        // 设置加速度
        location.direction.apply {
            x = velocityX
            y = 0.2
            z = velocityZ
        }
        
        // 调用回调函数
        block.invoke(this)

        if (breakBlock) return@spawn
        registerBukkitListener(EntityExplodeEvent::class.java, ignoreCancelled = true) {
            if(it.entity != this@spawn) return@registerBukkitListener
            it.blockList().clear()
            close()
        }
    }
    
    fun PersistentDataHolder.hasKey(key: NamespacedKey) = persistentDataContainer.has(key)
    fun PersistentDataHolder.removeKey(key: NamespacedKey) = persistentDataContainer.remove(key)
    fun createNamespacedKey(id: String) = NamespacedKey(BukkitPlugin.getInstance(), id)
}