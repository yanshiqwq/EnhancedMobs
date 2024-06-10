package cn.yanshiqwq.enhanced_mobs

data class Pack(val id: String, val typeMap: Map<String, EnhancedMob.() -> Unit>) {
    fun implement(id: String, function: EnhancedMob.() -> Unit): EnhancedMob.() -> Unit {
        return {
            typeMap[id]?.let { it(this) } ?: Main.logger.warning("${Main.prefix} Cannot implement type $id.")
            function(this)
        }
    }
}