package de.grundriss.mixin;

import de.grundriss.cam.FreeCamera;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Die Kamera-Figur selbst wird nie gezeichnet. */
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
	@Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
	private void grundriss$shouldRender(Entity entity, Frustum frustum, double camX, double camY, double camZ, float partialTicks, CallbackInfoReturnable<Boolean> cir) {
		if (entity instanceof FreeCamera) {
			cir.setReturnValue(false);
		}
	}
}
