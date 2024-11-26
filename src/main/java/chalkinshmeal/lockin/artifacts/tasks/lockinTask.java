package chalkinshmeal.lockin.artifacts.tasks;

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

import chalkinshmeal.lockin.artifacts.rewards.LockinReward;
import chalkinshmeal.lockin.artifacts.rewards.LockinRewardHandler;
import chalkinshmeal.lockin.artifacts.team.LockinTeamHandler;
import chalkinshmeal.lockin.data.ConfigHandler;
import chalkinshmeal.lockin.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public abstract class LockinTask {
    public static String maxTaskCount = "maxTaskCount";
    protected static JavaPlugin plugin;
    protected static ConfigHandler configHandler;
    private static LockinTaskHandler lockinTaskHandler;
    private static LockinRewardHandler lockinRewardHandler;
    protected static LockinTeamHandler lockinTeamHandler;
    protected List<Listener> listeners;
    private List<String> completed;
    public NamedTextColor nameColor;

    protected String name;
    protected ItemStack item;
    protected TextComponent itemDisplayName;
    protected int value;
    protected int tier;
    protected LockinReward reward;
    protected boolean isPunishment;
    protected boolean isSuddenDeath;
    protected boolean applyAAnRules;
    protected LockinTaskState state;
    protected boolean isCatchUpTask;

    //---------------------------------------------------------------------------------------------
    // Constructor
    //---------------------------------------------------------------------------------------------
    public LockinTask() {
        this.completed = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.name = "NotImplemented";
        this.item = new ItemStack(Material.DIRT);
        this.value = 1;
        this.tier = -1;
        this.reward = null;
        this.isPunishment = false;
        this.isSuddenDeath = false;
        this.applyAAnRules = true;
        this.nameColor = NamedTextColor.BLUE;
        this.state = LockinTaskState.RUN;
        this.isCatchUpTask = false;
        Utils.setDisplayName(item, this.itemDisplayName);

        this.validateConfig();
    }

    public static void initStaticVariables(JavaPlugin plugin, ConfigHandler configHandler, LockinTaskHandler lockinTaskHandler,
                                            LockinRewardHandler lockinRewardHandler, LockinTeamHandler lockinTeamHandler) {
        LockinTask.plugin = plugin;
        LockinTask.configHandler = configHandler;
        LockinTask.lockinTaskHandler = lockinTaskHandler;
        LockinTask.lockinRewardHandler = lockinRewardHandler;
        LockinTask.lockinTeamHandler = lockinTeamHandler;
    }

    //---------------------------------------------------------------------------------------------
    // Accessor/Mutator methods
    //---------------------------------------------------------------------------------------------
    public void init() {
        // Set reward
        this.reward = (this.isPunishment) ? lockinRewardHandler.getRandomPunishment() : lockinRewardHandler.getRandomReward();

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
        this.setLore();
        this.addListeners();
    }

    public ItemStack getItem() { return this.item; }
    public String getName() { return this.name; }
    public void setItemDisplayName(TextComponent displayName) { this.item = Utils.setDisplayName(this.item, displayName); }
    public void setLore() {
        this.item = Utils.resetLore(this.item);

        Component teamLore = Component.text("Status: ", NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false);
        this.item = Utils.addLore(this.item, teamLore);

        for (int i = 0; i < LockinTask.lockinTeamHandler.getNumTeams(); i++) {
            String teamName = LockinTask.lockinTeamHandler.getTeamName(i);
            String displayTeamName = LockinTask.lockinTeamHandler.getDisplayTeamName(i);
            NamedTextColor teamColor = this.completed.contains(teamName) ? NamedTextColor.GREEN : NamedTextColor.RED;
            String completeText = this.completed.contains(teamName) ? ":)" : ":(";
            Component individualTeamLore = Component.text(" " + displayTeamName, NamedTextColor.DARK_AQUA)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(" " + completeText, teamColor));
            this.item = Utils.addLore(this.item, individualTeamLore);
        }

        //Component valueLore = Component.text("Value: ", NamedTextColor.GRAY)
        //    .decoration(TextDecoration.ITALIC, false)
        //    .append(Component.text(String.valueOf(this.value), NamedTextColor.GOLD));
        //this.item = Utils.addLore(this.item, valueLore);

        if (this.reward != null) {
            Component rewardLore = Component.text((this.isPunishment) ? "Punishment: " : "Reward: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(this.reward.getDescription(), NamedTextColor.LIGHT_PURPLE));
            this.item = Utils.addLore(this.item, rewardLore);
        }
    }
    public boolean haveAllTeamsCompleted() {
        List<String> teamNames = (this.isCatchUpTask) ? lockinTeamHandler.getCatchUpTeamNames() : lockinTeamHandler.getNonCatchUpTeamNames();
        for (String teamName : teamNames) {
            if (!this.completed.contains(teamName)) return false;
        }
        return true;
    }
    public boolean hasCompleted(String teamName) { return this.completed.contains(teamName); }

    //---------------------------------------------------------------------------------------------
    // Task methods
    //---------------------------------------------------------------------------------------------
    public void complete(Player player) {
        String teamName = lockinTeamHandler.getTeamName(player);
        if (this.hasCompleted(teamName)) return;
        if (this.isCatchUpTask != lockinTeamHandler.isCatchUpTeam(teamName)) return;

        this.completed.add(teamName);
        lockinTaskHandler.complete(this, player);
        if (this.reward != null) this.reward.giveReward(player);
        this.setLore();

        if (this.haveAllTeamsCompleted()) {
            this.item = Utils.setMaterial(this.item, Material.GRAY_STAINED_GLASS_PANE);
            this.stop();
        }
    }
    
    public void stop() {
        this.state = LockinTaskState.DONE;
        this.unRegisterListeners();
    }

    //---------------------------------------------------------------------------------------------
    // Listener methods
    //---------------------------------------------------------------------------------------------
    public void registerListeners() {
		PluginManager manager = plugin.getServer().getPluginManager();
        for (Listener l : this.listeners) { manager.registerEvents(l, plugin); }
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
