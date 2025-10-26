package me.rootvortex.simpleChat.listeners;

import me.rootvortex.simpleChat.SimpleChat;
import me.rootvortex.simpleChat.managers.ChatManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Set;

public class ChatListener implements Listener {
    private final ChatManager chatManager;
    private final SimpleChat plugin;

    public ChatListener(ChatManager chatManager, SimpleChat plugin) {
        this.chatManager = chatManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        String originalMessage = event.getMessage();
        Set<Player> recipients = event.getRecipients();

        // USE plugin.getConfig() DIRECTLY - NO EXTRA METHOD NEEDED
        boolean chatEnabled = plugin.getConfig().getBoolean("chat.enabled", true);

        if (chatEnabled) {
            // USE plugin.getConfig() DIRECTLY
            String format = plugin.getConfig().getString("chat.format", "&7{player}&8: &f{message}");

            // REPLACE PLACEHOLDERS
            String formattedMessage = format
                    .replace("{player}", sender.getDisplayName())
                    .replace("{message}", originalMessage);

            // APPLY COLOR CODES
            formattedMessage = ChatColor.translateAlternateColorCodes('&', formattedMessage);

            // SET THE FORMATTED MESSAGE
            event.setFormat(formattedMessage);
        }

        // Filter recipients based on ignore settings (your existing code)
        Set<Player> filteredRecipients = chatManager.filterChatRecipients(sender, recipients);

        // Update the recipients
        event.getRecipients().clear();
        event.getRecipients().addAll(filteredRecipients);
    }
}