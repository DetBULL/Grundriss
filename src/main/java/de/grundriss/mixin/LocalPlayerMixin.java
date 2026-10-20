package de.grundriss.mixin;

import de.grundriss.cam.Freecam;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
	@Inject(method = "isControlledCamera", at = @At("HEAD"), cancellable = true)
	private void grundriss$controlledCamera(CallbackInfoReturnable<Boolean> cir) {
		if (Freecam.isEnabled()) cir.setReturnValue(true);
	}
}
