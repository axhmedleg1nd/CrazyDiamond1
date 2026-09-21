package com.example.crazydiamond.client;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.example.crazydiamond.CrazyDiamondMod;

import net.minecraft.util.Identifier;

/** Which players currently look like "Dvizhukha" (kept in sync by the server). */
public final class ClientState {
	public static final Identifier DVIZHUKHA_SKIN =
			new Identifier(CrazyDiamondMod.MOD_ID, "textures/entity/dvizhukha.png");
	/** The skin uses the normal (4 px) arms. */
	public static final boolean DVIZHUKHA_SLIM = false;

	private static final Set<UUID> TRANSFORMED = new HashSet<>();

	private ClientState() {
	}

	public static boolean isTransformed(UUID id) {
		return TRANSFORMED.contains(id);
	}

	public static void set(UUID id, boolean transformed) {
		if (transformed) {
			TRANSFORMED.add(id);
		} else {
			TRANSFORMED.remove(id);
		}
	}

	public static void clear() {
		TRANSFORMED.clear();
	}
}
