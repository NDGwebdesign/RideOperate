package me.frp.rideoperate.listener;

import me.frp.rideoperate.RideOperate;
import org.bukkit.configuration.file.FileConfiguration;

public class MassagesToggle {

    private final FileConfiguration config;

    public MassagesToggle(RideOperate plugin) {
        this.config = plugin.getConfig();
    }

    public boolean areMessagesEnabled() {
        return config.getBoolean("messages.enabled", true);
    }

    public String getNoPermissionMessage() {
        return getMessage("nopermission");
    }

    public String getAllExistMessage() {
        return getMessage("allexist");
    }

    public String getSuccessfullyCreatedMessage(String panelName) {
        return getMessage("successfullycreated").replace("%panelName%", panelName);
    }

    public String getSuccessfullyDeletedMessage(String panelName) {
        return getMessage("successfullydeleted").replace("%panelName%", panelName);
    }

    private String getMessage(String key) {
        if (areMessagesEnabled()) {
            return config.getString("messages." + key, "Message not found");
        } else {
            return "";
        }
    }
}
