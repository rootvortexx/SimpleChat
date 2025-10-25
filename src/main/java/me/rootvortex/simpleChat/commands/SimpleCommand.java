package me.rootvortex.simpleChat.commands;

import me.rootvortex.simpleChat.managers.ChatManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

public abstract class SimpleCommand implements TabExecutor {
    protected final ChatManager chatManager;

    public SimpleCommand(ChatManager chatManager) {
        this.chatManager = chatManager;
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
                Bukkit.getLogger().info("Registered command: " + getCommandName());
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to register command " + getCommandName() + ": " + e.getMessage());
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
            if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
                Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                field.setAccessible(true);
                return (CommandMap) field.get(Bukkit.getPluginManager());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected abstract String getCommandName();
    protected abstract String getDescription();
    protected abstract String getUsage();

    @Override
    public abstract boolean onCommand(CommandSender sender, Command command, String label, String[] args);

    @Override
    public abstract List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args);
}