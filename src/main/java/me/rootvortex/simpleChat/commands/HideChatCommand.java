package me.rootvortex.simpleChat.commands;

import me.rootvortex.simpleChat.SimpleChat;
import me.rootvortex.simpleChat.managers.ChatManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HideChatCommand extends SimpleCommand {

    public HideChatCommand(ChatManager chatManager, SimpleChat plugin) {
        super(chatManager, plugin);
    }

    @Override
    protected String getCommandName() {
        return "hidechat";
    }

    @Override
    protected String getDescription() {
        return "Toggle visibility of all chat messages";
    }

    @Override
    protected String getUsage() {
        return "/hidechat";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        boolean isNowHidden = chatManager.toggleGlobalChat(player);

        if (isNowHidden) {
            player.sendMessage("§6Chat is now hidden! You won't see any chat messages.");
        } else {
            player.sendMessage("§aChat is now visible! You can see all messages.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}