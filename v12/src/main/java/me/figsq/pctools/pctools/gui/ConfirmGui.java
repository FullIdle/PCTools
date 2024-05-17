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
    private final Inventory inventory = Bukkit.createInventory(this,3*9,SomeMethod.papi(null, Cache.plugin.getConfig().getString("msg.confirm_gui_title")));
    private final Pokemon pokemon;

    public ConfirmGui(Pokemon pokemon){
        {
            this.pokemon = pokemon;
            ItemStack photo = SomeMethod.getFormatPokePhoto(pokemon);
            this.inventory.setItem(4,photo);
        }
        {
            //confirm
            ItemStack itemStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(SomeMethod.papi(null, Cache.plugin.getConfig().getString("msg.confirm_gui_confirm_button")));
            itemStack.setItemMeta(itemMeta);
            this.inventory.setItem(21,itemStack);
        }
        {
            //cancel
            ItemStack itemStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(SomeMethod.papi(null, Cache.plugin.getConfig().getString("msg.confirm_gui_cancel_button")));
            itemStack.setItemMeta(itemMeta);
            this.inventory.setItem(23,itemStack);
        }

        onClick(e->{
            e.setCancelled(true);
            ItemStack item = e.getCurrentItem();
            if (item == null) return;
            if (!item.getType().equals(Material.STAINED_GLASS_PANE)) return;
            if (e.getSlot() == 21) {
                pokemon.getStorage().set(pokemon.getPosition(),null);
            }
            e.getWhoClicked().closeInventory();
        });
        onClose(e-> Bukkit.getScheduler().runTask(Cache.plugin, () ->
                e.getPlayer().openInventory(new PCPageGui(((PCPageGui) this.getPreviousInv().getHolder()).getBox()).getInventory())));
    }
}
