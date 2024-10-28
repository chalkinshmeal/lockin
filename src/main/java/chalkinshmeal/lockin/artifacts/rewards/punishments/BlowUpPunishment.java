package chalkinshmeal.lockin.artifacts.rewards.punishments;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.rewards.lockinReward;

public class BlowUpPunishment extends lockinReward {
    //---------------------------------------------------------------------------------------------
    // Constructor
    //---------------------------------------------------------------------------------------------
    public BlowUpPunishment(JavaPlugin plugin) {
        super(plugin);
        this.description = "Blow up";
    }

    //---------------------------------------------------------------------------------------------
    // Reward methods
    //---------------------------------------------------------------------------------------------
    public void giveReward(Player player) {
        player.getLocation().getWorld().createExplosion(player.getLocation(), 10.0F, true, true);
        player.damage(100);
    }
}
