package cn.yanshiqwq.enhanced_mobs

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.loot.LootContext
import org.bukkit.loot.LootTable
import kotlin.math.exp
import kotlin.random.Random
import kotlin.random.asKotlinRandom

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.data.LootTable
 *
 * @author yanshiqwq
 * @since 2024/6/22 16:04
 */

typealias EntryFunction = ((item: ItemStack, ctx: LootContext, random: LootTableX.Companion.RandomX) -> Unit)
typealias EntryCondition = ((ctx: LootContext, random: LootTableX.Companion.RandomX) -> Boolean)

@Suppress("unused", "MemberVisibilityCanBePrivate")
class LootTableX(private val key: NamespacedKey): LootTable {
    override fun getKey() = key
    override fun populateLoot(random: java.util.Random?, ctx: LootContext): MutableCollection<ItemStack> {
        val rand = if (random == null) RandomX() else RandomX(random)
        val lootList = getLootTableItems(ctx, rand)
        return lootList.toMutableList()
    }

    override fun fillInventory(inventory: Inventory, random: java.util.Random?, ctx: LootContext) {
        val lootList = populateLoot(random, ctx)
        for (item in lootList) {
            if (inventory.contents.size == inventory.size) return
            var slot: Int
            do {
                slot = random!!.nextInt(inventory.size)
            } while (inventory.getItem(slot) != null)
            inventory.setItem(slot, item)
        }
    }

    private val pools = mutableListOf<Pool>()

    fun addPool(pool: Pool) = this.apply { pools.add(pool) }

    private fun getLootTableItems(ctx: LootContext, random: RandomX): List<ItemStack> {
        val result = mutableListOf<ItemStack>()

        pools.asSequence()
            .filter { it.condition?.invoke(ctx, random) == true }
            .forEach { pool ->
                val rollCount = pool.rolls + random.nextUniform(0..pool.bonusRolls)
                repeat(rollCount) { result.addAll(pool.getPoolItems(ctx, random)) }
            }

        return result
    }

    data class Pool(
        val rolls: Int,
        val bonusRolls: Int,
        val entries: MutableList<Entry> = mutableListOf()
    ) : AbstractEntry() {
        fun addEntry(entry: Entry): Pool = this.apply { entries.add(entry) }
        fun getPoolItems(ctx: LootContext, random: RandomX): MutableList<ItemStack> {
            val list = mutableListOf<ItemStack>()

            // Entry process
            entries.forEach {
                list.add(it.process(it.item, ctx, random))
            }

            // Pool process
            list.forEach {
                this.process(it, ctx, random)
            }

            return list
        }
    }

    data class Entry(val item: ItemStack = ItemStack.empty()) : AbstractEntry() {
        constructor(type: Material): this(ItemStack(type))
    }

    abstract class AbstractEntry (
        var function: EntryFunction? = null,
        var condition: EntryCondition? = null
    ) {
        fun process(item: ItemStack, ctx: LootContext, random: RandomX): ItemStack {
            val itemClone = item.clone()
            if (condition?.invoke(ctx, random) == true) function?.invoke(itemClone, ctx, random)
            return itemClone
        }

        fun ItemStack.setCount(count: Int): ItemStack {
            return this.clone().apply { amount = count }
        }
        fun ItemStack.setLootingEnchant(ctx: LootContext, count: Int): ItemStack {
            return this.clone().apply { amount += ctx.lootingModifier * count }
        }
        fun ItemStack.setFurnaceSmelt(): ItemStack {
            return Bukkit.getRecipesFor(this)
                .asSequence()
                .filterIsInstance<FurnaceRecipe>()
                .map { it.result }
                .firstOrNull() ?: return this
        }

        fun ifKilledByPlayer(ctx: LootContext) = ctx.killer is Player

        fun ifRandomChance(
            random: RandomX,
            chance: Double,
        ) = random.ktRandom.nextDouble() in 0.0..chance

        fun ifRandomChanceWithLooting(
            ctx: LootContext,
            random: RandomX,
            chance: Double,
            lootingMultiplier: Double
        ) = ifRandomChance(random, chance + ctx.lootingModifier * lootingMultiplier)
    }

    companion object {
        class RandomX(val ktRandom: Random = Random) {
            constructor(random: java.util.Random): this(random.asKotlinRandom())

            fun nextUniform(range: IntRange): Int {
                return Random.nextInt(range.first, range.last + 1)
            }

            fun nextBinomial(n: Int, p: Double): Int {
                require(n >= 0) { "n must be non-negative" }
                require(p in 0.0..1.0) { "p must be between 0 and 1" }

                var count = 0
                for (i in 1..n) {
                    if (ktRandom.nextDouble() < p) {
                        count++
                    }
                }
                return count
            }

            fun nextPoisson(lambda: Double): Int {
                var events = 0
                val time = exp(-lambda)
                var product = ktRandom.nextDouble()

                while (product >= time) {
                    events++
                    product *= ktRandom.nextDouble()
                }

                return events
            }
        }
    }
}