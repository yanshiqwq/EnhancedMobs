package cn.yanshiqwq.enhanced_mobs.ui

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.Initialize
import cn.yanshiqwq.enhanced_mobs.config.ConfigV1
import cn.yanshiqwq.enhanced_mobs.manager.MobManager
import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.function.registerBukkitListener
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import taboolib.module.chat.colored
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max

@PlatformSide(Platform.BUKKIT)
object BossBarManager {
    private val playerBars = ConcurrentHashMap<UUID, Pair<BossBar, BossBar>>()
    // 存储最后交互时间
    private val lastInteraction = ConcurrentHashMap<UUID, Long>()
    // 定时任务引用
    private var updateTask: PlatformExecutor.PlatformTask? = null

    fun load(){
        // 注册事件监听
        registerEvents()
        // 启动更新任务
        startUpdateTask()
    }

    private fun registerEvents() {
        // 伤害事件监听
        registerBukkitListener(EntityDamageByEntityEvent::class.java, priority = EventPriority.MONITOR) { event ->
            val damager = event.damager as? Player ?: return@registerBukkitListener
            val entity = event.entity as? LivingEntity ?: return@registerBukkitListener

            MobManager.get(entity.uniqueId)?.let { mob ->
                val pair = playerBars.compute(damager.uniqueId) { _, existing ->
                    existing ?: createNewBars(entity).also {
                        it.first.addPlayer(damager)
                        it.second.addPlayer(damager)
                    }
                }

                val (healthBar, toughnessBar) = when {
                    pair != null -> pair
                    playerBars.containsKey(damager.uniqueId) -> playerBars[damager.uniqueId]!! // 已有条目
                    else -> return@let // 放弃更新
                }

                // 更新血条
                updateHealthBar(healthBar, entity, event.finalDamage)
                // 更新韧性条
                updateToughnessBar(toughnessBar, mob)

                // 更新最后交互时间
                lastInteraction[damager.uniqueId] = System.currentTimeMillis()
            }
        }

        // 实体死亡事件
        registerBukkitListener(EntityDeathEvent::class.java) { event ->
            event.entity.let { entity ->
                playerBars.values.removeAll { (healthBar, toughnessBar) ->
                    if (healthBar.title.contains(entity.name)) {
                        healthBar.removeAll()
                            toughnessBar.removeAll()
                            true
                    } else false
                }
            }
        }

        // 玩家退出事件
        registerBukkitListener(PlayerQuitEvent::class.java) { event ->
            playerBars.remove(event.player.uniqueId)?.let { (health, toughness) ->
                health.removeAll()
                toughness.removeAll()
            }
        }
    }

    private fun startUpdateTask() {
        updateTask = submit(period = 90) {
            val now = System.currentTimeMillis()
            playerBars.entries.removeIf { (uuid, bars) ->
                val player = Bukkit.getPlayer(uuid) ?: return@removeIf true
                val (healthBar, toughnessBar) = bars

                // 检查最后交互时间
                if (now - (lastInteraction[uuid] ?: 0) > 5000) {
                    healthBar.removePlayer(player)
                    toughnessBar.removePlayer(player)
                    true
                } else false
            }
        }
    }

    private fun createNewBars(entity: LivingEntity): Pair<BossBar, BossBar> {
        val maxHealth = entity.getAttribute(Attribute.MAX_HEALTH)!!.value
        val mob = MobManager.get(entity.uniqueId)
        val prefix = mob?.let { ConfigV1.namePrefix.format(Initialize.color(it.level), it.level) } ?: ""
        return Pair(
            Bukkit.createBossBar(
                "$prefix${entity.name}".colored(),
                BarColor.GREEN,
                when (maxHealth) {
                    in 4.0..16.0 -> BarStyle.SEGMENTED_6
                    in 16.0..32.0 -> BarStyle.SEGMENTED_10
                    in 32.0..1024.0 -> BarStyle.SEGMENTED_12
                    else -> BarStyle.SOLID
                }
            ).apply {
                progress = (entity.health / maxHealth).coerceIn(0.0, 1.0)
            },
            Bukkit.createBossBar(
                "&b✦".colored(),
                BarColor.WHITE,
                BarStyle.SOLID
            ).apply { progress = 1.0 }
        )
    }

    private fun updateHealthBar(bar: BossBar, entity: LivingEntity, damage: Double) {
        val maxHealth = entity.getAttribute(Attribute.MAX_HEALTH)!!.value
        val current = max(entity.health - damage, 0.0)
        val progress = (current / maxHealth).coerceIn(0.0..1.0)

        val mob = MobManager.get(entity.uniqueId)
        val prefix = if (mob != null) ConfigV1.namePrefix.format(Initialize.color(mob.level), mob.level) else ""
        bar.apply {
            setTitle("$prefix${entity.name} &7|&r &c❤ &f${formatValue(current)}&7/&f${formatValue(maxHealth)}".colored())
            this.progress = progress
            color = when {
                progress > 0.65 -> BarColor.GREEN
                progress > 0.35 -> BarColor.YELLOW
                else -> BarColor.RED
            }
        }
    }

    private fun updateToughnessBar(bar: BossBar, mob: EnhancedMob) {
        val toughness = mob.toughnessInstance
        val max = toughness.config.maxToughness
        val current = toughness.getToughness().coerceIn(0.0..max)
        val progress = current / max

        bar.apply {
            setTitle("&b✦ &f${formatValue(current)}&7/&f${formatValue(max)}".colored())
            this.progress = progress.coerceIn(0.0, 1.0)
        }
    }

    private fun formatValue(value: Double) = "%.2f".format(value)
}