package chalkinshmeal.lockout.artifacts.rewards;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class LockoutReward {
    protected JavaPlugin plugin;
    protected String description = "No description";

    public LockoutReward(JavaPlugin plugin) {
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
