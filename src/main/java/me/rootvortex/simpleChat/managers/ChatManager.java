package me.rootvortex.simpleChat.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class ChatManager {
    private final DatabaseManager databaseManager;

    // Memory cache for better performance
    private final Map<UUID, Boolean> globalChatCache = new HashMap<>();
    private final Map<UUID, Set<UUID>> ignoreCache = new HashMap<>();
    private final Map<UUID, UUID> lastMessengerCache = new HashMap<>();

    public ChatManager(Plugin plugin) {
        this.databaseManager = new DatabaseManager(plugin);
        loadAllData();
    }

    private void loadAllData() {
        Bukkit.getLogger().info("ChatManager initialized with database support");
    }

    // Test database connection
    public boolean testDatabaseConnection() {
        return databaseManager.testConnection();
    }

    // Global chat methods
    public boolean toggleGlobalChat(Player player) {
        UUID playerId = player.getUniqueId();
        boolean currentState = isChatHidden(player);
        boolean newState = !currentState;

        globalChatCache.put(playerId, newState);
        databaseManager.setGlobalChatHidden(playerId, newState);

        return newState;
    }

    public boolean isChatHidden(Player player) {
        UUID playerId = player.getUniqueId();

        // Check cache first
        if (globalChatCache.containsKey(playerId)) {
            return globalChatCache.get(playerId);
        }

        // Load from database
        boolean isHidden = databaseManager.isGlobalChatHidden(playerId);
        globalChatCache.put(playerId, isHidden);
        return isHidden;
    }

    // Ignore methods
    public boolean ignorePlayer(Player player, Player target) {
        UUID playerId = player.getUniqueId();
        UUID targetId = target.getUniqueId();

        // Update cache
        ignoreCache.computeIfAbsent(playerId, k -> new HashSet<>()).add(targetId);

        // Update database
        databaseManager.addIgnore(playerId, targetId);

        return true;
    }

    public boolean unignorePlayer(Player player, Player target) {
        UUID playerId = player.getUniqueId();
        UUID targetId = target.getUniqueId();

        Set<UUID> ignoredPlayers = ignoreCache.get(playerId);
        boolean removed = false;

        if (ignoredPlayers != null && ignoredPlayers.remove(targetId)) {
            removed = true;
            if (ignoredPlayers.isEmpty()) {
                ignoreCache.remove(playerId);
            }
        }

        // Update database
        databaseManager.removeIgnore(playerId, targetId);

        return removed;
    }

    public boolean unignoreAllPlayers(Player player) {
        UUID playerId = player.getUniqueId();
        Set<UUID> ignoredPlayers = ignoreCache.remove(playerId);
        boolean hadIgnores = ignoredPlayers != null && !ignoredPlayers.isEmpty();

        // Update database
        databaseManager.removeAllIgnores(playerId);

        return hadIgnores;
    }

    public Set<String> getIgnoredPlayers(Player player) {
        UUID playerId = player.getUniqueId();
        Set<UUID> ignoredUUIDs = getIgnoredUUIDs(playerId);
        Set<String> ignoredNames = new HashSet<>();

        for (UUID uuid : ignoredUUIDs) {
            Player onlinePlayer = Bukkit.getPlayer(uuid);
            if (onlinePlayer != null) {
                ignoredNames.add(onlinePlayer.getName());
            } else {
                String name = Bukkit.getOfflinePlayer(uuid).getName();
                if (name != null) {
                    ignoredNames.add(name);
                }
            }
        }
        return ignoredNames;
    }

    private Set<UUID> getIgnoredUUIDs(UUID playerId) {
        // Check cache first
        if (ignoreCache.containsKey(playerId)) {
            return ignoreCache.get(playerId);
        }

        // Load from database
        Set<UUID> ignoredPlayers = databaseManager.getIgnoredPlayers(playerId);
        ignoreCache.put(playerId, ignoredPlayers);
        return ignoredPlayers;
    }

    public boolean isIgnoring(Player viewer, Player target) {
        Set<UUID> ignoredPlayers = getIgnoredUUIDs(viewer.getUniqueId());
        return ignoredPlayers.contains(target.getUniqueId());
    }

    public int getIgnoreCount(Player player) {
        return getIgnoredUUIDs(player.getUniqueId()).size();
    }

    // Private message methods
    public void setLastMessenger(Player receiver, Player sender) {
        UUID receiverId = receiver.getUniqueId();
        UUID senderId = sender.getUniqueId();

        // Update cache
        lastMessengerCache.put(receiverId, senderId);

        // Update database
        databaseManager.setLastMessenger(receiverId, senderId);
    }

    public Player getLastMessenger(Player player) {
        UUID playerId = player.getUniqueId();
        UUID lastMessengerId;

        // Check cache first
        if (lastMessengerCache.containsKey(playerId)) {
            lastMessengerId = lastMessengerCache.get(playerId);
        } else {
            // Load from database
            lastMessengerId = databaseManager.getLastMessenger(playerId);
            if (lastMessengerId != null) {
                lastMessengerCache.put(playerId, lastMessengerId);
            }
        }

        return lastMessengerId != null ? Bukkit.getPlayer(lastMessengerId) : null;
    }

    // Chat filtering
    public Set<Player> filterChatRecipients(Player sender, Set<Player> recipients) {
        Set<Player> filteredRecipients = new HashSet<>();

        for (Player recipient : recipients) {
            if (shouldSeeMessage(recipient, sender)) {
                filteredRecipients.add(recipient);
            }
        }

        return filteredRecipients;
    }

    private boolean shouldSeeMessage(Player recipient, Player sender) {
        // Check if recipient has global chat hidden
        if (isChatHidden(recipient)) {
            return false;
        }

        // Check if recipient is ignoring the sender
        return !isIgnoring(recipient, sender);
    }

    // Cleanup method
    public void cleanup() {
        databaseManager.close();
    }

    // Method to clear cache for a player (useful when they logout)
    public void clearPlayerCache(UUID playerId) {
        globalChatCache.remove(playerId);
        ignoreCache.remove(playerId);
        lastMessengerCache.remove(playerId);
    }
}