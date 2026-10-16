package de.grundriss.mixin;

import de.grundriss.cam.FreeCamera;
import de.grundriss.cam.Freecam;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
	/** Die Maus dreht in der Freecam die Kamera statt den Spieler. */
	@Inject(method = "turn", at = @At("HEAD"), cancellable = true)
	private void grundriss$turn(double yRot, double xRot, CallbackInfo ci) {
		FreeCamera cam = Freecam.camera();
		if (Freecam.isEnabled() && cam != null && (Object) this == Minecraft.getInstance().player) {
			cam.turn(yRot, xRot);
			ci.cancel();
		}
	}

	/** Kamera schiebt keine Mobs und wird nicht geschoben. */
	@Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
	private void grundriss$push(Entity other, CallbackInfo ci) {
		if (other instanceof FreeCamera || (Object) this instanceof FreeCamera) {
			ci.cancel();
		}
	}
}
