package chalkinshmeal.lockout.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockout.utils.cmdframework.command.BaseCommand;
import chalkinshmeal.lockout.utils.cmdframework.command.ParentCommand;
import chalkinshmeal.lockout.utils.cmdframework.handler.CommandHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class HelpCommand extends BaseCommand {
    private final CommandHandler cmdHandler;

    // Constructor
    public HelpCommand(JavaPlugin plugin, CommandHandler cmdHandler) {
        super("help");
        this.setPlayerRequired(false);
        this.setHelpMsg(Component.text()
            .append(Component.text(this.getName() + ": ", NamedTextColor.GOLD))
            .append(Component.text("Shows help message", NamedTextColor.WHITE))
            .build());

        this.cmdHandler = cmdHandler;
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        sender.sendMessage(Component.text()
            .append(Component.text("--------- ", NamedTextColor.GOLD))
            .append(Component.text("Lockout Help ", NamedTextColor.WHITE))
            .append(Component.text("---------", NamedTextColor.GOLD))
            .build());

        for (BaseCommand cmd : this.cmdHandler.getSortedCommands()) {
            if (cmd instanceof ParentCommand) {
                for (BaseCommand child_cmd : ((ParentCommand) cmd).getSortedChildren()) {
                    if (child_cmd.getHelpMsg() != null)
                        sender.sendMessage(Component.text()
                            .append(Component.text("/" + cmd.getName() + " ", NamedTextColor.GOLD))
                            .append(child_cmd.getHelpMsg())
                            .build());
                }
            }
            else if (cmd.getHelpMsg() != null) {
                sender.sendMessage(Component.text()
                    .append(Component.text("/", NamedTextColor.GOLD))
                    .append(cmd.getHelpMsg())
                    .build());
            }
        }
    }
}
