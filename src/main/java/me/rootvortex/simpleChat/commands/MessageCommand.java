package me.rootvortex.simpleChat.commands;

import me.rootvortex.simpleChat.managers.ChatManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MessageCommand extends SimpleCommand {

    public MessageCommand(ChatManager chatManager) {
        super(chatManager);
    }

    @Override
    protected String getCommandName() {
        return "msg";
    }

    @Override
    protected String getDescription() {
        return "Send a private message";
    }

    @Override
    protected String getUsage() {
        return "/msg <player> <message>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /msg <player> <message>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("§cPlayer '" + args[0] + "' is not online!");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage("§cYou cannot message yourself!");
            return true;
        }

        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        // Store last messenger for reply command
        chatManager.setLastMessenger(target, player);

        // Send to sender
        player.sendMessage("§7[§6me §7-> §e" + target.getName() + "§7] §f" + message);

        // Send to target
        target.sendMessage("§7[§e" + player.getName() + " §7-> §6me§7] §f" + message);

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