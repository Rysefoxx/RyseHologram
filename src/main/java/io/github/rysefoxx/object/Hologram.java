package io.github.rysefoxx.object;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.google.common.base.Preconditions;
import io.github.rysefoxx.manager.HologramManager;
import io.github.rysefoxx.provider.HologramProvider;
import io.github.rysefoxx.util.Maths;
import io.github.rysefoxx.util.TimeSetting;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnegative;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Rysefoxx | Rysefoxx#6772
 * @since 4/29/2022
 */
@Getter
@Setter
public class Hologram {

    private static JavaPlugin plugin;

    private Object identifier;
    private HologramManager manager;
    private HologramProvider provider;

    private final HashMap<Integer, Integer> lineId = new HashMap<>();
    private final HashMap<UUID, BukkitTask> updaterTask = new HashMap<>();
    private List<String> lines = new ArrayList<>();
    private Location spawnLocation = null;

    private boolean toggled = true;
    private boolean liveUpdate = false;
    private boolean temporary = false;
    private boolean removeOnDisable = false;

    private int delay = 0;
    private int period = 1;
    private double distance = 0.25;

    /**
     * Makes the hologram disappear for all players.
     *
     * @return Returns how many players no longer see the hologram.
     */
    public void hideAll(Consumer<Integer> consumer) {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

        onlinePlayers.forEach(this::hide);
        consumer.accept(onlinePlayers.size());
    }

    /**
     * Makes the hologram disappear for all players.
     */
    public void hideAll() {
        Bukkit.getOnlinePlayers().forEach(this::hide);
    }

    /**
     * Destroys the hologram for a single player.
     *
     * @param player The player who should no longer see the hologram.
     */
    public void hide(Player player) {
        hideFunctionality(player);
    }

    /**
     * Destroys the hologram for multiple players.
     *
     * @param players The players the hologram should no longer see.
     */
    public void hide(Player... players) {
        for (Player player : players) {
            hideFunctionality(player);
        }
    }

    /**
     * Destroys the hologram for multiple players.
     *
     * @param players The players the hologram should no longer see.
     */
    public void hide(List<Player> players) {
        players.forEach(this::hideFunctionality);
    }

    /**
     * Spawns the hologram for a single player.
     *
     * @param player The player who should see the hologram.
     */
    public void show(Player player) {
        Location clonedLocation = this.spawnLocation.clone();

        showFunctionality(clonedLocation, player);
    }

    /**
     * Spawns the hologram for multiple players.
     *
     * @param players TThe players you want the hologram to see.
     */
    public void show(Player... players) {
        Location clonedLocation = this.spawnLocation.clone();

        for (Player player : players) {
            showFunctionality(clonedLocation, player);
        }
    }

    /**
     * Spawns the hologram for multiple players.
     *
     * @param players TThe players you want the hologram to see.
     */
    public void show(List<Player> players) {
        Location clonedLocation = this.spawnLocation.clone();

        for (Player player : players) {
            showFunctionality(clonedLocation, player);
        }
    }

    /**
     * Spawns the hologram for all players.
     *
     * @return Returns how many players see the hologram now.
     */
    public void showAll(Consumer<Integer> consumer) {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

        onlinePlayers.forEach(this::show);
        consumer.accept(onlinePlayers.size());
    }

    /**
     * Spawns the hologram for all players.
     */
    public void showAll() {
        Bukkit.getOnlinePlayers().forEach(this::show);
    }

    /**
     * Changes the spawn point of the hologram for all players.
     *
     * @param location The new spawn point.
     */
    public void updateSpawnLocation(Location location) {
        hideAll();
        setSpawnLocation(location);
        showAll();
    }

    /**
     * Changes the spawn point of the hologram for a single player only.
     *
     * @param player   The player for whom the spawn point of the hologram should be changed.
     * @param location The new spawn point.
     */
    public void updateSpawnLocation(Location location, Player player) {
        hide(player);
        setSpawnLocation(location);
        show(player);
    }

    /**
     * Changes the spawn point of the hologram for multiple players.
     *
     * @param location The new spawn point
     * @param players  The passed players will now see the hologram at the new spawn point.
     */
    public void updateSpawnLocation(Location location, Player... players) {
        Arrays.stream(players).forEach(this::hide);
        setSpawnLocation(location);
        Arrays.stream(players).forEach(this::show);
    }

