package com.example.crazydiamond;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * A private void dimension with a small tricolour platform. You can stay up to 1:30,
 * then you are sent back to where you were (or press the key again to leave early).
 * The dimension itself is defined by data/crazydiamond/dimension/pocket.json.
 */
public final class PocketDimension {
	public static final RegistryKey<World> KEY =
			RegistryKey.of(RegistryKeys.WORLD, new Identifier(CrazyDiamondMod.MOD_ID, "pocket"));

	public static final int MAX_TICKS = 90 * 20;
	public static final int COOLDOWN = 45 * 20;

	private static final int PLATFORM_Y = 64;
	private static final int HALF = 9;

	private record Session(RegistryKey<World> world, Vec3d pos, float yaw, float pitch, long endTick) {
	}

	private static final Map<UUID, Session> SESSIONS = new HashMap<>();

	private PocketDimension() {
	}

	public static boolean isInside(ServerPlayerEntity player) {
		return player.getWorld().getRegistryKey().equals(KEY);
	}

	public static boolean enter(ServerPlayerEntity player) {
		MinecraftServer server = player.getServer();
		ServerWorld pocket = server.getWorld(KEY);
		if (pocket == null) {
			player.sendMessage(Text.translatable("message.crazydiamond.pocket_missing"), true);
			CrazyDiamondMod.LOGGER.warn("Pocket dimension {} is not loaded (is the mod's data pack active?)", KEY.getValue());
			return false;
		}

		buildPlatform(pocket);
		SESSIONS.put(player.getUuid(), new Session(player.getWorld().getRegistryKey(), player.getPos(),
				player.getYaw(), player.getPitch(), server.getTicks() + MAX_TICKS));

		player.teleport(pocket, 0.5, PLATFORM_Y + 1.0, 0.5, player.getYaw(), 0.0f);
		player.fallDistance = 0.0f;
		pocket.playSound(null, new BlockPos(0, PLATFORM_Y + 1, 0), SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 0.4f, 1.6f);
		return true;
	}

	public static void exit(ServerPlayerEntity player) {
		MinecraftServer server = player.getServer();
		Session session = SESSIONS.remove(player.getUuid());

		ServerWorld target = session == null ? null : server.getWorld(session.world());
		if (target == null) {
			target = server.getOverworld();
			session = null;
		}
		Vec3d pos = session == null ? Vec3d.ofBottomCenter(target.getSpawnPos()) : session.pos();
		float yaw = session == null ? player.getYaw() : session.yaw();
		float pitch = session == null ? 0.0f : session.pitch();

		player.teleport(target, pos.x, pos.y, pos.z, yaw, pitch);
		player.fallDistance = 0.0f;
		target.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 0.4f, 1.2f);
		DvizhukhaManager.setCooldown(player.getUuid(), DvizhukhaAbility.POCKET, COOLDOWN);
	}

	public static void tick(MinecraftServer server) {
		long now = server.getTicks();
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			UUID id = player.getUuid();
			boolean inside = isInside(player);
			Session session = SESSIONS.get(id);

			if (inside && session == null) {
				exit(player); // e.g. server restarted while the player was inside
				continue;
			}
			if (!inside) {
				if (session != null) {
					SESSIONS.remove(id); // left some other way (death, command...)
					DvizhukhaManager.setCooldown(id, DvizhukhaAbility.POCKET, COOLDOWN);
				}
				continue;
			}

			long left = session.endTick() - now;
			if (left <= 0) {
				exit(player);
				continue;
			}
			if (now % 20 == 0) {
				long seconds = (left + 19) / 20;
				player.sendMessage(Text.translatable("message.crazydiamond.pocket_left", seconds / 60 + ":" + String.format("%02d", seconds % 60))
						.formatted(Formatting.AQUA), true);
			}
			// don't let the player wander off the platform / fall into the void
			if (player.getY() < PLATFORM_Y - 8 || Math.abs(player.getX()) > HALF + 4 || Math.abs(player.getZ()) > HALF + 4) {
				player.teleport(player.getServerWorld(), 0.5, PLATFORM_Y + 1.0, 0.5, player.getYaw(), player.getPitch());
				player.fallDistance = 0.0f;
			}
		}
	}

	/** White / blue / red stripes with sea lanterns in the corners. Built once. */
	private static void buildPlatform(ServerWorld world) {
		BlockPos marker = new BlockPos(0, PLATFORM_Y, 0);
		if (!world.getBlockState(marker).isAir()) {
			return;
		}
		for (int x = -HALF; x <= HALF; x++) {
			for (int z = -HALF; z <= HALF; z++) {
				Block block;
				if (z < -3) {
					block = Blocks.WHITE_CONCRETE;
				} else if (z <= 3) {
					block = Blocks.BLUE_CONCRETE;
				} else {
					block = Blocks.RED_CONCRETE;
				}
				world.setBlockState(new BlockPos(x, PLATFORM_Y, z), block.getDefaultState());
			}
		}
		int[] corners = {-HALF, HALF};
		for (int x : corners) {
			for (int z : corners) {
				world.setBlockState(new BlockPos(x, PLATFORM_Y, z), Blocks.SEA_LANTERN.getDefaultState());
			}
		}
	}

	public static void reset() {
		SESSIONS.clear();
	}
}
