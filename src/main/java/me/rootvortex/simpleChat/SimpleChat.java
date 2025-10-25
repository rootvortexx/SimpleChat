package me.rootvortex.simpleChat;

import me.rootvortex.simpleChat.commands.*;
import me.rootvortex.simpleChat.listeners.ChatListener;
import me.rootvortex.simpleChat.managers.ChatManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleChat extends JavaPlugin {

    private ChatManager chatManager;

    @Override
    public void onEnable() {
        // Initialize manager
        this.chatManager = new ChatManager();

        // Register events
        getServer().getPluginManager().registerEvents(new ChatListener(chatManager), this);

        // Register commands using Paper CommandAPI
        registerCommands();

        getLogger().info("SimpleChat has been enabled!");
    }

    private void registerCommands() {
        try {
            // Register commands using Paper's CommandAPI
            new HideChatCommand(chatManager).register(this);
            new IgnoreCommand(chatManager).register(this);
            new UnignoreCommand(chatManager).register(this);
            new IgnoreListCommand(chatManager).register(this);
            new UnignoreAllCommand(chatManager).register(this);
            new MessageCommand(chatManager).register(this);
            new ReplyCommand(chatManager).register(this);

            getLogger().info("All commands registered successfully using Paper CommandAPI!");
        } catch (Exception e) {
            getLogger().severe("Failed to register commands: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("SimpleChat has been disabled!");
    }

    public ChatManager getChatManager() {
        return chatManager;
    }
}