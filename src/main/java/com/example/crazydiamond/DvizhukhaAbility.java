package com.example.crazydiamond;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Abilities of the "Dvizhukha" form (unlocked by the Dvizhukha arrow). */
public enum DvizhukhaAbility {
	CLONES("clones", 30 * 20),
	POWER_STRIKE("power_strike", 8 * 20),
	POCKET("pocket", 45 * 20),
	TIME_STOP("time_stop", 20 * 20);

	/** Network action ids for these abilities start here (0..99 belong to the stand). */
	public static final int ACTION_BASE = 100;

	public final String id;
	public final int cooldownTicks;

	DvizhukhaAbility(String id, int cooldownTicks) {
		this.id = id;
		this.cooldownTicks = cooldownTicks;
	}

	public MutableText displayName() {
		return Text.translatable("dvizhukha." + CrazyDiamondMod.MOD_ID + "." + id).formatted(Formatting.AQUA);
	}

	public String keyTranslation() {
		return "key." + CrazyDiamondMod.MOD_ID + "." + id;
	}

	public int action() {
		return ACTION_BASE + ordinal();
	}
}
