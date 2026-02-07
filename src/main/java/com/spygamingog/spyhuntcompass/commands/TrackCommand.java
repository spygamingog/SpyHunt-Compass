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

public class TrackCommand implements CommandExecutor {

    private final SpyHuntCompass plugin;

    public TrackCommand(SpyHuntCompass plugin) {
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
            player.sendMessage(ChatColor.RED + "Usage: /track <player>");
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        ItemStack item = getOrGiveCompass(player);
        if (item == null) {
            player.sendMessage(ChatColor.RED + "Could not give compass (Inventory full?).");
            return true;
        }

        manager.setOnlyTarget(item, target.getUniqueId());
        boolean updated = manager.updateCompass(player, item, true);

        player.sendMessage(ChatColor.GREEN + "Compass set to track ONLY: " + ChatColor.GOLD + target.getName());
        if (!updated) {
            player.sendMessage(ChatColor.YELLOW + "Warning: Could not update compass target immediately (Target offline or different world?).");
        }

        return true;
    }

    private ItemStack getOrGiveCompass(Player player) {
        CompassManager manager = plugin.getCompassManager();
        ItemStack held = player.getInventory().getItemInMainHand();

        if (manager.isTrackerCompass(held)) {
            return held;
        }

        // Check inventory for EXISTING compass
        ItemStack[] contents = player.getInventory().getContents();
        for (ItemStack item : contents) {
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
        contents = player.getInventory().getContents();
        for (ItemStack item : contents) {
            if (manager.isTrackerCompass(item)) {
                return item;
            }
        }

        return null;
    }
}
