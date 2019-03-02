package fr.omegaserv.tower;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class Events implements Listener {

    private TheTowerPlugin main = null;

    public Events(TheTowerPlugin main)
    {
        this.main = main;
    }

    @EventHandler
    public void hung(FoodLevelChangeEvent e) {
        if (!this.main.game.running) {}
        e.setCancelled(true);
    }

    @EventHandler
    public void setMotd(ServerListPingEvent e) {
        if (this.main.getConfig().getBoolean("Motd.Enabled")) {
            if (this.main.game.running) {
                e.setMotd(this.main.getConfig().getString("Scoreboard.Title") + ChatColor.DARK_RED + " |" + ChatColor.GREEN + " Partie en cours : " + ChatColor.RED + this.main.MaxPlayersTeam + ChatColor.GOLD + " joueurs par �quipes.                  " +
                        "Score : " + ChatColor.BLUE + " " + this.main.game.equipeBlue.pts + ChatColor.GOLD + " / " + ChatColor.RED + " " + this.main.game.equipeRed.pts + ChatColor.GOLD + " points.");
            } else {
                e.setMotd(this.main.getConfig().getString("Scoreboard.Title") + ChatColor.DARK_RED + " | " + ChatColor.GOLD + "En attente de joueurs, venez ! :D");
            }
        }
    }

    @EventHandler
    public void Deco(PlayerQuitEvent e)
    {
        Player pl = e.getPlayer();
        if (this.main.game.running)
        {
            int size = -1;
            if (this.main.game.sc.getPlayerTeam(pl) != null)
            {
                for (OfflinePlayer op : this.main.game.sc.getPlayerTeam(pl).getPlayers()) {
                    if (op.isOnline()) {
                        size++;
                    }
                }
                if (size == 0)
                {
                    this.main.game.stop(300);
                    if (this.main.game.equipeBlue.isInTeam(pl)) {
                        Bukkit.broadcastMessage(ChatColor.GREEN + "L'équipe " + ChatColor.RED + "Rouge " + ChatColor.GREEN + " a gagnée !");
                    } else {
                        Bukkit.broadcastMessage(ChatColor.GREEN + "L'équipe " + ChatColor.BLUE + "Bleue " + ChatColor.GREEN + " a gagnée !");
                    }
                }
                else
                {
                    this.main.update();
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e)
    {
        Player pl = e.getEntity().getPlayer();
        Player plk = e.getEntity().getKiller();
        for (ItemStack drops : e.getDrops()) {
            if (drops.equals(this.main.menu.itemMenu)) {
                drops.setType(Material.AIR);
            }
        }
        if (this.main.game.running) {
            try
            {
                e.setDeathMessage(pl.getDisplayName() + " a été tué(e) par " + plk.getName() + " .");
            }
            catch (Exception except)
            {
                e.setDeathMessage(pl.getDisplayName() + " est mort.");
            }
        }
        if (!this.main.getConfig().getBoolean("Armors_drop")) {
            for (ItemStack drops : e.getDrops()) {
                if ((drops.getTypeId() == 298) || (drops.getTypeId() == 299) || (drops.getTypeId() == 300) || (drops.getTypeId() == 301)) {
                    drops.setType(Material.AIR);
                }
            }
        }
    }

    public void Pool(Player pl, Location to)
    {
        if ((this.main.game.equipeRed.isInTeam(pl)) && (this.main.game.pblue.isInLocation(to))) {
            this.main.game.equipeRed.addPoint(pl);
        } else if ((this.main.game.equipeBlue.isInTeam(pl)) && (this.main.game.pred.isInLocation(to))) {
            this.main.game.equipeBlue.addPoint(pl);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e)
    {
        if (this.main.game.running)
        {
            boolean hasteam = false;
            Player pl = e.getPlayer();
            Location locpl = e.getTo();
            for (Team tm : this.main.game.sc.getTeams()) {
                if (tm.hasPlayer(pl)) {
                    hasteam = true;
                }
            }
            if ((this.main.game.equipeBlue.isInTeam(pl)) || (this.main.game.equipeRed.isInTeam(pl))) {
                Pool(pl, locpl);
            }
        }
        if ((this.main.game.pause) && (e.getPlayer() != this.main.pausePlayer)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e)
    {
        Player pl = e.getPlayer();
        if (this.main.game.running)
        {
            if (this.main.game.equipeRed.isInTeam(pl)) {
                e.setRespawnLocation(this.main.redspawn);
            }
            if (this.main.game.equipeBlue.isInTeam(pl)) {
                e.setRespawnLocation(this.main.bluespawn);
            }
            this.main.stuff(pl);
            if ((!this.main.game.equipeBlue.isInTeam(pl)) && (!this.main.game.equipeRed.isInTeam(pl))) {
                pl.getInventory().setItem(4, this.main.menu.itemMenu);
            }
        }
        else
        {
            pl.getInventory().setItem(4, this.main.menu.itemMenu);
            e.setRespawnLocation(this.main.lobby);
        }
    }

    @EventHandler
    public void onDrops(PlayerDropItemEvent e)
    {
        if (e.getItemDrop().equals(this.main.menu.itemMenu)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void Break(BlockBreakEvent e)
    {
        Player pl = e.getPlayer();
        Location to = e.getBlock().getLocation();
        if ((pl.getWorld() == this.main.worldspawn) && (pl.getGameMode() != GameMode.CREATIVE)) {
            e.setCancelled(!this.main.getConfig().getBoolean("Worlds.Build_on_WorldSpawn"));
        }
        if ((this.main.game.running) && (pl.getWorld() == this.main.world)) {
            if (((this.main.redspawn.getBlockY() + 2 >= to.getBlockY()) && (to.getBlockY() >= this.main.redspawn.getBlockY())) || ((this.main.redspawn.getBlockY() - 2 <= to.getBlockY()) && (to.getBlockY() <= this.main.redspawn.getBlockY()) && (
                    ((this.main.redspawn.getBlockX() + 4 >= to.getBlockX()) && (to.getBlockX() >= this.main.redspawn.getBlockX() - 4)) || ((this.main.redspawn.getBlockX() - 4 <= to.getBlockX()) && (to.getBlockX() <= this.main.redspawn.getBlockX() + 4) && (
                            ((this.main.redspawn.getBlockZ() + 4 >= to.getBlockZ()) && (to.getBlockZ() >= this.main.redspawn.getBlockZ() - 4)) || ((this.main.redspawn.getBlockZ() - 4 <= to.getBlockZ()) && (to.getBlockZ() <= this.main.redspawn.getBlockZ() + 4))))))) {
                e.setCancelled(true);
                pl.sendMessage(ChatColor.RED + "Vous ne pouvez pas détruire les blocs appartenants aux spawn.");
            }
            if (((this.main.bluespawn.getBlockY() + 2 >= to.getBlockY()) && (to.getBlockY() >= this.main.bluespawn.getBlockY())) || ((this.main.bluespawn.getBlockY() - 2 <= to.getBlockY()) && (to.getBlockY() <= this.main.bluespawn.getBlockY()) && (
                    ((this.main.bluespawn.getBlockX() + 4 >= to.getBlockX()) && (to.getBlockX() >= this.main.bluespawn.getBlockX() - 4)) || ((this.main.bluespawn.getBlockX() - 4 <= to.getBlockX()) && (to.getBlockX() <= this.main.bluespawn.getBlockX() + 4) && (
                            ((this.main.bluespawn.getBlockZ() + 4 >= to.getBlockZ()) && (to.getBlockZ() >= this.main.bluespawn.getBlockZ() - 4)) || ((this.main.bluespawn.getBlockZ() - 4 <= to.getBlockZ()) && (to.getBlockZ() <= this.main.bluespawn.getBlockZ() + 4))))))) {
                e.setCancelled(true);
                pl.sendMessage(ChatColor.RED + "Vous ne pouvez pas détruire les blocs appartenants aux spawn.");
            }
            if ((this.main.game.pred.isInLocation(to)) || (this.main.game.pblue.isInLocation(to))) {
                e.setCancelled(true);
                pl.sendMessage(ChatColor.RED + "Vous ne pouvez pas casser les blocs appartenants à la piscine.");
            }
            if ((this.main.game.running) && (e.getBlock().getType() == Material.CHEST)) {
                Location lo = e.getBlock().getLocation();
                if ((this.main.game.equipeBlue.isInTeam(pl)) &&
                        (this.main.game.regionred.isInLocation(lo))) {
                    e.setCancelled(true);
                }
                if ((this.main.game.equipeRed.isInTeam(pl)) &&
                        (this.main.game.regionblue.isInLocation(lo))) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        Player pl = e.getPlayer();
        if (this.main.scoreboard) {
            pl.setScoreboard(this.main.game.sc);
        }
        if ((!this.main.game.running) && (pl.getWorld() == this.main.world)) {
            pl.teleport(this.main.lobby);
        }
        if (!pl.hasPlayedBefore())
        {
            pl.setGameMode(GameMode.SURVIVAL);
            pl.teleport(this.main.lobby);
            pl.getInventory().setItem(this.main.getConfig().getInt("Menu.Position"), this.main.menu.itemMenu);
            pl.getInventory().setHeldItemSlot(this.main.getConfig().getInt("Menu.Position"));
            ItemStack[] arrayOfItemStack;
            int j = (arrayOfItemStack = pl.getInventory().getArmorContents()).length;
            for (int i = 0; i < j; i++)
            {
                ItemStack is = arrayOfItemStack[i];
                is.setType(Material.AIR);
            }
        }
        if (!this.main.game.running)
        {
            pl.getInventory().setItem(this.main.getConfig().getInt("Menu.Position"), this.main.menu.itemMenu);
            pl.getInventory().setHeldItemSlot(this.main.getConfig().getInt("Menu.Position"));
        }
    }

    @EventHandler
    public void onChest(PlayerInteractEvent e)
    {
        if ((e.getAction() == Action.RIGHT_CLICK_BLOCK) && (this.main.game.running) && (e.getClickedBlock().getWorld() == this.main.world) &&
                (e.getClickedBlock().getType() == Material.CHEST) && (!this.main.getConfig().getBoolean("CanOpenAdverseChests")))
        {
            Player pl = e.getPlayer();

            Location lo = e.getClickedBlock().getLocation();
            if ((this.main.game.equipeBlue.isInTeam(pl)) && (this.main.game.regionred.isInLocation(lo))) {
                e.setCancelled(true);
            }
            if ((this.main.game.equipeRed.isInTeam(pl)) && (this.main.game.regionblue.isInLocation(lo))) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBuild(BlockPlaceEvent e)
    {
        Player pl = e.getPlayer();
        Location lo = e.getBlock().getLocation();
        if ((pl.getWorld() == this.main.worldspawn) && (pl.getGameMode() != GameMode.CREATIVE)) {
            e.setCancelled(!this.main.getConfig().getBoolean("Worlds.Build_on_WorldSpawn"));
        }
        if ((this.main.game.running) && (pl.getWorld() == this.main.world))
        {
            if (((this.main.redspawn.getBlockY() + 2 >= lo.getBlockY()) && (lo.getBlockY() >= this.main.redspawn.getBlockY() - 2)) || ((this.main.redspawn.getBlockY() - 2 <= lo.getBlockY()) && (lo.getBlockY() <= this.main.redspawn.getBlockY() + 2) && (
                    ((this.main.redspawn.getBlockX() + 4 >= lo.getBlockX()) && (lo.getBlockX() >= this.main.redspawn.getBlockX() - 4)) || ((this.main.redspawn.getBlockX() - 4 <= lo.getBlockX()) && (lo.getBlockX() <= this.main.redspawn.getBlockX() + 4) && (
                            ((this.main.redspawn.getBlockZ() + 4 >= lo.getBlockZ()) && (lo.getBlockZ() >= this.main.redspawn.getBlockZ() - 4)) || ((this.main.redspawn.getBlockZ() - 4 <= lo.getBlockZ()) && (lo.getBlockZ() <= this.main.redspawn.getBlockZ() + 4)))))))
            {
                e.setCancelled(true);
                pl.sendMessage(ChatColor.RED + "Vous ne pouvez pas poser de blocs au spawn.");
            }
            if (((this.main.bluespawn.getBlockY() + 2 >= lo.getBlockY()) && (lo.getBlockY() >= this.main.bluespawn.getBlockY() - 2)) || ((this.main.bluespawn.getBlockY() - 2 <= lo.getBlockY()) && (lo.getBlockY() <= this.main.bluespawn.getBlockY() + 2) && (
                    ((this.main.bluespawn.getBlockX() + 4 >= lo.getBlockX()) && (lo.getBlockX() >= this.main.bluespawn.getBlockX() - 4)) || ((this.main.bluespawn.getBlockX() - 4 <= lo.getBlockX()) && (lo.getBlockX() <= this.main.bluespawn.getBlockX() + 4) && (
                            ((this.main.bluespawn.getBlockZ() + 4 >= lo.getBlockZ()) && (lo.getBlockZ() >= this.main.bluespawn.getBlockZ() - 4)) || ((this.main.bluespawn.getBlockZ() - 3 <= lo.getBlockZ()) && (lo.getBlockZ() <= this.main.bluespawn.getBlockZ() + 4)))))))
            {
                e.setCancelled(true);
                pl.sendMessage(ChatColor.RED + "Vous ne pouvez pas poser de blocs au spawn.");
            }
            if (e.getBlock().getType() == Material.CHEST)
            {
                if (this.main.game.equipeBlue.isInTeam(pl)) {
                    e.setCancelled((this.main.game.pblue.isInLocation(lo)) || (this.main.game.regionred.isInLocation(lo)));
                }
                if (this.main.game.equipeRed.isInTeam(pl)) {
                    e.setCancelled((this.main.game.regionblue.isInLocation(lo)) || (this.main.game.pred.isInLocation(lo)));
                }
            }
        }
    }
}