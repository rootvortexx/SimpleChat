package me.rootvortex.simpleChat.commands;

import me.rootvortex.simpleChat.SimpleChat;
import me.rootvortex.simpleChat.managers.ChatManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class IgnoreListCommand extends SimpleCommand {

    public IgnoreListCommand(ChatManager chatManager, SimpleChat plugin) {
        super(chatManager, plugin); // ADD PLUGIN PARAMETER
    }

    @Override
    protected String getCommandName() {
        return "ignorelist";
    }

    @Override
    protected String getDescription() {
        return "Show list of ignored players";
    }

    @Override
    protected String getUsage() {
        return "/ignorelist";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        int ignoreCount = chatManager.getIgnoreCount(player);

        if (ignoreCount == 0) {
            player.sendMessage("§eYou are not ignoring any players.");
            return true;
        }

        player.sendMessage("§6Ignored players (§e" + ignoreCount + "§6):");
        for (String ignoredName : chatManager.getIgnoredPlayers(player)) {
            player.sendMessage("§7- " + ignoredName);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}