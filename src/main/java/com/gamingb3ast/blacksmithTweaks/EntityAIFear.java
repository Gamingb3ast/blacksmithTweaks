package com.gamingb3ast.blacksmithTweaks;

import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.Vec3;

public class EntityAIFear extends EntityAIAvoidEntity {

    public static final IEntitySelector field_98218_a = new IEntitySelector() {

        /**
         * Return whether the specified entity is applicable to this filter.
         */
        public boolean isEntityApplicable(Entity entity) {
            return entity.isEntityAlive() && entity instanceof EntityMob
                && ((EntityLivingBase) entity).getMaxHealth() <= 30.0F;
        }
    };
    /** The entity we are attached to */
    private EntityCreature theEntity;
    private double farSpeed;
    private double nearSpeed;
    private Entity closestLivingEntity;
    private float distanceFromEntity;
    /** The PathEntity of our entity */
    private PathEntity entityPathEntity;
    /** The PathNavigate of our entity */
    private PathNavigate entityPathNavigate;
    /** The class of the entity we should avoid */
    private Class targetEntityClass;
    private float fearFactor = 0.0F;

    public EntityAIFear(EntityCreature entityFearing, Class<? extends net.minecraft.entity.Entity> entityFeared,
        float distance, double farSpeed, double nearSpeed, float fearFactor) {
        super(entityFearing, entityFeared, distance, farSpeed, nearSpeed);
        this.theEntity = entityFearing;
        this.targetEntityClass = entityFeared;
        this.distanceFromEntity = distance;
        this.farSpeed = farSpeed;
        this.nearSpeed = nearSpeed;
        this.entityPathNavigate = entityFearing.getNavigator();
        this.setMutexBits(1 | 2 | 3 | 8);
        setFearFactor(fearFactor);

    }

    public void setFearFactor(float factor) {
        fearFactor = factor;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute() {
        if (fearFactor <= 0) return false;
        if (this.targetEntityClass == EntityPlayer.class) {

            this.closestLivingEntity = this.theEntity.worldObj
                .getClosestPlayerToEntity(this.theEntity, this.distanceFromEntity);

            if (this.closestLivingEntity == null || !this.closestLivingEntity.isEntityAlive()) return false;
        }
        Vec3 vec3 = RandomPositionGenerator.findRandomTargetBlockAwayFrom(
            this.theEntity,
            16,
            7,
            Vec3.createVectorHelper(
                this.closestLivingEntity.posX,
                this.closestLivingEntity.posY,
                this.closestLivingEntity.posZ));

        if (vec3 == null) {
            return false;
        } else if (this.closestLivingEntity.getDistanceSq(vec3.xCoord, vec3.yCoord, vec3.zCoord)
            < this.closestLivingEntity.getDistanceSqToEntity(this.theEntity)) {
                return false;
            } else {
                this.entityPathEntity = this.entityPathNavigate.getPathToXYZ(vec3.xCoord, vec3.yCoord, vec3.zCoord);
                return this.entityPathEntity != null && this.entityPathEntity.isDestinationSame(vec3);
            }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean continueExecuting() {
        if (!shouldExecute()) return false;

        if (this.theEntity.getDistanceToEntity(this.closestLivingEntity) > this.distanceFromEntity) return false;
        return !this.entityPathNavigate.noPath();

    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting() {
        this.entityPathNavigate.setPath(this.entityPathEntity, this.farSpeed);
    }

    /**
     * Resets the task
     */
    @Override
    public void resetTask() {
        this.closestLivingEntity = null;
    }

    /**
     * Updates the task
     */
    @Override
    public void updateTask() {
        if (this.theEntity.getDistanceSqToEntity(this.closestLivingEntity) < 49.0D) {
            this.theEntity.getNavigator()
                .setSpeed(this.nearSpeed);
        } else {
            this.theEntity.getNavigator()
                .setSpeed(this.farSpeed);
        }
    }

}
