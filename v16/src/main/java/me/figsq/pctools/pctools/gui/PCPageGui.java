package me.figsq.pctools.pctools.gui;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.config.PixelmonConfigProxy;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.*;
import com.pixelmonmod.pixelmon.api.util.helpers.NetworkHelper;
import com.pixelmonmod.pixelmon.comm.packetHandlers.clientStorage.newStorage.pc.ClientSetLastOpenBoxPacket;
import lombok.Getter;
import me.figsq.pctools.pctools.api.util.Cache;
import me.figsq.pctools.pctools.api.util.SomeMethod;
import me.figsq.pctools.pctools.api.util.StorageHelper;
import net.minecraft.util.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
public class PCPageGui extends AbstractPreviousInv {
    private final Inventory inventory;
    private final PCBox box;
    private final PlayerPartyStorage party;


    public PCPageGui(PCBox box) {
        this.box = box;
        this.inventory = Bukkit.createInventory(this, 54,
                SomeMethod.papi(Bukkit.getOfflinePlayer(this.box.pc.playerUUID),
                        Cache.plugin.getConfig().getString("msg.pc_page_gui_title").
                                replace("{box}", String.valueOf(box.boxNumber + 1))));
        party = StorageProxy.getParty(this.box.pc.playerUUID);
        //初始化界面
        initFrame();
        initEventHandler();
    }

