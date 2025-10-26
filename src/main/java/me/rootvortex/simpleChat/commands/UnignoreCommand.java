package me.rootvortex.simpleChat.commands;

import me.rootvortex.simpleChat.SimpleChat;
import me.rootvortex.simpleChat.managers.ChatManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class UnignoreCommand extends SimpleCommand {

    public UnignoreCommand(ChatManager chatManager, SimpleChat plugin) {
        super(chatManager, plugin); // ADD PLUGIN PARAMETER
    }

    @Override
    protected String getCommandName() {
        return "unignore";
    }

    @Override
    protected String getDescription() {
        return "Stop ignoring a player";
    }

    @Override
    protected String getUsage() {
        return "/unignore <player>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("§cUsage: /unignore <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("§cPlayer '" + args[0] + "' is not online!");
            return true;
        }

        if (chatManager.unignorePlayer(player, target)) {
            player.sendMessage("§aYou are no longer ignoring " + target.getName() + ".");
        } else {
            player.sendMessage("§cYou are not ignoring " + target.getName() + "!");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1 && sender instanceof Player player) {
            String partialName = args[0].toLowerCase();
            for (String ignoredName : chatManager.getIgnoredPlayers(player)) {
                if (ignoredName.toLowerCase().startsWith(partialName)) {
                    completions.add(ignoredName);
                }
            }
        }

        return completions;
    }
}