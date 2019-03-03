package fr.omegaserv.tower;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class GameManager
{
    public boolean pause = false;
    public boolean running = false;
    private NumberFormat format = new DecimalFormat("00");
    public Scoreboard sc;
    public Score redpts;
    public Score bluepts;
    public Score time;
    public Score Maxpoints;
    public int maxPoints;
    private int compte;
    private String ScTitle;
    private int secondes;
    private int minutes;
    TheTowerPlugin m;
    public Objective title;
    public Objective points;
    public Objective health;
    public TeamSystem equipeRed;
    public TeamSystem equipeBlue;
    public Cube pred;
    public Cube pblue;
    public Cube regionred;
    public Cube regionblue;

    public GameManager(TheTowerPlugin m)
    {
        this.m = m;
        this.sc = Bukkit.getScoreboardManager().getNewScoreboard();
        for (Objective obj : this.sc.getObjectives()) {
            obj.unregister();
        }
        this.health = this.sc.registerNewObjective("health", "health");
        this.health.setDisplaySlot(DisplaySlot.BELOW_NAME);
        this.health.setDisplayName(" /20");
        if (m.scoreboard) {
            this.title = this.sc.registerNewObjective("Sidebar", "dummy");
        }
        this.points = this.sc.registerNewObjective("Points", "dummy");
        this.points.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        this.equipeBlue = new TeamSystem(this, "Bleu", ChatColor.BLUE, DyeColor.BLUE, m.bluespawn, m.bluelobby);
        this.equipeRed = new TeamSystem(this, "Rouge", ChatColor.RED, DyeColor.RED, m.redspawn, m.redlobby);
        this.pred = new Cube(m.poolred1, m.poolred2);
        this.pblue = new Cube(m.poolblue1, m.poolblue2);
        this.regionred = new Cube(m.regionred1, m.regionred2);
        this.regionblue = new Cube(m.regionblue1, m.regionblue2);

        Scupdate();
        this.maxPoints = m.getConfig().getInt("Scoreboard.Points");
        if (m.scoreboard) {
            for (Player ap : Bukkit.getOnlinePlayers()) {
                ap.setScoreboard(this.sc);
            }
        }
    }

    public void stop(int tps)
    {
        this.running = false;
        this.secondes = Integer.valueOf(0).intValue();
        this.minutes = Integer.valueOf(0).intValue();
        Bukkit.broadcastMessage(ChatColor.GREEN + "La partie est terminée.");
        for (Player ap : this.m.world.getPlayers())
        {
            ap.setGameMode(GameMode.CREATIVE);
            ap.getInventory().setItem(1, new ItemStack(Material.FLINT_AND_STEEL));
            ap.getInventory().setItem(0, new ItemStack(Material.TNT));
        }
        if (this.m.bungee) {
            this.m.Configreload();
        }
        Bukkit.getScheduler().cancelTasks(this.m);
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.m, new Runnable()
        {
            public void run()
            {
                GameManager.this.m.reload();
                File worldgame = new File("TheTowers");
                Bukkit.unloadWorld(GameManager.this.m.world, true);
                MapManager.deleteWorld(worldgame);
                if ((GameManager.this.m.MapTheTowers.exists()) && (GameManager.this.m.MapTheTowers.isDirectory())) {
                    try
                    {
                        MapManager.Copy(GameManager.this.m.MapTheTowers, worldgame);
                    }
                    catch (IOException e)
                    {
                        System.out.println("Erreur: La copie de la map a echoue.");
                    }
                } else {
                    GameManager.this.m.getLogger().log(Level.SEVERE, "Attention, la map sélectionnée dans le config n'existe pas. Veuillez réparer cette erreur puis redémarrer votre serveur sous peine de crash.");
                }
                WorldCreator ok = new WorldCreator("TheTowers");
                World wor = Bukkit.createWorld(ok);
                Bukkit.getWorlds().add(wor);
            }
        }, tps);
    }

    public void timer()
    {
        Bukkit.getScheduler().cancelTasks(this.m);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this.m, new Runnable()
        {
            public void run()
            {
                if ((GameManager.this.running) && (!GameManager.this.pause)) {
                    GameManager.this.secondes += 1;
                }
                if (GameManager.this.secondes == 60)
                {
                    GameManager.this.secondes = 0;
                    GameManager.this.minutes += 1;
                }
                GameManager.this.Scupdate();
            }
        }, 20L, 20L);
    }

    public void pause(Player p)
    {
        this.pause = true;
        Bukkit.broadcastMessage(ChatColor.AQUA + "Le jeu est en pause.");
        this.m.pausePlayer = p;
    }

    public void unpause()
    {
        this.pause = false;
        Bukkit.broadcastMessage(ChatColor.AQUA + "Le jeu n'est plus en pause.");
    }

    public void start()
    {
        this.running = true;
        this.compte = this.m.getConfig().getInt("Timers.StartTimer");
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this.m, new Runnable()
        {
            public void run()
            {
                if (GameManager.this.compte == 0)
                {
                    GameManager.this.compte -= 1;
                    Bukkit.broadcastMessage(ChatColor.GREEN + "La partie démarre.");
                    GameManager.this.equipeBlue.startPlayers();
                    GameManager.this.equipeRed.startPlayers();

                    GameManager.this.timer();
                }
                if (GameManager.this.compte > 0)
                {
                    if (GameManager.this.compte <= 5)
                    {
                        Bukkit.broadcastMessage(ChatColor.AQUA + "La partie démarre dans " + ChatColor.GOLD + GameManager.this.compte + " secondes.");
                        for (Player cp : GameManager.this.m.worldspawn.getPlayers()) {
                            cp.playSound(cp.getLocation(), Sound.NOTE_PLING, 1.0F, 0.0F);
                        }
                    }
                    for (Player cp : GameManager.this.m.worldspawn.getPlayers()) {
                        cp.setLevel(GameManager.this.compte);
                    }
                    GameManager.this.compte -= 1;
                }
            }
        }, 20L, 20L);
    }

    public void Scupdate()
    {
        this.title.unregister();
        this.title = this.sc.registerNewObjective("Sidebar", "dummy");
        this.ScTitle = this.m.getConfig().getString("Scoreboard.Title").replace("&", "§");
        this.title.setDisplayName(this.ScTitle);
        this.title.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.title.getScore(this.m.getConfig().getString("Scoreboard.ip").replace("&", "§")).setScore(1);
        this.title.getScore(this.format.format(this.minutes) + ":" + this.format.format(this.secondes)).setScore(3);
        this.title.getScore(ChatColor.RED + "  ").setScore(5);
        this.title.getScore(ChatColor.RED + " ").setScore(2);
        this.title.getScore(ChatColor.RED+"").setScore(6);
        this.title.getScore(ChatColor.RED + "Rouge : " + ChatColor.WHITE + this.equipeRed.pts).setScore(6);
        this.title.getScore(ChatColor.BLUE + "Bleue : " + ChatColor.WHITE + this.equipeBlue.pts).setScore(5);
        this.title.getScore(ChatColor.YELLOW + "Points : " + Integer.toString(this.maxPoints)).setScore(7);
    }

    public ArrayList<Player> getPlayers()
    {
        ArrayList<Player> pl = new ArrayList();
        for (Player ap : this.equipeBlue.getPlayers()) {
            pl.add(ap);
        }
        for (Player ap : this.equipeRed.getPlayers()) {
            pl.add(ap);
        }
        return pl;
    }
}
