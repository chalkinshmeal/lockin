package chalkinshmeal.lockin.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.game.GameHandler;
import chalkinshmeal.mc_plugin_lib.commands.command.BaseCommand;
import chalkinshmeal.mc_plugin_lib.commands.handler.CommandHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class StopCommand extends BaseCommand {
    private final GameHandler gameHandler;

    // Constructor
    public StopCommand(JavaPlugin plugin, CommandHandler cmdHandler, GameHandler gameHandler) {
        super("stop");
        this.setPlayerRequired(false);
        this.setHelpMsg(Component.text()
            .append(Component.text(this.getName() + ": ", NamedTextColor.GOLD))
            .append(Component.text("Stops a lockin game", NamedTextColor.WHITE))
            .build());
        
        this.gameHandler = gameHandler;
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if (!this.gameHandler.isActive()) {
            sender.sendMessage(
                Component.text("No ", NamedTextColor.GRAY)
                    .append(Component.text("lockin ", NamedTextColor.GOLD))
                    .append(Component.text("game in progress", NamedTextColor.GRAY)));
            return;
        }
        this.gameHandler.end();
    }
}
