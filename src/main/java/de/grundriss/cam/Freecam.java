package de.grundriss.cam;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Input;
import org.jetbrains.annotations.Nullable;

/** Ein- und Ausschalten der Freecam. */
public final class Freecam {
	private Freecam() {}

	private static final Minecraft MC = Minecraft.getInstance();
	private static final int CAMERA_ENTITY_ID = -2741;

	private static boolean enabled = false;
	private static @Nullable FreeCamera camera;
	private static @Nullable CameraType rememberedCameraType;
	private static @Nullable ClientInput playerInput;
	private static @Nullable ClientInput dummyInput;

	public static boolean isEnabled() {
		return enabled;
	}

	public static @Nullable FreeCamera camera() {
		return camera;
	}

	public static void toggle() {
		if (enabled) disable(); else enable();
	}

	private static void enable() {
		if (MC.player == null || MC.level == null) return;

		rememberedCameraType = MC.options.getCameraType();
		if (MC.gameRenderer.mainCamera().isDetached()) {
			MC.options.setCameraType(CameraType.FIRST_PERSON);
		}
		MC.smartCull = false;

		// Spieler bleibt stehen: er bekommt eine leere Eingabe
		playerInput = MC.player.input;
		dummyInput = new ClientInput();
		dummyInput.keyPresses = new Input(false, false, false, false, false, false, false);
		MC.player.input = dummyInput;

		camera = new FreeCamera(CAMERA_ENTITY_ID);
		camera.moveToEntity(MC.player);
		camera.spawn();
		MC.setCameraEntity(camera);
		enabled = true;
		MC.player.sendOverlayMessage(Component.translatable("grundriss.msg.freecam_on"));
	}

	public static void disable() {
		if (!enabled) return;
		enabled = false;
		MC.smartCull = true;

		if (MC.player != null) {
			MC.setCameraEntity(MC.player);
			if (playerInput != null) MC.player.input = playerInput;
			MC.player.sendOverlayMessage(Component.translatable("grundriss.msg.freecam_off"));
		}
		if (camera != null && MC.level != null) {
			camera.despawn();
		}
		camera = null;
		playerInput = null;
		dummyInput = null;

		if (rememberedCameraType != null) {
			MC.options.setCameraType(rememberedCameraType);
			rememberedCameraType = null;
		}
	}

	/** Jeden Tick: sicherstellen, dass der Spieler keine Eingaben bekommt. */
	public static void preTick() {
		if (!enabled) return;
		if (MC.player == null || MC.level == null || camera == null) {
			enabled = false;
			camera = null;
			return;
		}
		if (dummyInput != null) MC.player.input = dummyInput;
	}
}