    /**
     * Changes the spawn point of the hologram for multiple players.
     *
     * @param location The new spawn point.
     * @param players  The passed players will now see the hologram at the new spawn point.
     */
    public void updateSpawnLocation(Location location, List<Player> players) {
        players.forEach(this::hide);
        setSpawnLocation(location);
        players.forEach(this::show);
    }

    /**
     * Changes the line spacing of the hologram for all players.
     *
     * @param distance The new line spacing.
     */
    public void updateDistance(double distance) {
        hideAll();
        this.distance = distance;
        showAll();
    }

    /**
     * Changes the line spacing of the hologram for a single player.
     *
     * @param distance The new line spacing.
     */
    public void updateDistance(double distance, Player player) {
        hide(player);
        this.distance = distance;
        show(player);
    }

    /**
     * Changes the line spacing of the hologram for multiple players.
     *
     * @param distance The new line spacing.
     * @param players  The players for whom the line spacing of the hologram should be changed.
     */
    public void updateDistance(double distance, Player... players) {
        Arrays.stream(players).forEach(this::hide);
        this.distance = distance;
        Arrays.stream(players).forEach(this::show);
    }

    /**
     * Changes the line spacing of the hologram for multiple players.
     *
     * @param distance The new line spacing.
     * @param players  The players for whom the line spacing of the hologram should be changed.
     */
    public void updateDistance(double distance, List<Player> players) {
        players.forEach(this::hide);
        this.distance = distance;
        players.forEach(this::show);
    }

    /**
     * Updates several lines of the hologram with new text for all players.
     *
     * @param indexes The lines that are to be changed.
     * @param lines   The new lines.
     */
    public void updateLines(int[] indexes, String[] lines) {
        Preconditions.checkArgument(indexes.length == lines.length, "The parameters passed must be of the same size.");

        hideAll();
        updateLineFunctionality(indexes, lines);
        showAll();
    }

    /**
     * Updates several lines of the hologram with new text for a single player.
     *
     * @param indexes The lines that are to be changed.
     * @param lines   The new lines.
     * @param player  The player for whom the lines in the hologram should be changed.
     */
    public void updateLines(int[] indexes, String[] lines, Player player) {
        Preconditions.checkArgument(indexes.length == lines.length, "The parameters passed must be of the same size.");

        hide(player);
        updateLineFunctionality(indexes, lines);
        show(player);
    }

    /**
     * Updates several lines of the hologram with new text for multiple players.
     *
     * @param indexes The lines that are to be changed.
     * @param lines   The new lines.
     * @param players The players for whom the lines in the hologram should be changed.
     */
    public void updateLines(int[] indexes, String[] lines, Player... players) {
        Preconditions.checkArgument(indexes.length == lines.length, "The parameters passed must be of the same size.");

        Arrays.stream(players).forEach(this::hide);
        updateLineFunctionality(indexes, lines);
        Arrays.stream(players).forEach(this::show);
    }

    /**
     * Updates several lines of the hologram with new text for multiple players.
     *
     * @param indexes The lines that are to be changed.
     * @param lines   The new lines.
     * @param players The players for whom the lines in the hologram should be changed.
     */
    public void updateLines(int[] indexes, String[] lines, List<Player> players) {
        Preconditions.checkArgument(indexes.length == lines.length, "The parameters passed must be of the same size.");

        players.forEach(this::hide);
        updateLineFunctionality(indexes, lines);
        players.forEach(this::show);
    }

    /**
     * Updates the line of the hologram with new text, for all players.
     *
     * @param index The line of the hologram that is changed.
     * @param line  The text that overwrites the old one.
     */
    public void updateLine(@Nonnegative int index, String line) {
        hideAll();
        while (index >= this.lines.size()) {
            this.lines.add("");
        }

        this.lines.set(index, line);
        showAll();
    }

    /**
     * Updates the line of the hologram with new text for a single player.
     *
     * @param index  The line of the hologram that is changed.
     * @param line   The text that overwrites the old one.
     * @param player The player for whom the line is to be changed.
     */
    public void updateLine(@Nonnegative int index, String line, Player player) {
        hide(player);
        while (index >= this.lines.size()) {
            this.lines.add("");
        }

        this.lines.set(index, line);
        show(player);
    }

    /**
     * Updates the line of the hologram with new text for multiple players.
     *
     * @param index   The line of the hologram that is changed.
     * @param line    The text that overwrites the old one.
     * @param players The players for whom the line should be changed.
     */
    public void updateLine(@Nonnegative int index, String line, Player... players) {
        Arrays.stream(players).forEach(this::hide);
        while (index >= this.lines.size()) {
            this.lines.add("");
        }

        this.lines.set(index, line);
        Arrays.stream(players).forEach(this::show);
    }

