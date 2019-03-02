package fr.omegaserv.tower;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class Menu
        implements Listener
{
    public Scoreboard sc = Bukkit.getScoreboardManager().getMainScoreboard();
    private TheTowerPlugin main = null;
    public ItemStack itemMenu;
    private ItemMeta menumeta;
    private Inventory invMenu;

    public Menu(TheTowerPlugin main)
    {
        this.main = main;
        this.itemMenu = new ItemStack(main.getConfig().getInt("Menu.Item"));
        this.menumeta = this.itemMenu.getItemMeta();
        this.menumeta.setDisplayName(main.getConfig().getString("Menu.Title"));
        this.itemMenu.setItemMeta(this.menumeta);

        this.invMenu = Bukkit.createInventory(null, 9, main.getConfig().getString("Menu.Title"));

        ItemStack rouge = new ItemStack(Material.WOOL, 1, main.game.equipeRed.dyeColor.getData());
        ItemMeta meta2 = rouge.getItemMeta();

        meta2.setDisplayName(main.game.equipeRed.chatColor + "Equipe " + main.game.equipeRed.name + " (" + main.game.equipeRed.getSize() + "/" + main.MaxPlayersTeam + ")");
        meta2.setLore(main.game.equipeRed.getPlayersName());
        rouge.setItemMeta(meta2);
        this.invMenu.addItem(new ItemStack[] { rouge });

        ItemStack bleue = new ItemStack(Material.WOOL, 1, main.game.equipeBlue.dyeColor.getData());
        ItemMeta meta3 = bleue.getItemMeta();

        meta3.setDisplayName(main.game.equipeBlue.chatColor + "Equipe " + main.game.equipeBlue.name + " (" + main.game.equipeBlue.getSize() + "/" + main.MaxPlayersTeam + ")");
        meta3.setLore(main.game.equipeBlue.getPlayersName());
        bleue.setItemMeta(meta3);
        this.invMenu.addItem(new ItemStack[] { bleue });

        ItemStack quitter = new ItemStack(Material.REDSTONE);
        ItemMeta metaquitter = quitter.getItemMeta();
        metaquitter.setDisplayName(ChatColor.GREEN + "Quitter l'�quipe.");
        quitter.setItemMeta(metaquitter);
        this.invMenu.setItem(3, quitter);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e)
    {
        Player pl = e.getPlayer();
        if (((e.getAction() == Action.RIGHT_CLICK_AIR) || (e.getAction() == Action.RIGHT_CLICK_BLOCK)) &&
                (pl.getInventory().getItemInHand().getTypeId() == this.main.getConfig().getInt("Menu.Item")) &&
                (e.getItem().getItemMeta().getDisplayName().equals(this.main.getConfig().getString("Menu.Title")))) {
            Menu(pl);
        }
    }

    @EventHandler
    public void onInv(InventoryClickEvent e)
    {
        Player pl = (Player)e.getWhoClicked();
        if ((e.getSlotType() == InventoryType.SlotType.ARMOR) && (!this.main.game.running)) {
            e.setCancelled(true);
        }
        if ((e.getCurrentItem().getTypeId() == 370) && (e.getCurrentItem().equals(this.main.menu.itemMenu)))
        {
            e.setCancelled(true);
        }
        else if (e.getInventory().equals(this.invMenu))
        {
            e.setCancelled(true);
            switch (e.getSlot())
            {
                case 1:
                    if ((!this.main.game.running) || (this.main.getConfig().getBoolean("Teams.TeamJoinAfterStart"))) {
                        if (this.main.game.equipeBlue.team.getSize() >= this.main.MaxPlayersTeam) {
                            pl.sendMessage(ChatColor.DARK_BLUE + "Cette �quipe est compl�te.");
                        } else {
                            this.main.game.equipeBlue.addPlayer(pl);
                        }
                    }
                    break;
                case 0:
                    if ((!this.main.game.running) || (this.main.getConfig().getBoolean("Teams.TeamJoinAfterStart"))) {
                        if (this.main.game.equipeRed.team.getSize() >= this.main.MaxPlayersTeam) {
                            pl.sendMessage(ChatColor.DARK_RED + "Cette �quipe est compl�te.");
                        } else {
                            this.main.game.equipeRed.addPlayer(pl);
                        }
                    }
                    break;
                case 3:
                    this.main.LeaveTeam(pl);
            }
        }
    }

    public void Menu(Player pl)
    {
        pl.openInventory(this.invMenu);
    }
}