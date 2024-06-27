package packs.yanshiqwq

import cn.yanshiqwq.enhanced_mobs.data.Record.DoubleFactor
import cn.yanshiqwq.enhanced_mobs.data.Record.logFormula
import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.Utils.applyEffect
import cn.yanshiqwq.enhanced_mobs.Utils.equip
import cn.yanshiqwq.enhanced_mobs.Utils.heal
import cn.yanshiqwq.enhanced_mobs.Utils.isAxe
import cn.yanshiqwq.enhanced_mobs.Utils.placeBlock
import cn.yanshiqwq.enhanced_mobs.Utils.playSound
import cn.yanshiqwq.enhanced_mobs.Utils.setMotionMultiplier
import cn.yanshiqwq.enhanced_mobs.Utils.spawnEntity
import cn.yanshiqwq.enhanced_mobs.Utils.spawnParticle
import cn.yanshiqwq.enhanced_mobs.script.DslBuilder.pack
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.*
import org.bukkit.event.entity.*
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.attribute.Attribute.*
import org.bukkit.attribute.AttributeModifier.Operation.*
import org.bukkit.util.Vector
import kotlin.random.Random
import kotlin.random.nextInt

pack("extend") {
    type("skeleton_frost", "vanilla.skeleton") { mob ->
        listener<EntityShootBowEvent> {
            if (it.entity != mob.entity) return@listener
            it.projectile.isGlowing = true
        }
        listener<EntityDamageByEntityEvent> {
            if (it.damager !is Arrow || it.entity !is LivingEntity) return@listener
            if (it.entity is Player && (it.entity as Player).isBlocking) return@listener
            val arrow = it.damager as Arrow
            if (arrow.shooter != mob.entity) return@listener
            val entity = it.entity as LivingEntity

            // 冻结 6~10 秒
            entity.freezeTicks = 140 + Random.nextInt(3..5) * 40
        }
    }
    type("skeleton_iron_sword", "vanilla.skeleton") {
        item(EquipmentSlot.HAND, Material.IRON_SWORD)
        attribute(GENERIC_MAX_HEALTH, MULTIPLY_SCALAR_1, DoubleFactor { 0.65 * it })
        attribute(GENERIC_ATTACK_DAMAGE, MULTIPLY_SCALAR_1, logFormula(0.8))
        attribute(GENERIC_MOVEMENT_SPEED, MULTIPLY_SCALAR_1, logFormula(0.1))
    }
    type("spider_cobweb", "vanilla.spider") { mob ->
        val taskPeriod = 80 - mob.multiplier * 10
        itemTask {
            distance = 5.0
            before = ItemStack(Material.COBWEB)
            period = taskPeriod.toLong().coerceIn(50L..100L)
            function = lambda@{
                val block = it.location.block
                if (block.isLiquid || !block.isReplaceable) return@lambda false
                it.location.run {
                    placeBlock(Material.COBWEB)
                    playSound(Sound.ENTITY_SPIDER_AMBIENT, 1.0F, 2.0F)
                    task {
                        delay = period * 2
                        function = delay@{
                            if (block.type != Material.COBWEB) return@delay false
                            placeBlock(Material.AIR)
                            playSound(Sound.ENTITY_SPIDER_STEP, 1.0F, 2.0F)
                            entity.equip(EquipmentSlot.OFF_HAND, Material.COBWEB)
                            return@delay true
                        }
                    }
                }
                entity.applyEffect(PotionEffectType.SPEED, 0, 5 * 20)
                return@lambda true
            }
        }
    }
    type("zombie_reduce_air", "vanilla.zombie") {
        listener<EntityDamageByEntityEvent> {
            if (it.damager.uniqueId != it.entity.uniqueId || it.entity !is LivingEntity) return@listener
            val entity = it.entity as LivingEntity
            entity.remainingAir = (entity.remainingAir - 180).coerceAtLeast(0)
        }
    }
    type("zombie_shield", "vanilla.zombie") { mob ->
        val itemType = Material.SHIELD
        val entity = mob.entity

        item(EquipmentSlot.OFF_HAND, ItemStack(itemType))
        listener<EntityDamageByEntityEvent> {
            if (it.entity.uniqueId != entity.uniqueId || entity.isDead) return@listener

            val shield = entity.equipment.itemInOffHand
            if (shield.type != itemType) return@listener
            val shieldKey = NamespacedKey(instance!!, "shield")

            if (it.damager !is LivingEntity) return@listener
            if ((it.damager as LivingEntity).equipment == null) return@listener
            val damager = it.damager as LivingEntity

            // 检查护盾是否不存在
            if (entity.persistentDataContainer.getOrDefault(shieldKey, PersistentDataType.BYTE, 0) != 1.toByte()) {
                if (Random.nextDouble() <= (0.35 * (mob.multiplier + 1)).coerceIn(0.0, 0.85)) {
                    // 激活护盾
                    entity.persistentDataContainer.set(shieldKey, PersistentDataType.BYTE, 1)
                    entity.location.spawnParticle(Particle.SPELL_INSTANT, 64, Vector(0.3, 0.5, 0.3), 1.0)
                    task {
                        id = "onShield"
                        delay = 100
                        function = delay@{
                            entity.persistentDataContainer.set(shieldKey, PersistentDataType.BYTE, 0)
                            return@delay true
                        }
                    }
                }
                return@listener
            }

            // 斧头破盾
            if (damager.equipment!!.itemInMainHand.type.isAxe()) {
                // 取消护盾并暂时禁用 AI
                entity.persistentDataContainer.set(shieldKey, PersistentDataType.BYTE, 0)
                entity.setAI(false)
                entity.damage(it.damage * 0.5, damager)
                task {
                    id = "setAI"
                    delay = 40
                    function = delay@{
                        entity.setAI(true)
                        return@delay true
                    }
                }

                // 攻击力降低 85%, 持续 100 ticks
                val modifier = AttributeModifier("Break shield bonus", -0.85, AttributeModifier.Operation.MULTIPLY_SCALAR_1)
                entity.getAttribute(GENERIC_ATTACK_DAMAGE)?.addModifier(modifier)
                task {
                    id = "weakness"
                    delay = 100
                    function = delay@{
                        entity.getAttribute(GENERIC_ATTACK_DAMAGE)?.removeModifier(modifier)
                        return@delay true
                    }
                }

                // 破盾后特效
                entity.location.playSound(Sound.ITEM_SHIELD_BREAK, 1.0F, 1.0F)
                entity.location.spawnParticle(Particle.ITEM_CRACK, 72, Vector(0.3, 0.5, 0.3), 0.0, ItemStack(Material.SHIELD))
                entity.location.spawnParticle(Particle.CRIT_MAGIC, 64, Vector(0.3, 0.5, 0.3), 0.5)
            } else {
                // 格挡攻击
                damager.setMotionMultiplier(-0.5)
                entity.location.playSound(Sound.ITEM_SHIELD_BLOCK, 1.0F, 1.0F)
                it.isCancelled = true
            }
        }
    }
    type("zombie_leader", "extend.zombie_shield") {
        item(EquipmentSlot.HAND, Material.IRON_SWORD)
        attribute(ZOMBIE_SPAWN_REINFORCEMENTS, ADD_NUMBER, DoubleFactor(0.0..0.65) { 0.35 + 0.05 * it })
    }
    type("zombie_cobweb", "vanilla.zombie") { mob ->
        itemTask {
            distance = 5.0
            before = ItemStack(Material.COBWEB)
            period = (100 - mob.multiplier * 10).toLong().coerceIn(60L..120L)
            function = task@{
                val block = it.location.block
                if (block.isLiquid || !block.isReplaceable) return@task false
                it.location.run {
                    placeBlock(Material.COBWEB)
                    playSound(Sound.BLOCK_STONE_PLACE, 1.0F, 1.0F)
                    task {
                        delay = period * 2
                        function = delay@{
                            if (block.type != before.type) return@delay false
                            playSound(Sound.BLOCK_STONE_BREAK, 1.0F, 1.0F)
                            item(EquipmentSlot.OFF_HAND, Material.COBWEB)
                            return@delay true
                        }
                    }
                }
                entity.applyEffect(PotionEffectType.SPEED, 7, 5 * 20)
                return@task true
            }
        }
    }
    type("zombie_strength_cloud", "vanilla.zombie") { mob ->
        listener<EntityDeathEvent> {
            val entity = mob.entity
            if (it.entity.uniqueId != entity.uniqueId) return@listener
            entity.location.spawnEntity<AreaEffectCloud>(EntityType.AREA_EFFECT_CLOUD) {
                color = PotionEffectType.INCREASE_DAMAGE.color
                duration = 100
                radius = 2.0F
                addCustomEffect(PotionEffect(PotionEffectType.INCREASE_DAMAGE, 120, 1), true)
            }
        }
    }
    type("zombie_totem", "vanilla.zombie") { mob ->
        val entity = mob.entity
        item(EquipmentSlot.OFF_HAND, Material.TOTEM_OF_UNDYING)
        listener<EntityResurrectEvent> {
            if (it.entity != entity) return@listener
            entity.heal(0.5)
            entity.applyEffect(PotionEffectType.SPEED, 0)
        }
    }
    type("zombie_tnt", "vanilla.zombie") { mob ->
        itemTask {
            distance = 6.0
            before = ItemStack(Material.TNT)
            function = task@{
                val loc = it.location
                loc.run {
                    spawnEntity<TNTPrimed>(EntityType.PRIMED_TNT) {
                        fuseTicks = 40
                        source = entity
                        velocity = loc.subtract(entity.location).toVector()
                    }
                    playSound(Sound.BLOCK_GRASS_PLACE, 1.0F, 0.75F)
                    playSound(Sound.ENTITY_TNT_PRIMED, 1.0F, 1.0F)
                }
                entity.applyEffect(PotionEffectType.DAMAGE_RESISTANCE, 1, 50)
                return@task true
            }
        }
    }
    type("zombie_anvil", "vanilla.zombie") {
        itemTask {
            distance = 5.0
            before = ItemStack(Material.DAMAGED_ANVIL)
            function = task@{
                val loc = it.location.clone().apply { y += 3 }
                if (!loc.block.isReplaceable) return@task false
                loc.run {
                    spawnEntity<FallingBlock>(EntityType.FALLING_BLOCK) {
                        setHurtEntities(true)
                        cancelDrop = true
                        dropItem = false
                        damagePerBlock = (6 + multiplier * 1.5).toFloat()
                        blockData = server.createBlockData(Material.DAMAGED_ANVIL)
                    }
                    playSound(Sound.BLOCK_ANVIL_PLACE, 1.0F, 1.0F)
                    entity.applyEffect(PotionEffectType.DAMAGE_RESISTANCE, 1, 50)
                }
                return@task true
            }
        }
    }
    type("zombie_lava", "vanilla.zombie") {
        itemTask {
            distance = 5.0
            before = ItemStack(Material.LAVA_BUCKET)
            after = ItemStack(Material.BUCKET)
            period = 40L
            function = task@{
                val block = it.location.block
                if (block.isLiquid || !block.isReplaceable) return@task false
                it.location.run {
                    placeBlock(Material.LAVA)
                    playSound(Sound.ITEM_BUCKET_EMPTY_LAVA, 1.0F, 1.0F)
                    task {
                        delay = 100L
                        function = delay@{
                            if (block.type != Material.LAVA) return@delay false
                            placeBlock(Material.AIR)
                            playSound(Sound.ITEM_BUCKET_FILL_LAVA, 1.0F, 1.0F)
                            item(EquipmentSlot.OFF_HAND, Material.LAVA_BUCKET)
                            return@delay true
                        }
                    }
                }
                entity.applyEffect(PotionEffectType.FIRE_RESISTANCE, 0, 72 * 20)
                return@task true
            }
        }
    }
    type("zombie_fire_charge", "vanilla.zombie") {
        val powerFormula = DoubleFactor(0.0..16.0) {1 + it * 0.5}
        itemTask {
            distance = 16.0
            before = ItemStack(Material.FIRE_CHARGE, 4)
            period = 80L
            function = task@{
                entity.location.playSound(Sound.ITEM_FIRECHARGE_USE, 1.0F, 1.0F)
                entity.launchProjectile(Fireball::class.java).apply {
                    velocity = entity.location.direction.normalize().multiply(0.65)
                    shooter = entity
                    yield = powerFormula.value(multiplier).toFloat()
                }
                entity.applyEffect(PotionEffectType.DAMAGE_RESISTANCE, 0, 12 * 20)
                entity.applyEffect(PotionEffectType.FIRE_RESISTANCE, 0, 12 * 20)
                entity.equipment.itemInMainHand.subtract(1)
                return@task true
            }
        }
    }
    type("zombie_flint_and_steel", "vanilla.zombie") {
        itemTask {
            distance = 5.0
            before = ItemStack(Material.FLINT_AND_STEEL)
            period = 80L
            function = task@{
                if (it.location.block.isLiquid || it.isInRain) return@task false
                it.location.run {
                    placeBlock(Material.FIRE)
                    playSound(Sound.ITEM_FLINTANDSTEEL_USE, 1.0F, 1.0F)
                    task {
                        delay = 40L
                        function = delay@{
                            if (block.type != Material.FIRE) return@delay false
                            placeBlock(Material.AIR)
                            playSound(Sound.BLOCK_FIRE_EXTINGUISH, 0.5F, 2.0F)
                            return@delay true
                        }
                    }
                }
                entity.applyEffect(PotionEffectType.FIRE_RESISTANCE, 0, 12 * 20)
                return@task true
            }
        }
    }
    type("zombie_ender_pearl", "vanilla.zombie") {
        itemTask {
            distance = 24.0
            before = ItemStack(Material.ENDER_PEARL)
            function = task@{
                if (entity.isInLava || entity.health <= 5.0) return@task false
                entity.location.playSound(Sound.ENTITY_ENDER_PEARL_THROW, 1.0F, 1.0F)
                entity.teleport(it)
                it.damage(0.0, entity)
                it.location.run {
                    playSound(Sound.ENTITY_GENERIC_BIG_FALL, 1.0F, 1.0F)
                    spawnParticle(Particle.PORTAL, 64, Vector(0.0, 0.0, 0.0),0.6)
                }
                entity.damage(5.0)
                return@task true
            }
        }
    }
}