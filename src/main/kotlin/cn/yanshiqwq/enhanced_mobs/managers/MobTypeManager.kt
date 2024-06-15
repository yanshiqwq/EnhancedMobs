package cn.yanshiqwq.enhanced_mobs.managers

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.Main.Companion.logger
import cn.yanshiqwq.enhanced_mobs.data.Pack
import org.bukkit.entity.EntityType
import kotlin.random.Random

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.managers.MobManager
 *
 * @author yanshiqwq
 * @since 2024/6/8 15:39
 */

class MobTypeManager {
    companion object {
        fun getRandomTypeId(type: EntityType): TypeId {
            val weightMap = when (type) {
                EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK, EntityType.DROWNED, EntityType.ZOMBIFIED_PIGLIN -> mapOf(
                    TypeId("vanilla", "zombie") to 80,
                    TypeId("extend", "zombie_strength_cloud") to 2,
                    TypeId("extend", "zombie_totem") to 2,
                    TypeId("extend", "zombie_tnt") to 2,
                    TypeId("extend", "zombie_lava") to 2,
                    TypeId("extend", "zombie_flint_and_steel") to 2,
                    TypeId("extend", "zombie_ender_pearl") to 2,
                    TypeId("extend", "zombie_shield") to 2,
                )
                EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON -> mapOf(
                    TypeId("vanilla", "skeleton") to 80,
                    TypeId("extend", "skeleton_iron_sword") to 10,
                )
                EntityType.SPIDER, EntityType.CAVE_SPIDER -> mapOf(
                    TypeId("vanilla", "spider") to 80
                )
                EntityType.CREEPER -> mapOf(
                    TypeId("vanilla", "creeper") to 80
                )
                else -> mapOf(
                    TypeId("vanilla", "generic") to 80
                )
            }
            val totalWeight = weightMap.values.sum()
            var randomValue = Random.nextInt(totalWeight)

            for ((typeId, weight) in weightMap) {
                randomValue -= weight
                if (randomValue < 0) {
                    return typeId
                }
            }

            return TypeId("vanilla", "generic")
        }
    }
    data class TypeId(val pack: String, val mob: String) {
        constructor(id: String) : this(id.split(".")[0], id.split(".")[1])

        fun value(): String {
            return "$pack.$mob"
        }
    }
    private val packMap: MutableMap<String, Set<String>> = mutableMapOf()
    private val typeMap: MutableMap<TypeId, (EnhancedMob) -> Unit> = mutableMapOf()
    fun loadPacks(vararg packs: Pack) {
        for (pack in packs) {
            logger.info("Loading pack: ${pack.id}")
            packMap[pack.id] = pack.typeMap.keys
            for (mob in pack.typeMap) {
                logger.info("   Loading mobType: ${mob.key}")
                typeMap[TypeId(pack.id, mob.key)] = mob.value
            }
        }
    }

    fun hasTypeId(id: TypeId): Boolean {
        return (typeMap[id] != null)
    }

    fun queryTypeFunction(id: TypeId): ((EnhancedMob) -> Unit)? {
        return typeMap[id]
    }

    fun listPackIds(): MutableSet<String> {
        return packMap.keys
    }

    fun listTypeIds(packId: String): ArrayList<TypeId> {
        val typeNames = packMap[packId]?.toTypedArray() ?: return arrayListOf()
        val typeIds = ArrayList<TypeId>()
        typeNames.forEach {
            typeIds.add(TypeId(packId, it))
        }
        return typeIds
    }
}