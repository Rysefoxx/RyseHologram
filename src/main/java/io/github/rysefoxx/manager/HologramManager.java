package io.github.rysefoxx.manager;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import io.github.rysefoxx.object.Hologram;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * @author Rysefoxx | Rysefoxx#6772
 * @since 4/29/2022
 */
@Getter
public class HologramManager {

    private final JavaPlugin plugin;

    private final ProtocolManager protocolManager;
    private final List<Hologram> holograms = new ArrayList<>();

    public HologramManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    /**
     * Creates a new hologram
     *
     * @param hologram The hologram to be created.
     */
    public void create(Hologram hologram) {
        this.holograms.add(hologram);
        if (hologram.isToggled()) hologram.showAll();
    }

    /**
     * Removes a hologram
     *
     * @param hologram The hologram that is to be removed.
     */
    public void remove(Hologram hologram) {
        this.holograms.remove(hologram);

        hologram.hideAll();
    }

    /**
     * Searches and returns the hologram based on the identifier.
     *
     * @param identifier Filtering is performed according to this criterion.
     * @return null if no hologram could be found.
     */
    public Optional<Hologram> fetchFromIdentifier(Object identifier) {
        if (this.holograms.isEmpty()) return Optional.empty();

        return this.holograms.stream().filter(hologram -> hologram.getIdentifier().equals(identifier)).findAny();
    }


    /**
     * A HashMap with all holograms in the given radius.
     *
     * @param location The starting point.
     * @param radius   The radius to be checked.
     * @return A HashMap with all holograms in the given radius.
     */
    public HashMap<Hologram, Double> locateNearestAsMap(Location location, double radius) {
        HashMap<Hologram, Double> hologramInRange = new HashMap<>();

        this.holograms.forEach(hologram -> {
            if (!Objects.equals(hologram.getSpawnLocation().getWorld(), location.getWorld())) return;
            double distance = hologram.getSpawnLocation().distance(location);

            if (distance > radius) return;

            hologramInRange.put(hologram, radius);
        });

        return hologramInRange;
    }

    /**
     * Gives you the closest hologram out.
     *
     * @param location The starting point.
     * @param radius   The radius to be checked.
     * @return null if no hologram could be found.
     */
    public Optional<Hologram> locateNearest(Location location, double radius) {

        for (Hologram hologram : this.holograms) {
            if (!Objects.equals(hologram.getSpawnLocation().getWorld(), location.getWorld())) continue;
            double distance = hologram.getSpawnLocation().distance(location);

            if (distance > radius) continue;

            return Optional.of(hologram);
        }
        return Optional.empty();
    }
}
