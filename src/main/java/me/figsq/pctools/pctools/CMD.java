package me.figsq.pctools.pctools;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.storage.PCBox;
import me.figsq.pctools.pctools.api.util.TidyPcUtil;
import me.figsq.pctools.pctools.gui.PCGui;
import me.figsq.pctools.pctools.api.util.Cache;
import me.figsq.pctools.pctools.api.util.SomeMethod;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CMD implements TabExecutor {
    enum Permissions{
        RELOAD, OPEN, OPENOTHER;
        public String getPermission(){
            return Cache.plugin.getDescription().getName().toLowerCase()+".cmd."+this.name().toLowerCase();
        }
    }

    ArrayList<String> subArg = Lists.newArrayList(
            "help", "reload", "open"
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
        return null;
    }
}
