package me.rootvortex.simpleChat.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class ChatManager {
    private final Set<UUID> globalChatHiders = new HashSet<>();
    private final Map<UUID, Set<UUID>> playerIgnoreMap = new HashMap<>();
    private final Map<UUID, UUID> lastMessengers = new HashMap<>();

    // Global chat methods
    public boolean toggleGlobalChat(Player player) {
        UUID playerId = player.getUniqueId();
        if (globalChatHiders.contains(playerId)) {
            globalChatHiders.remove(playerId);
            return false;
        } else {
            globalChatHiders.add(playerId);
            return true;
        }
    }

    public boolean isChatHidden(Player player) {
        return globalChatHiders.contains(player.getUniqueId());
    }

    // Ignore methods
    public boolean ignorePlayer(Player player, Player target) {
        UUID playerId = player.getUniqueId();
        UUID targetId = target.getUniqueId();
        playerIgnoreMap.computeIfAbsent(playerId, k -> new HashSet<>()).add(targetId);
        return true;
    }

    public boolean unignorePlayer(Player player, Player target) {
        UUID playerId = player.getUniqueId();
        UUID targetId = target.getUniqueId();

        Set<UUID> ignoredPlayers = playerIgnoreMap.get(playerId);
        if (ignoredPlayers != null && ignoredPlayers.remove(targetId)) {
            if (ignoredPlayers.isEmpty()) {
                playerIgnoreMap.remove(playerId);
            }
            return true;
        }
        return false;
    }

    public boolean unignoreAllPlayers(Player player) {
        Set<UUID> ignoredPlayers = playerIgnoreMap.remove(player.getUniqueId());
        return ignoredPlayers != null && !ignoredPlayers.isEmpty();
    }

    public Set<String> getIgnoredPlayers(Player player) {
        Set<UUID> ignoredUUIDs = playerIgnoreMap.get(player.getUniqueId());
        Set<String> ignoredNames = new HashSet<>();

        if (ignoredUUIDs != null) {
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
        }
        return ignoredNames;
    }

    public boolean isIgnoring(Player viewer, Player target) {
        Set<UUID> ignoredPlayers = playerIgnoreMap.get(viewer.getUniqueId());
        return ignoredPlayers != null && ignoredPlayers.contains(target.getUniqueId());
    }

    public int getIgnoreCount(Player player) {
        Set<UUID> ignoredPlayers = playerIgnoreMap.get(player.getUniqueId());
        return ignoredPlayers != null ? ignoredPlayers.size() : 0;
    }

    // Private message methods
    public void setLastMessenger(Player receiver, Player sender) {
        lastMessengers.put(receiver.getUniqueId(), sender.getUniqueId());
    }

    public Player getLastMessenger(Player player) {
        UUID lastMessengerId = lastMessengers.get(player.getUniqueId());
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
        if (globalChatHiders.contains(recipient.getUniqueId())) {
            return false;
        }

        // Check if recipient is ignoring the sender
        Set<UUID> ignoredPlayers = playerIgnoreMap.get(recipient.getUniqueId());
        return ignoredPlayers == null || !ignoredPlayers.contains(sender.getUniqueId());
    }
}