package me.frp.rideoperate.listener;

import me.frp.rideoperate.RideOperate;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class LiveCam {
    private final RideOperate plugin;
    private final File cameraFile;
    private FileConfiguration cameraConfig;

    public LiveCam(RideOperate plugin) {
        this.plugin = plugin;
        this.cameraFile = new File(plugin.getDataFolder(), "cams.yml");
        loadConfig();
        startPhotoCaptureTask();
    }

    private void loadConfig() {
        cameraConfig = YamlConfiguration.loadConfiguration(cameraFile);
    }

    private void startPhotoCaptureTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    // Reload config occasionally to catch changes
                    loadConfig();

                    if (cameraConfig.getConfigurationSection("cams") != null) {
                        for (String camId : cameraConfig.getConfigurationSection("cams").getKeys(false)) {
                            try {
                                capturePhoto(camId);
                            } catch (Exception e) {
                                plugin.getLogger().warning("Error capturing photo for camera '" + camId + "': " + e.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().severe("Error in camera capture task: " + e.getMessage());
                }
            }
        }.runTaskTimer(plugin, 20L, 100L); // Changed from 40L to 100L (5 seconds) to reduce server load
    }


    private void capturePhoto(String cameraName) {
        String path = "cams." + cameraName;

        // Check if world name exists in config
        String worldName = cameraConfig.getString(path + ".world");
        if (worldName == null) {
            plugin.getLogger().warning("Camera '" + cameraName + "' has no world configured. Skipping photo capture.");
            return;
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("World '" + worldName + "' for camera '" + cameraName + "' does not exist. Skipping photo capture.");
            return;
        }

        // Continue with existing code...
        Location cameraLocation = new Location(
                world,
                cameraConfig.getDouble(path + ".x"),
                cameraConfig.getDouble(path + ".y"),
                cameraConfig.getDouble(path + ".z"),
                (float) cameraConfig.getDouble(path + ".yaw"),
                (float) cameraConfig.getDouble(path + ".pitch")
        );

        BufferedImage image = renderScene(cameraLocation);
        savePhoto(image, cameraName);
    }


    private BufferedImage renderScene(Location cameraLocation) {
        int width = 320;  // Reduced from 640
        int height = 180; // Reduced from 360
        double fov = 90;
        double maxDistance = 30; // Reduced from 50

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Vector direction = cameraLocation.getDirection();
        Vector right = direction.clone().crossProduct(new Vector(0, 1, 0)).normalize();
        Vector up = right.clone().crossProduct(direction).normalize();

        double aspectRatio = (double) width / height;
        double tanFov = Math.tan(Math.toRadians(fov / 2));

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double u = (2 * (x + 0.5) / width - 1) * aspectRatio * tanFov;
                double v = (1 - 2 * (y + 0.5) / height) * tanFov;

                Vector rayDir = direction.clone()
                        .add(right.clone().multiply(u))
                        .add(up.clone().multiply(v))
                        .normalize();

                int color = traceRay(cameraLocation, rayDir, maxDistance);
                image.setRGB(x, y, color);
            }
        }
        return image;
    }

    private void savePhoto(BufferedImage image, String cameraName) {
        File photoDir = new File(plugin.getDataFolder(), "cams/" + cameraName);
        photoDir.mkdirs();

        File photoFile = new File(photoDir, "photo.png");
        try {
            ImageIO.write(image, "png", photoFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save photo for camera " + cameraName + ": " + e.getMessage());
        }
    }

    private int traceRay(Location start, Vector direction, double maxDistance) {
        World world = start.getWorld();
        if (world == null) return 0x000000;

        // First check for blocks to handle occlusion
        RayTraceResult blockTrace = world.rayTraceBlocks(start, direction, maxDistance);
        double blockDistance = blockTrace != null ? blockTrace.getHitPosition().distance(start.toVector()) : maxDistance;

        // Then check for entities
        RayTraceResult entityTrace = world.rayTraceEntities(start, direction, maxDistance);
        if (entityTrace != null && entityTrace.getHitEntity() != null) {
            double entityDistance = entityTrace.getHitPosition().distance(start.toVector());

            // Only render entity if it's closer than any blocking block
            if (entityDistance < blockDistance) {
                return applyDepthShading(getEntityColor(entityTrace.getHitEntity()), entityDistance, maxDistance);
            }
        }

        // Render block if we hit one
        if (blockTrace != null && blockTrace.getHitBlock() != null) {
            return applyDepthShading(BlockColor.getBlockColor(blockTrace.getHitBlock().getType()),
                    blockTrace.getHitPosition().distance(start.toVector()), maxDistance);
        }

        return 0x000000; // Sky/void color
    }


    private int applyDepthShading(int color, double distance, double maxDistance) {
        double depthFactor = 1.0 - (distance / maxDistance);
        depthFactor = Math.max(0.2, depthFactor); // Minimum brightness of 20%

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        r = (int)(r * depthFactor);
        g = (int)(g * depthFactor);
        b = (int)(b * depthFactor);

        return (r << 16) | (g << 8) | b;
    }


    private int getEntityColor(Entity entity) {
        if (entity instanceof Player) {
            return 0xFF0000; // Rood voor spelers
        } else if (entity.getType().name().contains("ZOMBIE")) {
            return 0x00FF00; // Groen voor zombies
        } else if (entity.getType().name().contains("SKELETON")) {
            return 0xCCCCCC; // Grijs voor skeletten
        } else if (entity.getType().name().contains("CREEPER")) {
            return 0x00FF00; // Groene kleur voor creepers
        } else {
            return 0xFFFFFF; // Wit voor andere entiteiten
        }
    }

}
