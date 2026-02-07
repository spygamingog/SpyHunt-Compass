package com.spygamingog.spyhuntcompass.managers;

import com.spygamingog.spyhuntcompass.SpyHuntCompass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class CompassManager {

    private final SpyHuntCompass plugin;
    private final NamespacedKey trackerKey; // Marks item as a SpyHunt compass
    private final NamespacedKey currentTargetKey; // UUID of currently tracked player
    private final NamespacedKey targetListKey; // String list of UUIDs (comma separated)

    // Caches for tracking
    private final Map<UUID, Location> lastLodestonePos = new HashMap<>();
    private final Map<UUID, Long> lastUpdateAt = new HashMap<>();
    
    // Tracking Data (Global for simplicity in standalone)
    // Map<PlayerUUID, Map<WorldUID, Location>>
    private final Map<UUID, Map<UUID, Location>> lastSeen = new HashMap<>();
    private final Map<UUID, Location> lastOverworldPortal = new HashMap<>();
    private final Map<UUID, Location> lastNetherPortal = new HashMap<>();

    public CompassManager(SpyHuntCompass plugin) {
        this.plugin = plugin;
        this.trackerKey = new NamespacedKey(plugin, "spyhunt_tracker");
        this.currentTargetKey = new NamespacedKey(plugin, "spyhunt_target");
        this.targetListKey = new NamespacedKey(plugin, "spyhunt_target_list");

        startAutoRefreshTask();
    }

    private void startAutoRefreshTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (isTrackerCompass(hand)) {
                    updateCompass(player, hand, false);
                }
                
                ItemStack offHand = player.getInventory().getItemInOffHand();
                if (isTrackerCompass(offHand)) {
                    updateCompass(player, offHand, false);
                }
            }
        }, 20L, 20L); // Refresh every second (20 ticks)
    }

    public ItemStack createCompass() {
        ItemStack item = new ItemStack(Material.COMPASS);
        CompassMeta meta = (CompassMeta) item.getItemMeta();
        meta.setDisplayName("§aTracker Compass");
        meta.setLore(Arrays.asList("§7Left-Click to refresh", "§7Right-Click to cycle targets"));
        meta.getPersistentDataContainer().set(trackerKey, PersistentDataType.BYTE, (byte) 1);
        meta.setLodestoneTracked(false);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isTrackerCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS) return false;
        if (!(item.getItemMeta() instanceof CompassMeta)) return false;
        return item.getItemMeta().getPersistentDataContainer().has(trackerKey, PersistentDataType.BYTE);
    }

    // --- Tracking Data Management ---

    public void updateLastSeen(UUID playerUuid, UUID worldUid, Location loc) {
        lastSeen.computeIfAbsent(playerUuid, k -> new HashMap<>()).put(worldUid, loc);
    }

    public void updatePortalLocation(UUID playerUuid, Location loc) {
        if (loc.getWorld().getEnvironment() == World.Environment.NORMAL) {
            lastOverworldPortal.put(playerUuid, loc);
        } else if (loc.getWorld().getEnvironment() == World.Environment.NETHER) {
            lastNetherPortal.put(playerUuid, loc);
        }
    }

    private Location getPortalLocation(UUID playerUuid, String env) {
        if (env.equals("overworld")) return lastOverworldPortal.get(playerUuid);
        if (env.equals("nether")) return lastNetherPortal.get(playerUuid);
        return null;
    }

    // --- Compass State Management ---

    public void addTarget(ItemStack item, UUID targetId) {
        if (!isTrackerCompass(item)) return;
        List<UUID> targets = getTargets(item);
        if (!targets.contains(targetId)) {
            targets.add(targetId);
            saveTargets(item, targets);
        }
        // If no current target, set this one
        if (getCurrentTarget(item) == null) {
            setTarget(item, targetId);
        }
    }

    public void removeTarget(ItemStack item, UUID targetId) {
        if (!isTrackerCompass(item)) return;
        List<UUID> targets = getTargets(item);
        if (targets.remove(targetId)) {
            saveTargets(item, targets);
        }
        // If current was removed, cycle or clear
        if (targetId.equals(getCurrentTarget(item))) {
            if (!targets.isEmpty()) {
                setTarget(item, targets.get(0));
            } else {
                setTarget(item, null);
            }
        }
    }
    
    public void setOnlyTarget(ItemStack item, UUID targetId) {
        if (!isTrackerCompass(item)) return;
        List<UUID> targets = new ArrayList<>();
        targets.add(targetId);
        saveTargets(item, targets);
        setTarget(item, targetId);
    }

    public void cycleTarget(ItemStack item) {
        if (!isTrackerCompass(item)) return;
        List<UUID> targets = getTargets(item);
        if (targets.isEmpty()) return;

        UUID current = getCurrentTarget(item);
        int index = targets.indexOf(current);
        
        int nextIndex = (index + 1) % targets.size();
        setTarget(item, targets.get(nextIndex));
    }

    private List<UUID> getTargets(ItemStack item) {
        CompassMeta meta = (CompassMeta) item.getItemMeta();
        String data = meta.getPersistentDataContainer().getOrDefault(targetListKey, PersistentDataType.STRING, "");
        List<UUID> list = new ArrayList<>();
        if (!data.isEmpty()) {
            for (String s : data.split(",")) {
                try { list.add(UUID.fromString(s)); } catch (Exception ignored) {}
            }
        }
        return list;
    }

    private void saveTargets(ItemStack item, List<UUID> targets) {
        CompassMeta meta = (CompassMeta) item.getItemMeta();
        StringBuilder sb = new StringBuilder();
        for (UUID id : targets) {
            if (sb.length() > 0) sb.append(",");
            sb.append(id.toString());
        }
        meta.getPersistentDataContainer().set(targetListKey, PersistentDataType.STRING, sb.toString());
        item.setItemMeta(meta);
    }

    public UUID getCurrentTarget(ItemStack item) {
        if (!isTrackerCompass(item)) return null;
        CompassMeta meta = (CompassMeta) item.getItemMeta();
        String s = meta.getPersistentDataContainer().getOrDefault(currentTargetKey, PersistentDataType.STRING, "");
        if (s.isEmpty()) return null;
        try { return UUID.fromString(s); } catch (Exception e) { return null; }
    }

    public void setTarget(ItemStack item, UUID targetId) {
        if (!isTrackerCompass(item)) return;
        CompassMeta meta = (CompassMeta) item.getItemMeta();
        if (targetId == null) {
            meta.getPersistentDataContainer().remove(currentTargetKey);
        } else {
            meta.getPersistentDataContainer().set(currentTargetKey, PersistentDataType.STRING, targetId.toString());
        }
        item.setItemMeta(meta);
    }

    // --- Compass Update Logic (The "Brain") ---

    public boolean updateCompass(Player holder, ItemStack item, boolean force) {
        if (!isTrackerCompass(item)) return false;

        UUID targetUuid = getCurrentTarget(item);
        if (targetUuid == null) return false;

        Player target = Bukkit.getPlayer(targetUuid);
        Location targetLoc = null;

        if (target != null && target.isOnline()) {
            targetLoc = target.getLocation();
        }

        // Logic adapted from SpyHunts
        org.bukkit.World.Environment holderEnv = holder.getWorld().getEnvironment();
        Location actualTargetLoc = targetLoc;

        // Cross-dimension logic
        if (targetLoc == null || !holder.getWorld().getUID().equals(targetLoc.getWorld().getUID())) {
            // Target is offline OR in different world
            boolean found = false;
            
            // 1. Check Last Seen in current world
            Map<UUID, Location> pLastSeen = lastSeen.get(targetUuid);
            if (pLastSeen != null && pLastSeen.containsKey(holder.getWorld().getUID())) {
                actualTargetLoc = pLastSeen.get(holder.getWorld().getUID());
                found = true;
            }

            // 2. Check Portals if not found in world directly
            if (!found) {
                // If we don't know where they are (offline or just different world), try to guess via portal logic
                // If targetLoc is null (offline), we can only rely on last known state.
                // Assuming we know which world they were in? 
                // For standalone, let's rely on stored portal locations.
                
                // If target is ONLINE but different world:
                if (targetLoc != null) {
                    World.Environment targetEnv = targetLoc.getWorld().getEnvironment();
                    if (holderEnv == World.Environment.NORMAL && targetEnv == World.Environment.NETHER) {
                         actualTargetLoc = getPortalLocation(targetUuid, "overworld");
                    } else if (holderEnv == World.Environment.NETHER && targetEnv == World.Environment.NORMAL) {
                         actualTargetLoc = getPortalLocation(targetUuid, "nether");
                    } else if (holderEnv == World.Environment.NORMAL && targetEnv == World.Environment.THE_END) {
                         actualTargetLoc = getPortalLocation(targetUuid, "overworld"); // Approximate
                    }
                } else {
                    // Target OFFLINE. 
                    // We just use the last seen location if available, otherwise we can't track.
                    if (actualTargetLoc == null && pLastSeen != null) {
                        // Pick any last seen? No, that might be misleading.
                    }
                }
            }
        }

        if (actualTargetLoc == null) {
            return false;
        }

        // Update the compass meta
        CompassMeta meta = (CompassMeta) item.getItemMeta();
        boolean itemUpdated = false;

        holder.setCompassTarget(actualTargetLoc);

        Location prev = lastLodestonePos.get(holder.getUniqueId());
        long now = System.currentTimeMillis();
        Long lastT = lastUpdateAt.getOrDefault(holder.getUniqueId(), 0L);
        boolean far = prev == null || prev.getWorld() != actualTargetLoc.getWorld() || prev.distanceSquared(actualTargetLoc) > 16.0;
        boolean timeOk = (now - lastT) > 1000; // 1s cooldown for item meta updates to prevent lag/spam

        if (force || (far && timeOk)) {
            boolean changed = false;
            
            if (meta.isLodestoneTracked()) {
                meta.setLodestoneTracked(false);
                changed = true;
            }
            if (!actualTargetLoc.equals(meta.getLodestone())) {
                meta.setLodestone(actualTargetLoc);
                changed = true;
            }

            if (changed) {
                item.setItemMeta(meta);
                itemUpdated = true;
            }
            lastLodestonePos.put(holder.getUniqueId(), actualTargetLoc);
            lastUpdateAt.put(holder.getUniqueId(), now);
        }

        return itemUpdated;
    }
}
