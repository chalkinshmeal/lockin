package chalkinshmeal.lockin.artifacts.rewards.types;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import chalkinshmeal.lockin.artifacts.rewards.LockinReward;
import chalkinshmeal.lockin.utils.Utils;

public class PotionEffectReward extends LockinReward {
    private final PotionEffectType type;
    private final int duration;
    private final int level;

    //---------------------------------------------------------------------------------------------
    // Constructor
    //---------------------------------------------------------------------------------------------
    public PotionEffectReward(JavaPlugin plugin, PotionEffectType type, int duration, int level) {
        super(plugin);
        this.type = type;
        this.duration = duration;
        this.level = level;
        this.description = "Receive " + Utils.toTitleCase(type.getKey().asMinimalString().replace('_', ' ')) + " " + Utils.intToRomanNumerals(level);
    }

    //---------------------------------------------------------------------------------------------
    // Reward methods
    //---------------------------------------------------------------------------------------------
    public void giveReward(Player player) {
        Utils.giveEffect(player, this.type, this.duration, this.level);
    }
}
