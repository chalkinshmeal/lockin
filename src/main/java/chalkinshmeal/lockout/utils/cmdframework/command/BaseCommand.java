package chalkinshmeal.lockout.utils.cmdframework.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.TextComponent;

import java.util.*;

/**
 * A command with no defined way of execution (not arguments or sub commands)
 */
@SuppressWarnings("deprecation")
public abstract class BaseCommand {
    public static final UUID CONSOLE_ID = UUID.randomUUID();
    private final String name;
    private final Set<String> aliases;
    private ParentCommand parent;
    private String permission;
    private boolean isPlayerRequired;
    private TextComponent helpMsg;

    public BaseCommand(String name) {
        this.name = name.toLowerCase();
        aliases = new HashSet<>();
        aliases.add(this.name);
    }

    /** Booleans */
    public boolean matchesAlias(String alias) { return aliases.contains(alias); }

    /** Getters */
    public String getName() { return name; }
    protected ParentCommand getParent() { return parent; }
    public boolean isPlayerRequired() { return isPlayerRequired; }
    public String getPermission() { return permission; }
    public TextComponent getHelpMsg() { return helpMsg; }
    public List<String> getTabList(String[] arguments) { return new LinkedList<>(); }
    protected UUID getSenderId(CommandSender sender) { return sender instanceof Player ? ((Player) sender).getUniqueId() : CONSOLE_ID; }
    public String getUsage() {
        if (getParent() != null) { return getParent().getParentUsage() + " " + getName(); }
        return ChatColor.RED + "/" + getName();
    }

    /** Setters */
    public BaseCommand setParent(ParentCommand parent) { this.parent = parent; return this; }
    public void setPlayerRequired(boolean playerRequired) { isPlayerRequired = playerRequired; }
    public void setPermission(String permission) { this.permission = permission; }
    public void addAlias(String alias) { aliases.add(alias.toLowerCase()); }
    public void setHelpMsg(TextComponent helpMsg) { this.helpMsg = helpMsg; }


    /** Usage */
    public void sendUsage(CommandSender sender) { sender.sendMessage(getUsage()); }

    /** Command */
    // Execute command if permissions + isPlayer conditions are met
    public void execute(CommandSender sender, String[] args) {
        if (isPlayerRequired() && !(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
            return;
        }
        if (getPermission() != null && !sender.hasPermission(getPermission())) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to run this command.");
            return;
        }
        onCommand(sender, args);
    }

    // Execute command
    protected abstract void onCommand(CommandSender sender, String[] args);
}
