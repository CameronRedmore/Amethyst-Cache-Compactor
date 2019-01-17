package uk.co.amethystdevelopment.acc.commands;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import uk.co.amethystdevelopment.acc.backend.ACC_NetworkUtils;

public class Command_search implements TabExecutor
{

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if(!(sender instanceof Player))
        {
            return true;
        }
        Player player = (Player) sender;
        if(args.length < 1)
        {
            return false;
        }
        String search = StringUtils.join(args, " ");
        ACC_NetworkUtils.searches.put(player.getName(), search);
        if(!search.equalsIgnoreCase("off"))
        {
            player.sendMessage(ChatColor.AQUA + "ACC Digitisation Complexes will now only display items with names matching: \"" + search + "\".");
        }
        else
        {
            player.sendMessage(ChatColor.AQUA + "Now displaying all items in ACC Digitisation Complexes.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
    {
        return new ArrayList<>();
    }
}
