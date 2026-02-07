package com.spygamingog.spyhuntcompass;

import com.spygamingog.spyhuntcompass.commands.TrackCommand;
import com.spygamingog.spyhuntcompass.commands.TrackerCommand;
import com.spygamingog.spyhuntcompass.commands.TrackerTabCompleter;
import com.spygamingog.spyhuntcompass.listeners.CompassListener;
import com.spygamingog.spyhuntcompass.managers.CompassManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SpyHuntCompass extends JavaPlugin {

    private static SpyHuntCompass instance;
    private CompassManager compassManager;

    @Override
    public void onEnable() {
        instance = this;
        
        this.compassManager = new CompassManager(this);
        
        getCommand("tracker").setExecutor(new TrackerCommand(this));
        getCommand("tracker").setTabCompleter(new TrackerTabCompleter());
        
        getCommand("track").setExecutor(new TrackCommand(this));
        getCommand("track").setTabCompleter(new TrackerTabCompleter()); // Reuse logic
        
        getServer().getPluginManager().registerEvents(new CompassListener(this), this);
        
        getLogger().info("SpyHunt-Compass enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("SpyHunt-Compass disabled!");
    }

    public static SpyHuntCompass getInstance() {
        return instance;
    }

    public CompassManager getCompassManager() {
        return compassManager;
    }
}
