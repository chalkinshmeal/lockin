package chalkinshmeal.lockout.artifacts.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockout.artifacts.rewards.LockoutReward;
import chalkinshmeal.lockout.artifacts.rewards.LockoutRewardHandler;
import chalkinshmeal.lockout.data.ConfigHandler;
import chalkinshmeal.lockout.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public abstract class LockoutTask {
    public static String maxTaskCount = "maxTaskCount";
    protected final JavaPlugin plugin;
    protected final ConfigHandler configHandler;
    private final LockoutTaskHandler lockoutTaskHandler;
    private final LockoutRewardHandler lockoutRewardHandler;
    protected List<Listener> listeners;
    private boolean completed;
    public NamedTextColor nameColor;

    protected String name;
    protected ItemStack item;
    protected TextComponent itemDisplayName;
    protected int value;
    protected LockoutReward reward;
    protected boolean isPunishment;
    protected boolean isSuddenDeath;
    protected boolean applyAAnRules;

    //---------------------------------------------------------------------------------------------
    // Constructor
    //---------------------------------------------------------------------------------------------
    public LockoutTask(JavaPlugin plugin, ConfigHandler configHandler, LockoutTaskHandler lockoutTaskHandler, LockoutRewardHandler lockoutRewardHandler) {
        this.plugin = plugin;
        this.configHandler = configHandler;
        this.lockoutTaskHandler = lockoutTaskHandler;
        this.lockoutRewardHandler = lockoutRewardHandler;
        this.completed = false;
        this.listeners = new ArrayList<>();
        this.name = "NotImplemented";
        this.item = new ItemStack(Material.DIRT);
        this.value = 1;
        this.reward = null;
        this.isPunishment = false;
        this.isSuddenDeath = false;
        this.applyAAnRules = true;
        this.nameColor = NamedTextColor.BLUE;
        Utils.setDisplayName(item, this.itemDisplayName);

        this.validateConfig();
    }

    //---------------------------------------------------------------------------------------------
    // Accessor/Mutator methods
    //---------------------------------------------------------------------------------------------
    public void init() {
        // Set reward
        this.reward = (this.isPunishment) ? this.lockoutRewardHandler.getRandomPunishment() : this.lockoutRewardHandler.getRandomReward();

        // Change values based on if it's a punishment
        if (this.isPunishment) {
            this.value *= -1;
            this.name = "Don't " + this.name.toLowerCase();
            this.nameColor = NamedTextColor.RED;
        }

        // Chance name based on 1/a rules and a/an rules
        if (this.applyAAnRules) {
            if (this.name.contains(" 1 ")) {
                this.name = this.name.replace(" 1 ", " a ");
            }
            if (this.name.contains(" a ")) {
                String regexPattern = "^[aeiou].*"; 
                List<String> stringList = Arrays.asList(this.name.split(" "));
                for (int i = 0; i < stringList.size() - 1; i++) {
                    if (stringList.get(i).equals("a") && stringList.get(i + 1).matches(regexPattern)) {
                        stringList.set(i, "an");
                    }
                }
                this.name = String.join(" ", stringList);
            }
        }

        this.setItemDisplayName(Component.text(this.name, this.nameColor).decoration(TextDecoration.ITALIC, false));
        this.addLore();
        this.addListeners();
    }

    public ItemStack getItem() { return this.item; }
    public String getName() { return this.name; }
    public void setItemDisplayName(TextComponent displayName) { this.item = Utils.setDisplayName(this.item, displayName); }
    public void addLore() {
        Component valueLore = Component.text("Value: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                .append(Component.text(String.valueOf(this.value), NamedTextColor.GOLD));
        Component rewardLore = Component.text((this.isPunishment) ? "Punishment: " : "Reward: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                .append(Component.text((this.reward == null) ? "Nothing" : this.reward.getDescription(), NamedTextColor.LIGHT_PURPLE));

        this.item = Utils.addLore(this.item, valueLore);
        this.item = Utils.addLore(this.item, rewardLore);
    }
    public boolean isComplete() { return this.completed; }

    //---------------------------------------------------------------------------------------------
    // Task methods
    //---------------------------------------------------------------------------------------------
    public void complete(Player player) {
        this.completed = true;
        this.item = Utils.setMaterial(this.item, Material.GRAY_STAINED_GLASS_PANE);
        this.lockoutTaskHandler.complete(this, player);
        this.unRegisterListeners();
        if (this.reward != null) this.reward.giveReward(player);
    }

    //---------------------------------------------------------------------------------------------
    // Listener methods
    //---------------------------------------------------------------------------------------------
    public void registerListeners() {
		PluginManager manager = this.plugin.getServer().getPluginManager();
        for (Listener l : this.listeners) { manager.registerEvents(l, this.plugin); }
    }
    public void unRegisterListeners() {
        for (Listener l : this.listeners) { HandlerList.unregisterAll(l); }
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public abstract void addListeners();
    public abstract void validateConfig();
}
