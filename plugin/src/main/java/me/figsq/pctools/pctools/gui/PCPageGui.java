package me.figsq.pctools.pctools.gui;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.val;
import me.figsq.pctools.pctools.api.*;
import me.figsq.pctools.pctools.api.events.PCPageChangeEvent;
import me.fullidle.ficore.ficore.FICoreAPI;
import me.fullidle.ficore.ficore.common.api.pokemon.storage.StoragePos;
import me.fullidle.ficore.ficore.common.api.pokemon.wrapper.IPokeStorageWrapper;
import me.fullidle.ficore.ficore.common.api.pokemon.wrapper.IPokemonWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
public class PCPageGui extends AbstractPreviousInv {
    private final Inventory inventory;
    private final PCBox box;
    private final IPokeStorageWrapper<?> party;


    public PCPageGui(PCBox box) {
        this.box = box;
        this.inventory = Bukkit.createInventory(this, 54,
                PapiUtil.papi(Bukkit.getOfflinePlayer(this.box.getUUID()),
                        Config.plugin.getConfig().getString("msg.pc_page_gui_title").
                                replace("{box}", String.valueOf(box.getPage() + 1))));
        party = FICoreAPI.getPokeStorageManager().getParty(this.box.getUUID());
        //初始化界面
        initFrame();
        initEventHandler();
    }

    private void initEventHandler() {
        //打开处理
        this.onOpen(e -> {
            //精灵实体清除判断
            for (IPokemonWrapper<?> pokemon : party.all()) {
                if (pokemon == null||pokemon.getEntity() == null) continue;
                pokemon.getEntity().asBukkitEntity().remove();
            }


            //排序按钮
            if (e.getPlayer().hasPermission("pctools.function.sort") && this.inventory.getItem(53).getDurability() != 1) {
                //设置排序按钮
                val itemStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 1);
                val itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(PapiUtil.papi(null, Config.plugin.getConfig().getString("msg.pc_page_gui_sort_button")));
                itemStack.setItemMeta(itemMeta);
                this.inventory.setItem(53, itemStack);
            }
        });

