package echotrace.core.targets;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerTarget implements Target {

    private final Player player;

    public PlayerTarget(Player player) {
        this.player = player;
    }

    @Override
    public TargetType getType() {
        return TargetType.PLAYER;
    }

    @Override
    public Location getLocation() {
        return player.getEyeLocation();
    }

}