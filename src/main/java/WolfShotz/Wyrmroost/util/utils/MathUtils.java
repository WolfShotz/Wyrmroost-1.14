package WolfShotz.Wyrmroost.util.utils;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;

/**
 * Math utility class to make my life like way easier.
 *
 * Half of this shit is just me throwing numbers in and hoping it works,
 * seems to be going well so far!
 */
public class MathUtils
{
    public static final float PI = (float) Math.PI;
    
    /**
     * Attempt to rotate the first angle to become the second angle, but only allow overall direction change to at max be
     * third parameter
     */
    public static float limitAngle(float sourceAngle, float targetAngle, float maximumChange) {
        float f = MathHelper.wrapDegrees(targetAngle - sourceAngle);

        if (f > maximumChange) f = maximumChange;
        if (f < -maximumChange) f = -maximumChange;

        float f1 = sourceAngle + f;

        if (f1 < 0.0F) f1 += 360.0F;
        else if (f1 > 360.0F) f1 -= 360.0F;

        return f1;
    }
    
    public static Vec3d rotateYaw(float amount, double xOffset, double zOffset) {
        return new Vec3d(xOffset, 0, zOffset).rotateYaw(-amount * (PI / 180f));
    }
    
    /**
     * Get the Altitude of an entity from the world surface
     * Subtract 1 from plane pose's as a workaround for a vanilla bug using the wrong pos when plane pose's are negative...
     */
    public static double getAltitude(Entity entity) {
        return entity.posY - entity.world.getHeight(Heightmap.Type.MOTION_BLOCKING, (int) entity.posX - (entity.posX < 0? 1 : 0), (int) entity.posZ - (entity.posZ < 0? 1 : 0));
    }
    
    /**
     * Get the altitude of a world position from the world surface
     * Subtract 1 from plane pose's as a workaround for a vanilla bug using the wrong pos when plane pose's are negative...
     */
    public static double getAltitude(World world, int x, double y, int z) {
        return y - world.getHeight(Heightmap.Type.WORLD_SURFACE, x - (x < 0? 1 : 0), z - (z < 0? 1 : 0));
    }
    
    /**
     * Calculate the euclidean plane distance of two points.
     * Double
     */
    public static double getPlaneDistSq(double sourceX, double targetX, double sourceZ, double targetZ) {
        double x = targetX - sourceX;
        double z = targetZ - sourceZ;
        return x * x + z * z;
    }
    
    /**
     * Calculate the euclidean plane distance between two points.
     * Float
     */
    public static float getPlaneDistSq(float sourceX, float targetX, float sourceZ, float targetZ) {
        float x = targetX - sourceX;
        float z = targetZ - sourceZ;
        return x * x + z * z;
    }
    
    /**
     * Calculate euclidean space distance
     * Double
     */
    public static double getSpaceDistSq(double sourceX, double targetX, double sourceY, double targetY, double sourceZ, double targetZ) {
        double x = targetX - sourceX;
        double y = targetY - sourceY;
        double z = targetZ - sourceZ;
        return x * x + y * y + z * z;
    }
    
    /**
     * Calculate euclidean space distance
     * Float
     */
    public static float getSpaceDistSq(float sourceX, float targetX, float sourceY, float targetY, float sourceZ, float targetZ) {
        float x = targetX - sourceX;
        float y = targetY - sourceY;
        float z = targetZ - sourceZ;
        return x * x + y * y + z * z;
    }
    
    /**
     * Angle measurement converted to radians as a float value <P>
     * <code>angle / 180.0 * PI</code>
     */
    public static float toRadians(float angle) { return (float) Math.toRadians(angle); }
    
    /**
     * Angle measurement converted to degrees as a float value <P>
     * <code>angle * 180.0 / PI</code>
     */
    public static float toDegrees(float angle) { return (float) Math.toDegrees(angle); }
    
    /**
     * Get the angle between 2 sources
     */
    public static double getAngle(double x1, double x2, double z1, double z2) {
        return Math.atan2(z2 - z1, x2 - x1) * (180 / Math.PI) + 90;
    }
}