package de.grundriss;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.sdl.SDLScancode;

/** Alle Tasten. Lassen sich unter Optionen → Steuerung → "Grundriss" ändern. */
public final class Keys {
	private Keys() {}

	private static final KeyMapping.Category CATEGORY =
			KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Grundriss.MOD_ID, "steuerung"));

	public static KeyMapping FREECAM, TOOL, VISIBLE, UNDO, AIR_MODE, LAYER_UP, LAYER_DOWN, LAYER_OFF;

	static void register() {
		FREECAM = key("freecam", SDLScancode.SDL_SCANCODE_F6);
		TOOL = key("tool", SDLScancode.SDL_SCANCODE_G);
		VISIBLE = key("visible", SDLScancode.SDL_SCANCODE_H);
		UNDO = key("undo", SDLScancode.SDL_SCANCODE_BACKSPACE);
		AIR_MODE = key("air_mode", SDLScancode.SDL_SCANCODE_LALT);
		LAYER_UP = key("layer_up", SDLScancode.SDL_SCANCODE_PAGEUP);
		LAYER_DOWN = key("layer_down", SDLScancode.SDL_SCANCODE_PAGEDOWN);
		LAYER_OFF = key("layer_off", SDLScancode.SDL_SCANCODE_END);
	}

	private static KeyMapping key(String name, int scancode) {
		return KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.grundriss." + name, InputConstants.Type.KEYBOARD, scancode, CATEGORY));
	}
}