    private void initEventHandler() {
        //打开处理
        this.onOpen(e -> {
            //排序按钮
            if (e.getPlayer().hasPermission("pctools.function.sort") && this.inventory.getItem(53).getDurability() != 1) {
                //设置排序按钮
                ItemStack itemStack = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(SomeMethod.papi(null, Cache.plugin.getConfig().getString("msg.pc_page_gui_sort_button")));
                itemStack.setItemMeta(itemMeta);
                this.inventory.setItem(53, itemStack);
            }
        });

        //拖拽的特殊处理
        this.onDrag(e -> {
            ArrayList<Integer> pc_pack_list = (ArrayList<Integer>) Cache.invPcSlot.clone();
            pc_pack_list.addAll(Cache.invBackpackSlot);
            //涉及到非可点击的地方的时候直接返回
            Set<Integer> rawSlots = e.getRawSlots();
            for (Integer rawSlot : rawSlots) {
                if (!pc_pack_list.contains(rawSlot)) {
                    e.setCancelled(true);
                    return;
                }
            }
            ItemStack cursor = e.getOldCursor();
            HumanEntity whoClicked = e.getWhoClicked();
            Integer next = rawSlots.iterator().next();
            Tuple<PokemonStorage, StoragePosition> currentInfo = SomeMethod.computeStorageAndPosition(next, party, box);
            Pokemon cursorPoke = StorageHelper.find(SomeMethod.getFormatItemUUID(cursor), party, box.pc);
            Tuple<PokemonStorage, StoragePosition> cursorInfo = new Tuple<>(cursorPoke.getStorage(), cursorPoke.getPosition());
            putInto(cursorInfo, currentInfo, cursorPoke, whoClicked, e.getInventory(), next);
        });

        //点击处理
        this.onClick(e -> {
            int clickSlot = e.getSlot();
            Inventory inv = e.getInventory();
            ClickType clickType = e.getClick();
            boolean shiftClick = clickType.isShiftClick();
            HumanEntity whoClicked = e.getWhoClicked();
            ItemStack cursorItem = whoClicked.getItemOnCursor();
            //如果是左右翻页键
            int computerBoxes = PixelmonConfigProxy.getStorage().getComputerBoxes();
            if (clickSlot == 45) {
                //上一页
                int page = box.boxNumber - (shiftClick ? 5 : 1);
                e.setCancelled(true);
                changePage(whoClicked, page < 0 ? computerBoxes + page : page, cursorItem);
                return;
            }
            if (clickSlot == 50) {
                //下一页
                int page = box.boxNumber + (shiftClick ? 5 : 1);
                e.setCancelled(true);
                changePage(whoClicked, page >= computerBoxes ? page - computerBoxes : page, cursorItem);
                return;
            }
            if (clickSlot == 53) {
                //排序
                e.setCancelled(true);
                whoClicked.setItemOnCursor(null);
                SortGui gui = new SortGui();
                gui.setPreviousInv(this.inventory);
                Inventory temp = this.getPreviousInv();
                this.setPreviousInv(null);
                whoClicked.closeInventory();
                whoClicked.openInventory(gui.getInventory());
                this.setPreviousInv(temp);
                return;
            }
            /*
            ========================================================
            宝可梦有关
            点击的不是pc也不是pack的位置且点击的不是背包*/
            if ((!Cache.invPcSlot.contains(clickSlot) &&
                    !Cache.invBackpackSlot.contains(clickSlot)) ||
                    e.getRawSlot() >= this.inventory.getSize() ||
                    clickType.isKeyboardClick()) {
                e.setCancelled(true);
                return;
            }
            //点击和鼠标上的宝可梦数据
            Tuple<PokemonStorage, StoragePosition> currentInfo = SomeMethod.computeStorageAndPosition(clickSlot, party, box);
            Pokemon currentPoke = currentInfo.func_76341_a().get(currentInfo.func_76340_b());
            UUID cursorPokeUuid = SomeMethod.getFormatItemUUID(cursorItem);
            Pokemon cursorPoke = StorageHelper.find(cursorPokeUuid, box.pc, party);
            Tuple<PokemonStorage, StoragePosition> cursorInfo = cursorPoke == null ? null : new Tuple<>(cursorPoke.getStorage(), cursorPoke.getPosition());

/*            //一些特殊判断
            if (currentPoke != null && currentPoke.isInRanch()){
                e.setCancelled(true);
                return;
            }*/


            //左键
            if (clickType.isLeftClick()) {
                //shift的特殊处理
                if (shiftClick) {
                    e.setCancelled(true);
                    if (currentPoke == null) return;
                    PokemonStorage target_storage = currentPoke.getStorage() instanceof PlayerPartyStorage ? box : party;
                    //获取要挪移存储内是否有空余位置
                    StoragePosition targetPosition = target_storage.getFirstEmptyPosition();
                    if (targetPosition == null) {
                        return;
                    }
                    putInto(new Tuple<>(currentPoke.getStorage(), currentPoke.getPosition()),
                            new Tuple<>(target_storage, targetPosition),
                            currentPoke, whoClicked, inv, clickSlot);
                    return;
                }
                //鼠标上没有物品,那么要么就是拿起来,要是有按照下面判断要么就是放下了
                if (cursorPoke == null) return;
                e.setCancelled(true);
                if (currentPoke == null) {
                    //放入
                    putInto(cursorInfo, currentInfo, cursorPoke, whoClicked, inv, clickSlot);
                } else {
                    whoClicked.setItemOnCursor(null);
                    //交换逻辑
                    currentInfo.func_76341_a().set(currentInfo.func_76340_b(), null);
                    cursorInfo.func_76341_a().set(cursorInfo.func_76340_b(), null);
                    currentInfo.func_76341_a().set(currentInfo.func_76340_b(), cursorPoke);
                    cursorInfo.func_76341_a().set(cursorInfo.func_76340_b(), currentPoke);

                    /*===================================*/
                    boolean currentIsParty = currentInfo.func_76341_a() instanceof PlayerPartyStorage;
                    boolean cursorIsParty = cursorInfo.func_76341_a() instanceof PlayerPartyStorage;
                    /*===================================*/

                    //物品交换
                    if (currentInfo.func_76341_a().equals(cursorInfo.func_76341_a())) {
                        ArrayList<Integer> target = currentIsParty ? Cache.invBackpackSlot : Cache.invPcSlot;
                        //物品直接交换
                        inv.setItem(target.get(currentInfo.func_76340_b().order), SomeMethod.getFormatPokePhoto(cursorPoke));
                        inv.setItem(target.get(cursorInfo.func_76340_b().order), SomeMethod.getFormatPokePhoto(currentPoke));
                        return;
                    }
                    //其中一个是背包
                    if (currentIsParty) {
                        inv.setItem(Cache.invBackpackSlot.get(currentInfo.func_76340_b().order), SomeMethod.getFormatPokePhoto(cursorPoke));
                        inv.setItem(Cache.invPcSlot.get(cursorInfo.func_76340_b().order), SomeMethod.getFormatPokePhoto(currentPoke));
                        return;
                    }
                    if (cursorIsParty) {
                        inv.setItem(Cache.invPcSlot.get(currentInfo.func_76340_b().order), SomeMethod.getFormatPokePhoto(cursorPoke));
                        inv.setItem(Cache.invBackpackSlot.get(cursorInfo.func_76340_b().order), SomeMethod.getFormatPokePhoto(currentPoke));
                        return;
                    }
                    //都是pc页,且不同页
                    inv.setItem(clickSlot, SomeMethod.getFormatPokePhoto(cursorPoke));
                }
                return;
            }
            //右键
            if (clickType.isRightClick()) {
                e.setCancelled(true);
                //鼠标上有宝可梦或者点击的地方没有宝可梦都直接返回
                if (cursorPoke != null || currentPoke == null) return;
                ConfirmGui confirmGui = new ConfirmGui(currentPoke);
                confirmGui.setPreviousInv(this.getInventory());
                Inventory temp = this.getPreviousInv();
                this.setPreviousInv(null);
                whoClicked.closeInventory();
                whoClicked.openInventory(confirmGui.getInventory());
                this.setPreviousInv(temp);
                return;
            }
        });

        this.onClose(e -> {
            e.getPlayer().setItemOnCursor(null);
            //设置最后的页码
            this.box.pc.setLastBox(box.boxNumber);
            NetworkHelper.sendPacket(new ClientSetLastOpenBoxPacket(party.getPlayer(), box.boxNumber), party.getPlayer());

            if (this.getPreviousInv() == null) {
                return;
            }
            Bukkit.getScheduler().runTask(Cache.plugin, () ->
                    e.getPlayer().openInventory(this.getPreviousInv()));
        });
    }

