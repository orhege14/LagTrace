package com.redstonecircuits.lagTrace;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class LagTrace extends JavaPlugin {

    private static final int CELL_SIZE = 8;
    private static final int TOP_HOTSPOTS = 3;

    @Override
    public void onEnable() {
        Objects.requireNonNull(getCommand("lagtrace"))
                .setExecutor(this::handleCommand);

        getLogger().info("LagTrace enabled.");
    }

    private boolean handleCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("LagTrace can only be used by a player.");
            return true;
        }

        player.sendMessage(Component.text(
                "§c§lLAGTRACE §7» §fScanning loaded world activity..."
        ));

        List<Hotspot> hotspots = findHotspots();

        if (hotspots.isEmpty()) {
            player.sendMessage(Component.text(
                    "§aLagTrace §7» §fNo activity hotspots detected."
            ));
            return true;
        }

        sendResults(player, hotspots);
        return true;
    }

    private List<Hotspot> findHotspots() {

        Map<Cell, Activity> activities = new HashMap<>();

        for (World world : getServer().getWorlds()) {

            for (Chunk chunk : world.getLoadedChunks()) {
                analyzeChunk(chunk, activities);
            }
        }

        return activities.values()
                .stream()
                .filter(activity -> activity.score > 0)
                .map(Activity::toHotspot)
                .sorted(Comparator.comparingInt(Hotspot::score).reversed())
                .limit(TOP_HOTSPOTS)
                .toList();
    }

    private void analyzeChunk(
            Chunk chunk,
            Map<Cell, Activity> activities
    ) {

        /*
         * ENTITY ACTIVITY
         */
        for (Entity entity : chunk.getEntities()) {

            if (entity instanceof Player) {
                continue;
            }

            Location location = entity.getLocation();

            Activity activity = getActivity(
                    activities,
                    location
            );

            activity.entities++;

            activity.entityTypes.merge(
                    entity.getType().name(),
                    1,
                    Integer::sum
            );

            /*
             * Count the REAL amount of dropped items.
             *
             * 1 item entity can contain up to 64 items.
             */
            if (entity instanceof Item item) {

                activity.itemsStored +=
                        item.getItemStack().getAmount();

                activity.itemTypes.merge(
                        item.getItemStack().getType().name(),
                        item.getItemStack().getAmount(),
                        Integer::sum
                );
            }

            activity.score += 2;

            if (activity.strongestLocation == null) {
                activity.strongestLocation = location.clone();
            }
        }

        /*
         * BLOCK ENTITY ACTIVITY
         *
         * No inventory contents are counted here.
         */
        for (BlockState state : chunk.getTileEntities()) {

            Location location = state.getLocation();

            Activity activity = getActivity(
                    activities,
                    location
            );

            activity.blockEntities++;

            activity.blockEntityTypes.merge(
                    state.getType().name(),
                    1,
                    Integer::sum
            );

            activity.score += 4;

            if (activity.strongestLocation == null) {
                activity.strongestLocation = location.clone();
            }
        }
    }

    private Activity getActivity(
            Map<Cell, Activity> activities,
            Location location
    ) {

        int cellX = Math.floorDiv(
                location.getBlockX(),
                CELL_SIZE
        );

        int cellZ = Math.floorDiv(
                location.getBlockZ(),
                CELL_SIZE
        );

        Cell cell = new Cell(
                location.getWorld().getUID(),
                cellX,
                cellZ
        );

        return activities.computeIfAbsent(
                cell,
                ignored -> new Activity(
                        location.getWorld(),
                        cellX,
                        cellZ
                )
        );
    }

    private void sendResults(
            Player player,
            List<Hotspot> hotspots
    ) {

        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text(
                "§c§l⚠ LAGTRACE — TOP 3 HOTSPOTS"
        ));
        player.sendMessage(Component.text(
                "§7━━━━━━━━━━━━━━━━━━━━━━━━━━"
        ));

        for (int i = 0; i < hotspots.size(); i++) {

            sendHotspot(
                    player,
                    i + 1,
                    hotspots.get(i)
            );
        }

        player.sendMessage(Component.text(
                "§7━━━━━━━━━━━━━━━━━━━━━━━━━━"
        ));

        player.sendMessage(Component.text(
                "§8Only currently loaded chunks were analyzed."
        ));
    }

    private void sendHotspot(
            Player player,
            int rank,
            Hotspot hotspot
    ) {

        Location location = hotspot.location;

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text(
                "§c§l#" + rank + " HOTSPOT"
        ));

        player.sendMessage(Component.text(
                "§fWorld: §e" + hotspot.world.getName()
        ));

        Component coordinates = Component.text(
                        "§f📍 §e" + x + " " + y + " " + z
                )
                .clickEvent(
                        ClickEvent.suggestCommand(
                                "/tp " + x + " " + y + " " + z
                        )
                )
                .hoverEvent(
                        HoverEvent.showText(
                                Component.text(
                                        "§aClick → /tp "
                                                + x + " "
                                                + y + " "
                                                + z
                                )
                        )
                );

        player.sendMessage(coordinates);

        player.sendMessage(Component.text(
                "§fActivity Score: §c" + hotspot.score
        ));

        player.sendMessage(Component.text(
                "§fEntities: §e" + hotspot.entities
        ));

        player.sendMessage(Component.text(
                "§fBlock Entities: §e" + hotspot.blockEntities
        ));

        if (hotspot.itemsStored > 0) {

            player.sendMessage(Component.text(
                    "§fDropped Items: §e"
                            + hotspot.itemsStored
            ));

            sendTypes(
                    player,
                    "§6DROPPED ITEM TYPES",
                    hotspot.itemTypes
            );
        }

        sendTypes(
                player,
                "§cENTITY ACTIVITY",
                hotspot.entityTypes
        );

        sendTypes(
                player,
                "§6BLOCK ENTITY ACTIVITY",
                hotspot.blockEntityTypes
        );
    }

    private void sendTypes(
            Player player,
            String title,
            Map<String, Integer> types
    ) {

        if (types.isEmpty()) {
            return;
        }

        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text(title));

        types.entrySet()
                .stream()
                .sorted(
                        Map.Entry
                                .<String, Integer>
                                        comparingByValue()
                                .reversed()
                )
                .limit(5)
                .forEach(entry ->
                        player.sendMessage(
                                Component.text(
                                        "§7• §f"
                                                + entry.getKey()
                                                + " §7× §e"
                                                + entry.getValue()
                                )
                        )
                );
    }

    private record Cell(
            UUID world,
            int x,
            int z
    ) {
    }

    private static final class Activity {

        private final World world;
        private final int cellX;
        private final int cellZ;

        private int score;
        private int entities;
        private int blockEntities;

        private long itemsStored;

        private Location strongestLocation;

        private final Map<String, Integer> entityTypes =
                new HashMap<>();

        private final Map<String, Integer> blockEntityTypes =
                new HashMap<>();

        private final Map<String, Integer> itemTypes =
                new HashMap<>();

        private Activity(
                World world,
                int cellX,
                int cellZ
        ) {
            this.world = world;
            this.cellX = cellX;
            this.cellZ = cellZ;
        }

        private Hotspot toHotspot() {

            Location location;

            if (strongestLocation != null) {

                location = strongestLocation.clone();

            } else {

                int x = cellX * CELL_SIZE + CELL_SIZE / 2;
                int z = cellZ * CELL_SIZE + CELL_SIZE / 2;

                int y = world.getHighestBlockYAt(x, z);

                location = new Location(
                        world,
                        x,
                        y,
                        z
                );
            }

            return new Hotspot(
                    world,
                    location,
                    score,
                    entities,
                    blockEntities,
                    itemsStored,
                    new HashMap<>(entityTypes),
                    new HashMap<>(blockEntityTypes),
                    new HashMap<>(itemTypes)
            );
        }
    }

    private record Hotspot(
            World world,
            Location location,
            int score,
            int entities,
            int blockEntities,
            long itemsStored,
            Map<String, Integer> entityTypes,
            Map<String, Integer> blockEntityTypes,
            Map<String, Integer> itemTypes
    ) {
    }
}