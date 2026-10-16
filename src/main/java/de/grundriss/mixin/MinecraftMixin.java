package de.grundriss.mixin;

import de.grundriss.cam.Freecam;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * In der Freecam baut, benutzt oder greift der Spieler nichts – Klicks gehen nur an die Planung
 * (die Klicks selbst werden in Grundriss#onStartTick abgefangen).
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
	private void grundriss$noAttack(CallbackInfoReturnable<Boolean> cir) {
		if (Freecam.isEnabled()) cir.setReturnValue(false);
	}

	@Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
	private void grundriss$noBreaking(boolean breaking, CallbackInfo ci) {
		if (Freecam.isEnabled()) ci.cancel();
	}

	@Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
	private void grundriss$noUse(CallbackInfo ci) {
		if (Freecam.isEnabled()) ci.cancel();
	}

	@Inject(method = "pickBlockOrEntity", at = @At("HEAD"), cancellable = true)
	private void grundriss$noPick(CallbackInfo ci) {
		if (Freecam.isEnabled()) ci.cancel();
	}
}
