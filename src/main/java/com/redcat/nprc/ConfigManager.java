package com.redcat.nprc;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handles all configuration file operations, providing type-safe getters
 * for world-specific settings.
 */
public class ConfigManager {

    private final JavaPlugin plugin;

    /**
     * Constructor injecting the main plugin instance.
     *
     * @param plugin The JavaPlugin instance
     */
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads or reloads the plugin configuration file from disk.
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
    }

    /**
     * Retrieves the active FileConfiguration instance.
     *
     * @return FileConfiguration object
     */
    private FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    /**
     * Checks if a specific world is defined and enabled within the configuration.
     *
     * @param worldName Name of the world
     * @return true if configured, false otherwise
     */
    public boolean isWorldEnabled(String worldName) {
        return getConfig().contains("worlds." + worldName);
    }

    /**
     * Gets the boundary judgment shape for the given world ("circle" or "square").
     *
     * @param worldName Name of the world
     * @return Shape string (defaults to "circle")
     */
    public String getShape(String worldName) {
        return getConfig().getString("worlds." + worldName + ".shape", "circle");
    }

    /**
     * Gets the center X coordinate for boundary checks in the specified world.
     *
     * @param worldName Name of the world
     * @return Center X coordinate value
     */
    public double getCenterX(String worldName) {
        return getConfig().getDouble("worlds." + worldName + ".center_x", 0.0);
    }

    /**
     * Gets the center Z coordinate for boundary checks in the specified world.
     *
     * @param worldName Name of the world
     * @return Center Z coordinate value
     */
    public double getCenterZ(String worldName) {
        return getConfig().getDouble("worlds." + worldName + ".center_z", 0.0);
    }

    /**
     * Gets the permitted radius or side half-length for the specified world.
     *
     * @param worldName Name of the world
     * @return Allowed radius value
     */
    public double getAllowedRadius(String worldName) {
        return getConfig().getDouble("worlds." + worldName + ".allowed_radius", 375.0);
    }

    /**
     * Gets the raw warning message string configured for the specified world.
     *
     * @param worldName Name of the world
     * @return Raw message string
     */
    public String getRawMessage(String worldName) {
        return getConfig().getString("worlds." + worldName + ".message", "§cYou cannot create a portal outside the permitted area!");
    }
}