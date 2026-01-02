package chalkinshmeal.lockin.artifacts.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.rewards.LockinReward;
import chalkinshmeal.lockin.artifacts.rewards.LockinRewardHandler;
import chalkinshmeal.mc_plugin_lib.config.ConfigHandler;
import chalkinshmeal.mc_plugin_lib.teams.Team;
import chalkinshmeal.mc_plugin_lib.teams.TeamHandler;
import chalkinshmeal.lockin.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public abstract class LockinTask {
    protected static String maxTaskCount = "maxTaskCount";
    protected static JavaPlugin plugin;
    protected static ConfigHandler configHandler;
    protected static LockinTaskHandler lockinTaskHandler;
    protected static LockinRewardHandler lockinRewardHandler;
    protected static TeamHandler teamHandler;
    protected List<Listener> listeners;
    protected List<String> completed;
    protected NamedTextColor nameColor;

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
                                            LockinRewardHandler lockinRewardHandler, TeamHandler teamHandler) {
        LockinTask.plugin = plugin;
        LockinTask.configHandler = configHandler;
        LockinTask.lockinTaskHandler = lockinTaskHandler;
        LockinTask.lockinRewardHandler = lockinRewardHandler;
        LockinTask.teamHandler = teamHandler;
    }

    //---------------------------------------------------------------------------------------------
    // Accessor/Mutator methods
    //---------------------------------------------------------------------------------------------
    public void init() {
        // Set reward
        if (this.reward == null) 
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

        for (Team team : LockinTask.teamHandler.getTeams()) {
            NamedTextColor teamColor = this.completed.contains(team.getKey()) ? NamedTextColor.GREEN : NamedTextColor.RED;
            String completeText = this.completed.contains(team.getKey()) ? ":)" : ":(";
            Component individualTeamLore = Component.text(" " + team.getKey(), NamedTextColor.DARK_AQUA)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(" " + completeText, teamColor));
            this.item = Utils.addLore(this.item, individualTeamLore);
        }
    }

    public boolean haveAllTeamsCompleted() {
        for (Team team : LockinTask.teamHandler.getTeamsWithPositiveLives()) {
            if (!this.completed.contains(team.getKey())) return false;
        }
        return true;
    }
    public boolean hasCompleted(String teamName) { return this.completed.contains(teamName); }

    //---------------------------------------------------------------------------------------------
    // Task methods
    //---------------------------------------------------------------------------------------------
    public void complete(Player player) {
        Team team = LockinTask.teamHandler.getTeam(player);
        if (this.hasCompleted(team.getKey())) return;
        this.completed.add(team.getKey());
        lockinTaskHandler.complete(this, player);
        this.setLore();

        // Check if team has completed all tasks
        if (lockinTaskHandler.hasTeamCompletedAllTasks(team)) {
            for (Player _player : teamHandler.getAllOnlinePlayers()) {
                player.sendMessage(Component.text()
                    .append(Component.text(team.getKey(), NamedTextColor.GOLD))
                    .append(Component.text(" has completed the tier", NamedTextColor.GRAY)));
                Utils.playSound(_player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO);
            }
        }

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
