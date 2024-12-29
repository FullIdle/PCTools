package me.figsq.pctools.pctools;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import me.figsq.pctools.pctools.api.enums.Permissions;
import me.figsq.pctools.pctools.api.Cache;
import me.figsq.pctools.pctools.gui.PCPageGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class PlayerListener implements Listener {
    private static final List<String> pc_list = Lists.newArrayList(
            "PIXELMON_BLACK_PC",
            "PIXELMON_BLUE_PC",
            "PIXELMON_BROWN_PC",
            "PIXELMON_CYAN_PC",
            "PIXELMON_GRAY_PC",
            "PIXELMON_GREEN_PC",
            "PIXELMON_LIGHT_BLUE_PC",
            "PIXELMON_LIGHT_GRAY_PC",
            "PIXELMON_LIME_PC",
            "PIXELMON_MAGENTA_PC",
            "PIXELMON_ORANGE_PC",
            "PIXELMON_PINK_PC",
            "PIXELMON_PURPLE_PC",
            "PIXELMON_RED_PC",
            "PIXELMON_WHITE_PC",
            "PIXELMON_YELLOW_PC"
    );

    @EventHandler
    public void interactBlock(PlayerInteractEvent e){
        if (e.getClickedBlock() == null||
                e.getClickedBlock().getType().equals(Material.AIR)||
                !e.getAction().equals(Action.RIGHT_CLICK_BLOCK)||
                e.isCancelled()
        )return;
        if (!pc_list.contains(e.getClickedBlock().getType().name())) {
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

            PCStorage pc = StorageProxy.getPCForPlayer(p.getUniqueId());
            PCPageGui pcGui = new PCPageGui(pc.getBox(pc.getLastBox()));
            Inventory inv = pcGui.getInventory();
            p.openInventory(inv);
        }
    }
}
