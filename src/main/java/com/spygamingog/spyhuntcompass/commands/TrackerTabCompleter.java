package com.spygamingog.spyhuntcompass.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TrackerTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("track")) {
            if (args.length == 1) {
                // Suggest online players for /track <player>
                return null; // Bukkit default: online players
            }
        } else if (command.getName().equalsIgnoreCase("tracker")) {
            if (args.length == 1) {
                List<String> completions = new ArrayList<>();
                List<String> options = Arrays.asList("add", "remove", "help");
                StringUtil.copyPartialMatches(args[0], options, completions);
                Collections.sort(completions);
                return completions;
            }
            
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                    // Hide player names for add/remove as requested
                    return Collections.emptyList();
                }
            }
        }
        
        return Collections.emptyList();
    }
}
