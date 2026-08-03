package com.redcat.nprc;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Manages spatial calculations (circle and square boundary validation)
 * and message formatting/dispatching to players.
 */
public class PortalCheckManager {

    private final ConfigManager configManager;

    /**
     * Constructor injecting the configuration manager.
     *
     * @param configManager Configuration management instance
     */
    public PortalCheckManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * Determines whether the specified location is outside the permitted boundary.
     *
     * @param worldName Target world name
     * @param loc       Location to check
     * @return true if outside the limit, false otherwise
     */
    public boolean isOutsideLimit(String worldName, Location loc) {
        if (!configManager.isWorldEnabled(worldName)) {
            return false;
        }

        String shape = configManager.getShape(worldName);
        double centerX = configManager.getCenterX(worldName);
        double centerZ = configManager.getCenterZ(worldName);
        double radius = configManager.getAllowedRadius(worldName);

        double x = loc.getX();
        double z = loc.getZ();

        if (shape.equalsIgnoreCase("circle")) {
            // Circle boundary check: (x - cx)^2 + (z - cz)^2 > r^2
            double distanceSquared = Math.pow(x - centerX, 2) + Math.pow(z - centerZ, 2);
            return distanceSquared > Math.pow(radius, 2);
        } else if (shape.equalsIgnoreCase("square")) {
            // Square boundary check: absolute axis-aligned difference > radius
            double diffX = Math.abs(x - centerX);
            double diffZ = Math.abs(z - centerZ);
            return diffX > radius || diffZ > radius;
        }

        return false;
    }

    /**
     * Sends the formatted warning message to the player with placeholders replaced.
     *
     * @param player    The target player
     * @param worldName The world name for config lookup
     */
    public void sendWarningMessage(Player player, String worldName) {
        String rawMessage = configManager.getRawMessage(worldName);
        String shape = configManager.getShape(worldName);
        int radius = (int) configManager.getAllowedRadius(worldName);

        // Replace custom placeholders and translate color formatting codes (& and §)
        String formattedMessage = ChatColor.translateAlternateColorCodes('&', rawMessage
                .replace("<msg_shape>", shape)
                .replace("<msg_allowed_radius>", String.valueOf(radius))
        );

        player.sendMessage(formattedMessage);
    }
}