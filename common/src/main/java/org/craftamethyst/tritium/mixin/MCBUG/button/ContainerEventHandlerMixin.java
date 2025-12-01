package org.craftamethyst.tritium.mixin.MCBUG.button;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContainerEventHandler.class)
public interface ContainerEventHandlerMixin {

    @Unique
    private static boolean tritium$shouldClearFocus(GuiEventListener focused) {
        return focused instanceof AbstractButton || focused instanceof AbstractSliderButton;
    }

    @Unique
    private void tritium$clearFocusIfNeeded() {
        ContainerEventHandler self = (ContainerEventHandler) this;
        if (TritiumConfigBase.Fixes.ButtonFix.buttonFix && tritium$shouldClearFocus(self.getFocused())) {
            self.setFocused(null);
        }
    }

    @Inject(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/events/ContainerEventHandler;setDragging(Z)V"))
    private void onMouseInteraction(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        tritium$clearFocusIfNeeded();
    }

    @Inject(method = "mouseReleased", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/events/ContainerEventHandler;setDragging(Z)V"))
    private void onMouseRelease(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        tritium$clearFocusIfNeeded();
    }
}