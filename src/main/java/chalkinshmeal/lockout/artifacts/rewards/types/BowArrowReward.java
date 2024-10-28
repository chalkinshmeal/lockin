package chalkinshmeal.lockout.artifacts.rewards.types;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockout.artifacts.rewards.LockoutReward;
import chalkinshmeal.lockout.utils.Utils;

public class BowArrowReward extends LockoutReward {
    //---------------------------------------------------------------------------------------------
    // Constructor
    //---------------------------------------------------------------------------------------------
    public BowArrowReward(JavaPlugin plugin) {
        super(plugin);
        this.description = "Receive a bow and arrows";
    }

    //---------------------------------------------------------------------------------------------
    // Reward methods
    //---------------------------------------------------------------------------------------------
    public void giveReward(Player player) {
        Utils.giveItem(player, new ItemStack(Material.BOW, 1));
        Utils.giveItem(player, new ItemStack(Material.ARROW, 64));
    }
}
