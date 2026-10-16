package de.grundriss.cam;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Entscheidet, was für die Freecam "Gelände" ist.
 * Durchfliegbar: Bäume, Pflanzen, Schnee-Schichten, Wasser/Lava und alles Ersetzbare.
 * Alles andere (Erde, Stein, Sand, gebaute Blöcke ...) ist fest.
 */
public final class Terrain {
	private Terrain() {}

	public static boolean isPassable(BlockState state) {
		if (state.isAir() || state.canBeReplaced()) return true; // Luft, Gras, Blumen, Wasser, Lava, Ranken ...
		if (state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS)) return true; // Bäume
		return state.is(Blocks.SNOW)
				|| state.is(Blocks.BAMBOO)
				|| state.is(Blocks.CACTUS)
				|| state.is(Blocks.SWEET_BERRY_BUSH)
				|| state.is(Blocks.MUSHROOM_STEM)
				|| state.is(Blocks.BROWN_MUSHROOM_BLOCK)
				|| state.is(Blocks.RED_MUSHROOM_BLOCK);
	}

	/** Gelände, auf das man Planblöcke setzen kann. */
	public static boolean isSolidGround(BlockState state) {
		return !isPassable(state);
	}
}
