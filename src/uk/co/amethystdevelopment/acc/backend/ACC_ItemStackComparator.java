package uk.co.amethystdevelopment.acc.backend;

import java.util.Comparator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import uk.co.amethystdevelopment.acc.utils.CustomUtils;

public class ACC_ItemStackComparator implements Comparator<ItemStack>
{

    @Override
    public int compare(ItemStack stack1, ItemStack stack2)
    {
        int i = ChatColor.stripColor(CustomUtils.getName(stack1)).compareTo(ChatColor.stripColor(CustomUtils.getName(stack2)));
        if(i == 0)
        {
            i = Integer.compare(stack1.getDurability(), stack2.getDurability());
        }
        if(i == 0)
        {
            if(stack1.getType() == Material.POTION && stack2.getType() == Material.POTION && Potion.fromItemStack(stack1) != null && Potion.fromItemStack(stack2) != null)
            {
                i = Boolean.compare(Potion.fromItemStack(stack1).isSplash(), Potion.fromItemStack(stack2).isSplash());
            }
        }
        if(i == 0)
        {
            if(stack1.hasItemMeta() && stack2.hasItemMeta() && stack1.getItemMeta().getLore() != null && stack2.getItemMeta().getLore() != null && stack1.getItemMeta().getLore().size() > 0 && stack2.getItemMeta().getLore().size() > 0)
            {
                i = stack1.getItemMeta().getLore().get(0).compareTo(stack2.getItemMeta().getLore().get(0));
            }
        }
        return i;
    }
}
