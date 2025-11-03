package echotrace.core.targets;

import org.bukkit.Location;

public class PositionTarget implements Target {

    private final Location location;

    public PositionTarget(Location location) {
        this.location = location;
    }

    @Override
    public TargetType getType() {
        return TargetType.POSITION;
    }

    @Override
    public Location getLocation() {
        return location;
    }

}