package WolfShotz.Wyrmroost.content.entities.owdrake;

import WolfShotz.Wyrmroost.content.entities.AbstractDragonEntity;
import WolfShotz.Wyrmroost.content.entities.ai.goals.DragonBreedGoal;
import WolfShotz.Wyrmroost.content.entities.ai.goals.DragonGrazeGoal;
import WolfShotz.Wyrmroost.util.NetworkUtils;
import com.github.alexthe666.citadel.animation.Animation;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SaddleItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static net.minecraft.entity.SharedMonsterAttributes.*;

/**
 * Created by WolfShotz 7/10/19 - 22:18
 */
public class OWDrakeEntity extends AbstractDragonEntity
{
    private static final UUID SPRINTING_ID = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
    private static final AttributeModifier SPRINTING_SPEED_BOOST = (new AttributeModifier(SPRINTING_ID, "Sprinting speed boost", (double)0.8F, AttributeModifier.Operation.MULTIPLY_TOTAL)).setSaved(false);
    
    // Dragon Entity Animations
    public static Animation SIT_ANIMATION = Animation.create(15);
    public static Animation STAND_ANIMATION = Animation.create(15);
    public static Animation GRAZE_ANIMATION = Animation.create(35);
    public static Animation HORN_ATTACK_ANIMATION = Animation.create(22);

    // Dragon Entity Data
    private static final DataParameter<Boolean> VARIANT = EntityDataManager.createKey(OWDrakeEntity.class, DataSerializers.BOOLEAN);

    public OWDrakeEntity(EntityType<? extends OWDrakeEntity> drake, World world) {
        super(drake, world);

        moveController = new MovementController(this);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(4, new MeleeAttackGoal(this, 1d, true));
        goalSelector.addGoal(6, new DragonBreedGoal(this, 12000));
        goalSelector.addGoal(10, new DragonGrazeGoal(this, 2, GRAZE_ANIMATION));
        goalSelector.addGoal(11, new WaterAvoidingRandomWalkingGoal(this, 1d));
        goalSelector.addGoal(12, new LookAtGoal(this, LivingEntity.class, 10f));
        goalSelector.addGoal(13, new LookRandomlyGoal(this));

        targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        targetSelector.addGoal(3, new NonTamedTargetGoal(this, PlayerEntity.class, true, EntityPredicates.CAN_AI_TARGET));
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        getAttribute(MAX_HEALTH).setBaseValue(50.0d);
        getAttribute(MOVEMENT_SPEED).setBaseValue(0.20989d);
        getAttributes().registerAttribute(ATTACK_DAMAGE).setBaseValue(6.0d);
    }

    // ================================
    //           Entity NBT
    // ================================
    @Override
    protected void registerData() {
        super.registerData();
        dataManager.register(VARIANT, false);
    }

