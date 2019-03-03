package fr.omegaserv.tower;


import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TeamSystem {

    public Team team;
    public Score score;
    public int pts;
    public String name;
    public Location spawn;
    public Location lobby;
    private GameManager game;
    public Color c;
    ChatColor chatColor;
    DyeColor dyeColor;

    public TeamSystem(GameManager game, String name, ChatColor c, DyeColor dyeColor, Location spawn, Location lobby) {
        this.game = game;
        this.lobby = lobby;
        this.chatColor = c;
        this.c = dyeColor.getColor();
        this.name = (c + name + ChatColor.RESET);
        this.dyeColor = dyeColor;
        this.spawn = spawn;
        if (game.sc.getTeam(name) != null) {
            game.sc.getTeam(name).unregister();
        }
        this.team = game.sc.registerNewTeam(this.name);
        this.team.setPrefix(c + " ");
        this.team.setAllowFriendlyFire(false);
    }

    public void addPlayer(Player pl)
    {
        for (Team t : this.game.sc.getTeams()) {
            t.removePlayer(pl);
        }
        this.team.addPlayer(pl);
        pl.sendMessage(ChatColor.AQUA + "Vous avez été ajouté à la team " + this.name);
        setArmorTeam(pl.getPlayer());
        if (!this.game.running) {
            pl.teleport(this.lobby);
        } else {
            pl.teleport(this.spawn);
        }
        pl.setDisplayName(this.chatColor + pl.getDisplayName() + ChatColor.RESET);
    }

    public boolean isInTeam(Player pl)
    {
        for (OfflinePlayer op : this.team.getPlayers()) {
            if (pl.getPlayer().equals(op)) {
                return true;
            }
        }
        return false;
    }

    public void setArmorTeam(Player pl)
    {
        ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta meta = (LeatherArmorMeta)chest.getItemMeta();
        meta.setColor(this.dyeColor.getColor());
        chest.setItemMeta(meta);
        pl.getInventory().setChestplate(chest);
    }

    public void startPlayers()
    {
        for (OfflinePlayer pl : this.team.getPlayers()) {
            if (pl.isOnline())
            {
                Player pla = (Player)pl;
                pla.setExp(0.0F);
                pla.setHealth(20.0D);
                pla.setFoodLevel(20);
                pla.setExp(0.0F);
                pla.setLevel(0);
                pla.setExhaustion(5.0F);
                pla.closeInventory();
                pla.getInventory().clear();
                pla.setGameMode(GameMode.SURVIVAL);
                pla.teleport(this.spawn);
                pla.playSound(pla.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0F, 1.0F);
                stuffPlayer(pla);
            }
        }
    }

    public void addPoint(Player pl)
    {
        pl.teleport(this.spawn);
        this.pts += 1;
        Bukkit.broadcastMessage("§6§lL'équipe " + this.name + " §6§lmarque un point. (§1§l" + Integer.toString(this.game.equipeBlue.pts) +
                "§6§l/§c§l" + Integer.toString(this.game.equipeRed.pts) + "§6§l)");
        for (Player ap : Bukkit.getOnlinePlayers()) {
            ap.playSound(ap.getLocation(), Sound.NOTE_PIANO, 1.0F, 1.0F);
        }
        this.game.m.game.points.getScore(pl).setScore(this.game.points.getScore(pl).getScore() + 1);
        this.game.m.game.Scupdate();
        if (this.pts == this.game.maxPoints) {
            win(this);
        }
    }

    public void win(final TeamSystem e)
    {
        Bukkit.broadcastMessage(ChatColor.GREEN + "L'équipe " + this.team.getDisplayName() + ChatColor.GREEN + " a gagné !");
        this.game.stop(this.game.m.getConfig().getInt("Timers.EndTimer") * 20);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this.game.m, new Runnable()
        {
            public void run()
            {
                for (OfflinePlayer op : e.team.getPlayers()) {
                    if (op.isOnline()) {
                        FireworkEffect fre = FireworkEffect.builder().flicker(true).withColor(e.c).withFade(Color.PURPLE).with(FireworkEffect.Type.STAR).trail(true).build();
                        Player gg = (Player)op;
                        Firework fw = (Firework)TeamSystem.this.game.m.world.spawn(gg.getLocation(), Firework.class);
                        FireworkMeta me = fw.getFireworkMeta();
                        me.addEffect(fre);
                        fw.setFireworkMeta(me);
                    }
                }
            }
        }, 20L, 20L);
    }

    public void stuffPlayer(final Player pl)
    {
        pl.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 300, 1));
        this.game.m.getServer().getScheduler().scheduleSyncDelayedTask(this.game.m, new Runnable()
        {
            public void run()
            {
                pl.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 300, 1));
            }
        }, 5L);

        pl.getInventory().setItem(0, new ItemStack(393, 8));
        pl.getInventory().setArmorContents(new ItemStack[] { new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR) });

        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);

        LeatherArmorMeta chestmeta = (LeatherArmorMeta)chestplate.getItemMeta();
        chestmeta.setColor(this.c);
        chestplate.setItemMeta(chestmeta);
        pl.getInventory().setChestplate(chestplate);

        LeatherArmorMeta helmeta = (LeatherArmorMeta)helmet.getItemMeta();
        helmeta.setColor(this.c);
        helmet.setItemMeta(helmeta);
        pl.getInventory().setHelmet(helmet);

        LeatherArmorMeta bootmeta = (LeatherArmorMeta)boots.getItemMeta();
        bootmeta.setColor(this.c);
        boots.setItemMeta(bootmeta);
        pl.getInventory().setBoots(boots);

        LeatherArmorMeta leggmeta = (LeatherArmorMeta)leggings.getItemMeta();
        leggmeta.setColor(this.c);
        leggings.setItemMeta(leggmeta);
        pl.getInventory().setLeggings(leggings);
    }

    public int getSize()
    {
        int s = 0;

        return s;
    }

    public ArrayList<Player> getPlayers()
    {
        ArrayList<Player> l = new ArrayList();
        for (OfflinePlayer op : this.team.getPlayers()) {
            l.add(op.getPlayer());
        }
        return l;
    }

    public ArrayList<String> getPlayersName()
    {
        ArrayList<String> n = new ArrayList();
        for (OfflinePlayer op : this.team.getPlayers()) {
            n.add(op.getName());
        }
        return n;
    }
}
