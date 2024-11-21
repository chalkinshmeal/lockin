package chalkinshmeal.lockin.artifacts.game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private final int multipleTeamTimeLimit;
    private final int maxTier;
    private final boolean debug = false;

    // Temporary status
    public GameState state = GameState.INACTIVE;
    public GameType gameType = GameType.SINGLE_TEAM;
    public int currentTier = 0;
    public List<Integer> taskIDs = new ArrayList<>();

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

        // Set world state
        this.resetWorldState();

        // Set game state
        this.lockinTeamHandler.init();
        this.lockinScoreboard.init(this.lockinTeamHandler);
        this.lockinCompass.SetIsActive(true);
        this.incrementTier();
        this.gameType = this.lockinTeamHandler.getNumTeams() <= 1 ? GameType.SINGLE_TEAM : GameType.MULTIPLE_TEAM;

        // Delayed start
        this.taskIDs.add(TaskUtils.runDelayedTask(this.plugin, this::start, this.queueTime));

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
        this.lockinTaskHandler.registerListeners();
        if (this.gameType == GameType.SINGLE_TEAM) {
            this.countdownBossBar = new CountdownBossBar(this.plugin, this.configHandler, this.singleTeamTimeLimit);
            this.countdownBossBar.start();

            // Repeatedly check if all tasks are done, then increment tier
            this.taskIDs.add(TaskUtils.runRepeatingTask(this.plugin, () -> {
                if (lockinTaskHandler.hasOneTeamCompletedAllTasks()) {
                    incrementTier();
                }
            }, 0f, 1f));
        }

        else if (this.gameType == GameType.MULTIPLE_TEAM) {
            this.countdownBossBar = new CountdownBossBar(this.plugin, this.configHandler, this.multipleTeamTimeLimit);

            // Repeatedly check if one team has completed all tasks, then start countdown
            this.taskIDs.add(TaskUtils.runRepeatingTask(this.plugin, () -> {
                if (lockinTaskHandler.hasOneTeamCompletedAllTasks()) {
                    this.countdownBossBar.start();
                    this.taskIDs.add(TaskUtils.runDelayedTask(this.plugin, this::incrementTier, this.multipleTeamTimeLimit));
                }
            }, 0f, 1f));
        }

        // Cosmetics
        for (Player player : this.lockinTeamHandler.getAllOnlinePlayers()) {
            player.sendMessage(Component.text("Lockin game starting.", NamedTextColor.GOLD));
        }
    }

    public void incrementTier() {
        this.currentTier += 1;

        // Subtract lives
        if (this.currentTier != 1) {
            for (LockinTask task : this.lockinTaskHandler.getTasks()) {
                for (String teamName : this.lockinTeamHandler.getTeamNames()) {
                    if (!task.hasCompleted(teamName)) this.lockinScoreboard.subtractScore(teamName, 1);
                }
            }
        }

        // Check sudden death conditions
        if (this.gameType == GameType.MULTIPLE_TEAM && this.currentTier > this.maxTier) {
            this.suddenDeath();
            return;
        }

        // Check end game conditions
        if (this.gameType == GameType.MULTIPLE_TEAM && this.lockinScoreboard.atMostOneTeamHasPositiveLives() && this.currentTier > 1 ||
            this.gameType == GameType.SINGLE_TEAM && this.currentTier > this.maxTier ||
            this.gameType == GameType.SINGLE_TEAM && !this.lockinScoreboard.atLeastOneTeamHasPositiveLives() && this.currentTier > 1) {
            this.stop();
            return;
        }

        // Set game state
        this.lockinTaskHandler.updateTaskList(this.currentTier);
        this.lockinTaskHandler.registerListeners();
        this.lockinCompass.updateTasksInventory(this.lockinTaskHandler);

        // Delayed increment tier (if single team mode)
        // In multiple team mode, the responsibility to start the next tier lies in the start()::runRepeatingTask call
        if (this.gameType == GameType.SINGLE_TEAM) {
            float delayTime = (this.currentTier == 1) ? this.singleTeamTimeLimit + this.queueTime : this.singleTeamTimeLimit;
            this.taskIDs.add(TaskUtils.runDelayedTask(this.plugin, this::incrementTier, (long) delayTime));
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
        // Set world state (set all teams with <= 0 points to Spectator Mode)
        LoggerUtils.info("In sudden death");
        for (String teamName : this.lockinTeamHandler.getTeamNames()) {
            LoggerUtils.info("  Checking team name: " + teamName);
            LoggerUtils.info("    Display Name:     " + this.lockinTeamHandler.getDisplayTeamName(teamName));
            LoggerUtils.info("    Score:            " + this.lockinScoreboard.getScore(this.lockinTeamHandler.getDisplayTeamName(teamName)));
            LoggerUtils.info("    Teams:            ");
            for (String _teamName : this.lockinScoreboard.getTeamNames()) {
                LoggerUtils.info("      " + _teamName);
            }
            if (this.lockinScoreboard.getScore(teamName) <= 0) {
                for (UUID uuid : this.lockinTeamHandler.getTeamPlayers(teamName)) {
                    Player player = EntityUtils.getPlayer(uuid);
                    if (player != null) player.setGameMode(GameMode.SPECTATOR);
                }
            }
        }

        // Set game state
        this.countdownBossBar.update(Component.text("Overtime", NamedTextColor.RED));
        this.lockinTaskHandler.updateSuddenDeathTaskList();
        this.lockinTaskHandler.registerListeners();
        this.lockinCompass.updateTasksInventory(this.lockinTaskHandler);
        for (String teamName : this.lockinTeamHandler.getTeamNames()) {
            if (this.lockinScoreboard.getScore(teamName) > 0) {
                this.lockinScoreboard.setScore(teamName, 1);
            }
        }

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