    /**
     * Updates the line of the hologram with new text for multiple players.
     *
     * @param index   The line of the hologram that is changed.
     * @param line    The text that overwrites the old one.
     * @param players The players for whom the line should be changed.
     */
    public void updateLine(@Nonnegative int index, String line, List<Player> players) {
        players.forEach(this::hide);
        while (index >= this.lines.size()) {
            this.lines.add("");
        }

        this.lines.set(index, line);
        players.forEach(this::show);
    }

    /**
     * Adds multiple blank lines for all players in the hologram.
     *
     * @param indexes The indexes where an empty line should appear.
     */
    public void addEmptyLines(@Nonnegative int[] indexes) {
        hideAll();

        for (int index : indexes) {
            if (addEmptyLinesForAllFunctionality(index)) return;
        }
        showAll();
    }

    /**
     * Adds multiple blank lines for a single player in the hologram.
     *
     * @param indexes The indexes where an empty line should appear.
     * @param player  The player for whom the hologram should get multiple blank lines.
     */
    public void addEmptyLines(@Nonnegative int[] indexes, Player player) {
        hide(player);

        for (int index : indexes) {
            if (addEmptyLineSinglePlayerFunctionality(index, player)) return;
        }
        show(player);
    }

    /**
     * Adds multiple blank lines for multiple players in the hologram.
     *
     * @param indexes The indexes where an empty line should appear.
     * @param players The players for whom the hologram should receive several blank lines.
     */
    public void addEmptyLines(@Nonnegative int[] indexes, Player... players) {
        Arrays.stream(players).forEach(this::hide);

        for (int index : indexes) {
            if (addEmptyLineFunctionality(index, players)) return;
        }
        Arrays.stream(players).forEach(this::show);
    }

    /**
     * Adds multiple blank lines for multiple players in the hologram.
     *
     * @param indexes The indexes where an empty line should appear.
     * @param players The players for whom the hologram should receive several blank lines.
     */
    public void addEmptyLines(@Nonnegative int[] indexes, List<Player> players) {
        players.forEach(this::hide);

        for (int index : indexes) {
            if (addEmptyLinesFunctionality(players, index)) return;
        }
        players.forEach(this::show);
    }

    /**
     * Adds a blank line for all players in the hologram.
     *
     * @param index The index where the empty row should be.
     */
    public void addEmptyLine(@Nonnegative int index) {
        hideAll();

        if (addEmptyLinesForAllFunctionality(index)) return;
        showAll();
    }

    /**
     * Adds a blank line for a single player in the hologram.
     *
     * @param index  The index where the empty row should be.
     * @param player The player for whom the hologram should get a blank line.
     */
    public void addEmptyLine(@Nonnegative int index, Player player) {
        hide(player);

        if (addEmptyLineSinglePlayerFunctionality(index, player)) return;
        show(player);
    }

    /**
     * Adds a blank line for multiple players in the hologram.
     *
     * @param index   The index where the empty row should be.
     * @param players The players for whom the hologram should get a blank line.
     */
    public void addEmptyLine(@Nonnegative int index, Player... players) {
        Arrays.stream(players).forEach(this::hide);

        if (addEmptyLineFunctionality(index, players)) return;
        Arrays.stream(players).forEach(this::show);
    }

    /**
     * Adds a blank line for multiple players in the hologram.
     *
     * @param index   The index where the empty row should be.
     * @param players The players for whom the hologram should get a blank line.
     */
    public void addEmptyLine(@Nonnegative int index, List<Player> players) {
        players.forEach(this::hide);

        if (addEmptyLinesFunctionality(players, index)) return;
        players.forEach(this::show);
    }

    /**
     * We remove several lines in the hologram for all players.
     *
     * @param indexes The indexes of the lines to be removed.
     * @return false If not all lines could be deleted.
     */
    public boolean clearLines(@Nonnegative int[] indexes) {
        int linesAffected = 0;
        for (int index : indexes) {
            if (index >= this.lines.size()) continue;
            hideAll();

            this.lines.remove(index);
            showAll();
            linesAffected++;
        }

        return linesAffected >= indexes.length;
    }

