package me.frp.rideoperate.commands.subcommands;

import me.frp.rideoperate.RideOperate;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class SubAddCommand {

    private final RideOperate plugin;

    public SubAddCommand(RideOperate plugin) {
        this.plugin = plugin;
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("Usage: /rp addcommand <panelname> <buttonname> <command (without the /)>");
            return false;
        }

        String panelName = args[1];
        String buttonName = args[2];
        StringBuilder commandBuilder = new StringBuilder();

        for (int i = 3; i < args.length; i++) {
            commandBuilder.append(args[i]);
            if (i < args.length - 1) {
                commandBuilder.append(" ");
            }
        }

        String commandToAdd = commandBuilder.toString();

        File panelFile = new File(plugin.getDataFolder(), "panel.yml");
        if (!panelFile.exists()) {
            sender.sendMessage("The panel.yml file does not exist!");
            return false;
        }

        FileConfiguration panelConfig = YamlConfiguration.loadConfiguration(panelFile);

        if (!panelConfig.contains("panels." + panelName)) {
            sender.sendMessage("The panel '" + panelName + "' does not exist!");
            return false;
        }

        if (!panelConfig.contains("panels." + panelName + ".Commands." + buttonName)) {
            sender.sendMessage("The button '" + buttonName + "' does not exist in the panel '" + panelName + "'!");
            return false;
        }

        panelConfig.set("panels." + panelName + ".Commands." + buttonName, commandToAdd);

        try {
            panelConfig.save(panelFile);
            sender.sendMessage("Command successfully added to the button '" + buttonName + "' in panel '" + panelName + "'.");
            return true;
        } catch (IOException e) {
            sender.sendMessage("An error occurred while saving the panel.yml file!");
            e.printStackTrace();
            return false;
        }
    }
}
