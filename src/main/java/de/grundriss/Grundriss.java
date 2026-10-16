package de.grundriss;

import de.grundriss.cam.Freecam;
import de.grundriss.plan.PlanColor;
import de.grundriss.plan.PlanController;
import de.grundriss.plan.PlanHud;
import de.grundriss.plan.PlanRenderer;
import de.grundriss.plan.PlanStore;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.client.player.ClientHotbarScrollEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Grundriss implements ClientModInitializer {
	public static final String MOD_ID = "grundriss";
	public static final Logger LOGGER = LoggerFactory.getLogger("Grundriss");

	@Override
	public void onInitializeClient() {
		Keys.register();

		ClientTickEvents.START_CLIENT_TICK.register(Grundriss::onStartTick);
		ClientTickEvents.END_CLIENT_TICK.register(PlanStore::tick);

		ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> {
			Freecam.disable();
			PlanController.reset();
			PlanStore.onDisconnect();
		});
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> PlanStore.save());

		// Mausrad in der Freecam: Farbe wechseln, mit Alt den Abstand im Luft-Modus
		ClientHotbarScrollEvents.ALLOW.register((inventory, currentSlot, newSlot, xOffset, yOffset) -> {
			if (!Freecam.isEnabled()) return true;
			if (yOffset == 0) return false;
			int dir = yOffset > 0 ? 1 : -1;
			if (Keys.AIR_MODE.isDown()) {
				PlanController.changeAirDistance(dir);
			} else {
				PlanController.cycleColor(-dir);
			}
			return false;
		});

		LevelRenderEvents.BEFORE_TRANSLUCENT_TERRAIN.register(PlanRenderer::render);
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "info"), PlanHud::render);

		LOGGER.info("Grundriss geladen – F6 startet die Planungs-Kamera.");
	}

	/**
	 * Läuft vor der Tastenverarbeitung von Minecraft. Klicks und Zahlentasten werden hier
	 * "abgefangen", damit der Spieler in der Freecam weder Blöcke abbaut noch den Hotbar-Slot wechselt.
	 */
	private static void onStartTick(Minecraft mc) {
		Freecam.preTick();
		if (mc.player == null || mc.level == null) return;

		while (Keys.FREECAM.consumeClick()) {
			Freecam.toggle();
			PlanController.reset();
		}
		while (Keys.VISIBLE.consumeClick()) PlanController.toggleVisible();
		while (Keys.UNDO.consumeClick()) PlanController.undo();
		while (Keys.LAYER_UP.consumeClick()) PlanController.layerStep(1);
		while (Keys.LAYER_DOWN.consumeClick()) PlanController.layerStep(-1);
		while (Keys.LAYER_OFF.consumeClick()) PlanController.layerOff();

		if (!Freecam.isEnabled()) {
			drain(Keys.TOOL);
			return;
		}

		PlanController.setAirMode(Keys.AIR_MODE.isDown());
		while (Keys.TOOL.consumeClick()) PlanController.nextTool();

		KeyMapping[] hotbar = mc.options.keyHotbarSlots;
		for (int i = 0; i < hotbar.length; i++) {
			while (hotbar[i].consumeClick()) {
				if (i < PlanColor.values().length) PlanController.setColor(PlanColor.byIndex(i));
			}
		}

		while (mc.options.keyUse.consumeClick()) PlanController.onUse();
		while (mc.options.keyAttack.consumeClick()) PlanController.onAttack();
		while (mc.options.keyPickItem.consumeClick()) PlanController.onPick();

		// Gedrückt halten: beim Einzelblock-Werkzeug weiter malen bzw. radieren
		useHold = mc.options.keyUse.isDown() ? useHold + 1 : 0;
		attackHold = mc.options.keyAttack.isDown() ? attackHold + 1 : 0;
		if (!PlanController.tool().twoPoint) {
			if (useHold > HOLD_DELAY && useHold % HOLD_INTERVAL == 0) PlanController.onUse();
			if (attackHold > HOLD_DELAY && attackHold % HOLD_INTERVAL == 0) PlanController.onAttack();
		}
	}

	private static final int HOLD_DELAY = 5, HOLD_INTERVAL = 3;
	private static int useHold = 0, attackHold = 0;

	private static void drain(KeyMapping key) {
		while (key.consumeClick()) {
			// nichts
		}
	}
}
