package chalkinshmeal.lockin.artifacts.rewards.types;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.rewards.LockinReward;
import chalkinshmeal.lockin.utils.Utils;

public class ItemReward extends LockinReward {
    private final Material material;
    private final int amount;

    //---------------------------------------------------------------------------------------------
    // Constructor
    //---------------------------------------------------------------------------------------------
    public ItemReward(JavaPlugin plugin, Material material, int amount) {
        super(plugin);
        this.description = "Receive " + amount + " " + Utils.getReadableMaterialName(material);
        this.material = material;
        this.amount = amount;
    }

    //---------------------------------------------------------------------------------------------
    // Reward methods
    //---------------------------------------------------------------------------------------------
    public void giveReward(Player player) {
        Utils.giveItem(player, new ItemStack(this.material, this.amount));
    }
}
