package com.example.crazydiamond.client;

import com.example.crazydiamond.CloneEntity;

import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

/** Draws the clones as a player with the Dvizhukha skin. */
public class CloneRenderer extends BipedEntityRenderer<CloneEntity, PlayerEntityModel<CloneEntity>> {
	public CloneRenderer(EntityRendererFactory.Context context) {
		super(context, new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), ClientState.DVIZHUKHA_SLIM), 0.5f);
	}

	@Override
	public Identifier getTexture(CloneEntity entity) {
		return ClientState.DVIZHUKHA_SKIN;
	}
}
