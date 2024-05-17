package me.figsq.pctools.pctools.api;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class ItemComparedMap<V> extends HashMap<ItemStack, V> {
    @Override
    public V get(Object key) {
        if (!(key instanceof ItemStack)) return null;
        ItemStack k = (ItemStack) key;
        for (ItemStack stack : keySet()) {
            if (stack.isSimilar(k)) {
                return super.get(stack);
            }
        }
        return null;
    }
}
