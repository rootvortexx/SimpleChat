package me.rootvortex.simpleChat.commands;

import me.rootvortex.simpleChat.SimpleChat;
import me.rootvortex.simpleChat.managers.ChatManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

public abstract class SimpleCommand implements TabExecutor {
    protected final ChatManager chatManager;
    protected final SimpleChat plugin; // ADD THIS

    // UPDATE CONSTRUCTOR TO ACCEPT PLUGIN
    public SimpleCommand(ChatManager chatManager, SimpleChat plugin) {
        this.chatManager = chatManager;
        this.plugin = plugin;
    }

    // ADD THIS METHOD FOR GETTING MESSAGES
    protected String getMessage(String path) {
        String message = plugin.getConfig().getString("messages." + path, "&cMessage not found: " + path);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public void register(Plugin plugin) {
        try {
            // Create command manually using reflection
            PluginCommand command = createCommand(getCommandName(), plugin);
            if (command != null) {
                command.setExecutor(this);
                command.setTabCompleter(this);
                command.setDescription(getDescription());
                command.setUsage(getUsage());

                // Register the command
                getCommandMap().register(plugin.getName(), command);
                plugin.getLogger().info("Registered command: " + getCommandName());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register command " + getCommandName() + ": " + e.getMessage());
        }
    }

    private PluginCommand createCommand(String name, Plugin plugin) {
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            return constructor.newInstance(name, plugin);
        } catch (Exception e) {
            return null;
        }
    }

    private CommandMap getCommandMap() {
        try {
            // Use direct server access instead of SimplePluginManager
            Field commandMapField = org.bukkit.Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            return (CommandMap) commandMapField.get(org.bukkit.Bukkit.getServer());
        } catch (Exception e) {
            org.bukkit.Bukkit.getLogger().warning("Failed to get CommandMap: " + e.getMessage());
            return null;
        }
    }

    protected abstract String getCommandName();
    protected abstract String getDescription();
    protected abstract String getUsage();

    @Override
    public abstract boolean onCommand(CommandSender sender, Command command, String label, String[] args);

    @Override
    public abstract List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args);
}