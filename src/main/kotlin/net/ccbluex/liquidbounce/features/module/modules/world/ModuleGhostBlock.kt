package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.config.Choice
import net.ccbluex.liquidbounce.config.ChoiceConfigurable
import net.ccbluex.liquidbounce.event.events.KeyEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.event.repeatable
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.block.getState
import net.minecraft.block.Blocks
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import org.lwjgl.glfw.GLFW


/**
 * GhostBlock Module
 *
 * Remove blocks client-side.
 */
object ModuleGhostBlock : Module("GhostBlock", Category.WORLD, lockedState = true) {

    private var isKeyHeld = false
    private var isKeyPressed = false

    val modes = choices("TriggerMode", Single, arrayOf(Single, Hold))

    val repeatable = repeatable {
        if (!isKeyHeld) {
            return@repeatable
        }
        println("a")
        val crosshairTarget = mc.crosshairTarget
        if (!(crosshairTarget is BlockHitResult && crosshairTarget.type == HitResult.Type.BLOCK)) {
            return@repeatable
        }
        val blockState = crosshairTarget.blockPos.getState() ?: return@repeatable
        if (blockState.isAir) {
            return@repeatable
        }

        when (modes.activeChoice) {
            Single -> {
                if (isKeyPressed) {
                    mc.world!!.setBlockState(crosshairTarget.blockPos, Blocks.AIR.defaultState)
                    isKeyPressed = false
                }
            }
            Hold -> {
                mc.world!!.setBlockState(crosshairTarget.blockPos, Blocks.AIR.defaultState)
                waitTicks(Hold.delay)
            }
        }

    }

    @Suppress("unused")
    val keyHandler = handler<KeyEvent> { ev ->
        if (ev.key.keyCode == bind) {
            isKeyHeld = ev.action == GLFW.GLFW_PRESS || ev.action == GLFW.GLFW_REPEAT
            isKeyPressed = ev.action == GLFW.GLFW_PRESS
        }
    }


    override fun enable() {
        isKeyHeld = false
        isKeyPressed = false
    }

    override fun disable() {
        isKeyHeld = false
        isKeyPressed = false
    }

    object Single : Choice("Single") {
        override val parent: ChoiceConfigurable<Choice>
            get() = modes
    }

    object Hold : Choice("Hold") {
        override val parent: ChoiceConfigurable<Choice>
            get() = modes

        val delay by int("Delay", 2, 0..20, "ticks")
    }
}
