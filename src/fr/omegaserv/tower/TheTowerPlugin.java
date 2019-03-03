package fr.omegaserv.tower;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TheTowerPlugin extends JavaPlugin implements Listener {
    public World world;
    public World worldspawn;
    public boolean runauto = getConfig().getBoolean("Teams.Run_auto");
    public boolean bungee = false;
    public boolean pvpWorldSpawn;
    public boolean Pvp_on_WorldSpawn;
    public boolean Build_on_WorldSpawn;
    public boolean scoreboard;
    public int MaxPlayersTeam = getConfig().getInt("Teams.MaxPlayersPerTeams");
    public int compte = 10;
    public CommandExecutor cmdexec = new TowersCommandExecutor(this);
    public GameManager game;
    public Location middle;
    public Location lobby;
    public Location bluelobby;
    public Location redlobby;
    public Location redspawn;
    public Location bluespawn;
    public Location poolblue1;
    public Location poolblue2;
    public Location poolred2;
    public Location poolred1;
    public Location regionblue1;
    public Location regionblue2;
    public Location regionred1;
    public Location regionred2;
    Player pausePlayer;
    File backup = new File("plugins/TheTowers/backups/");
    File MapTheTowers = new File("plugins/TheTowers/backups/TheTowers");
    Menu menu;

    public void onEnable()
    {
        saveDefaultConfig();
        if ((!Bukkit.getVersion().contains("1.8")) && (!Bukkit.getVersion().contains("1.9")))
        {
            getLogger().log(Level.SEVERE, "Attention, vous possédez une mauvaise version du plugin, vous devez etre en 1.8/1.9");
            error();
            return;
        }
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.worldspawn = Bukkit.getWorld(getConfig().getString("Worlds.WorldSpawn"));

        this.bungee = getConfig().getBoolean("Worlds.BungeeCoord_Support");
        this.lobby = new Location(this.worldspawn, getConfig().getInt("Coordinates.Lobby.x"), getConfig().getInt("Coordinates.Lobby.y") + 1, getConfig().getInt("Coordinates.Lobby.z"));
        this.compte = getConfig().getInt("Timers.StartTimer");

        Configreload();
        if (this.world != null) {
            for (Player ap : this.world.getPlayers()) {
                ap.teleport(this.lobby);
            }
        }
        reloadWorld();
        for (Player ap : this.worldspawn.getPlayers()) {
            ap.teleport(this.lobby);
        }
        Configreload();
        this.game = new GameManager(this);
        this.menu = new Menu(this);
        getServer().getPluginManager().registerEvents(new Events(this), this);
        getServer().getPluginManager().registerEvents(this.menu, this);
        getCommand("towers").setExecutor(this.cmdexec);
        getCommand("tw").setExecutor(this.cmdexec);
        for (Player ap : this.worldspawn.getPlayers()) {
            replacePlayer(ap);
        }
        for (Player ap : this.world.getPlayers()) {
            replacePlayer(ap);
        }
    }

    public void replacePlayer(Player ap)
    {
        ap.setFoodLevel(20);
        ap.removePotionEffect(PotionEffectType.INVISIBILITY);
        ap.setGameMode(GameMode.SURVIVAL);
        ap.getInventory().clear();
        ap.getInventory().setArmorContents(new ItemStack[] { new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR) });
        ap.getInventory().setItem(getConfig().getInt("Menu.Position"), this.menu.itemMenu);
        ap.getInventory().setHeldItemSlot(getConfig().getInt("Menu.Position"));
        ap.setLevel(0);
        ap.teleport(this.lobby);
    }

    public void onDisable()
    {
        for (Player p : this.game.getPlayers()) {
            LeaveTeam(p);
        }
        if (this.game != null)
        {
            for (Team tm : this.game.sc.getTeams())
            {
                for (Player ap : Bukkit.getOnlinePlayers()) {
                    tm.removePlayer(ap);
                }
                tm.setPrefix(ChatColor.WHITE+"");
                tm.unregister();
            }
            this.game.sc.clearSlot(DisplaySlot.SIDEBAR);
        }
    }

    public void reloadWorld()
    {
        this.backup.mkdirs();
        File worldgame = new File("TheTowers");
        if (worldgame.exists())
        {
            Bukkit.unloadWorld(Bukkit.getWorld("TheTowers"), true);
            MapManager.deleteWorld(worldgame);
        }
        if ((this.MapTheTowers.exists()) && (this.MapTheTowers.isDirectory()))
        {
            try
            {
                MapManager.Copy(this.MapTheTowers, worldgame);
                WorldCreator ok = new WorldCreator("TheTowers");
                World wor = Bukkit.createWorld(ok);
                Bukkit.getWorlds().add(wor);
            }
            catch (IOException e)
            {
                System.out.println("Erreur: La copie de la map a echoue.");
            }
        }
        else
        {
            getLogger().log(Level.SEVERE, "Le monde TheTowers n'existe pas dans le dossier backup. Veuillez r�parer cette erreur avant de reload.");
            System.out.println("ok");
            error();
            return;
        }
        this.world = Bukkit.getWorld("TheTowers");
        this.middle = new Location(this.world, getConfig().getInt("Coordinates.Spawns.SpectatorSpawn.x"), getConfig().getInt("Coordinates.Spawns.SpectatorSpawn.y") + 1, getConfig().getInt("Coordinates.Spawns.SpectatorSpawn.z"), (float)getConfig().getDouble("Coordinates.Spawns.Blue.yaw"), 0.0F);this.world.setDifficulty(Difficulty.NORMAL);
        this.world.setGameRuleValue("doMobSpawning", "false");
        this.world.setAnimalSpawnLimit(0);
        this.world.setWaterAnimalSpawnLimit(0);
        this.world.setSpawnLocation(this.lobby.getBlockX(), this.lobby.getBlockY(), this.lobby.getBlockZ());
        this.world.setGameRuleValue("doDaylightCycle", Boolean.valueOf(getConfig().getBoolean("Daylightcycle")).toString());
        this.world.setGameRuleValue("naturalRegeneration", getConfig().getString("NaturalRegeneration"));
    }

    public void connect(Player player, String server){
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

    public void error()
    {
        getLogger().warning("Desactivation du plugin TheTowers Reloaded");
        Bukkit.getPluginManager().disablePlugin(this);
    }

    public void Configreload()
    {
        reloadConfig();
        this.worldspawn = Bukkit.getWorld(getConfig().getString("Worlds.WorldSpawn"));
        this.world = Bukkit.getWorld("TheTowers");
        if (this.worldspawn == null)
        {
            this.worldspawn = ((World)getServer().getWorlds().get(0));
            getLogger().log(Level.SEVERE, "Le monde indique dans le fichier de configuration n'existe pas, choix d'un autre monde : " + this.worldspawn.getName());
        }
        getConfig().set("Worlds.WorldSpawn", this.worldspawn.getName());
        saveConfig();
        this.worldspawn.setPVP(getConfig().getBoolean("Worlds.Pvp_on_WorldSpawn"));
        this.lobby = new Location(this.worldspawn, getConfig().getInt("Coordinates.Lobby.x"), getConfig().getInt("Coordinates.Lobby.y") + 1, getConfig().getInt("Coordinates.Lobby.z"), (float)getConfig().getDouble("Coordinates.Lobby.yaw"), 0.0F);
        this.scoreboard = getConfig().getBoolean("Scoreboard.Enabled");
        this.bluelobby = new Location(this.worldspawn, getConfig().getInt("Coordinates.Waitrooms.Blue.x"), getConfig().getInt("Coordinates.Waitrooms.Blue.y") + 1, getConfig().getInt("Coordinates.Waitrooms.Blue.z"), (float)getConfig().getDouble("Coordinates.Waitromms.Blue.yaw"), 0.0F);
        this.redlobby = new Location(this.worldspawn, getConfig().getInt("Coordinates.Waitrooms.Red.x"), getConfig().getInt("Coordinates.Waitrooms.Red.y") + 1, getConfig().getInt("Coordinates.Waitrooms.Red.z"), (float)getConfig().getDouble("Coordinates.Waitrooms.Red.yaw"), 0.0F);
        this.bluespawn = new Location(this.world, getConfig().getInt("Coordinates.Spawns.Blue.x"), getConfig().getInt("Coordinates.Spawns.Blue.y") + 1, getConfig().getInt("Coordinates.Spawns.Blue.z"), (float)getConfig().getDouble("Coordinates.Spawns.Blue.yaw"), 0.0F);
        this.redspawn = new Location(this.world, getConfig().getInt("Coordinates.Spawns.Red.x"), getConfig().getInt("Coordinates.Spawns.Red.y") + 1, getConfig().getInt("Coordinates.Spawns.Red.z"), (float)getConfig().getDouble("Coordinates.Spawns.Red.yaw"), 0.0F);
        this.poolred1 = new Location(this.world, getConfig().getInt("Coordinates.Pools.red.1.x"), getConfig().getInt("Coordinates.Pools.red.1.y"), getConfig().getInt("Coordinates.Pools.red.1.z"));
        this.poolblue1 = new Location(this.world, getConfig().getInt("Coordinates.Pools.blue.1.x"), getConfig().getInt("Coordinates.Pools.blue.1.y"), getConfig().getInt("Coordinates.Pools.blue.1.z"));
        this.poolred2 = new Location(this.world, getConfig().getInt("Coordinates.Pools.red.2.x"), getConfig().getInt("Coordinates.Pools.red.2.y"), getConfig().getInt("Coordinates.Pools.red.2.z"));
        this.poolblue2 = new Location(this.world, getConfig().getInt("Coordinates.Pools.blue.2.x"), getConfig().getInt("Coordinates.Pools.blue.2.y"), getConfig().getInt("Coordinates.Pools.blue.2.z"));
        this.regionred1 = new Location(this.world, getConfig().getInt("Coordinates.Regions.Red.1.x"), getConfig().getInt("Coordinates.Regions.Red.1.y"), getConfig().getInt("Coordinates.Regions.Red.1.z"));
        this.regionblue1 = new Location(this.world, getConfig().getInt("Coordinates.Regions.Blue.1.x"), getConfig().getInt("Coordinates.Regions.Blue.1.y"), getConfig().getInt("Coordinates.Regions.Blue.1.z"));
        this.regionred2 = new Location(this.world, getConfig().getInt("Coordinates.Regions.Red.2.x"), getConfig().getInt("Coordinates.Regions.Red.2.y"), getConfig().getInt("Coordinates.Regions.Red.2.z"));
        this.regionblue2 = new Location(this.world, getConfig().getInt("Coordinates.Regions.Blue.2.x"), getConfig().getInt("Coordinates.Regions.Blue.2.y"), getConfig().getInt("Coordinates.Regions.Blue.2.z"));
        this.compte = getConfig().getInt("Timers.StartTimer");
    }

    public void LeaveTeam(Player pl)
    {
        for (Team tm : this.game.sc.getTeams()) {
            tm.removePlayer(pl);
        }
        pl.sendMessage(ChatColor.RED + "Vous ne faites désormais plus partie de l'équipe.");
        pl.teleport(this.lobby);
        pl.setDisplayName(pl.getName());
        pl.getActivePotionEffects().clear();
        if (this.game.running)
        {
            pl.getInventory().clear();
            ItemStack[] arrayOfItemStack;
            int j = (arrayOfItemStack = pl.getInventory().getArmorContents()).length;
            for (int i = 0; i < j; i++)
            {
                ItemStack is = arrayOfItemStack[i];
                is.setType(Material.AIR);
            }
            pl.getInventory().setItem(4, this.menu.itemMenu);
        }
        else
        {
            pl.getInventory().setChestplate(new ItemStack(Material.AIR));
        }
    }

    public void update()
    {
        if ((this.runauto) && (this.game.equipeBlue.team.getSize() == this.MaxPlayersTeam) && (this.game.equipeRed.team.getSize() == this.MaxPlayersTeam))
        {
            System.out.println("La partie a démarre automatiquement avec " + this.MaxPlayersTeam + " joueurs par équipes");
            this.game.start();
        }
    }

    public void stuff(Player pl)
    {
        if (this.game.equipeBlue.isInTeam(pl)) {
            this.game.equipeBlue.stuffPlayer(pl);
        } else if (this.game.equipeRed.isInTeam(pl)) {
            this.game.equipeRed.stuffPlayer(pl);
        }
    }

    public void reload()
    {
        if (this.bungee) {
            for (Player ap : this.world.getPlayers())
            {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(getConfig().getString("Worlds.BungeeCoord_Lobby_Server"));
                ap.sendPluginMessage(this, "BungeeCord", out.toByteArray());
            }
        }
        getServer().getPluginManager().disablePlugin(this);
        getServer().getPluginManager().enablePlugin(this);
    }
}
