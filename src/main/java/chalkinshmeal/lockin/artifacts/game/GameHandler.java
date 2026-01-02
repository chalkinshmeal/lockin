package chalkinshmeal.lockin.artifacts.game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import chalkinshmeal.lockin.artifacts.compass.LockinCompass;
import chalkinshmeal.lockin.artifacts.countdown.CountdownBossBar;
import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.artifacts.tasks.LockinTaskHandler;
import chalkinshmeal.mc_plugin_lib.config.ConfigHandler;
import chalkinshmeal.mc_plugin_lib.logging.LoggerUtils;
import chalkinshmeal.mc_plugin_lib.teams.Team;
import chalkinshmeal.mc_plugin_lib.teams.TeamHandler;
import chalkinshmeal.lockin.utils.EntityUtils;
import chalkinshmeal.lockin.utils.TaskUtils;
import chalkinshmeal.lockin.utils.Utils;
import chalkinshmeal.lockin.utils.WorldUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public class GameHandler {
    private final JavaPlugin plugin;
    private final ConfigHandler configHandler;
    private final LockinCompass lockinCompass;
    private final LockinTaskHandler lockinTaskHandler;
    private CountdownBossBar countdownBossBar;
    private final TeamHandler teamHandler;
    private final int queueTime;
    private final int singleTeamTimeLimit;
    private final int multipleCompleteTeamTimeLimit;
    private final int multipleTeamTimeLimit;
    private final int maxTier;
    private final boolean debug = false;

    // Temporary status
    public GameState state = GameState.INACTIVE;
    public GameType gameType = GameType.SINGLE_TEAM;
    public int currentTier = 0;
    public List<Integer> taskIDs = new ArrayList<>();
    public List<Integer> incrementTierTaskIDs = new ArrayList<>();
    public boolean multipleTeamOnePlayerHasCompleted = false;

    //---------------------------------------------------------------------------------------------
    // Constructor
    //---------------------------------------------------------------------------------------------
    public GameHandler(JavaPlugin plugin, ConfigHandler configHandler, LockinCompass lockinCompass, LockinTaskHandler lockinTaskHandler, TeamHandler teamHandler) {
        this.plugin = plugin;
        this.configHandler = configHandler;
        this.lockinCompass = lockinCompass;
        this.lockinTaskHandler = lockinTaskHandler;
        this.teamHandler = teamHandler;
        this.queueTime = this.configHandler.getInt("queueTime", 120);
        this.singleTeamTimeLimit = this.configHandler.getInt("singleTeamTimeLimit", 600);
        this.multipleCompleteTeamTimeLimit = this.configHandler.getInt("multipleCompletedTeamTimeLimit", 600);
        this.multipleTeamTimeLimit = this.configHandler.getInt("multipleTeamTimeLimit", 600);
        this.maxTier = 10;
        this.init();
    }

    //---------------------------------------------------------------------------------------------
    // Accessor/Mutator methods
    //---------------------------------------------------------------------------------------------
    public int getNumTeams() { return this.teamHandler.getNumTeams(); }
    public boolean isActive() { return this.state != GameState.INACTIVE; }

    //---------------------------------------------------------------------------------------------
    // Game methods
    //---------------------------------------------------------------------------------------------
    public void init() {
        int maxLives = this.configHandler.getInt("maxLives", 5);
        this.teamHandler.addTeam("Blue Team", Component.text("Blue Team"), Material.BLUE_WOOL, maxLives);
        this.teamHandler.addTeam("Green Team", Component.text("Green Team"), Material.GREEN_WOOL, maxLives);
        this.teamHandler.addTeam("Red Team", Component.text("Red Team"), Material.RED_WOOL, maxLives);
        this.teamHandler.addTeam("Magenta Team", Component.text("Magenta Team"), Material.MAGENTA_WOOL, maxLives);
        this.lockinCompass.addTeam(this.teamHandler.getTeam("Blue Team"));
        this.lockinCompass.addTeam(this.teamHandler.getTeam("Green Team"));
        this.lockinCompass.addTeam(this.teamHandler.getTeam("Red Team"));
        this.lockinCompass.addTeam(this.teamHandler.getTeam("Magenta Team"));
    }

    public void queue() {
        // Set world state
        this.resetWorldState();

        // Set game state
        this.state = GameState.QUEUE;
        this.currentTier = 0;
        this.teamHandler.removeEmptyTeams();
        this.teamHandler.setScoreboardVisible(true);
        this.teamHandler.showScoreboardToAllPlayers();
        this.lockinTaskHandler.setCurrentTier(this.currentTier);
        this.lockinCompass.SetIsActive(true);
        this.incrementTier();
        this.gameType = this.teamHandler.getNumTeams() <= 1 ? GameType.SINGLE_TEAM : GameType.MULTIPLE_TEAM;

        // Delayed start
        this.taskIDs.add(TaskUtils.runDelayedTask(this.plugin, this::start, this.queueTime*20));

        // Cosmetics
        for (Player player : this.teamHandler.getAllOnlinePlayers()) {
            this.displayCountdownTask(plugin, player, this.queueTime);
        }
    }

    public void start() {
        this.state = GameState.PLAY;

        // Set world state
        WorldUtils.destroySpawnCage();

        // Set game state
        if (this.gameType == GameType.SINGLE_TEAM) {
            this.countdownBossBar = new CountdownBossBar(this.plugin, this.configHandler, this.singleTeamTimeLimit);
        }
        else if (this.gameType == GameType.MULTIPLE_TEAM) {
            this.countdownBossBar = new CountdownBossBar(this.plugin, this.configHandler, this.multipleTeamTimeLimit);
        }
        this.countdownBossBar.start();
        this.resetIncrementTierTasks();
    }

    public void incrementTier() {
        this.currentTier += 1;
        this.lockinTaskHandler.setCurrentTier(this.currentTier);
        this.multipleTeamOnePlayerHasCompleted = false;

        if (this.currentTier != 1) {
            // Calculate tasks completed by the leading team
            int maxCompletedTasks = 0;
            for (Team team : this.teamHandler.getTeams()) {
                int completedTasks = 0;
                for (LockinTask task : this.lockinTaskHandler.getTasks()) {
                    if (task.hasCompleted(team.getKey())) completedTasks += 1;
                }
                if (completedTasks > maxCompletedTasks) maxCompletedTasks = completedTasks;
            }

            // Subtract score
            for (Team team : this.teamHandler.getTeams()) {
                if (this.teamHandler.getScore(team) <= 0) continue;

                int completedTasks = 0;
                for (LockinTask task : this.lockinTaskHandler.getTasks()) {
                    if (task.hasCompleted(team.getKey())) completedTasks += 1;
                }

                int lostScore = Math.max(0, Math.min(maxCompletedTasks, this.lockinTaskHandler.getTasksToCompletePerTier(this.currentTier - 1)) - completedTasks);
                if (this.state == GameState.SUDDEN_DEATH) lostScore = 1;

                this.teamHandler.subtractScore(team, lostScore);
            }

            // Check end game conditions
            if (this.gameType == GameType.MULTIPLE_TEAM && this.teamHandler.atMostOneTeamHasPositiveLives() && this.currentTier > 1 ||
                this.gameType == GameType.SINGLE_TEAM && this.currentTier > this.maxTier ||
                this.gameType == GameType.SINGLE_TEAM && !this.teamHandler.atLeastOneTeamHasPositiveLives() && this.currentTier > 1) {
                this.stop();
                return;
            }
        }

        // Set game state
        this.lockinTaskHandler.unRegisterListeners();
        this.lockinTaskHandler.updateTaskList(this.currentTier);
        this.lockinTaskHandler.registerListeners();
        this.lockinCompass.updateTasksInventory(this.lockinTaskHandler);

        // Reset increment tier checker
        if (this.currentTier != 1 && this.currentTier <= this.maxTier) {
            resetIncrementTierTasks();
        }

        // Cosmetics
        if (this.currentTier != 1) this.countdownBossBar.reset();

        for (Player player : this.teamHandler.getAllOnlinePlayers()) {
            player.sendMessage(Component.text()
                .append(Component.text("Advancing to ", NamedTextColor.GRAY))
                .append(Component.text("Tier " + this.currentTier + " ", NamedTextColor.BLUE))
                .append(Component.text("(Need to complete " + this.lockinTaskHandler.getTasksToCompletePerTier(this.currentTier) + " tasks)", NamedTextColor.GRAY))
            );
            Utils.playSound(player, Sound.BLOCK_NOTE_BLOCK_CHIME);
        }
    }

    @SuppressWarnings("deprecation")
    public void stop() {
        // Note, at this point, there should only be one winning team
        Team winningTeam = this.teamHandler.getWinningTeams().iterator().next();
        int maxLives = this.teamHandler.getMaxScore();

        // Per-player operations
        for (Player player : this.teamHandler.getAllOnlinePlayers()) {
            boolean inWinningTeam = (
                this.gameType == GameType.MULTIPLE_TEAM && winningTeam.hasPlayer(player) ||
                this.gameType == GameType.SINGLE_TEAM && maxLives <= 0);
            String winOrLose = (inWinningTeam) ? " won!" : " lost!";
            player.showTitle(Title.title(
                Component.text(winningTeam.getDisplayName() + winOrLose, NamedTextColor.GOLD),
                Component.empty(), // No subtitle
                Title.Times.of(java.time.Duration.ZERO, java.time.Duration.ofSeconds(5), java.time.Duration.ofSeconds(1))
            ));
            if (inWinningTeam) Utils.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_1);
            else Utils.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_6);
        }

        this.end();
    }

    public void end() {
        this.state = GameState.INACTIVE;

        // Set game state
        this.lockinTaskHandler.unRegisterListeners();
        this.countdownBossBar.stop();

        // Stop all tasks
        for (int taskID : this.taskIDs) TaskUtils.cancelTask(taskID);

        // Cosmetics
        for (Player player : this.teamHandler.getAllOnlinePlayers()) {
            player.sendMessage(Component.text("Lockin game ended.", NamedTextColor.GOLD));
        }
    }

    private void resetWorldState() {
        WorldUtils.resetWorldState();
        WorldUtils.setGameRule(GameRule.KEEP_INVENTORY, true);
        for (Player player : this.teamHandler.getAllOnlinePlayers()) {
            EntityUtils.resetPlayerState(player, true);
            this.lockinCompass.giveCompass(player);
        }
    }

    private void resetIncrementTierTasks() {
        if (debug) LoggerUtils.info("Resetting increment tier checker tasks");
        if (debug) {
            LoggerUtils.info("TaskIDs:");
            for (int taskID : this.taskIDs) {
                LoggerUtils.info("  " + taskID);
            }
            LoggerUtils.info("Increment Tier Checker TaskIDs:");
            for (int taskID : this.incrementTierTaskIDs) {
                LoggerUtils.info("  " + taskID);
            }
        }
        // Clear incrementTier tasks
        for (int taskID : this.incrementTierTaskIDs) {
            this.taskIDs.remove(Integer.valueOf(taskID));
            TaskUtils.cancelTask(taskID);
        }
        this.incrementTierTaskIDs.clear();

        // Make task to increment after time limit is reached
        float delayTime = 0;
        if (this.gameType == GameType.SINGLE_TEAM) {
            //delayTime = (this.currentTier == 1) ? this.singleTeamTimeLimit + this.queueTime : this.singleTeamTimeLimit;
            delayTime = this.singleTeamTimeLimit;
        }
        else if (this.gameType == GameType.MULTIPLE_TEAM) {
            //delayTime = (this.currentTier == 1) ? this.multipleTeamTimeLimit + this.queueTime : this.multipleTeamTimeLimit;
            delayTime = this.multipleTeamTimeLimit;
        }

        int taskID = TaskUtils.runDelayedTask(this.plugin, this::incrementTier, (long) delayTime*20);
        this.incrementTierTaskIDs.add(taskID);
        this.taskIDs.add(taskID);

        // Make task to increment if tasks are completed
        taskID = TaskUtils.runRepeatingTask(this.plugin, () -> {
            if (lockinTaskHandler.hasOneTeamCompletedTheTier() && this.gameType == GameType.SINGLE_TEAM) {
                incrementTier();
            }
            else if (lockinTaskHandler.haveAllTeamsCompletedTheTier() && this.gameType == GameType.MULTIPLE_TEAM) {
                incrementTier();
            }
            else if (lockinTaskHandler.hasOneTeamCompletedTheTier() && this.gameType == GameType.MULTIPLE_TEAM && !this.multipleTeamOnePlayerHasCompleted) {
                this.multipleTeamOnePlayerHasCompleted = true;
                int newTime = Math.min(this.countdownBossBar.getTime(), this.multipleCompleteTeamTimeLimit);
                boolean reduceTime = this.multipleCompleteTeamTimeLimit < this.countdownBossBar.getTime();
                this.countdownBossBar.setTime(newTime);
                int newTaskID = TaskUtils.runDelayedTask(this.plugin, this::incrementTier, (long) newTime*20);
                this.incrementTierTaskIDs.add(newTaskID);
                this.taskIDs.add(newTaskID);

                if (reduceTime) {
                    for (Player player : this.teamHandler.getAllOnlinePlayers()) {
                        player.sendMessage(Component.text()
                            .append(Component.text("Time limit reduced to ", NamedTextColor.GRAY))
                            .append(Component.text(newTime, NamedTextColor.BLUE))
                            .append(Component.text(" seconds", NamedTextColor.GRAY)));
                        Utils.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO);
                    }
                }
            }
        }, 0f, 1f*20);
        this.incrementTierTaskIDs.add(taskID);
        this.taskIDs.add(taskID);
    }

    //---------------------------------------------------------------------------------------------
    // Listener methods
    //---------------------------------------------------------------------------------------------
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (this.state == GameState.PLAY) return;
        event.setCancelled(true);
    }

    //---------------------------------------------------------------------------------------------
    // Task methods
    //---------------------------------------------------------------------------------------------
    private void displayCountdownTask(JavaPlugin plugin, Player player, int durationSeconds) {
        new BukkitRunnable() {
            private int remainingSeconds = durationSeconds;

            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                if (remainingSeconds <= 0) {
                    player.clearTitle();
                    this.cancel(); // Stop the task when the timer ends
                    return;
                }
                if (state == GameState.INACTIVE) {
                    this.cancel();
                    return;
                }

                // Cosmetics
                Sound sound = (remainingSeconds <= 5) ? Sound.BLOCK_NOTE_BLOCK_PLING : Sound.BLOCK_NOTE_BLOCK_HAT;
                Utils.playSound(player, sound);

                player.showTitle(Title.title(
                    Component.text(remainingSeconds, NamedTextColor.GOLD),
                    Component.empty(), // No subtitle
                    Title.Times.of(java.time.Duration.ZERO, java.time.Duration.ofSeconds(1), java.time.Duration.ofSeconds(5))
                ));
                remainingSeconds--;
            }
        }.runTaskTimer(plugin, 0, 20); // Run the task every 20 ticks (1 second)
    }
}