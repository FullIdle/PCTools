package me.figsq.pctools.pctools;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import me.figsq.pctools.pctools.api.ISearchProperty;
import me.figsq.pctools.pctools.api.enums.Permissions;
import me.figsq.pctools.pctools.gui.PCGui;
import me.figsq.pctools.pctools.api.util.Cache;
import me.figsq.pctools.pctools.api.util.SomeMethod;
import me.figsq.pctools.pctools.gui.PCResultGui;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.*;
import java.util.stream.Collectors;

public class CMD implements TabExecutor {
    ArrayList<String> subArg = Lists.newArrayList(
            "help", "reload", "open", "search"
    );

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0) {
            String arg = args[0];
            if (subArg.contains(arg.toLowerCase())){
                if (arg.equalsIgnoreCase("help")) {
                    sender.sendMessage(Cache.helpMsg);
                }
                if (arg.equalsIgnoreCase("reload")){
                    String permission = Permissions.RELOAD.getPermission();
                    if (!sender.hasPermission(permission)){
                        sender.sendMessage("§cYou lack §3"+permission+"§c permissions");
                        return false;
                    }
                    Cache.plugin.reloadConfig();
                    sender.sendMessage(SomeMethod.papi(null,Cache.plugin.getConfig()
                            .getString("msg.reloadSuccessful")));
                }
                if (arg.equalsIgnoreCase("open")){
                    String permission = Permissions.OPEN.getPermission();
                    if (!sender.hasPermission(permission)){
                        sender.sendMessage("§cYou lack §3"+permission+"§c permissions");
                        return false;
                    }

                    if (!(sender instanceof Player)){
                        sender.sendMessage("§cThis command is not available to non-players!");
                        return false;
                    }
                    Player player = (Player) sender;
                    //页码
                    int page;
                    try {
                        page = args.length >= 2 ? Integer.parseInt(args[1]) :
                                Pixelmon.storageManager.getPCForPlayer(player.getUniqueId()).getLastBox()+1;
                        if (page < 1) {
                            sender.sendMessage(SomeMethod.papi(null,"§cNoPage "+page+" !!!"));
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage(SomeMethod.papi(null,Cache.plugin.getConfig()
                                .getString("msg.nonNumeric")));
                        return false;
                    }
                    Player other = null;
                    if (args.length > 2){
                        String otherPer = Permissions.OPENOTHER.getPermission();
                        if (!sender.hasPermission(otherPer)){
                            sender.sendMessage("§cYou lack §3"+otherPer+"§c permissions");
                            return false;
                        }


                        other = Bukkit.getPlayer(args[2]);
                        if (other == null){
                            sender.sendMessage(SomeMethod.papi(null,Cache.plugin.getConfig()
                                    .getString("msg.playerDoesNotExist")));
                            return false;
                        }
                    }
                    PCGui pcGui = new PCGui((other == null ? player : other),page-1);
                    Inventory inv = pcGui.getInventory();
                    player.closeInventory();
                    player.openInventory(inv);
                }
                if (arg.equalsIgnoreCase("search")){
                    String permission = Permissions.SEARCH.getPermission();
                    if (!sender.hasPermission(permission)){
                        sender.sendMessage("§cYou lack §3"+permission+"§c permissions");
                        return false;
                    }
                    if (!(sender instanceof Player)){
                        sender.sendMessage("§cThis command is not available to non-players!");
                        return false;
                    }
                    //解析
                    Map<ISearchProperty,String> map = new HashMap<>();
                    for (int i = 1; i < args.length; i++) {
                        String s = args[i];
                        if (!s.contains(":")) {
                            sender.sendMessage("无效参数: "+s);
                            return false;
                        }
                        String[] split = s.split(":");
                        String k = split[0];
                        String a = split[1];
                        if (!Cache.searchProperties.containsKey(k)){
                            sender.sendMessage("无效参数: "+s);
                            return false;
                        }
                        map.put(Cache.searchProperties.get(k),a);
                    }
                    Player player = (Player) sender;
                    if (map.isEmpty()){
                        player.closeInventory();
                        player.openInventory(new PCResultGui(new ArrayList<>()).getInventory());
                        return false;
                    }

                    List<Pokemon> pokemons = Lists.newArrayList(
                            Pixelmon.storageManager.getPCForPlayer(player.getUniqueId()).getAll());
                    pokemons.removeIf(pokemon -> {
                        if (pokemon == null)return true;
                        for (Map.Entry<ISearchProperty, String> entry : map.entrySet()) {
                            if (!entry.getKey().hasProperty(pokemon, entry.getValue())) {
                                return true;
                            }
                        }
                        return false;
                    });
                    player.closeInventory();
                    player.openInventory(new PCResultGui(pokemons).getInventory());
                }
                return false;
            }
        }
        sender.sendMessage(Cache.helpMsg);
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            return subArg;
        }
        if (args.length == 1){
            return subArg.stream().filter(s->s.startsWith(args[0])).collect(Collectors.toList());
        }
        if (args[0].equalsIgnoreCase("search")){
            ArrayList<String> list = new ArrayList<>();
            for (String arg : args) {
                if (arg.contains(":")) {
                    list.add(arg.substring(0, arg.indexOf(":")));
                }
            }
            return Cache.searchProperties.keySet().stream().
                    filter(s->s.startsWith(args[args.length-1])&&!list.contains(s)).map(s->s+":").
                    collect(Collectors.toList());
        }
        return null;
    }
}
