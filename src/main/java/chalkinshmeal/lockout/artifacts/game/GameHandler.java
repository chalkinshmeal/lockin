package chalkinshmeal.lockout.artifacts.game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
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

import chalkinshmeal.lockout.artifacts.compass.LockoutCompass;
import chalkinshmeal.lockout.artifacts.countdown.CountdownBossBar;
import chalkinshmeal.lockout.artifacts.scoreboard.LockoutScoreboard;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTaskHandler;
import chalkinshmeal.lockout.artifacts.team.LockoutTeamHandler;
import chalkinshmeal.lockout.data.ConfigHandler;
import chalkinshmeal.lockout.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public class GameHandler {
    private final JavaPlugin plugin;
    private final ConfigHandler configHandler;
    private final LockoutCompass lockoutCompass;
    private final LockoutTaskHandler lockoutTaskHandler;
    private final CountdownBossBar countdownBossBar;
    private final LockoutScoreboard lockoutScoreboard;
    public final LockoutTeamHandler lockoutTeamHandler;
    private final int queueTime;
    private final int gameTime;

    // Temporary status
    public boolean isActive = false;
    public GameState state = GameState.INACTIVE;

    //---------------------------------------------------------------------------------------------
    // Constructor
    //---------------------------------------------------------------------------------------------
    public GameHandler(JavaPlugin plugin, ConfigHandler configHandler, LockoutCompass lockoutCompass, LockoutTaskHandler lockoutTaskHandler, LockoutScoreboard lockoutScoreboard, LockoutTeamHandler lockoutTeamHandler) {
        this.plugin = plugin;
        this.configHandler = configHandler;
        this.lockoutCompass = lockoutCompass;
        this.lockoutTaskHandler = lockoutTaskHandler;
        this.lockoutScoreboard = lockoutScoreboard;
        this.lockoutTeamHandler = lockoutTeamHandler;
        this.queueTime = this.configHandler.getInt("queueTime", 120);
        this.gameTime = this.configHandler.getInt("timeLimit", 600);
        this.countdownBossBar = new CountdownBossBar(this.plugin, this.configHandler, this.gameTime);
    }

    //---------------------------------------------------------------------------------------------
    // Accessor/Mutator methods
    //---------------------------------------------------------------------------------------------
    public int getNumTeams() { return this.lockoutTeamHandler.getNumTeams(); }

    //---------------------------------------------------------------------------------------------
    // Game methods
    //---------------------------------------------------------------------------------------------
    public void queue() {
        this.state = GameState.QUEUE;

        // Create task list first, in case there are any problems with the config
        if (!this.lockoutTaskHandler.CreateTaskList()) return;

        this.isActive = true;
        this.lockoutScoreboard.init(this.lockoutTeamHandler);

        for (Player player : this.lockoutTeamHandler.getAllPlayers()) {
            this.resetPlayerState(player);
            this.lockoutCompass.giveCompass(player);
            this.DisplayCountdownTask(plugin, player, this.queueTime);
            this.lockoutScoreboard.setScore(this.lockoutTeamHandler.getTeamName(player), 0);
            this.lockoutScoreboard.showToPlayer(player);
        }

        this.resetWorldState();
        this.lockoutCompass.SetIsActive(true);
        this.lockoutCompass.updateTasksInventory(this.lockoutTaskHandler);
        this.delayStartTask(this.plugin, this, this.queueTime);
    }

    public void start() {
        this.state = GameState.PLAY;

        // Global operations
        this.countdownBossBar.start();
        this.lockoutTaskHandler.registerListeners();
        this.checkAllTasksDoneTask(plugin, lockoutTaskHandler);
        this.delayStopTask(this.plugin, this, this.gameTime);
        this.destroySpawnCage();

        // Per-player operations
        for (Player player : this.lockoutTeamHandler.getAllPlayers()) {
            player.sendMessage(Component.text("Lockout game starting.", NamedTextColor.GOLD));
        }
    }

    @SuppressWarnings("deprecation")
    public void stop() {
        // Get winner
        List<String> winningTeams = new ArrayList<>();
        int maxPoints = -100;
        for (String teamName : this.lockoutTeamHandler.getTeamNames()) {
            int teamPoints = this.lockoutScoreboard.getScore(teamName);
            if (teamPoints > maxPoints) {
                maxPoints = teamPoints;
                winningTeams.clear();
                winningTeams.add(teamName);
            }
            else if (teamPoints == maxPoints) {
                winningTeams.add(teamName);
            }
        }

        if (winningTeams.size() > 1) {
            this.suddenDeath(winningTeams);
            return;
        }

        // Per-player operations
        for (Player player : this.lockoutTeamHandler.getAllPlayers()) {
            player.showTitle(Title.title(
                Component.text(winningTeams.get(0) + " won!", NamedTextColor.GOLD),
                Component.empty(), // No subtitle
                Title.Times.of(java.time.Duration.ZERO, java.time.Duration.ofSeconds(5), java.time.Duration.ofSeconds(1))
            ));
            if (this.lockoutTeamHandler.getTeamPlayers(winningTeams.get(0)).contains(player.getUniqueId())) {
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
        this.lockoutTaskHandler.unRegisterListeners();
        this.lockoutTaskHandler.CreateSuddenDeathTaskList();
        this.lockoutTaskHandler.registerListeners();
        this.lockoutCompass.updateTasksInventory(this.lockoutTaskHandler);
        this.checkSuddenDeathTasksDoneTask(plugin, lockoutTaskHandler);

        // Per-player operations
        for (Player player : this.lockoutTeamHandler.getAllPlayers()) {
            player.showTitle(Title.title(
                Component.text("Sudden Death", NamedTextColor.GOLD),
                Component.text("Compass updated. First to 3 wins", NamedTextColor.GOLD),
                Title.Times.of(java.time.Duration.ZERO, java.time.Duration.ofSeconds(5), java.time.Duration.ofSeconds(1))
            ));
            this.lockoutScoreboard.setScore(this.lockoutTeamHandler.getTeamName(player), 0);
            this.resetPlayerState(player);
            this.lockoutCompass.giveCompass(player);
            player.setBedSpawnLocation(null, true);
        }
    }

    public void end() {
        this.isActive = false;
        this.state = GameState.DONE;
        //this.lockoutCompass.SetIsActive(false);
        this.lockoutTaskHandler.unRegisterListeners();
        this.countdownBossBar.stop();

        for (Player player : this.lockoutTeamHandler.getAllPlayers()) {
            //this.lockoutScoreboard.hideFromPlayer(player);
            for (PotionEffect effect : player.getActivePotionEffects()) player.removePotionEffect(effect.getType());
            player.sendMessage(Component.text("Lockout game ended.", NamedTextColor.GOLD));
        }
    }

    //---------------------------------------------------------------------------------------------
    // Listener methods
    //---------------------------------------------------------------------------------------------
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (!this.isActive) {
            System.out.println("Teleporting to: " + Bukkit.getWorld("world").getSpawnLocation());
            this.delayTeleportTask(this.plugin, this, player, Bukkit.getWorld("world").getSpawnLocation());
            event.setCancelled(true);
            return;
        }

        if (!this.lockoutTeamHandler.getAllPlayers().contains(player)) return;
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
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
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

    private void resetWorldState() {
        World world = Bukkit.getWorld("world");
        World nether = Bukkit.getWorld("world_nether");
        World theend = Bukkit.getWorld("world_the_end");
        world.setTime(1000);

        world.setGameRule(GameRule.KEEP_INVENTORY, true);
        nether.setGameRule(GameRule.KEEP_INVENTORY, true);
        theend.setGameRule(GameRule.KEEP_INVENTORY, true);
    }

    public void createSpawnCage()
    {
        Location location = Bukkit.getWorld("world").getSpawnLocation();
        this.createHollowCube(location);
    }

    public void destroySpawnCage()
    {
        Location location = Bukkit.getWorld("world").getSpawnLocation();
        this.destroyHollowCube(location);
    }

    // Method to create a 5x5 hollow cube of obsidian centered around the given location
    public void createHollowCube(Location center) {
        World world = center.getWorld();
        int startX = center.getBlockX() - 2;  // Adjust to make it centered
        int startY = center.getBlockY() - 1;
        int startZ = center.getBlockZ() - 2;

        // Loop through a 5x5x5 area
        for (int x = startX; x < startX + 5; x++) {
            for (int y = startY; y < startY + 5; y++) {
                for (int z = startZ; z < startZ + 5; z++) {
                    boolean isEdge = (x == startX || x == startX + 4 || 
                                      y == startY || y == startY + 4 || 
                                      z == startZ || z == startZ + 4);

                    // Only place obsidian on the edges, leaving the inside hollow
                    if (isEdge) {
                        world.getBlockAt(x, y, z).setType(Material.BEDROCK);
                    }
                    else {
                        world.getBlockAt(x, y, z).setType(Material.AIR);
                    }
                }
            }
        }
    }

    public void destroyHollowCube(Location center) {
        World world = center.getWorld();
        int startX = center.getBlockX() - 2;  // Adjust to make it centered
        int startY = center.getBlockY() - 1;
        int startZ = center.getBlockZ() - 2;

        // Loop through a 5x5x5 area
        for (int x = startX; x < startX + 5; x++) {
            for (int y = startY; y < startY + 5; y++) {
                for (int z = startZ; z < startZ + 5; z++) {
                    boolean isEdge = (x == startX || x == startX + 4 || 
                                      y == startY || y == startY + 4 || 
                                      z == startZ || z == startZ + 4);

                    // Only place obsidian on the edges, leaving the inside hollow
                    if (isEdge) {
                        world.getBlockAt(x, y, z).setType(Material.AIR);
                    }
                }
            }
        }
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

    private void delayStopTask(JavaPlugin plugin, GameHandler gameHandler, int delaySeconds) {
        int delayTicks = delaySeconds * 20;
 
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive) {
                    this.cancel();
                    return;
                }
                gameHandler.stop();
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

    private void DisplayCountdownTask(JavaPlugin plugin, Player player, int durationSeconds) {
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
                if (!isActive) {
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

    private void checkAllTasksDoneTask(JavaPlugin plugin, LockoutTaskHandler lockoutTaskHandler) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive) {
                    this.cancel();
                    return;
                }
                if (lockoutTaskHandler.areAllTasksDone()) {
                    stop();
                    this.cancel();
                    return;
                }
            }
        }.runTaskTimer(plugin, 0, 20); // Run the task every 20 ticks (1 second)
    }

    private void checkSuddenDeathTasksDoneTask(JavaPlugin plugin, LockoutTaskHandler lockoutTaskHandler) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive) {
                    this.cancel();
                    return;
                }
                if (lockoutTaskHandler.areSuddenDeathTasksDone()) {
                    stop();
                    this.cancel();
                    return;
                }
            }
        }.runTaskTimer(plugin, 0, 20); // Run the task every 20 ticks (1 second)
    }
}