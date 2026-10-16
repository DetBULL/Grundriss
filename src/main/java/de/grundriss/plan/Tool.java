package de.grundriss.plan;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/** Werkzeuge zum Setzen und Entfernen (Taste G wechselt). */
public enum Tool {
	EINZEL("einzel", false),
	LINIE("linie", true),
	FLAECHE("flaeche", true),
	WAENDE("waende", true);

	/** Schutz gegen versehentlich riesige Aktionen. */
	public static final int MAX_BLOCKS = 32768;

	public final String key;
	/** Braucht zwei Klicks (Start und Ende). */
	public final boolean twoPoint;

	Tool(String key, boolean twoPoint) {
		this.key = key;
		this.twoPoint = twoPoint;
	}

	public String translationKey() {
		return "grundriss.tool." + key;
	}

	public Tool next() {
		Tool[] v = values();
		return v[(ordinal() + 1) % v.length];
	}

	/** Alle Positionen der Form zwischen a und b, oder null wenn sie zu groß wäre. */
	public List<BlockPos> shape(BlockPos a, BlockPos b) {
		return switch (this) {
			case EINZEL -> List.of(b);
			case LINIE -> line(a, b);
			case FLAECHE -> box(a, b, false);
			case WAENDE -> box(a, b, true);
		};
	}

	private static List<BlockPos> line(BlockPos a, BlockPos b) {
		int dx = b.getX() - a.getX(), dy = b.getY() - a.getY(), dz = b.getZ() - a.getZ();
		int steps = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
		List<BlockPos> out = new ArrayList<>(steps + 1);
		if (steps == 0) {
			out.add(a);
			return out;
		}
		BlockPos last = null;
		for (int i = 0; i <= steps; i++) {
			double t = (double) i / steps;
			BlockPos p = BlockPos.containing(a.getX() + 0.5 + dx * t, a.getY() + 0.5 + dy * t, a.getZ() + 0.5 + dz * t);
			if (!p.equals(last)) out.add(p);
			last = p;
		}
		return out;
	}

	private static List<BlockPos> box(BlockPos a, BlockPos b, boolean wallsOnly) {
		int x0 = Math.min(a.getX(), b.getX()), x1 = Math.max(a.getX(), b.getX());
		int y0 = Math.min(a.getY(), b.getY()), y1 = Math.max(a.getY(), b.getY());
		int z0 = Math.min(a.getZ(), b.getZ()), z1 = Math.max(a.getZ(), b.getZ());
		long volume = (long) (x1 - x0 + 1) * (y1 - y0 + 1) * (z1 - z0 + 1);
		if (!wallsOnly && volume > MAX_BLOCKS) return null;

		List<BlockPos> out = new ArrayList<>();
		for (int y = y0; y <= y1; y++) {
			for (int x = x0; x <= x1; x++) {
				for (int z = z0; z <= z1; z++) {
					if (wallsOnly && x != x0 && x != x1 && z != z0 && z != z1) continue;
					out.add(new BlockPos(x, y, z));
					if (out.size() > MAX_BLOCKS) return null;
				}
			}
		}
		return out;
	}
}
