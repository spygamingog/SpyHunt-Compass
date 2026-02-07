package com.spygamingog.spyhuntcompass.commands;

import com.spygamingog.spyhuntcompass.SpyHuntCompass;
import com.spygamingog.spyhuntcompass.managers.CompassManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TrackerCommand implements CommandExecutor {

    private final SpyHuntCompass plugin;

    public TrackerCommand(SpyHuntCompass plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        CompassManager manager = plugin.getCompassManager();

        if (args.length == 0) {
            // Give empty tracker
            if (getOrGiveCompass(player) != null) {
                // Message handled in getOrGiveCompass, but we can add more specific if needed
            } else {
                 player.sendMessage(ChatColor.RED + "Could not give compass (Inventory full?).");
            }
            return true;
        }

        String sub = args[0];
        
        if (sub.equalsIgnoreCase("add")) {
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Usage: /tracker add <player>");
                return true;
            }
            handleModify(player, args[1], true);
        } else if (sub.equalsIgnoreCase("remove")) {
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Usage: /tracker remove <player>");
                return true;
            }
            handleModify(player, args[1], false);
        } else if (sub.equalsIgnoreCase("help")) {
            sendHelp(player);
        } else {
            // Unknown subcommand, previously this was "set target", now it should show help or error
            // User requested "/tracker without any subcommand will give an empty tracker" -> handled above
            // "add /tracker help to show list of all commands"
            sendHelp(player);
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Tracker Help ===");
        player.sendMessage(ChatColor.YELLOW + "/tracker" + ChatColor.WHITE + " - Get an empty tracker compass.");
        player.sendMessage(ChatColor.YELLOW + "/track <player>" + ChatColor.WHITE + " - Track a specific player (clears other targets).");
        player.sendMessage(ChatColor.YELLOW + "/tracker add <player>" + ChatColor.WHITE + " - Add a player to your tracking list.");
        player.sendMessage(ChatColor.YELLOW + "/tracker remove <player>" + ChatColor.WHITE + " - Remove a player from your tracking list.");
        player.sendMessage(ChatColor.YELLOW + "/tracker help" + ChatColor.WHITE + " - Show this help message.");
    }

    private void handleModify(Player player, String targetName, boolean add) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        ItemStack item = getOrGiveCompass(player);
        if (item == null) {
            player.sendMessage(ChatColor.RED + "Could not give compass (Inventory full?).");
            return;
        }
        
        CompassManager manager = plugin.getCompassManager();

        if (add) {
            manager.addTarget(item, target.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Added " + ChatColor.GOLD + target.getName() + ChatColor.GREEN + " to tracker.");
            // If it's the first target, update immediately
            manager.updateCompass(player, item, true);
        } else {
            manager.removeTarget(item, target.getUniqueId());
            player.sendMessage(ChatColor.YELLOW + "Removed " + ChatColor.GOLD + target.getName() + ChatColor.YELLOW + " from tracker.");
        }
    }

    private ItemStack getOrGiveCompass(Player player) {
        CompassManager manager = plugin.getCompassManager();
        ItemStack held = player.getInventory().getItemInMainHand();
        
        if (manager.isTrackerCompass(held)) {
            return held;
        }
        
        // Check inventory for EXISTING compass
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (manager.isTrackerCompass(item)) {
                return item;
            }
        }

        // Give new
        ItemStack newItem = manager.createCompass();
        // Add to inventory
        if (!player.getInventory().addItem(newItem).isEmpty()) {
            // Inventory full
            return null;
        }
        player.sendMessage(ChatColor.GREEN + "Here is your Tracker Compass!");
        
        // RETRIEVE LIVE REFERENCE
        // Scan inventory again to find the item we just added
        contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (manager.isTrackerCompass(item)) {
                // If the player had no compass before, this MUST be the new one.
                return item;
            }
        }
        
        return null; // Should not happen if addItem succeeded
    }
}
