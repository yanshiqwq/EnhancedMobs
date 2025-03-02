package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.Utils.createNamespacedKey
import cn.yanshiqwq.enhanced_mobs.Utils.equip
import cn.yanshiqwq.enhanced_mobs.Utils.hasKey
import cn.yanshiqwq.enhanced_mobs.Utils.heal
import cn.yanshiqwq.enhanced_mobs.Utils.name
import cn.yanshiqwq.enhanced_mobs.dsl.AttributeBuilder
import cn.yanshiqwq.enhanced_mobs.dsl.AttributeBuilder.Companion.base
import cn.yanshiqwq.enhanced_mobs.dsl.EquipmentBuilder
import cn.yanshiqwq.enhanced_mobs.dsl.SkillApi
import cn.yanshiqwq.enhanced_mobs.manager.LevelCurveManager
import cn.yanshiqwq.enhanced_mobs.manager.LevelCurveManager.applyLevelCurve
import cn.yanshiqwq.enhanced_mobs.manager.MobManager
import cn.yanshiqwq.enhanced_mobs.manager.MobTypeManager
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.Mob
import org.bukkit.persistence.PersistentDataType
import java.util.*

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.EnhancedMob
 *
 * @author yanshiqwq
 * @since 2024/8/21 下午 10:34
 */
/**
 * 本插件特有的怪物实例
 */
class EnhancedMob(
    private val uuid: UUID,
    val typeId: String,
    private val levelCurveId: String,
    val level: Int
) {
    /**
     * 此怪物类对应的怪物实体
     *
     * @return 若实体死亡或被移除则为 null
     */
    val entity
        get() = Bukkit.getEntity(uuid) as? Mob
    
    val levelCurve
        get() = LevelCurveManager.getValue(levelCurveId)
    
    val type
        get() = MobTypeManager.getValue(typeId)

    var toughnessInstance: SkillApi.ToughnessMechanism =
        SkillApi.ToughnessMechanism(this)

    companion object {
        /**
         * 用于在持久数据容器中存储怪物数据的键
         */
        private val MobDataKey = object {
            val TYPE_ID_KEY = createNamespacedKey("type_id")
            val LEVEL_KEY = createNamespacedKey("level")
            val LEVEL_CURVE_KEY = createNamespacedKey("level_curve")
            val INTALIZE_KEY = createNamespacedKey("is_intalized")
        }
        
        /**
         * 构造一个怪物实例
         *
         * @param mob 代表怪物实体
         * @param type 怪物类型
         * @param level 怪物等级
         */
        fun buildMob(mob: Mob, type: String, level: Int, levelCurve: String, block: EnhancedMob.() -> Unit = {}) =
            EnhancedMob(mob.uniqueId, type, levelCurve, level).apply {
                // 应用等级曲线
                entity?.applyLevelCurve(levelCurve, level)
                
                // 应用类型代码块
                MobTypeManager.getValue(type).block.invoke(this)
                
                // 调用回调函数
                block.invoke(this)
                
                // 如果未被初始化则初始化
                if (entity?.hasKey(MobDataKey.INTALIZE_KEY) == true) return@apply
                initalize()
            }
        
        private fun EnhancedMob.initalize() = entity?.run{
            // 持久化数据
            persistentDataContainer.apply {
                set(MobDataKey.TYPE_ID_KEY, PersistentDataType.STRING, typeId)
                set(MobDataKey.LEVEL_KEY, PersistentDataType.INTEGER, level)
                set(MobDataKey.LEVEL_CURVE_KEY, PersistentDataType.STRING, LevelCurveManager.getKey(levelCurve))
                set(MobDataKey.INTALIZE_KEY, PersistentDataType.BOOLEAN, true)
            }

            // 回血
            heal()
        }
        
        /**
         * 尝试将实体加载为怪物实例
         * 用于插件重启后重新加载数据
         *
         * @param entity 需要加载的实体
         */
        fun load(entity: Entity) {
            // 如果已经加载过则取消加载
            if (MobManager.has(entity.uniqueId)) return

            val mob = entity as? Mob ?: return
            val container = mob.persistentDataContainer
            val typeId = container.get(MobDataKey.TYPE_ID_KEY, PersistentDataType.STRING) ?: return
            val level = container.get(MobDataKey.LEVEL_KEY, PersistentDataType.INTEGER) ?: return
            val levelCurveId = container.get(MobDataKey.LEVEL_CURVE_KEY, PersistentDataType.STRING) ?: return
            try {
                MobManager.register(buildMob(mob, typeId, level, levelCurveId))
            } catch (e: Exception) {
                throw RuntimeException("Failed casting entity ${mob.uniqueId} (${mob.type}) to EnhancedMob", e)
            }
        }
    }
    
    fun name(name: String) = entity?.name(name)
    
    inline fun base(block: AttributeBuilder.() -> Unit)= entity?.base(block)
    
    inline fun equip(block: EquipmentBuilder.() -> Unit)= entity?.equip(block)
    
    inline fun mechanic(block: Mob.() -> Unit)= entity?.run(block)
}