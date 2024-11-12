package chalkinshmeal.lockin.artifacts.game;

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

import chalkinshmeal.lockin.artifacts.compass.LockinCompass;
import chalkinshmeal.lockin.artifacts.countdown.CountdownBossBar;
import chalkinshmeal.lockin.artifacts.scoreboard.LockinScoreboard;
import chalkinshmeal.lockin.artifacts.tasks.LockinTaskHandler;
import chalkinshmeal.lockin.artifacts.team.LockinTeamHandler;
import chalkinshmeal.lockin.data.ConfigHandler;
import chalkinshmeal.lockin.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public class GameHandler {
    private final JavaPlugin plugin;
    private final ConfigHandler configHandler;
    private final LockinCompass lockinCompass;
    private final LockinTaskHandler lockinTaskHandler;
    private final CountdownBossBar countdownBossBar;
    private final LockinScoreboard lockinScoreboard;
    public final LockinTeamHandler lockinTeamHandler;
    private final int queueTime;
    private final int gameTime;
    private final int maxTier;

    // Temporary status
    public boolean isActive = false;
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

    //---------------------------------------------------------------------------------------------
    // Game methods
    //---------------------------------------------------------------------------------------------
    public void incrementTier(int tier)
    {
        for (Player player : this.lockinTeamHandler.getAllPlayers()) {
            player.sendMessage("Moving to tier " + tier);

        }
        // Create task list
        if (!this.lockinTaskHandler.CreateTaskList(tier)) {
            System.out.println("Something went wrong with tier " + tier);
        }

        // Register listeners for tasks
        this.lockinTaskHandler.registerListeners();

        // Update compass with tasks
        this.lockinCompass.updateTasksInventory(this.lockinTaskHandler);
    }

    public void queue() {
        this.state = GameState.QUEUE;
        this.currentTier = 1;

        this.isActive = true;
        this.lockinScoreboard.init(this.lockinTeamHandler);

        for (Player player : this.lockinTeamHandler.getAllPlayers()) {
            this.resetPlayerState(player);
            this.lockinCompass.giveCompass(player);
            this.DisplayCountdownTask(plugin, player, this.queueTime);
            this.lockinScoreboard.setScore(this.lockinTeamHandler.getTeamName(player), 0);
            this.lockinScoreboard.showToPlayer(player);
        }

        this.resetWorldState();
        this.lockinCompass.SetIsActive(true);

        this.incrementTier(this.currentTier);
        this.delayStartTask(this.plugin, this, this.queueTime);
    }

    public void start() {
        this.state = GameState.PLAY;

        // Global operations
        this.countdownBossBar.start();
        this.lockinTaskHandler.registerListeners();
        this.checkAllTasksDoneTask(plugin, this, lockinTaskHandler);
        //this.delayStopTask(this.plugin, this, this.gameTime);
        this.destroySpawnCage();

        // Per-player operations
        for (Player player : this.lockinTeamHandler.getAllPlayers()) {
            player.sendMessage(Component.text("lockin game starting.", NamedTextColor.GOLD));
        }
    }

    @SuppressWarnings("deprecation")
    public void stop() {
        // Get winner
        List<String> winningTeams = new ArrayList<>();
        int maxPoints = -100;
        for (String teamName : this.lockinTeamHandler.getTeamNames()) {
            int teamPoints = this.lockinScoreboard.getScore(teamName);
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
        for (Player player : this.lockinTeamHandler.getAllPlayers()) {
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
        //this.lockinTaskHandler.CreateSuddenDeathTaskList();
        this.lockinTaskHandler.registerListeners();
        this.lockinCompass.updateTasksInventory(this.lockinTaskHandler);
        this.checkSuddenDeathTasksDoneTask(plugin, lockinTaskHandler);

        // Per-player operations
        for (Player player : this.lockinTeamHandler.getAllPlayers()) {
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
        this.isActive = false;
        this.state = GameState.DONE;
        //this.lockinCompass.SetIsActive(false);
        this.lockinTaskHandler.unRegisterListeners();
        this.countdownBossBar.stop();

        for (Player player : this.lockinTeamHandler.getAllPlayers()) {
            //this.lockinScoreboard.hideFromPlayer(player);
            for (PotionEffect effect : player.getActivePotionEffects()) player.removePotionEffect(effect.getType());
            player.sendMessage(Component.text("lockin game ended.", NamedTextColor.GOLD));
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

    private void checkAllTasksDoneTask(JavaPlugin plugin, GameHandler gameHandler, LockinTaskHandler lockinTaskHandler) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive) {
                    this.cancel();
                    return;
                }
                if (lockinTaskHandler.areAllTasksDone()) {
                    if (gameHandler.currentTier == gameHandler.maxTier) {
                        stop();
                        this.cancel();
                        return;
                    }
                    else {
                        gameHandler.currentTier += 1;
                        gameHandler.incrementTier(gameHandler.currentTier);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20); // Run the task every 20 ticks (1 second)
    }

    private void checkSuddenDeathTasksDoneTask(JavaPlugin plugin, LockinTaskHandler lockinTaskHandler) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive) {
                    this.cancel();
                    return;
                }
                if (lockinTaskHandler.areSuddenDeathTasksDone()) {
                    stop();
                    this.cancel();
                    return;
                }
            }
        }.runTaskTimer(plugin, 0, 20); // Run the task every 20 ticks (1 second)
    }
}