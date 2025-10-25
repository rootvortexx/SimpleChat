package me.rootvortex.simpleChat.commands;

import me.rootvortex.simpleChat.managers.ChatManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ReplyCommand extends SimpleCommand {

    public ReplyCommand(ChatManager chatManager) {
        super(chatManager);
    }

    @Override
    protected String getCommandName() {
        return "reply";
    }

    @Override
    protected String getDescription() {
        return "Reply to last private message";
    }

    @Override
    protected String getUsage() {
        return "/reply <message>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("§cUsage: /reply <message>");
            return true;
        }

        Player target = chatManager.getLastMessenger(player);
        if (target == null) {
            player.sendMessage("§cYou have no one to reply to!");
            return true;
        }

        if (!target.isOnline()) {
            player.sendMessage("§cThat player is no longer online!");
            return true;
        }

        String message = String.join(" ", args);

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
        return new ArrayList<>();
    }
}