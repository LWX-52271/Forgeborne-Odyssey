package com.lwx.forgeborneodyssey.entities;

import com.lwx.forgeborneodyssey.core.registration.ModEntities;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.tags.BlockTags;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class BisonEntity extends Animal {

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState grazeAnimationState = new AnimationState();
    public final AnimationState threatenAnimationState = new AnimationState();
    public final AnimationState restAnimationState = new AnimationState();
    public final AnimationState fightAnimationState = new AnimationState();

    public int attackAnimationRemaining = 0;
    public int fightAnimationRemaining = 0;
    public int grazeTicks = 0;
    public int threatenTicks = 0;
    public boolean resting = false;
    public int trampleCooldown = 0;
    public int fightCooldown = 0;

    public float animSpeed;

    // 网络事件ID — public 供命令和网络使用
    public static final byte EVENT_ATTACK = 64;
    public static final byte EVENT_GRAZE = 65;
    public static final byte EVENT_THREATEN = 66;
    public static final byte EVENT_REST_START = 67;
    public static final byte EVENT_REST_STOP = 68;
    public static final byte EVENT_FIGHT = 69;

    public BisonEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BisonPanicGoal(this, 2.0D));
        this.goalSelector.addGoal(2, new BisonDefendCalvesGoal(this));
        this.goalSelector.addGoal(2, new BisonAttackGoal(this, 1.8D, true));
        this.goalSelector.addGoal(2, new BisonFightGoal(this));
        this.goalSelector.addGoal(3, new BisonThreatenGoal(this));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new TemptGoal(this, 1.25D, Ingredient.of(Items.WHEAT), false));
        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.25D));
        this.goalSelector.addGoal(6, new AvoidEntityGoal<>(this, Wolf.class, 12.0F, 1.5D, 2.0D));
        this.goalSelector.addGoal(7, new BisonHerdStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(9, new BisonGrazeGoal(this));
        this.goalSelector.addGoal(10, new BisonRestGoal(this));

        this.targetSelector.addGoal(1, new BisonHurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 3.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    public int getExperienceReward() {
        return this.isBaby() ? 1 : 3;
    }

    /**
     * 评估野牛是否能打赢目标。
     * 综合考虑血量比例、攻击力对比。
     */
    public static boolean canWinFight(BisonEntity bison, LivingEntity attacker) {
        if (bison.getHealth() < bison.getMaxHealth() * 0.3) {
            return false;
        }

        double bisonDamage = bison.getAttributeValue(Attributes.ATTACK_DAMAGE);
        double attackerDamage = attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);

        double bisonEffective = bison.getHealth() * (bisonDamage + bison.getArmorValue() * 0.5);
        double attackerEffective = attacker.getHealth() * (attackerDamage + attacker.getArmorValue() * 0.5);

        if (attackerEffective > bisonEffective * 1.8) {
            return false;
        }

        List<? extends BisonEntity> nearbyBisons = bison.level().getEntitiesOfClass(
            BisonEntity.class,
            bison.getBoundingBox().inflate(10.0),
            e -> e != bison && !e.isBaby() && e.getTarget() == null);

        double totalBisonPower = bisonEffective;
        for (BisonEntity ally : nearbyBisons) {
            totalBisonPower += ally.getHealth() * (ally.getAttributeValue(Attributes.ATTACK_DAMAGE) + ally.getArmorValue() * 0.5);
        }

        return totalBisonPower >= attackerEffective * 0.6;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return ModEntities.BISON.get().create(level);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.WHEAT);
    }

    @Override
    @Nullable
    public SoundEvent getAmbientSound() {
        if (this.getTarget() != null || this.attackAnimationRemaining > 0 || this.threatenTicks > 0) {
            return SoundEvents.RAVAGER_ROAR;
        }
        return SoundEvents.COW_AMBIENT;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    public SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.COW_HURT;
    }

    @Override
    public SoundEvent getDeathSound() {
        return SoundEvents.COW_DEATH;
    }

    @Override
    public float getScale() {
        return this.isBaby() ? 0.5F : 1.0F;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt) {
            // 互顶时不再重复广播（FightGoal 已广播 EVENT_FIGHT）
            if (this.fightAnimationRemaining <= 0) {
                this.level().broadcastEntityEvent(this, EVENT_ATTACK);
            } else {
                // 互顶标记已消耗，后续攻击正常广播
                this.fightAnimationRemaining = 0;
            }

            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.RAVAGER_ROAR, this.getSoundSource(), 1.2F, 0.8F);

            if (this.level() instanceof ServerLevel serverLevel) {
                Vec3 targetPos = target.position();
                serverLevel.sendParticles(ParticleTypes.CRIT,
                    targetPos.x, targetPos.y + target.getBbHeight() / 2.0, targetPos.z,
                    8, 0.3, 0.2, 0.3, 0.1);
                serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    targetPos.x, targetPos.y + target.getBbHeight() / 2.0, targetPos.z,
                    2, 0.15, 0.1, 0.15, 0.05);
            }
        }
        return hurt;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_ATTACK) {
            this.attackAnimationRemaining = 8;
            if (this.level().isClientSide()) {
                this.attackAnimationState.start(this.tickCount);
            }
        } else if (id == EVENT_GRAZE) {
            this.grazeTicks = 60;
            if (this.level().isClientSide()) {
                this.grazeAnimationState.start(this.tickCount);
            }
        } else if (id == EVENT_THREATEN) {
            this.threatenTicks = 60;
            if (this.level().isClientSide()) {
                this.threatenAnimationState.start(this.tickCount);
            }
        } else if (id == EVENT_REST_START) {
            this.resting = true;
            if (this.level().isClientSide()) {
                this.restAnimationState.start(this.tickCount);
            }
        } else if (id == EVENT_REST_STOP) {
            this.resting = false;
            if (this.level().isClientSide()) {
                this.restAnimationState.stop();
            }
        } else if (id == EVENT_FIGHT) {
            this.fightAnimationRemaining = 20;
            if (this.level().isClientSide()) {
                this.fightAnimationState.start(this.tickCount);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.trampleCooldown > 0) {
            this.trampleCooldown--;
        }
        if (this.fightCooldown > 0) {
            this.fightCooldown--;
        }
        if (this.level().isClientSide()) {
            if (this.attackAnimationRemaining > 0) {
                this.attackAnimationRemaining--;
                if (this.attackAnimationRemaining <= 0) {
                    this.attackAnimationState.stop();
                }
            }

            if (this.fightAnimationRemaining > 0) {
                this.fightAnimationRemaining--;
                if (this.fightAnimationRemaining <= 0) {
                    this.fightAnimationState.stop();
                }
            }

            double speed = this.getDeltaMovement().horizontalDistanceSqr();

            if (this.grazeTicks > 0) {
                if (speed > 1.0E-4) {
                    this.grazeTicks = 1;
                } else {
                    this.grazeTicks--;
                }
                if (this.grazeTicks <= 0) {
                    this.grazeAnimationState.stop();
                }
            }

            if (this.threatenTicks > 0) {
                if (speed > 1.0E-4) {
                    this.threatenTicks = 1;
                } else {
                    this.threatenTicks--;
                }
                if (this.threatenTicks <= 0) {
                    this.threatenAnimationState.stop();
                }
            }

            float rawSpeed = (float)Math.sqrt(Math.max(speed, 0.0D));
            this.animSpeed += (rawSpeed - this.animSpeed) * 0.3F;

            if (this.attackAnimationState.isStarted()) {
                this.idleAnimationState.stop();
                this.walkAnimationState.stop();
                this.runAnimationState.stop();
                this.grazeAnimationState.stop();
                this.threatenAnimationState.stop();
            } else if (this.fightAnimationState.isStarted()) {
                this.idleAnimationState.stop();
                this.walkAnimationState.stop();
                this.runAnimationState.stop();
                this.grazeAnimationState.stop();
                this.threatenAnimationState.stop();
            } else if (this.threatenAnimationState.isStarted()) {
                this.idleAnimationState.stop();
                this.walkAnimationState.stop();
                this.runAnimationState.stop();
                this.grazeAnimationState.stop();

                if (this.threatenTicks % 8 == 0 && this.threatenTicks > 5) {
                    Vec3 look = this.getLookAngle();
                    double nx = this.getX() + look.x * 1.0;
                    double ny = this.getY() + 1.05;
                    double nz = this.getZ() + look.z * 1.0;
                    for (int i = 0; i < 4; i++) {
                        this.level().addParticle(
                            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GRASS_BLOCK.defaultBlockState()),
                            nx + (this.random.nextDouble() - 0.5) * 0.3,
                            ny - 0.6 + this.random.nextDouble() * 0.1,
                            nz + (this.random.nextDouble() - 0.5) * 0.3,
                            look.x * 0.2 + (this.random.nextDouble() - 0.5) * 0.06,
                            0.03 + this.random.nextDouble() * 0.04,
                            look.z * 0.2 + (this.random.nextDouble() - 0.5) * 0.06);
                    }
                }
            } else if (this.resting) {
                this.idleAnimationState.stop();
                this.walkAnimationState.stop();
                this.runAnimationState.stop();
                this.grazeAnimationState.stop();
                this.threatenAnimationState.stop();
                if (!this.restAnimationState.isStarted()) {
                    this.restAnimationState.start(this.tickCount);
                }
            } else if (this.grazeAnimationState.isStarted()) {
                this.idleAnimationState.stop();
                this.walkAnimationState.stop();
                this.runAnimationState.stop();

                if (this.grazeTicks % 4 == 0 && this.grazeTicks > 3) {
                    Vec3 look = this.getLookAngle();
                    double mx = this.getX() + look.x * 0.8;
                    double my = this.getY() + 0.15;
                    double mz = this.getZ() + look.z * 0.8;
                    for (int i = 0; i < 4; i++) {
                        this.level().addParticle(
                            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GRASS_BLOCK.defaultBlockState()),
                            mx + (this.random.nextDouble() - 0.5) * 0.2,
                            my + this.random.nextDouble() * 0.08,
                            mz + (this.random.nextDouble() - 0.5) * 0.2,
                            (this.random.nextDouble() - 0.5) * 0.04,
                            0.04 + this.random.nextDouble() * 0.04,
                            (this.random.nextDouble() - 0.5) * 0.04);
                    }
                }
            } else if (speed > 0.12D) {
                this.idleAnimationState.stop();
                this.walkAnimationState.stop();
                this.runAnimationState.startIfStopped(this.tickCount);
            } else if (speed > 1.0E-6) {
                this.idleAnimationState.stop();
                this.runAnimationState.stop();
                this.walkAnimationState.startIfStopped(this.tickCount);
            } else {
                this.idleAnimationState.startIfStopped(this.tickCount);
                this.walkAnimationState.stop();
                this.runAnimationState.stop();
            }

            if (this.runAnimationState.isStarted() && this.tickCount % 2 == 0) {
                double bx = this.getBbWidth() * 1.2;
                double x = this.getX() + (this.random.nextDouble() - 0.5) * bx;
                double z = this.getZ() + (this.random.nextDouble() - 0.5) * bx;
                this.level().addParticle(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GRASS_BLOCK.defaultBlockState()),
                    x, this.getY() + 0.05, z,
                    (this.random.nextDouble() - 0.5) * 0.1, 0.04, (this.random.nextDouble() - 0.5) * 0.1);
            }
        }

        if (this.resting) {
            this.setDeltaMovement(Vec3.ZERO);
            this.getNavigation().stop();
        }

        if (!this.level().isClientSide() && this.onGround()) {
            if (this.resting) return;
            double moveSpeed = this.getDeltaMovement().horizontalDistanceSqr();
            if (moveSpeed > 1.0E-6) {
                int stepInterval = moveSpeed > 0.12D ? 4 : 7;
                if (this.tickCount % stepInterval == 0) {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.COW_STEP, this.getSoundSource(), 0.5F,
                        moveSpeed > 0.12D ? 1.2F : 0.9F);
                }
            }

            // 奔跑时撞烂脚下的草丛、花等植物方块
            if (moveSpeed > 0.12D && this.tickCount % 3 == 0) {
                AABB bb = this.getBoundingBox();
                int minX = (int)Math.floor(bb.minX);
                int maxX = (int)Math.floor(bb.maxX);
                int minZ = (int)Math.floor(bb.minZ);
                int maxZ = (int)Math.floor(bb.maxZ);
                int y = (int)Math.floor(this.getY());
                for (int bx = minX; bx <= maxX; bx++) {
                    for (int bz = minZ; bz <= maxZ; bz++) {
                        if (this.random.nextInt(3) != 0) continue;
                        BlockPos pos = new BlockPos(bx, y, bz);
                        BlockState state = this.level().getBlockState(pos);
                        if (state.is(BlockTags.REPLACEABLE) || state.is(BlockTags.SMALL_FLOWERS)) {
                            this.level().destroyBlock(pos, true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void push(Entity other) {
        if (!this.level().isClientSide && this.getTarget() != null
                && other instanceof LivingEntity living && other != this.getTarget()) {
            float otherWidth = other.getBbWidth();
            float myWidth = this.getBbWidth();
            if (otherWidth < myWidth && this.trampleCooldown <= 0) {
                Vec3 pushDir = other.position().subtract(this.position());
                if (pushDir.lengthSqr() > 1.0E-7) {
                    pushDir = pushDir.normalize();
                    living.knockback(2.0, pushDir.x, pushDir.z);
                    living.hurt(this.damageSources().mobAttack(this),
                            (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
                    this.trampleCooldown = 10;
                }
                return;
            }
        }
        super.push(other);
    }

    private static class BisonHurtByTargetGoal extends HurtByTargetGoal {

        private final BisonEntity bison;

        public BisonHurtByTargetGoal(BisonEntity bison) {
            super(bison);
            this.bison = bison;
        }

        @Override
        public boolean canUse() {
            LivingEntity lastHurtBy = this.bison.getLastHurtByMob();
            if (lastHurtBy == null) return false;
            if (this.bison.lastHurt < 1.0F) return false;
            return super.canUse();
        }

        @Override
        public void start() {
            super.start();

            List<BisonEntity> babies = this.bison.level().getEntitiesOfClass(
                BisonEntity.class,
                this.bison.getBoundingBox().inflate(12.0),
                e -> e != this.bison && e.isBaby());
            for (BisonEntity baby : babies) {
                baby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1, false, false));
            }
        }
    }

    private static class BisonAttackGoal extends MeleeAttackGoal {

        private final double chargeSpeed;
        private int pathRecalcCooldown;
        private int attackCooldown;

        public BisonAttackGoal(BisonEntity mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(mob, speedModifier, followingTargetEvenIfNotSeen);
            this.chargeSpeed = speedModifier;
        }

        @Override
        protected double getAttackReachSqr(LivingEntity target) {
            return 4.0D + target.getBbWidth();
        }

        @Override
        public void tick() {
            LivingEntity target = this.mob.getTarget();
            if (target == null) return;

            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double dist = this.mob.distanceToSqr(target);

            if (--this.pathRecalcCooldown <= 0 || this.mob.getNavigation().isDone()) {
                this.pathRecalcCooldown = 4 + this.mob.getRandom().nextInt(7);
                this.mob.getNavigation().moveTo(target, this.chargeSpeed);
            }

            if (--this.attackCooldown <= 0) {
                this.attackCooldown = 20;
                if (dist <= this.getAttackReachSqr(target)) {
                    this.mob.doHurtTarget(target);
                }
            }
        }
    }

    private static class BisonGrazeGoal extends Goal {

        private final BisonEntity bison;
        private int grazeTimer;
        private int cooldown;

        public BisonGrazeGoal(BisonEntity bison) {
            this.bison = bison;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.cooldown > 0) {
                this.cooldown--;
                return false;
            }
            if (this.bison.getTarget() != null) return false;
            if (this.bison.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4) return false;
            if (!this.bison.onGround()) return false;
            return this.bison.getRandom().nextInt(150) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.grazeTimer > 0;
        }

        @Override
        public void start() {
            this.grazeTimer = 60;
            this.bison.level().broadcastEntityEvent(this.bison, EVENT_GRAZE);
        }

        @Override
        public void tick() {
            this.grazeTimer--;
        }

        @Override
        public void stop() {
            this.grazeTimer = 0;
            this.cooldown = 100 + this.bison.getRandom().nextInt(200);
        }

        @Override
        public boolean isInterruptable() {
            return true;
        }
    }

    private static class BisonThreatenGoal extends Goal {

        private final BisonEntity bison;
        private int threatenTimer;
        private int cooldown;

        public BisonThreatenGoal(BisonEntity bison) {
            this.bison = bison;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.cooldown > 0) {
                this.cooldown--;
                return false;
            }
            LivingEntity target = this.bison.getTarget();
            if (target == null || !target.isAlive()) return false;
            double dist = this.bison.distanceToSqr(target);
            if (dist < 9.0 || dist > 144.0) return false;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.threatenTimer <= 0) return false;
            LivingEntity target = this.bison.getTarget();
            if (target == null || !target.isAlive()) return false;
            double dist = this.bison.distanceToSqr(target);
            return dist >= 6.25 && dist <= 144.0;
        }

        @Override
        public void start() {
            this.threatenTimer = 60;
            this.bison.navigation.stop();
            this.bison.level().broadcastEntityEvent(this.bison, EVENT_THREATEN);

            LivingEntity target = this.bison.getTarget();
            if (target != null) {
                List<BisonEntity> nearby = this.bison.level().getEntitiesOfClass(
                    BisonEntity.class,
                    this.bison.getBoundingBox().inflate(8.0),
                    e -> e != this.bison && e.getTarget() == null && !e.isBaby() && !e.resting);
                for (BisonEntity ally : nearby) {
                    ally.setTarget(target);
                    ally.threatenTicks = 60;
                    ally.navigation.stop();
                    this.bison.level().broadcastEntityEvent(ally, EVENT_THREATEN);
                }
            }
        }

        @Override
        public void tick() {
            this.threatenTimer--;
            LivingEntity target = this.bison.getTarget();
            if (target != null) {
                this.bison.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
            if (this.threatenTimer % 20 == 0 && this.bison.getTarget() != null) {
                this.bison.playSound(SoundEvents.RAVAGER_ROAR, 0.6F, 0.7F + this.bison.getRandom().nextFloat() * 0.4F);
            }
        }

        @Override
        public void stop() {
            this.threatenTimer = 0;
            this.cooldown = 40 + this.bison.getRandom().nextInt(80);
        }

        @Override
        public boolean isInterruptable() {
            return true;
        }
    }

    private static class BisonFightGoal extends Goal {

        private final BisonEntity bison;
        private BisonEntity opponent;
        private int fightTimer;
        private int chargeCooldown;
        private int cooldown;

        public BisonFightGoal(BisonEntity bison) {
            this.bison = bison;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.cooldown > 0) {
                this.cooldown--;
                return false;
            }
            if (this.bison.fightCooldown > 0) return false;
            if (this.bison.isBaby()) return false;
            if (this.bison.getTarget() != null) return false;
            if (this.bison.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4) return false;
            if (!this.bison.onGround()) return false;
            if (this.bison.getRandom().nextInt(300) != 0) return false;

            java.util.List<BisonEntity> nearby = this.bison.level().getEntitiesOfClass(
                BisonEntity.class,
                this.bison.getBoundingBox().inflate(5.0),
                e -> e != this.bison && !e.isBaby() && e.getTarget() == null);
            if (!nearby.isEmpty()) {
                this.opponent = nearby.get(this.bison.getRandom().nextInt(nearby.size()));
                return true;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.fightTimer <= 0) return false;
            if (this.opponent == null || !this.opponent.isAlive()) return false;
            double dist = this.bison.distanceToSqr(this.opponent);
            return dist < 36.0;
        }

        @Override
        public void start() {
            this.fightTimer = 80 + this.bison.getRandom().nextInt(80);
            this.chargeCooldown = 0;
            this.bison.navigation.stop();
        }

        @Override
        public void tick() {
            this.fightTimer--;
            if (this.chargeCooldown > 0) {
                this.chargeCooldown--;
            }

            if (this.opponent != null) {
                this.bison.getLookControl().setLookAt(this.opponent, 30.0F, 30.0F);

                if (this.chargeCooldown <= 0 && this.fightTimer > 20
                    && this.bison.distanceToSqr(this.opponent) < 9.0) {
                    this.chargeCooldown = 20 + this.bison.getRandom().nextInt(20);

                    // 播放互顶动画 + 音效
                    this.bison.fightAnimationRemaining = 20; // 先在服务端设标记
                    this.bison.level().broadcastEntityEvent(this.bison, EVENT_FIGHT);
                    this.bison.level().playSound(null, this.bison.getX(), this.bison.getY(), this.bison.getZ(),
                        SoundEvents.RAVAGER_ROAR, this.bison.getSoundSource(), 0.8F, 0.6F);

                    // 僵持推挤，不造成伤害
                    Vec3 pushDir = this.opponent.position().subtract(this.bison.position());
                    if (pushDir.lengthSqr() > 1.0E-7) {
                        this.opponent.knockback(2.5, pushDir.x, pushDir.z);
                    }

                    if (this.bison.level() instanceof ServerLevel sl) {
                        Vec3 mid = this.opponent.position().add(this.bison.position()).scale(0.5);
                        sl.sendParticles(ParticleTypes.CRIT,
                            mid.x, mid.y + 0.5, mid.z,
                            5, 0.4, 0.2, 0.4, 0.1);
                    }
                }
            }
        }

        @Override
        public void stop() {
            this.fightTimer = 0;
            this.chargeCooldown = 0;
            int cd = 300 + this.bison.getRandom().nextInt(600);
            this.bison.fightCooldown = cd;
            if (this.opponent != null) {
                this.opponent.fightCooldown = cd;
            }
            this.opponent = null;
            this.cooldown = cd;
        }

        @Override
        public boolean isInterruptable() {
            return true;
        }
    }

    private static class BisonRestGoal extends Goal {

        private final BisonEntity bison;
        private int restTimer;
        private int cooldown;

        public BisonRestGoal(BisonEntity bison) {
            this.bison = bison;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (this.cooldown > 0) {
                this.cooldown--;
                return false;
            }
            if (this.bison.getTarget() != null) return false;
            if (this.bison.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4) return false;
            if (!this.bison.onGround()) return false;
            long dayTime = this.bison.level().getDayTime() % 24000;
            if (dayTime < 12000 || dayTime > 23000) return false;
            return this.bison.getRandom().nextInt(200) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.restTimer <= 0) return false;
            if (this.bison.getTarget() != null) return false;
            return true;
        }

        @Override
        public void start() {
            this.restTimer = 100 + this.bison.getRandom().nextInt(200);
            this.bison.resting = true;
            this.bison.level().broadcastEntityEvent(this.bison, EVENT_REST_START);
        }

        @Override
        public void tick() {
            this.restTimer--;
        }

        @Override
        public void stop() {
            this.restTimer = 0;
            this.bison.resting = false;
            this.bison.level().broadcastEntityEvent(this.bison, EVENT_REST_STOP);
            this.cooldown = 200 + this.bison.getRandom().nextInt(600);
        }

        @Override
        public boolean isInterruptable() {
            return true;
        }
    }

    private static class BisonHerdStrollGoal extends Goal {

        private final BisonEntity bison;
        private final double speedModifier;
        @Nullable
        private BlockPos wantedPos;
        private static final int HERD_RADIUS = 10;

        public BisonHerdStrollGoal(BisonEntity bison, double speedModifier) {
            this.bison = bison;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.bison.getTarget() != null) return false;
            if (this.bison.isVehicle()) return false;
            if (this.bison.resting) return false;
            if (this.bison.getRandom().nextInt(120) != 0) return false;

            return this.findPosition();
        }

        private boolean findPosition() {
            List<BisonEntity> nearby = this.bison.level().getEntitiesOfClass(
                BisonEntity.class,
                this.bison.getBoundingBox().inflate(HERD_RADIUS),
                e -> e != this.bison);

            if (!nearby.isEmpty()) {
                double nearestDist = Double.MAX_VALUE;
                for (BisonEntity other : nearby) {
                    double d = this.bison.position().distanceToSqr(other.position());
                    if (d < nearestDist) nearestDist = d;
                }
                boolean tooClose = nearestDist < 4.0;

                if (!tooClose && this.bison.getRandom().nextFloat() < 0.6F) {
                    Vec3 herdCenter = Vec3.ZERO;
                    for (BisonEntity other : nearby) {
                        herdCenter = herdCenter.add(other.position());
                    }
                    herdCenter = herdCenter.scale(1.0 / nearby.size());

                    double dist = herdCenter.distanceTo(this.bison.position());
                    if (dist > 3.0) {
                        Vec3 targetPos = herdCenter.add(
                            (this.bison.getRandom().nextDouble() - 0.5) * 6.0,
                            0,
                            (this.bison.getRandom().nextDouble() - 0.5) * 6.0
                        );
                        this.wantedPos = BlockPos.containing(targetPos);
                        return true;
                    }
                }
            }

            Vec3 randomPos = DefaultRandomPos.getPos(this.bison, 10, 7);
            if (randomPos != null) {
                this.wantedPos = BlockPos.containing(randomPos);
                return true;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.wantedPos != null
                && !this.bison.getNavigation().isDone()
                && this.bison.getTarget() == null
                && !this.bison.resting;
        }

        @Override
        public void start() {
            if (this.wantedPos != null) {
                this.bison.getNavigation().moveTo(
                    this.wantedPos.getX() + 0.5,
                    this.wantedPos.getY(),
                    this.wantedPos.getZ() + 0.5,
                    this.speedModifier);
            }
        }

        @Override
        public void stop() {
            this.wantedPos = null;
        }

        @Override
        public boolean isInterruptable() {
            return true;
        }
    }

    private static class BisonDefendCalvesGoal extends Goal {

        private final BisonEntity bison;
        @Nullable
        private BlockPos defendPos;

        public BisonDefendCalvesGoal(BisonEntity bison) {
            this.bison = bison;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.bison.isBaby()) return false;
            LivingEntity target = this.bison.getTarget();
            if (target == null) return false;
            if (this.bison.getRandom().nextInt(40) != 0) return false;

            List<BisonEntity> babies = this.bison.level().getEntitiesOfClass(
                BisonEntity.class,
                this.bison.getBoundingBox().inflate(10.0),
                e -> e != this.bison && e.isBaby());
            if (babies.isEmpty()) return false;

            double selfToTarget = this.bison.distanceToSqr(target);
            for (BisonEntity baby : babies) {
                double babyToTarget = baby.distanceToSqr(target);
                if (babyToTarget < selfToTarget && babyToTarget < 64.0) {
                    Vec3 awayFromTarget = baby.position().subtract(target.position()).normalize();
                    Vec3 pos = baby.position().add(awayFromTarget.scale(3.0));
                    pos = pos.add(
                        (this.bison.getRandom().nextDouble() - 0.5) * 2.0,
                        0,
                        (this.bison.getRandom().nextDouble() - 0.5) * 2.0
                    );
                    this.defendPos = BlockPos.containing(pos);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.defendPos != null
                && !this.bison.getNavigation().isDone()
                && this.bison.getTarget() != null;
        }

        @Override
        public void start() {
            if (this.defendPos != null) {
                this.bison.getNavigation().moveTo(
                    this.defendPos.getX() + 0.5,
                    this.defendPos.getY(),
                    this.defendPos.getZ() + 0.5,
                    1.2D);
            }
        }

        @Override
        public void stop() {
            this.defendPos = null;
        }

        @Override
        public boolean isInterruptable() {
            return true;
        }
    }
}