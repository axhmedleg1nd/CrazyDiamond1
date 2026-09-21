package com.example.crazydiamond;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

/** Which players got the Crazy Diamond stand / the Dvizhukha form. Saved with the world. */
public class PlayerPowers extends PersistentState {
	private static final String KEY = "crazydiamond_powers";

	private final Set<UUID> standUsers = new HashSet<>();
	private final Set<UUID> dvizhukha = new HashSet<>();

	public static PlayerPowers get(MinecraftServer server) {
		return server.getOverworld().getPersistentStateManager()
				.getOrCreate(PlayerPowers::fromNbt, PlayerPowers::new, KEY);
	}

	public static PlayerPowers fromNbt(NbtCompound nbt) {
		PlayerPowers powers = new PlayerPowers();
		for (NbtElement e : nbt.getList("Stand", NbtElement.INT_ARRAY_TYPE)) {
			powers.standUsers.add(NbtHelper.toUuid(e));
		}
		for (NbtElement e : nbt.getList("Dvizhukha", NbtElement.INT_ARRAY_TYPE)) {
			powers.dvizhukha.add(NbtHelper.toUuid(e));
		}
		return powers;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		NbtList stand = new NbtList();
		for (UUID id : standUsers) {
			stand.add(NbtHelper.fromUuid(id));
		}
		NbtList form = new NbtList();
		for (UUID id : dvizhukha) {
			form.add(NbtHelper.fromUuid(id));
		}
		nbt.put("Stand", stand);
		nbt.put("Dvizhukha", form);
		return nbt;
	}

	public boolean hasStand(UUID id) {
		return standUsers.contains(id);
	}

	public boolean isDvizhukha(UUID id) {
		return dvizhukha.contains(id);
	}

	public void giveStand(UUID id) {
		if (standUsers.add(id)) {
			markDirty();
		}
	}

	public void giveDvizhukha(UUID id) {
		if (dvizhukha.add(id)) {
			markDirty();
		}
	}

	public void reset(UUID id) {
		boolean changed = standUsers.remove(id);
		changed |= dvizhukha.remove(id);
		if (changed) {
			markDirty();
		}
	}
}
