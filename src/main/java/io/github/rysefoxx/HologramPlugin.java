package io.github.rysefoxx;

import io.github.rysefoxx.manager.HologramManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class HologramPlugin extends JavaPlugin {

    private HologramManager hologramManager;

    @Override
    public void onEnable() {
        hologramManager = new HologramManager(this);
    }

    @Override
    public void onDisable() {
    }
}
