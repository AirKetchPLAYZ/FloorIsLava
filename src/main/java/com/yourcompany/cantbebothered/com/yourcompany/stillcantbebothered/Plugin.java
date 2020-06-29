package com.yourcompany.cantbebothered.com.yourcompany.stillcantbebothered;

import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;


class MyListener implements Listener
{
    @EventHandler
    public void onGeneration(ChunkPopulateEvent e) {
        ItemStack sword = new ItemStack(Material.PAPER);
        ItemMeta swordMeta = sword.getItemMeta();
        swordMeta.setDisplayName("Push back lava");
        sword.setItemMeta(swordMeta);
        BlockState[] tileEntities = e.getChunk().getTileEntities();

        for(BlockState state : tileEntities) {
            if(state.getType() == Material.CHEST) {
                Chest chest = (Chest) state;
                chest.getBlockInventory().addItem(sword);
                chest.update(true);
            }
        }
    }
    @EventHandler
    public void onPlayerClicks(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        if (action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)) {
            if (player.getInventory().getItemInMainHand().getType() == Material.PAPER && player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals("Push back lava")) {
                Plugin.getSelf().useCallback();
                player.getInventory().getItemInMainHand().setType(Material.AIR);
                player.updateInventory();
            }
        }

    }
}


public final class Plugin extends JavaPlugin {
    boolean rising = false;
    int level = 0;
    int levelNether = 0;
    int levelEnd = 0;
    static Plugin instance;
    public List<World> getPopulatedWorlds() {
        List<World> worlds = new ArrayList<World>();
        int lowest = 0;
        for (World w : Bukkit.getWorlds()) {
            if (w.getPlayerCount() > lowest) {
                worlds.clear();
                worlds.add(w);
            } else if (w.getPlayerCount() == lowest) {
                worlds.add(w);
            }
        }
        return worlds;
    }

    public void changeLevel(int by) {
        for (World w : getPopulatedWorlds()) {
            if (w.getEnvironment() == World.Environment.NETHER) {
                levelNether += by;
                levelNether = Math.max(0, Math.min(256, levelNether));
            }
            if (w.getEnvironment() == World.Environment.THE_END) {
                levelEnd += by;
                levelEnd = Math.max(0, Math.min(256, levelEnd));
            } else {
                level += by;
                level = Math.max(0, Math.min(256, level));
            }
        }
    }
    public void useCallback() {
        changeLevel(-30);
        updateLavaLevel();
        rising = false;
        Bukkit.broadcastMessage("The lava has been pushed back 30 blocks and disabled for 2 minutes.");
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage("Lava is rising again");
                rising = true;
            }
        }, 2400);
    }
    public static Plugin getSelf() {
        return instance;
    }
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new MyListener(), this);
        instance = this;
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                if (!rising)
                    return;
                Bukkit.broadcastMessage("Lava is rising!");
                changeLevel(1);
                updateLavaLevel();
            }
        }, 0, 1200);
    }

    public void updateLavaLevel() {
        Bukkit.broadcastMessage("Updating lava level...");
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() == World.Environment.NETHER) {
                Bukkit.broadcastMessage("Nether lava level..." + levelNether);
                int prev = levelNether;
                for (Chunk c : w.getLoadedChunks()) {

                    int X = c.getX() * 16;
                    int Z = c.getZ() * 16;

                    for (int x = 0; x < 16; x++) // whole chunk
                    {
                        for (int z = 0; z < 16; z++) // whole chunk
                        {
                            for (int y = 0; y <= levelNether; y++)
                            {
                                Bukkit.broadcastMessage("Y level "+ y);
                                Bukkit.broadcastMessage("X: " + X+x + " Y: " + y + " Z: " + Z+z);
                                if (c.getWorld().getBlockAt(X+x, y, Z+z).getType() == Material.AIR)
                                {
                                    c.getWorld().getBlockAt(X+x, y, Z+z).setType(Material.LAVA);
                                }
                            }
                        }

                    }
                    for (int x = 0; x < 16; x++) // whole chunk
                    {
                        for (int z = 0; z < 16; z++) // whole chunk
                        {
                            for (int y = levelNether+1; y <= 256; y++)
                            {
                                if (c.getWorld().getBlockAt(X+x, y, Z+z).getType() == Material.LAVA)
                                {
                                    c.getWorld().getBlockAt(X+x, y, Z+z).setType(Material.AIR);
                                }
                            }
                        }
                    }
                }
            }
            if (w.getEnvironment() == World.Environment.THE_END) {
                Bukkit.broadcastMessage("End lava level..." + levelEnd);
                int prev = levelEnd;
                for (Chunk c : w.getLoadedChunks()) {

                    int X = c.getX() * 16;
                    int Z = c.getZ() * 16;

                    for (int x = 0; x < 16; x++) // whole chunk
                    {
                        for (int z = 0; z < 16; z++) // whole chunk
                        {
                            for (int y = 0; y <= levelEnd; y++)
                            {
                                Bukkit.broadcastMessage("Y level "+ y);
                                Bukkit.broadcastMessage("X: " + X+x + " Y: " + y + " Z: " + Z+z);
                                if (c.getWorld().getBlockAt(X+x, y, Z+z).getType() == Material.AIR)
                                {
                                    c.getWorld().getBlockAt(X+x, y, Z+z).setType(Material.LAVA);
                                }
                            }
                        }

                    }
                    for (int x = 0; x < 16; x++) // whole chunk
                    {
                        for (int z = 0; z < 16; z++) // whole chunk
                        {
                            for (int y = levelEnd+1; y <= 256; y++)
                            {
                                if (c.getWorld().getBlockAt(X+x, y, Z+z).getType() == Material.LAVA)
                                {
                                    c.getWorld().getBlockAt(X+x, y, Z+z).setType(Material.AIR);
                                }
                            }
                        }
                    }
                }
            } else {
                Bukkit.broadcastMessage("Overworld lava level..." + level);
                int prev = level;
                for (Chunk c : w.getLoadedChunks()) {

                    int X = c.getX() * 16;
                    int Z = c.getZ() * 16;

                    for (int x = 0; x < 16; x++) // whole chunk
                    {
                        for (int z = 0; z < 16; z++) // whole chunk
                        {
                            for (int y = 0; y <= level; y++)
                            {
                                Bukkit.broadcastMessage("Y level "+ y);
                                Bukkit.broadcastMessage("X: " + X+x + " Y: " + y + " Z: " + Z+z);
                                if (c.getWorld().getBlockAt(X+x, y, Z+z).getType() == Material.AIR)
                                {
                                    c.getWorld().getBlockAt(X+x, y, Z+z).setType(Material.LAVA);
                                }
                            }
                        }

                    }
                    for (int x = 0; x < 16; x++) // whole chunk
                    {
                        for (int z = 0; z < 16; z++) // whole chunk
                        {
                            for (int y = level+1; y <= 256; y++)
                            {
                                if (c.getWorld().getBlockAt(X+x, y, Z+z).getType() == Material.LAVA)
                                {
                                    c.getWorld().getBlockAt(X+x, y, Z+z).setType(Material.AIR);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("startlava")) {
            rising = true;
            return true;
        }
        if (label.equalsIgnoreCase("stoplava")) {
            rising = false;
            return true;
        }
        return false;
    }

}
