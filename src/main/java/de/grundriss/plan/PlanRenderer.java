package de.grundriss.plan;

import com.mojang.blaze3d.vertex.PoseStack;
import de.grundriss.cam.Freecam;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

/** Zeichnet die halbtransparenten Planblöcke und die Vorschau des aktuellen Werkzeugs. */
public final class PlanRenderer {
	private PlanRenderer() {}

	private static final double RADIUS = 192;
	private static final double REBUILD_MOVE = 48;
	private static final float INSET = 0.004f;

	private static final int ALPHA_NORMAL = 0x66;
	private static final int ALPHA_LAYER = 0x99;
	private static final int ALPHA_OTHER_LAYER = 0x18;

	// Seiten-Helligkeit: oben, unten, Nord/Süd, West/Ost
	private static final float SHADE_TOP = 1.0f, SHADE_BOTTOM = 0.55f, SHADE_NS = 0.85f, SHADE_WE = 0.72f;

	private static final QuadBuffer planQuads = new QuadBuffer();
	private static final QuadBuffer previewQuads = new QuadBuffer();
	private static int builtRevision = -1;
	private static Integer builtLayer = null;
	private static double originX, originY, originZ;
	private static boolean built = false;

	public static void render(LevelRenderContext ctx) {
		Vec3 cam = ctx.levelState().cameraRenderState.pos;
		PlanController.updateTarget(cam);

		if (PlanController.visible()) {
			rebuildIfNeeded(cam);
			if (!planQuads.isEmpty()) submit(ctx, planQuads, originX - cam.x, originY - cam.y, originZ - cam.z);
		}

		if (Freecam.isEnabled()) {
			buildPreview(cam);
			if (!previewQuads.isEmpty()) submit(ctx, previewQuads, 0, 0, 0);
		}
	}

	private static void submit(LevelRenderContext ctx, QuadBuffer quads, double dx, double dy, double dz) {
		PoseStack ps = ctx.poseStack();
		ps.pushPose();
		ps.translate(dx, dy, dz);
		ctx.submitNodeCollector().submitCustomGeometry(ps, RenderTypes.debugFilledBox(), quads::draw);
		ps.popPose();
	}

	// ---------- Planblöcke ----------

	private static void rebuildIfNeeded(Vec3 cam) {
		Integer layer = PlanController.layer();
		boolean moved = !built
				|| Math.abs(cam.x - originX) > REBUILD_MOVE
				|| Math.abs(cam.y - originY) > REBUILD_MOVE
				|| Math.abs(cam.z - originZ) > REBUILD_MOVE;
		boolean layerChanged = layer == null ? builtLayer != null : !layer.equals(builtLayer);
		if (!moved && !layerChanged && builtRevision == PlanStore.revision()) return;

		built = true;
		builtRevision = PlanStore.revision();
		builtLayer = layer;
		originX = Math.floor(cam.x);
		originY = Math.floor(cam.y);
		originZ = Math.floor(cam.z);
		planQuads.clear();

		double r2 = RADIUS * RADIUS;
		for (Map.Entry<Long, Integer> e : PlanStore.view().entrySet()) {
			BlockPos p = BlockPos.of(e.getKey());
			double ddx = p.getX() + 0.5 - cam.x, ddy = p.getY() + 0.5 - cam.y, ddz = p.getZ() + 0.5 - cam.z;
			if (ddx * ddx + ddy * ddy + ddz * ddz > r2) continue;

			PlanColor color = PlanColor.byIndex(e.getValue());
			boolean onLayer = layer == null || layer == p.getY();
			int alpha = layer == null ? ALPHA_NORMAL : (onLayer ? ALPHA_LAYER : ALPHA_OTHER_LAYER);
			addBlockFaces(p, color, alpha, layer != null);
		}
	}

