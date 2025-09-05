package me.frp.rideoperate.commands;

import me.frp.rideoperate.RideOperate;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class WebserverCommand implements CommandExecutor {

    private final RideOperate plugin;

    public WebserverCommand(RideOperate plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        // Check if the player has the required permission to use the command
        if (!player.hasPermission("rideoperate.weblink")) {
            player.sendMessage("You don't have permission to use this command!");
            return true;
        }

        if (args.length != 0) {
            player.sendMessage("Usage: /panellink");
            return true;
        }

        // Get the server IP and port from the configuration
        String serverIP;
        try {
            serverIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            player.sendMessage("Unable to retrieve server IP address.");
            return true;
        }

        int serverPort = plugin.getConfig().getInt("webserver.port", 8080); // Standaard poort voor de webserver

        // Construct the URL based on server IP and port
        String webServerURL = "http://" + serverIP + ":" + serverPort;

        // Create a clickable message
        TextComponent message = new TextComponent("Click here to open the web interface: ");
        TextComponent clickableURL = new TextComponent(webServerURL);
        clickableURL.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, webServerURL));
        message.addExtra(clickableURL);

        // Send the clickable message to the player
        player.spigot().sendMessage(message);

        return true;
    }
}
