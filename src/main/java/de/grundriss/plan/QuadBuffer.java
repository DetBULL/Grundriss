package de.grundriss.plan;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.Arrays;

/** Sammelt Vierecke (je 4 Ecken + Farbe), damit sie gesammelt gezeichnet werden können. */
final class QuadBuffer {
	private float[] pos = new float[4 * 3 * 256];
	private int[] col = new int[256];
	private int quads = 0;

	void clear() {
		quads = 0;
	}

	boolean isEmpty() {
		return quads == 0;
	}

	private void grow() {
		if (quads < col.length) return;
		col = Arrays.copyOf(col, col.length * 2);
		pos = Arrays.copyOf(pos, pos.length * 2);
	}

	void quad(int argb,
			  float x0, float y0, float z0, float x1, float y1, float z1,
			  float x2, float y2, float z2, float x3, float y3, float z3) {
		grow();
		int i = quads * 12;
		pos[i] = x0; pos[i + 1] = y0; pos[i + 2] = z0;
		pos[i + 3] = x1; pos[i + 4] = y1; pos[i + 5] = z1;
		pos[i + 6] = x2; pos[i + 7] = y2; pos[i + 8] = z2;
		pos[i + 9] = x3; pos[i + 10] = y3; pos[i + 11] = z3;
		col[quads] = argb;
		quads++;
	}

	/** Alle 6 Seiten eines Quaders. */
	void box(float x0, float y0, float z0, float x1, float y1, float z1, int argb) {
		quad(argb, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0); // Norden
		quad(argb, x1, y0, z1, x0, y0, z1, x0, y1, z1, x1, y1, z1); // Süden
		quad(argb, x0, y0, z1, x0, y0, z0, x0, y1, z0, x0, y1, z1); // Westen
		quad(argb, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0); // Osten
		quad(argb, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1); // oben
		quad(argb, x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0); // unten
	}

	/** Umriss eines Quaders aus 12 dünnen Balken. */
	void outline(float x0, float y0, float z0, float x1, float y1, float z1, float w, int argb) {
		float h = w / 2;
		for (float y : new float[]{y0, y1}) for (float z : new float[]{z0, z1})
			box(x0 - h, y - h, z - h, x1 + h, y + h, z + h, argb);
		for (float x : new float[]{x0, x1}) for (float z : new float[]{z0, z1})
			box(x - h, y0 - h, z - h, x + h, y1 + h, z + h, argb);
		for (float x : new float[]{x0, x1}) for (float y : new float[]{y0, y1})
			box(x - h, y - h, z0 - h, x + h, y + h, z1 + h, argb);
	}

	void draw(PoseStack.Pose pose, VertexConsumer vc) {
		for (int q = 0; q < quads; q++) {
			int i = q * 12;
			int c = col[q];
			vc.addVertex(pose, pos[i], pos[i + 1], pos[i + 2]).setColor(c);
			vc.addVertex(pose, pos[i + 3], pos[i + 4], pos[i + 5]).setColor(c);
			vc.addVertex(pose, pos[i + 6], pos[i + 7], pos[i + 8]).setColor(c);
			vc.addVertex(pose, pos[i + 9], pos[i + 10], pos[i + 11]).setColor(c);
		}
	}
}
