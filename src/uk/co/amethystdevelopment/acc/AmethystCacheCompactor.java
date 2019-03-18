package uk.co.amethystdevelopment.acc;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import uk.co.amethystdevelopment.acc.backend.ACC_DIR;
import uk.co.amethystdevelopment.acc.backend.ACC_DatabaseInterface;
import uk.co.amethystdevelopment.acc.backend.ACC_DigitisedItem;
import uk.co.amethystdevelopment.acc.backend.ACC_HTAD;
import uk.co.amethystdevelopment.acc.commands.Command_acc;
import uk.co.amethystdevelopment.acc.commands.Command_createdir;
import uk.co.amethystdevelopment.acc.commands.Command_search;
import uk.co.amethystdevelopment.acc.utils.MetricsLite;

public class AmethystCacheCompactor extends JavaPlugin
{

    public static AmethystCacheCompactor plugin;
    public static ACC_Config pluginConfig;
    public static ShapedRecipe basic;
    public static ShapedRecipe advanced;
    public static ShapedRecipe expert;
    public static ShapedRecipe remote;
    public static boolean usePower = false;

    @Override
    public void onEnable()
    {
        Bukkit.getLogger().info("ACC has been enabled!");
        try
        {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        }
        catch(IOException e)
        {
            // Failed to submit the stats :-(
        }
        plugin = this;
        pluginConfig = new ACC_Config(plugin, "config.yml");
        pluginConfig.saveDefaultConfig();
        
        basic = new ShapedRecipe(new ACC_DIR(16, 16 * 128 * 4, new ArrayList<ACC_DigitisedItem>(), "CraftingItem").getItem());
        basic.shape("XYX", "YZY", "XYX");
        basic.setIngredient('X', Material.GLASS);
        basic.setIngredient('Y', Material.OBSIDIAN);
        basic.setIngredient('Z', Material.IRON_INGOT);
        Bukkit.addRecipe(basic);

        advanced = new ShapedRecipe(new ACC_DIR(32, 32 * 256 * 4, new ArrayList<ACC_DigitisedItem>(), "CraftingItem").getItem());
        advanced.shape("XYX", "YZY", "XYX");
        advanced.setIngredient('X', Material.GLASS);
        advanced.setIngredient('Y', Material.OBSIDIAN);
        advanced.setIngredient('Z', Material.GOLD_INGOT);
        Bukkit.addRecipe(advanced);

        expert = new ShapedRecipe(new ACC_DIR(64, 64 * 512 * 4, new ArrayList<ACC_DigitisedItem>(), "CraftingItem").getItem());
        expert.shape("XYX", "YZY", "XYX");
        expert.setIngredient('X', Material.GLASS);
        expert.setIngredient('Y', Material.OBSIDIAN);
        expert.setIngredient('Z', Material.DIAMOND);
        Bukkit.addRecipe(expert);

        remote = new ShapedRecipe(new ACC_HTAD().toItemStack());
        remote.shape("DCD", "BAB", "DDD");
        remote.setIngredient('A', Material.BREWING_STAND);
        remote.setIngredient('B', Material.STONE_BUTTON);
        remote.setIngredient('C', Material.REDSTONE);
        remote.setIngredient('D', Material.IRON_INGOT);
        Bukkit.addRecipe(remote);

        this.getCommand("acc").setExecutor(new Command_acc());
        this.getCommand("createdir").setExecutor(new Command_createdir());
        this.getCommand("search").setExecutor(new Command_search());
        Bukkit.getPluginManager().registerEvents(new ACC_Listener(), plugin);
        
        if(pluginConfig.getConfig().contains("energy.consume") && pluginConfig.getConfig().getBoolean("energy.consume"))
        {
            Plugin stb = Bukkit.getPluginManager().getPlugin("SensibleToolbox");
            if(stb != null)
            {
                usePower = true;
            }
        }
        
        try
        {
            ACC_DatabaseInterface.prepareDatabase();
        }
        catch(SQLException ex)
        {
            Logger.getLogger(AmethystCacheCompactor.class.getName()).log(Level.SEVERE, null, ex);
        }
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ACC_DatabaseInterface.getConnection().commit();
                }
                catch (SQLException ex)
                {
                    Logger.getLogger(AmethystCacheCompactor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.runTaskTimerAsynchronously(this, 0, 200);
    }
    
    @Override
    public void onDisable()
    {
        ACC_DatabaseInterface.closeConnection(ACC_DatabaseInterface.getConnection());
    }
    
    @Override
    public File getFile()
    {
        return super.getFile();
    }

    public static <T> List<List<T>> chopped(List<T> list, final int L)
    {
        List<List<T>> parts = new ArrayList<>();
        final int N = list.size();
        for(int i = 0; i < N; i += L)
        {
            parts.add(new ArrayList<>(
                    list.subList(i, Math.min(N, i + L)))
            );
        }
        return parts;
    }
}
