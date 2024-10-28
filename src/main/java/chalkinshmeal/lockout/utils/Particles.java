package chalkinshmeal.lockout.utils;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class Particles {
    public static void spawnParticle(Location loc, Particle particle, int amount) { loc.getWorld().spawnParticle(particle, loc, amount); }
    public static void spawnParticle(Location loc, Particle particle, int amount, double velocity) { loc.getWorld().spawnParticle(particle, loc, amount, velocity, velocity, velocity, velocity); }
    public static void spawnParticle(Location loc, Color color, int amount) {
        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0F);
        loc.getWorld().spawnParticle(Particle.DUST, loc, amount, dustOptions);

    }

    public static void spawnParticleLine(Location loc1, Location loc2, Particle particle, double interval) {
        Vector v = Utils.getVectorFromLocationToLocation(loc1, loc2);
        double distance = loc1.distance(loc2);
        double steps = distance / interval;

        // Spawn particle line
        for (int i = 0; i < steps; i++) {
            Vector vAdd = new Vector(v.getX(), v.getY(), v.getZ());
            vAdd.multiply(interval);
            loc1.add(vAdd);
            spawnParticle(loc1, particle, 1, 0);
        }
    }
    public static void spawnParticleLine(Location loc1, Location loc2, Color color, double interval) {
        Vector v = Utils.getVectorFromLocationToLocation(loc1, loc2);
        double distance = loc1.distance(loc2);
        double steps = distance / interval;

        // Spawn particle line
        for (int i = 0; i < steps; i++) {
            Vector vAdd = new Vector(v.getX(), v.getY(), v.getZ());
            vAdd.multiply(interval);
            loc1.add(vAdd);
            spawnParticle(loc1, color, 1);
        }

    }

    public static void spawnParticleCircle(Location center, Particle particle, float radius) {
        World world = center.getWorld();

        // Spawn particle circle
        for (double t = 0; t <= 2*Math.PI*radius; t += 0.2) {
            double x = (radius * Math.cos(t)) + center.getX();
            double z = center.getZ() + (radius * Math.sin(t));
            Location loc = new Location(world, x, center.getY() + 1, z);
            world.spawnParticle(particle, loc, 0, 0, 0, 0, 0);
        }
    }

    public static void spawnParticleSphere(Location center, Color color, float radius) {
        for (double i = 0; i <= Math.PI; i += Math.PI / 10) {
            double y = Math.cos(i) * radius;
            double r = Math.sin(i) * radius;
            for (double a = 0; a < Math.PI * 2; a+= Math.PI / 10) {
                double x = Math.cos(a) * r;
                double z = Math.sin(a) * r;
                center.add(x, y, z);
                spawnParticle(center, color, 1);
                center.subtract(x, y, z);
            }
        }
    }
}
