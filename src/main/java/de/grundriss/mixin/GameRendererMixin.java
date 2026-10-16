package de.grundriss.mixin;

import de.grundriss.cam.Freecam;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	/** Keine Hand in der Freecam. */
	@Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
	private void grundriss$hideHand(CallbackInfo ci) {
		if (Freecam.isEnabled()) ci.cancel();
	}

	/** Kein normaler Block-Rahmen – die Mod zeichnet ihre eigene Vorschau. */
	@Inject(method = "shouldRenderBlockOutline", at = @At("HEAD"), cancellable = true)
	private void grundriss$noOutline(CallbackInfoReturnable<Boolean> cir) {
		if (Freecam.isEnabled()) cir.setReturnValue(false);
	}
}
