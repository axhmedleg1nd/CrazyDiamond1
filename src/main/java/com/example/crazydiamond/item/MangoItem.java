package com.example.crazydiamond.item;

import com.example.crazydiamond.MangoEntity;
import com.example.crazydiamond.ModSounds;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** Throw it and it shouts "MANGO". */
public class MangoItem extends Item {
	public MangoItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (!world.isClient) {
			world.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.MANGO, SoundCategory.PLAYERS, 1.0f, 1.0f);
			MangoEntity mango = new MangoEntity(world, user);
			mango.setItem(stack);
			mango.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 1.5f, 1.0f);
			world.spawnEntity(mango);
		}
		user.incrementStat(Stats.USED.getOrCreateStat(this));
		if (!user.getAbilities().creativeMode) {
			stack.decrement(1);
		}
		return TypedActionResult.success(stack, world.isClient());
	}
}
