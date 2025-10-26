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
        String originalMessage = event.getMessage();
        Set<Player> recipients = event.getRecipients();

        // ADD THIS SIMPLE FORMATTING LINE:
        event.setFormat("§7%s§6 » §f%s"); // This changes "Player: message" to gray name + white message

        // Filter recipients based on ignore settings (your existing code)
        Set<Player> filteredRecipients = chatManager.filterChatRecipients(sender, recipients);

        // Update the recipients
        event.getRecipients().clear();
        event.getRecipients().addAll(filteredRecipients);
    }
}