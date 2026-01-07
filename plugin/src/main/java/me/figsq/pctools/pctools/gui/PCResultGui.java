package me.figsq.pctools.pctools.gui;

import lombok.Getter;
import lombok.val;
import me.figsq.pctools.pctools.api.*;
import me.fullidle.ficore.ficore.FICoreAPI;
import me.fullidle.ficore.ficore.common.api.pokemon.wrapper.IPokemonWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class PCResultGui extends AbstractPreviousInv {
    private final Inventory inventory;

    public final List<List<ItemStack>> allPage = new ArrayList<>();
    private final Map<ItemStack, IPokemonWrapper<?>> cache = new ItemComparedMap<>();
    private int nowPage;

    public PCResultGui(List<IPokemonWrapper<?>> pokemons) {
        this.inventory = Bukkit.createInventory(this, 6 * 9, PapiUtil.papi(null, Config.plugin.getConfig().getString("msg.search_gui_title")));
        {
            //上一页
            ItemStack itemStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(PapiUtil.papi(null, Config.plugin.getConfig().getString("msg.search_gui_previous_button")));
            itemStack.setItemMeta(itemMeta);
            this.inventory.setItem(47, itemStack);
        }
        {
            //下一页
            ItemStack itemStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(PapiUtil.papi(null, Config.plugin.getConfig().getString("msg.search_gui_next_button")));
            itemStack.setItemMeta(itemMeta);
            this.inventory.setItem(51, itemStack);
        }

        for (int i = 0; i < pokemons.size(); i += 45) {
            ArrayList<ItemStack> page = new ArrayList<>();
            for (val pokemon : pokemons.subList(i, Math.min(i + 45, pokemons.size()))) {
                ItemStack photo = PokeUtil.getFormatPokePhoto(pokemon);
                page.add(photo);
                cache.put(photo, pokemon);
            }
            allPage.add(page);
        }
        if (allPage.isEmpty()) allPage.add(new ArrayList<>());

        this.nowPage = 0;

        onOpen(e -> changePage(this.nowPage));

        onClick(e -> {
            e.setCancelled(true);
            int slot = e.getSlot();
            ItemStack item = e.getCurrentItem();
            if (e.getClickedInventory() instanceof PlayerInventory ||
                    item == null || item.getType().equals(Material.AIR)) return;
            if (slot == 47) {
                if (this.nowPage == 0) {
                    return;
                }
                changePage(this.nowPage - 1);
                return;
            }
            if (slot == 51) {
                if (this.nowPage == this.allPage.size() - 1) {
                    return;
                }
                changePage(this.nowPage + 1);
                return;
            }
            //跳转PCGUi
            Player whoClicked = (Player) e.getWhoClicked();
            val pokemon = cache.get(item);
            int box = pokemon.getStoragePos().getBox();
            PCPageGui gui = new PCPageGui(new PCBox(FICoreAPI.getPokeStorageManager().getPC(whoClicked.getUniqueId()), box));
            gui.setPreviousInv(this.inventory);
            whoClicked.closeInventory();
            whoClicked.openInventory(gui.getInventory());
        });
    }

    public void changePage(Integer page) {
        this.nowPage = 0;
        List<ItemStack> list = allPage.get(page);
        for (int i = 0; i < list.size(); i++) {
            this.inventory.setItem(i, null);
        }
        ArrayList<ItemStack> removeList = new ArrayList<>();
        for (ItemStack item : list) {
            if (cache.containsKey(item)) {
                val pokemon = cache.get(item);
                if (pokemon.getStorage() == null || StorageHelper.isParty(pokemon.getStorage()) || StorageHelper.findPos(pokemon.getStorage(), pokemon) == null) {
                    removeList.add(item);
                    cache.remove(item);
                    continue;
                }
            }
            this.inventory.addItem(item);
        }
        if (!removeList.isEmpty())
            allPage.get(page).removeAll(removeList);
    }
}
