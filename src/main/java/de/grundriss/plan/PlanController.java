package de.grundriss.plan;

import de.grundriss.cam.Freecam;
import de.grundriss.cam.Terrain;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Werkzeug, Farbe, Zielerfassung und die eigentlichen Klick-Aktionen. */
public final class PlanController {
	private PlanController() {}

	private static final Minecraft MC = Minecraft.getInstance();
	public static final double REACH = 96.0;
	public static final int MIN_AIR_DIST = 1, MAX_AIR_DIST = 64;

	private static Tool tool = Tool.EINZEL;
	private static PlanColor color = PlanColor.GRAU;
	private static boolean visible = true;
	private static int airDistance = 5;
	private static boolean airMode = false;
	private static @Nullable Integer layer = null;

	/** Erster Punkt eines Zwei-Klick-Werkzeugs. */
	private static @Nullable BlockPos pendingStart = null;
	private static boolean pendingRemove = false;

	/** Wo neu gesetzt würde (vor der getroffenen Fläche) bzw. welcher Planblock entfernt würde. */
	public record Target(@Nullable BlockPos place, @Nullable BlockPos remove) {}

	private static @Nullable Target target = null;

	// ---------- Zustand für HUD/Renderer ----------

	public static Tool tool() { return tool; }
	public static PlanColor color() { return color; }
	public static boolean visible() { return visible; }
	public static boolean airMode() { return airMode; }
	public static int airDistance() { return airDistance; }
	public static @Nullable Integer layer() { return layer; }
	public static @Nullable Target target() { return target; }
	public static @Nullable BlockPos pendingStart() { return pendingStart; }
	public static boolean pendingRemove() { return pendingRemove; }

	public static void setAirMode(boolean on) { airMode = on; }

	// ---------- Zielerfassung ----------

	/** Wird jeden Frame mit der echten Kameraposition aufgerufen. */
	public static void updateTarget(Vec3 eye) {
		Entity cam = Freecam.camera();
		if (!Freecam.isEnabled() || cam == null || MC.level == null) {
			target = null;
			return;
		}
		Vec3 dir = cam.getViewVector(1.0f);
		if (airMode) {
			BlockPos p = BlockPos.containing(eye.add(dir.scale(airDistance)));
			target = new Target(p, PlanStore.get(p) != null ? p : null);
		} else {
			target = raycast(MC.level, eye, dir, REACH);
		}
	}

