package me.figsq.pctools.pctools.gui;

import lombok.Getter;
import me.figsq.pctools.pctools.api.util.Cache;
import me.figsq.pctools.pctools.api.util.TidyPcUtil;
import me.fullidle.ficore.ficore.common.api.ineventory.ListenerInvHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;

@Getter
public class SortGui extends ListenerInvHolder {
    private final Inventory inventory = Bukkit.createInventory(this,9,"§3排序");
    private final PCGui pcGui;

    public SortGui(PCGui pcGui){
        this.pcGui = pcGui;
        {
            //随机排序
            ItemStack itemStack = new ItemStack(Material.getMaterial("PIXELMON_GS_BALL"));
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName("§c随机排序");
            itemMeta.setLore(Collections.singletonList("§c点击后则会对该页进行 随机排序"));
            itemStack.setItemMeta(itemMeta);
            this.inventory.setItem(0,itemStack);
        }
        {
            //种类排序/宝可梦编号排序
            ItemStack itemStack = new ItemStack(Material.getMaterial("PIXELMON_POKE_BALL"));
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName("§6种类排序");
            itemMeta.setLore(Collections.singletonList("§c点击后则会对该页进行 §6种族排序"));
            itemStack.setItemMeta(itemMeta);
            this.inventory.setItem(1,itemStack);
        }
        {
            //特殊种类排序
            ItemStack itemStack = new ItemStack(Material.getMaterial("PIXELMON_MASTER_BALL"));
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName("§5特殊种类排序");
            itemMeta.setLore(Arrays.asList(
                    "§c点击后则会对该页进行 §5特殊种族排序",
                    "§c顺序神兽->究极->普通->蛋"
            ));
            itemStack.setItemMeta(itemMeta);
            this.inventory.setItem(2,itemStack);
        }

        onClick(e->{
            e.setCancelled(true);
            int slot = e.getSlot();
            if (e.getCurrentItem() == null||
                    e.getCurrentItem().getType().equals(Material.AIR)) return;
            if (slot == 0){
                TidyPcUtil.randomSort(this.pcGui.getNowBox());
            }
            if (slot == 1){
                TidyPcUtil.speciesSort(this.pcGui.getNowBox());
            }
            if (slot == 2){
                TidyPcUtil.specialSort(this.pcGui.getNowBox());
            }
            this.pcGui.setNeedUpdate(true);
            e.getWhoClicked().openInventory(this.pcGui.getInventory());
        });

        onClose(e->{
            this.pcGui.setNeedUpdate(true);
            Bukkit.getScheduler().runTask(Cache.plugin,()->e.getPlayer().openInventory(this.pcGui.getInventory()));
        });
    }
}
