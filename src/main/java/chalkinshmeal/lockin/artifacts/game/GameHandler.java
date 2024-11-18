package chalkinshmeal.lockin.artifacts.game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import chalkinshmeal.lockin.artifacts.compass.LockinCompass;
import chalkinshmeal.lockin.artifacts.countdown.CountdownBossBar;
import chalkinshmeal.lockin.artifacts.scoreboard.LockinScoreboard;
import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.artifacts.tasks.LockinTaskHandler;
import chalkinshmeal.lockin.artifacts.team.LockinTeamHandler;
import chalkinshmeal.lockin.data.ConfigHandler;
import chalkinshmeal.lockin.utils.LoggerUtils;
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
    private final int gameTime;
    private final int maxTier;
    private final boolean debug = false;

    // Temporary status
    public GameState state = GameState.INACTIVE;
    public int currentTier = 0;

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
        this.gameTime = this.configHandler.getInt("timeLimit", 600);
        this.maxTier = 10;
        this.countdownBossBar = new CountdownBossBar(this.plugin, this.configHandler, this.gameTime);
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

        // Set world state
        WorldUtils.resetWorldState();
        WorldUtils.setGameRule(GameRule.KEEP_INVENTORY, true);

        this.lockinTeamHandler.init();
        this.lockinScoreboard.init(this.lockinTeamHandler);

        for (Player player : this.lockinTeamHandler.getAllOnlinePlayers()) {
            this.resetPlayerState(player);
            this.lockinCompass.giveCompass(player);
            this.displayCountdownTask(plugin, player, this.queueTime);
        }

        this.lockinCompass.SetIsActive(true);

        this.incrementTier();
        this.delayStartTask(this.plugin, this, this.queueTime);
    }

    public void start() {
        this.state = GameState.PLAY;

        // Set world state
        WorldUtils.destroySpawnCage();

        // Global operations
        this.countdownBossBar.start();
        this.lockinTaskHandler.registerListeners();
        this.checkAllTasksDoneTask();

        // Per-player operations
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

        // Check end game conditions
        boolean afterMaxTier = (this.currentTier > this.maxTier);
        boolean atMostOneTeamHasPositiveLives = (this.lockinScoreboard.atMostOneTeamHasPositiveLives() && this.currentTier > 1);
        if (afterMaxTier || atMostOneTeamHasPositiveLives) {
            this.stop();
            return;
        }

        // Cosmetics
        if (this.currentTier != 1) {
            for (Player player : this.lockinTeamHandler.getAllOnlinePlayers()) {
                player.sendMessage(Component.text()
                    .append(Component.text("Advancing to ", NamedTextColor.GRAY))
                    .append(Component.text("Tier " + this.currentTier, NamedTextColor.BLUE)));
                Utils.playSound(player, Sound.BLOCK_NOTE_BLOCK_CHIME);
            }
        }

        // Update task list
        this.lockinTaskHandler.stopCurrentTasks();
        if (!this.lockinTaskHandler.updateTaskList(this.currentTier)) {
            System.out.println("Something went wrong with tier " + this.currentTier);
        }

        // Register listeners for tasks
        this.lockinTaskHandler.registerListeners();

        // Update compass with tasks
        this.lockinCompass.updateTasksInventory(this.lockinTaskHandler);

        // Start countdown again
        if (this.currentTier != 1) {
            if (debug) LoggerUtils.info("Resetting countdown boss bar");
            this.countdownBossBar.reset();
        }

        // Start delayed times up task
        float delayTime = (this.currentTier == 1) ? this.gameTime + this.queueTime : this.gameTime;
        this.delayTimesUpTask(plugin, this, (int) delayTime);
    }

    @SuppressWarnings("deprecation")
    public void stop() {
        // Get winner
        List<String> winningTeams = this.lockinScoreboard.getWinningTeams();

        if (winningTeams.size() > 1) {
            this.suddenDeath(winningTeams);
            return;
        }

        // Per-player operations
        for (Player player : this.lockinTeamHandler.getAllOnlinePlayers()) {
            player.showTitle(Title.title(
                Component.text(winningTeams.get(0) + " won!", NamedTextColor.GOLD),
                Component.empty(), // No subtitle
                Title.Times.of(java.time.Duration.ZERO, java.time.Duration.ofSeconds(5), java.time.Duration.ofSeconds(1))
            ));
            if (this.lockinTeamHandler.getTeamPlayers(winningTeams.get(0)).contains(player.getUniqueId())) {
                Utils.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_1);
            }
            else {
                Utils.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_6);
            }
        }

        this.end();
    }

    @SuppressWarnings("deprecation")
    public void suddenDeath(List<String> winningTeams) {
        // Global operations
        this.countdownBossBar.update(Component.text("Overtime", NamedTextColor.RED));
        this.lockinTaskHandler.unRegisterListeners();
        this.lockinTaskHandler.registerListeners();
        this.lockinCompass.updateTasksInventory(this.lockinTaskHandler);

        // Per-player operations
        for (Player player : this.lockinTeamHandler.getAllOnlinePlayers()) {
            player.showTitle(Title.title(
                Component.text("Sudden Death", NamedTextColor.GOLD),
                Component.text("Compass updated. First to 3 wins", NamedTextColor.GOLD),
                Title.Times.of(java.time.Duration.ZERO, java.time.Duration.ofSeconds(5), java.time.Duration.ofSeconds(1))
            ));
            this.lockinScoreboard.setScore(this.lockinTeamHandler.getTeamName(player), 0);
            this.resetPlayerState(player);
            this.lockinCompass.giveCompass(player);
            player.setBedSpawnLocation(null, true);
        }
    }

    public void end() {
        this.state = GameState.DONE;
        this.lockinTaskHandler.unRegisterListeners();
        this.countdownBossBar.stop();

        for (Player player : this.lockinTeamHandler.getAllOnlinePlayers()) {
            player.sendMessage(Component.text("Lockin game ended.", NamedTextColor.GOLD));
        }
    }

    //---------------------------------------------------------------------------------------------
    // Listener methods
    //---------------------------------------------------------------------------------------------
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (this.state == GameState.INACTIVE) {
            System.out.println("Teleporting to: " + Bukkit.getWorld("world").getSpawnLocation());
            this.delayTeleportTask(this.plugin, this, player, Bukkit.getWorld("world").getSpawnLocation());
            event.setCancelled(true);
            return;
        }

        if (!this.lockinTeamHandler.getAllPlayers().contains(player)) return;
        for (PotionEffect effect : player.getActivePotionEffects()) player.removePotionEffect(effect.getType());
    }

    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (this.state != GameState.QUEUE) return;

        event.setCancelled(true);
    }

    //---------------------------------------------------------------------------------------------
    // Utility methods
    //---------------------------------------------------------------------------------------------
    private void resetPlayerState(Player player) {
        player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.setSaturation(5);
        player.setFireTicks(0);
        player.setExp(0);
        player.setLevel(0);
        player.setGlowing(false);
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.setGameMode(GameMode.SURVIVAL);
        for (PotionEffect effect : player.getActivePotionEffects()) player.removePotionEffect(effect.getType());

        World world = Bukkit.getWorld("world");
        Location spawnLocation = world.getSpawnLocation();
        Location teleportLocation = new Location(
            world,
            spawnLocation.getX() + 0.5,
            spawnLocation.getY() + 0.1,
            spawnLocation.getZ() + 0.5);
        player.teleport(teleportLocation);
    }

    //---------------------------------------------------------------------------------------------
    // Task methods
    //---------------------------------------------------------------------------------------------
    private void delayStartTask(JavaPlugin plugin, GameHandler gameHandler, int delaySeconds) {
        int delayTicks = delaySeconds * 20;
 
        new BukkitRunnable() {
            @Override
            public void run() {
                gameHandler.start();
            }
        }.runTaskLater(plugin, delayTicks);
    }

    private void delayTimesUpTask(JavaPlugin plugin, GameHandler gameHandler, int delaySeconds) {
        int delayTicks = delaySeconds * 20;
 
        new BukkitRunnable() {
            @Override
            public void run() {
                gameHandler.incrementTier();
            }
        }.runTaskLater(plugin, delayTicks);
    }

    private void delayTeleportTask(JavaPlugin plugin, GameHandler gameHandler, Player player, Location location) {
        int delayTicks = 1;
 
        new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(location);
            }
        }.runTaskLater(plugin, delayTicks);
    }

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

                // Sound
                if (remainingSeconds <= 5) {
                    Utils.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING);
                }
                else {
                    Utils.playSound(player, Sound.BLOCK_NOTE_BLOCK_HAT);
                }

                // Display the remaining time in the center of the screen
                player.showTitle(Title.title(
                    Component.text(remainingSeconds, NamedTextColor.GOLD),
                    Component.empty(), // No subtitle
                    Title.Times.of(java.time.Duration.ZERO, java.time.Duration.ofSeconds(1), java.time.Duration.ofSeconds(5))
                ));
                remainingSeconds--;
            }
        }.runTaskTimer(plugin, 0, 20); // Run the task every 20 ticks (1 second)
    }

    private void checkAllTasksDoneTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (state == GameState.INACTIVE) {
                    this.cancel();
                    return;
                }
                if (lockinTaskHandler.areAllTasksDone()) {
                    if (currentTier > maxTier) {
                        stop();
                        this.cancel();
                        return;
                    }
                    else {
                        incrementTier();
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20); // Run the task every 20 ticks (1 second)
    }
}