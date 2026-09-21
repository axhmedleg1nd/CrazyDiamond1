package com.example.crazydiamond;

import java.util.EnumSet;
import java.util.UUID;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

/** A short-lived copy of the owner that fights monsters and whatever the owner is attacking. */
public class CloneEntity extends PathAwareEntity {
	private static final int LIFETIME = 30 * 20;

	private UUID ownerUuid;
	private int life = 0;

	public CloneEntity(EntityType<? extends CloneEntity> type, World world) {
		super(type, world);
		this.setPersistent();
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.36)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
	}

	public void setOwner(PlayerEntity owner) {
		this.ownerUuid = owner.getUuid();
	}

	public UUID getOwnerUuid() {
		return ownerUuid;
	}

	private PlayerEntity getOwnerPlayer() {
		return ownerUuid == null ? null : this.getWorld().getPlayerByUuid(ownerUuid);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(1, new SwimGoal(this));
		this.goalSelector.add(2, new MeleeAttackGoal(this, 1.25, true));
		this.goalSelector.add(3, new FollowOwnerGoal(this));
		this.goalSelector.add(4, new WanderAroundFarGoal(this, 0.8));
		this.goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
		this.goalSelector.add(6, new LookAroundGoal(this));

		this.targetSelector.add(1, new RevengeGoal(this));
		this.targetSelector.add(2, new ActiveTargetGoal<>(this, MobEntity.class, 10, true, false,
				entity -> entity instanceof Monster && !(entity instanceof CloneEntity)));
	}

	@Override
	public void tick() {
		super.tick();
		if (this.getWorld().isClient) {
			return;
		}
		PlayerEntity owner = getOwnerPlayer();
		if (owner == null || !owner.isAlive() || ++life > LIFETIME) {
			poof();
			return;
		}
		if (this.age % 10 == 0) {
			LivingEntity victim = owner.getAttacking();
			if (victim != null && victim.isAlive() && victim != this && victim != owner
					&& !(victim instanceof CloneEntity) && owner.age - owner.getLastAttackTime() < 200) {
				this.setTarget(victim);
			}
		}
	}

	private void poof() {
		if (this.getWorld() instanceof ServerWorld serverWorld) {
			DvizhukhaActions.tricolor(serverWorld, this.getX(), this.getBodyY(0.5), this.getZ(), 20, 0.4);
		}
		this.discard();
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (source.getAttacker() != null && source.getAttacker().getUuid().equals(ownerUuid)) {
			return false;
		}
		return super.damage(source, amount);
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		if (ownerUuid != null) {
			nbt.putUuid("Owner", ownerUuid);
		}
		nbt.putInt("Life", life);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		if (nbt.containsUuid("Owner")) {
			ownerUuid = nbt.getUuid("Owner");
		}
		life = nbt.getInt("Life");
	}

	/** Walks back to the owner when the clone drifts too far away. */
	private static final class FollowOwnerGoal extends Goal {
		private final CloneEntity clone;

		FollowOwnerGoal(CloneEntity clone) {
			this.clone = clone;
			this.setControls(EnumSet.of(Goal.Control.MOVE));
		}

		@Override
		public boolean canStart() {
			PlayerEntity owner = clone.getOwnerPlayer();
			return owner != null && clone.getTarget() == null && clone.squaredDistanceTo(owner) > 36.0;
		}

		@Override
		public boolean shouldContinue() {
			PlayerEntity owner = clone.getOwnerPlayer();
			return owner != null && clone.getTarget() == null && clone.squaredDistanceTo(owner) > 9.0;
		}

		@Override
		public void tick() {
			PlayerEntity owner = clone.getOwnerPlayer();
			if (owner == null) {
				return;
			}
			if (clone.squaredDistanceTo(owner) > 400.0) {
				clone.refreshPositionAndAngles(owner.getX(), owner.getY(), owner.getZ(), clone.getYaw(), 0.0f);
				clone.getNavigation().stop();
			} else {
				clone.getNavigation().startMovingTo(owner, 1.3);
			}
		}
	}
}
