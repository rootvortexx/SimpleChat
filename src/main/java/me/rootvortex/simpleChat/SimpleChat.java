package me.rootvortex.simpleChat;

import me.rootvortex.simpleChat.commands.*;
import me.rootvortex.simpleChat.listeners.ChatListener;
import me.rootvortex.simpleChat.managers.ChatManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public final class SimpleChat extends JavaPlugin implements Listener {

    private ChatManager chatManager;
    private File databaseConfigFile;
    private FileConfiguration databaseConfig;

    @Override
    public void onEnable() {
        // ADD THIS LINE: Save default config
        saveDefaultConfig();

        // Create and load database.yml
        setupDatabaseConfig();

        // Initialize manager with database support
        try {
            this.chatManager = new ChatManager(this);
            getLogger().info("ChatManager initialized successfully");
        } catch (Exception e) {
            getLogger().severe("Failed to initialize ChatManager: " + e.getMessage());
            e.printStackTrace();
            // Disable plugin if database fails to initialize
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register events - UPDATE THIS LINE: pass 'this' to ChatListener
        getServer().getPluginManager().registerEvents(new ChatListener(chatManager, this), this);
        getServer().getPluginManager().registerEvents(this, this); // Register for quit events

        // Register commands using Paper CommandAPI
        registerCommands();

        getLogger().info("SimpleChat has been enabled with database support!");
        getLogger().info("Database type: " + getDatabaseConfig().getString("database.type", "sqlite"));

        // Test database connection
        if (chatManager.testDatabaseConnection()) {
            getLogger().info("Database connection test: SUCCESS");
        } else {
            getLogger().warning("Database connection test: FAILED - using memory cache only");
        }
    }

    private void setupDatabaseConfig() {
        databaseConfigFile = new File(getDataFolder(), "database.yml");

        if (!databaseConfigFile.exists()) {
            // Create data folder if it doesn't exist
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }

            // Copy default database.yml from resources
            try (InputStream inputStream = getResource("database.yml")) {
                if (inputStream != null) {
                    Files.copy(inputStream, databaseConfigFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    getLogger().info("Created default database.yml configuration file");
                } else {
                    getLogger().warning("Could not find database.yml in resources, creating empty file");
                    databaseConfigFile.createNewFile();
                }
            } catch (Exception e) {
                getLogger().severe("Failed to create database.yml: " + e.getMessage());
                e.printStackTrace();
            }
        }

        reloadDatabaseConfig();
    }

    public void reloadDatabaseConfig() {
        databaseConfig = YamlConfiguration.loadConfiguration(databaseConfigFile);

        // Set defaults from embedded resource
        try (InputStream defaultStream = getResource("database.yml")) {
            if (defaultStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
                databaseConfig.setDefaults(defaultConfig);
                databaseConfig.options().copyDefaults(true);
                saveDatabaseConfig();
            }
        } catch (Exception e) {
            getLogger().warning("Could not load default database.yml from resources: " + e.getMessage());
        }
    }

    public void saveDatabaseConfig() {
        try {
            databaseConfig.save(databaseConfigFile);
        } catch (Exception e) {
            getLogger().severe("Could not save database.yml: " + e.getMessage());
        }
    }

    public FileConfiguration getDatabaseConfig() {
        if (databaseConfig == null) {
            reloadDatabaseConfig();
        }
        return databaseConfig;
    }

    public void reloadPluginConfig() {
        reloadConfig();
        getLogger().info("SimpleChat configuration reloaded!");
    }


    private void registerCommands() {
        try {
            // Register commands using Paper's CommandAPI
            new HideChatCommand(chatManager, this).register(this);
            new IgnoreCommand(chatManager, this).register(this);
            new UnignoreCommand(chatManager, this).register(this);
            new IgnoreListCommand(chatManager, this).register(this);
            new UnignoreAllCommand(chatManager, this).register(this);
            new MessageCommand(chatManager, this).register(this);
            new ReplyCommand(chatManager, this).register(this);
            new SimpleChatCommand(chatManager, this).register(this);

            getLogger().info("All commands registered successfully!");
        } catch (Exception e) {
            getLogger().severe("Failed to register commands: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Clear player cache when they leave
        chatManager.clearPlayerCache(playerId);
    }

    @Override
    public void onDisable() {
        // Cleanup database connections
        if (chatManager != null) {
            chatManager.cleanup();
        }
        getLogger().info("SimpleChat has been disabled!");
    }

    public ChatManager getChatManager() {
        return chatManager;
    }
}