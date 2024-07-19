@file:Suppress("MayBeConstant")

package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import cn.yanshiqwq.enhanced_mobs.Utils.all
import cn.yanshiqwq.enhanced_mobs.Utils.percentHeal
import cn.yanshiqwq.enhanced_mobs.api.ListenerApi
import cn.yanshiqwq.enhanced_mobs.api.TaskApi
import cn.yanshiqwq.enhanced_mobs.managers.TypeManager
import cn.yanshiqwq.enhanced_mobs.script.Config.applyVariantBoost
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.Mob
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitTask
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

        val mainKey = NamespacedKey(instance!!, "main_boost_type")
        val subKey = NamespacedKey(instance!!, "sub_boost_type")
        val multiplierKey = NamespacedKey(instance!!, "multiplier")

        const val MULTIPLIER_MAX_VALUE = 114514.0

        fun Entity.isEnhancedMob(): Boolean {
            if (this !is Mob) return false
            return instance!!.mobManager.has(this)
        }
        fun Entity.hasEnhancedMobData(): Boolean {
            if (this !is Mob) return false
            return with(persistentDataContainer) {
                Boolean.all(
                    has(multiplierKey),
                    has(mainKey),
                    has(subKey)
                )
            }
        }
        fun Mob.asEnhancedMob(multiplier: Double, mainBoostTypeKey: TypeManager.TypeKey, subBoostTypeKey: TypeManager.TypeKey?, isReload: Boolean = true): EnhancedMob {
            val mob = EnhancedMob(multiplier, this)
            mob.boost(mainBoostTypeKey, subBoostTypeKey)
            mob.applyVariantBoost()
            instance!!.mobManager.register(this.uniqueId, mob)
            if (isReload) percentHeal()
            return mob
        }
    }

    val tasks = mutableMapOf<TaskApi.TaskId, BukkitTask>()
    val listeners = mutableListOf<ListenerApi.Listener>()

    fun boost(mainBoostTypeKey: TypeManager.TypeKey, subBoostTypeKey: TypeManager.TypeKey?) {
        with(entity.persistentDataContainer) {
            set(mainKey, PersistentDataType.STRING, mainBoostTypeKey.value())
            set(subKey, PersistentDataType.STRING, subBoostTypeKey?.value() ?: "none")
        }

        instance!!.typeManager.getType(mainBoostTypeKey).function.invoke(this)
        subBoostTypeKey?.let {
            instance!!.typeManager.getType(it).function.invoke(this)
        }
    }

}