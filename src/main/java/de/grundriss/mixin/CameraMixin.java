package de.grundriss.mixin;

import de.grundriss.cam.FreeCamera;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Beim Umschalten springt die Augenhöhe sofort, statt langsam zu gleiten. */
@Mixin(Camera.class)
public class CameraMixin {
	@Shadow private Entity entity;
	@Shadow private float eyeHeightOld;
	@Shadow private float eyeHeight;

	@Inject(method = "setEntity", at = @At("HEAD"))
	private void grundriss$setEntity(Entity newEntity, CallbackInfo ci) {
		if (newEntity == null || this.entity == null) return;
		if (newEntity instanceof FreeCamera || this.entity instanceof FreeCamera) {
			this.eyeHeightOld = this.eyeHeight = newEntity.getEyeHeight();
		}
	}
}