        //拖拽的特殊处理
        this.onDrag(e -> {
            val pc_pack_list = (ArrayList<Integer>) Config.invPcSlot.clone();
            pc_pack_list.addAll(Config.invBackpackSlot);
            //涉及到非可点击的地方的时候直接返回
            val rawSlots = e.getRawSlots();
            for (Integer rawSlot : rawSlots) {
                if (!pc_pack_list.contains(rawSlot)) {
                    e.setCancelled(true);
                    return;
                }
            }
            val cursor = e.getOldCursor();
            val whoClicked = e.getWhoClicked();
            val next = rawSlots.iterator().next();
            val currentInfo = PokeUtil.computeStorageAndPosition(next, party, box);
            val cursorPoke = StorageHelper.find(PokeUtil.getFormatItemUUID(cursor), party, box.getPc());
            final Pair<IPokeStorageWrapper<?>, StoragePos> cursorInfo = new Pair<>(cursorPoke.getStorage(), cursorPoke.getStoragePos());
            if (!putInto(cursorInfo, currentInfo, cursorPoke, whoClicked, e.getInventory(), next)) {
                e.setCancelled(true);
            }
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
            val pokemonConfig = FICoreAPI.getPokemonConfigManager();
            if (clickSlot == 45) {
                //上一页
                int page = box.getPage() - (shiftClick ? 5 : 1);
                e.setCancelled(true);
                changePage(whoClicked, page < 0 ? pokemonConfig.getComputerBoxes() + page : page, cursorItem);
                return;
            }
            if (clickSlot == 50) {
                //下一页
                int page = box.getPage() + (shiftClick ? 5 : 1);
                e.setCancelled(true);
                changePage(whoClicked, page >= pokemonConfig.getComputerBoxes() ? page - pokemonConfig.getComputerBoxes() : page, cursorItem);
                return;
            }
            if (clickSlot == 53) {
                //排序
                if (whoClicked.hasPermission("pctools.function.sort")) {
                    e.setCancelled(true);
                    whoClicked.setItemOnCursor(null);
                    SortGui gui = new SortGui();
                    gui.setPreviousInv(this.inventory);
                    Inventory temp = this.getPreviousInv();
                    this.setPreviousInv(null);
                    whoClicked.openInventory(gui.getInventory());
                    this.setPreviousInv(temp);
                }
                return;
            }
            /*
            ========================================================
            宝可梦有关
            点击的不是pc也不是pack的位置且点击的不是背包*/
            if ((!Config.invPcSlot.contains(clickSlot) &&
                    !Config.invBackpackSlot.contains(clickSlot)) ||
                    e.getRawSlot() >= this.inventory.getSize() ||
                    clickType.isKeyboardClick()) {
                e.setCancelled(true);
                return;
            }
            //点击和鼠标上的宝可梦数据
            val currentInfo = PokeUtil.computeStorageAndPosition(clickSlot, party, box);
            val currentPoke = currentInfo.getKey().get(currentInfo.getValue());
            val cursorPokeUuid = PokeUtil.getFormatItemUUID(cursorItem);
            val cursorPoke = StorageHelper.find(cursorPokeUuid, box.getPc(), party);
            final Pair<IPokeStorageWrapper<?>, StoragePos> cursorInfo = cursorPoke == null ? null : new Pair<>(cursorPoke.getStorage(), cursorPoke.getStoragePos());
            //一些特殊判断
            if (currentPoke != null && currentPoke.inRanch()) {
                e.setCancelled(true);
                return;
            }


            //左键
            if (clickType.isLeftClick()) {
                //shift的特殊处理
                if (shiftClick) {
                    e.setCancelled(true);
                    if (currentPoke == null) return;
                    final IPokeStorageWrapper<?> target_storage = StorageHelper.isParty(currentPoke.getStorage()) ? box : party;
                    //获取要挪移存储内是否有空余位置
                    val targetPosition = StorageHelper.findEmpty(target_storage);
                    if (targetPosition == null) {
                        return;
                    }
                    putInto(new Pair<>(currentPoke.getStorage(), currentPoke.getStoragePos()),
                            new Pair<>(target_storage, targetPosition),
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
                    currentInfo.getKey().set(currentInfo.getValue(), null);
                    cursorInfo.getKey().set(cursorInfo.getValue(), null);
                    currentInfo.getKey().set(currentInfo.getValue(), cursorPoke);
                    cursorInfo.getKey().set(cursorInfo.getValue(), currentPoke);

                    /*===================================*/
                    boolean currentIsParty = StorageHelper.isParty(currentInfo.getKey());
                    boolean cursorIsParty = StorageHelper.isParty(cursorInfo.getKey());
                    /*===================================*/

                    //物品交换
                    if (currentInfo.getKey().equals(cursorInfo.getKey())) {
                        ArrayList<Integer> target = currentIsParty ? Config.invBackpackSlot : Config.invPcSlot;
                        //物品直接交换
                        inv.setItem(target.get(currentInfo.getValue().getSlot()), PokeUtil.getFormatPokePhoto(cursorPoke));
                        inv.setItem(target.get(cursorInfo.getValue().getSlot()), PokeUtil.getFormatPokePhoto(currentPoke));
                        return;
                    }
                    //其中一个是背包
                    if (currentIsParty) {
                        inv.setItem(Config.invBackpackSlot.get(currentInfo.getValue().getSlot()), PokeUtil.getFormatPokePhoto(cursorPoke));
                        inv.setItem(Config.invPcSlot.get(cursorInfo.getValue().getSlot()), PokeUtil.getFormatPokePhoto(currentPoke));
                        return;
                    }
                    if (cursorIsParty) {
                        inv.setItem(Config.invPcSlot.get(currentInfo.getValue().getSlot()), PokeUtil.getFormatPokePhoto(cursorPoke));
                        inv.setItem(Config.invBackpackSlot.get(cursorInfo.getValue().getSlot()), PokeUtil.getFormatPokePhoto(currentPoke));
                        return;
                    }
                    //都是pc页,且不同页
                    inv.setItem(clickSlot, PokeUtil.getFormatPokePhoto(cursorPoke));
                }
                return;
            }
            //右键
            if (clickType.isRightClick()) {
                e.setCancelled(true);
                //鼠标上有宝可梦或者点击的地方没有宝可梦都直接返回
                if (cursorPoke != null || currentPoke == null) return;
                //最后一只背包精灵则不打开了
                if (!Config.packCanEmpty) {
                    val storage = currentPoke.getStorage();
                    if (StorageHelper.isParty(storage)) {
                        val list = storage.all();
                        if (list.size() < 2) return;
                    }
                }

                ConfirmGui confirmGui = new ConfirmGui(currentPoke);
                Inventory temp = this.getPreviousInv();
                this.setPreviousInv(null);
                this.setPreviousInv(temp);
                confirmGui.setPreviousInv(this.inventory);
                whoClicked.openInventory(confirmGui.getInventory());
            }
        });

        this.onClose(e -> {
            e.getPlayer().setItemOnCursor(null);
            if (this.getPreviousInv() == null) return;
            Bukkit.getScheduler().runTask(Config.plugin, () ->
                    e.getPlayer().openInventory(this.getPreviousInv()));
        });
    }

