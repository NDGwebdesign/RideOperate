package me.frp.rideoperate.web;
import com.sun.net.httpserver.*;
import me.frp.rideoperate.RideOperate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.net.URLDecoder;
import java.net.URLEncoder;
public class HostSite {
    private final HttpServer server;
    private final RideOperate plugin;
    private final String url;
    private Map<String, Map<String, String>> panels;
    private final Path panelFilePath;
    private long lastModifiedTime;
    // Modified constructor to accept useSSL parameter
    public HostSite(RideOperate plugin, int port, boolean useSSL) throws Exception {
        this.plugin = plugin;
        this.panelFilePath = Paths.get(plugin.getDataFolder().getPath(), "panel.yml");
        this.panels = loadPanels();
        this.lastModifiedTime = panelFilePath.toFile().lastModified();
        String serverIp = plugin.getServer().getIp();
        if (serverIp == null || serverIp.isEmpty()) {
            serverIp = "localhost";
        }

        // Update URL protocol based on SSL setting
        String protocol = useSSL ? "https" : "http";
        this.url = protocol + "://" + serverIp + ":" + port;

        // Create appropriate server type based on SSL setting
        if (useSSL) {
            // Create HTTPS server using system's default SSL context
            SSLContext sslContext = SSLContext.getDefault();
            HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(port), 0);

            // Configure SSL parameters
            SSLParameters sslParams = sslContext.getDefaultSSLParameters();
            HttpsConfigurator configurator = new HttpsConfigurator(sslContext) {
                @Override
                public void configure(HttpsParameters params) {
                    params.setSSLParameters(sslParams);
                }
            };

            httpsServer.setHttpsConfigurator(configurator);
            server = httpsServer;
            Bukkit.getLogger().info("[RideOperate] Using system SSL configuration for HTTPS");
        } else {
            // Create regular HTTP server
            server = HttpServer.create(new InetSocketAddress(port), 0);
        }

