package net.sacredlabyrinth.phaed.simpleclans.executors;

import net.sacredlabyrinth.phaed.simpleclans.ChatBlock;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.Helper;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class AllyCommandExecutor implements CommandExecutor
{
    SimpleClans plugin;

    public AllyCommandExecutor()
    {
        plugin = SimpleClans.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings)
    {
        Player player = (Player) commandSender;

        if (!plugin.getSettingsManager().isAllyChatEnable())
        {
            return false;
        }
        
        if (strings.length == 0)
        {
            return false;
        }
        ClanPlayer cp = plugin.getClanManager().getClanPlayer(player);

        if (cp == null)
        {
            return false;
        }

        String subCommand = strings[0];

        if (subCommand.equals(plugin.getLang("join")) && strings.length == 1)
        {
            cp.setChannel(ClanPlayer.Channel.ALLY);
            plugin.getStorageManager().updateClanPlayer(cp);
            ChatBlock.sendMessage(player, ChatColor.AQUA + "You have joined ally chat");
        }
        else if (subCommand.equals(plugin.getLang("leave")) && strings.length == 1)
        {
            cp.setChannel(ClanPlayer.Channel.NONE);
            plugin.getStorageManager().updateClanPlayer(cp);
            ChatBlock.sendMessage(player, ChatColor.AQUA + "You have left ally chat");
        }
        else if (subCommand.equals(plugin.getLang("mute")) && strings.length == 1)
        {
            if (!cp.isMutedAlly())
            {
                cp.setMutedAlly(true);
                ChatBlock.sendMessage(player, ChatColor.AQUA + "You have muted ally chat");
            }
            else
            {
                cp.setMutedAlly(false);
                ChatBlock.sendMessage(player, ChatColor.AQUA + "You have unmuted ally chat");
            }
        }
        else
        {
            String code = "" + ChatColor.AQUA + ChatColor.WHITE + ChatColor.AQUA + ChatColor.BLACK;
            String message = code + plugin.getSettingsManager().getAllyChatBracketColor() + plugin.getSettingsManager().getAllyChatTagBracketLeft() + plugin.getSettingsManager().getAllyChatTagColor() + plugin.getSettingsManager().getCommandAlly() + plugin.getSettingsManager().getAllyChatBracketColor() + plugin.getSettingsManager().getAllyChatTagBracketRight() + " " + plugin.getSettingsManager().getAllyChatNameColor() + plugin.getSettingsManager().getAllyChatPlayerBracketLeft() + player.getName() + plugin.getSettingsManager().getAllyChatPlayerBracketRight() + " " + plugin.getSettingsManager().getAllyChatMessageColor() + Helper.toMessage(strings);
            if (plugin.getSettingsManager().isAllyChatFilter()) {
                message = Helper.filterMsg(message);
            }
            String eyeMessage = code + plugin.getSettingsManager().getAllyChatBracketColor() + plugin.getSettingsManager().getAllyChatTagBracketLeft() + plugin.getSettingsManager().getAllyChatTagColor() + plugin.getSettingsManager().getCommandAlly() + plugin.getSettingsManager().getAllyChatBracketColor() + plugin.getSettingsManager().getAllyChatTagBracketRight() + " " + plugin.getSettingsManager().getAllyChatNameColor() + plugin.getSettingsManager().getAllyChatPlayerBracketLeft() + player.getName() + plugin.getSettingsManager().getAllyChatPlayerBracketRight() + " " + plugin.getSettingsManager().getAllyChatMessageColor() + Helper.toMessage(strings);

            plugin.getServer().getConsoleSender().sendMessage(eyeMessage);

            Player self = cp.toPlayer();
            ChatBlock.sendMessage(self, message);

            Set<ClanPlayer> allies = cp.getClan().getAllAllyMembers();
            allies.addAll(cp.getClan().getMembers());

            for (ClanPlayer ally : allies)
            {
                if (ally.isMutedAlly())
                {
                    continue;
                }
                Player member = ally.toPlayer();

                if (player.getUniqueId().equals(ally.getUniqueId()))
                {
                    continue;
                }

                ChatBlock.sendMessage(member, message);
            }

            sendToAllSeeing(eyeMessage, allies);
        }
        return false;
    }

    public void sendToAllSeeing(String msg, Set<ClanPlayer> allies)
    {
        Collection<Player> players = Helper.getOnlinePlayers();

        for (Player player : players)
        {
            if (plugin.getPermissionsManager().has(player, "simpleclans.admin.all-seeing-eye"))
            {
                boolean alreadySent = false;

                ClanPlayer cp = plugin.getClanManager().getClanPlayer(player);

                if (cp != null && cp.isMutedAlly())
                {
                    continue;
                }

                for (ClanPlayer cpp : allies)
                {
                    if (cpp.getName().equalsIgnoreCase(player.getName()))
                    {
                        alreadySent = true;
                    }
                }

                if (!alreadySent)
                {
                    ChatBlock.sendMessage(player, ChatColor.DARK_GRAY + Helper.stripColors(msg));
                }
            }
        }
    }
}
