package com.example.crazydiamond;

import com.example.crazydiamond.item.DvizhukhaArrowItem;
import com.example.crazydiamond.item.MangoItem;
import com.example.crazydiamond.item.StandArrowItem;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public final class ModItems {
	public static final Item STAND_ARROW = Registry.register(Registries.ITEM,
			new Identifier(CrazyDiamondMod.MOD_ID, "stand_arrow"),
			new StandArrowItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE)));

	public static final Item DVIZHUKHA_ARROW = Registry.register(Registries.ITEM,
			new Identifier(CrazyDiamondMod.MOD_ID, "dvizhukha_arrow"),
			new DvizhukhaArrowItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC)));

	public static final Item MANGO = Registry.register(Registries.ITEM,
			new Identifier(CrazyDiamondMod.MOD_ID, "mango"),
			new MangoItem(new Item.Settings().maxCount(16)));

	private ModItems() {
	}

	public static void register() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
			entries.add(STAND_ARROW);
			entries.add(DVIZHUKHA_ARROW);
			entries.add(MANGO);
		});
	}
}
