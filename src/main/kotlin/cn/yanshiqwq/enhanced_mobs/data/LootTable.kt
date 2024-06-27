package cn.yanshiqwq.enhanced_mobs.data

import cn.yanshiqwq.enhanced_mobs.LootTableX
import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.Utils.all
import cn.yanshiqwq.enhanced_mobs.Utils.isOnFire
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Mob

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.data.LootTable
 *
 * @author yanshiqwq
 * @since 2024/6/22 16:34
 */
object LootTable {
    fun Mob.applyLootTableX(multiplier: Double) {
        val lootTable = when (type) {
            in Tags.Entity.zombies -> zombieLoot(multiplier)
//            in Tags.Entity.skeletons -> skeletonLoot(multiplier)
//            in Tags.Entity.spiders -> spiderLoot(multiplier)
//            in Tags.Entity.creepers -> creeperLoot(multiplier)
            else -> return
        }
        setLootTable(lootTable)
    }

    val zombieLoot: (Double) -> LootTableX = lambda@{
        return@lambda LootTableX(NamespacedKey(instance!!, "zombie"))
            .addPool(LootTableX.Pool(rolls = 1, bonusRolls = 0)
                .addEntry(LootTableX.Entry(Material.ROTTEN_FLESH).apply {
                    function = { item, ctx, random ->
                        item.setCount(random.nextUniform(8..12))
                            .setLootingEnchant(ctx, random.nextUniform(0..1))
                    }
                    condition = { ctx, _ ->
                        ifKilledByPlayer(ctx)
                    }
                })
            )
            .addPool(LootTableX.Pool(rolls = 1, bonusRolls = 0)
                .addEntry(LootTableX.Entry(Material.CARROT))
                .addEntry(LootTableX.Entry(Material.BREAD))
                .addEntry(LootTableX.Entry(Material.POTATO).apply {
                    function = { item, _, _ ->
                        item.setFurnaceSmelt()
                    }
                    condition = { ctx, _ ->
                        ctx.lootedEntity?.isOnFire() == true
                    }
                })
                .apply {
                    function = { item, _, random ->
                        item.setCount(random.nextUniform(3..5))
                    }
                    condition = { ctx, random ->
                        Boolean.all(
                            ifKilledByPlayer(ctx),
                            ifRandomChanceWithLooting(ctx, random, 0.65, 0.15)
                        )
                    }
                }
            )
    }
}