package com.example.crazydiamond.item;

import java.util.List;

import com.example.crazydiamond.PlayerPowers;
import com.example.crazydiamond.StandActions;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** Right-click to awaken the Crazy Diamond stand. */
public class StandArrowItem extends Item {
	public StandArrowItem(Settings settings) {
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
		if (powers.hasStand(player.getUuid())) {
			player.sendMessage(Text.translatable("message.crazydiamond.already_stand"), true);
			return TypedActionResult.fail(stack);
		}

		powers.giveStand(player.getUuid());
		ServerWorld serverWorld = player.getServerWorld();
		StandActions.gold(serverWorld, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.7);
		serverWorld.playSound(null, player.getBlockPos(), SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.PLAYERS, 1.0f, 1.2f);
		player.sendMessage(Text.translatable("message.crazydiamond.got_stand"), true);

		if (!player.isCreative()) {
			stack.decrement(1);
		}
		return TypedActionResult.success(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(Text.translatable("item.crazydiamond.stand_arrow.tooltip").formatted(Formatting.GRAY));
	}
}
