package chalkinshmeal.lockin.artifacts.rewards;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class LockinReward {
    protected JavaPlugin plugin;
    protected String description = "No description";

    public LockinReward(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    //---------------------------------------------------------------------------------------------
    // Accessor/Mutator methods
    //---------------------------------------------------------------------------------------------
    public String getDescription() { return this.description; }

    //---------------------------------------------------------------------------------------------
    // Reward methods
    //---------------------------------------------------------------------------------------------
    public abstract void giveReward(Player player);
}
