package uk.co.amethystdevelopment.acc.backend;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ACC_HTAD
{
    private World world;
    private int x;
    private int y;
    private int z;
    private boolean isCrafting;

    public ACC_HTAD(World world, int x, int y, int z)
    {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.isCrafting = false;
    }
    
    public ACC_HTAD()
    {
        this.isCrafting = true;
    }
    
    public void setLocation(Location location)
    {
        world = location.getWorld();
        x = location.getBlockX();
        y = location.getBlockY();
        z = location.getBlockZ();
        this.isCrafting = false;
    }
    
    public boolean isCrafting()
    {
        return isCrafting;
    }
    
    public Location getLocation()
    {
        return new Location(world, x, y, z);
    }
    
    public ItemStack toItemStack()
    {
        ItemStack stack = new ItemStack(Material.BREWING_STAND, 1);
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.BREWING_STAND);
        meta.setDisplayName(ChatColor.GREEN + "ACC Handheld Terminal Access Device");
        ArrayList<String> lore = new ArrayList<>();
        if(this.isCrafting)
        {
            lore.add(ChatColor.GOLD + "This HTAD is currently unlinked.");
        }
        else
        {
            lore.add(ChatColor.GOLD + "This HTAD is linked to the ACC Human Interface Terminal at");
            lore.add(ChatColor.BLUE.toString() + x + ChatColor.GOLD + ", " + ChatColor.BLUE + y + ChatColor.GOLD + ", " + ChatColor.BLUE + z);
            lore.add(ChatColor.GOLD + "in the world " + ChatColor.BLUE + world.getName() + ChatColor.GOLD + ".");
            lore.add(ChatColor.RED + world.getName() + ":" + x + ":" + y + ":" + z);
        }
        meta.setLore(lore);
        stack.setItemMeta(meta);
        return stack;
    }
    
    public Block getLinkedBlock()
    {
        return getLocation().getBlock();
    }
}
