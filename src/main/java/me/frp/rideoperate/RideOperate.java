package me.frp.rideoperate;

import me.frp.rideoperate.api.TcpApiServer;
import me.frp.rideoperate.commands.*;
import me.frp.rideoperate.commands.subcommands.DeleteButton;
import me.frp.rideoperate.commands.subcommands.SubHelp;
import me.frp.rideoperate.listener.LiveCam;
import me.frp.rideoperate.listener.MassagesToggle;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandExecutor;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;


public final class RideOperate extends JavaPlugin implements CommandExecutor {

    private static RideOperate instance;

    private MassagesToggle massagesToggle;

    private TcpApiServer tcpApiServer;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getCommand("createpanel").setExecutor(new CreatePanel(this));
        getCommand("rpreload").setExecutor(new ReloadCommand(this));
        new MainCommand(this);
        new OpenPanel(this);
        new DeletePanel(this);
        new Help(this);
        new Panels(this);
        new info(this);
        new SubHelp(this);
        new AddCommand(this);
        getCommand("rpsetlore").setExecutor(new Setlore( this));
        new CreateCam(this);
        new DeleteCam(this);
        new ClickSign(this);
        new LiveCam(this);
        new changeItem(this);
        new AddButton(this);
        new DeleteButton(this);

        File panelFile = new File(getDataFolder(), "panel.yml");
        int apiPort = getConfig().getInt("api-port", 5555); // Read from config.yml
        tcpApiServer = new TcpApiServer(apiPort,this, panelFile);
        tcpApiServer.start();
        this.getCommand("genapikey").setExecutor(new GenAPIKey(this));

        getLogger().info("Ride Operate is enabled");

        // Load .yml files
        loadConfigFile("config.yml");
        loadConfigFile("panel.yml");
        loadConfigFile("cams.yml");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Ride Operate is disabled");

        if (tcpApiServer != null) tcpApiServer.stop();

        massagesToggle = new MassagesToggle(this);

        // Voorbeeld van hoe je MassagesToggle kunt gebruiken
        if (massagesToggle.areMessagesEnabled()) {
            getLogger().info(massagesToggle.getNoPermissionMessage());
        }


    }

    public static RideOperate getInstance() {
        return instance;
    }

    private void loadConfigFile(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) {
            saveResource(fileName, false);
            getLogger().info(fileName + " created and loaded.");
        } else {
            getLogger().info(fileName + " loaded.");
        }
    }


    private String getServerIP() {
        // Implement logic to get the server IP address
        // For example, fetch from configuration or detect dynamically
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "localhost"; // Fallback to localhost if IP detection fails
        }
    }

}
