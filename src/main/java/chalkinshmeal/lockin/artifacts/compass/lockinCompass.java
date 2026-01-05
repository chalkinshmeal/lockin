package chalkinshmeal.lockin.artifacts.compass;

import java.util.ArrayList;
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

import chalkinshmeal.lockin.artifacts.tasks.CustomTaskHandler;
import chalkinshmeal.mc_plugin_lib.config.ConfigFile;
import chalkinshmeal.mc_plugin_lib.custom_tasks.CustomTask;
import chalkinshmeal.mc_plugin_lib.items.ItemStackUtils;
import chalkinshmeal.mc_plugin_lib.logging.LoggerUtils;
import chalkinshmeal.mc_plugin_lib.teams.Team;
import chalkinshmeal.mc_plugin_lib.teams.TeamHandler;
import chalkinshmeal.lockin.utils.Utils;
import chalkinshmeal.lockin.utils.EntityUtils;
import chalkinshmeal.lockin.utils.ItemUtils;
import chalkinshmeal.lockin.utils.TaskUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class LockinCompass {
    private final JavaPlugin plugin;
    private final TeamHandler teamHandler;
    private final Inventory teamsInv;
    private final Map<String, Inventory> tasksInvs; 
    private final int tasksPerTier;
    private final Map<UUID, UUID> targets;
    private boolean isActive;
    private final Set<UUID> clickedPlayers = new HashSet<>();
    private final Map<UUID, Location> lastKnownLocation = new HashMap<>();

    private final Component compassDisplayName = Component.text(
        "Lockin", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false);
    private final String teamsInvName = "Lockin Teams";
    private final String tasksInvName = "Lockin Tasks";

    //---------------------------------------------------------------------------------------------
    // Constructor
    //---------------------------------------------------------------------------------------------
    public LockinCompass(JavaPlugin plugin, ConfigFile config, TeamHandler teamHandler) {
        this.plugin = plugin;
        this.teamHandler = teamHandler;
        this.tasksPerTier = Utils.getHighestMultiple((int) config.getInt("tasksPerTier", 27), 9);
        this.teamsInv = Bukkit.createInventory(null, 9, Component.text(this.teamsInvName, NamedTextColor.LIGHT_PURPLE));
        this.tasksInvs = new HashMap<>();
        for (Team team : this.teamHandler.getTeams()) {
            Inventory newInv = Bukkit.createInventory(null, this.tasksPerTier, Component.text(this.tasksInvName, NamedTextColor.LIGHT_PURPLE));
            this.tasksInvs.put(team.getKey(), newInv);
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
    public int getMaxTeams() { return this.teamHandler.getNumTeams(); }
    public String getInvName() { return (this.isActive) ? Utils.stripColor(this.tasksInvName) : Utils.stripColor(this.teamsInvName); }
    public int getMaxSlots() {return (this.isActive) ? this.tasksPerTier : this.teamHandler.getNumTeams(); }
    public Inventory getTaskInv(Player player) { return this.tasksInvs.get(this.teamHandler.getTeam(player).getKey()); }
    public void SetIsActive(boolean isActive) { this.isActive = isActive; }
    public void addTeam(Team team) {
        Inventory newInv = Bukkit.createInventory(null, this.tasksPerTier, Component.text(this.tasksInvName, NamedTextColor.LIGHT_PURPLE));
        this.tasksInvs.put(team.getKey(), newInv);
    }
    public void setLastKnownLocation(Player player) {
        this.lastKnownLocation.put(player.getUniqueId(), player.getLocation());
    }

    //---------------------------------------------------------------------------------------------
    // Inventory methods
    //---------------------------------------------------------------------------------------------
    public void updateTeamsInventory() {
        this.teamsInv.clear();
        int i = 0;
        for (Team team : this.teamHandler.getTeams()) {
            this.teamsInv.addItem(this.constructTeamItem(i, team));
            i++;
        }
    }

    public void updateTasksInventory(CustomTaskHandler lockinTaskHandler) {
        for (Inventory tasksInv : this.tasksInvs.values()) {
            tasksInv.clear();
        }
        if (lockinTaskHandler == null) return;

        for (Team team : this.teamHandler.getTeams()) {
            for (CustomTask task : lockinTaskHandler.getTasks()) {
                task.updateStatus();
                Inventory tasksInv = this.tasksInvs.get(team.getKey());
                if (tasksInv == null) {
                    throw new IllegalArgumentException("Team '" + team.getKey() + "' does not have a valid task inventory.");
                }
                ItemStack taskItem = task.getDisplayItem();
                if (task.hasCompleted(team.getKey())) {
                    taskItem = Utils.setMaterial(taskItem, Material.GRAY_STAINED_GLASS_PANE);
                }
                tasksInv.addItem(taskItem);
            }
        }
    }

    public void openInventory(Player player) {
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
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getItem() == null) return;
        if (event.getItem().getItemMeta().displayName() == null) return;
        if (!event.getItem().getItemMeta().displayName().equals(this.compassDisplayName)) return;
        if (this.clickedPlayers.contains(event.getPlayer().getUniqueId())) return;
        if (Utils.isRightClick(event.getAction())) {
            this.updateTeamsInventory();
            this.openInventory(event.getPlayer());
        }
        else if (Utils.isLeftClick(event.getAction())) {
            this.setTarget(event.getPlayer(), event.getItem());
            this.clickedPlayers.add(event.getPlayer().getUniqueId());
            TaskUtils.runDelayedTask(this.plugin, () -> {
                clickedPlayers.remove(event.getPlayer().getUniqueId());
            }, 0.2f*20);
        }
    }

    public void onInventoryClickEvent(InventoryClickEvent event) {
        // Check that inventory name matches
        String invName = Utils.asString(event.getView().title());
        if (!invName.equals(this.getInvName())) return;

        // Prevent movement
        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)
            event.setCancelled(true);

        // Check that slot is valid
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= this.getMaxSlots()) return;

        event.setCancelled(true);

        if (this.isActive) return;

        // Change team of player
        Player player = (Player) event.getWhoClicked();
        this.teamHandler.removePlayerIfExists(player);
        this.teamHandler.addPlayer(player, slot);
        this.updateTeamsInventory();
        player.updateInventory();
    }

    public void onInventoryDragEvent(InventoryDragEvent event) {
        // Check that inventory name matches
        String invName = Utils.asString(event.getView().title());
        if (!invName.equals(this.getInvName())) return;

        event.setCancelled(true);
    }

    //---------------------------------------------------------------------------------------------
    // Target methods
    //---------------------------------------------------------------------------------------------
    private void setTarget(Player player, ItemStack compass) {
        // Populate targets map if the UUID is new
        UUID uuid = player.getUniqueId();
        if (!this.targets.containsKey(uuid)) this.targets.put(uuid, null);

        // Get all online player UUIDs, excluding the current player
        List<UUID> allUUIDsInGame = new ArrayList<>(this.teamHandler.getAllOnlineUUIDs());
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
    private ItemStack constructTeamItem(int teamIndex, Team team) {
        ItemStack item = new ItemStack(team.getMaterial());
        List<Component> loreLines = new ArrayList<>();
        for (String playerName : team.getPlayerNames()) {
            loreLines.add(Component.text(" " + playerName, NamedTextColor.DARK_AQUA));
        }
        loreLines.add(Component.text(team.getNumPlayers() + " players", NamedTextColor.DARK_PURPLE));

        item = ItemStackUtils.setDisplayName(item, Component.text(team.getKey(), NamedTextColor.AQUA));
        item = ItemStackUtils.setLore(item, loreLines);
        return item;
    }
}
