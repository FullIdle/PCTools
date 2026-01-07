package me.figsq.pctools.pctools.gui;

import lombok.Getter;
import me.figsq.pctools.pctools.api.Config;
import me.figsq.pctools.pctools.api.PapiUtil;
import me.figsq.pctools.pctools.api.PCSortUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@Getter
public class SortGui extends AbstractPreviousInv {
    private final Inventory inventory = Bukkit.createInventory(this,9, PapiUtil.papi(null, Config.plugin.getConfig().getString("msg.sort_gui_title")));

    public SortGui(){
        {
            //随机排序
            ItemStack itemStack = new ItemStack(Material.getMaterial("PIXELMON_GS_BALL"));
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(PapiUtil.papi(null,Config.plugin.getConfig().getString("msg.sort_gui_random_order_button")));
            itemStack.setItemMeta(itemMeta);
            this.inventory.setItem(0,itemStack);
        }
        {
            //种类排序/宝可梦编号排序
            ItemStack itemStack = new ItemStack(Material.getMaterial("PIXELMON_POKE_BALL"));
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(PapiUtil.papi(null,Config.plugin.getConfig().getString("msg.sort_gui_category_sorting_button")));
            itemStack.setItemMeta(itemMeta);
            this.inventory.setItem(1,itemStack);
        }
        {
            //特殊种类排序
            ItemStack itemStack = new ItemStack(Material.getMaterial("PIXELMON_MASTER_BALL"));
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(PapiUtil.papi(null,Config.plugin.getConfig().getString("msg.sort_gui_quality_sorting_button")));
            itemStack.setItemMeta(itemMeta);
            this.inventory.setItem(2,itemStack);
        }

        onClick(e->{
            e.setCancelled(true);
            int slot = e.getSlot();
            if (e.getCurrentItem() == null||
                    e.getCurrentItem().getType().equals(Material.AIR)) return;
            PCPageGui gui = (PCPageGui) this.getPreviousInv().getHolder();
            if (slot == 0){
                PCSortUtil.randomSort(gui.getBox());
            }
            if (slot == 1){
                PCSortUtil.speciesSort(gui.getBox());
            }
            if (slot == 2){
                PCSortUtil.specialSort(gui.getBox());
            }

            e.getWhoClicked().closeInventory();
        });

        onClose(e -> {
            PCPageGui previousGui = (PCPageGui) this.getPreviousInv().getHolder();
            PCPageGui pcPageGui = new PCPageGui(previousGui.getBox());
            pcPageGui.setPreviousInv(previousGui.getPreviousInv());
            Bukkit.getScheduler().runTask(Config.plugin, () ->
                    e.getPlayer().openInventory(pcPageGui.getInventory()));
        });
    }
}
