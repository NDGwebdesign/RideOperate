package me.frp.rideoperate.api;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public class TcpApiServer {

    private ServerSocket serverSocket;
    private final int port;
    private final String apiKey = "mijnsupergeheimeapikey123";
    private final File panelFile;

    public TcpApiServer(int port, File panelFile) {
        this.port = port;
        this.panelFile = panelFile;
    }

    public void start() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                Bukkit.getLogger().info("[RideOperate] TCP API Server started on port " + port);

                while (!serverSocket.isClosed()) {
                    Socket clientSocket = serverSocket.accept();
                    handleClient(clientSocket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleClient(Socket clientSocket) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

                String key = reader.readLine();
                if (!apiKey.equals(key)) {
                    writer.write("ERROR: Invalid API key\n");
                    writer.flush();
                    clientSocket.close();
                    return;
                }

                String command = reader.readLine(); // bijv. GET_PANELS, GET_PANEL:<name>, EXECUTE:<panel>:<action>
                if (command == null) command = "";

                if (command.equals("GET_PANELS")) {
                    Map<String, Map<String, String>> panels = loadPanels();
                    for (String name : panels.keySet()) {
                        writer.write(name + "\n");
                    }
                } else if (command.startsWith("GET_PANEL:")) {
                    String panelName = command.split(":")[1];
                    Map<String, Map<String, String>> panels = loadPanels();
                    if (panels.containsKey(panelName)) {
                        Map<String, String> actions = panels.get(panelName);
                        for (String action : actions.keySet()) {
                            writer.write(action + ":" + actions.get(action) + "\n");
                        }
                    } else {
                        writer.write("ERROR: Panel not found\n");
                    }
                } else if (command.startsWith("EXECUTE:")) {
                    String[] parts = command.split(":");
                    if (parts.length == 3) {
                        String panel = parts[1];
                        String action = parts[2];
                        Map<String, Map<String, String>> panels = loadPanels();
                        if (panels.containsKey(panel) && panels.get(panel).containsKey(action)) {
                            String cmd = panels.get(panel).get(action);
                            Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("RideOperate"),
                                    () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
                            writer.write("OK\n");
                        } else {
                            writer.write("ERROR: Action not found\n");
                        }
                    }
                } else {
                    writer.write("ERROR: Unknown command\n");
                }

                writer.flush();
                clientSocket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private Map<String, Map<String, String>> loadPanels() {
        Map<String, Map<String, String>> panels = new HashMap<>();
        FileConfiguration config = YamlConfiguration.loadConfiguration(panelFile);
        if (config.contains("panels")) {
            for (String panelName : config.getConfigurationSection("panels").getKeys(false)) {
                Map<String, String> actions = new HashMap<>();
                for (String action : config.getConfigurationSection("panels." + panelName + ".Commands").getKeys(false)) {
                    actions.put(action, config.getString("panels." + panelName + ".Commands." + action));
                }
                panels.put(panelName, actions);
            }
        }
        return panels;
    }

    public void stop() {
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
