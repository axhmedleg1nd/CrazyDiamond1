package com.example.crazydiamond.item;

import java.util.List;

import com.example.crazydiamond.DvizhukhaActions;
import com.example.crazydiamond.PlayerPowers;
import com.example.crazydiamond.SkinSync;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** Right-click to become "Dvizhukha": new skin + four abilities. */
public class DvizhukhaArrowItem extends Item {
	public DvizhukhaArrowItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (world.isClient) {
			return TypedActionResult.consume(stack);
		}

		ServerPlayerEntity player = (ServerPlayerEntity) user;
		PlayerPowers powers = PlayerPowers.get(player.getServer());
		if (powers.isDvizhukha(player.getUuid())) {
			player.sendMessage(Text.translatable("message.crazydiamond.already_dvizhukha"), true);
			return TypedActionResult.fail(stack);
		}

		powers.giveDvizhukha(player.getUuid());
		SkinSync.broadcast(player.getServer(), player.getUuid(), true);

		ServerWorld serverWorld = player.getServerWorld();
		DvizhukhaActions.tricolor(serverWorld, player.getX(), player.getY() + 1.0, player.getZ(), 80, 0.9);
		serverWorld.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 1.0f, 1.0f);
		player.sendMessage(Text.translatable("message.crazydiamond.got_dvizhukha"), true);

		if (!player.isCreative()) {
			stack.decrement(1);
		}
		return TypedActionResult.success(stack);
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		return true;
	}

	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(Text.translatable("item.crazydiamond.dvizhukha_arrow.tooltip").formatted(Formatting.GRAY));
	}
}
