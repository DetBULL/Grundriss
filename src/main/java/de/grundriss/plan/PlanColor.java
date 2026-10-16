package de.grundriss.plan;

/** Die 8 Planungsfarben (Tasten 1–8). */
public enum PlanColor {
	GRAU("grau", 0xB4B4B4),
	BRAUN("braun", 0x8B5A2B),
	ROT("rot", 0xE03C31),
	ORANGE("orange", 0xF28C28),
	GELB("gelb", 0xF2D43D),
	GRUEN("gruen", 0x4CB84C),
	BLAU("blau", 0x3D7EE0),
	LILA("lila", 0xA04CD9);

	public final String key;
	public final int rgb;

	PlanColor(String key, int rgb) {
		this.key = key;
		this.rgb = rgb;
	}

	/** Farbe mit Transparenz (0–255) als ARGB. */
	public int argb(int alpha) {
		return (alpha << 24) | rgb;
	}

	public String translationKey() {
		return "grundriss.color." + key;
	}

	public static PlanColor byIndex(int i) {
		PlanColor[] v = values();
		return v[Math.floorMod(i, v.length)];
	}
}
