package me.figsq.pctools.pctools.gui;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import lombok.Getter;
import me.figsq.pctools.pctools.api.util.Cache;
import me.figsq.pctools.pctools.api.util.SomeMethod;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@Getter
public class ConfirmGui extends AbstractPreviousInv {
    private final Inventory inventory = Bukkit.createInventory(this,3*9,"§c放生宝可梦-确定?");
    private final Pokemon pokemon;

    public ConfirmGui(Pokemon pokemon){
        {
            this.pokemon = pokemon;
            ItemStack photo = SomeMethod.getFormatPokePhoto(pokemon);
            this.inventory.setItem(4,photo);
        }
        {
            //confirm
            ItemStack itemStack = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName("§c§l确认");
            itemStack.setItemMeta(itemMeta);
            this.inventory.setItem(21,itemStack);
        }
        {
            //cancel
            ItemStack itemStack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName("§a§l取消");
            itemStack.setItemMeta(itemMeta);
            this.inventory.setItem(23,itemStack);
        }

        onClick(e->{
            e.setCancelled(true);
            ItemStack item = e.getCurrentItem();
            if (item == null) return;
            if (!item.getType().name().contains("STAINED_GLASS_PANE")) return;
            if (e.getSlot() == 21) {
                pokemon.getStorage().set(pokemon.getPosition(),null);
            }
            e.getWhoClicked().closeInventory();
        });
        onClose(e-> Bukkit.getScheduler().runTask(Cache.plugin, () ->
                e.getPlayer().openInventory(new PCPageGui(((PCPageGui) this.getPreviousInv().getHolder()).getBox()).getInventory())));
    }
}
