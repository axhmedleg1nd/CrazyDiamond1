package com.example.crazydiamond;

import java.util.List;

import org.joml.Vector3f;

import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** Server-side implementation of the Dvizhukha abilities (except time stop and the pocket dimension). */
public final class DvizhukhaActions {
	// Russian flag colours
	private static final DustParticleEffect WHITE = new DustParticleEffect(new Vector3f(1.0f, 1.0f, 1.0f), 1.3f);
	private static final DustParticleEffect BLUE = new DustParticleEffect(new Vector3f(0.0f, 0.22f, 0.65f), 1.3f);
	private static final DustParticleEffect RED = new DustParticleEffect(new Vector3f(0.84f, 0.17f, 0.12f), 1.3f);
	private static final DustParticleEffect[] TRICOLOR = {WHITE, BLUE, RED};

	private static final double STRIKE_RANGE = 16.0;
	private static final float STRIKE_DAMAGE = 14.0f;

	private DvizhukhaActions() {
	}

	/** White-blue-red particle burst. */
	public static void tricolor(ServerWorld world, double x, double y, double z, int count, double spread) {
		int each = Math.max(1, count / 3);
		for (DustParticleEffect effect : TRICOLOR) {
			world.spawnParticles(effect, x, y, z, each, spread, spread, spread, 0.02);
		}
	}

	// ------------------------------------------------------------- clones

	static void clones(ServerWorld world, ServerPlayerEntity owner) {
		for (int i = 0; i < 5; i++) {
			double angle = Math.toRadians(owner.getYaw()) + i * (Math.PI * 2.0 / 5.0);
			double x = owner.getX() + Math.cos(angle) * 2.0;
			double z = owner.getZ() + Math.sin(angle) * 2.0;

			CloneEntity clone = new CloneEntity(ModEntities.CLONE, world);
			clone.setOwner(owner);
			clone.refreshPositionAndAngles(x, owner.getY(), z, owner.getYaw(), 0.0f);
			world.spawnEntity(clone);
			tricolor(world, x, owner.getY() + 1.0, z, 18, 0.35);
		}
		world.playSound(null, owner.getBlockPos(), SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, SoundCategory.PLAYERS, 1.5f, 1.0f);
	}

	// ------------------------------------------------------- power strike

	/** A Warden-style sonic boom in white-blue-red: ignores armour, hurls targets away. */
	static void powerStrike(ServerWorld world, ServerPlayerEntity owner) {
		Vec3d start = owner.getEyePos().add(0.0, -0.2, 0.0);
		Vec3d look = owner.getRotationVec(1.0f);
		Vec3d end = start.add(look.multiply(STRIKE_RANGE));

		// the beam
		for (double d = 1.0; d <= STRIKE_RANGE; d += 0.5) {
			Vec3d p = start.add(look.multiply(d));
			DustParticleEffect color = TRICOLOR[(int) (d * 2.0) % 3];
			world.spawnParticles(color, p.x, p.y, p.z, 3, 0.12, 0.12, 0.12, 0.0);
			if (((int) (d * 2.0)) % 6 == 0) {
				world.spawnParticles(ParticleTypes.SONIC_BOOM, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
			}
		}

		world.playSound(null, owner.getBlockPos(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 3.0f, 1.0f);

		// the damage
		Box box = new Box(start, end).expand(2.0);
		List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, box,
				e -> e.isAlive() && e != owner && !e.isSpectator()
						&& !(e instanceof CloneEntity clone && owner.getUuid().equals(clone.getOwnerUuid())));
		for (LivingEntity target : targets) {
			Vec3d center = target.getBoundingBox().getCenter();
			double along = MathHelper.clamp(center.subtract(start).dotProduct(look), 0.0, STRIKE_RANGE);
			Vec3d closest = start.add(look.multiply(along));
			if (center.distanceTo(closest) > 1.6) {
				continue;
			}
			target.damage(world.getDamageSources().sonicBoom(owner), STRIKE_DAMAGE);
			target.addVelocity(look.x * 2.2, 0.55 + look.y * 0.5, look.z * 2.2);
			target.velocityModified = true;
			tricolor(world, center.x, center.y, center.z, 30, 0.5);
			world.spawnParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
		}
	}
}
