package me.rootvortex.simpleChat.commands;

import me.rootvortex.simpleChat.SimpleChat;
import me.rootvortex.simpleChat.managers.ChatManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class UnignoreAllCommand extends SimpleCommand {

    public UnignoreAllCommand(ChatManager chatManager, SimpleChat plugin) {
        super(chatManager, plugin); // ADD PLUGIN PARAMETER
    }

    @Override
    protected String getCommandName() {
        return "unignoreall";
    }

    @Override
    protected String getDescription() {
        return "Unignore all players";
    }

    @Override
    protected String getUsage() {
        return "/unignoreall";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (chatManager.unignoreAllPlayers(player)) {
            player.sendMessage("§aYou are no longer ignoring all players.");
        } else {
            player.sendMessage("§cYou weren't ignoring anyone!");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}