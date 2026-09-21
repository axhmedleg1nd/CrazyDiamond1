package com.example.crazydiamond.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.example.crazydiamond.client.ClientState;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;

/** Swaps the skin (and arm model) of players who are in the Dvizhukha form. */
@Mixin(AbstractClientPlayerEntity.class)
public abstract class PlayerSkinMixin {
	@Inject(method = "getSkinTexture", at = @At("HEAD"), cancellable = true)
	private void crazydiamond$skin(CallbackInfoReturnable<Identifier> cir) {
		AbstractClientPlayerEntity self = (AbstractClientPlayerEntity) (Object) this;
		if (ClientState.isTransformed(self.getUuid())) {
			cir.setReturnValue(ClientState.DVIZHUKHA_SKIN);
		}
	}

	@Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
	private void crazydiamond$model(CallbackInfoReturnable<String> cir) {
		AbstractClientPlayerEntity self = (AbstractClientPlayerEntity) (Object) this;
		if (ClientState.isTransformed(self.getUuid())) {
			cir.setReturnValue(ClientState.DVIZHUKHA_SLIM ? "slim" : "default");
		}
	}
}
