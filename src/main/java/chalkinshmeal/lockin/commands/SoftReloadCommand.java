package chalkinshmeal.lockin.commands;

import org.bukkit.command.CommandSender;

import chalkinshmeal.lockin.Plugin;
import chalkinshmeal.mc_plugin_lib.commands.command.BaseCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class SoftReloadCommand extends BaseCommand {
    private final Plugin plugin;

    public SoftReloadCommand(Plugin plugin) {
        super("softreload");
        this.setPlayerRequired(false);
        this.setHelpMsg(Component.text()
            .append(Component.text(this.getName() + ": ", NamedTextColor.GOLD))
            .append(Component.text("Reloads any config files for this plugin", NamedTextColor.WHITE))
            .build());
        
        this.plugin = plugin;
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        this.plugin.reload();
    }
}