    /**
     * We remove several lines in the hologram for a single player.
     *
     * @param indexes The indexes of the lines to be removed.
     * @param player  The player who receives the hologram where the lines were reduced.
     * @return false If not all lines could be deleted.
     */
    public boolean clearLines(@Nonnegative int[] indexes, Player player) {
        int linesAffected = 0;
        for (int index : indexes) {
            if (index >= this.lines.size()) continue;
            hide(player);

            this.lines.remove(index);
            show(player);
            linesAffected++;
        }

        return linesAffected >= indexes.length;
    }

    /**
     * We remove several lines in the hologram for multiple players.
     *
     * @param indexes The indexes of the lines to be removed.
     * @param players The players who receive the hologram where the lines were reduced.
     * @return false If not all lines could be deleted.
     */
    public boolean clearLines(@Nonnegative int[] indexes, Player... players) {
        int linesAffected = 0;
        for (int index : indexes) {
            if (index >= this.lines.size()) continue;
            Arrays.stream(players).forEach(this::hide);

            this.lines.remove(index);
            Arrays.stream(players).forEach(this::show);
            linesAffected++;
        }

        return linesAffected >= indexes.length;
    }

    /**
     * We remove several lines in the hologram for multiple players.
     *
     * @param indexes The indexes of the lines to be removed.
     * @param players The players who receive the hologram where the lines were reduced.
     * @return false If not all lines could be deleted.
     */
    public boolean clearLines(@Nonnegative int[] indexes, List<Player> players) {
        int linesAffected = 0;
        for (int index : indexes) {
            if (index >= this.lines.size()) continue;
            players.forEach(this::hide);

            this.lines.remove(index);
            players.forEach(this::show);
            linesAffected++;
        }

        return linesAffected >= indexes.length;
    }

    /**
     * We remove one line from the hologram for all players.
     *
     * @param index The index of the line to be removed.
     * @return false if the index is greater than or equal to the number of rows.
     */
    public boolean clearLine(@Nonnegative int index) {
        if (index >= this.lines.size()) return false;
        hideAll();

        this.lines.remove(index);
        showAll();
        return true;
    }

    /**
     * We remove one line from the hologram for a single player.
     *
     * @param index  The index of the line to be removed.
     * @param player The player for whom the line in the hologram should be removed.
     * @return false if the index is greater than or equal to the number of rows.
     */
    public boolean clearLine(@Nonnegative int index, Player player) {
        if (index >= this.lines.size()) return false;
        hide(player);

        this.lines.remove(index);
        show(player);
        return true;
    }

    /**
     * We remove one line from the hologram for multiple players.
     *
     * @param index   The index of the line to be removed.
     * @param players The players for whom the line in the hologram should be removed.
     * @return false if the index is greater than or equal to the number of rows.
     */
    public boolean clearLine(@Nonnegative int index, Player... players) {
        if (index >= this.lines.size()) return false;
        Arrays.stream(players).forEach(this::hide);

        this.lines.remove(index);
        Arrays.stream(players).forEach(this::show);
        return true;
    }

    /**
     * We remove one line from the hologram for multiple players.
     *
     * @param index   The index of the line to be removed.
     * @param players The players for whom the line in the hologram should be removed.
     * @return false if the index is greater than or equal to the number of rows.
     */
    public boolean clearLine(@Nonnegative int index, List<Player> players) {
        if (index >= this.lines.size()) return false;
        players.forEach(this::hide);

        this.lines.remove(index);
        players.forEach(this::show);
        return true;
    }

    /**
     * Builder to create an Hologram
     *
     * @return The Builder object with several methods.
     */
    public static Builder builder(JavaPlugin plugin) {
        Hologram.plugin = plugin;
        return new Builder();
    }

    public static class Builder {
        private Object identifier;
        private HologramManager manager;
        private HologramProvider provider;

        private List<String> lines = new ArrayList<>();
        private Location spawnLocation = null;

        private boolean toggled = true;
        private boolean temporary = false;
        private boolean removeOnDisable = false;

        private int delay = 0;
        private int period = 1;
        private double distance = 0.25;

        /**
         * If this method is called, the hologram will be destroyed at the PluginDisableEvent.
         *
         * @return The builder object.
         */
        public Builder onDisable() {
            this.removeOnDisable = true;
            return this;
        }

        /**
         * The provider to fill the hologram with content.
         *
         * @param provider Implement with new HologramProvider()
         * @return The builder object.
         */
        public Builder provider(HologramProvider provider) {
            this.provider = provider;
            return this;
        }