    /**
     * 将点击者贯标上的宝可梦放入一个位置
     */
    private static void putInto(Tuple<PokemonStorage, StoragePosition> cursorInfo, Tuple<PokemonStorage, StoragePosition> currentInfo, Pokemon putIntoPoke, HumanEntity whoClicked, Inventory inv, int clickSlot) {
        //判断要放的精灵是否是背包且是唯一一只的情况下,并更具配置来确定是否拦截
        if (!Cache.packCanEmpty) {
            if (putIntoPoke.getStorage() instanceof PlayerPartyStorage) {
                ArrayList<Pokemon> list = Lists.newArrayList(putIntoPoke.getStorage().getAll());
                list.removeIf(Objects::isNull);
                if (list.size() < 2 && !(currentInfo.func_76341_a() instanceof PlayerPartyStorage)) {
                    return;
                }
            }
        }
        inv.setItem(clickSlot, null);

        cursorInfo.func_76341_a().set(cursorInfo.func_76340_b(), null);
        currentInfo.func_76341_a().set(currentInfo.func_76340_b(), putIntoPoke);
        whoClicked.setItemOnCursor(null);
        ArrayList<Integer> target_list = currentInfo.func_76341_a() instanceof PlayerPartyStorage ? Cache.invBackpackSlot : Cache.invPcSlot;
        inv.setItem(target_list.get(currentInfo.func_76340_b().order), SomeMethod.getFormatPokePhoto(putIntoPoke));
    }

    private void changePage(HumanEntity player, int page, ItemStack cursor) {
        player.closeInventory();
        PCPageGui gui = new PCPageGui(box.pc.getBox(page));
        Pokemon pokemon = StorageHelper.find(SomeMethod.getFormatItemUUID(cursor), box.pc, party);
        if (pokemon != null) {
            boolean b = pokemon.getStorage() instanceof PlayerPartyStorage;
            StoragePosition position = pokemon.getPosition();
            int box = position.box;
            if (b || box == page) {
                ArrayList<Integer> list = b ? Cache.invBackpackSlot : Cache.invPcSlot;
                gui.getInventory().setItem(list.get(position.order), null);
            }
        }
        player.openInventory(gui.getInventory());
        player.setItemOnCursor(SomeMethod.getFormatPokePhoto(pokemon));
    }

    private void initFrame() {
        //页面初始化
        //填满
        {
            ItemStack itemStack = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(" ");
            itemStack.setItemMeta(itemMeta);
            ArrayList<Integer> list = (ArrayList<Integer>) Cache.invPcSlot.clone();
            list.addAll(Cache.invBackpackSlot);
            for (int i = 0; i < inventory.getSize(); i++) {
                if (!list.contains(i)) {
                    this.inventory.setItem(i, itemStack);
                }
            }
        }
        //上下页按钮控制
        {
            //上
            ItemStack itemStack = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(SomeMethod.papi(null, Cache.plugin.getConfig().getString("msg.pc_page_gui_previous_button")));
            itemStack.setItemMeta(itemMeta);
            this.inventory.setItem(45, itemStack);
        }
        {
            //下
            ItemStack itemStack = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(SomeMethod.papi(null, Cache.plugin.getConfig().getString("msg.pc_page_gui_next_button")));
            itemStack.setItemMeta(itemMeta);
            this.inventory.setItem(50, itemStack);
        }

        //初始化宝可梦的布局
        initPokeFrame();
    }

    /**
     * 初始化宝可梦布局
     */
    private void initPokeFrame() {
        {
            Pokemon[] all = box.getAll();
            for (int i = 0; i < all.length; i++) {
                Pokemon pokemon = all[i];
                if (pokemon != null) {
                    ItemStack photo = SomeMethod.getFormatPokePhoto(pokemon);
                    this.inventory.setItem(Cache.invPcSlot.get(i), photo);
                }
            }
        }
        //Pack
        {
            Pokemon[] all = party.getAll();
            for (int i = 0; i < all.length; i++) {
                Pokemon pokemon = all[i];
                if (pokemon != null) {
                    ItemStack photo = SomeMethod.getFormatPokePhoto(pokemon);
                    this.inventory.setItem(Cache.invBackpackSlot.get(i), photo);
                }
            }
        }
    }
}
