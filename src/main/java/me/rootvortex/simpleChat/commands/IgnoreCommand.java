package me.rootvortex.simpleChat.commands;

import me.rootvortex.simpleChat.SimpleChat;
import me.rootvortex.simpleChat.managers.ChatManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class IgnoreCommand extends SimpleCommand {

    public IgnoreCommand(ChatManager chatManager, SimpleChat plugin) {
        super(chatManager, plugin); // ADD PLUGIN PARAMETER
    }

    @Override
    protected String getCommandName() {
        return "ignore";
    }

    @Override
    protected String getDescription() {
        return "Ignore a specific player's messages";
    }

    @Override
    protected String getUsage() {
        return "/ignore <player>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("§cUsage: /ignore <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("§cPlayer '" + args[0] + "' is not online!");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage("§cYou cannot ignore yourself!");
            return true;
        }

        // Check if player is already ignoring the target
        if (chatManager.isIgnoring(player, target)) {
            player.sendMessage("§cYou are already ignoring " + target.getName() + "!");
            return true;
        }

        if (chatManager.ignorePlayer(player, target)) {
            player.sendMessage("§6You are now ignoring " + target.getName() + ". You won't see their messages.");
        } else {
            player.sendMessage("§cFailed to ignore " + target.getName() + "!");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1 && sender instanceof Player) {
            String partialName = args[0].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player != sender && player.getName().toLowerCase().startsWith(partialName)) {
                    completions.add(player.getName());
                }
            }
        }

        return completions;
    }
}