    /** Save Game */
    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putBoolean("variant", getVariant());
    }

    /** Load Game */
    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        setVariant(compound.getBoolean("variant"));
    }

    /**
     * The Variant of the drake.
     * false == Common, true == Savanna. Boolean since we only have 2 different variants
     */
    public boolean getVariant() { return dataManager.get(VARIANT); }
    public void setVariant(boolean variant) { dataManager.set(VARIANT, variant); }

    /**
     * Set sprinting switch for Entity.
     */
    public void setSprinting(boolean sprinting) {
        IAttributeInstance attribute = getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);

        super.setSprinting(sprinting);

        if (attribute.getModifier(SPRINTING_ID) != null) attribute.removeModifier(SPRINTING_SPEED_BOOST);
        if (sprinting) attribute.applyModifier(SPRINTING_SPEED_BOOST);
    }

    /** Set The chances this dragon can be an albino. Set it to 0 to have no chance */
    @Override
    public int getAlbinoChances() { return 50; }

    // ================================

    @Nullable
    @Override
    public ILivingEntityData onInitialSpawn(IWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
        Biome biome = worldIn.getBiome(new BlockPos(this));
        Set<Biome> biomes = BiomeDictionary.getBiomes(BiomeDictionary.Type.SAVANNA);

        if (biomes.contains(biome)) setVariant(true);

        return super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }

    @Override
    public void livingTick() {
        setSprinting(isAngry());
        if (!world.isRemote && getAttackTarget() == null && isAngry()) setAngry(false);
        
        if (getAnimation() == HORN_ATTACK_ANIMATION && getAnimationTick() == 8)
            attackInFront(-1, false);

        super.livingTick();
    }

    @Override
    public boolean processInteract(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (stack.getItem() == Items.STICK && !world.isRemote) NetworkUtils.sendAnimationPacket(this, GRAZE_ANIMATION);
        
        // If holding a saddle and this is not a child, Saddle up!
        if (stack.getItem() instanceof SaddleItem && !isSaddled() && !isChild()) { // instaceof: for custom saddles (if any)
            consumeItemFromStack(player, stack);
            setSaddled(true);
            playSound(SoundEvents.ENTITY_HORSE_SADDLE, 1f, 1f);

            return true;
        }
        
        // If Saddled and not sneaking, start riding
        if (isSaddled() && !isBreedingItem(stack) && !player.isSneaking() && !world.isRemote) {
            player.startRiding(this);
            sitGoal.setSitting(false);

            return true;
        }
    
        // If Sneaking, Sit
        if (isTamed() && !isBreedingItem(stack) && player.isSneaking() && isOwner(player)) {
            setSit(!isSitting());
            setAnimation(isSitting()? SIT_ANIMATION : STAND_ANIMATION);
        
            return true;
        }
        
        // If holding this dragons favorite food...
        if (isBreedingItem(stack)) {
            
            // If a child, tame it the old fashioned way. (otherwise RODEO!)
            if (isChild() && !isTamed()) {
                tame(getRNG().nextInt(10) == 0, player);
                consumeItemFromStack(player, stack);
                
                return true;
            }
            
            // If health is low, then heal up (Heal has priority over setting love mode!)
            if (isTamed() && getHealth() < getMaxHealth() && !player.isSneaking()) {
                consumeItemFromStack(player, stack);
                heal(stack.getItem() == Items.HAY_BLOCK? 6f : 2f);
        
                return true;
            }
        }

        return super.processInteract(player, hand);
    }
    
    /**
     * Called to handle the movement of the entity
     */
    @Override
    public void travel(Vec3d vec3d) {
        if (isBeingRidden() && canBeSteered() && isTamed() && !hasActiveAnimation()) {
            LivingEntity rider = (LivingEntity) getControllingPassenger();
            if (canPassengerSteer()) {
                float f = rider.moveForward, s = rider.moveStrafing;
                float speed = (float) getAttribute(MOVEMENT_SPEED).getValue() * (rider.isSprinting() ? 2 : 1);
                boolean moving = (f != 0 || s != 0);
                Vec3d target = new Vec3d(s, vec3d.y, f);

                setSprinting(rider.isSprinting());
                setAIMoveSpeed(speed);
                super.travel(target);
                if (moving) {
                    prevRotationYaw = rotationYaw = rider.rotationYaw;
                    rotationPitch = rider.rotationPitch * 0.5f;
                    setRotation(rotationYaw, rotationPitch);
                    renderYawOffset = rotationYaw;
                    rotationYawHead = renderYawOffset;
                }
//              setRotation(ModUtils.limitAngle(rotationYaw, ModUtils.calcAngle(target), 15), rotationPitch); TODO: Smooth Rotations

                return;
            }
        }

        super.travel(vec3d);
    }

    @Override
    public void updatePassenger(Entity passenger) {
        super.updatePassenger(passenger);

        if (!isTamed() && passenger instanceof LivingEntity && !world.isRemote) {
            int rand = new Random().nextInt(100);

            if (passenger instanceof PlayerEntity && rand == 0) {
                tame(true, (PlayerEntity) passenger);
            } else
            if (rand % 15 == 0) {
                setAttackTarget((LivingEntity) passenger);
                removePassengers();
                tame(false, null);
                passenger.addVelocity(0, 5, 0);
            }
        }
    }

    @Override
    public void eatGrassBonus() {
        if (isChild()) addGrowth(60);
        if (getHealth() < getMaxHealth()) heal(4f);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        playSound(SoundEvents.ENTITY_COW_STEP, 0.3f, 1);

        super.playStepSound(pos, blockIn);
    }
    
    @Override
    public void performGenericAttack() {
        setAnimation(HORN_ATTACK_ANIMATION);
    }
    
    @Override
    public EntitySize getSize(Pose poseIn) {
        System.out.println("test");
        return super.getSize(poseIn);
    }
    
    /**
     * Array Containing all of the dragons food items
     */
    @Override
    protected Item[] getFoodItems() { return new Item[] {Items.WHEAT, Items.HAY_BLOCK.asItem()}; }
    
    @Override
    public boolean canFly() { return false; }
    
    // == Entity Animation ==
    @Override
    public Animation[] getAnimations() { return new Animation[] {NO_ANIMATION, GRAZE_ANIMATION, HORN_ATTACK_ANIMATION, SIT_ANIMATION, STAND_ANIMATION}; }
    // ==

}
