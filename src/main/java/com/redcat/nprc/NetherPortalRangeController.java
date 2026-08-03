package com.redcat.nprc;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for NetherPortalRangeController (NPRC).
 * Handles plugin lifecycle initialization, manager binding, and command dispatching.
 */
public final class NetherPortalRangeController extends JavaPlugin implements CommandExecutor {

    private ConfigManager configManager;
    private PortalCheckManager portalCheckManager;
    private PortalEventListener portalEventListener;

    @Override
    public void onEnable() {
        // Initialize core configuration and management components
        this.configManager = new ConfigManager(this);
        this.portalCheckManager = new PortalCheckManager(configManager);
        this.portalEventListener = new PortalEventListener(configManager, portalCheckManager);

        // Load configuration file from disk
        configManager.loadConfig();

        // Register event listener for portal creation, interaction, and usage
        getServer().getPluginManager().registerEvents(portalEventListener, this);

        // Register main command executor
        if (getCommand("nprc") != null) {
            getCommand("nprc").setExecutor(this);
        }

        getLogger().info("NetherPortalRangeController (NPRC) by Red Cat has been successfully enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("NetherPortalRangeController (NPRC) has been disabled.");
    }

    /**
     * Handle administrative commands for the plugin (e.g., /nprc reload).
     *
     * @param sender  The command sender (player or console)
     * @param command The executed command instance
     * @param label   The alias used for the command
     * @param args    Command arguments passed by the sender
     * @return true if the command was handled successfully, false otherwise
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("nprc")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                // Check for administrative permissions
                if (!sender.hasPermission("nprc.admin")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou do not have permission to use this command."));
                    return true;
                }

                // Reload configuration parameters
                configManager.loadConfig();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a[NPRC] Config has been successfully reloaded!"));
                return true;
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eUsage: /nprc reload"));
                return true;
            }
        }
        return false;
    }
}