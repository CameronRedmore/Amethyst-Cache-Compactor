package uk.co.amethystdevelopment.acc.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import uk.co.amethystdevelopment.acc.ACC_Listener;
import uk.co.amethystdevelopment.acc.backend.ACC_DIR;
import static uk.co.amethystdevelopment.acc.backend.ACC_DatabaseInterface.toStorageUnit;

public class Command_createdir implements TabExecutor
{

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if(!(sender instanceof Player))
        {
            return true;
        }
        Player player = (Player) sender;
        if(args.length == 1)
        {
            ACC_DIR unit = toStorageUnit(args[0]);
            if(unit != null)
            {
                player.sendMessage(ChatColor.GREEN + "Giving you an ADIR with the uuid of " + ChatColor.RED + args[0] + ChatColor.GREEN + ".");
                ACC_Listener.addOrDrop(player, unit.getItem());
            }
            else
            {
                player.sendMessage(ChatColor.RED + "A disc could not be found with that UUID.");
            }
            return true;
        }
        if(args.length != 2)
        {
            return false;
        }
        try
        {
            int maxTypes = Integer.valueOf(args[0]);
            int maxTotal = Integer.valueOf(args[1]);
            if(maxTypes < 1 || maxTotal < 1)
            {
                player.sendMessage(ChatColor.RED + "Both arguments must be a positive Integer.");
                return false;
            }
            ACC_DIR unit = new ACC_DIR(maxTypes, maxTotal);
            player.sendMessage(ChatColor.GREEN + "Giving you a new ACC Digitised Item Repository with your custom values.");
            ACC_Listener.addOrDrop(player, unit.getItem());
            return true;
        }
        catch(NumberFormatException ex)
        {
            player.sendMessage(ChatColor.RED + "Both arguments must be a positive Integer.");
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
    {
        ArrayList<String> strings = new ArrayList<>();
        for(int i = 4; i <= 16; i++)
        {
            strings.add(String.valueOf((int) Math.floor(Math.pow(2, i))));
        }
        return strings;
    }

}