        /**
         * Changes the spacing between the lines.
         *
         * @param distance The new line spacing.
         * @return The builder object.
         */
        public Builder distance(double distance) {
            this.distance = distance;
            return this;
        }

        /**
         * Adjusts the period of the scheduler.
         *
         * @param time    Time
         * @param setting Set your own time type.
         * @return The builder object.
         */
        public Builder period(@Nonnegative int time, TimeSetting setting) {
            this.period = setting == TimeSetting.MILLISECONDS ? time : setting == TimeSetting.SECONDS ? time * 20 : setting == TimeSetting.MINUTES ? (time * 20) * 60 : time;
            return this;
        }

        /**
         * Adjusts the delay of the scheduler.
         *
         * @param time    Time
         * @param setting Set your own time type.
         * @return The builder object.
         */
        public Builder delay(@Nonnegative int time, TimeSetting setting) {
            this.delay = setting == TimeSetting.MILLISECONDS ? time : setting == TimeSetting.SECONDS ? time * 20 : setting == TimeSetting.MINUTES ? (time * 20) * 60 : time;
            return this;
        }

        /**
         * This function sets the temporary variable to true and returns the builder.
         *
         * @return The builder object.
         */
        public Builder temporary() {
            this.temporary = true;
            return this;
        }

        /**
         * This will not spawn the hologram directly when it is created.
         *
         * @return The builder object.
         * @apiNote The hologram must be spawned manually.
         */
        public Builder disable() {
            this.toggled = false;
            return this;
        }

        /**
         * Specifies where the hologram should appear at the end.
         *
         * @return The builder object.
         */
        public Builder spawnLocation(Location spawnLocation) {
            this.spawnLocation = spawnLocation;
            return this;
        }

        /**
         * Adds several lines to the hologram.
         *
         * @param lines These lines will be visible later in the hologram.
         * @return The builder object.
         */
        public Builder lines(String... lines) {
            this.lines = Arrays.stream(lines).collect(Collectors.toList());
            return this;
        }

        /**
         * Adds several lines to the hologram.
         *
         * @param lines These lines will be visible later in the hologram.
         * @return The builder object.
         */
        public Builder lines(List<String> lines) {
            this.lines = new ArrayList<>(lines);
            return this;
        }

        /**
         * Adds a single line to the hologram.
         *
         * @param line This line will be seen later in the hologram.
         * @return The builder object.
         */
        public Builder line(String line) {
            this.lines.add(line);
            return this;
        }

        /**
         * Adds a manager to the Hologram.
         *
         * @param manager The HologramManager that will be used to manage the hologram.
         * @return The builder object.
         */
        public Builder manager(HologramManager manager) {
            this.manager = manager;
            return this;
        }

        /**
         * Gives the hologram an identification.
         *
         * @param identifier The ID through which you can get the hologram.
         * @return The builder object.
         */
        public Builder identifier(Object identifier) {
            this.identifier = identifier;
            return this;
        }


        /**
         * Builds the Hologram
         *
         * @return the Hologram
         * @throws NullPointerException     when {@link Builder#identifier} is null, when {@link Builder#manager} is null or when {@link Builder#spawnLocation} is null
         * @throws IllegalArgumentException when lines are empty
         */
        public Hologram build() throws NullPointerException, IllegalArgumentException {
            Hologram hologram = new Hologram();

            if (this.identifier == null) {
                throw new NullPointerException("You need to give the hologram an identification.");
            }
            if (this.manager == null) {
                throw new NullPointerException("You need to pass the HologramManager.");
            }
            if (this.spawnLocation == null) {
                throw new NullPointerException("You must pass the spawn point.");
            }
            if (this.lines.isEmpty()) {
                throw new IllegalArgumentException("You must set at least 1 line.");
            }

            hologram.identifier = this.identifier;
            hologram.manager = this.manager;
            hologram.lines = this.lines;
            hologram.spawnLocation = this.spawnLocation;
            hologram.toggled = this.toggled;
            hologram.temporary = this.temporary;
            hologram.delay = this.delay;
            hologram.period = this.period;
            hologram.distance = this.distance;
            hologram.provider = this.provider;
            hologram.removeOnDisable = this.removeOnDisable;

            this.manager.create(hologram);

            return hologram;
        }
    }

    private void updateLineFunctionality(int[] indexes, String[] lines) {
        for (int i = 0; i < indexes.length; i++) {
            int index = indexes[i];
            String line = lines[i];
            while (index >= this.lines.size()) {
                this.lines.add("");
            }
            this.lines.set(index, line);
        }
    }

