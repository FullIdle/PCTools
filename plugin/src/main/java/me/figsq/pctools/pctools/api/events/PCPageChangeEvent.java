package me.figsq.pctools.pctools.api.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Getter
public class PCPageChangeEvent extends Event implements Cancellable {
    @Getter
    public static HandlerList handlerList = new HandlerList();
    @Setter
    private Inventory target;
    @Setter
    private boolean cancelled = false;
    private final Inventory original;
    @Setter
    private ItemStack cursorItem;

    public PCPageChangeEvent(Inventory original, Inventory target, ItemStack cursorItem){
        this.original = original;
        this.target = target;
        this.cursorItem = cursorItem;
    }


    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
