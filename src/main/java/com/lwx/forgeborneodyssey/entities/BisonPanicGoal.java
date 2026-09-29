package com.lwx.forgeborneodyssey.entities;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 野牛专用的恐慌奔跑目标。
 * 相比原版 {@link PanicGoal}：
 * - 奔跑半径从 5 格增加到 20 格，让野牛逃到远处
 * - 到达一个目标后如果还在恐慌状态，会继续寻找新目标继续跑
 * - 被攻击时会先评估实力：打得过就不跑，打不过才跑
 */
public class BisonPanicGoal extends PanicGoal {

    private static final int PANIC_RADIUS = 20;
    private final BisonEntity bison;

    public BisonPanicGoal(BisonEntity bison, double speedModifier) {
        super(bison, speedModifier);
        this.bison = bison;
    }

    @Override
    public boolean canUse() {
        if (this.mob.isOnFire() || this.mob.isFreezing()) {
            return true;
        }

        LivingEntity lastHurt = this.mob.getLastHurtByMob();
        if (lastHurt == null) {
            return false;
        }

        if (this.bison.getTarget() != null) {
            return false;
        }

        return !BisonEntity.canWinFight(this.bison, lastHurt);
    }

    /**
     * 使用更大的搜索半径（20格，原版5格），并偏向远离攻击者的方向，避免来回折返。
     */
    @Override
    protected boolean findRandomPosition() {
        Vec3 vec3 = DefaultRandomPos.getPos(this.mob, PANIC_RADIUS, 7);
        if (vec3 == null) {
            return super.findRandomPosition();
        }

        LivingEntity lastHurt = this.mob.getLastHurtByMob();
        if (lastHurt != null) {
            Vec3 away = this.mob.position().subtract(lastHurt.position());
            if (away.lengthSqr() > 1.0E-7) {
                vec3 = vec3.add(away.normalize().scale(PANIC_RADIUS * 0.6));
            }
        }

        this.posX = vec3.x;
        this.posY = vec3.y;
        this.posZ = vec3.z;
        return true;
    }

    @Override
    public void start() {
        super.start();

        // 恐慌传染：让附近没有目标的成年野牛也一起逃跑
        LivingEntity lastHurt = this.mob.getLastHurtByMob();
        if (lastHurt != null) {
            List<BisonEntity> nearby = this.mob.level().getEntitiesOfClass(
                BisonEntity.class,
                this.mob.getBoundingBox().inflate(10.0),
                e -> e != this.mob && !e.isBaby() && e.getTarget() == null && !e.isOnFire() && !e.isFreezing());
            for (BisonEntity ally : nearby) {
                if (ally.getLastHurtByMob() == null) {
                    ally.setLastHurtByMob(lastHurt);
                }
            }
        }
    }

    /**
     * 只要野牛还在恐慌状态（有攻击者或着火），就持续奔跑。
     * 到达一个目标后自动寻找下一个目标，不会中途停下。
     */
    @Override
    public boolean canContinueToUse() {
        if (this.shouldPanic()) {
            if (this.mob.getNavigation().isDone()) {
                // 到达上一个目标但仍在恐慌 → 找下一个逃跑位置
                if (this.findRandomPosition()) {
                    // 手动启动导航到新目标
                    this.mob.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
                    return true;
                }
                return false; // 找不到新目标，停止
            }
            return true; // 导航未完成，继续跑
        }
        return false; // 不再恐慌，停止
    }
}