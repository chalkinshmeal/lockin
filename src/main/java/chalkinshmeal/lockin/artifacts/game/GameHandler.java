package chalkinshmeal.lockin.artifacts.game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import chalkinshmeal.lockin.artifacts.compass.LockinCompass;
import chalkinshmeal.lockin.artifacts.countdown.CountdownBossBar;
import chalkinshmeal.lockin.artifacts.scoreboard.LockinScoreboard;
import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.artifacts.tasks.LockinTaskHandler;
import chalkinshmeal.lockin.artifacts.team.LockinTeamHandler;
import chalkinshmeal.lockin.data.ConfigHandler;
import chalkinshmeal.lockin.utils.EntityUtils;
import chalkinshmeal.lockin.utils.LoggerUtils;
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
    private final LockinScoreboard lockinScoreboard;
    public final LockinTeamHandler lockinTeamHandler;
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
    public GameHandler(JavaPlugin plugin, ConfigHandler configHandler, LockinCompass lockinCompass, LockinTaskHandler lockinTaskHandler, LockinScoreboard lockinScoreboard, LockinTeamHandler lockinTeamHandler) {
        this.plugin = plugin;
        this.configHandler = configHandler;
        this.lockinCompass = lockinCompass;
        this.lockinTaskHandler = lockinTaskHandler;
        this.lockinScoreboard = lockinScoreboard;
        this.lockinTeamHandler = lockinTeamHandler;
        this.queueTime = this.configHandler.getInt("queueTime", 120);
        this.singleTeamTimeLimit = this.configHandler.getInt("singleTeamTimeLimit", 600);
        this.multipleCompleteTeamTimeLimit = this.configHandler.getInt("multipleCompletedTeamTimeLimit", 600);
        this.multipleTeamTimeLimit = this.configHandler.getInt("multipleTeamTimeLimit", 600);
        this.maxTier = 10;
    }

    //---------------------------------------------------------------------------------------------
    // Accessor/Mutator methods
    //---------------------------------------------------------------------------------------------
    public int getNumTeams() { return this.lockinTeamHandler.getNumTeams(); }
    public boolean isActive() { return this.state != GameState.INACTIVE; }

    //---------------------------------------------------------------------------------------------
    // Game methods
    //---------------------------------------------------------------------------------------------
    public void queue() {
        this.state = GameState.QUEUE;
        this.currentTier = 0;
        this.lockinTaskHandler.setCurrentTier(this.currentTier);

        // Set world state
        this.resetWorldState();

        // Set game state
        this.lockinTeamHandler.init();
        this.lockinScoreboard.init(this.lockinTeamHandler);
        this.lockinCompass.SetIsActive(true);
        this.incrementTier();
        this.gameType = this.lockinTeamHandler.getNumTeams() <= 1 ? GameType.SINGLE_TEAM : GameType.MULTIPLE_TEAM;

        // Delayed start
        this.taskIDs.add(TaskUtils.runDelayedTask(this.plugin, this::start, this.queueTime*20));

        // Cosmetics
        for (Player player : this.lockinTeamHandler.getAllOnlinePlayers()) {
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

        // Cosmetics
        for (Player player : this.lockinTeamHandler.getAllOnlinePlayers()) {
            player.sendMessage(Component.text("Lockin game starting. Complete " + this.lockinTaskHandler.getTasksToCompletePerTier(this.currentTier) + "/" + this.lockinTaskHandler.tasksPerTier + " tasks to complete a tier", NamedTextColor.GOLD));
        }
    }

    public void incrementTier() {
        this.currentTier += 1;
        this.lockinTaskHandler.setCurrentTier(this.currentTier);
        this.multipleTeamOnePlayerHasCompleted = false;

        if (this.currentTier != 1) {
            // Calculate tasks completed by the leading team
            int maxCompletedTasks = 0;
            for (String teamName : this.lockinTeamHandler.getTeamNames()) {
                int completedTasks = 0;
                for (LockinTask task : this.lockinTaskHandler.getTasks()) {
                    if (task.hasCompleted(teamName)) completedTasks += 1;
                }
                if (completedTasks > maxCompletedTasks) maxCompletedTasks = completedTasks;
            }

            // Subtract score
            for (String teamName : this.lockinTeamHandler.getTeamNames()) {
                if (this.lockinScoreboard.getScore(teamName) <= 0) continue;
                if (this.lockinTeamHandler.isCatchUpTeam(teamName)) {
                    this.lockinTeamHandler.removeCatchUpTeam(teamName);
                    continue;
                }

                int completedTasks = 0;
                for (LockinTask task : this.lockinTaskHandler.getTasks()) {
                    if (task.hasCompleted(teamName)) completedTasks += 1;
                }

                int lostScore = Math.max(0, Math.min(maxCompletedTasks, this.lockinTaskHandler.getTasksToCompletePerTier(this.currentTier - 1)) - completedTasks);
                if (this.state == GameState.SUDDEN_DEATH) lostScore = 1;

                this.lockinScoreboard.subtractScore(teamName, lostScore);

                if (this.lockinScoreboard.getScore(teamName) <= 0) {
                    this.lockinTeamHandler.addCatchUpTeam(teamName);
                }
            }

            // Check sudden death conditions
            if (this.gameType == GameType.MULTIPLE_TEAM && this.currentTier > this.maxTier ||
                this.lockinScoreboard.noTeamHasPositiveLives() && this.lockinScoreboard.getWinningTeams().size() > 1) {
                this.suddenDeath();
                return;
            }

            // Check end game conditions
            if (this.gameType == GameType.MULTIPLE_TEAM && this.lockinScoreboard.atMostOneTeamHasPositiveLives() && this.currentTier > 1 ||
                this.gameType == GameType.SINGLE_TEAM && this.currentTier > this.maxTier ||
                this.gameType == GameType.SINGLE_TEAM && !this.lockinScoreboard.atLeastOneTeamHasPositiveLives() && this.currentTier > 1) {
                if (debug) LoggerUtils.info("End conditions met.");
                if (debug) LoggerUtils.info("  Game Type: " + this.gameType);
                if (debug) LoggerUtils.info("  Does at least one team have positive lives?: " + this.lockinScoreboard.atLeastOneTeamHasPositiveLives());
                if (debug) LoggerUtils.info("  Current Tier: " + this.currentTier);
                this.stop();
                return;
            }
        }

        // Set game state
        this.lockinTaskHandler.unRegisterListeners();
        this.lockinTaskHandler.updateTaskList(this.currentTier);
        this.lockinTaskHandler.registerListeners();
        if (this.gameType == GameType.MULTIPLE_TEAM) {
            this.lockinTaskHandler.unRegisterCatchUpListeners();
            this.lockinTaskHandler.updateCatchupTaskList();
            this.lockinTaskHandler.registerCatchUpListeners();
        }
        this.lockinCompass.updateTasksInventory(this.lockinTaskHandler);

        // Reset increment tier checker
        if (this.currentTier != 1 && this.currentTier <= this.maxTier) {
            resetIncrementTierTasks();
        }

        // Cosmetics
        if (this.currentTier != 1) {
            this.countdownBossBar.reset();

            for (Player player : this.lockinTeamHandler.getAllOnlinePlayers()) {
                player.sendMessage(Component.text()
                    .append(Component.text("Advancing to ", NamedTextColor.GRAY))
                    .append(Component.text("Tier " + this.currentTier, NamedTextColor.BLUE)));
                Utils.playSound(player, Sound.BLOCK_NOTE_BLOCK_CHIME);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void stop() {
        if (debug) LoggerUtils.info("Stopping game");
        // Get winner
        List<String> winningTeams = this.lockinScoreboard.getWinningTeams();
        if (debug) LoggerUtils.info("Winning teams (Size: " + winningTeams.size() + ")");
        for (String _winningTeam : winningTeams) if (debug) LoggerUtils.info("  " + _winningTeam);

        // Per-player operations
        for (Player player : this.lockinTeamHandler.getAllOnlinePlayers()) {
            String winOrLose = " won!";
            if (this.gameType == GameType.SINGLE_TEAM && this.lockinScoreboard.getScore(winningTeams.get(0)) <= 0) winOrLose = " lost!";
            player.showTitle(Title.title(
                Component.text(winningTeams.get(0) + winOrLose, NamedTextColor.GOLD),
                Component.empty(), // No subtitle
                Title.Times.of(java.time.Duration.ZERO, java.time.Duration.ofSeconds(5), java.time.Duration.ofSeconds(1))
            ));
            if (this.lockinTeamHandler.getTeamPlayers(winningTeams.get(0)).contains(player.getUniqueId()) &&
                !(this.gameType == GameType.SINGLE_TEAM && winOrLose.equals(" lost!"))) {
                Utils.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_1);
            }
            else {
                Utils.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_6);
            }
        }

        this.end();
    }

    @SuppressWarnings("deprecation")
    public void suddenDeath() {
        this.state = GameState.SUDDEN_DEATH;
        // Set non-winning-teams to spectator mode
        for (Player player : this.lockinTeamHandler.getNonLeadingTeamPlayers()) {
            player.setGameMode(GameMode.SPECTATOR);
        }

        // Clear incrementTier tasks
        for (int taskID : this.incrementTierTaskIDs) {
            this.taskIDs.remove(Integer.valueOf(taskID));
            TaskUtils.cancelTask(taskID);
        }

        // Set game state
        this.incrementTierTaskIDs.clear();
        this.countdownBossBar.stop();
        this.lockinTaskHandler.unRegisterListeners();
        this.lockinTaskHandler.updateSuddenDeathTaskList();
        this.lockinTaskHandler.registerListeners();
        this.lockinCompass.updateTasksInventory(this.lockinTaskHandler);
        for (String teamName : this.lockinTeamHandler.getTeamNames()) {
            if (this.lockinScoreboard.getScore(teamName) > 0) {
                this.lockinScoreboard.setScore(teamName, 1);
            }
        }

        // Run repeating task to check if any one team has completed all tasks (kill opposing player)
        int taskID = TaskUtils.runRepeatingTask(this.plugin, () -> {
            if (lockinTaskHandler.hasOneTeamCompletedAllTasks()) this.stop();
        }, 0f, 0.5f*20);
        this.taskIDs.add(taskID);

        // Cosmetics
        for (Player player : this.lockinTeamHandler.getAllOnlinePlayers()) {
            player.showTitle(Title.title(
                Component.text("Sudden Death", NamedTextColor.GOLD),
                Component.text("Kill all opposing players", NamedTextColor.GOLD),
                Title.Times.of(java.time.Duration.ZERO, java.time.Duration.ofSeconds(5), java.time.Duration.ofSeconds(1))
            ));
        }
    }

    public void end() {
        this.state = GameState.INACTIVE;

        // Set game state
        this.lockinTaskHandler.unRegisterListeners();
        this.countdownBossBar.stop();

        // Stop all tasks
        for (int taskID : this.taskIDs) TaskUtils.cancelTask(taskID);

        // Cosmetics
        for (Player player : this.lockinTeamHandler.getAllOnlinePlayers()) {
            player.sendMessage(Component.text("Lockin game ended.", NamedTextColor.GOLD));
        }
    }

    private void resetWorldState() {
        WorldUtils.resetWorldState();
        WorldUtils.setGameRule(GameRule.KEEP_INVENTORY, true);
        for (Player player : this.lockinTeamHandler.getAllOnlinePlayers()) {
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
                    for (Player player : this.lockinTeamHandler.getAllOnlinePlayers()) {
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