package uk.co.amethystdevelopment.acc.backend;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;

public final class ACC_DIR
{

    public final ArrayList<ACC_DigitisedItem> items;
    public final int maxItemTypes;
    public final int maxItems;
    public final String uuid;

    public ACC_DIR(int maxItemTypes, int maxItems, ArrayList<ACC_DigitisedItem> items, UUID uuid)
    {
        this.items = items;
        this.maxItemTypes = maxItemTypes;
        this.maxItems = maxItems;
        this.uuid = uuid.toString();
        save();
    }

    public ACC_DIR(int maxItemTypes, int maxItems, ArrayList<ACC_DigitisedItem> items, String uuid)
    {
        this.items = items;
        this.maxItemTypes = maxItemTypes;
        this.maxItems = maxItems;
        this.uuid = uuid;
    }

    public ACC_DIR(int maxItemTypes, int maxItems)
    {
        this(maxItemTypes, maxItems, new ArrayList<ACC_DigitisedItem>(), UUID.randomUUID());
        this.save();
    }

    public void save()
    {
        ACC_DatabaseInterface.save(this);
    }

    public int addItem(ItemStack stack, int amount)
    {
        if(amount == 0)
        {
            return 0;
        }
        ItemMeta meta = stack.getItemMeta();
        if(meta.hasLore())
        {
            ArrayList<String> newlore = new ArrayList<>();
            for(String oldlore : meta.getLore())
            {
                if(!oldlore.contains(ChatColor.GOLD + " stored in this DIR."))
                {
                    newlore.add(oldlore);
                }
            }
            meta.setLore(newlore);
            stack.setItemMeta(meta);
        }
        boolean splash = stack.getType() == Material.POTION && Potion.fromItemStack(stack).isSplash();
        ACC_DigitisedItem stored = new ACC_DigitisedItem(stack.getType(), stack.getData().getData(), meta, 0, splash);
        int compare = howManyStored();
        if(hasItem(stored))
        {
            stored = getStored(stored);
            if(compare + amount > maxItems)
            {
                items.remove(stored);
                stored.addItems(maxItems - compare);
                items.add(stored);
                amount = amount - (maxItems - compare);
            }
            else
            {
                items.remove(stored);
                stored.addItems(amount);
                items.add(stored);
                amount = 0;
            }
        }
        else if(items.size() < maxItemTypes)
        {
            if(compare + amount > maxItems)
            {
                stored.addItems(maxItems - compare);
                items.add(stored);
                amount = amount - (maxItems - compare);
            }
            else
            {
                stored.addItems(amount);
                items.add(stored);
                amount = 0;
            }
        }
        save();
        return amount;
    }

    public int takeItem(ItemStack stack, int amount)
    {
        ItemMeta meta;
        if(stack.getItemMeta() != null)
        {
            meta = stack.getItemMeta().clone();
        }
        else
        {
            meta = Bukkit.getServer().getItemFactory().getItemMeta(stack.getType());
        }
        if(meta != null)
        {
            ArrayList<String> newLore = new ArrayList<>();
            if(meta.getLore() != null)
            {
                List<String> lore = meta.getLore();
                for(String loreline : lore)
                {
                    if(!lore.contains(ChatColor.GOLD + " stored in this DIR."))
                    {
                        newLore.add(loreline);
                    }
                }
                meta.setLore(newLore);
            }
            stack.setItemMeta(meta);
        }
        if(amount == 0)
        {
            return amount;
        }
        boolean splash = stack.getType() == Material.POTION && Potion.fromItemStack(stack).isSplash();
        ACC_DigitisedItem stored = new ACC_DigitisedItem(stack.getType(), stack.getData().getData(), meta, 0, splash);
        if(!hasItem(stored))
        {
            return amount;
        }
        stored = getStored(stored);
        if(stored.getAmount() <= amount)
        {
            items.remove(stored);
            int number = stored.getAmount();
            stored.setAmount(0);
            items.add(stored);
            save();
            return amount - number;
        }
        items.remove(stored);
        stored.removeItems(amount);
        items.add(stored);
        save();
        return 0;
    }

    public Integer howManyStored()
    {
        int stored = 0;
        for(ACC_DigitisedItem item : items)
        {
            stored += item.getAmount();
        }
        return stored;
    }

    public ItemStack getItem()
    {
        ItemStack item = new ItemStack(Material.MUSIC_DISC_MALL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "ACC Digitised Item Repository");
        meta.setLore(Arrays.asList(ChatColor.BLUE + commas(items.size()) + ChatColor.GOLD + " / " + ChatColor.BLUE + commas(maxItemTypes) + ChatColor.GOLD + " item types stored.", ChatColor.BLUE + commas(howManyStored()) + ChatColor.GOLD + " / " + ChatColor.BLUE + commas(maxItems) + ChatColor.GOLD + " items stored.", ChatColor.RED + uuid.toString()));
        item.setItemMeta(meta);
        return item;
    }

    public boolean hasItem(ACC_DigitisedItem item)
    {
        for(ACC_DigitisedItem stored : items)
        {
            if(stored.getMaterial() == item.getMaterial() && stored.getData() == item.getData() && stored.getMeta().equals(item.getMeta()) && ((stored.isSplash() && item.isSplash()) || (!stored.isSplash() && !item.isSplash())))
            {
                return true;
            }
        }
        return false;
    }

    public ACC_DigitisedItem getStored(ACC_DigitisedItem item)
    {
        for(ACC_DigitisedItem stored : items)
        {
            if(stored.getMaterial() == item.getMaterial() && stored.getData() == item.getData() && stored.getMeta().equals(item.getMeta()) && ((stored.isSplash() && item.isSplash()) || (!stored.isSplash() && !item.isSplash())))
            {
                return stored;
            }
        }
        return null;
    }

    public String getId()
    {
        return this.uuid;
    }

    public ArrayList<ItemStack> getItems()
    {
        ArrayList<ItemStack> newItems = new ArrayList<>();
        if(!items.isEmpty())
        {
            for(ACC_DigitisedItem stored : items)
            {
                ItemStack stack = new ItemStack(stored.getMaterial(), 1);
                stack.setDurability(stored.getData());
                ItemMeta meta = stored.getMeta().clone();
                ArrayList<String> lore = new ArrayList<>();
                if(meta.getLore() != null)
                {
                    for(String newlore : meta.getLore())
                    {
                        lore.add(newlore);
                    }
                }
                lore.add(ChatColor.GREEN + commas(stored.getAmount()) + ChatColor.GOLD + " stored in this DIR.");
                meta.setLore(lore);
                stack.setItemMeta(meta);
                if(stored.isSplash())
                {
                    Potion potion = Potion.fromItemStack(stack);
                    potion.setSplash(true);
                    potion.apply(stack);
                }
                newItems.add(stack);
            }
        }
        return newItems;
    }

    public String commas(int number)
    {
        return NumberFormat.getIntegerInstance().format(number);
    }
}
