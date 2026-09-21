package com.example.crazydiamond.client;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.lwjgl.glfw.GLFW;

import com.example.crazydiamond.Ability;
import com.example.crazydiamond.DvizhukhaAbility;
import com.example.crazydiamond.ModEntities;
import com.example.crazydiamond.SkinSync;
import com.example.crazydiamond.StandEntity;
import com.example.crazydiamond.StandNetworking;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;

public class CrazyDiamondClient implements ClientModInitializer {
	private static final String CATEGORY = "category.crazydiamond";

	/** A key that sends one network action (stand ability or Dvizhukha ability). */
	private record ActionKey(KeyBinding key, int action) {
	}

	private static KeyBinding summonKey;
	private static final List<ActionKey> ACTION_KEYS = new ArrayList<>();

	@Override
	public void onInitializeClient() {
		summonKey = register("key.crazydiamond.summon", GLFW.GLFW_KEY_V);

		// Stand: Punch is on left click; every other ability has its own key.
		bind(Ability.BARRAGE.keyTranslation(), StandNetworking.actionFor(Ability.BARRAGE), GLFW.GLFW_KEY_R);
		bind(Ability.RETURN_BLOCK.keyTranslation(), StandNetworking.actionFor(Ability.RETURN_BLOCK), GLFW.GLFW_KEY_Z);
		bind(Ability.HEAL_MODE.keyTranslation(), StandNetworking.actionFor(Ability.HEAL_MODE), GLFW.GLFW_KEY_X);
		bind(Ability.STONE_SHOT.keyTranslation(), StandNetworking.actionFor(Ability.STONE_SHOT), GLFW.GLFW_KEY_C);
		bind(Ability.DISASSEMBLE.keyTranslation(), StandNetworking.actionFor(Ability.DISASSEMBLE), GLFW.GLFW_KEY_G);
		bind(Ability.REPAIR_ITEM.keyTranslation(), StandNetworking.actionFor(Ability.REPAIR_ITEM), GLFW.GLFW_KEY_B);

		// Dvizhukha form
		bind(DvizhukhaAbility.CLONES.keyTranslation(), DvizhukhaAbility.CLONES.action(), GLFW.GLFW_KEY_H);
		bind(DvizhukhaAbility.POWER_STRIKE.keyTranslation(), DvizhukhaAbility.POWER_STRIKE.action(), GLFW.GLFW_KEY_J);
		bind(DvizhukhaAbility.POCKET.keyTranslation(), DvizhukhaAbility.POCKET.action(), GLFW.GLFW_KEY_K);
		bind(DvizhukhaAbility.TIME_STOP.keyTranslation(), DvizhukhaAbility.TIME_STOP.action(), GLFW.GLFW_KEY_Y);

		EntityModelLayerRegistry.registerModelLayer(StandModel.LAYER, StandModel::getTexturedModelData);
		EntityRendererRegistry.register(ModEntities.STAND, StandRenderer::new);
		EntityRendererRegistry.register(ModEntities.STONE_SHOT, FlyingItemEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.CLONE, CloneRenderer::new);
		EntityRendererRegistry.register(ModEntities.MANGO, FlyingItemEntityRenderer::new);

		// Skin sync: the server tells us who is in the Dvizhukha form.
		ClientPlayNetworking.registerGlobalReceiver(SkinSync.ID, (client, handler, buf, responseSender) -> {
			UUID id = buf.readUuid();
			boolean transformed = buf.readBoolean();
			client.execute(() -> ClientState.set(id, transformed));
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientState.clear());

		// Left click (click or hold) = stand punch while the stand is summoned.
		ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> {
			if (client.currentScreen == null && hasStand(client)) {
				send(StandNetworking.actionFor(Ability.PUNCH));
				return true; // cancel the normal attack / block breaking
			}
			return false;
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) {
				return;
			}
			while (summonKey.wasPressed()) {
				send(StandNetworking.SUMMON);
			}
			for (ActionKey entry : ACTION_KEYS) {
				while (entry.key().wasPressed()) {
					send(entry.action());
				}
			}
		});
	}

	private static KeyBinding register(String translationKey, int glfwKey) {
		return KeyBindingHelper.registerKeyBinding(
				new KeyBinding(translationKey, InputUtil.Type.KEYSYM, glfwKey, CATEGORY));
	}

	private static void bind(String translationKey, int action, int glfwKey) {
		ACTION_KEYS.add(new ActionKey(register(translationKey, glfwKey), action));
	}

	private static boolean hasStand(MinecraftClient client) {
		ClientPlayerEntity player = client.player;
		if (player == null || client.world == null) {
			return false;
		}
		UUID id = player.getUuid();
		return !client.world.getEntitiesByClass(StandEntity.class,
				player.getBoundingBox().expand(16.0), stand -> id.equals(stand.getOwnerUuid())).isEmpty();
	}

	private static void send(int action) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeVarInt(action);
		ClientPlayNetworking.send(StandNetworking.ACTION, buf);
	}
}
