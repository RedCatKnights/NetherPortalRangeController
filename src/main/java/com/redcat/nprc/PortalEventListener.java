package com.redcat.nprc;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.PortalCreateEvent;

/**
 * Handles all event-driven logic related to nether portals,
 * including creation restriction, ignition validation, and existing portal purging.
 */
public class PortalEventListener implements Listener {

    private final ConfigManager configManager;
    private final PortalCheckManager portalCheckManager;

    /**
     * Constructor injecting required managers.
     *
     * @param configManager      Configuration management instance
     * @param portalCheckManager Boundary and validation management instance
     */
    public PortalEventListener(ConfigManager configManager, PortalCheckManager portalCheckManager) {
        this.configManager = configManager;
        this.portalCheckManager = portalCheckManager;
    }

    /**
     * Intercepts portal creation events (natural or structural generation).
     *
     * @param event PortalCreateEvent triggered by server or players
     */
    @EventHandler
    public void onPortalCreate(PortalCreateEvent event) {
        World world = event.getWorld();
        String worldName = world.getName();

        // Check if the world is registered and enabled in configuration
        if (!configManager.isWorldEnabled(worldName)) return;
        if (event.getBlocks().isEmpty()) return;

        Location loc = event.getBlocks().get(0).getLocation();

        // Verify if the portal blocks are generated outside the permitted boundary
        if (portalCheckManager.isOutsideLimit(worldName, loc)) {
            event.setCancelled(true);

            // Notify the player if the entity responsible is a player
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                portalCheckManager.sendWarningMessage(player, worldName);
            }
        }
    }

    /**
     * Intercepts player interaction events to control portal ignition using flint and steel or fire charges.
     *
     * @param event PlayerInteractEvent triggered by player actions
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Ensure the action is a block right-click
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        // Verify if the item used is an ignition tool (flint and steel or fire charge)
        if (event.getItem() == null || (event.getItem().getType() != Material.FLINT_AND_STEEL && event.getItem().getType() != Material.FIRE_CHARGE)) {
            return;
        }

        World world = clickedBlock.getWorld();
        String worldName = world.getName();

        if (!configManager.isWorldEnabled(worldName)) return;

        // Check if the clicked block or its immediate surroundings involve obsidian
        boolean nearObsidian = (clickedBlock.getType() == Material.OBSIDIAN);
        if (!nearObsidian) {
            for (BlockFace face : new BlockFace[]{BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
                if (clickedBlock.getRelative(face).getType() == Material.OBSIDIAN) {
                    nearObsidian = true;
                    break;
                }
            }
        }

        // Ignore interactions not associated with portal frames
        if (!nearObsidian) {
            return;
        }

        Location loc = clickedBlock.getLocation();

        // Restrict and warn if ignition location exceeds the permitted boundary
        if (portalCheckManager.isOutsideLimit(worldName, loc)) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            portalCheckManager.sendWarningMessage(player, worldName);
        }
    }

    /**
     * Intercepts players entering portals to block teleportation and destroy pre-existing or data-pack generated portals.
     *
     * @param event PlayerPortalEvent triggered when a player enters a nether portal
     */
    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getFrom();
        World world = loc.getWorld();
        if (world == null) return;

        String worldName = world.getName();
        if (!configManager.isWorldEnabled(worldName)) return;

        // Check if the portal location is outside the boundary limit
        if (portalCheckManager.isOutsideLimit(worldName, loc)) {
            event.setCancelled(true); // Cancel teleportation attempt
            portalCheckManager.sendWarningMessage(player, worldName); // Send warning feedback

            // Automatically destroy the illegal portal blocks to prevent future use
            destroyPortalAt(loc);
        }
    }

    /**
     * Helper method to purge nether portal blocks around the specified coordinate.
     *
     * @param loc The reference location of the portal block
     */
    private void destroyPortalAt(Location loc) {
        Block block = loc.getBlock();

        // Clear direct portal block if present
        if (block.getType() == Material.NETHER_PORTAL) {
            block.setType(Material.AIR);
        }

        // Clean up connected/adjacent portal blocks
        for (BlockFace face : new BlockFace[]{BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            Block relative = block.getRelative(face);
            if (relative.getType() == Material.NETHER_PORTAL) {
                relative.setType(Material.AIR);
            }
        }
    }
}