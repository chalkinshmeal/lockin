package chalkinshmeal.lockin.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.compass.LockinCompass;
import chalkinshmeal.lockin.utils.cmdframework.command.BaseCommand;
import chalkinshmeal.lockin.utils.cmdframework.handler.CommandHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CompassCommand extends BaseCommand {
    private final LockinCompass lockinCompass;

    // Constructor
    public CompassCommand(JavaPlugin plugin, CommandHandler cmdHandler, LockinCompass lockinCompass) {
        super("compass");
        this.setPlayerRequired(false);
        this.setHelpMsg(Component.text()
            .append(Component.text(this.getName() + ": ", NamedTextColor.GOLD))
            .append(Component.text("Gives lockin compass", NamedTextColor.WHITE))
            .build());
        
            this.lockinCompass = lockinCompass;
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;

        this.lockinCompass.giveCompass(player);
    }
}
