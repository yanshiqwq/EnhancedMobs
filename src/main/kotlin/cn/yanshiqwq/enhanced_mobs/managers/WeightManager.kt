package cn.yanshiqwq.enhanced_mobs.managers

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.EntityType
import java.util.stream.Collectors

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.managers.WeightManager
 *
 * @author yanshiqwq
 * @since 2024/6/25 21:35
 */
class WeightManager(private val config: FileConfiguration) {
    private lateinit var weightMap: Map<List<EntityType>, Map<TypeManager.TypeKey, Int>>

    init {
        loadWeightMap()
    }

    private fun loadWeightMap() {
        val weightMap = mutableMapOf<List<EntityType>, Map<TypeManager.TypeKey, Int>>()

        // Read each entry in the YAML file
        val entries = config.getConfigurationSection("weightMap")?.getKeys(false) ?: return

        for (entryKey in entries) {
            val types = config.getStringList("weightMap.$entryKey.types").map {
                EntityType.valueOf(it.uppercase())
            }
            val weightsSection = config.getConfigurationSection("weightMap.$entryKey.weights")
            val weights = weightsSection?.getValues(false)?.entries?.stream()
                ?.collect(
                    Collectors.toMap(
                    { TypeManager.TypeKey(it.key) },
                    { it.value as Int }
                )) ?: emptyMap()

            weightMap[types] = weights
        }

        this.weightMap = weightMap
    }


    fun getWeightMap(): Map<List<EntityType>, Map<TypeManager.TypeKey, Int>> {
        return weightMap
    }
}