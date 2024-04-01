package me.figsq.pctools.pctools.gui;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PokemonStorage;
import lombok.Getter;
import me.figsq.pctools.pctools.api.util.Cache;
import me.fullidle.ficore.ficore.common.api.ineventory.ListenerInvHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@Getter
public class ConfirmGui extends ListenerInvHolder {
    private final Inventory inventory = Bukkit.createInventory(this,3*9,"§c放生宝可梦-确定?");
    private boolean openUpGui = false;
    private ItemStack pokeItem;
    private Pokemon pokemon;
    private final PCGui pcGui;

    public ConfirmGui(PCGui pcGui){
        this.pcGui = pcGui;

        {
            //confirm
            ItemStack itemStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName("§c§l确认");
            itemStack.setItemMeta(itemMeta);
            this.inventory.setItem(21,itemStack);
        }
        {
            //cancel
            ItemStack itemStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName("§a§l取消");
            itemStack.setItemMeta(itemMeta);
            this.inventory.setItem(23,itemStack);
        }

        onClick(e->{
            e.setCancelled(true);
            if (!e.getCurrentItem().getType().equals(Material.STAINED_GLASS_PANE)) return;
            if (e.getSlot() == 21) {
                pcGui.setNeedUpdate(true);
                pokemon.getStorage().set(pokemon.getPosition(),null);
            }
            this.openUpGui = true;
            e.getWhoClicked().openInventory(pcGui.getInventory());
        });
        onClose(e-> {
            if (this.openUpGui){
                this.openUpGui = false;
                Bukkit.getScheduler().runTask(Cache.plugin, () -> e.getPlayer().openInventory(this.pcGui.getInventory()));
            }
        });
    }

    public void setPokeItem(ItemStack pokeItem, Pokemon pokemon) {
        this.inventory.setItem(4,pokeItem);
        this.pokeItem = pokeItem;
        this.pokemon = pokemon;
    }
}