    /**
     * 将点击者贯标上的宝可梦放入一个位置
     * 返回值证明是否可移动
     */
    private static boolean putInto(Pair<IPokeStorageWrapper<?>, StoragePos> cursorInfo, Pair<IPokeStorageWrapper<?>, StoragePos> currentInfo, IPokemonWrapper<?> putIntoPoke, HumanEntity whoClicked, Inventory inv, int clickSlot) {
        //判断要放的精灵是否是背包且是唯一一只的情况下,并更具配置来确定是否拦截
        if (!Config.packCanEmpty) {
            if (StorageHelper.isParty(putIntoPoke.getStorage())) {
                val list = Lists.newArrayList(putIntoPoke.getStorage().all());
                list.removeIf(Objects::isNull);
                if (list.size() < 2 && !StorageHelper.isParty(currentInfo.getKey())) return false;
            }
        }
        inv.setItem(clickSlot, null);
        cursorInfo.getKey().set(cursorInfo.getValue(), null);
        currentInfo.getKey().set(currentInfo.getValue(), putIntoPoke);
        whoClicked.setItemOnCursor(null);
        val target_list = StorageHelper.isParty(currentInfo.getKey()) ? Config.invBackpackSlot : Config.invPcSlot;
        inv.setItem(target_list.get(currentInfo.getValue().getSlot()), PokeUtil.getFormatPokePhoto(putIntoPoke));
        return true;
    }

    private void changePage(HumanEntity player, int page, ItemStack cursor) {
        val temp = this.getPreviousInv();
        this.setPreviousInv(null);
        val gui = new PCPageGui(new PCBox(box.getPc(),page));
        gui.setPreviousInv(temp);
        val pokemon = StorageHelper.find(PokeUtil.getFormatItemUUID(cursor), box.getPc(), party);
        if (pokemon != null) {
            boolean b = StorageHelper.isParty(pokemon.getStorage());
            val position = pokemon.getStoragePos();
            int box = position.getBox();
            if (b || box == page) {
                ArrayList<Integer> list = b ? Config.invBackpackSlot : Config.invPcSlot;
                gui.getInventory().setItem(list.get(position.getSlot()), null);
            }
        }
        cursor = PokeUtil.getFormatPokePhoto(pokemon);
        val event = new PCPageChangeEvent(this.getInventory(), gui.getInventory(), cursor);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        player.openInventory(event.getTarget());
        player.setItemOnCursor(event.getCursorItem());
    }

    private void initFrame() {
        //页面初始化
        //填满
        {
            val itemStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 4);
            val itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(" ");
            itemStack.setItemMeta(itemMeta);
            val list = (ArrayList<Integer>) Config.invPcSlot.clone();
            list.addAll(Config.invBackpackSlot);
            for (int i = 0; i < inventory.getSize(); i++) if (!list.contains(i)) this.inventory.setItem(i, itemStack);
        }
        //上下页按钮控制
        {
            //上
            ItemStack itemStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 2);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(PapiUtil.papi(null, Config.plugin.getConfig().getString("msg.pc_page_gui_previous_button")));
            itemStack.setItemMeta(itemMeta);
            this.inventory.setItem(45, itemStack);
        }
        {
            //下
            ItemStack itemStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 2);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(PapiUtil.papi(null, Config.plugin.getConfig().getString("msg.pc_page_gui_next_button")));
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
            for (int i = 0; i < 30; i++) {
                val pokemon = box.get(i);
                ItemStack photo = PokeUtil.getFormatPokePhoto(pokemon);
                this.inventory.setItem(Config.invPcSlot.get(i), photo);
            }
        }
        //Pack
        {
            for (int i = 0; i < 6; i++) {
                val pokemon = party.get(new StoragePos(-1, i));
                ItemStack photo = PokeUtil.getFormatPokePhoto(pokemon);
                this.inventory.setItem(Config.invBackpackSlot.get(i), photo);
            }
        }
    }
}
