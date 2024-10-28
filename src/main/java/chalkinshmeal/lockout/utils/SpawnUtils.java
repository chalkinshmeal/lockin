package chalkinshmeal.lockout.utils;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.meta.FireworkMeta;

public class SpawnUtils {
    public static void spawnFirework(Location loc, Color color, int power) {
        Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK_ROCKET, CreatureSpawnEvent.SpawnReason.CUSTOM);
        FireworkMeta fwm = fw.getFireworkMeta();

        fwm.setPower(power);
        fwm.addEffect(FireworkEffect.builder()
            .withColor(color)
            .build());

        fw.setFireworkMeta(fwm);
        fw.detonate();
    }

    public static void spawnExplosion(Location loc, float power) {
        loc.getWorld().createExplosion(loc, power, false, false);
    }
}
