package uk.co.amethystdevelopment.acc.backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import me.mrCookieSlime.sensibletoolbox.blocks.machines.BatteryBox;
import me.mrCookieSlime.sensibletoolbox.core.storage.LocationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import static org.bukkit.block.BlockFace.DOWN;
import static org.bukkit.block.BlockFace.EAST;
import static org.bukkit.block.BlockFace.EAST_NORTH_EAST;
import static org.bukkit.block.BlockFace.EAST_SOUTH_EAST;
import static org.bukkit.block.BlockFace.NORTH;
import static org.bukkit.block.BlockFace.NORTH_EAST;
import static org.bukkit.block.BlockFace.NORTH_NORTH_EAST;
import static org.bukkit.block.BlockFace.NORTH_NORTH_WEST;
import static org.bukkit.block.BlockFace.NORTH_WEST;
import static org.bukkit.block.BlockFace.SELF;
import static org.bukkit.block.BlockFace.SOUTH;
import static org.bukkit.block.BlockFace.SOUTH_EAST;
import static org.bukkit.block.BlockFace.SOUTH_SOUTH_EAST;
import static org.bukkit.block.BlockFace.SOUTH_SOUTH_WEST;
import static org.bukkit.block.BlockFace.SOUTH_WEST;
import static org.bukkit.block.BlockFace.UP;
import static org.bukkit.block.BlockFace.WEST;
import static org.bukkit.block.BlockFace.WEST_NORTH_WEST;
import static org.bukkit.block.BlockFace.WEST_SOUTH_WEST;
import org.bukkit.block.Dropper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import uk.co.amethystdevelopment.acc.AmethystCacheCompactor;
import static uk.co.amethystdevelopment.acc.backend.ACC_DatabaseInterface.toStorageUnit;

public class ACC_NetworkUtils
{

    public ArrayList<Block> cables = new ArrayList<>();
    public static HashMap<String, String> searches = new HashMap<>();

    public ArrayList<ACC_DIR> getUnits(Block block)
    {
        ArrayList<ACC_DIR> units = new ArrayList<>();
        cables.clear();
        getCables(block);
        ArrayList<String> usedids = new ArrayList<>();
        for(ACC_DIR unit : runOn(block))
        {
            if(!usedids.contains(unit.getId()))
            {
                usedids.add(unit.getId());
                units.add(unit);
            }
        }
        for(Block cable : cables)
        {
            for(ACC_DIR unit : runOn(cable))
            {
                if(!usedids.contains(unit.getId()))
                {
                    usedids.add(unit.getId());
                    units.add(unit);
                }
            }
        }
        List<BlockFace> faces = Arrays.asList(BlockFace.SELF, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN);

        for(BlockFace face : faces)
        {
            if(block.getRelative(face).getType() == Material.CHEST)
            {
                cables.clear();
                getCables(block.getRelative(face));
                for(Block cable : cables)
                {
                    for(ACC_DIR unit : runOn(cable))
                    {
                        if(!usedids.contains(unit.getId()))
                        {
                            usedids.add(unit.getId());
                            units.add(unit);
                        }
                    }
                }
                for(ACC_DIR unit : runOn(block.getRelative(face)))
                {
                    if(!usedids.contains(unit.getId()))
                    {
                        usedids.add(unit.getId());
                        units.add(unit);
                    }
                }
            }
        }
        return units;
    }

    public ArrayList<ACC_DIR> runOn(final Block block)
    {
        ArrayList<ACC_DIR> units = new ArrayList<>();
        List<BlockFace> faces = Arrays.asList(SELF, NORTH, EAST, SOUTH, WEST, UP, DOWN);
        ArrayList<String> usedids = new ArrayList<>();
        for(BlockFace face : faces)
        {
            if(block.getRelative(face).getType() == Material.DROPPER)
            {
                Dropper dropper = (Dropper) block.getRelative(face).getState();
                for(ItemStack stack : dropper.getInventory().getContents())
                {
                    if(isStorageUnit(stack) && !usedids.contains(toStorageUnit(stack).getId()))
                    {
                        usedids.add(toStorageUnit(stack).getId());
                        units.add(toStorageUnit(stack));
                    }
                }
            }
        }
        return units;
    }

    public void getCables(Block block)
    {
        for(BlockFace f : new BlockFace[]
        {
            BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN
        })
        {
            Block b = block.getRelative(f);
            if((b.getType().name().toUpperCase().contains("GLASS")) && !cables.contains(b))
            {
                cables.add(b);
                getCables(b);
            }
        }
    }

