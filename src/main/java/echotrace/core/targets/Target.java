package echotrace.core.targets;

import org.bukkit.Location;

public interface Target {

    TargetType getType();

    Location getLocation();

}
