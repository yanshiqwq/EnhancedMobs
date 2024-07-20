package cn.yanshiqwq.enhanced_mobs.script

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.api.MobApi.baseAttribute
import cn.yanshiqwq.enhanced_mobs.data.Tags
import cn.yanshiqwq.enhanced_mobs.dsl.WeightDslBuilder
import cn.yanshiqwq.enhanced_mobs.managers.TypeManager
import org.bukkit.attribute.Attribute.*
import org.bukkit.entity.*
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.script.Config
 *
 * @author yanshiqwq
 * @since 2024/6/30 下午4:17
 */
object Config {
    fun EnhancedMob.applyVariantBoost() {
        when (this.entity) {
            is Stray, is WitherSkeleton -> {
                baseAttribute(GENERIC_MAX_HEALTH, 24.0)
                baseAttribute(GENERIC_ARMOR, 2.0)
                baseAttribute(GENERIC_ATTACK_DAMAGE, 4.0)
                baseAttribute(GENERIC_FOLLOW_RANGE, 24.0)
            }

            is Husk, is Drowned -> {
                baseAttribute(GENERIC_MAX_HEALTH, 24.0)
                baseAttribute(GENERIC_ARMOR, 4.0)
                baseAttribute(GENERIC_ATTACK_DAMAGE, 4.0)
                baseAttribute(GENERIC_FOLLOW_RANGE, 42.0)
            }

            is Creeper -> if (entity.isPowered) {
                baseAttribute(GENERIC_MAX_HEALTH, 24.0)
                baseAttribute(GENERIC_ARMOR, 6.0)
                baseAttribute(GENERIC_FOLLOW_RANGE, 24.0)
                baseAttribute(GENERIC_KNOCKBACK_RESISTANCE, 0.35)
            }

            is Giant -> {
                baseAttribute(GENERIC_MAX_HEALTH, 196.0)
                baseAttribute(GENERIC_ARMOR, 6.0)
                baseAttribute(GENERIC_ATTACK_DAMAGE, 13.0)
                baseAttribute(GENERIC_MOVEMENT_SPEED, 0.4)
                baseAttribute(GENERIC_FOLLOW_RANGE, 128.0)
                baseAttribute(GENERIC_KNOCKBACK_RESISTANCE, 0.65)
                baseAttribute(ZOMBIE_SPAWN_REINFORCEMENTS, 0.65)
                entity.addPotionEffect(PotionEffect(PotionEffectType.JUMP, Int.MAX_VALUE, 3, true, false))
            }

            is Piglin -> {
                baseAttribute(GENERIC_ARMOR, 2.0)
            }

            is PiglinBrute -> {
                baseAttribute(GENERIC_MAX_HEALTH, 48.0)
                baseAttribute(GENERIC_ARMOR, 4.0)
                baseAttribute(GENERIC_ATTACK_KNOCKBACK, 0.35)
                baseAttribute(GENERIC_ATTACK_DAMAGE, -1.0)
                baseAttribute(GENERIC_KNOCKBACK_RESISTANCE, 0.35)
                entity.isImmuneToZombification = true
            }

            is Vindicator -> baseAttribute(GENERIC_ATTACK_DAMAGE, -2.0)

            is Ravager -> baseAttribute(GENERIC_ATTACK_DAMAGE, 7.0)

            is Vex -> {
                baseAttribute(GENERIC_MAX_HEALTH, 5.0)
                baseAttribute(GENERIC_ATTACK_DAMAGE, 1.0)
            }
        }
    }
    private val block: WeightDslBuilder.EntityPropertiesBuilder.() -> Unit = {
        entity(
            types = listOf(EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK, EntityType.DROWNED, EntityType.ZOMBIFIED_PIGLIN),
            items = mapOf(
                "extend.zombie_leader" to 1,
                "extend.strength_cloud" to 1,
                "extend.totem" to 3,
                "extend.tnt" to 1,
                "extend.lava" to 1,
                "extend.flint_and_steel" to 1,
                "extend.ender_pearl" to 1,
                "extend.anvil" to 1,
                "extend.fire_charge" to 1,
                "extend.shield" to 2
            )
        )
        entity(
            types = listOf(EntityType.DROWNED),
            items = mapOf(
                "extend.reduce_air" to 10
            )
        )
        entity(
            types = listOf(EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON),
            items = mapOf(
                "extend.iron_sword" to 5,
                "extend.frost" to 1
            )
        )
        entity(
            types = listOf(EntityType.SPIDER, EntityType.CAVE_SPIDER),
            items = mapOf(
                "extend.spider_cobweb" to 2
            )
        )
        entity(
            types = listOf(EntityType.CREEPER),
            items = mapOf(
                "extend.creeper_charged" to 2
            )
        )
    }
    fun getWeightMap() = WeightDslBuilder().loadWeightMap(block)
    fun getMainTypeKey(type: EntityType) = when (type) {
        in Tags.Entity.zombies -> TypeManager.TypeKey("vanilla", "zombie")
        in Tags.Entity.skeletons -> TypeManager.TypeKey("vanilla", "skeleton")
        in Tags.Entity.spiders -> TypeManager.TypeKey("vanilla", "spider")
        in Tags.Entity.creepers -> TypeManager.TypeKey("vanilla", "creeper")
        EntityType.WITCH -> TypeManager.TypeKey("vanilla", "witch")
        EntityType.PILLAGER -> TypeManager.TypeKey("vanilla", "pillager")
        EntityType.VINDICATOR -> TypeManager.TypeKey("vanilla", "vindicator")
        EntityType.RAVAGER -> TypeManager.TypeKey("vanilla", "ravager")
        EntityType.ENDERMAN -> TypeManager.TypeKey("vanilla", "enderman")
        else -> TypeManager.TypeKey.mainDefault
    }
}