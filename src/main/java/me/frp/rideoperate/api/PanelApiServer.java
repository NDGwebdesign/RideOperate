package me.frp.rideoperate.api;

import com.sun.net.httpserver.*;
import me.frp.rideoperate.RideOperate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class PanelApiServer {
    private final RideOperate plugin;
    private final HttpServer server;
    private final File panelFile;
    private final String apiKey;

    public PanelApiServer(RideOperate plugin) throws IOException {
        this.plugin = plugin;
        this.panelFile = new File(plugin.getDataFolder(), "panel.yml");
        this.apiKey = plugin.getConfig().getString("api-key");

        int port = plugin.getConfig().getInt("api-port", 50435);
        server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/api", new ApiHandler());
        server.setExecutor(Executors.newFixedThreadPool(10));
    }

    public void start() {
        server.start();
        Bukkit.getLogger().info("[RideOperate] Panel API server gestart op poort " + server.getAddress().getPort());
    }

    public void stop() {
        server.stop(0);
    }

    private class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> queryParams = parseQuery(exchange.getRequestURI().getQuery());

            String key = queryParams.get("key");
            if (key == null || !key.equals(apiKey)) {
                exchange.sendResponseHeaders(403, 0);
                exchange.getResponseBody().write("Invalid API key".getBytes());
                exchange.close();
                return;
            }

            String action = queryParams.get("action");
            if (action == null) action = "";

            switch (action) {
                case "getPanels":
                    sendFile(exchange, panelFile);
                    break;
                case "execute":
                    if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                        Map<String, String> postParams = parsePost(exchange);
                        String panel = postParams.get("panel");
                        String act = postParams.get("action");
                        executeCommand(panel, act);
                        writeJson(exchange, "{\"status\":\"ok\"}");
                    } else {
                        writeJson(exchange, "{\"error\":\"Use POST method\"}");
                    }
                    break;
                default:
                    writeJson(exchange, "{\"error\":\"Unknown action\"}");
            }
        }

        private void sendFile(HttpExchange exchange, File file) throws IOException {
            if (!file.exists()) {
                writeJson(exchange, "{\"error\":\"File not found\"}");
                return;
            }
            exchange.getResponseHeaders().add("Content-Type", "text/yaml");
            byte[] data = Files.readAllBytes(file.toPath());
            exchange.sendResponseHeaders(200, data.length);
            OutputStream os = exchange.getResponseBody();
            os.write(data);
            os.close();
        }

        private void executeCommand(String panel, String action) {
            if (panel == null || action == null) return;

            FileConfiguration config = YamlConfiguration.loadConfiguration(panelFile);
            if (!config.contains("panels." + panel + ".Commands." + action)) return;

            String cmd = config.getString("panels." + panel + ".Commands." + action);
            if (cmd != null && !cmd.isEmpty()) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)
                );
            }
        }

        private void writeJson(HttpExchange exchange, String json) throws IOException {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, json.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(json.getBytes());
            os.close();
        }

        private Map<String, String> parseQuery(String query) throws UnsupportedEncodingException {
            Map<String, String> map = new HashMap<>();
            if (query == null) return map;
            String[] params = query.split("&");
            for (String param : params) {
                String[] pair = param.split("=", 2);
                if (pair.length == 2) {
                    map.put(URLDecoder.decode(pair[0], "UTF-8"), URLDecoder.decode(pair[1], "UTF-8"));
                }
            }
            return map;
        }

        private Map<String, String> parsePost(HttpExchange exchange) throws IOException {
            InputStream is = exchange.getRequestBody();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            Map<String, String> map = new HashMap<>();
            if (line != null) {
                String[] params = line.split("&");
                for (String param : params) {
                    String[] pair = param.split("=", 2);
                    if (pair.length == 2) {
                        map.put(URLDecoder.decode(pair[0], "UTF-8"), URLDecoder.decode(pair[1], "UTF-8"));
                    }
                }
            }
            return map;
        }
    }
}
