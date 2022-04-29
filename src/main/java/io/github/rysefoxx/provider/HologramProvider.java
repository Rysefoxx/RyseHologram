package io.github.rysefoxx.provider;

import io.github.rysefoxx.object.Hologram;
import org.bukkit.entity.Player;

public interface HologramProvider {

    void update(Player player, Hologram hologram);

}
