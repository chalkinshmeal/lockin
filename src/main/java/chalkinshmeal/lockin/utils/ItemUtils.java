package chalkinshmeal.lockin.utils;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

public class ItemUtils {
    public static void setCompassTarget(ItemStack compass, Entity entity) {
        // Get the CompassMeta
        CompassMeta compassMeta = (CompassMeta) compass.getItemMeta();
        if (compassMeta == null) return; // Fallback if meta is null

        // Set the target location
        Location target = entity.getLocation();
        compassMeta.setLodestone(target);
        compassMeta.setLodestoneTracked(false); // Disable tracking for a lodestone

        // Apply the CompassMeta back to the ItemStack
        compass.setItemMeta(compassMeta);
    }
}
