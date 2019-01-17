package uk.co.amethystdevelopment.acc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.scheduler.BukkitRunnable;
import uk.co.amethystdevelopment.acc.backend.ACC_DIR;
import static uk.co.amethystdevelopment.acc.backend.ACC_DatabaseInterface.toStorageUnit;
import uk.co.amethystdevelopment.acc.backend.ACC_HTAD;
import uk.co.amethystdevelopment.acc.backend.ACC_InventoryHandler;
import uk.co.amethystdevelopment.acc.backend.ACC_ItemStackComparator;
import uk.co.amethystdevelopment.acc.backend.ACC_NetworkUtils;
import static uk.co.amethystdevelopment.acc.backend.ACC_NetworkUtils.isStorageUnit;
import uk.co.amethystdevelopment.acc.utils.RecipeUtil;

public class ACC_Listener implements Listener
{

    public static HashMap<Player, ACC_InventoryHandler> handlers = new HashMap<>();

    @EventHandler
    public void onHopperMoveItem(final InventoryMoveItemEvent event)
    {
        if (event.getSource().getType() == InventoryType.HOPPER)
        {
            if (event.getDestination().getType() == InventoryType.CHEST)
            {
                if (ACC_NetworkUtils.isRemoteAccess(event.getItem()))
                {
                    Location location;
                    if (event.getDestination().getHolder() instanceof Chest)
                    {
                        location = ((Chest) event.getDestination().getHolder()).getLocation();
                    }
                    else if (event.getDestination().getHolder() instanceof DoubleChest)
                    {
                        location = ((DoubleChest) event.getDestination().getHolder()).getLocation();
                    }
                    else
                    {
                        return;
                    }
                    ACC_HTAD remote = ACC_NetworkUtils.toRemoteAccess(event.getItem());
                    remote.setLocation(location);
                    event.setCancelled(true);
                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            event.getSource().clear(event.getSource().first(event.getItem()));
                        }
                    }.runTaskLaterAsynchronously(AmethystCacheCompactor.plugin, 1L);
                    event.getDestination().addItem(remote.toItemStack());
                }
                else
                {
                    Location location;
                    if (event.getDestination().getHolder() instanceof Chest)
                    {
                        location = ((Chest) event.getDestination().getHolder()).getLocation();
                    }
                    else if (event.getDestination().getHolder() instanceof DoubleChest)
                    {
                        location = ((DoubleChest) event.getDestination().getHolder()).getLocation();
                    }
                    else
                    {
                        return;
                    }
                    Block chest = location.getBlock();
                    ArrayList<ACC_DIR> units = new ACC_NetworkUtils().getUnits(chest);
                    if (units.size() > 0)
                    {
                        ArrayList<ItemStack> totalItems = new ArrayList<>();
                        for (ACC_DIR unit : units)
                        {
                            for (ItemStack stack : unit.getItems())
                            {
                                totalItems.add(stack);
                            }
                        }
                        Collections.sort(totalItems, new ACC_ItemStackComparator());
                        List<List<ItemStack>> chopped = AmethystCacheCompactor.chopped(totalItems, 25);
                        ACC_InventoryHandler handler = new ACC_InventoryHandler(chopped, chest);
                        int i = handler.addItem(event.getItem(), null, event.getItem().getAmount());
                        if (i == 0)
                        {
                            //new BukkitRunnable()
                            //{
                            //@Override
                            //public void run()
                            //{
                            if (event.getDestination().first(event.getItem()) != -1 && event.getDestination().getItem(event.getDestination().first(event.getItem())).getAmount() - 1 == 0)
                            {
                                event.getDestination().clear(event.getDestination().first(event.getItem()));
                            }
                            else if (event.getDestination().first(event.getItem()) != -1)
                            {
                                event.getDestination().getItem(event.getDestination().first(event.getItem())).setAmount(event.getDestination().getItem(event.getDestination().first(event.getItem())).getAmount() - 1);
                            }
                            //}
                            //}.runTaskLaterAsynchronously(AmethystCacheCompactor.plugin, 1L);
                        }
                    }
                }
            }
            if (event.getDestination().getType() == InventoryType.DROPPER)
            {
                if (ACC_NetworkUtils.isStorageUnit(event.getItem()))
                {
                    for (ItemStack stack : event.getDestination().getContents())
                    {
                        if (stack != null)
                        {
                            if (ACC_NetworkUtils.isStorageUnit(stack))
                            {
                                if (toStorageUnit(event.getItem()).maxItemTypes == toStorageUnit(stack).maxItemTypes)
                                {
                                    stack.setItemMeta(event.getItem().getItemMeta().clone());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event)
    {
        if (event.getCursor() != null && ACC_NetworkUtils.isStorageUnit(event.getCursor()))
        {
            ItemStack unit = toStorageUnit(event.getCursor()).getItem();
            ItemStack stack = event.getCursor();
            stack.setType(unit.getType());
            stack.setData(unit.getData());
            stack.setItemMeta(unit.getItemMeta());
        }
        if (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName() && event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Next Page") && event.getWhoClicked() instanceof Player)
        {
            if (handlers.containsKey((Player) event.getWhoClicked()))
            {
                ACC_InventoryHandler handler = handlers.get((Player) event.getWhoClicked());
                handler.page += 1;
                handler.openPage((Player) event.getWhoClicked());
                event.setCancelled(true);
            }
        }
        else if ((event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD || event.getAction() == InventoryAction.HOTBAR_SWAP || event.getAction() == InventoryAction.DROP_ONE_SLOT || event.getAction() == InventoryAction.DROP_ONE_CURSOR || event.getAction() == InventoryAction.DROP_ALL_SLOT || event.getAction() == InventoryAction.DROP_ALL_CURSOR || event.getAction() == InventoryAction.UNKNOWN) && handlers.containsKey((Player) event.getWhoClicked()))
        {
            if (handlers.containsKey((Player) event.getWhoClicked()))
            {
                ACC_InventoryHandler handler = handlers.get((Player) event.getWhoClicked());
                handler.openPage((Player) event.getWhoClicked());
                event.setCancelled(true);
            }
        }
        else if (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasLore() && event.getCurrentItem().getItemMeta().getLore().get(event.getCurrentItem().getItemMeta().getLore().size() - 1).contains(ChatColor.GOLD + " stored in this DIR.") && event.getWhoClicked() instanceof Player)
        {
            if (handlers.containsKey((Player) event.getWhoClicked()))
            {
                ACC_InventoryHandler handler = handlers.get((Player) event.getWhoClicked());
                if (event.isLeftClick())
                {
                    int i = handler.takeItem(event.getCurrentItem(), (Player) event.getWhoClicked(), event.getCurrentItem().getMaxStackSize());
                    if (i != event.getCurrentItem().getMaxStackSize())
                    {
                        ItemStack item = new ItemStack(event.getCurrentItem().getType(), event.getCurrentItem().getMaxStackSize() - i);
                        item.setDurability(event.getCurrentItem().getDurability());;
                        ItemMeta meta = event.getCurrentItem().getItemMeta();
                        ArrayList<String> lore = new ArrayList<>();
                        for (String oldlore : meta.getLore())
                        {
                            if (meta.getLore().indexOf(oldlore) != meta.getLore().size() - 1)
                            {
                                lore.add(oldlore);
                            }
                        }
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                        if (event.getCurrentItem().getType() == Material.POTION && Potion.fromItemStack(event.getCurrentItem()).isSplash())
                        {
                            Potion potion = Potion.fromItemStack(item);
                            potion.setSplash(true);
                            potion.apply(item);
                        }
                        addOrDrop((Player) event.getWhoClicked(), item);
                        handler.openPage((Player) event.getWhoClicked());
                        event.setCancelled(true);
                    }
                }
                if (event.isRightClick())
                {
                    int i = handler.takeItem(event.getCurrentItem(), (Player) event.getWhoClicked(), 1);
                    if (i != 1)
                    {
                        ItemStack item = new ItemStack(event.getCurrentItem().getType(), 1);
                        item.setDurability(event.getCurrentItem().getDurability());
                        ItemMeta meta = event.getCurrentItem().getItemMeta();
                        ArrayList<String> lore = new ArrayList<>();
                        for (String oldlore : meta.getLore())
                        {
                            if (meta.getLore().indexOf(oldlore) != meta.getLore().size() - 1)
                            {
                                lore.add(oldlore);
                            }
                        }
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                        if (event.getCurrentItem().getType() == Material.POTION && Potion.fromItemStack(event.getCurrentItem()).isSplash())
                        {
                            Potion potion = Potion.fromItemStack(item);
                            potion.setSplash(true);
                            potion.apply(item);
                        }
                        addOrDrop((Player) event.getWhoClicked(), item);
                        handler.openPage((Player) event.getWhoClicked());
                        event.setCancelled(true);
                    }
                }
            }
        }
        else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && handlers.containsKey((Player) event.getWhoClicked()))
        {
            if (event.getClickedInventory() instanceof PlayerInventory)
            {
                final ACC_InventoryHandler handler = handlers.get((Player) event.getWhoClicked());
                int i = handler.addItem(event.getCurrentItem(), (Player) event.getWhoClicked(), event.getCurrentItem().getAmount());
                ItemStack stack = new ItemStack(event.getCurrentItem().getType());
                stack.setItemMeta(event.getCurrentItem().getItemMeta());
                stack.setDurability(event.getCurrentItem().getDurability());
                stack.setAmount(i);
                if (event.getCurrentItem().getType() == Material.POTION && Potion.fromItemStack(event.getCurrentItem()).isSplash())
                {
                    Potion.fromItemStack(stack).setSplash(true);
                }
                if (i != 0)
                {
                    addOrDrop((Player) event.getWhoClicked(), stack);
                }
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        event.setCurrentItem(new ItemStack(Material.AIR, 1));
                        handler.openPage((Player) event.getWhoClicked());
                    }
                }.runTaskLaterAsynchronously(AmethystCacheCompactor.plugin, 1L);
            }
        }
        else if (event.getAction() == InventoryAction.PLACE_ALL && handlers.containsKey((Player) event.getWhoClicked()) && !(event.getClickedInventory() instanceof PlayerInventory))
        {
            final ACC_InventoryHandler handler = handlers.get((Player) event.getWhoClicked());
            int i = handler.addItem(event.getCursor(), (Player) event.getWhoClicked(), event.getCursor().getAmount());
            if (i == 0)
            {
                event.getCursor().setType(Material.AIR);
            }
            else
            {
                event.setCancelled(true);
                event.getCursor().setAmount(i);
            }
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    event.setCurrentItem(new ItemStack(Material.AIR, 1));
                    handler.openPage((Player) event.getWhoClicked());
                }
            }.runTaskLaterAsynchronously(AmethystCacheCompactor.plugin, 1L);
        }
        else if (event.getAction() == InventoryAction.PLACE_ONE && handlers.containsKey((Player) event.getWhoClicked()) && !(event.getClickedInventory() instanceof PlayerInventory))
        {
            final ACC_InventoryHandler handler = handlers.get((Player) event.getWhoClicked());
            int i = handler.addItem(event.getCursor(), (Player) event.getWhoClicked(), 1);
            if (i == 0)
            {
                if (event.getCursor().getAmount() - 1 == 0)
                {
                    event.getCursor().setType(Material.AIR);
                }
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        event.setCurrentItem(new ItemStack(Material.AIR, 1));
                        handler.openPage((Player) event.getWhoClicked());
                    }
                }.runTaskLaterAsynchronously(AmethystCacheCompactor.plugin, 1L);
            }
            else
            {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event
    )
    {
        if (event.getWhoClicked() instanceof Player && handlers.containsKey((Player) event.getWhoClicked()))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event)
    {
        if (event.getPlayer() instanceof Player && event.getInventory().getType() == InventoryType.CHEST && event.getInventory().getHolder() != null)
        {
            Location location;
            if (event.getInventory().getHolder() instanceof Chest)
            {
                location = ((Chest) event.getInventory().getHolder()).getLocation();
            }
            else if (event.getInventory().getHolder() instanceof DoubleChest)
            {
                location = ((DoubleChest) event.getInventory().getHolder()).getLocation();
            }
            else
            {
                return;
            }
            Block chest = location.getBlock();
            ArrayList<ACC_DIR> units = (new ACC_NetworkUtils()).getUnits(chest);
            if (units.size() > 0)
            {
                handlers.put((Player) event.getPlayer(), new ACC_InventoryHandler(chest));
                event.setCancelled(true);
                handlers.get((Player) event.getPlayer()).openPage((Player) event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event)
    {
        if (event.getPlayer() instanceof Player && handlers.containsKey((Player) event.getPlayer()))
        {
            handlers.remove((Player) event.getPlayer());
        }
    }

    @EventHandler
    public void onCraftEvent(CraftItemEvent event)
    {
        if (RecipeUtil.areEqual(event.getRecipe(), AmethystCacheCompactor.basic))
        {
            if (event.isShiftClick())
            {
                ((Player) event.getWhoClicked()).sendMessage(ChatColor.RED + "You cannot craft multiple DIRs at once, please craft each disc individually.");
                event.setCancelled(true);
                return;
            }
            if (event.getWhoClicked() instanceof Player && !((Player) event.getWhoClicked()).isOp() && !((Player) event.getWhoClicked()).hasPermission("acc.craft.basic"))
            {
                event.setCancelled(true);
                return;
            }
            event.setCurrentItem(new ACC_DIR(16, 16 * 128 * 4).getItem());
        }
        if (RecipeUtil.areEqual(event.getRecipe(), AmethystCacheCompactor.advanced))
        {
            if (event.isShiftClick())
            {
                ((Player) event.getWhoClicked()).sendMessage(ChatColor.RED + "You cannot craft multiple DIRs at once, please craft each disc individually.");
                event.setCancelled(true);
                return;
            }
            if (event.getWhoClicked() instanceof Player && !((Player) event.getWhoClicked()).isOp() && !((Player) event.getWhoClicked()).hasPermission("acc.craft.advanced"))
            {
                event.setCancelled(true);
                return;
            }
            event.setCurrentItem(new ACC_DIR(32, 32 * 256 * 4).getItem());
        }
        if (RecipeUtil.areEqual(event.getRecipe(), AmethystCacheCompactor.expert))
        {
            if (event.isShiftClick())
            {
                ((Player) event.getWhoClicked()).sendMessage(ChatColor.RED + "You cannot craft multiple DIRs at once, please craft each disc individually.");
                event.setCancelled(true);
                return;
            }
            if (event.getWhoClicked() instanceof Player && !((Player) event.getWhoClicked()).isOp() && !((Player) event.getWhoClicked()).hasPermission("acc.craft.expert"))
            {
                event.setCancelled(true);
                return;
            }
            event.setCurrentItem(new ACC_DIR(64, 64 * 512 * 4).getItem());
        }
        if (RecipeUtil.areEqual(event.getRecipe(), AmethystCacheCompactor.remote))
        {
            if (event.getWhoClicked() instanceof Player && !((Player) event.getWhoClicked()).isOp() && !((Player) event.getWhoClicked()).hasPermission("acc.craft.remote"))
            {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public static void onDispenserDispense(BlockDispenseEvent event)
    {
        if (isStorageUnit(event.getItem()))
        {
            int types = Integer.parseInt(ChatColor.stripColor(event.getItem().getItemMeta().getLore().get(0).split("/ ")[1].replaceAll(ChatColor.GOLD + " item types stored.", "")));
            int items = Integer.parseInt(ChatColor.stripColor(event.getItem().getItemMeta().getLore().get(1).split("/ ")[1].replaceAll(ChatColor.GOLD + " items stored.", "").replaceAll(",", "")));
            event.setItem(new ACC_DIR(types, items).getItem());
        }
    }

    @EventHandler
    public static void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.hasItem() && ACC_NetworkUtils.isRemoteAccess(event.getItem()))
        {
            if (event.getItem().getItemMeta().getLore().get(0).contains("unlinked."))
            {
                return;
            }
            ArrayList<ACC_DIR> units = new ArrayList<>();
            Block chest = ACC_NetworkUtils.toRemoteAccess(event.getItem()).getLinkedBlock();
            if (chest == null || chest.getType() == Material.AIR)
            {
                event.getPlayer().sendMessage(ChatColor.RED + "The ACC Human Interface Terminal has been removed from its location.");
                return;
            }
            units = new ACC_NetworkUtils().getUnits(chest);
            if (units.size() > 0)
            {
                handlers.put((Player) event.getPlayer(), new ACC_InventoryHandler(chest));
                event.setCancelled(true);
                handlers.get((Player) event.getPlayer()).openPage((Player) event.getPlayer());
            }
            else
            {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "No DIRs were found connected to this HIT.");
            }
        }
    }

    @EventHandler
    public static void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        if (event.getRightClicked() instanceof ItemFrame)
        {
            ItemFrame frame = (ItemFrame) event.getRightClicked();
            Player player = (Player) event.getPlayer();
            Block block = frame.getLocation().getBlock().getRelative(frame.getAttachedFace());
            if ((block.getType().name().toUpperCase().contains("GLASS")))
            {
                ACC_InventoryHandler handler = new ACC_InventoryHandler(block);
                if (player.isSneaking())
                {
                    if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR)
                    {
                        int i = handler.addItem(player.getItemInHand(), player, player.getItemInHand().getAmount());
                        if (i == 0)
                        {
                            player.getInventory().setItemInHand(null);
                        }
                        else
                        {
                            ItemStack stack = player.getItemInHand();
                            stack.setAmount(i);
                            player.getInventory().setItemInHand(stack);
                        }
                        event.setCancelled(true);
                    }
                }
                else
                {
                    int i = handler.addItem(player.getItemInHand(), player, 1);
                    if (i == 0)
                    {
                        int newAmount = player.getItemInHand().getAmount() - 1;
                        if (newAmount != 0)
                        {
                            ItemStack stack = player.getItemInHand();
                            stack.setAmount(newAmount);
                            player.getInventory().setItemInHand(stack);
                        }
                        else
                        {
                            player.getInventory().setItemInHand(null);
                        }
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public static void onEntityDamageByEntity(EntityDamageByEntityEvent event)
    {
        if (event.getEntity() instanceof ItemFrame && event.getDamager() instanceof Player)
        {
            ItemFrame frame = (ItemFrame) event.getEntity();
            Player player = (Player) event.getDamager();
            Block block = frame.getLocation().getBlock().getRelative(frame.getAttachedFace());
            if ((block.getType().name().toUpperCase().contains("GLASS")))
            {
                ACC_InventoryHandler handler = new ACC_InventoryHandler(block);
                if (player.isSneaking())
                {
                    int i = handler.takeItem(frame.getItem(), player, frame.getItem().getMaxStackSize());
                    if (i != frame.getItem().getMaxStackSize())
                    {
                        ItemStack item = new ItemStack(frame.getItem().getType(), frame.getItem().getMaxStackSize() - i);
                        item.setDurability(frame.getItem().getDurability());;
                        ItemMeta meta = frame.getItem().getItemMeta();
                        if (meta != null)
                        {
                            if (meta.getLore() != null)
                            {
                                ArrayList<String> lore = new ArrayList<>();
                                for (String oldlore : meta.getLore())
                                {
                                    if (meta.getLore().indexOf(oldlore) != meta.getLore().size() - 1)
                                    {
                                        lore.add(oldlore);
                                    }
                                }
                                meta.setLore(lore);
                            }
                            item.setItemMeta(meta);
                            if (frame.getItem().getType() == Material.POTION && Potion.fromItemStack(frame.getItem()).isSplash())
                            {
                                Potion potion = Potion.fromItemStack(item);
                                potion.setSplash(true);
                                potion.apply(item);
                            }
                        }
                        addOrDrop(player, item);
                        event.setCancelled(true);
                        return;
                    }
                }
                else
                {
                    int i = handler.takeItem(frame.getItem(), player, 1);
                    if (i != 1)
                    {
                        ItemStack item = new ItemStack(frame.getItem().getType(), 1);
                        item.setDurability(frame.getItem().getDurability());
                        ItemMeta meta = frame.getItem().getItemMeta();
                        if (meta != null)
                        {
                            if (meta.getLore() != null)
                            {
                                ArrayList<String> lore = new ArrayList<>();
                                for (String oldlore : meta.getLore())
                                {
                                    if (meta.getLore().indexOf(oldlore) != meta.getLore().size() - 1)
                                    {
                                        lore.add(oldlore);
                                    }
                                }
                                meta.setLore(lore);
                            }

                            item.setItemMeta(meta);

                            if (frame.getItem().getType() == Material.POTION && Potion.fromItemStack(frame.getItem()).isSplash())
                            {
                                Potion potion = Potion.fromItemStack(item);
                                potion.setSplash(true);
                                potion.apply(item);
                            }
                        }
                        addOrDrop(player, item);
                        event.setCancelled(true);
                        return;
                    }
                }
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "The connected network does not have any of the displayed item present.");
            }
        }
    }

    public static void addOrDrop(Player player, ItemStack item)
    {
        int i = player.getInventory().addItem(item).size();
        if (i != 0)
        {
            ItemStack item2 = new ItemStack(item.getType(), i);
            item2.setItemMeta(item.getItemMeta());
            item2.setDurability(item.getDurability());
            player.getLocation().getWorld().dropItemNaturally(player.getLocation(), item2);
        }
    }
}
