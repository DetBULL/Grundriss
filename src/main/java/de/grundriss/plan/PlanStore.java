package de.grundriss.plan;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.grundriss.Grundriss;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Alle Planblöcke der aktuellen Welt + Dimension, mit Rückgängig und Speichern.
 * Gespeichert wird unter .minecraft/config/grundriss/&lt;welt&gt;.json
 */
public final class PlanStore {
	private PlanStore() {}

	private static final Gson GSON = new GsonBuilder().create();
	private static final int UNDO_LIMIT = 100;
	private static final int AUTOSAVE_TICKS = 100;

	/** BlockPos.asLong -> Farb-Index */
	private static final Map<Long, Integer> blocks = new HashMap<>();
	private static final Deque<List<Change>> undo = new ArrayDeque<>();
	private static final EnumMap<PlanColor, Integer> counts = new EnumMap<>(PlanColor.class);

	private static @Nullable String worldKey;
	private static boolean dirty;
	private static int ticksSinceSave;
	/** Wird bei jeder Änderung erhöht, damit der Renderer weiß, wann er neu bauen muss. */
	private static int revision;

	private record Change(long pos, int before, int after) {} // -1 = leer

	// ---------- Lesen ----------

	public static @Nullable PlanColor get(BlockPos pos) {
		Integer c = blocks.get(pos.asLong());
		return c == null ? null : PlanColor.byIndex(c);
	}

	public static boolean has(int x, int y, int z) {
		return blocks.containsKey(BlockPos.asLong(x, y, z));
	}

	public static Map<Long, Integer> view() {
		return Collections.unmodifiableMap(blocks);
	}

	public static int count(PlanColor color) {
		return counts.getOrDefault(color, 0);
	}

	public static int total() {
		return blocks.size();
	}

	public static int revision() {
		return revision;
	}

	// ---------- Ändern ----------

	/** Setzt (color != null) oder entfernt (color == null) alle Positionen als ein Rückgängig-Schritt. */
	public static int apply(List<BlockPos> positions, @Nullable PlanColor color) {
		List<Change> changes = new ArrayList<>();
		int after = color == null ? -1 : color.ordinal();
		for (BlockPos p : positions) {
			long key = p.asLong();
			Integer old = blocks.get(key);
			int before = old == null ? -1 : old;
			if (before == after) continue;
			set(key, after);
			changes.add(new Change(key, before, after));
		}
		if (!changes.isEmpty()) {
			undo.push(changes);
			while (undo.size() > UNDO_LIMIT) undo.removeLast();
			markChanged();
		}
		return changes.size();
	}

	public static boolean undo() {
		List<Change> last = undo.poll();
		if (last == null) return false;
		for (int i = last.size() - 1; i >= 0; i--) {
			Change c = last.get(i);
			set(c.pos, c.before);
		}
		markChanged();
		return true;
	}

	private static void set(long key, int colorIndex) {
		Integer old = colorIndex < 0 ? blocks.remove(key) : blocks.put(key, colorIndex);
		if (old != null) counts.merge(PlanColor.byIndex(old), -1, Integer::sum);
		if (colorIndex >= 0) counts.merge(PlanColor.byIndex(colorIndex), 1, Integer::sum);
	}

	private static void markChanged() {
		dirty = true;
		revision++;
	}

	// ---------- Welt wechseln / Speichern ----------

	/** Jeden Tick: prüft Welt-/Dimensionswechsel und speichert regelmäßig. */
	public static void tick(Minecraft mc) {
		String key = currentWorldKey(mc);
		if (key == null ? worldKey != null : !key.equals(worldKey)) {
			save();
			worldKey = key;
			load();
		}
		if (dirty && ++ticksSinceSave >= AUTOSAVE_TICKS) {
			save();
		}
	}

	public static void onDisconnect() {
		save();
		worldKey = null;
		blocks.clear();
		counts.clear();
		undo.clear();
		revision++;
	}

	private static @Nullable String currentWorldKey(Minecraft mc) {
		if (mc.level == null) return null;
		String server;
		if (mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null) {
			server = "einzelspieler_" + mc.getSingleplayerServer().getWorldData().getLevelName();
		} else if (mc.getCurrentServer() != null) {
			server = "server_" + mc.getCurrentServer().ip;
		} else {
			server = "unbekannt";
		}
		String dim = mc.level.dimension().toString();
		int slash = dim.lastIndexOf('/');
		if (slash >= 0) dim = dim.substring(slash + 1);
		return (server + "_" + dim).replaceAll("[^a-zA-Z0-9._-]+", "_");
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("grundriss").resolve(worldKey + ".json");
	}

	public static void save() {
		ticksSinceSave = 0;
		if (!dirty || worldKey == null) return;
		dirty = false;
		JsonObject root = new JsonObject();
		root.addProperty("version", 1);
		JsonArray arr = new JsonArray();
		for (Map.Entry<Long, Integer> e : blocks.entrySet()) {
			BlockPos p = BlockPos.of(e.getKey());
			JsonArray b = new JsonArray();
			b.add(p.getX());
			b.add(p.getY());
			b.add(p.getZ());
			b.add(e.getValue());
			arr.add(b);
		}
		root.add("blocks", arr);
		try {
			Path f = file();
			Files.createDirectories(f.getParent());
			Path tmp = f.resolveSibling(f.getFileName() + ".tmp");
			Files.writeString(tmp, GSON.toJson(root), StandardCharsets.UTF_8);
			Files.move(tmp, f, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ex) {
			dirty = true; // beim nächsten Mal erneut versuchen
			Grundriss.LOGGER.error("Konnte Plan nicht speichern", ex);
		}
	}

	private static void load() {
		blocks.clear();
		counts.clear();
		undo.clear();
		dirty = false;
		revision++;
		if (worldKey == null) return;
		Path f = file();
		if (!Files.exists(f)) return;
		try {
			JsonObject root = JsonParser.parseString(Files.readString(f, StandardCharsets.UTF_8)).getAsJsonObject();
			for (JsonElement el : root.getAsJsonArray("blocks")) {
				JsonArray b = el.getAsJsonArray();
				int color = Math.floorMod(b.get(3).getAsInt(), PlanColor.values().length);
				set(BlockPos.asLong(b.get(0).getAsInt(), b.get(1).getAsInt(), b.get(2).getAsInt()), color);
			}
			Grundriss.LOGGER.info("Plan geladen: {} Blöcke ({})", blocks.size(), worldKey);
		} catch (Exception ex) {
			Grundriss.LOGGER.error("Konnte Plan nicht laden: " + f, ex);
		}
	}
}
