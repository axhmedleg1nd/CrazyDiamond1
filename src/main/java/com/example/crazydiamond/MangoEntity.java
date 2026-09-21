package com.example.crazydiamond;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

/** A thrown mango. Harmless, just funny. */
public class MangoEntity extends ThrownItemEntity {
	public MangoEntity(EntityType<? extends MangoEntity> type, World world) {
		super(type, world);
	}

	public MangoEntity(World world, LivingEntity owner) {
		super(ModEntities.MANGO, owner, world);
	}

	@Override
	protected Item getDefaultItem() {
		return ModItems.MANGO;
	}

	@Override
	protected void onEntityHit(EntityHitResult hit) {
		super.onEntityHit(hit);
		if (this.getWorld().isClient) {
			return;
		}
		Entity target = hit.getEntity();
		target.damage(this.getWorld().getDamageSources().thrown(this, this.getOwner()), 1.0f);
	}

	@Override
	protected void onCollision(HitResult hit) {
		super.onCollision(hit);
		if (this.getWorld() instanceof ServerWorld serverWorld) {
			serverWorld.spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, this.getStack()),
					this.getX(), this.getY(), this.getZ(), 12, 0.2, 0.2, 0.2, 0.05);
			this.discard();
		}
	}
}
