package org.craftamethyst.tritium.mixin.MCBUG.button;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetMixin {

    @Shadow
    public abstract boolean isFocused();

    @Shadow
    public abstract void setFocused(boolean focused);

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    public void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!TritiumConfigBase.Fixes.ButtonFix.buttonFix) {
            return;
        }
        AbstractWidget self = (AbstractWidget) (Object) this;
        if (self instanceof EditBox) {
            return;
        }
        if (this.isFocused()) {
            this.setFocused(false);
        }
    }

    @Inject(method = "mouseReleased", at = @At("RETURN"))
    public void onMouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!TritiumConfigBase.Fixes.ButtonFix.buttonFix) {
            return;
        }
        AbstractWidget self = (AbstractWidget) (Object) this;
        if (self instanceof EditBox) {
            return;
        }
        if (this.isFocused()) {
            this.setFocused(false);
        }
    }
}