	/** Schrittweise durch die Blöcke entlang des Blicks (Amanatides-Woo). */
	private static @Nullable Target raycast(Level level, Vec3 from, Vec3 dir, double maxDist) {
		int x = (int) Math.floor(from.x), y = (int) Math.floor(from.y), z = (int) Math.floor(from.z);
		int sx = (int) Math.signum(dir.x), sy = (int) Math.signum(dir.y), sz = (int) Math.signum(dir.z);
		double tdx = sx == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dir.x);
		double tdy = sy == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dir.y);
		double tdz = sz == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dir.z);
		double tmx = sx == 0 ? Double.POSITIVE_INFINITY : ((sx > 0 ? x + 1 - from.x : from.x - x) * tdx);
		double tmy = sy == 0 ? Double.POSITIVE_INFINITY : ((sy > 0 ? y + 1 - from.y : from.y - y) * tdy);
		double tmz = sz == 0 ? Double.POSITIVE_INFINITY : ((sz > 0 ? z + 1 - from.z : from.z - z) * tdz);

		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		BlockPos prev = null;
		double t = 0;
		while (t <= maxDist) {
			cursor.set(x, y, z);
			if (PlanStore.has(x, y, z) && isOnActiveLayer(y)) {
				return new Target(prev, cursor.immutable());
			}
			if (Terrain.isSolidGround(level.getBlockState(cursor))) {
				return prev == null ? null : new Target(prev, null);
			}
			prev = cursor.immutable();
			if (tmx < tmy && tmx < tmz) { x += sx; t = tmx; tmx += tdx; }
			else if (tmy < tmz) { y += sy; t = tmy; tmy += tdy; }
			else { z += sz; t = tmz; tmz += tdz; }
		}
		return null;
	}

	private static boolean isOnActiveLayer(int y) {
		return layer == null || layer == y;
	}

	// ---------- Aktionen ----------

	public static void onUse() {
		Target t = target;
		if (t == null || t.place() == null) return;
		if (pendingStart != null && pendingRemove) { cancelPending(); return; }
		if (!tool.twoPoint) {
			PlanStore.apply(List.of(t.place()), color);
			return;
		}
		if (pendingStart == null) {
			pendingStart = t.place();
			pendingRemove = false;
			msg(Component.translatable("grundriss.msg.start_set"));
		} else {
			finish(t.place(), color);
		}
	}

	public static void onAttack() {
		Target t = target;
		if (t == null) return;
		if (pendingStart != null && !pendingRemove) { cancelPending(); return; }
		if (!tool.twoPoint) {
			if (t.remove() != null) PlanStore.apply(List.of(t.remove()), null);
			return;
		}
		BlockPos p = t.remove() != null ? t.remove() : t.place();
		if (p == null) return;
		if (pendingStart == null) {
			pendingStart = p;
			pendingRemove = true;
			msg(Component.translatable("grundriss.msg.start_set_remove"));
		} else {
			finish(p, null);
		}
	}

	private static void finish(BlockPos end, @Nullable PlanColor c) {
		List<BlockPos> shape = tool.shape(pendingStart, end);
		pendingStart = null;
		if (shape == null) {
			msg(Component.translatable("grundriss.msg.too_big", Tool.MAX_BLOCKS));
			return;
		}
		int n = PlanStore.apply(shape, c);
		msg(Component.translatable(c == null ? "grundriss.msg.removed" : "grundriss.msg.placed", n));
	}

	/** Mittlere Maustaste: Farbe vom anvisierten Planblock übernehmen. */
	public static void onPick() {
		Target t = target;
		if (t == null || t.remove() == null) return;
		PlanColor c = PlanStore.get(t.remove());
		if (c != null) setColor(c);
	}

	public static void cancelPending() {
		if (pendingStart != null) {
			pendingStart = null;
			msg(Component.translatable("grundriss.msg.cancelled"));
		}
	}

	public static void nextTool() {
		tool = tool.next();
		pendingStart = null;
		msg(Component.translatable("grundriss.msg.tool", Component.translatable(tool.translationKey())));
	}

	public static void setColor(PlanColor c) {
		color = c;
		msg(Component.translatable("grundriss.msg.color", Component.translatable(c.translationKey())));
	}

	public static void cycleColor(int delta) {
		setColor(PlanColor.byIndex(color.ordinal() + delta));
	}

	public static void changeAirDistance(int delta) {
		airDistance = Math.max(MIN_AIR_DIST, Math.min(MAX_AIR_DIST, airDistance + delta));
	}

	public static void toggleVisible() {
		visible = !visible;
		msg(Component.translatable(visible ? "grundriss.msg.visible_on" : "grundriss.msg.visible_off"));
	}

	public static void undo() {
		pendingStart = null;
		msg(Component.translatable(PlanStore.undo() ? "grundriss.msg.undo" : "grundriss.msg.undo_empty"));
	}

	public static void layerStep(int delta) {
		if (layer == null) {
			Entity e = MC.getCameraEntity();
			if (e == null) return;
			BlockPos base = target != null && target.place() != null ? target.place() : e.blockPosition();
			layer = base.getY();
		} else {
			layer += delta;
		}
		msg(Component.translatable("grundriss.msg.layer", layer));
	}

	public static void layerOff() {
		if (layer != null) {
			layer = null;
			msg(Component.translatable("grundriss.msg.layer_all"));
		}
	}

	public static void reset() {
		pendingStart = null;
		target = null;
	}

	private static void msg(Component c) {
		if (MC.player != null) MC.player.sendOverlayMessage(c);
	}
}
