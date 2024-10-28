package chalkinshmeal.lockout.artifacts.compass;

import java.util.List;

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

import chalkinshmeal.lockout.artifacts.tasks.LockoutTask;
import chalkinshmeal.lockout.artifacts.tasks.LockoutTaskHandler;
import chalkinshmeal.lockout.artifacts.team.LockoutTeamHandler;
import chalkinshmeal.lockout.data.ConfigHandler;
import chalkinshmeal.lockout.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class LockoutCompass {
    private final LockoutTeamHandler lockoutTeamHandler;
    private final Inventory teamsInv;
    private final Inventory tasksInv; 
    private final int taskCount;
    private boolean isActive;

    private final Component compassDisplayName = Component.text(
        "Lockout", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false);
    private final String teamsInvName = "Lockout Teams";
    private final String tasksInvName = "Lockout Tasks";

    //---------------------------------------------------------------------------------------------
    // Constructor
    //---------------------------------------------------------------------------------------------
    public LockoutCompass(ConfigHandler configHandler, LockoutTeamHandler lockoutTeamHandler) {
        this.lockoutTeamHandler = lockoutTeamHandler;
        this.taskCount = Utils.getHighestMultiple((int) configHandler.getInt("taskCount", 27), 9);
        this.teamsInv = Bukkit.createInventory(null, 9, Component.text(this.teamsInvName, NamedTextColor.LIGHT_PURPLE));
        this.tasksInv = Bukkit.createInventory(null, this.taskCount, Component.text(this.tasksInvName, NamedTextColor.LIGHT_PURPLE));
        this.isActive = false;

        this.updateTeamsInventory();
        this.updateTasksInventory(null);
    }

    //---------------------------------------------------------------------------------------------
    // Accessor/Mutator methods 
    //---------------------------------------------------------------------------------------------
    public int getMaxTeams() { return this.lockoutTeamHandler.getNumTeams(); }
    public String getInvName() { return (this.isActive) ? Utils.stripColor(this.tasksInvName) : Utils.stripColor(this.teamsInvName); }
    public void SetIsActive(boolean isActive) { this.isActive = isActive; }
    public int getMaxSlots() {return (this.isActive) ? this.taskCount : this.lockoutTeamHandler.getNumTeams(); }

    //---------------------------------------------------------------------------------------------
    // Inventory methods
    //---------------------------------------------------------------------------------------------
    public void updateTeamsInventory() {
        this.teamsInv.clear();
        for (int i = 0; i < this.lockoutTeamHandler.getNumTeams(); i++) {
            List<String> playerNames = this.lockoutTeamHandler.getPlayerNames(i);
            this.teamsInv.addItem(this.constructTeamItem(i, playerNames));
        }
    }

    public void updateTasksInventory(LockoutTaskHandler lockoutTaskHandler) {
        this.tasksInv.clear();
        if (lockoutTaskHandler == null) return;

        for (LockoutTask task : lockoutTaskHandler.GetTasks()) {
            this.tasksInv.addItem(task.getItem());
        }
    }

    public void openInventory(Player player) {
        if (this.isActive) player.openInventory(this.tasksInv);
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
        if (!Utils.isRightClick(event.getAction())) return;
        if (event.getItem() == null) return;
        if (event.getItem().getItemMeta().displayName() == null) return;
        if (!event.getItem().getItemMeta().displayName().equals(this.compassDisplayName)) return;

        this.updateTeamsInventory();
        this.openInventory(event.getPlayer());
    }

    public void onInventoryClickEvent(InventoryClickEvent event) {
        // Check that inventory name matches
        String invName = Utils.stripColor(event.getView().getOriginalTitle());
        if (!invName.equals(this.getInvName())) return;

        // Prevent movement
        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)
            event.setCancelled(true);

        // Check that slot is valid
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= this.getMaxSlots()) return;

        event.setCancelled(true);

        // Change team of player
        Player player = (Player) event.getWhoClicked();
        this.lockoutTeamHandler.removePlayer(player);
        this.lockoutTeamHandler.addPlayer(player, slot);
        this.updateTeamsInventory();
        player.updateInventory();
    }

    public void onInventoryDragEvent(InventoryDragEvent event) {
        // Check that inventory name matches
        String invName = Utils.stripColor(event.getView().getOriginalTitle());
        if (!invName.equals(this.getInvName())) return;

        event.setCancelled(true);
    }

    //---------------------------------------------------------------------------------------------
    // Utility methods
    //---------------------------------------------------------------------------------------------
    private ItemStack constructTeamItem(int teamIndex, List<String> playerNames) {
        ItemStack item = new ItemStack(this.lockoutTeamHandler.getTeamMaterials().get(teamIndex));
        item = Utils.setDisplayName(item, Component.text(this.lockoutTeamHandler.getTeamName(teamIndex), NamedTextColor.AQUA));
        item = Utils.addLore(item, Component.text(playerNames.size() + " players", NamedTextColor.DARK_PURPLE));
        for (String playerName : playerNames) {
            item = Utils.addLore(item, Component.text(" " + playerName, NamedTextColor.DARK_AQUA));
        }
        return item;
    }
}
