package cn.yanshiqwq.enhanced_mobs.script

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.Utils.byChance
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
                baseAttribute(GENERIC_MAX_HEALTH, 24.0)
                baseAttribute(GENERIC_ARMOR, 2.0)
                entity.isImmuneToZombification = true
            }

            is PiglinBrute -> {
                baseAttribute(GENERIC_MAX_HEALTH, 72.0)
                baseAttribute(GENERIC_ARMOR, 6.0)
                baseAttribute(GENERIC_ATTACK_KNOCKBACK, 0.35)
                baseAttribute(GENERIC_KNOCKBACK_RESISTANCE, 0.35)
                entity.isImmuneToZombification = true
            }
        }
    }
    private val block: WeightDslBuilder.EntityPropertiesBuilder.() -> Unit = {
        entity(EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK, EntityType.DROWNED, EntityType.ZOMBIFIED_PIGLIN) {
            weight(
                "extend.zombie_leader" to 1,
                "extend.strength_cloud" to 1,
                "extend.totem" to 3,
                "extend.tnt" to 3,
                "extend.lava" to 5,
                "extend.flint_and_steel" to 5,
                "extend.ender_pearl" to 3,
                "extend.anvil" to 2,
                "extend.fire_charge" to 3,
                "extend.shield" to 5
            )
        }
        entity(EntityType.DROWNED) {
            weight(
                "extend.reduce_air" to 10
            )
        }
        entity(EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON) {
            weight(
                "extend.iron_sword" to 5,
                "extend.frost" to 2
            )
        }
        entity(EntityType.SPIDER, EntityType.CAVE_SPIDER) {
            weight(
                "extend.spider_cobweb" to 2
            )
        }
        entity(EntityType.CREEPER) {
            weight(
                "extend.creeper_charged" to 2
            )
        }
    }
    private const val BOOST_CHANCE = 0.25
    fun getWeightMap() = WeightDslBuilder().loadWeightMap(block).byChance(BOOST_CHANCE)
    fun getMainTypeKey(type: EntityType) = when (type) {
        in Tags.Entity.zombies -> TypeManager.TypeKey("vanilla", "zombie")
        in Tags.Entity.skeletons -> TypeManager.TypeKey("vanilla", "skeleton")
        in Tags.Entity.spiders -> TypeManager.TypeKey("vanilla", "spider")
        in Tags.Entity.creepers -> TypeManager.TypeKey("vanilla", "creeper")
        EntityType.WITCH -> TypeManager.TypeKey("vanilla", "witch")
        EntityType.PILLAGER -> TypeManager.TypeKey("vanilla", "pillager")
        else -> TypeManager.TypeKey.mainDefault
    }
}