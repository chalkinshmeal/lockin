package chalkinshmeal.lockout.utils.cmdframework.handler;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockout.utils.cmdframework.command.ArgCommand;
import chalkinshmeal.lockout.utils.cmdframework.command.BaseCommand;
import chalkinshmeal.lockout.utils.cmdframework.command.ParentCommand;

import java.util.*;

/**
 * Takes care of executing any registered command when it is being called
 */
public class CommandHandler implements CommandExecutor {
    private final JavaPlugin plugin;
    private final Set<BaseCommand> commands;
    private final CommandCompleter cmdCompleter;

    // Constructor
    public CommandHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.commands = new HashSet<>();
        this.cmdCompleter = new CommandCompleter(this);
    }

    // Register a command with plugin
    public void registerCommand(BaseCommand command) {
        commands.add(command);
        plugin.getCommand(command.getName()).setExecutor(this);
        plugin.getCommand(command.getName()).setTabCompleter(cmdCompleter);
    }

    /** Setters */
    public void setTabList(String cmdName, String argName, List<String> tabList) {
        ArgCommand cmd = (ArgCommand) this.getCommandByName(cmdName);
        if (cmd == null) {
            this.plugin.getLogger().warning("Could not find command of name '" + cmdName + "'. Cannot set tablist.");
            return;
        }
        cmd.setTabListOfArgument(argName, tabList);
    }

    public Set<BaseCommand> getCommands() { return commands; }
    public BaseCommand getCommandByName(String name) {
        for (BaseCommand cmd : this.commands) {
            if (cmd instanceof ParentCommand) {
                for (BaseCommand child_cmd : ((ParentCommand) cmd).getChildren()) {
                    if (child_cmd.getName() == name) return child_cmd;
                }
            }
            if (cmd.getName() == name) return cmd;
        }
        this.plugin.getLogger().warning("getCommandByName(): Could not find command '" + name + "'");
        return null;
    }
    public List<BaseCommand> getSortedCommands() {
        List<BaseCommand> commands = new ArrayList<>(this.getCommands());
        Collections.sort(commands, new Comparator<BaseCommand>() {
            public int compare(BaseCommand c1, BaseCommand c2) {
                return c1.getName().compareTo(c2.getName());
            }
        });
        return commands;
    }

    // Execute command given
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String cmdName = cmd.getName();

        // Find the command within our set that matches this command, and execute
        for (BaseCommand command : commands) {
            if (command.matchesAlias(cmdName)) {
                command.execute(sender, args);
                return true;
            }
        }
        return false;
    }
}