    private void addEmptyLineInListFunctionality(@Nonnegative int index) {
        List<String> lines = new ArrayList<>();

        for (int i = 0; i < this.lines.size(); i++) {
            String line = this.lines.get(i);
            if (i == index) lines.add("");
            lines.add(line);
        }
        this.lines = lines;
    }

    private boolean addEmptyLinesFunctionality(List<Player> players, int index) {
        if (index >= this.lines.size()) {
            this.lines.add("");
            while (index > this.lines.size()) {
                this.lines.add("");
            }
            players.forEach(this::show);
            return true;
        }
        addEmptyLineInListFunctionality(index);
        return false;
    }

    private boolean addEmptyLineFunctionality(@Nonnegative int index, Player[] players) {
        if (index >= this.lines.size()) {
            this.lines.add("");
            while (index > this.lines.size()) {
                this.lines.add("");
            }
            Arrays.stream(players).forEach(this::show);
            return true;
        }

        addEmptyLineInListFunctionality(index);
        return false;
    }

    private boolean addEmptyLinesForAllFunctionality(int index) {
        if (index >= this.lines.size()) {
            this.lines.add("");
            while (index > this.lines.size()) {
                this.lines.add("");
            }
            showAll();
            return true;
        }
        addEmptyLineInListFunctionality(index);
        return false;
    }

    private boolean addEmptyLineSinglePlayerFunctionality(@Nonnegative int index, Player player) {
        if (index >= this.lines.size()) {
            this.lines.add("");
            while (index > this.lines.size()) {
                this.lines.add("");
            }
            show(player);
            return true;
        }

        addEmptyLineInListFunctionality(index);
        return false;
    }

    private void hideFunctionality(Player player) {
        if (this.updaterTask.containsKey(player.getUniqueId())) {
            BukkitTask task = this.updaterTask.remove(player.getUniqueId());
            if (task != null && Bukkit.getScheduler().isQueued(task.getTaskId())) task.cancel();
        }

        for (int i = 0; i < this.lines.size(); i++) {
            if (!this.lineId.containsKey(i)) continue;

            PacketContainer packet = this.manager.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            packet.getIntegerArrays().writeSafely(0, new int[]{this.lineId.get(i)});

            try {
                this.manager.getProtocolManager().sendServerPacket(player, packet);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private void showFunctionality(Location clonedLocation, Player player) {
        invokeUpdateScheduler(player);

        for (int i = 0; i < this.lines.size(); i++) {
            String line = this.lines.get(i);
            if (!this.lineId.containsKey(i)) {
                this.lineId.put(i, Maths.randomInteger(1, Integer.MAX_VALUE));
            }
            PacketContainer packet = this.manager.getProtocolManager().createPacket(PacketType.Play.Server.SPAWN_ENTITY);

            packet.getIntegers().write(0, this.lineId.get(i));

            packet.getIntegers().write(1, (int) Math.floor(clonedLocation.getX() * 32.0D));
            packet.getIntegers().write(2, (int) Math.floor(clonedLocation.getY() * 32.0D));
            packet.getIntegers().write(3, (int) Math.floor(clonedLocation.getZ() * 32.0D));
            packet.getIntegers().write(4, 0);
            packet.getIntegers().write(5, 0);
            packet.getIntegers().write(6, 0);
            packet.getIntegers().write(7, 0);
            packet.getIntegers().write(8, 0);
            packet.getIntegers().write(9, 78);
            packet.getIntegers().write(10, 0);

            clonedLocation.subtract(0, this.distance, 0);
            try {
                this.manager.getProtocolManager().sendServerPacket(player, packet);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            loadLines(player, i, line);
        }
    }

    private void loadLines(Player player, @Nonnegative int index, String line) {
        PacketContainer packet = this.manager.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        WrappedDataWatcher watcher = new WrappedDataWatcher();
        watcher.setObject(10, (byte) 31, true);
        watcher.setObject(0, (byte) 32, true);
        watcher.setObject(3, (byte) 1, true);
        watcher.setObject(2, line);
        packet.getIntegers().write(0, this.lineId.get(index));
        packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

        try {
            this.manager.getProtocolManager().sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void invokeUpdateScheduler(Player player) {
        if (this.updaterTask.containsKey(player.getUniqueId())) return;

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> this.provider.update(player, this), this.delay, this.period);
        this.updaterTask.put(player.getUniqueId(), task);
    }
}
