package de.grundriss.cam;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoveSimulationType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Unsichtbare Kamera-Figur, durch die man im Freecam-Modus schaut.
 * Sie hat eine kleine Hitbox (Schwimm-Pose) und kollidiert nur mit Gelände
 * (siehe {@link Terrain#isPassable}).
 *
 * Aufbau angelehnt an MinecraftFreecam/Freecam (MIT-Lizenz).
 */
public class FreeCamera extends AbstractClientPlayer {

	private static final double BASE_SPEED = 0.6;
	private static final double SPRINT_FACTOR = 2.0;
	private static final double DIAGONAL = Mth.sin((float) Math.toRadians(45));

	private final Minecraft mc = Minecraft.getInstance();
	private final KeyboardInput keyboardInput;

	public FreeCamera(int id) {
		super(Minecraft.getInstance().level, new GameProfile(UUID.randomUUID(), "GrundrissCam"));
		setId(id);
		setPose(Pose.SWIMMING);
		getAbilities().flying = true;
		keyboardInput = new KeyboardInput(mc.options);
	}

	/** Setzt die Kamera auf die Augenhöhe des Spielers. */
	public void moveToEntity(Entity entity) {
		double y = entity.getEyeY() - getEyeHeight();
		snapTo(entity.getX(), y, entity.getZ(), entity.getYRot(), entity.getXRot());
	}

	public void spawn() {
		((ClientLevel) level()).addEntity(this);
	}

	public void despawn() {
		((ClientLevel) level()).removeEntity(getId(), RemovalReason.DISCARDED);
	}

	@Override
	public void tick() {
		keyboardInput.tick();
		doMotion();
		super.tick();
	}

	private void doMotion() {
		ClientInput in = keyboardInput;
		double speed = BASE_SPEED * (mc.options.keySprint.isDown() ? SPRINT_FACTOR : 1.0);
		Vec3 forward = Vec3.directionFromRotation(0, getYRot());
		Vec3 side = Vec3.directionFromRotation(0, getYRot() + 90);

		double vx = 0, vy = 0, vz = 0;
		boolean straight = false, strafe = false;
		if (in.keyPresses.forward()) { vx += forward.x * speed; vz += forward.z * speed; straight = true; }
		if (in.keyPresses.backward()) { vx -= forward.x * speed; vz -= forward.z * speed; straight = true; }
		if (in.keyPresses.right()) { vx += side.x * speed; vz += side.z * speed; strafe = true; }
		if (in.keyPresses.left()) { vx -= side.x * speed; vz -= side.z * speed; strafe = true; }
		if (straight && strafe) { vx *= DIAGONAL; vz *= DIAGONAL; }
		if (in.keyPresses.jump()) vy += speed;
		if (in.keyPresses.shift()) vy -= speed;

		getAbilities().setFlyingSpeed(0);
		getAbilities().flying = true;
		setDeltaMovement(vx, vy, vz);
		setOnGround(false);
	}

	// Bewegung wird lokal simuliert
	@Override
	public boolean isEffectiveAi() {
		return true;
	}

	@Override
	public @NotNull MoveSimulationType getMoveSimulationType() {
		return MoveSimulationType.SERVER_AND_CLIENT;
	}

	@Override
	protected void applyInput() {
		Vec2 move = keyboardInput.getMoveVector();
		this.xxa = move.x;
		this.zza = move.y;
		this.jumping = keyboardInput.keyPresses.jump();
	}

	@Override
	public float getViewXRot(float partialTick) {
		return getXRot();
	}

	@Override
	public float getViewYRot(float partialTick) {
		return getYRot();
	}

	// Keine Fallgeräusche, kein Klettern, kein Wasser-Bremsen, keine Wassergeräusche
	@Override
	protected void checkFallDamage(double heightDifference, boolean onGround, @NotNull BlockState landedState, @NotNull BlockPos landedPosition) {
	}

	@Override
	public boolean onClimbable() {
		return false;
	}

	@Override
	public boolean isInWater() {
		return false;
	}

	@Override
	protected boolean updateIsUnderwater() {
		this.wasUnderwater = this.isEyeInFluid(FluidTags.WATER);
		return this.wasUnderwater;
	}

	@Override
	protected void doWaterSplashEffect() {
	}

	// Nachtsicht des Spielers gilt auch für die Kamera
	@Override
	public MobEffectInstance getEffect(@NotNull Holder<MobEffect> effect) {
		return mc.player != null ? mc.player.getEffect(effect) : null;
	}

	@Override
	public boolean canCollideWith(@NotNull Entity other) {
		return false;
	}

	@Override
	public void setPose(@NotNull Pose pose) {
		super.setPose(Pose.SWIMMING);
	}
}
