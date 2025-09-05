package me.frp.rideoperate.commands.subcommands;

import me.frp.rideoperate.RideOperate;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SubInfo implements CommandExecutor {

    private final RideOperate plugin;

    public SubInfo(RideOperate plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return execute(sender, args);
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        // Check if the player has the required permission to use the command
        if (!player.hasPermission("rideoperate.info")) {
            player.sendMessage("You don't have permission to use this command!");
            return true;
        }

        player.sendMessage(ChatColor.BLUE + "-=*Ride Operate*=-");
        player.sendMessage("==================");
        player.sendMessage("Version: " + plugin.getDescription().getVersion());
        player.sendMessage("Author: Friendspark, NDG-webdesign");
        player.sendMessage("==================");

        return true;
    }
}
