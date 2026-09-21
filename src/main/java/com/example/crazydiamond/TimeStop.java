package com.example.crazydiamond;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * Freezes everything near the owner for 3 seconds: mobs lose their AI, projectiles and items
 * hang in the air, other players are pinned in place. The owner (and their stand and clones) move freely.
 */
public final class TimeStop {
	public static final int DURATION = 3 * 20;
	public static final int COOLDOWN = 20 * 20;
	private static final double RADIUS = 96.0;

	private record Frozen(Vec3d velocity, boolean noGravity, boolean aiDisabled, int fuse) {
	}

	private static final class State {
		UUID owner;
		ServerWorld world;
		int ticksLeft = DURATION;
		final Map<UUID, Frozen> frozen = new HashMap<>();
		final Map<UUID, Vec3d> pins = new HashMap<>();
	}

	private static State state;

	private TimeStop() {
	}

	public static boolean isActive() {
		return state != null;
	}

	public static boolean start(ServerPlayerEntity owner) {
		if (state != null) {
			return false;
		}
		State s = new State();
		s.owner = owner.getUuid();
		s.world = owner.getServerWorld();
		state = s;

		s.world.playSound(null, owner.getBlockPos(), SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 2.0f, 0.5f);
		DvizhukhaActions.tricolor(s.world, owner.getX(), owner.getY() + 1.0, owner.getZ(), 60, 1.2);
		return true;
	}

	public static void tick(MinecraftServer server) {
		if (state == null) {
			return;
		}
		State s = state;
		ServerPlayerEntity owner = server.getPlayerManager().getPlayer(s.owner);
		if (owner == null || !owner.isAlive() || owner.getWorld() != s.world) {
			end();
			return;
		}

		s.ticksLeft--;

		Box box = owner.getBoundingBox().expand(RADIUS);
		for (Entity entity : s.world.getOtherEntities(owner, box)) {
			if (isExempt(entity, owner)) {
				continue;
			}
			UUID id = entity.getUuid();
			Frozen frozen = s.frozen.get(id);
			if (frozen == null) {
				int fuse = entity instanceof TntEntity tnt ? tnt.getFuse() : -1;
				boolean ai = entity instanceof MobEntity mob && mob.isAiDisabled();
				frozen = new Frozen(entity.getVelocity(), entity.hasNoGravity(), ai, fuse);
				s.frozen.put(id, frozen);
				if (entity instanceof MobEntity mob) {
					mob.setAiDisabled(true);
				}
				entity.setNoGravity(true);
				if (entity instanceof ServerPlayerEntity player) {
					s.pins.put(id, player.getPos());
				}
			}
			hold(s, entity, frozen);
		}

		if (s.ticksLeft % 4 == 0) {
			double left = Math.max(0, s.ticksLeft) / 20.0;
			owner.sendMessage(Text.translatable("message.crazydiamond.time_stop_left", String.format("%.1f", left))
					.formatted(Formatting.AQUA), true);
		}

		if (s.ticksLeft <= 0) {
			end();
		}
	}

	private static boolean isExempt(Entity entity, ServerPlayerEntity owner) {
		if (entity instanceof StandEntity) {
			return true;
		}
		if (entity instanceof CloneEntity clone && owner.getUuid().equals(clone.getOwnerUuid())) {
			return true;
		}
		return entity.isSpectator();
	}

	private static void hold(State s, Entity entity, Frozen frozen) {
		entity.setVelocity(Vec3d.ZERO);
		entity.velocityModified = true;
		if (entity instanceof TntEntity tnt && frozen.fuse() >= 0) {
			tnt.setFuse(frozen.fuse());
		}
		if (entity instanceof ServerPlayerEntity player) {
			Vec3d pin = s.pins.get(player.getUuid());
			if (pin != null && player.getPos().squaredDistanceTo(pin) > 0.01) {
				player.networkHandler.requestTeleport(pin.x, pin.y, pin.z, player.getYaw(), player.getPitch());
			}
		}
	}

	private static void end() {
		State s = state;
		state = null;
		if (s == null) {
			return;
		}
		for (Map.Entry<UUID, Frozen> entry : s.frozen.entrySet()) {
			Entity entity = s.world.getEntity(entry.getKey());
			if (entity == null) {
				continue;
			}
			Frozen f = entry.getValue();
			if (entity instanceof MobEntity mob) {
				mob.setAiDisabled(f.aiDisabled());
			}
			entity.setNoGravity(f.noGravity());
			entity.setVelocity(f.velocity());
			entity.velocityModified = true;
		}
		ServerPlayerEntity owner = s.world.getServer().getPlayerManager().getPlayer(s.owner);
		if (owner != null) {
			s.world.playSound(null, owner.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1.0f, 0.6f);
		}
		DvizhukhaManager.setCooldown(s.owner, DvizhukhaAbility.TIME_STOP, COOLDOWN);
	}

	/** Cleans up when the server stops so nothing stays frozen. */
	public static void reset() {
		if (state != null) {
			end();
		}
	}
}
