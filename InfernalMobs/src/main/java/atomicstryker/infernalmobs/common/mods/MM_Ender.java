package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class MM_Ender extends MobModifier {

    private final static long coolDown = 15000L;
    private static String[] suffix = {"theEnderborn", "theTrickster"};
    private static String[] prefix = {"enderborn", "tricky"};
    private long nextAbilityUse = 0L;

    public MM_Ender() {
        super();
    }

    public MM_Ender(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "Ender";
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        long time = System.currentTimeMillis();
        if (time > nextAbilityUse && source.getTrueSource() != null && source.getTrueSource() != mob && !InfernalMobsCore.instance().isInfiniteLoop(mob, source.getTrueSource())
                && teleportToEntity(mob, source.getTrueSource())) {
            nextAbilityUse = time + coolDown;
            source.getTrueSource().attackEntityFrom(DamageSource.causeMobDamage(mob), InfernalMobsCore.instance().getLimitedDamage(damage));

            return super.onHurt(mob, source, 0);
        }

        return super.onHurt(mob, source, damage);
    }

    private boolean teleportToEntity(LivingEntity mob, Entity par1Entity) {
        Vector3d vector = new Vector3d(mob.getPosX() - par1Entity.getPosX(), mob.getBoundingBox().minY + (double) (mob.getHeight() / 2.0F) - par1Entity.getPosY() + (double) par1Entity.getEyeHeight(),
                mob.getPosZ() - par1Entity.getPosZ());
        vector = vector.normalize();
        double telDist = 16.0D;
        double destX = mob.getPosX() + (mob.world.rand.nextDouble() - 0.5D) * 8.0D - vector.x * telDist;
        double destY = mob.getPosY() + (double) (mob.world.rand.nextInt(16) - 8) - vector.y * telDist;
        double destZ = mob.getPosZ() + (mob.world.rand.nextDouble() - 0.5D) * 8.0D - vector.z * telDist;
        // forge event hook
        net.minecraftforge.event.entity.living.EnderTeleportEvent event = new net.minecraftforge.event.entity.living.EnderTeleportEvent(mob, destX, destY, destZ, 0);
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) return false;

        return teleportTo(mob, destX, destY, destZ);
    }

    private boolean teleportTo(LivingEntity mob, double destX, double destY, double destZ) {
        double oldX = mob.getPosX();
        double oldY = mob.getPosY();
        double oldZ = mob.getPosZ();
        boolean success = false;
        mob.setPosition(destX, destY, destZ);
        int x = MathHelper.floor(mob.getPosX());
        int y = MathHelper.floor(mob.getPosY());
        int z = MathHelper.floor(mob.getPosZ());

        boolean hitGround = false;
        while (!hitGround && y < 96 && y > 0) {
            BlockState bs = mob.world.getBlockState(new BlockPos(x, y - 1, z));
            if (bs.getMaterial().blocksMovement()) {
                hitGround = true;
            } else {
                mob.setPosition(destX, --destY, destZ);
                --y;
            }
        }

        if (hitGround) {
            mob.setPosition(mob.getPosX(), mob.getPosY(), mob.getPosZ());

            if (mob.attemptTeleport(destX, destY, destZ, true)) {
                success = true;
            }
        } else {
            return false;
        }

        if (!success) {
            mob.setPosition(oldX, oldY, oldZ);
            return false;
        } else {
            short range = 128;
            for (int i = 0; i < range; ++i) {
                double var19 = (double) i / ((double) range - 1.0D);
                float var21 = (mob.world.rand.nextFloat() - 0.5F) * 0.2F;
                float var22 = (mob.world.rand.nextFloat() - 0.5F) * 0.2F;
                float var23 = (mob.world.rand.nextFloat() - 0.5F) * 0.2F;
                double var24 = oldX + (mob.getPosX() - oldX) * var19 + (mob.world.rand.nextDouble() - 0.5D) * (double) mob.getWidth() * 2.0D;
                double var26 = oldY + (mob.getPosY() - oldY) * var19 + mob.world.rand.nextDouble() * (double) mob.getHeight();
                double var28 = oldZ + (mob.getPosZ() - oldZ) * var19 + (mob.world.rand.nextDouble() - 0.5D) * (double) mob.getWidth() * 2.0D;
                mob.world.addParticle(ParticleTypes.PORTAL, var24, var26, var28, (double) var21, (double) var22, (double) var23);
            }

            mob.world.playSound(null, new BlockPos(oldX, oldY, oldZ), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 1.0F + mob.getRNG().nextFloat(),
                    mob.getRNG().nextFloat() * 0.7F + 0.3F);
            mob.world.playSound(null, mob.getPosition(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 1.0F + mob.getRNG().nextFloat(), mob.getRNG().nextFloat() * 0.7F + 0.3F);
        }
        return true;
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

}
