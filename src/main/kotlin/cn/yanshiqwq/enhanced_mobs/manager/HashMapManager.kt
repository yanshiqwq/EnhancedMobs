package cn.yanshiqwq.enhanced_mobs.manager

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.manager.Manager
 *
 * @author yanshiqwq
 * @since 2024/10/2 下午6:16
 */
abstract class HashMapManager<K: Any, V: Any> {
    val entries = hashMapOf<K,V>()
    fun register(key: K, value: V) = entries.put(key,value)
    fun register(entry: Pair<K, V>) = entries.put(entry.first, entry.second)
    fun register(entry: Map.Entry<K, V>) = entries.put(entry.key, entry.value)
    fun register(map: Map<K, V>) = entries.putAll(map)
    fun unregister(key: K) = entries.remove(key)
    fun unregister(keys: Collection<K>) = keys.forEach { entries.remove(it) }
    fun getValue(key: K): V = entries[key] ?: throw NoSuchElementException("Key $key doesn't match any value in map")
    fun getKey(value: V): K = entries.filterValues { it == value }.keys.first()
    fun get(predicate: (Map.Entry<K, V>) -> Boolean): Map<K, V> = entries.filter(predicate)
    fun contains(key: K): Boolean = entries.containsKey(key)
}