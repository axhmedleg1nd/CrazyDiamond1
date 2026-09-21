package com.example.crazydiamond;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class ModSounds {
	/** Played when a mango is thrown. File: assets/crazydiamond/sounds/mango.ogg */
	public static final SoundEvent MANGO = register("mango");
	/** The awakening track for the Dvizhukha arrow. File: assets/crazydiamond/sounds/dvizhukha_theme.ogg */
	public static final SoundEvent DVIZHUKHA_THEME = register("dvizhukha_theme");

	private ModSounds() {
	}

	private static SoundEvent register(String name) {
		Identifier id = new Identifier(CrazyDiamondMod.MOD_ID, name);
		return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
	}

	/** Called from the mod initializer just to force this class to load. */
	public static void register() {
	}
}
