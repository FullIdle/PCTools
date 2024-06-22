package me.figsq.pctools.pctools.gui;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.config.PixelmonConfigProxy;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.*;
import com.pixelmonmod.pixelmon.api.util.helpers.NetworkHelper;
import com.pixelmonmod.pixelmon.comm.packetHandlers.clientStorage.newStorage.pc.ClientSetLastOpenBoxPacket;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import lombok.Getter;
import me.figsq.pctools.pctools.api.events.PCPageChangeEvent;
import me.figsq.pctools.pctools.api.util.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

@Getter
public class PCPageGui extends AbstractPreviousInv {
    private final Inventory inventory;
    private final PCBox box;
    private final PlayerPartyStorage party;


    public PCPageGui(PCBox box) {
        this.box = box;
        this.inventory = Bukkit.createInventory(this, 54,
                PapiUtil.papi(Bukkit.getOfflinePlayer(this.box.pc.playerUUID),
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
                itemMeta.setDisplayName(PapiUtil.papi(null, Cache.plugin.getConfig().getString("msg.pc_page_gui_sort_button")));
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
            Tuple<PokemonStorage, StoragePosition> currentInfo = PokeUtil.computeStorageAndPosition(next, party, box);
            Pokemon cursorPoke = StorageHelper.find(PokeUtil.getFormatItemUUID(cursor), party, box.pc);
            Tuple<PokemonStorage, StoragePosition> cursorInfo = new Tuple<>(cursorPoke.getStorage(), cursorPoke.getPosition());
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
                if (whoClicked.hasPermission("pctools.function.sort")) {
                    e.setCancelled(true);
                    whoClicked.setItemOnCursor(null);
                    SortGui gui = new SortGui();
                    gui.setPreviousInv(this.inventory);
                    Inventory temp = this.getPreviousInv();
                    this.setPreviousInv(null);
                    whoClicked.openInventory(gui.getInventory());
                    this.setPreviousInv(temp);
                    return;
                }
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
            Tuple<PokemonStorage, StoragePosition> currentInfo = PokeUtil.computeStorageAndPosition(clickSlot, party, box);
            Pokemon currentPoke = currentInfo.func_76341_a().get(currentInfo.func_76340_b());
            UUID cursorPokeUuid = PokeUtil.getFormatItemUUID(cursorItem);
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
                    //判断精灵实体
                    cursorPoke.getPixelmonEntity().ifPresent(Entity::func_70106_y);
                    currentPoke.getPixelmonEntity().ifPresent(Entity::func_70106_y);
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
                        inv.setItem(target.get(currentInfo.func_76340_b().order), PokeUtil.getFormatPokePhoto(cursorPoke));
                        inv.setItem(target.get(cursorInfo.func_76340_b().order), PokeUtil.getFormatPokePhoto(currentPoke));
                        return;
                    }
                    //其中一个是背包
                    if (currentIsParty) {
                        inv.setItem(Cache.invBackpackSlot.get(currentInfo.func_76340_b().order), PokeUtil.getFormatPokePhoto(cursorPoke));
                        inv.setItem(Cache.invPcSlot.get(cursorInfo.func_76340_b().order), PokeUtil.getFormatPokePhoto(currentPoke));
                        return;
                    }
                    if (cursorIsParty) {
                        inv.setItem(Cache.invPcSlot.get(currentInfo.func_76340_b().order), PokeUtil.getFormatPokePhoto(cursorPoke));
                        inv.setItem(Cache.invBackpackSlot.get(cursorInfo.func_76340_b().order), PokeUtil.getFormatPokePhoto(currentPoke));
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
                if (!Cache.packCanEmpty) {
                    PokemonStorage storage = currentPoke.getStorage();
                    if (storage instanceof PlayerPartyStorage) {
                        ArrayList<Pokemon> list = Lists.newArrayList(storage.getAll());
                        list.removeIf(Objects::isNull);
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
     * 返回值证明是否可移动
     */
    private static boolean putInto(Tuple<PokemonStorage, StoragePosition> cursorInfo, Tuple<PokemonStorage, StoragePosition> currentInfo, Pokemon putIntoPoke, HumanEntity whoClicked, Inventory inv, int clickSlot) {
        //判断要放的精灵是否是背包且是唯一一只的情况下,并更具配置来确定是否拦截
        if (!Cache.packCanEmpty) {
            if (putIntoPoke.getStorage() instanceof PlayerPartyStorage) {
                ArrayList<Pokemon> list = Lists.newArrayList(putIntoPoke.getStorage().getAll());
                list.removeIf(Objects::isNull);
                if (list.size() < 2 && !(currentInfo.func_76341_a() instanceof PlayerPartyStorage)) {
                    return false;
                }
            }
        }
        inv.setItem(clickSlot, null);

        cursorInfo.func_76341_a().set(cursorInfo.func_76340_b(), null);
        currentInfo.func_76341_a().set(currentInfo.func_76340_b(), putIntoPoke);
        whoClicked.setItemOnCursor(null);
        ArrayList<Integer> target_list = currentInfo.func_76341_a() instanceof PlayerPartyStorage ? Cache.invBackpackSlot : Cache.invPcSlot;
        inv.setItem(target_list.get(currentInfo.func_76340_b().order), PokeUtil.getFormatPokePhoto(putIntoPoke));
        return true;
    }

    private void changePage(HumanEntity player, int page, ItemStack cursor) {
        Inventory temp = this.getPreviousInv();
        this.setPreviousInv(null);
        PCPageGui gui = new PCPageGui(box.pc.getBox(page));
        gui.setPreviousInv(temp);
        Pokemon pokemon = StorageHelper.find(PokeUtil.getFormatItemUUID(cursor), box.pc, party);
        if (pokemon != null) {
            boolean b = pokemon.getStorage() instanceof PlayerPartyStorage;
            StoragePosition position = pokemon.getPosition();
            int box = position.box;
            if (b || box == page) {
                ArrayList<Integer> list = b ? Cache.invBackpackSlot : Cache.invPcSlot;
                gui.getInventory().setItem(list.get(position.order), null);
            }
        }
        cursor = PokeUtil.getFormatPokePhoto(pokemon);
        PCPageChangeEvent event = new PCPageChangeEvent(this.getInventory(), gui.getInventory(), cursor);
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
            itemMeta.setDisplayName(PapiUtil.papi(null, Cache.plugin.getConfig().getString("msg.pc_page_gui_previous_button")));
            itemStack.setItemMeta(itemMeta);
            this.inventory.setItem(45, itemStack);
        }
        {
            //下
            ItemStack itemStack = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(PapiUtil.papi(null, Cache.plugin.getConfig().getString("msg.pc_page_gui_next_button")));
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
                    ItemStack photo = PokeUtil.getFormatPokePhoto(pokemon);
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
                    ItemStack photo = PokeUtil.getFormatPokePhoto(pokemon);
                    this.inventory.setItem(Cache.invBackpackSlot.get(i), photo);
                }
            }
        }
    }
}