	private static void addBlockFaces(BlockPos p, PlanColor color, int alpha, boolean layerMode) {
		int x = p.getX(), y = p.getY(), z = p.getZ();
		float x0 = (float) (x - originX) + INSET, y0 = (float) (y - originY) + INSET, z0 = (float) (z - originZ) + INSET;
		float x1 = x0 + 1 - 2 * INSET, y1 = y0 + 1 - 2 * INSET, z1 = z0 + 1 - 2 * INSET;

		// Innenflächen zwischen zwei Planblöcken weglassen, damit Wände wie eine Fläche aussehen.
		// Im Ebenen-Modus bleiben Ober-/Unterseiten sichtbar.
		if (!PlanStore.has(x, y, z - 1)) planQuads.quad(shade(color, alpha, SHADE_NS), x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0);
		if (!PlanStore.has(x, y, z + 1)) planQuads.quad(shade(color, alpha, SHADE_NS), x1, y0, z1, x0, y0, z1, x0, y1, z1, x1, y1, z1);
		if (!PlanStore.has(x - 1, y, z)) planQuads.quad(shade(color, alpha, SHADE_WE), x0, y0, z1, x0, y0, z0, x0, y1, z0, x0, y1, z1);
		if (!PlanStore.has(x + 1, y, z)) planQuads.quad(shade(color, alpha, SHADE_WE), x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0);
		if (layerMode || !PlanStore.has(x, y + 1, z)) planQuads.quad(shade(color, alpha, SHADE_TOP), x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1);
		if (layerMode || !PlanStore.has(x, y - 1, z)) planQuads.quad(shade(color, alpha, SHADE_BOTTOM), x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0);
	}

	private static int shade(PlanColor color, int alpha, float f) {
		int r = (int) (((color.rgb >> 16) & 0xFF) * f);
		int g = (int) (((color.rgb >> 8) & 0xFF) * f);
		int b = (int) ((color.rgb & 0xFF) * f);
		return (alpha << 24) | (r << 16) | (g << 8) | b;
	}

	// ---------- Vorschau ----------

	private static final int OUTLINE_PLACE = 0xE6FFFFFF;
	private static final int OUTLINE_REMOVE = 0xE6FF3030;
	private static final int PREVIEW_REMOVE = 0x50FF3030;
	private static final int PREVIEW_LIMIT = 6000;

	private static void buildPreview(Vec3 cam) {
		previewQuads.clear();
		PlanController.Target t = PlanController.target();
		BlockPos start = PlanController.pendingStart();
		boolean removing = PlanController.pendingRemove();

		if (start != null && t != null) {
			BlockPos end = removing
					? (t.remove() != null ? t.remove() : t.place())
					: t.place();
			if (end == null) return;
			List<BlockPos> shape = PlanController.tool().shape(start, end);
			int fill = removing ? PREVIEW_REMOVE : PlanController.color().argb(0x50);
			if (shape != null && shape.size() <= PREVIEW_LIMIT) {
				for (BlockPos p : shape) {
					float x = (float) (p.getX() - cam.x), y = (float) (p.getY() - cam.y), z = (float) (p.getZ() - cam.z);
					previewQuads.box(x + 0.02f, y + 0.02f, z + 0.02f, x + 0.98f, y + 0.98f, z + 0.98f, fill);
				}
			}
			// Rahmen um die ganze Form (bei sehr großen Formen nur der Rahmen)
			int ox = Math.min(start.getX(), end.getX()), oy = Math.min(start.getY(), end.getY()), oz = Math.min(start.getZ(), end.getZ());
			int mx = Math.max(start.getX(), end.getX()) + 1, my = Math.max(start.getY(), end.getY()) + 1, mz = Math.max(start.getZ(), end.getZ()) + 1;
			outline(cam, ox, oy, oz, mx, my, mz, shape == null ? OUTLINE_REMOVE : (removing ? OUTLINE_REMOVE : OUTLINE_PLACE), 0.05f);
			return;
		}

		if (t == null) return;
		if (t.remove() != null && !PlanController.tool().twoPoint) {
			// Linksklick würde diesen Block entfernen: rot umranden
			BlockPos r = t.remove();
			outline(cam, r.getX(), r.getY(), r.getZ(), r.getX() + 1, r.getY() + 1, r.getZ() + 1, OUTLINE_REMOVE, 0.03f);
		}
		if (t.place() != null) {
			BlockPos p = t.place();
			float x = (float) (p.getX() - cam.x), y = (float) (p.getY() - cam.y), z = (float) (p.getZ() - cam.z);
			previewQuads.box(x + 0.02f, y + 0.02f, z + 0.02f, x + 0.98f, y + 0.98f, z + 0.98f, PlanController.color().argb(0x40));
			outline(cam, p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 1, p.getZ() + 1, OUTLINE_PLACE, 0.03f);
		}
	}

	private static void outline(Vec3 cam, double x0, double y0, double z0, double x1, double y1, double z1, int argb, float width) {
		previewQuads.outline((float) (x0 - cam.x), (float) (y0 - cam.y), (float) (z0 - cam.z),
				(float) (x1 - cam.x), (float) (y1 - cam.y), (float) (z1 - cam.z), width, argb);
	}
}
