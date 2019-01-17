package uk.co.amethystdevelopment.acc.backend;

import java.util.ArrayList;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;

public class ACC_DigitisedItem
{

    private final Material material;
    private final Byte data;
    private final ItemMeta meta;
    private final String uuid;
    private final boolean splash;
    private int amount;

    public ACC_DigitisedItem(Material material, Byte data, ItemMeta meta, int amount, boolean splash)
    {
        this.material = material;
        this.data = data;
        this.meta = meta;
        this.amount = amount;
        this.splash = splash;
        this.uuid = UUID.randomUUID().toString();
    }
    
    public ACC_DigitisedItem(Material material, Byte data, ItemMeta meta, int amount, boolean splash, String uuid)
    {
        this.material = material;
        this.data = data;
        this.meta = meta;
        this.amount = amount;
        this.splash = splash;
        this.uuid = uuid;
    }
    
    public String getId()
    {
        return this.uuid;
    }

    public int getAmount()
    {
        return this.amount;
    }

    public ItemMeta getMeta()
    {
        ArrayList<String> lore = new ArrayList<>();
        if(meta.hasLore())
        {
            for(String oldlore : meta.getLore())
            {
                if(!oldlore.contains(ChatColor.GOLD + " stored in this DIR."))
                {
                    lore.add(oldlore);
                }
            }
        }
        meta.setLore(lore);
        return this.meta;
    }

    public Material getMaterial()
    {
        return this.material;
    }

    public byte getData()
    {
        return this.data;
    }

    public void addItems(int amount)
    {
        this.amount += amount;
    }

    public void removeItems(int amount)
    {
        this.amount = this.amount - amount;
    }

    public void setAmount(int amount)
    {
        this.amount = amount;
    }
    
    public boolean isSplash()
    {
        return this.splash;
    }

    public ItemStack toItemStack(int amount)
    {
        ItemStack item = new ItemStack(material, amount);
        item.setItemMeta(meta);
        if(isSplash())
        {
            Potion potion = Potion.fromItemStack(item);
            potion.setSplash(true);
            potion.apply(item);
        }
        return item;
    }
}
