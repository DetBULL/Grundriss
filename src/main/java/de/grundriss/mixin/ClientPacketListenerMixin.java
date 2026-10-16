package de.grundriss.mixin;

import de.grundriss.cam.Freecam;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Freecam beim Respawn / Dimensionswechsel ausschalten. */
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	@Inject(method = "handleRespawn", at = @At("TAIL"))
	private void grundriss$onRespawn(CallbackInfo ci) {
		Freecam.disable();
	}
}
