package me.frp.rideoperate.panel;

import me.frp.rideoperate.RideOperate;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PanelSpawner {

        private static final String PANEL_NAME_KEY = "panel_name";
        private static final String BUTTON_NAME_KEY = "button_name";

        /*
         *
         * PUBLIC PANEL SPAWN
         *
         */

        public static BlockDisplay spawnBackBoard(
                        RideOperate plugin,
                        Location center,
                        float yaw,
                        String panelName,
                        int buttonCount) {

                World world = center.getWorld();
                if (world == null) {
                        throw new IllegalStateException("Cannot spawn panel without world");
                }

                Location origin = center.clone();
                origin.setYaw(yaw);
                origin.setPitch(0f);

                spawnModelPanel(plugin, origin, panelName);

                // dummy display (voor compatibiliteit met bestaande code)
                BlockDisplay root = (BlockDisplay) world.spawnEntity(origin, EntityType.BLOCK_DISPLAY);
                root.setBlock(Material.BARRIER.createBlockData());
                root.setTransformation(new Transformation(
                                new Vector3f(0, 0, 0),
                                new Quaternionf(),
                                new Vector3f(0, 0, 0),
                                new Quaternionf()));

                root.setPersistent(true);
                root.addScoreboardTag("rideoperate_panel");
                root.addScoreboardTag("rideoperate_panel_" + sanitize(panelName));

                root.getPersistentDataContainer().set(
                                new NamespacedKey(plugin, PANEL_NAME_KEY),
                                PersistentDataType.STRING,
                                panelName);

                return root;
        }

        /*
         *
         * BLOCKBENCH MODEL
         *
         */

        private static void spawnModelPanel(
                        RideOperate plugin,
                        Location origin,
                        String panelName) {

                World world = origin.getWorld();
                if (world == null)
                        return;

                // Element 1
                spawnPartFromTo(world, origin, Material.POLISHED_ANDESITE,
                                bb(-8, 0, 0),
                                bb(24, 18, 16),
                                plugin, panelName);

                // Element 2
                spawnPartFromToRotated(world, origin, Material.GRAY_CONCRETE,
                                bb(-8, 16, 1),
                                bb(24, 18, 17),
                                -22.5f,
                                'x',
                                bb(0, 16, 1),
                                plugin, panelName);

                // Element 3
                spawnPartFromTo(world, origin, Material.POLISHED_DEEPSLATE,
                                bb(-8, 18, 13.8f),
                                bb(24, 24, 16),
                                plugin, panelName);

                // Element 4
                spawnPartFromToRotated(world, origin, Material.POLISHED_ANDESITE,
                                bb(22, 17, 6),
                                bb(24, 21, 15),
                                -22.5f,
                                'x',
                                bb(22, 18, 13),
                                plugin, panelName);

                // Element 5
                spawnPartFromToRotated(world, origin, Material.POLISHED_ANDESITE,
                                bb(-8, 17, 6),
                                bb(-6, 21, 15),
                                -22.5f,
                                'x',
                                bb(-8, 18, 13),
                                plugin, panelName);
        }

        /*
         *
         * BUTTON SPAWNER
         *
         */

        public static ItemDisplay spawnButton(
                        RideOperate plugin,
                        Location panelCenter,
                        Vector forward,
                        int index,
                        int total,
                        float yaw,
                        String panelName,
                        String buttonName) {

                World world = panelCenter.getWorld();
                if (world == null)
                        throw new IllegalStateException("Cannot spawn button without world");

                int columns = total > 4 ? 2 : 1;
                int row = index / columns;
                int column = index % columns;
                int rows = (int) Math.ceil(total / (double) columns);

                // Place buttons in model-space and project them onto the sloped control
                // surface.
                float xCenter = 0f;
                float xSpacing = 4.0f;
                float xModel = columns == 1
                                ? xCenter
                                : xCenter + (column - ((columns - 1) / 2f)) * xSpacing;

                float zMin = 4.8f;
                float zMax = 13.2f;
                float zModel;
                if (rows <= 1) {
                        zModel = (zMin + zMax) / 2f;
                } else {
                        float t = row / (float) (rows - 1);
                        zModel = zMax - ((zMax - zMin) * t);
                }

                float controlAngleRad = (float) Math.toRadians(-22.5f);
                float pivotY = 16f;
                float pivotZ = 1f;
                float yTopSurface = 18f;
                float yLift = 0.18f;

                float yRel = yTopSurface - pivotY;
                float zRel = zModel - pivotZ;
                float cos = (float) Math.cos(controlAngleRad);
                float sin = (float) Math.sin(controlAngleRad);

                float yModel = pivotY + (yRel * cos - zRel * sin) + yLift;
                float zModelRotated = pivotZ + (yRel * sin + zRel * cos);

                Vector horizontalForward = forward.clone().setY(0);
                if (horizontalForward.lengthSquared() == 0) {
                        horizontalForward = new Vector(0, 0, 1);
                }
                horizontalForward.normalize();
                Vector horizontalRight = new Vector(0, 1, 0).crossProduct(horizontalForward).normalize();

                Location location = modelToWorld(panelCenter, horizontalRight, horizontalForward, xModel, yModel,
                                zModelRotated);

                location.setYaw(yaw);
                location.setPitch(0f);

                Quaternionf tilt = new Quaternionf()
                                .rotateX((float) Math.toRadians(-22.5f))
                                .rotateY((float) Math.toRadians(90f));

                // Button item
                ItemDisplay display = (ItemDisplay) world.spawnEntity(location, EntityType.ITEM_DISPLAY);
                display.setItemStack(new ItemStack(Material.STONE_BUTTON));
                display.setTransformation(new Transformation(
                                new Vector3f(0f, 0f, 0.015f),
                                tilt,
                                new Vector3f(0.19f, 0.19f, 0.19f),
                                new Quaternionf()));
                display.setPersistent(true);
                applyButtonMetadata(plugin, display, panelName, buttonName);

                // Hitbox
                Interaction hitbox = (Interaction) world.spawnEntity(
                                location.clone().add(0.0, 0.01, 0.0),
                                EntityType.INTERACTION);
                hitbox.setInteractionWidth(0.24f);
                hitbox.setInteractionHeight(0.16f);
                hitbox.setPersistent(true);
                applyButtonMetadata(plugin, hitbox, panelName, buttonName);

                return display;
        }

        private static Location modelToWorld(
                        Location origin,
                        Vector right,
                        Vector forward,
                        float modelX,
                        float modelY,
                        float modelZ) {

                return origin.clone()
                                .add(right.clone().multiply(modelX / 16.0))
                                .add(0.0, modelY / 16.0, 0.0)
                                .add(forward.clone().multiply(modelZ / 16.0));
        }

        /*
         *
         * MODEL PART SPAWNING
         *
         */

        private static void spawnPart(
                        World world,
                        Location origin,
                        Material material,
                        Vector3f translation,
                        Vector3f scale,
                        RideOperate plugin,
                        String panelName) {

                BlockDisplay part = (BlockDisplay) world.spawnEntity(origin, EntityType.BLOCK_DISPLAY);

                part.setBlock(material.createBlockData());

                part.setTransformation(new Transformation(
                                translation,
                                new Quaternionf(),
                                scale,
                                new Quaternionf()));

                part.setPersistent(true);

                part.addScoreboardTag("rideoperate_panel");
                part.addScoreboardTag("rideoperate_panel_" + sanitize(panelName));

                part.getPersistentDataContainer().set(
                                new NamespacedKey(plugin, PANEL_NAME_KEY),
                                PersistentDataType.STRING,
                                panelName);
        }

        private static void spawnPartFromTo(
                        World world,
                        Location origin,
                        Material material,
                        Vector3f from,
                        Vector3f to,
                        RideOperate plugin,
                        String panelName) {

                Vector3f translation = new Vector3f(from).div(16f);
                Vector3f scale = new Vector3f(to).sub(from).div(16f);

                spawnPart(world, origin, material, translation, scale, plugin, panelName);
        }

        private static void spawnPartFromToRotated(
                        World world,
                        Location origin,
                        Material material,
                        Vector3f from,
                        Vector3f to,
                        float angle,
                        char axis,
                        Vector3f pivot,
                        RideOperate plugin,
                        String panelName) {

                Vector3f fromScaled = new Vector3f(from).div(16f);
                Vector3f scale = new Vector3f(to).sub(from).div(16f);
                Vector3f pivotScaled = new Vector3f(pivot).div(16f);

                Quaternionf rotation = new Quaternionf();
                float radians = (float) Math.toRadians(angle);

                if (axis == 'x') {
                        rotation.rotateX(radians);
                } else if (axis == 'y') {
                        rotation.rotateY(radians);
                } else if (axis == 'z') {
                        rotation.rotateZ(radians);
                } else {
                        throw new IllegalArgumentException("Unsupported axis: " + axis);
                }

                Vector3f translation = new Vector3f(fromScaled)
                                .sub(pivotScaled)
                                .rotate(rotation)
                                .add(pivotScaled);

                BlockDisplay part = (BlockDisplay) world.spawnEntity(origin, EntityType.BLOCK_DISPLAY);

                part.setBlock(material.createBlockData());

                part.setTransformation(new Transformation(
                                translation,
                                rotation,
                                scale,
                                new Quaternionf()));

                part.setPersistent(true);

                part.addScoreboardTag("rideoperate_panel");
                part.addScoreboardTag("rideoperate_panel_" + sanitize(panelName));

                part.getPersistentDataContainer().set(
                                new NamespacedKey(plugin, PANEL_NAME_KEY),
                                PersistentDataType.STRING,
                                panelName);
        }

        /*
         *
         * VECTOR HELPERS (Blockbench -> Minecraft scale)
         *
         */

        private static Vector3f bb(float x, float y, float z) {
                return new Vector3f(x, y, z);
        }

        private static Vector3f vec(float x, float y, float z) {
                return new Vector3f(x / 16f, y / 16f, z / 16f);
        }

        private static Vector3f size(float x, float y, float z) {
                return new Vector3f(x / 16f, y / 16f, z / 16f);
        }

        /*
         *
         * BUTTON METADATA
         *
         */

        private static void applyButtonMetadata(
                        RideOperate plugin,
                        Entity entity,
                        String panelName,
                        String buttonName) {

                entity.addScoreboardTag("rideoperate_button");
                entity.addScoreboardTag("rideoperate_panel_" + sanitize(panelName));
                entity.addScoreboardTag("rideoperate_button_" + sanitize(buttonName));

                entity.getPersistentDataContainer().set(
                                new NamespacedKey(plugin, PANEL_NAME_KEY),
                                PersistentDataType.STRING,
                                panelName);

                entity.getPersistentDataContainer().set(
                                new NamespacedKey(plugin, BUTTON_NAME_KEY),
                                PersistentDataType.STRING,
                                buttonName);
        }

        private static String sanitize(String value) {
                return value.toLowerCase().replace(' ', '_');
        }
}