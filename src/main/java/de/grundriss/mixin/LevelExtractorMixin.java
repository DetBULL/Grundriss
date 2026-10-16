package de.grundriss.mixin;

import de.grundriss.cam.Freecam;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Den eigenen Spieler in der Freecam sichtbar machen, damit man sieht, wo man steht. */
@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {
	@Shadow protected abstract EntityRenderState extractEntity(Entity entity, float partialTick);

	@Inject(method = "extractVisibleEntities", at = @At("RETURN"))
	private void grundriss$showPlayer(Camera camera, Frustum frustum, DeltaTracker deltaTracker, LevelRenderState renderState, CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null && mc.player != null && Freecam.isEnabled()) {
			TickRateManager tickRate = mc.level.tickRateManager();
			float pt = deltaTracker.getGameTimeDeltaPartialTick(!tickRate.isEntityFrozen(mc.player));
			renderState.entityRenderStates.add(this.extractEntity(mc.player, pt));
		}
	}
}
