package me.figsq.pctools.pctools.gui;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PCBox;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.PokemonStorage;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import com.pixelmonmod.pixelmon.comm.packetHandlers.clientStorage.newStorage.pc.ClientSetLastOpenBox;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import lombok.Getter;
import lombok.Setter;
import me.figsq.pctools.pctools.api.ItemComparedMap;
import me.figsq.pctools.pctools.api.util.Cache;
import me.figsq.pctools.pctools.api.util.SomeMethod;
import me.fullidle.ficore.ficore.common.api.ineventory.ListenerInvHolder;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.v1_12_R1.ChatMessage;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_12_R1.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

@Getter
public class PCGui extends ListenerInvHolder {
    @Setter
    private PCResultGui pcResultGui;
    private boolean needReturnGui = true;
    private final Inventory inventory;
    private final Player owner;
    private final PlayerPartyStorage partyStorage;
    private final PCStorage pcStorage;
    private PCBox nowBox;
    private final Map<ItemStack, Pokemon> pokemonCache = new ItemComparedMap<>();
    private ItemStack cacheCursor = null;
    private final ConfirmGui confirmGui = new ConfirmGui(this);
    private final SortGui sortGui = new SortGui(this);
    @Setter
    private boolean needUpdate = false;

    public PCGui(Player owner, Integer page) {
        this.inventory = Bukkit.createInventory(this, 6 * 9,
                SomeMethod.papi(owner, Cache.pCGuiTitle.replace("{box}", String.valueOf(page + 1))));
        this.owner = owner;
        this.pcStorage = Pixelmon.storageManager.getPCForPlayer(owner.getUniqueId());
        this.partyStorage = Pixelmon.storageManager.getParty(owner.getUniqueId());
        initFrame();
        initPoke(page);

        onOpen(e -> {
            this.needReturnGui = true;
            if (cacheCursor != null) {
                e.getPlayer().setItemOnCursor(cacheCursor);
                cacheCursor = null;
            }
            //sort button
            {
                if (e.getPlayer().hasPermission("pctools.function.sort") && this.inventory.getItem(53).getDurability() != 1) {
                    ItemStack itemStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 1);
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName("§6排序");
                    itemStack.setItemMeta(itemMeta);
                    this.inventory.setItem(53, itemStack);
                }
            }

            if (needUpdate) {
                needUpdate = false;
                Bukkit.getScheduler().runTask(Cache.plugin, () -> {
                    initPackPoke();
                    changeBox(this.nowBox.boxNumber, (Player) e.getPlayer(), true);
                });
            }
        });
        onClose(e -> {
            HumanEntity closePlayer = e.getPlayer();
            closePlayer.setItemOnCursor(null);
            this.pcStorage.setLastBox(this.nowBox.boxNumber);
            EntityPlayerMP mp = (EntityPlayerMP) ((Object) ((CraftEntity) closePlayer).getHandle());
            ClientSetLastOpenBox box = new ClientSetLastOpenBox(mp, this.nowBox.boxNumber);
            Pixelmon.network.sendTo(box, mp);

            if (this.pcResultGui != null&&this.needReturnGui)
                Bukkit.getScheduler().runTask(Cache.plugin,
                        ()->closePlayer.openInventory(pcResultGui.getInventory()));
        });
        onDrag(e -> {
            if (needUpdate) {
                e.setCancelled(true);
                return;
            }

            for (Map.Entry<Integer, ItemStack> entry : e.getNewItems().entrySet()) {
                Integer slot = entry.getKey();
                if (slot >= 54) {
                    e.setCancelled(true);
                    return;
                }
                ItemStack item = entry.getValue();
                if (!swapPoke(
                        this.pokemonCache.get(item),
                        null,
                        slot
                )) {
                    e.setCancelled(true);
                    return;
                }
            }
        });
        onClick(e -> {
            if (needUpdate) {
                e.setCancelled(true);
                return;
            }

            int slot = e.getSlot();
            ClickType click = e.getClick();
            boolean clickPcAPack = Cache.invBackpackSlot.contains(slot) || Cache.invPcSlot.contains(slot);
            if (e.getClickedInventory() instanceof PlayerInventory
                    || !clickPcAPack) {
                e.setCancelled(true);
            }
            Player whoClicked = (Player) e.getWhoClicked();
            int boxNumber = nowBox.boxNumber;
            boolean shift = click.equals(ClickType.SHIFT_LEFT);
            ItemStack cursor = e.getCursor();
            ItemStack currentItem = e.getCurrentItem();
            if (slot == 45) {
                //上一页
                int cop = boxNumber - (shift ? 5 : 1);
                cop = cop < 0 ? PixelmonConfig.computerBoxes + (cop) : cop;
                changeBox(
                        cop,
                        whoClicked, true
                );
                return;
            }
            if (slot == 50) {
                int cop = boxNumber + (shift ? 5 : 1);
                cop = cop >= PixelmonConfig.computerBoxes ? cop - PixelmonConfig.computerBoxes : cop;
                //下一页
                changeBox(cop,
                        whoClicked, true
                );
                return;
            }
            if (currentItem != null && currentItem.getDurability() == 1) {
                this.needReturnGui = false;
                whoClicked.openInventory(this.sortGui.getInventory());
                return;
            }

            if (e.isCancelled()) {
                return;
            }

            //左键删除
            if (currentItem != null && click.isRightClick() && clickPcAPack) {
                this.confirmGui.setPokeItem(currentItem, pokemonCache.get(currentItem));
                this.needReturnGui = false;
                whoClicked.openInventory(confirmGui.getInventory());
                return;
            }

            //shift
            Pokemon pokemon = pokemonCache.get(cursor);
            Pokemon ciPoke = this.pokemonCache.get(currentItem);
            Tuple<PokemonStorage, StoragePosition> tuple = SomeMethod.computeStorageAndPosition(slot, this.partyStorage, this.nowBox);
            PokemonStorage ciStorage = tuple.a();

            if (shift) {
                e.setCancelled(true);
                PokemonStorage storage;
                ArrayList<Integer> indexList;
                if (ciStorage instanceof PCBox) {
                    storage = this.partyStorage;
                    indexList = Cache.invBackpackSlot;
                } else {
                    storage = this.nowBox;
                    indexList = Cache.invPcSlot;
                }
                StoragePosition firstEmptyPosition = storage.getFirstEmptyPosition();
                if (firstEmptyPosition == null) return;
                Integer i = indexList.get(firstEmptyPosition.order);

                if (!swapPoke(
                        ciPoke,
                        this.pokemonCache.get(null),
                        i
                )) {
                    e.setCancelled(true);
                    return;
                }

                this.inventory.setItem(slot, null);
                this.inventory.setItem(i, currentItem);
                return;
            }

            //物品互换
            PokemonStorage storage = null;
            StoragePosition position = null;
            //提前缓存
            if (pokemon != null) {
                storage = pokemon.getStorage();
                position = pokemon.getPosition();
            }
            //精灵位置互换
            if (!swapPoke(
                    pokemon,
                    ciPoke,
                    slot
            )) {
                e.setCancelled(true);
                return;
            }

            if (e.getAction().equals(InventoryAction.SWAP_WITH_CURSOR)) {
                e.setCancelled(true);

                if (storage instanceof PCBox) {
                    if (position.box == this.nowBox.boxNumber) {
                        this.inventory.setItem(Cache.invPcSlot.get(position.order), currentItem);
                    }
                } else {
                    this.inventory.setItem(Cache.invBackpackSlot.get(position.order), currentItem);
                }

                this.inventory.setItem(slot, cursor);
                e.getWhoClicked().setItemOnCursor(null);
            }
        });
    }

    private void initFrame() {
        //页面初始化
        //填满
        {
            ItemStack itemStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 4);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(" ");
            itemStack.setItemMeta(itemMeta);
            for (int i = 0; i < inventory.getSize(); i++) {
                this.inventory.setItem(i, itemStack);
            }
        }
        //上下页按钮控制
        {
            //上
            ItemStack itemStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 2);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName("§3PREVIOUS");
            itemStack.setItemMeta(itemMeta);
            this.inventory.setItem(45, itemStack);
        }
        {
            //下
            ItemStack itemStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 2);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName("§3NEXT");
            itemStack.setItemMeta(itemMeta);
            this.inventory.setItem(50, itemStack);
        }
    }

    private void initPoke(Integer page) {
        initPackPoke();
        changeBox(page, this.owner, false);
    }

    private void initPackPoke() {
        Pokemon[] all = partyStorage.getAll();
        for (int i = 0; i < all.length; i++) {
            Pokemon pokemon = all[i];
            ItemStack photo = SomeMethod.getFormatPokePhoto(pokemon);
            if (pokemon != null) {
                pokemonCache.put(photo, pokemon);
            }
            this.inventory.setItem(Cache.invBackpackSlot.get(i),
                    photo);
        }
    }

    public void changeBox(Integer page, Player triggeredPlayer, boolean updateTitle) {
        this.nowBox = this.pcStorage.getBox(page);
        Pokemon[] all = nowBox.getAll();
        for (int i = 0; i < all.length; i++) {
            Pokemon pokemon = all[i];
            ItemStack photo = SomeMethod.getFormatPokePhoto(pokemon);

            if (pokemon != null) {
                pokemonCache.put(photo, pokemon);
            }

            if (Objects.equals(SomeMethod.getFormatItemUUID(triggeredPlayer.getItemOnCursor()),
                    SomeMethod.getFormatItemUUID(photo))) {
                photo = null;
            }
            this.inventory.setItem(Cache.invPcSlot.get(i), photo);
        }
        //title
        if (!updateTitle) {
            return;
        }
        EntityPlayer ep = ((CraftPlayer) triggeredPlayer.getPlayer()).getHandle();
        ChatMessage title = new ChatMessage(SomeMethod.papi(owner, Cache.pCGuiTitle
                .replace("{box}", String.valueOf(this.nowBox.boxNumber + 1))));
        PacketPlayOutOpenWindow play = new PacketPlayOutOpenWindow(
                ep.activeContainer.windowId,
                "minecraft:chest",
                title,
                this.inventory.getSize());
        ep.playerConnection.sendPacket(play);
        ep.updateInventory(ep.activeContainer);
    }

    /**
     * 交换精灵位置
     */
    public boolean swapPoke(Pokemon cuPoke, Pokemon ciPoke, int clickSlot) {
        Pokemon temp = cuPoke;
        cuPoke = cuPoke == null ? ciPoke : cuPoke;
        ciPoke = ciPoke == cuPoke ? temp : ciPoke;
        if (cuPoke == null) return false;

        PokemonStorage cuStorage = cuPoke.getStorage();
        StoragePosition cuPosition = cuPoke.getPosition();
        Tuple<PokemonStorage, StoragePosition> tuple = SomeMethod.computeStorageAndPosition(clickSlot, this.partyStorage, this.nowBox);
        PokemonStorage ciStorage = tuple.a();
        StoragePosition ciPosition = tuple.b();
        if (cuStorage.equals(ciStorage)) {
            if (cuPosition.equals(ciPosition)) return true;
            cuStorage.swap(cuPosition, ciPosition);
            return true;
        }

        if (!Cache.packCanEmpty && ciPoke == null) {
            if (cuStorage instanceof PlayerPartyStorage && ciStorage instanceof PCBox) {
                ArrayList<Pokemon> list = Lists.newArrayList(cuStorage.getAll());
                list.removeIf(Objects::isNull);
                if (list.size() == 1) {
                    return false;
                }
            }
        }
        //非同一存储交换
        ciStorage.set(ciPosition, cuPoke);
        cuStorage.set(cuPosition, ciPoke);
        return true;
    }
}
