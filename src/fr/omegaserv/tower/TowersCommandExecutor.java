package fr.omegaserv.tower;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TowersCommandExecutor implements CommandExecutor, TabCompleter {
    private TheTowerPlugin main;

    public TowersCommandExecutor(TheTowerPlugin main)
    {
        this.main = main;
    }

    public List<String> onTabComplete(CommandSender arg0, Command arg1, String label, String[] args)
    {
        List<String> tab = new ArrayList();
        if ((label.equalsIgnoreCase("towers")) || (label.equalsIgnoreCase("tw"))) {
            if (args.length == 1)
            {
                if (args[0].equals(""))
                {
                    tab.add("game");
                    tab.add("team");
                    tab.add("setwaitroom");
                    tab.add("setspawm");
                    tab.add("setregion");
                    tab.add("setpool");
                    tab.add("reload");
                    tab.add("tp");
                }
                else if (args[0].charAt(0) == 'g')
                {
                    tab.add("game");
                }
                else if (args[0].charAt(0) == 't')
                {
                    tab.add("team");tab.add("tp");
                }
                else if (args[0].charAt(0) == 's')
                {
                    tab.add("setwaitroom");tab.add("setspawn");tab.add("setregion");tab.add("setpool");
                }
                else if (args[0].charAt(0) == 'r')
                {
                    tab.add("reload");
                }
            }
            else if (args.length == 2)
            {
                if (args[0].equalsIgnoreCase("game"))
                {
                    tab.add("start");
                    tab.add("stop");
                    tab.add("pause");
                    tab.add("restart");
                }
                else if (args[0].equalsIgnoreCase("team"))
                {
                    tab.add("join");
                    tab.add("leave");
                }
            }
            else if (args.length == 3)
            {
                if ((args[1].equalsIgnoreCase("leave")) || (args[1].equalsIgnoreCase("join"))) {
                    for (Player ap : Bukkit.getOnlinePlayers()) {
                        tab.add(ap.getName());
                    }
                }
            }
            else if (args.length == 4) {
                if (args[1].equalsIgnoreCase("join"))
                {
                    tab.add("red");
                    tab.add("blue");
                }
            }
        }
        return tab;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if ((sender.isOp()) || (sender.hasPermission("towers.admin"))) {
            if ((args.length == 0) || (args[0].equalsIgnoreCase("help")))
            {
                print(sender, "§b------ Commandes liées au jeu ------");
                print(sender, "§6/towers team §r: Gère toutes les commandes assignées aux équipes. Executez /towers team pour plus de details.");
                print(sender, "§6/towers game §r: Gère les commandes relatives au jeu. Executez /towers game pour plus de details.");
                print(sender, "§6/towers reload §r: Permet de prendre en compte les changements effectuée dans le fichier config du plugin.");
            }
            else if (args[0].equalsIgnoreCase("reload"))
            {
                this.main.Configreload();
                print(sender, ChatColor.GREEN + "Fichier config reload.");
            }
            else if (args[0].equalsIgnoreCase("game"))
            {
                if (args.length != 2) {
                    print(sender, "/towers game <start|stop|pause|restart> : Permet de gerer le jeu (arret, pause..).");
                } else if (args[1].equalsIgnoreCase("start"))
                {
                    if (this.main.game.running) {
                        print(sender, ChatColor.RED + "Une partie est en cours.");
                    } else {
                        this.main.game.start();
                    }
                }
                else if ((args[1].equalsIgnoreCase("pause")) && (this.main.game.running))
                {
                    if (!this.main.game.pause)
                    {
                        if ((sender instanceof Player)) {
                            this.main.game.pause((Player)sender);
                        } else {
                            this.main.game.pause(null);
                        }
                    }
                    else {
                        print(sender, "Le jeu est deja en pause");
                    }
                }
                else if (args[1].equalsIgnoreCase("restart"))
                {
                    if (this.main.game.pause) {
                        this.main.game.unpause();
                    } else {
                        print(sender, "Le jeu n'est pas en pause.");
                    }
                }
                else if (args[1].equalsIgnoreCase("stop"))
                {
                    if (this.main.game.running) {
                        this.main.game.stop(5);
                    } else {
                        print(sender, "La partie n'a pas demarree.");
                    }
                }
                else {
                    print(sender, "Cette commande n'existe pas. Tapez /towers game pour plus d'informations.");
                }
            }
            else if (args[0].equalsIgnoreCase("team"))
            {
                if (args.length < 3)
                {
                    print(sender, "/towers team size <nombre> : Change le limite du nombre de joueurs par equipes.");
                    print(sender, "/towers team leave <joueur> : Enleve le joueur de son equipe.");
                    print(sender, "/towers team join <joueur> <blue|red> : Ajoute le joueur a l'�quipe Bleue ou Rouge.");
                }
                else if (args[1].equalsIgnoreCase("size"))
                {
                    int size = Integer.valueOf(args[2]).intValue();
                    this.main.MaxPlayersTeam = size;
                    print(sender, "Taille des equipes changées a " + this.main.MaxPlayersTeam);
                    this.main.getConfig().set("Teams.MaxPlayersPerTeams", Integer.valueOf(size));
                }
                else if (args[1].equalsIgnoreCase("leave"))
                {
                    OfflinePlayer op = Bukkit.getOfflinePlayer(args[2]);
                    for (Team tm : this.main.game.sc.getTeams()) {
                        tm.removePlayer(op);
                    }
                    print(sender, "Le joueur " + args[2] + " a ete enleve de sa team.");
                    if (op.isOnline())
                    {
                        Player p = (Player)op;
                        this.main.LeaveTeam(p);
                    }
                }
                else if (args[1].equalsIgnoreCase("join"))
                {
                    if (args.length == 4)
                    {
                        OfflinePlayer op = Bukkit.getOfflinePlayer(args[2]);
                        if (args[3].equalsIgnoreCase("blue"))
                        {
                            if (op.isOnline())
                            {
                                Player p = (Player)op;
                                this.main.game.equipeBlue.addPlayer(p);
                                print(sender, "Le joueur a ete ajoute a la team Bleue");
                            }
                            else
                            {
                                print(sender, "Ce joueur n'est pas connecte");
                            }
                        }
                        else if (args[3].equalsIgnoreCase("red"))
                        {
                            if (op.isOnline())
                            {
                                Player p = (Player)op;
                                this.main.game.equipeRed.addPlayer(p);
                                print(sender, "Le joueur a ete ajoute a la team Rouge");
                            }
                            else
                            {
                                print(sender, ChatColor.RED + "Ce joueur n'est pas connecte");
                            }
                        }
                        else {
                            print(sender, "Erreur de syntaxe.");
                        }
                    }
                    else
                    {
                        print(sender, "Cette commande n'existe pas. Tapez /towers pour plus d'informations.");
                    }
                }
            }
            else if (!(sender instanceof Player))
            {
                print(sender, "Cette commande n'existe pas. Tapez /towers pour plus d'informations.");
            }
        }
        if ((sender instanceof Player))
        {
            Player pl = (Player)sender;
            if ((!pl.isOp()) && (!pl.hasPermission("towers.admin")))
            {
                pl.sendMessage(ChatColor.RED + "Vous n'avez pas la permission.");
            }
            else if (args.length == 0)
            {
                pl.sendMessage("§6/towers setpool : §rPlace les piscines en deux points.");
                pl.sendMessage("§6/towers setwaitroom : §rPlace les salles d'attentes.");
                pl.sendMessage("§6/towers setregion : §rPlace les zones des coffres appartenant � l'�quipe.");
                pl.sendMessage("§6/towers setspawn : §rPlace les points d'apparition.");
            }
            else if (args[0].equalsIgnoreCase("tp"))
            {
                if (args.length != 1) {
                    pl.sendMessage("§6/towers tp §r: Permet la téléportation entre le monde du jeu et celui du spawn.");
                } else if (pl.getWorld() == this.main.world) {
                    pl.teleport(this.main.lobby);
                } else {
                    pl.teleport(this.main.middle);
                }
            }
            else if (args[0].equalsIgnoreCase("setpool"))
            {
                if (args.length != 3)
                {
                    pl.sendMessage("�6/towers setpool <blue|red> <1|2> �r: Place la zone des piscines.");
                }
                else if ((args[1].equals("red")) || (args[1].equals("blue")))
                {
                    Location locpl = pl.getLocation();
                    String team = args[1].toLowerCase();
                    int i = Integer.valueOf(args[2]).intValue();
                    this.main.getConfig().set("Coordinates.Pools." + team + "." + i + ".x", Integer.valueOf(locpl.getBlockX()));
                    this.main.getConfig().set("Coordinates.Pools." + team + "." + i + ".y", Integer.valueOf(locpl.getBlockY()));
                    this.main.getConfig().set("Coordinates.Pools." + team + "." + i + ".z", Integer.valueOf(locpl.getBlockZ()));
                    this.main.saveConfig();
                    pl.sendMessage(ChatColor.DARK_GREEN + "Coordonn�es de la piscine enregistrées : " + locpl.getBlockX() + " / " + locpl.getBlockY() + " / " + locpl.getBlockZ() + " sur " + this.main.world.getName());
                }
                else
                {
                    pl.sendMessage(ChatColor.RED + "Cette commande n'existe pas. Tapez /towers setpool pour plus d'informations.");
                }
            }
            else if (args[0].equalsIgnoreCase("setregion"))
            {
                if (args.length != 3)
                {
                    pl.sendMessage("§6/towers region <blue|red|lobby> <1/2> §r: Place les zones des protections de coffre.");
                }
                else
                {
                    Location locpl = pl.getLocation();
                    if (args[1].equalsIgnoreCase("blue"))
                    {
                        int i = Integer.valueOf(args[2]).intValue();
                        this.main.getConfig().set("Coordinates.Regions.Blue." + i + ".x", Integer.valueOf(locpl.getBlockX()));
                        this.main.getConfig().set("Coordinates.Regions.Blue." + i + ".y", Integer.valueOf(locpl.getBlockY()));
                        this.main.getConfig().set("Coordinates.Regions.Blue." + i + ".z", Integer.valueOf(locpl.getBlockZ()));
                        this.main.saveConfig();

                        pl.sendMessage(ChatColor.DARK_GREEN + "Coordonnées enregistrées : " + locpl.getBlockX() + " / " + locpl.getBlockY() + " / " + locpl.getBlockZ() + " sur " + this.main.world.getName());
                    }
                    else if (args[1].equalsIgnoreCase("red"))
                    {
                        int i = Integer.valueOf(args[2]).intValue();
                        this.main.getConfig().set("Coordinates.Regions.Red." + i + ".x", Integer.valueOf(locpl.getBlockX()));
                        this.main.getConfig().set("Coordinates.Regions.Red." + i + ".y", Integer.valueOf(locpl.getBlockY()));
                        this.main.getConfig().set("Coordinates.Regions.Red." + i + ".z", Integer.valueOf(locpl.getBlockZ()));
                        this.main.saveConfig();
                        pl.sendMessage(ChatColor.DARK_GREEN + "Coordonnées enregistrées : " + locpl.getBlockX() + " / " + locpl.getBlockY() + " / " + locpl.getBlockZ() + " sur " + this.main.world.getName());
                    }
                    else
                    {
                        pl.sendMessage(ChatColor.RED + "Cette commande n'existe pas. Tapez /towers setregion pour plus d'informations.");
                    }
                }
            }
            else if (args[0].equalsIgnoreCase("setwaitroom"))
            {
                if (args.length != 2)
                {
                    pl.sendMessage("§6/towers setwaitroom <blue|red|lobby> §r: Place les positions des salles d'attentes.");
                }
                else if (args[1].equalsIgnoreCase("blue"))
                {
                    Location locpl = pl.getLocation();
                    this.main.getConfig().set("Coordinates.Waitrooms.Blue.x", Integer.valueOf(locpl.getBlockX()));
                    this.main.getConfig().set("Coordinates.Waitrooms.Blue.y", Integer.valueOf(locpl.getBlockY()));
                    this.main.getConfig().set("Coordinates.Waitrooms.Blue.z", Integer.valueOf(locpl.getBlockZ()));
                    this.main.saveConfig();
                    this.main.bluelobby = locpl;
                    pl.sendMessage("§cCoordonnées du Lobby Blue enregistrées : §b§l" + locpl.getBlockX() + " §c/ §b§l" + locpl.getBlockY() + " §c/ §b§l" + locpl.getBlockZ() + " §csur §6§l" + this.main.worldspawn.getName());
                }
                else if (args[1].equalsIgnoreCase("red"))
                {
                    Location locpl = pl.getLocation();
                    this.main.getConfig().set("Coordinates.Waitrooms.Red.x", Integer.valueOf(locpl.getBlockX()));
                    this.main.getConfig().set("Coordinates.Waitrooms.Red.y", Integer.valueOf(locpl.getBlockY()));
                    this.main.getConfig().set("Coordinates.Waitrooms.Red.z", Integer.valueOf(locpl.getBlockZ()));
                    this.main.saveConfig();
                    this.main.redlobby = locpl;
                    pl.sendMessage("§cCoordonnées du Lobby Red enregistrées : §b§l" + locpl.getBlockX() + " §c/ §b§l" + locpl.getBlockY() + " §c/ §b§l" + locpl.getBlockZ() + " §csur §6§l" + this.main.worldspawn.getName());
                }
                else if (args[1].equalsIgnoreCase("lobby"))
                {
                    Location locpl = pl.getLocation();
                    this.main.getConfig().set("Coordinates.Lobby.x", Integer.valueOf(locpl.getBlockX()));
                    this.main.getConfig().set("Coordinates.Lobby.y", Integer.valueOf(locpl.getBlockY()));
                    this.main.getConfig().set("Coordinates.Lobby.z", Integer.valueOf(locpl.getBlockZ()));
                    this.main.getConfig().set("Coordinates.Lobby.yaw", Float.valueOf(locpl.getYaw()));
                    this.main.saveConfig();
                    this.main.lobby = locpl;
                    this.main.worldspawn.setSpawnLocation(locpl.getBlockX(), locpl.getBlockY(), locpl.getBlockZ());
                    pl.sendMessage("§cCoordonnées du Lobby lobby enregistrées : §b§l" + locpl.getBlockX() + " §c/ §b§l" + locpl.getBlockY() + " §c/ §b§l" + locpl.getBlockZ() + " §csur §b§l" + this.main.worldspawn.getName());
                }
                else
                {
                    pl.sendMessage(ChatColor.RED + "Cette commande n'existe pas. Tapez /towers setwaitroom pour plus d'informations.");
                }
            }
            else if (args[0].equalsIgnoreCase("setspawn"))
            {
                if (args.length != 2)
                {
                    pl.sendMessage("§6/towers setspawn red §r: Définit le point d'apparition de l'équipe Rouge.");
                    pl.sendMessage("§6/towers setspawn blue §r: Définit le point d'apparition de l'équipe Bleue.");
                    pl.sendMessage("§6/towers setspawn spect §r: Définit le point d'apparition pour les spectateurs.");
                }
                else if (args[1].equalsIgnoreCase("blue"))
                {
                    Location locpl = pl.getLocation();
                    this.main.getConfig().set("Coordinates.Spawns.Blue.x", Integer.valueOf(locpl.getBlockX()));
                    this.main.getConfig().set("Coordinates.Spawns.Blue.y", Integer.valueOf(locpl.getBlockY()));
                    this.main.getConfig().set("Coordinates.Spawns.Blue.z", Integer.valueOf(locpl.getBlockZ()));
                    this.main.getConfig().set("Coordinates.Spawns.Blue.yaw", Float.valueOf(locpl.getYaw()));
                    this.main.saveConfig();
                    this.main.bluespawn = locpl;
                    pl.sendMessage("§cCoordonnées du spawn Blue enregistrées : §b§l" + locpl.getBlockX() + " §c/ §b§l" + locpl.getBlockY() + " §c/ §b§l" + locpl.getBlockZ() + " §csur §b§l" + this.main.world.getName());
                }
                else if (args[1].equalsIgnoreCase("red"))
                {
                    Location locpl = pl.getLocation();
                    this.main.getConfig().set("Coordinates.Spawns.Red.x", Integer.valueOf(locpl.getBlockX()));
                    this.main.getConfig().set("Coordinates.Spawns.Red.y", Integer.valueOf(locpl.getBlockY()));
                    this.main.getConfig().set("Coordinates.Spawns.Red.z", Integer.valueOf(locpl.getBlockZ()));
                    this.main.getConfig().set("Coordinates.Spawns.Red.yaw", Float.valueOf(locpl.getYaw()));
                    this.main.saveConfig();
                    this.main.redspawn = locpl;
                    pl.sendMessage("§cCoordonnées du spawn Red enregistrées : §b§l" + locpl.getBlockX() + " §c/ §b§l" + locpl.getBlockY() + " §c/ §b§l" + locpl.getBlockZ() + " §csur §b§l" + this.main.world.getName());
                }
                else
                {
                    print(sender, "Cette commande n'existe pas. Tapez /towers pour plus d'informations.");
                }
            }
        }
        return true;
    }

    public void print(CommandSender sender, String s)
    {
        sender.sendMessage(s);
    }
}