    public ArrayList<BatteryBox> getBatteries(final Block block)
    {
        ArrayList<BlockFace> faces = new ArrayList<>();
        faces.addAll(Arrays.asList(BlockFace.SELF, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN));
        ArrayList<BatteryBox> boxes = new ArrayList<>();
        cables.clear();
        getCables(block);
        for(Block block2 : cables)
        {
            for(BlockFace face : faces)
            {
                if(LocationManager.getManager().get(block2.getRelative(face).getLocation()) != null && LocationManager.getManager().get(block2.getRelative(face).getLocation()) instanceof BatteryBox)
                {
                    BatteryBox box = (BatteryBox) LocationManager.getManager().get(block2.getRelative(face).getLocation());
                    boxes.add(box);
                }
            }
        }
        return boxes;
    }

    public boolean managePower(Block block)
    {
        cables.clear();
        getCables(block);
        double power = (AmethystCacheCompactor.pluginConfig.getConfig().getDouble("energy.disc") * getUnits(block).size()) + (AmethystCacheCompactor.pluginConfig.getConfig().getDouble("energy.cable") * cables.size());
        for(BatteryBox box : getBatteries(block))
        {
            if(box.getCharge() <= power)
            {
                power -= box.getCharge();
                box.setCharge(0);
            }
            if(box.getCharge() > power)
            {
                box.setCharge(box.getCharge() - power);
                power = 0;
            }
        }
        return power == 0;
    }

    public static boolean isStorageUnit(ItemStack item)
    {
        if(item == null)
        {
            return false;
        }
        Material type = item.getType();
        ItemMeta meta = item.getItemMeta();
        if(meta == null || meta.getDisplayName() == null || meta.getLore() == null)
        {
            return false;
        }
        String name = meta.getDisplayName();
        List<String> lore = meta.getLore();
        if(lore.size() != 3)
        {
            return false;
        }
        return type == Material.MUSIC_DISC_MALL && name.contains(ChatColor.GREEN + "ACC Digitised Item Repository") && lore.get(0).contains(ChatColor.GOLD + " item types stored.") && lore.get(1).contains(ChatColor.GOLD + " items stored.");
    }

    public static boolean isRemoteAccess(ItemStack stack)
    {
        return stack.getType() == Material.BREWING_STAND && stack.hasItemMeta() && stack.getItemMeta().hasLore() && (stack.getItemMeta().getLore().get(0).contains(ChatColor.GOLD + "This HTAD is linked to the ACC Human Interface Terminal at") || stack.getItemMeta().getLore().get(0).contains(ChatColor.GOLD + "This HTAD is currently unlinked."));
    }

    public static ACC_HTAD toRemoteAccess(ItemStack stack)
    {
        if(!isRemoteAccess(stack))
        {
            return null;
        }
        if(stack.getItemMeta().getLore().get(0).contains(ChatColor.GOLD + "This HTAD is currently unlinked."))
        {
            return new ACC_HTAD();
        }
        String[] split = ChatColor.stripColor(stack.getItemMeta().getLore().get(3)).split(":");
        World world = Bukkit.getWorld(split[0]);
        int x = Integer.parseInt(split[1]);
        int y = Integer.parseInt(split[2]);
        int z = Integer.parseInt(split[3]);
        if(world == null)
        {
            return null;
        }
        return new ACC_HTAD(world, x, y, z);
    }

    public static BlockFace getOpposite(BlockFace face)
    {
        switch(face)
        {
            case UP:
                return DOWN;
            case DOWN:
                return UP;
            case NORTH:
                return SOUTH;
            case SOUTH:
                return NORTH;
            case EAST:
                return WEST;
            case WEST:
                return EAST;
            case NORTH_EAST:
                return SOUTH_WEST;
            case SOUTH_EAST:
                return NORTH_WEST;
            case SOUTH_WEST:
                return NORTH_EAST;
            case NORTH_WEST:
                return SOUTH_EAST;
            case NORTH_NORTH_EAST:
                return SOUTH_SOUTH_WEST;
            case EAST_NORTH_EAST:
                return WEST_SOUTH_WEST;
            case EAST_SOUTH_EAST:
                return WEST_NORTH_WEST;
            case SOUTH_SOUTH_EAST:
                return NORTH_NORTH_WEST;
            case SOUTH_SOUTH_WEST:
                return NORTH_NORTH_EAST;
            case WEST_SOUTH_WEST:
                return EAST_NORTH_EAST;
            case WEST_NORTH_WEST:
                return EAST_SOUTH_EAST;
            case NORTH_NORTH_WEST:
                return SOUTH_SOUTH_EAST;
            default:
                return SELF;
        }
    }
}
