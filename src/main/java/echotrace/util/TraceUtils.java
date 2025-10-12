package echotrace.util;

import echotrace.config.Config;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class TraceUtils {

    public static Vector turnTowards(Vector from, Vector to) {
        Vector a = from.clone().normalize();
        Vector b = to.clone().normalize();

        double dot = Math.max(-1.0, Math.min(1.0, a.dot(b)));
        double angle = Math.toDegrees(Math.acos(dot));
        if (angle <= Config.turn_rate || angle == 0.0) return b;

        double t = Config.turn_rate / angle; // 0..1
        Vector blended = a.multiply(1.0 - t).add(b.multiply(t));
        if (blended.lengthSquared() == 0.0) {
            // 180° edge case (pick any perpendicular)
            Vector perp = new Vector(-a.getZ(), 0, a.getX());
            if (perp.lengthSquared() == 0.0) perp = new Vector(0, 1, 0);
            return perp.normalize();
        }
        return blended.normalize();
    }

    public static boolean isChunkLoaded(World world, Location location) {
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        return world.isChunkLoaded(chunkX, chunkZ);
    }

}