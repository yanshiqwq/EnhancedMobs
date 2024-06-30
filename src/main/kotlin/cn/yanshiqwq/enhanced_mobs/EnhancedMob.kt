package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.Utils.percentHeal
import cn.yanshiqwq.enhanced_mobs.api.ListenerApi
import cn.yanshiqwq.enhanced_mobs.managers.TypeManager
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.Mob
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitTask
import java.lang.NullPointerException
import java.util.*

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.EnhancedMob
 *
 * @author yanshiqwq
 * @since 2024/6/7 05:29
 */
class EnhancedMob(val multiplier: Double, val entity: Mob) {
    init {
        entity.persistentDataContainer.set(multiplierKey, PersistentDataType.DOUBLE, multiplier)
        instance!!.mobManager.register(entity.uniqueId, this)
    }

    companion object {
        val attributeUUID: UUID = UUID.fromString("a8d0bc44-1534-43f0-a594-f74c7c91bc59")
        val attributeName = "EnhancedMob Spawn Boost"

        val boostTypeKey = NamespacedKey(instance!!, "boost_type")
        val multiplierKey = NamespacedKey(instance!!, "multiplier")

        fun Entity.isEnhancedMob(): Boolean {
            if (this !is Mob) return false
            return instance!!.mobManager.has(this)
        }
        fun Entity.hasEnhancedMobData(): Boolean {
            if (this !is Mob) return false
            val container = persistentDataContainer
            return container.has(multiplierKey) && container.has(boostTypeKey)
        }
        fun Mob.asEnhancedMob(multiplier: Double, boostTypeKey: TypeManager.TypeKey, isReload: Boolean = true): EnhancedMob? {
            val mob = try {
                EnhancedMob(multiplier, this).apply { boost(boostTypeKey) }
            } catch (e: NullPointerException) { return null }
            instance!!.mobManager.register(this.uniqueId, mob)
            if (isReload) percentHeal()
            return mob
        }
    }

    val tasks = mutableMapOf<String, BukkitTask>()
    val listeners = mutableListOf<ListenerApi.Listener>()

    fun boost(boostTypeKey: TypeManager.TypeKey) {
        entity.persistentDataContainer.set(EnhancedMob.boostTypeKey, PersistentDataType.STRING, boostTypeKey.value())
        instance!!.typeManager.getType(boostTypeKey).function.invoke(this)
    }
}