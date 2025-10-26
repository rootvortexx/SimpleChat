package me.rootvortex.simpleChat.commands;

import me.rootvortex.simpleChat.SimpleChat;
import me.rootvortex.simpleChat.managers.ChatManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class SimpleChatCommand extends SimpleCommand {
    private final SimpleChat plugin;

    public SimpleChatCommand(ChatManager chatManager, SimpleChat plugin) {
        super(chatManager, plugin); // THIS SHOULD WORK NOW
        this.plugin = plugin;
    }

    @Override
    protected String getCommandName() {
        return "simplechat";
    }

    @Override
    protected String getDescription() {
        return "Main SimpleChat command";
    }

    @Override
    protected String getUsage() {
        return "/simplechat reload";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§6SimpleChat Commands:");
            sender.sendMessage("§7/simplechat reload §8- §fReload configuration and database");
            sender.sendMessage("§7/hidechat §8- §fToggle chat visibility");
            sender.sendMessage("§7/ignore <player> §8- §fIgnore a player");
            sender.sendMessage("§7/msg <player> <message> §8- §fSend private message");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("simplechat.reload")) {
                sender.sendMessage(getMessage("no-permission")); // THIS SHOULD WORK NOW
                return true;
            }

            try {
                // Reload main config
                plugin.reloadPluginConfig(); // THIS SHOULD WORK NOW

                // Reload database config
                plugin.reloadDatabaseConfig();

                // Test database connection after reload
                if (chatManager.testDatabaseConnection()) {
                    sender.sendMessage(getMessage("reload-success")); // THIS SHOULD WORK NOW
                    plugin.getLogger().info("Configuration reloaded by " + sender.getName());
                } else {
                    sender.sendMessage("§cConfiguration reloaded but database connection failed!");
                }

            } catch (Exception e) {
                sender.sendMessage("§cFailed to reload configuration: " + e.getMessage());
                plugin.getLogger().severe("Failed to reload configuration: " + e.getMessage());
            }
            return true;
        }

        sender.sendMessage("§cUnknown subcommand. Use §7/simplechat §cfor help.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Only show "reload" if player has permission
            if (sender.hasPermission("simplechat.reload")) {
                completions.add("reload");
            }
        }

        return completions;
    }
}