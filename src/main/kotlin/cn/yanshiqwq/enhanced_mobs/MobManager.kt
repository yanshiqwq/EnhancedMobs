package cn.yanshiqwq.enhanced_mobs

import cn.yanshiqwq.enhanced_mobs.Main.Companion.logger
import java.io.*
import java.util.*

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.EnhancedMobManager
 *
 * @author yanshiqwq
 * @since 2024/6/8 15:40
 */

class MobManager(path: String) {
    init {
        val file = File(path)
        if (!file.exists()) {
            file.createNewFile()
        } else {
            try {
                ObjectInputStream(FileInputStream(file)).use { stream ->
                    @Suppress("UNCHECKED_CAST")
                    map.putAll(stream.readObject() as Map<UUID, EnhancedMob>)
                }
            } catch (exception: EOFException) {
                logger.warning("Failed to load mobData file: $path")
            }

        }
    }

    private val map: MutableMap<UUID, EnhancedMob> = mutableMapOf()

    fun register(uuid: UUID, entity: EnhancedMob){
        map[uuid] = entity
    }
    fun remove(uuid: UUID){
        map.remove(uuid)
    }
    fun list(): List<UUID> {
        return map.keys.toList()
    }
    fun get(uuid: UUID): EnhancedMob? {
        return map[uuid]
    }
    fun save(path: String) {
        ObjectOutputStream(FileOutputStream(path)).use { stream ->
            stream.writeObject(map)
        }
    }
}