package chalkinshmeal.lockin.artifacts.compass;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
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

import chalkinshmeal.lockin.artifacts.tasks.LockinTask;
import chalkinshmeal.lockin.artifacts.tasks.LockinTaskHandler;
import chalkinshmeal.lockin.artifacts.team.LockinTeamHandler;
import chalkinshmeal.lockin.data.ConfigHandler;
import chalkinshmeal.lockin.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class LockinCompass {
    private final LockinTeamHandler lockinTeamHandler;
    private final Inventory teamsInv;
    private final Map<String, Inventory> tasksInvs; 
    private final int tasksPerTier;
    private boolean isActive;
    private boolean debug = false;

    private final Component compassDisplayName = Component.text(
        "Lockin", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false);
    private final String teamsInvName = "Lockin Teams";
    private final String tasksInvName = "Lockin Tasks";

    //---------------------------------------------------------------------------------------------
    // Constructor
    //---------------------------------------------------------------------------------------------
    public LockinCompass(ConfigHandler configHandler, LockinTeamHandler lockinTeamHandler) {
        this.lockinTeamHandler = lockinTeamHandler;
        this.tasksPerTier = Utils.getHighestMultiple((int) configHandler.getInt("tasksPerTier", 27), 9);
        this.teamsInv = Bukkit.createInventory(null, 9, Component.text(this.teamsInvName, NamedTextColor.LIGHT_PURPLE));
        this.tasksInvs = new HashMap<>();
        for (String teamName : this.lockinTeamHandler.getTeamNames()) {
            Inventory newInv = Bukkit.createInventory(null, this.tasksPerTier, Component.text(this.tasksInvName, NamedTextColor.LIGHT_PURPLE));
            this.tasksInvs.put(teamName, newInv);
        }
        this.isActive = false;

        this.updateTeamsInventory();
        this.updateTasksInventory(null);
    }

    //---------------------------------------------------------------------------------------------
    // Accessor/Mutator methods 
    //---------------------------------------------------------------------------------------------
    public int getMaxTeams() { return this.lockinTeamHandler.getNumTeams(); }
    public String getInvName() { return (this.isActive) ? Utils.stripColor(this.tasksInvName) : Utils.stripColor(this.teamsInvName); }
    public int getMaxSlots() {return (this.isActive) ? this.tasksPerTier : this.lockinTeamHandler.getNumTeams(); }
    public Inventory getTaskInv(Player player) { return this.tasksInvs.get(this.lockinTeamHandler.getTeamName(player)); }
    public void SetIsActive(boolean isActive) { this.isActive = isActive; }

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
        for (LockinTask task : lockinTaskHandler.getTasks()) {
            task.setLore();
            for (String teamName : this.tasksInvs.keySet()) {
                if (debug) Bukkit.getServer().getLogger().info("[LockinCompass::updateTasksInventory]     Task: " + task.getName() + ", Team: " + teamName);
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
        if (!Utils.isRightClick(event.getAction())) return;
        if (event.getItem() == null) return;
        if (event.getItem().getItemMeta().displayName() == null) return;
        if (!event.getItem().getItemMeta().displayName().equals(this.compassDisplayName)) return;

        this.updateTeamsInventory();
        this.openInventory(event.getPlayer());
        if (debug) Bukkit.getServer().getLogger().info("[LockinCompass::onPlayerInteractEvent] --- Get Team (After) ---");
        if (debug) this.lockinTeamHandler.getTeamName(event.getPlayer());
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
