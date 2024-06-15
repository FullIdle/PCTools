package me.figsq.taiyiuisub.taiyiuisub;

import lombok.SneakyThrows;
import me.figsq.pctools.pctools.api.events.PCPageChangeEvent;
import me.figsq.pctools.pctools.gui.PCPageGui;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

public class Main extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this,this);
    }

    @SneakyThrows
    @EventHandler
    public void onPCPageChange(PCPageChangeEvent e){
        e.setCancelled(true);
        Inventory original = e.getOriginal();
        Inventory target = e.getTarget();
        for (int i = 0; i < e.getTarget().getSize(); i++) {
            original.setItem(i,target.getItem(i));
        }
        PCPageGui originalHolder = (PCPageGui) original.getHolder();
        PCPageGui targetHolder = (PCPageGui) target.getHolder();
        Field box = PCPageGui.class.getDeclaredField("box");
        box.setAccessible(true);
        box.set(originalHolder,box.get(targetHolder));
    }
}