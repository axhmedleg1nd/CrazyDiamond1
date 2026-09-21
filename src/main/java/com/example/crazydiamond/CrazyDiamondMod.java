package com.example.crazydiamond;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrazyDiamondMod implements ModInitializer {
	public static final String MOD_ID = "crazydiamond";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/** The running server (integrated or dedicated), or null when none is running. */
	public static MinecraftServer server;

	@Override
	public void onInitialize() {
		ModSounds.register();
		ModItems.register();
		ModEntities.register();
		StandNetworking.register();
		SkinSync.register();
		ModCommands.register();

		ServerLifecycleEvents.SERVER_STARTED.register(s -> server = s);
		ServerLifecycleEvents.SERVER_STOPPING.register(s -> {
			TimeStop.reset();
			PocketDimension.reset();
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(s -> {
			server = null;
			DvizhukhaManager.reset();
		});

		ServerTickEvents.END_SERVER_TICK.register(s -> {
			TimeStop.tick(s);
			PocketDimension.tick(s);
		});

		LOGGER.info("Crazy Diamond loaded");
	}
}
