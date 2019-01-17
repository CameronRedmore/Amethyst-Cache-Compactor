package uk.co.amethystdevelopment.acc.backend;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import uk.co.amethystdevelopment.acc.AmethystCacheCompactor;
import static uk.co.amethystdevelopment.acc.backend.ACC_DatabaseInterface.getConnection;
import static uk.co.amethystdevelopment.acc.backend.ACC_NetworkUtils.isStorageUnit;

public class ACC_DatabaseInterface
{

    private static Connection connection;
    private static boolean mysql = false;

    public static Connection getConnection()
    {
        FileConfiguration config = AmethystCacheCompactor.pluginConfig.getConfig();
        if(connection != null)
        {
            try
            {
                connection.setAutoCommit(false);
                return connection;
            }
            catch(SQLException ex)
            {
                Logger.getLogger(ACC_DatabaseInterface.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else if(config.getBoolean("database.mysql"))
        {
            try
            {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                connection = DriverManager.getConnection("jdbc:mysql://" + config.getString("database.url") + ":" + config.getString("database.port") + "/" + config.getString("database.database"), config.getString("database.name"), config.getString("database.password"));
                connection.setAutoCommit(false);
                mysql = true;
                return connection;
            }
            catch(ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex)
            {
                AmethystCacheCompactor.plugin.getLogger().log(Level.SEVERE, ex.getMessage());
                AmethystCacheCompactor.plugin.getLogger().log(Level.SEVERE, "Failed to get the connection with the MySQL database.");
                Bukkit.getServer().getPluginManager().disablePlugin(AmethystCacheCompactor.plugin);
            }
        }
        else
        {
            try
            {
                connection = DriverManager.getConnection("jdbc:sqlite:" + AmethystCacheCompactor.plugin.getDataFolder().getAbsolutePath() + "/AmethystCacheCompactor.db");
                connection.setAutoCommit(false);
                return connection;
            }
            catch(SQLException ex)
            {
                AmethystCacheCompactor.plugin.getLogger().log(Level.SEVERE, ex.getMessage());
                AmethystCacheCompactor.plugin.getLogger().log(Level.SEVERE, "Failed to get the connection with the database.db file.");
                Bukkit.getServer().getPluginManager().disablePlugin(AmethystCacheCompactor.plugin);
            }
        }
        return connection;
    }

    public static ACC_DIR toStorageUnit(ItemStack item)
    {
        if(!isStorageUnit(item))
        {
            return null;
        }
        String uuid = ChatColor.stripColor(item.getItemMeta().getLore().get(2));
        return toStorageUnit(uuid);
    }

    public static ACC_DIR toStorageUnit(String uuid)
    {
        try
        {
            Connection con = getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT * FROM `" + uuid + "`");
            ResultSet set = statement.executeQuery();
            ArrayList<ACC_DigitisedItem> items = new ArrayList<>();
            while(set.next())
            {
                Material material = Material.getMaterial(set.getString("MATERIAL"));
                byte data = (byte) set.getInt("DATA");
                byte[] array = set.getBytes("ITEMMETA");
                BukkitObjectInputStream stream = new BukkitObjectInputStream(new ByteArrayInputStream(array));
                ItemMeta meta = (ItemMeta) stream.readObject();
                int amount = set.getInt("AMOUNT");
                boolean splash = set.getBoolean("SPLASH");
                String id = set.getString("UUID");
                items.add(new ACC_DigitisedItem(material, data, meta, amount, splash, id));
            }
            statement = con.prepareStatement("SELECT * FROM `DISCINFO` WHERE UUID = ?");
            statement.setString(1, uuid);
            set = statement.executeQuery();
            int maxItems = set.getInt("MAXITEMS");
            int maxTypes = set.getInt("MAXTYPES");
            return new ACC_DIR(maxTypes, maxItems, items, uuid);
        }
        catch(SQLException | IOException | ClassNotFoundException ex)
        {
            Logger.getLogger(ACC_DatabaseInterface.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static void save(ACC_DIR unit)
    {
        try
        {
            Connection con = getConnection();
            PreparedStatement statement = con.prepareStatement("CREATE TABLE IF NOT EXISTS `" + unit.getId() + "` ("
                    + "ID INTEGER PRIMARY KEY,"
                    + "UUID TEXT,"
                    + "MATERIAL TEXT,"
                    + "DATA INTEGER,"
                    + "ITEMMETA BLOB,"
                    + "SPLASH BOOLEAN,"
                    + "AMOUNT INTEGER)");
            statement.executeUpdate();
            statement = con.prepareStatement("DELETE FROM `" + unit.getId() + "`");
            statement.executeUpdate();
            for(ACC_DigitisedItem item : unit.items)
            {
                if(item.getAmount() != 0)
                {
                    statement = con.prepareStatement("INSERT INTO `" + unit.getId() + "` (UUID, MATERIAL, DATA, ITEMMETA, SPLASH, AMOUNT) values (?, ?, ?, ?, ?, ?)");
                    statement.setString(1, item.getId());
                    statement.setString(2, item.getMaterial().name());
                    statement.setInt(3, item.getData());
                    ByteArrayOutputStream meta = new ByteArrayOutputStream();
                    BukkitObjectOutputStream stream = new BukkitObjectOutputStream(meta);
                    stream.writeObject(item.getMeta());
                    statement.setBytes(4, meta.toByteArray());
                    statement.setBoolean(5, item.isSplash());
                    statement.setInt(6, item.getAmount());
                    statement.executeUpdate();
                }
            }
            statement = con.prepareStatement((isMySQL() ? "INSERT IGNORE INTO `DISCINFO` (UUID, MAXITEMS, MAXTYPES) VALUES(?, ?, ?)" : "INSERT OR IGNORE INTO `DISCINFO` (UUID, MAXITEMS, MAXTYPES) VALUES(?, ?, ?)"));
            statement.setString(1, unit.getId());
            statement.setInt(2, unit.maxItems);
            statement.setInt(3, unit.maxItemTypes);
            statement.executeUpdate();
        }
        catch(SQLException | IOException ex)
        {
            Logger.getLogger(ACC_DatabaseInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void prepareDatabase() throws SQLException
    {
        PreparedStatement statement = getConnection().prepareStatement(
                (isMySQL()
                        ? "CREATE TABLE IF NOT EXISTS `DISCINFO` ("
                        + "ID INTEGER PRIMARY KEY AUTO_INCREMENT,"
                        + "UUID VARCHAR(36) UNIQUE,"
                        + "MAXITEMS INTEGER,"
                        + "MAXTYPES INTEGER)"
                        : "CREATE TABLE IF NOT EXISTS `DISCINFO` ("
                        + "ID INTEGER PRIMARY KEY,"
                        + "UUID VARCHAR(36) UNIQUE,"
                        + "MAXITEMS INTEGER,"
                        + "MAXTYPES INTEGER)"));
        statement.executeUpdate();
    }

    public static void closeConnection(Connection connection)
    {
        try
        {
            if(connection != null)
            {
                connection.close();
            }
        }
        catch(SQLException e)
        {
            System.err.println(e);
        }
    }

    public static boolean isMySQL()
    {
        return mysql;
    }
}
