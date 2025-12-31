package chalkinshmeal.lockin.artifacts.compass;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import chalkinshmeal.lockin.artifacts.scoreboard.LockinScoreboard;
import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.artifacts.tasks.LockinTaskHandler;
import chalkinshmeal.lockin.artifacts.team.LockinTeamHandler;
import chalkinshmeal.mc_plugin_lib.config.ConfigHandler;
import chalkinshmeal.lockin.utils.Utils;
import chalkinshmeal.lockin.utils.EntityUtils;
import chalkinshmeal.lockin.utils.ItemUtils;
import chalkinshmeal.lockin.utils.LoggerUtils;
import chalkinshmeal.lockin.utils.TaskUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class LockinCompass {
    private final JavaPlugin plugin;
    private final LockinTeamHandler lockinTeamHandler;
    private final LockinScoreboard lockinScoreboard;
    private final Inventory teamsInv;
    private final Map<String, Inventory> tasksInvs; 
    private final int tasksPerTier;
    private final Map<UUID, UUID> targets;
    private boolean isActive;
    private boolean debug = false;
    private final Set<UUID> clickedPlayers = new HashSet<>();
    private final Map<UUID, Location> lastKnownLocation = new HashMap<>();

    private final Component compassDisplayName = Component.text(
        "Lockin", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false);
    private final String teamsInvName = "Lockin Teams";
    private final String tasksInvName = "Lockin Tasks";

    //---------------------------------------------------------------------------------------------
    // Constructor
    //---------------------------------------------------------------------------------------------
    public LockinCompass(JavaPlugin plugin, ConfigHandler configHandler, LockinTeamHandler lockinTeamHandler, LockinScoreboard lockinScoreboard) {
        this.plugin = plugin;
        this.lockinTeamHandler = lockinTeamHandler;
        this.lockinScoreboard = lockinScoreboard;
        this.tasksPerTier = Utils.getHighestMultiple((int) configHandler.getInt("tasksPerTier", 27), 9);
        this.teamsInv = Bukkit.createInventory(null, 9, Component.text(this.teamsInvName, NamedTextColor.LIGHT_PURPLE));
        this.tasksInvs = new HashMap<>();
        for (String teamName : this.lockinTeamHandler.getTeamNames()) {
            Inventory newInv = Bukkit.createInventory(null, this.tasksPerTier, Component.text(this.tasksInvName, NamedTextColor.LIGHT_PURPLE));
            this.tasksInvs.put(teamName, newInv);
        }
        this.targets = new HashMap<>();
        this.isActive = false;

        this.updateTeamsInventory();
        this.updateTasksInventory(null);
        this.sendLocationInfo();
    }

    //---------------------------------------------------------------------------------------------
    // Accessor/Mutator methods 
    //---------------------------------------------------------------------------------------------
    public int getMaxTeams() { return this.lockinTeamHandler.getNumTeams(); }
    public String getInvName() { return (this.isActive) ? Utils.stripColor(this.tasksInvName) : Utils.stripColor(this.teamsInvName); }
    public int getMaxSlots() {return (this.isActive) ? this.tasksPerTier : this.lockinTeamHandler.getNumTeams(); }
    public Inventory getTaskInv(Player player) { return this.tasksInvs.get(this.lockinTeamHandler.getTeamName(player)); }
    public void SetIsActive(boolean isActive) { this.isActive = isActive; }
    public void addTeam(String teamName) {
        Inventory newInv = Bukkit.createInventory(null, this.tasksPerTier, Component.text(this.tasksInvName, NamedTextColor.LIGHT_PURPLE));
        this.tasksInvs.put(teamName, newInv);
    }
    public void setLastKnownLocation(Player player) {
        this.lastKnownLocation.put(player.getUniqueId(), player.getLocation());
    }

    //---------------------------------------------------------------------------------------------
    // Inventory methods
    //---------------------------------------------------------------------------------------------
    public void updateTeamsInventory() {
        this.teamsInv.clear();
        for (int i = 0; i < this.lockinTeamHandler.getNumTeams(); i++) {
            List<String> playerNames = this.lockinTeamHandler.getPlayerNames(i);
            this.teamsInv.addItem(this.constructTeamItem(i, playerNames));
        }
    }

    public void updateTasksInventory(LockinTaskHandler lockinTaskHandler) {
        if (debug) Bukkit.getServer().getLogger().info("[LockinCompass::updateTasksInventory] Updating task inventories");
        if (debug) Bukkit.getServer().getLogger().info("[LockinCompass::updateTasksInventory]   Clearing inventories");
        for (Inventory tasksInv : this.tasksInvs.values()) {
            tasksInv.clear();
        }
        if (lockinTaskHandler == null) return;

        if (debug) Bukkit.getServer().getLogger().info("[LockinCompass::updateTasksInventory]   Populating with tasks");
        for (String teamName : this.tasksInvs.keySet()) {
            boolean catchUpTeam = this.lockinTeamHandler.getNumTeams() >= 3 && this.lockinScoreboard.getScore(teamName) <= 0;
            List<LockinTask> tasks = (catchUpTeam) ? lockinTaskHandler.getCatchUpTasks() : lockinTaskHandler.getTasks();
            for (LockinTask task : tasks) {
                if (debug) Bukkit.getServer().getLogger().info("[LockinCompass::updateTasksInventory]     Task: " + task.getName() + ", Team: " + teamName);
                task.setLore();
                Inventory tasksInv = this.tasksInvs.get(teamName);
                ItemStack taskItem = task.getItem();
                if (task.hasCompleted(teamName)) {
                    taskItem = Utils.setMaterial(taskItem, Material.GRAY_STAINED_GLASS_PANE);
                }
                tasksInv.addItem(taskItem);
            }
        }
    }

    public void openInventory(Player player) {
        if (debug) Bukkit.getServer().getLogger().info("[LockinCompass::openInventory] Opening inventory to player " + player.getName());
        if (debug) Bukkit.getServer().getLogger().info("[LockinCompass::openInventory]   Current Inventory Count: " + this.tasksInvs.size());
        if (debug) Bukkit.getServer().getLogger().info("[LockinCompass::openInventory]   Task Inventory: " + this.getTaskInv(player));
        if (this.isActive) player.openInventory(this.getTaskInv(player));
        else player.openInventory(this.teamsInv);
    }

    //---------------------------------------------------------------------------------------------
    // Compass methods
    //---------------------------------------------------------------------------------------------
    public void giveCompass(Player player) {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(compassDisplayName);
        item.setItemMeta(meta);

        Utils.giveItem(player, item);
    }

    //---------------------------------------------------------------------------------------------
    // Listener methods
    //---------------------------------------------------------------------------------------------
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (debug) Bukkit.getServer().getLogger().info("[LockinCompass::onPlayerInteractEvent] --- Get Team (Before) ---");
        if (debug) this.lockinTeamHandler.getTeamName(event.getPlayer());
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getItem() == null) return;
        if (event.getItem().getItemMeta().displayName() == null) return;
        if (!event.getItem().getItemMeta().displayName().equals(this.compassDisplayName)) return;
        if (this.clickedPlayers.contains(event.getPlayer().getUniqueId())) return;
        if (Utils.isRightClick(event.getAction())) {
            this.updateTeamsInventory();
            this.openInventory(event.getPlayer());
            if (debug) Bukkit.getServer().getLogger().info("[LockinCompass::onPlayerInteractEvent] --- Get Team (After) ---");
            if (debug) this.lockinTeamHandler.getTeamName(event.getPlayer());
        }
        else if (Utils.isLeftClick(event.getAction())) {
            this.setTarget(event.getPlayer(), event.getItem());
            this.clickedPlayers.add(event.getPlayer().getUniqueId());
            TaskUtils.runDelayedTask(this.plugin, () -> {
                clickedPlayers.remove(event.getPlayer().getUniqueId());
            }, 0.2f*20);

            if (debug) {
                LoggerUtils.info("Players in clickedPlayers:");
                for (UUID uuid : this.clickedPlayers) {
                    LoggerUtils.info("  " + EntityUtils.getPlayerName(uuid));
                }
            }
        }
    }

    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (debug) Bukkit.getServer().getLogger().info("[LockinCompass::onPlayerClickEvent] --- Get Team --- (Before)");
        if (debug) this.lockinTeamHandler.getTeamName((Player) event.getWhoClicked());
        // Check that inventory name matches
        String invName = Utils.asString(event.getView().title());
        if (!invName.equals(this.getInvName())) return;
        if (debug) Bukkit.getServer().getLogger().info("[LockinCompass::onPlayerClickEvent]   Inventory name matches");

        // Prevent movement
        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)
            event.setCancelled(true);
        if (debug) Bukkit.getServer().getLogger().info("[LockinCompass::onPlayerClickEvent]   Preventing movement");

        // Check that slot is valid
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= this.getMaxSlots()) return;
        if (debug) Bukkit.getServer().getLogger().info("[LockinCompass::onPlayerClickEvent]   Slot is valid");

        event.setCancelled(true);

        if (this.isActive) return;

        // Change team of player
        if (debug) Bukkit.getServer().getLogger().info("[LockinCompass::onPlayerClickEvent]   Changing team of player");
        Player player = (Player) event.getWhoClicked();
        this.lockinTeamHandler.removePlayer(player);
        this.lockinTeamHandler.addPlayer(player, slot);
        this.updateTeamsInventory();
        player.updateInventory();

        if (debug) Bukkit.getServer().getLogger().info("[LockinCompass::onPlayerClickEvent] --- Get Team (After) ---");
        if (debug) this.lockinTeamHandler.getTeamName((Player) event.getWhoClicked());
    }

    public void onInventoryDragEvent(InventoryDragEvent event) {
        if (debug) Bukkit.getServer().getLogger().info("[LockinCompass::onPlayerDragEvent] --- Get Team --- (Before)");
        if (debug) this.lockinTeamHandler.getTeamName((Player) event.getWhoClicked());
        // Check that inventory name matches
        String invName = Utils.asString(event.getView().title());
        if (!invName.equals(this.getInvName())) return;

        event.setCancelled(true);

        if (debug) Bukkit.getServer().getLogger().info("[LockinCompass::onPlayerDragEvent] --- Get Team --- (After)");
        if (debug) this.lockinTeamHandler.getTeamName((Player) event.getWhoClicked());
    }

    //---------------------------------------------------------------------------------------------
    // Target methods
    //---------------------------------------------------------------------------------------------
    private void setTarget(Player player, ItemStack compass) {
        if (debug) LoggerUtils.info("Setting target for player: " + player.getName());
        // Populate targets map if the UUID is new
        UUID uuid = player.getUniqueId();
        if (!this.targets.containsKey(uuid)) this.targets.put(uuid, null);

        // Get all online player UUIDs, excluding the current player
        List<UUID> allUUIDsInGame = this.lockinTeamHandler.getAllOnlinePlayerUUIDs();
        if (allUUIDsInGame.size() == 0) return;

        // Get old target
        UUID oldTarget = this.targets.get(uuid);
        LoggerUtils.info("  Old Target: " + oldTarget + "(" + EntityUtils.getPlayerName(oldTarget) + ")");

        // Get the next target
        int newTargetIndex = (oldTarget == null) ? 0 : allUUIDsInGame.indexOf(oldTarget) + 1;
        LoggerUtils.info("  New Target Index: " + newTargetIndex);

        // Cycle back to 0 if at size limit
        if (newTargetIndex > allUUIDsInGame.size() - 1) newTargetIndex = 0;
        LoggerUtils.info("  New Target Index, after cycling back: " + newTargetIndex);

        // Increase by 1 if matches this player
        if (allUUIDsInGame.get(newTargetIndex) == uuid) newTargetIndex += 1;
        LoggerUtils.info("  New Target Index, after increasing if matches player: " + newTargetIndex);

        // Cycle back to 0 if at size limit
        if (newTargetIndex > allUUIDsInGame.size() - 1) newTargetIndex = 0;
        LoggerUtils.info("  New Target Index, after cycling back: " + newTargetIndex);

        // Set new target
        UUID newTarget = allUUIDsInGame.get(newTargetIndex);
        LoggerUtils.info("  New Target: " + newTarget + "(" + EntityUtils.getPlayerName(newTarget) + ")");

        if (debug) {
            LoggerUtils.info("  Players in allUUIDsInGame:");
            for (UUID _uuid : allUUIDsInGame) {
                LoggerUtils.info("    " + EntityUtils.getPlayerName(_uuid));
            }
        }

        // Update target
        this.targets.put(uuid, newTarget);
    }

    private void sendLocationInfo() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : targets.keySet()) {
                    // Require that the player is online
                    Player player = plugin.getServer().getPlayer(uuid);
                    if (player == null || !player.isOnline()) continue;

                    // Require that the player is holding the compass in their main hand
                    ItemStack compass = player.getInventory().getItemInMainHand();
                    if (compass == null) continue;
                    if (compass.getItemMeta() == null) continue;
                    if (compass.getItemMeta().displayName() == null) continue;
                    if (!compass.getItemMeta().displayName().equals(compassDisplayName)) continue;

                    // Require that the player has an active target
                    if (targets.get(uuid) == null) continue;

                    // Send player location information
                    Player targetPlayer = plugin.getServer().getPlayer(targets.get(uuid));
                    Component locationInfo = null;
                    if (targetPlayer == null || !player.isOnline()) {
                        locationInfo = Component.text()
                            .decoration(TextDecoration.ITALIC, false)
                            .append(Component.text("Pointing to " + targetPlayer.getName() + " ", NamedTextColor.GRAY))
                            .append(Component.text("(Player is offline)")).build();
                    }
                    else if (player.getWorld().getName() != targetPlayer.getWorld().getName()) {
                        locationInfo = Component.text()
                            .decoration(TextDecoration.ITALIC, false)
                            .append(Component.text("Pointing to " + targetPlayer.getName() + " ", NamedTextColor.GRAY))
                            .append(Component.text("(Player is not in your dimension)")).build();
                    }
                    else {
                        float distanceAway = EntityUtils.getDistance(player, targetPlayer);
                        locationInfo = Component.text()
                            .decoration(TextDecoration.ITALIC, false)
                            .append(Component.text("Pointing to " + targetPlayer.getName() + " ", NamedTextColor.GRAY))
                            .append(Component.text("(" + distanceAway + " blocks)", NamedTextColor.BLUE)).build();
                    }
                    player.sendActionBar(locationInfo);

                    // Update compass target
                    ItemUtils.setCompassTarget(compass, targetPlayer);
                }
            }
        }.runTaskTimer(plugin, 0, 2); // Run the task every 2 ticks (0.1 second)
    }

    //---------------------------------------------------------------------------------------------
    // Utility methods
    //---------------------------------------------------------------------------------------------
    private ItemStack constructTeamItem(int teamIndex, List<String> playerNames) {
        ItemStack item = new ItemStack(this.lockinTeamHandler.getTeamMaterials().get(teamIndex));
        item = Utils.setDisplayName(item, Component.text(this.lockinTeamHandler.getTeamName(teamIndex), NamedTextColor.AQUA));
        item = Utils.addLore(item, Component.text(playerNames.size() + " players", NamedTextColor.DARK_PURPLE));
        for (String playerName : playerNames) {
            item = Utils.addLore(item, Component.text(" " + playerName, NamedTextColor.DARK_AQUA));
        }
        return item;
    }
}
