package com.example.crazydiamond;

import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/** Tells every client which players are in the Dvizhukha form so they can show the skin. */
public final class SkinSync {
	public static final Identifier ID = new Identifier(CrazyDiamondMod.MOD_ID, "skin");

	private SkinSync() {
	}

	public static void register() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity joined = handler.getPlayer();
			PlayerPowers powers = PlayerPowers.get(server);
			for (ServerPlayerEntity other : server.getPlayerManager().getPlayerList()) {
				if (powers.isDvizhukha(other.getUuid())) {
					send(joined, other.getUuid(), true);
				}
			}
			if (powers.isDvizhukha(joined.getUuid())) {
				broadcast(server, joined.getUuid(), true);
			}
		});
	}

	public static void broadcast(MinecraftServer server, UUID id, boolean transformed) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			send(player, id, transformed);
		}
	}

	private static void send(ServerPlayerEntity to, UUID id, boolean transformed) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeUuid(id);
		buf.writeBoolean(transformed);
		ServerPlayNetworking.send(to, ID, buf);
	}
}
