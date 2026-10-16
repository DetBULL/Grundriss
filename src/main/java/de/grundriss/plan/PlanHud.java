package de.grundriss.plan;

import de.grundriss.cam.Freecam;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** Kleine Info-Box oben links, solange die Freecam an ist. */
public final class PlanHud {
	private PlanHud() {}

	private static final int WHITE = 0xFFFFFFFF;
	private static final int GREY = 0xFFB0B0B0;
	private static final int SWATCH = 12; // Platz für das Farbkästchen

	public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
		Minecraft mc = Minecraft.getInstance();
		if (!Freecam.isEnabled()) return;
		Font font = mc.font;
		PlanColor color = PlanController.color();
		Integer layer = PlanController.layer();

		List<String> lines = new ArrayList<>();
		lines.add(tr("grundriss.hud.title"));
		lines.add(tr("grundriss.hud.tool", tr(PlanController.tool().translationKey())));
		lines.add(tr("grundriss.hud.color", tr(color.translationKey()), PlanStore.count(color)));
		lines.add(PlanController.airMode()
				? tr("grundriss.hud.mode_air", PlanController.airDistance())
				: tr("grundriss.hud.mode_surface"));
		lines.add(layer == null ? tr("grundriss.hud.layer_all") : tr("grundriss.hud.layer", layer));
		lines.add(PlanController.pendingStart() != null
				? tr(PlanController.pendingRemove() ? "grundriss.hud.pending_remove" : "grundriss.hud.pending")
				: tr("grundriss.hud.total", PlanStore.total()));

		int x = 6, y = 6, lh = 11;
		int width = 0;
		for (int i = 0; i < lines.size(); i++) {
			width = Math.max(width, font.width(lines.get(i)) + (i == 2 ? SWATCH : 0));
		}
		g.fill(x - 3, y - 3, x + width + 3, y + lh * lines.size() + 1, 0x80000000);

		for (int i = 0; i < lines.size(); i++) {
			int tx = x;
			if (i == 2) {
				g.fill(x, y, x + 8, y + 8, color.argb(0xFF));
				tx += SWATCH;
			}
			g.text(font, lines.get(i), tx, y, i == lines.size() - 1 ? GREY : WHITE, true);
			y += lh;
		}
	}

	private static String tr(String key, Object... args) {
		return Component.translatable(key, args).getString();
	}
}
