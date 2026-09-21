package com.example.crazydiamond;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * /crazydiamond reset   - take away the stand and the Dvizhukha form (and the skin)
 * /crazydiamond arrows  - give yourself both arrows
 * /crazydiamond mango   - give yourself 16 mangoes
 * Both need operator permissions (cheats on in single player).
 */
public final class ModCommands {
	private ModCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(CommandManager.literal("crazydiamond")
						.requires(source -> source.hasPermissionLevel(2))
						.then(CommandManager.literal("reset").executes(context -> {
							ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
							PlayerPowers.get(player.getServer()).reset(player.getUuid());
							SkinSync.broadcast(player.getServer(), player.getUuid(), false);
							context.getSource().sendFeedback(() -> Text.translatable("message.crazydiamond.reset_done"), false);
							return 1;
						}))
						.then(CommandManager.literal("mango").executes(context -> {
							ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
							player.giveItemStack(new ItemStack(ModItems.MANGO, 16));
							return 1;
						}))
						.then(CommandManager.literal("arrows").executes(context -> {
							ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
							player.giveItemStack(new ItemStack(ModItems.STAND_ARROW));
							player.giveItemStack(new ItemStack(ModItems.DVIZHUKHA_ARROW));
							return 1;
						}))));
	}
}
