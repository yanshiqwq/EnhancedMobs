package cn.yanshiqwq.enhanced_mobs.manager

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.manager.ListManager
 *
 * @author yanshiqwq
 * @since 2024/10/2 下午6:22
 */
abstract class HashSetManager<T: Any> {
    val entries = hashSetOf<T>()
    fun register(element: T) = entries.add(element)
    fun register(elements: Collection<T>) = this.entries.addAll(elements)
    fun unregister(element: T) = entries.remove(element)
    fun unregister(elements: HashSet<T>) = this.entries.removeAll(elements)
    fun contains(element: T): Boolean = entries.contains(element)
    fun get(predicate: (T) -> Boolean) = entries.find(predicate)
    fun getAll(predicate: (T) -> Boolean) = entries.filter(predicate)
}