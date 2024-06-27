package cn.yanshiqwq.enhanced_mobs.managers

import cn.yanshiqwq.enhanced_mobs.Main.Companion.instance
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import kotlin.io.path.isDirectory

/**
 * enhanced_mobs
 * cn.yanshiqwq.enhanced_mobs.managers.PackManager
 *
 * @author yanshiqwq
 * @since 2024/6/23 15:59
 */
class PackManager {
    data class Pack(val id: String, val typeMap: List<TypeManager.MobType> = listOf())

    private val packs = mutableListOf<Pack>()

    fun loadPacks(path: Path) {
        if (!path.isDirectory()) return

        val ktsFiles = try {
            Files.walk(path).use { stream ->
                stream.filter { it.toString().endsWith(".kts") }
                    .map { it.toFile() }
                    .toList()
            }
        } catch (e: IOException) {
            throw IOException("Failed to load packs in $path", e)
        }

        val engine: ScriptEngine = try {
            ScriptEngineManager().getEngineByExtension("kts")
        } catch (e: Exception) {
            throw IllegalStateException("Failed to load kts script engine", e)
        }

        ktsFiles.forEach {
            val relativePath = it.parentFile.relativeTo(path.toFile()).path
            val id = "${relativePath.replace(File.separatorChar, '.')}.${it.nameWithoutExtension}"
            val script = it.readText()

            try {
                register(engine.eval(script) as Pack)
            } catch (e: Exception) {
                throw IllegalStateException("Failed to eval kotlin script $id", e)
            }
        }
    }

    private fun register(pack: Pack) {
        packs.add(pack)
        instance!!.typeManager.loadTypes(pack)
        println("Registered pack: ${pack.id} (${pack.typeMap.size} types)")
    }
}
