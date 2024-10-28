package chalkinshmeal.lockout.artifacts.rewards;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockout.artifacts.rewards.punishments.BlowUpPunishment;
import chalkinshmeal.lockout.utils.Utils;

public class LockoutRewardHandler {
    public final JavaPlugin plugin;
    public List<LockoutReward> rewards = new ArrayList<>();
    public List<LockoutReward> punishments = new ArrayList<>();

    public LockoutRewardHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.createRewardsList();
        this.createPunishmentList();
    }

    public void createRewardsList() {
        //this.rewards.add(new BowArrowReward());
        //this.rewards.add(new ItemReward(Material.GOLDEN_APPLE, 1));
        //this.rewards.add(new ItemReward(Material.IRON_INGOT, 32));
        //this.rewards.add(new ItemReward(Material.DIAMOND_AXE, 1));
        //this.rewards.add(new ItemReward(Material.DIAMOND_SWORD, 1));
        //this.rewards.add(new ItemReward(Material.DIAMOND_SHOVEL, 1));
        //this.rewards.add(new ItemReward(Material.SHIELD, 1));
        //this.rewards.add(new ItemReward(Material.COOKED_BEEF, 16));
        //this.rewards.add(new ItemReward(Material.DIRT, 128));
        //this.rewards.add(new PotionEffectReward(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
        //this.rewards.add(new PotionEffectReward(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1));
        //this.rewards.add(new PotionEffectReward(PotionEffectType.HASTE, Integer.MAX_VALUE, 1));
        //this.rewards.add(new PotionEffectReward(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 1));
        //this.rewards.add(new PotionEffectReward(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1));
        //this.rewards.add(new PotionEffectReward(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1));
        //this.rewards.add(new PotionEffectReward(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 2));
        //this.rewards.add(new PotionEffectReward(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 2));
    }

    public void createPunishmentList() {
        this.punishments.add(new BlowUpPunishment(this.plugin));
        this.punishments.add(new BlowUpPunishment(this.plugin));
        this.punishments.add(new BlowUpPunishment(this.plugin));
        this.punishments.add(new BlowUpPunishment(this.plugin));
        this.punishments.add(new BlowUpPunishment(this.plugin));
        this.punishments.add(new BlowUpPunishment(this.plugin));
        this.punishments.add(new BlowUpPunishment(this.plugin));
        this.punishments.add(new BlowUpPunishment(this.plugin));
        this.punishments.add(new BlowUpPunishment(this.plugin));
        //this.punishments.add(new BlowUpPunishment());
        //this.punishments.add(new PotionEffectReward(PotionEffectType.SLOWNESS, 120, 2));
        //this.punishments.add(new PotionEffectReward(PotionEffectType.BLINDNESS, 120, 1));
        //this.punishments.add(new PotionEffectReward(PotionEffectType.LEVITATION, 60, 1));
        //this.punishments.add(new PotionEffectReward(PotionEffectType.WEAKNESS, 120, 1));
        //this.punishments.add(new PotionEffectReward(PotionEffectType.POISON, 60, 1));
    }

    //---------------------------------------------------------------------------------------------
    // Reward methods
    //---------------------------------------------------------------------------------------------
    public LockoutReward getRandomReward() {
        if (this.rewards.size() == 0) return null;
        return this.rewards.remove(Utils.getRandNum(0, this.rewards.size() - 1));
    }

    public LockoutReward getRandomPunishment() {
        if (this.punishments.size() == 0) return null;
        return this.punishments.remove(Utils.getRandNum(0, this.punishments.size() - 1));
    }
}
