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
            ),
            chance = 0.1
        )
        entity(
            types = listOf(EntityType.DROWNED),
            items = mapOf(
                "extend.reduce_air" to 1
            ),
            chance = 0.1
        )
        entity(
            types = listOf(EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON),
            items = mapOf(
                "extend.frost" to 1
            ),
            chance = 0.02
        )
        entity(
            types = listOf(EntityType.SPIDER, EntityType.CAVE_SPIDER),
            items = mapOf(
                "extend.spider_cobweb" to 1
            ),
            chance = 0.02
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