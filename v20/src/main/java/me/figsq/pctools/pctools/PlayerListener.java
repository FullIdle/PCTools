package me.figsq.pctools.pctools;

import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import me.figsq.pctools.pctools.api.enums.Permissions;
import me.figsq.pctools.pctools.api.util.Cache;
import me.figsq.pctools.pctools.gui.PCPageGui;
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
                !e.getAction().equals(Action.RIGHT_CLICK_BLOCK)||
                e.isCancelled()
        )return;
        if (!e.getClickedBlock().getType().name().equalsIgnoreCase("PIXELMON_PC")) {
            return;
        }
        if (Cache.cancelPC) {
            e.setCancelled(true);

            String permission = Permissions.OPEN.getPermission();
            Player p = e.getPlayer();
            if (!p.hasPermission(permission)){
                p.sendMessage("§cYou lack §3"+permission+"§c permissions");
                return;
            }

            PCStorage pc = StorageProxy.getPCForPlayerNow(p.getUniqueId());
            PCPageGui pcGui = new PCPageGui(pc.getBox(pc.getLastBox()));
            Inventory inv = pcGui.getInventory();
            p.openInventory(inv);
        }
    }
}
