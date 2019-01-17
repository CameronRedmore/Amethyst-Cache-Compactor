package uk.co.amethystdevelopment.acc.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import uk.co.amethystdevelopment.acc.AmethystCacheCompactor;

public class Command_acc implements TabExecutor
{

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        sender.sendMessage(ChatColor.RED + "Welcome to Amethyst Cache Compactor by " + ChatColor.BLUE + "Amethyst Development" + ChatColor.RED + "!");
        sender.sendMessage(ChatColor.GOLD + "This server is running version " + ChatColor.BLUE + AmethystCacheCompactor.plugin.getDescription().getVersion() + ChatColor.GOLD + " of CamStorage.");
        sender.sendMessage(ChatColor.GOLD + "If you wish to discover how to use Amethyst Cache Compactor, please view the BukkitDev post here: " + ChatColor.BLUE + "http://dev.bukkit.org/bukkit-plugins/camstorage/" + ChatColor.GOLD + ".");
        sender.sendMessage(ChatColor.GOLD + "If you have made a video or some over kind of resource relating to Amethyst Cache Compactor and you would like it to be placed on the thread, send an email to " + ChatColor.BLUE + "admin@amethystdevelopment.co.uk" + ChatColor.GOLD + ".");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
    {
        return new ArrayList<>();
    }

}
