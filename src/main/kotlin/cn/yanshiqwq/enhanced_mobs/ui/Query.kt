package cn.yanshiqwq.enhanced_mobs.ui

import cn.yanshiqwq.enhanced_mobs.EnhancedMob
import cn.yanshiqwq.enhanced_mobs.manager.MobManager
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.event.player.PlayerInteractEntityEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.adaptPlayer
import taboolib.module.chat.ComponentText
import taboolib.module.chat.Components.text
import taboolib.platform.util.actionBar
import java.awt.Color

@PlatformSide(Platform.BUKKIT)
object Query {
    private var delay = false

    @SubscribeEvent
    fun onPlayerQuery(event: PlayerInteractEntityEvent) {
        delay = !delay
        if (delay) return // ???

        val player = event.player
        if (!player.isSneaking) return

        val entity = event.rightClicked.let {
            if (it is LivingEntity) it else null
        } ?: return

        // 添加调试日志
        player.actionBar("§a检测到实体交互: ${entity.type}")

        val mob = MobManager.get(entity.uniqueId) ?: run {
            player.actionBar("§c该实体未被增强")
            return
        }

        buildDisplayComponent(entity, mob).sendTo(adaptPlayer(player))
    }

    private val splitter = text(" | ").color(Color.GRAY)

    private fun buildDisplayComponent(entity: LivingEntity, mob: EnhancedMob): ComponentText {
        val baseComponent = buildBaseComponent(entity, mob)
        val mobAttributes = listOf(
            Attribute.MAX_HEALTH to "❤",
            Attribute.ARMOR to "🛡",
            Attribute.ATTACK_DAMAGE to "🗡",
            Attribute.MOVEMENT_SPEED to "⚡"
        )
        return mobAttributes.fold(baseComponent) { acc, (attr, icon) ->
            acc.append(splitter).append(createAttributeComponent(entity, attr, icon))
        }
    }

    private fun buildBaseComponent(entity: LivingEntity, mob: EnhancedMob): ComponentText {
        return text(entity.name)
            .append(text(" (${mob.typeId})").color(Color.GRAY))
    }

    private fun createAttributeComponent(
        entity: LivingEntity,
        attribute: Attribute,
        icon: String
    ): ComponentText {
        val value = entity.getAttribute(attribute)?.value ?: 0.0
        return text(icon).color(
            when (attribute) {
                Attribute.MAX_HEALTH -> Color.RED
                Attribute.ARMOR -> Color.GRAY
                Attribute.ATTACK_DAMAGE -> Color.YELLOW
                Attribute.MOVEMENT_SPEED -> Color.CYAN
                else -> Color.GRAY
            }
        ).append(" %.2f".format(value))
    }
}