        // Contexten instellen
        server.createContext("/", new HomeHandler());
        server.createContext("/panel", new PanelHandler());
        server.createContext("/create-panel", new CreatePanelHandler());
        server.createContext("/create-panel-submit", new CreatePanelSubmitHandler());
        server.createContext("/execute", new ExecuteHandler());
        server.createContext("/edit-panel", new EditPanelHandler());
        server.createContext("/save-panel", new SavePanelHandler());
        server.createContext("/cameras/", new CameraHandler());
        startPanelWatcher();
        server.setExecutor(Executors.newFixedThreadPool(10));
        Bukkit.getLogger().info("[RideOperate] Webserver gestart op " + url);
    }
    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }
    private String getCssContent(String fileName) {
        try {
            Path cssPath = Paths.get(plugin.getDataFolder().getPath(), "css", fileName);
            return new String(Files.readAllBytes(cssPath));
        } catch (IOException e) {
            e.printStackTrace();
            return ""; // Return empty string if file not found or error occurs
        }
    }
    private Map<String, Map<String, String>> loadPanels() {
        Map<String, Map<String, String>> panels = new HashMap<>();
        FileConfiguration config = YamlConfiguration.loadConfiguration(panelFilePath.toFile());

        if (config.contains("panels")) {
            for (String panelName : config.getConfigurationSection("panels").getKeys(false)) {
                Map<String, String> commands = new HashMap<>();
                for (String action : config.getConfigurationSection("panels." + panelName + ".Commands").getKeys(false)) {
                    commands.put(action, config.getString("panels." + panelName + ".Commands." + action));
                }
                panels.put(panelName, commands);
            }
        }

        return panels;
    }
    // Helper method to parse POST parameters - centralized to avoid duplication
    private Map<String, String> parsePostParameters(HttpExchange exchange) throws IOException {
        Map<String, String> parameters = new HashMap<>();
        String[] rawParams = new String(exchange.getRequestBody().readAllBytes()).split("&");
        for (String param : rawParams) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                String key = URLDecoder.decode(keyValue[0], "UTF-8");
                String value = URLDecoder.decode(keyValue[1], "UTF-8");
                parameters.put(key, value);
            }
        }
        return parameters;
    }
    private List<String> getCamerasForPanel(String panelName) {
        List<String> panelCameras = new ArrayList<>();
        File camsFile = new File(plugin.getDataFolder(), "cams.yml");
        FileConfiguration camsConfig = YamlConfiguration.loadConfiguration(camsFile);

        if (camsConfig.contains("cams")) {
            for (String camName : camsConfig.getConfigurationSection("cams").getKeys(false)) {
                String camPanel = camsConfig.getString("cams." + camName + ".panel");
                if (camPanel != null && camPanel.equals(panelName)) {
                    panelCameras.add(camName);
                }
            }
        }
        return panelCameras;
    }
    private void startPanelWatcher() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            long currentModifiedTime = panelFilePath.toFile().lastModified();
            if (currentModifiedTime != lastModifiedTime) {
                lastModifiedTime = currentModifiedTime;
                panels = loadPanels();
                Bukkit.getLogger().info("panel.yml updated, reloading panels.");
            }
        }, 0, 10, TimeUnit.SECONDS);
    }
    private class HomeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String response = generateHomePageHTML();
                exchange.getResponseHeaders().add("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("Error handling home request: " + e.getMessage());
                exchange.sendResponseHeaders(500, 0);
            }
        }
        private String generateHomePageHTML() {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><title>RideOperate | Panels</title>");
            html.append("<style>");
            html.append(getCssContent("home.css"));
            html.append("</style>");
            html.append("</head><body>");
            html.append("<nav>");
            html.append("<a href=\"/\" class=\"home-link\">Home</a>");
            html.append("<a href=\"/create-panel\" class=\"create-panel-link\">Create New Panel</a>");
            html.append("</nav>");
            html.append("<main><div class='panelContainer'>");
            if (panels.isEmpty()) {
                html.append("<p>Er zijn nog geen panels beschikbaar.</p>");
            } else {
                for (String panelName : panels.keySet()) {
                    html.append("<div class='panel-item'>");
                    html.append("<a href='/panel?name=").append(panelName).append("'>").append(panelName).append("</a>");
                    html.append("</div>");
                }
            }
            html.append("</div></main></body></html>");
            return html.toString();
        }
    }
    private class PanelHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                    String panelName = getPanelNameFromQuery(exchange.getRequestURI().getQuery());
                    if (panelName != null && panels.containsKey(panelName)) {
                        String response = generatePanelControlHTML(panelName);
                        exchange.getResponseHeaders().add("Content-Type", "text/html");
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response.getBytes());
                        }
                    } else {
                        exchange.sendResponseHeaders(404, 0);
                    }
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("Error handling panel request: " + e.getMessage());
                exchange.sendResponseHeaders(500, 0);
            }
        }
        private String getPanelNameFromQuery(String query) {
            if (query != null && query.startsWith("name=")) {
                return query.substring(5);
            }
            return null;
        }
        private String generatePanelControlHTML(String panelName) {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><title>RideOperate | ").append(panelName).append("</title>");
            html.append("<style>");
            html.append(getCssContent("panel.css"));
            html.append("</style>");
            html.append("<script>");
            html.append("function refreshCameraFeeds() {");
            html.append("  const images = document.getElementsByClassName('camera-feed');");
            html.append("  for(let img of images) {");
            html.append("    img.src = img.src.split('?')[0] + '?t=' + new Date().getTime();");
            html.append("  }");
            html.append("}");
            html.append("setInterval(refreshCameraFeeds, 1000);");
            html.append("</script>");
            html.append("</head><body>");
            html.append("<nav>");
            html.append("<a href=\"/\" class=\"home-link\">Home</a>");
            html.append("<a href=\"/create-panel\" class=\"create-panel-link\">Create New Panel</a>");
            html.append("<a href=\"/edit-panel?name=").append(panelName).append("\" class=\"edit-panel-link\">Edit</a>");
            html.append("</nav>");
            // Control panel form
            html.append("<form method='POST' action='/execute'>");
            html.append("<input type='hidden' name='panel' value='").append(panelName).append("'>");
            html.append("<div class='panel' id='panel'>");
            for (String action : panels.get(panelName).keySet()) {
                html.append("<button type='submit' name='action' value='").append(action)
                        .append("' class='action-button'>").append(action).append("</button>");
            }
            html.append("</div></form>");
            // Camera feeds section
            html.append("<div class='camera-container'>");
            List<String> panelCameras = getCamerasForPanel(panelName);
            for (String camName : panelCameras) {
                File photoFile = new File(plugin.getDataFolder(), "cams/" + camName + "/photo.png");
                if (photoFile.exists()) {
                    html.append("<div class='camera-view'>");
                    html.append("<h3>").append(camName).append("</h3>");
                    html.append("<img class='camera-feed' src='/cameras/").append(camName)
                            .append("/photo.png' alt='Camera ").append(camName).append("'>");
                    html.append("</div>");
                }
            }
            html.append("</div>");

            html.append("</body></html>");
            return html.toString();
        }
        private List<String> getAllCameras() {
            List<String> cameraList = new ArrayList<>();
            File camsFile = new File(plugin.getDataFolder(), "cams.yml");
            FileConfiguration camsConfig = YamlConfiguration.loadConfiguration(camsFile);

            if (camsConfig.contains("cams")) {
                for (String camName : camsConfig.getConfigurationSection("cams").getKeys(false)) {
                    cameraList.add(camName);
                }
            }
            return cameraList;
        }
    }
    private class CameraHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String path = exchange.getRequestURI().getPath();
                String[] parts = path.split("/");
                if (parts.length < 4) {
                    exchange.sendResponseHeaders(404, -1);
                    return;
                }

                File photoFile = new File(plugin.getDataFolder(), "cams/" + parts[2] + "/photo.png");
                if (photoFile.exists()) {
                    exchange.getResponseHeaders().add("Content-Type", "image/png");
                    exchange.sendResponseHeaders(200, photoFile.length());
                    try (OutputStream os = exchange.getResponseBody();
                         FileInputStream fis = new FileInputStream(photoFile)) {
                        byte[] buffer = new byte[4096];
                        int count;
                        while ((count = fis.read(buffer)) != -1) {
                            os.write(buffer, 0, count);
                        }
                    }
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("Error handling camera request: " + e.getMessage());
                exchange.sendResponseHeaders(500, -1);
            }
        }
    }
    private class CreatePanelHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                    String response = generateCreatePanelHTML();
                    exchange.getResponseHeaders().add("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("Error handling create panel request: " + e.getMessage());
                exchange.sendResponseHeaders(500, 0);
            }
        }
        private String generateCreatePanelHTML() {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><title>RideOperate | Create</title>");
            html.append("<style>");
            html.append(getCssContent("create.css"));
            html.append("</style>");
            html.append("</head><body>");
            html.append("<nav>");
            html.append("<a href=\"/\" class=\"home-link\">Home</a>");
            html.append("<a href=\"/create-panel\" class=\"create-panel-link\">Create New Panel</a>");
            html.append("</nav>");
            html.append("<main>");
            html.append("<h1>Create a New Panel</h1>");
            html.append("<form method='POST' action='/create-panel-submit'>");
            html.append("<label for='panelName'>Panel Name:</label>");
            html.append("<input type='text' id='panelName' name='panelName' required>");
            html.append("<input type='submit' value='Create Panel'>");
            html.append("</form>");
            html.append("</main>");
            html.append("</body></html>");
            return html.toString();
        }
    }
    private class CreatePanelSubmitHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                Map<String, String> params = parsePostParameters(exchange);
                String panelName = params.get("panelName");
                if (panelName != null && !panelName.isEmpty()) {
                    createPanel(panelName);
                    exchange.getResponseHeaders().add("Location", "/");
                    exchange.sendResponseHeaders(302, -1);
                } else {
                    exchange.sendResponseHeaders(400, 0);
                }
            }
        }
        private Map<String, String> parsePostParameters(HttpExchange exchange) throws IOException {
            Map<String, String> parameters = new HashMap<>();
            String[] rawParams = new String(exchange.getRequestBody().readAllBytes()).split("&");
            for (String param : rawParams) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    String key = URLDecoder.decode(keyValue[0], "UTF-8");
                    String value = URLDecoder.decode(keyValue[1], "UTF-8");
                    parameters.put(key, value);
                }
            }
            return parameters;
        }
        private void createPanel(String panelName) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(panelFilePath.toFile());
            if (!config.contains("panels." + panelName)) {
                config.set("panels." + panelName + ".Power.material", "REDSTONE_TORCH");
                config.set("panels." + panelName + ".Power.lore", "This is the power button!");
                config.set("panels." + panelName + ".Gates.material", "OAK_FENCE_GATE");
                config.set("panels." + panelName + ".Gates.lore", "This is the gates button!");
                config.set("panels." + panelName + ".Dispatch.material", "MINECART");
                config.set("panels." + panelName + ".Dispatch.lore", "This is the dispatch button!");
                config.set("panels." + panelName + ".safety-bar.material", "IRON_TRAPDOOR");
                config.set("panels." + panelName + ".safety-bar.lore", "This is the safety-bar button!");
                config.set("panels." + panelName + ".status.material", "IRON_TRAPDOOR");
                config.set("panels." + panelName + ".status.lore", "This is the status button!");
                config.set("panels." + panelName + ".Commands.Power", "/power_command");
                config.set("panels." + panelName + ".Commands.Gates", "/gates_command");
                config.set("panels." + panelName + ".Commands.Dispatch", "/dispatch_command");
                config.set("panels." + panelName + ".Commands.safety-bar", "/safetybar_command");
                config.set("panels." + panelName + ".Commands.status", "/status_command");
                try {
                    config.save(panelFilePath.toFile());
                    panels = loadPanels();
                    Bukkit.getLogger().info("New panel " + panelName + " created successfully.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private class ExecuteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                Map<String, String> params = parsePostParameters(exchange);
                String panelName = params.get("panel");
                String action = params.get("action");
                if (panelName != null && action != null && panels.containsKey(panelName)) {
                    String command = panels.get(panelName).get(action);
                    if (command != null) {
                        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
                    }
                }
                exchange.getResponseHeaders().add("Location", "/panel?name=" + panelName);
                exchange.sendResponseHeaders(302, -1);
            }
        }
        private Map<String, String> parsePostParameters(HttpExchange exchange) throws IOException {
            Map<String, String> parameters = new HashMap<>();
            String[] rawParams = new String(exchange.getRequestBody().readAllBytes()).split("&");
            for (String param : rawParams) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    parameters.put(keyValue[0], keyValue[1]);
                }
            }
            return parameters;
        }
    }
    private class EditPanelHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                String panelName = getPanelNameFromQuery(exchange.getRequestURI().getQuery());
                if (panelName != null && panels.containsKey(panelName)) {
                    String response = generateEditPanelHTML(panelName);
                    exchange.getResponseHeaders().add("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                } else {
                    exchange.sendResponseHeaders(404, 0);
                }
            }
        }
        private String getPanelNameFromQuery(String query) {
            if (query != null && query.startsWith("name=")) {
                return query.substring(5);
            }
            return null;
        }
        private String generateEditPanelHTML(String panelName) {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><title>Edit Panel - ").append(panelName).append("</title>");
            html.append("<style>");
            html.append(getCssContent("edit.css"));
            html.append("</style>");
            html.append("</head><body>");
            html.append("<nav>");
            html.append("<a href=\"/\" class=\"home-link\">Home</a>");
            html.append("<a href=\"/create-panel\" class=\"create-panel-link\">Create New Panel</a>");
            html.append("<a href=\"/edit-panel?name=").append(panelName).append("\" class=\"edit-panel-link\">Edit</a>");
            html.append("<a href=\"/panel?name=").append(panelName).append("\" class=\"\">Back</a>");
            html.append("</nav>");
            html.append("<main>");
            html.append("<form method='POST' action='/save-panel'>");
            html.append("<input type='hidden' name='panel' value='").append(panelName).append("'>");
            for (Map.Entry<String, String> entry : panels.get(panelName).entrySet()) {
                String buttonName = entry.getKey();
                String command = entry.getValue();

                html.append("<div class='button-edit'>");
                html.append("<label for='").append(buttonName).append("_material'>").append(buttonName).append(" Material:</label>");
                html.append("<input type='text' name='").append(buttonName).append("_material' value='").append(getButtonMaterial(panelName, buttonName)).append("' required>");

                html.append("<label for='").append(buttonName).append("_lore'>").append(buttonName).append(" Lore:</label>");
                html.append("<input type='text' name='").append(buttonName).append("_lore' value='").append(getButtonLore(panelName, buttonName)).append("' required>");

                html.append("<label for='").append(buttonName).append("_command'>").append(buttonName).append(" Command:</label>");
                html.append("<input type='text' name='").append(buttonName).append("_command' value='").append(command).append("' required>");
                html.append("</div>");
            }
            html.append("<input type='submit' value='Save Changes'>");
            html.append("</form>");
            html.append("</main></body></html>");
            return html.toString();
        }
        private String getButtonMaterial(String panelName, String buttonName) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(panelFilePath.toFile());
            return config.getString("panels." + panelName + "." + buttonName + ".material", "");
        }
        private String getButtonLore(String panelName, String buttonName) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(panelFilePath.toFile());
            return config.getString("panels." + panelName + "." + buttonName + ".lore", "");
        }
    }
    private class SavePanelHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                Map<String, String> params = parsePostParameters(exchange);
                String panelName = params.get("panel");
                if (panelName != null && panels.containsKey(panelName)) {
                    savePanelChanges(panelName, params);
                    panels = loadPanels(); // Herlaad de panelen na het opslaan
                    exchange.getResponseHeaders().add("Location", "/panel?name=" + URLEncoder.encode(panelName, "UTF-8"));
                    exchange.sendResponseHeaders(302, -1); // Redirect naar de panel-pagina
                } else {
                    exchange.sendResponseHeaders(400, 0); // Ongeldig panel of fout bij het opslaan
                }
            }
        }
        private void savePanelChanges(String panelName, Map<String, String> params) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(panelFilePath.toFile());
            for (String key : params.keySet()) {
                if (key.endsWith("_material")) {
                    String buttonName = key.replace("_material", "");
                    config.set("panels." + panelName + "." + buttonName + ".material", params.get(key));
                } else if (key.endsWith("_lore")) {
                    String buttonName = key.replace("_lore", "");
                    config.set("panels." + panelName + "." + buttonName + ".lore", params.get(key));
                } else if (key.endsWith("_command")) {
                    String buttonName = key.replace("_command", "");
                    config.set("panels." + panelName + ".Commands." + buttonName, params.get(key));
                }
            }
            try {
                config.save(panelFilePath.toFile());
                Bukkit.getLogger().info("Panel " + panelName + " successfully updated.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        private Map<String, String> parsePostParameters(HttpExchange exchange) throws IOException {
            Map<String, String> parameters = new HashMap<>();
            String[] rawParams = new String(exchange.getRequestBody().readAllBytes()).split("&");
            for (String param : rawParams) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    String key = URLDecoder.decode(keyValue[0], "UTF-8");
                    String value = URLDecoder.decode(keyValue[1], "UTF-8");
                    parameters.put(key, value);
                }
            }
            return parameters;
        }
    }
}