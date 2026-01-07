package me.figsq.pctools.pctools;

import lombok.val;
import me.figsq.pctools.pctools.api.Config;
import me.figsq.pctools.pctools.api.PCBox;
import me.figsq.pctools.pctools.api.Permissions;
import me.figsq.pctools.pctools.gui.PCPageGui;
import me.fullidle.ficore.ficore.FICoreAPI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

public class PlayerListener implements Listener {
    @EventHandler
    public void interactBlock(PlayerInteractEvent e){
        if (e.getClickedBlock() == null||
                e.getClickedBlock().getType().equals(Material.AIR)||
                e.getAction() == null ||
                !e.getAction().equals(Action.RIGHT_CLICK_BLOCK)||
                e.isCancelled()
        )return;
        val name = e.getClickedBlock().getType().name();
        if (!name.endsWith("pc") && !name.equalsIgnoreCase("pc")) return;
        if (Config.cancelPC) {
            e.setCancelled(true);

            String permission = Permissions.OPEN.getPermission();
            Player p = e.getPlayer();
            if (!p.hasPermission(permission)){
                p.sendMessage("§cYou lack §3"+permission+"§c permissions");
                return;
            }

            PCPageGui pcGui = new PCPageGui(new PCBox(FICoreAPI.getPokeStorageManager().getPC(p.getUniqueId()), 0));
            Inventory inv = pcGui.getInventory();
            p.openInventory(inv);
        }
    }
}
