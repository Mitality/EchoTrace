package echotrace.core.targets;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class BlockTarget implements Target {

    private final Block block;

    public BlockTarget(Block block) {
        this.block = block;
    }

    @Override
    public TargetType getType() {
        return TargetType.BLOCK;
    }

    @Override
    public Location getLocation() {
        return block.getLocation()
                .add(0.5, 0.5, 0.5);
    }

}
