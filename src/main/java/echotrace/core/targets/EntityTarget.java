package echotrace.core.targets;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class EntityTarget implements Target {

    private final Entity entity;

    public EntityTarget(Entity entity) {
        this.entity = entity;
    }

    @Override
    public TargetType getType() {
        return TargetType.ENTITY;
    }

    @Override
    public Location getLocation() {
        return (entity instanceof LivingEntity le) ?
                le.getEyeLocation() : entity.getLocation();
    }

}
