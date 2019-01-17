package uk.co.amethystdevelopment.acc.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import static org.bukkit.event.inventory.InventoryType.CRAFTING;
import static org.bukkit.event.inventory.InventoryType.CREATIVE;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import uk.co.amethystdevelopment.acc.ACC_Listener;
import uk.co.amethystdevelopment.acc.AmethystCacheCompactor;
import uk.co.amethystdevelopment.acc.utils.CustomUtils;

public class ACC_InventoryHandler
{

    Block block;
    List<List<ItemStack>> invs;
    public int page;
    public Inventory inv;

    public ACC_InventoryHandler(Block block)
    {
        this.block = block;
        this.page = 0;
    }

    public ACC_InventoryHandler(List<List<ItemStack>> invs, Block block)
    {
        this.block = block;
        this.invs = invs;
        this.page = 0;
    }

    public void openPage(Player player)
    {
        if(!player.hasPermission("acc.view") && !player.isOp())
        {
            return;
        }
        if(AmethystCacheCompactor.usePower)
        {
            if(!new ACC_NetworkUtils().managePower(block))
            {
                player.sendMessage(ChatColor.RED + "Unfortunately, this ACC Digitisation Complex is out of power, please consider upgrading the power system for this network!");
                player.closeInventory();
                return;
            }
        }
        Inventory inv = Bukkit.createInventory(null, 54);
        ItemStack[] stacks = new ItemStack[54];
        invs = new ArrayList<>();
        ArrayList<ItemStack> allItems = new ArrayList<>();
        ArrayList<String> usedids = new ArrayList<>();
        for(ACC_DIR unit : new ACC_NetworkUtils().getUnits(block))
        {
            if(!usedids.contains(unit.getId()))
            {
                for(ItemStack stack : unit.getItems())
                {
                    if(ACC_NetworkUtils.searches.containsKey(player.getName()) && !ACC_NetworkUtils.searches.get(player.getName()).equalsIgnoreCase("off"))
                    {
                        if(!StringUtils.containsIgnoreCase(ChatColor.stripColor(CustomUtils.getName(stack)), (ACC_NetworkUtils.searches.get(player.getName()))))
                        {
                            continue;
                        }
                    }
                    allItems.add(stack);
                }
                usedids.add(unit.getId());
            }
        }
        if(ACC_NetworkUtils.searches.containsKey(player.getName()) && !ACC_NetworkUtils.searches.get(player.getName()).equalsIgnoreCase("off"))
        {
            player.sendMessage(ChatColor.AQUA + "Showing all items that match the name of: " + ACC_NetworkUtils.searches.get(player.getName()));
        }
        Collections.sort(allItems, new ACC_ItemStackComparator());
        invs = AmethystCacheCompactor.chopped(allItems, 53);
        if(page > invs.size() - 1)
        {
            page = 0;
        }
        if(!invs.isEmpty())
        {
            invs.get(page).toArray(stacks);
        }
        ItemStack next = new ItemStack(Material.WHITE_WOOL, 1);
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.WHITE_WOOL);
        meta.setDisplayName(ChatColor.GOLD + "Next Page");
        next.setItemMeta(meta);
        stacks[53] = next;
        inv.setContents(stacks);
        if(player.getOpenInventory().getType() == CRAFTING || player.getOpenInventory().getType() == CREATIVE)
        {
            player.openInventory(inv);
        }
        else
        {
            player.getOpenInventory().getTopInventory().setContents(stacks);
        }
        this.inv = inv;
        ACC_Listener.handlers.put(player, this);
    }

    public int addItem(ItemStack item, Player player, int amount)
    {
        if(player != null && !player.hasPermission("acc.add") && !player.isOp())
        {
            return amount;
        }
        int leftover = amount;
        for(ACC_DIR unit : new ACC_NetworkUtils().getUnits(block))
        {
            leftover = unit.addItem(item, leftover);
        }
        if(leftover > 0 && player != null)
        {
            player.sendMessage(ChatColor.RED + "The DIRs that this HIT is connected to cannot hold all the items you tried to insert, have you reached the limit for item types?");
        }
        return leftover;
    }

    public int takeItem(ItemStack item, Player player, int amount)
    {
        if(!player.hasPermission("acc.take") && !player.isOp())
        {
            return amount;
        }
        int leftover = amount;
        for(ACC_DIR unit : new ACC_NetworkUtils().getUnits(block))
        {
            int taken = unit.takeItem(item, leftover);
            leftover = taken;
        }
        return leftover;
    }

}
