package chalkinshmeal.lockout.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockout.artifacts.compass.LockoutCompass;
import chalkinshmeal.lockout.utils.cmdframework.command.BaseCommand;
import chalkinshmeal.lockout.utils.cmdframework.handler.CommandHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CompassCommand extends BaseCommand {
    private final LockoutCompass lockoutCompass;

    // Constructor
    public CompassCommand(JavaPlugin plugin, CommandHandler cmdHandler, LockoutCompass lockoutCompass) {
        super("compass");
        this.setPlayerRequired(false);
        this.setHelpMsg(Component.text()
            .append(Component.text(this.getName() + ": ", NamedTextColor.GOLD))
            .append(Component.text("Gives Lockout compass", NamedTextColor.WHITE))
            .build());
        
            this.lockoutCompass = lockoutCompass;
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;

        this.lockoutCompass.giveCompass(player);
    }
}
