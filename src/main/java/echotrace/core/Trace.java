package echotrace.core;

import echotrace.config.Config;
import echotrace.core.targets.Target;
import echotrace.util.TraceUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Trace {

    private Player player;
    private Target target;
    private Location currentLocation;
    private Vector currentDirection;
    private long maxPoints;
    private long rendered;
    private boolean hit;

    private Trace() {};
    public Trace(Player player, Target target, long points) {
        this.player = player;
        this.target = target;
        this.currentLocation = player.getEyeLocation();
        this.currentDirection = player.getEyeLocation().getDirection().normalize();
        this.maxPoints = points;
        this.rendered = 0;
        this.hit = false;
    }

    public Player getPlayer() {
        return player;
    }
    public Location getCurrentLocation() {
        return currentLocation;
    }
    public Target getTarget() {
        return target;
    }
    public Vector getCurrentDirection() {
        return currentDirection;
    }

    public void advance(long times) {

        for (long i = 0; i < times; i++) {

            if (maxPoints > 0 && rendered >= maxPoints) break;

            if (arrived()) {
                TraceRenderer.queueRender(player, currentLocation, true);
                rendered++; // irrelevant but whatever
                hit = true;
                break;
            }

            Vector toTarget = target.getLocation().toVector()
                    .subtract(currentLocation.toVector());
            Vector targetDirection = toTarget.clone().normalize();

            Vector newDirection = Config.turn_rate <= 0 ? targetDirection :
                    TraceUtils.turnTowards(currentDirection, targetDirection);
            Location newLocation = currentLocation.clone().add(newDirection.clone()
                    .multiply(Config.tracing_step_size));

            TraceRenderer.queueRender(player, newLocation, false);
            rendered++;

            currentDirection = newDirection;
            currentLocation = newLocation;
        }
    }

    public boolean arrived() {
        Vector toTarget = target.getLocation().toVector()
                .subtract(currentLocation.toVector());
        return toTarget.lengthSquared() < 0.25;
    }

    public boolean active() {
        return currentLocation.getWorld() == target.getLocation().getWorld()
                && !hit && (maxPoints <= 0 || rendered < maxPoints);
    }

}
