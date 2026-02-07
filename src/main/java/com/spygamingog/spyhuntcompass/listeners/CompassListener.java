package com.spygamingog.spyhuntcompass.listeners;

import com.spygamingog.spyhuntcompass.SpyHuntCompass;
import com.spygamingog.spyhuntcompass.managers.CompassManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class CompassListener implements Listener {

    private final SpyHuntCompass plugin;
    private final CompassManager manager;

    public CompassListener(SpyHuntCompass plugin) {
        this.plugin = plugin;
        this.manager = plugin.getCompassManager();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !manager.isTrackerCompass(item)) return;

        Action action = event.getAction();
        
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            // Refresh
            manager.updateCompass(player, item, true);
            player.sendMessage(ChatColor.YELLOW + "Compass refreshed!");
        } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            // Cycle
            manager.cycleTarget(item);
            manager.updateCompass(player, item, true);
            
            UUID targetId = manager.getCurrentTarget(item);
            if (targetId != null) {
                Player target = Bukkit.getPlayer(targetId);
                String name = target != null ? target.getName() : "Unknown/Offline";
                player.sendMessage(ChatColor.GREEN + "Now tracking: " + ChatColor.GOLD + name);
            } else {
                player.sendMessage(ChatColor.RED + "No targets set.");
            }
        }
    }

    // --- Tracking Data Collection ---

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        // Update last seen for every player occasionally (or every move? Optimization needed?)
        // To save resources, we could only do this if they move significantly or across chunks.
        // But for precise tracking, every move is best if we handle it efficiently.
        // We only update the map in memory, so it's fast.
        
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() 
            && event.getFrom().getBlockY() == event.getTo().getBlockY() 
            && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player p = event.getPlayer();
        manager.updateLastSeen(p.getUniqueId(), p.getWorld().getUID(), p.getLocation());
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        // Capture portal entry location
        manager.updatePortalLocation(event.getPlayer().getUniqueId(), event.getFrom());
    }
}
