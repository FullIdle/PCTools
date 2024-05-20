package me.figsq.pctools.pctools.gui;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import lombok.Getter;
import me.figsq.pctools.pctools.api.ItemComparedMap;
import me.figsq.pctools.pctools.api.util.Cache;
import me.figsq.pctools.pctools.api.util.SomeMethod;
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
    private final Map<ItemStack, Pokemon> cache = new ItemComparedMap<>();
    private int nowPage;

    public PCResultGui(List<Pokemon> pokemons) {
        this.inventory = Bukkit.createInventory(this, 6 * 9, SomeMethod.papi(null, Cache.plugin.getConfig().getString("msg.search_gui_title")));
        {
            //上一页
            ItemStack itemStack = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(SomeMethod.papi(null, Cache.plugin.getConfig().getString("msg.search_gui_previous_button")));
            itemStack.setItemMeta(itemMeta);
            this.inventory.setItem(47, itemStack);
        }
        {
            //下一页
            ItemStack itemStack = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(SomeMethod.papi(null, Cache.plugin.getConfig().getString("msg.search_gui_next_button")));
            itemStack.setItemMeta(itemMeta);
            this.inventory.setItem(51, itemStack);
        }

        for (int i = 0; i < pokemons.size(); i += 45) {
            ArrayList<ItemStack> page = new ArrayList<>();
            for (Pokemon pokemon : pokemons.subList(i, Math.min(i + 45, pokemons.size()))) {
                ItemStack photo = SomeMethod.getFormatPokePhoto(pokemon);
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
            Pokemon pokemon = cache.get(item);
            int box = pokemon.getPosition().box;
            PCPageGui gui = new PCPageGui(StorageProxy.getPCForPlayer(whoClicked.getUniqueId()).getBox(box));
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
                Pokemon pokemon = cache.get(item);
                if (pokemon.getStorage() == null || pokemon.getStorage() instanceof PlayerPartyStorage || pokemon.getStorage().getPosition(pokemon) == null) {
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
