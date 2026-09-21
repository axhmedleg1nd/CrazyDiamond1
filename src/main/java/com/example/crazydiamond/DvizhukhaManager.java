package com.example.crazydiamond;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Checks who may use the Dvizhukha abilities and handles cooldowns. */
public final class DvizhukhaManager {
	/** Server tick at which each ability is ready again, per player. */
	private static final Map<UUID, long[]> READY_AT = new HashMap<>();

	private DvizhukhaManager() {
	}

	public static void setCooldown(UUID player, DvizhukhaAbility ability, int ticks) {
		MinecraftServer server = CrazyDiamondMod.server;
		if (server == null) {
			return;
		}
		long[] ready = READY_AT.computeIfAbsent(player, k -> new long[DvizhukhaAbility.values().length]);
		ready[ability.ordinal()] = server.getTicks() + ticks;
	}

	public static void use(ServerPlayerEntity player, DvizhukhaAbility ability) {
		MinecraftServer server = player.getServer();
		UUID id = player.getUuid();

		if (!PlayerPowers.get(server).isDvizhukha(id)) {
			player.sendMessage(Text.translatable("message.crazydiamond.need_dvizhukha"), true);
			return;
		}

		// leaving the pocket dimension early is always allowed
		if (ability == DvizhukhaAbility.POCKET && PocketDimension.isInside(player)) {
			PocketDimension.exit(player);
			return;
		}
		if (PocketDimension.isInside(player)) {
			return; // no other abilities in there
		}

		long[] ready = READY_AT.computeIfAbsent(id, k -> new long[DvizhukhaAbility.values().length]);
		long now = server.getTicks();
		if (now < ready[ability.ordinal()]) {
			long seconds = (ready[ability.ordinal()] - now + 19) / 20;
			player.sendMessage(Text.translatable("message.crazydiamond.cooldown", seconds).formatted(Formatting.RED), true);
			return;
		}

		ServerWorld world = player.getServerWorld();
		boolean started = switch (ability) {
			case CLONES -> {
				DvizhukhaActions.clones(world, player);
				yield true;
			}
			case POWER_STRIKE -> {
				DvizhukhaActions.powerStrike(world, player);
				yield true;
			}
			case POCKET -> PocketDimension.enter(player);
			case TIME_STOP -> TimeStop.start(player);
		};

		if (started) {
			player.sendMessage(ability.displayName(), true);
			// time stop and the pocket dimension set their own cooldown when they end
			if (ability == DvizhukhaAbility.CLONES || ability == DvizhukhaAbility.POWER_STRIKE) {
				ready[ability.ordinal()] = now + ability.cooldownTicks;
			} else {
				ready[ability.ordinal()] = Long.MAX_VALUE;
			}
		}
	}

	public static void reset() {
		READY_AT.clear();
	}
}
