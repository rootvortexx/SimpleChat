package me.rootvortex.simpleChat.listeners;

import me.rootvortex.simpleChat.managers.ChatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Set;

public class ChatListener implements Listener {
    private final ChatManager chatManager;

    public ChatListener(ChatManager chatManager) {
        this.chatManager = chatManager;
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        Set<Player> recipients = event.getRecipients();

        // Filter recipients based on ignore settings
        Set<Player> filteredRecipients = chatManager.filterChatRecipients(sender, recipients);

        // Update the recipients
        event.getRecipients().clear();
        event.getRecipients().addAll(filteredRecipients);
    }
}