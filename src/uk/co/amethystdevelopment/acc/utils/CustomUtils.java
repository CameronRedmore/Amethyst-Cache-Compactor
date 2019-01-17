/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.amethystdevelopment.acc.utils;

import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author cameronredmore
 */
public class CustomUtils {
    public static String getName(ItemStack stack)
    {
        if(stack.hasItemMeta())
        {
            ItemMeta meta = stack.getItemMeta();
            if(meta.hasDisplayName())
            {
                return meta.getDisplayName();
            }
            if(meta.hasLocalizedName())
            {
                return meta.getLocalizedName();
            }
        }
        return WordUtils.capitalize(stack.getType().name().replace("_", " ").toLowerCase());
    }
}
