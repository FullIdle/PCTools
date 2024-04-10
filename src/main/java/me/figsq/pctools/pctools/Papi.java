package me.figsq.pctools.pctools;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.BaseStats;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IStatStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Stats;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.items.ItemHeld;
import com.pixelmonmod.pixelmon.items.heldItems.NoItem;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import lombok.SneakyThrows;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.figsq.pctools.pctools.api.util.Cache;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static me.figsq.pctools.pctools.api.util.Cache.*;

public class Papi extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return plugin.getDescription().getName().toLowerCase();
    }

    @Override
    public @NotNull String getAuthor() {
        return Arrays.toString(plugin.getDescription().getAuthors().toArray());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @SneakyThrows
    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        Pokemon poke = null;
        ArrayList<String> args = Lists.newArrayList(params.split("_"));

        String pokeO = args.get(0);
        if (pokeO.startsWith("{") && pokeO.endsWith("}")) {
            if (pokeO.length() != 2) {
                poke = Pixelmon.pokemonFactory.create(JsonToNBT.func_180713_a(pokeO));
                //删一
            }
        } else if (pokeO.startsWith("pokedex:")) {
            poke = Pixelmon.pokemonFactory.create(
                    EnumSpecies.getFromDex(Integer.parseInt(pokeO.substring(8))));
        } else if (pokeO.startsWith("species:")) {
            poke = Pixelmon.pokemonFactory.create(
                    EnumSpecies.getFromNameAnyCase(pokeO.substring(8)));
        } else{
            int box = (int) (Double.parseDouble(pokeO) + papiIndexOffset);
            int order = (int) (Double.parseDouble(args.get(1)) + papiIndexOffset);
            poke = Pixelmon.storageManager.getPokemon(
                    (EntityPlayerMP) ((Object) ((CraftEntity) player).getHandle()),
                    new StoragePosition(box, order));
            //删两
            args.remove(0);
        }
        args.remove(0);
        return papiReplace(parsePoke(player,poke, args),args);
    }

    public static String papiReplace(String request,ArrayList<String> args){
        a:
        for (Map.Entry<String, ConfigurationSection> entry : argsPapiReplace.entrySet()) {
            String old = entry.getKey();
            if (old.contains("_")) {
                String[] ss = old.split("_");
                if (ss.length > args.size()) continue;
                for (int i = 0; i < ss.length; i++) {
                    if (!ss[i].equalsIgnoreCase(args.get(i))) continue a;
                }
                ConfigurationSection value = entry.getValue();
                for (String key : value.getKeys(false)) {
                    request = request.replace(key, value.getString(key));
                }
                continue;
            }
            if (args.get(0).equalsIgnoreCase(old)) {
                ConfigurationSection value = entry.getValue();
                for (String key : value.getKeys(false)) {
                    request = request.replace(key, value.getString(key));
                }
            }
        }

        for (Map.Entry<String, String> entry : globalPapiReplace.entrySet()) {
            if (request.contains(entry.getKey())) {
                request = request.replace(entry.getKey(), entry.getValue());
            }
        }
        return request;
    }

    @SneakyThrows
    public static String parsePoke(OfflinePlayer player,Pokemon poke, List<String> args) {
        if (args.isEmpty()) {
            return "WRONG FORMAT";
        }
        String arg = args.get(0).toLowerCase();
        if (poke == null) {
            //无宝可梦变量
            if (arg.equalsIgnoreCase("pokeslot")||
                    arg.equalsIgnoreCase("pcslot")||
                    arg.equalsIgnoreCase("partyslot")
            ) {
                PCStorage pc = Pixelmon.storageManager.getPCForPlayer(player.getUniqueId());
                PlayerPartyStorage party = Pixelmon.storageManager.getParty(player.getUniqueId());
                ArrayList<Pokemon> list = null;
                switch (arg){
                    case "pokeslot":{
                        list = Lists.newArrayList(pc.getAll());
                        list.addAll(Arrays.asList(party.getAll()));
                        break;
                    }
                    case "pcslot":{
                        list = Lists.newArrayList(pc.getAll());
                        break;
                    }
                    case "partyslot":{
                        list = Lists.newArrayList(party.getAll());
                        break;
                    }
                }
                list.removeIf(Objects::isNull);
                return String.valueOf(list.size());
            }
            //无宝可梦变量
            return "POKE IS EMPTY";
        }

        switch (arg) {
            case "hypertrained": {
                StatsType type = StatsType.getStatsEffect(args.get(1));
                if (type == null) return "UNKNOWN PARAMETERS";
                return String.valueOf(poke.getIVs().isHyperTrained(type));
            }
            case "mintnature": {
                return poke.getMintNature().getLocalizedName();
            }
            case "catchrate": {
                return String.valueOf(poke.getBaseStats().getCatchRate());
            }
            case "weight": {
                return String.valueOf(poke.getBaseStats().getWeight());
            }
            case "malepercent": {
                return String.valueOf(((int) poke.getBaseStats().getMalePercent()));
            }
            case "types": {
                return poke.getSpecies().getBaseStats().types.toString();
            }
            case "egggroup": {
                return Arrays.toString(poke.getSpecies().getBaseStats().eggGroups);
            }
            case "position": {
                String o = args.get(1);
                StoragePosition position = poke.getPosition();
                return o.equalsIgnoreCase("box") ?
                        String.valueOf(((int) (position.box - papiIndexOffset))) : o.equalsIgnoreCase("order") ?
                        String.valueOf(((int) (position.order - papiIndexOffset))) : "UNKNOWN PARAMETERS";
            }
            case "statstotal": {
                Stats stats = poke.getStats();
                return String.valueOf(addUp(Lists.newArrayList(
                        stats.hp,
                        stats.attack,
                        stats.defence,
                        stats.specialAttack,
                        stats.specialDefence,
                        stats.speed
                )));
            }
            case "stats":
                return stats(args.get(1), poke.getStats());
            case "basestats":
                return stats(args.get(1), poke.getBaseStats());
            case "basetotal": {
                return String.valueOf(addUp(poke.getBaseStats().stats.values()));
            }
            case "ivstotal":
                return String.valueOf(poke.getStats().ivs.getTotal());
            case "evstotal":
                return String.valueOf(poke.getStats().evs.getTotal());
            case "eggsteps":
                return String.valueOf(poke.getEggSteps());
            case "eggcycles":
                return String.valueOf(poke.getEggCycles());
            case "growth":
                return poke.getGrowth().getLocalizedName();
            case "helditem":
                ItemHeld held = poke.getHeldItemAsItemHeld();
                if (held == NoItem.noItem) {
                    return "NONE";
                }
                return held.getLocalizedName();
            case "status":
                return poke.getStatus().type.getLocalizedName();
            case "nature":
                return poke.getNature().getLocalizedName();
            case "gender":
                return poke.getGender().getLocalizedName();
            case "hastag":
                return String.valueOf(poke.hasSpecFlag(args.get(1).toLowerCase()));
            case "uuid":
                return poke.getUUID().toString();
            case "moveset": {
                int i = Integer.parseInt(args.get(1));
                Attack attack = poke.getMoveset().attacks[i];
                return attack == null ? "NONE" : attack.getMove().getLocalizedName();
            }
            case "originalname":
                return poke.getSpecies().name;
            case "localizedname":
                return poke.getLocalizedName();
            case "nickname":
                String nickname = poke.getNickname();
                return nickname == null ? poke.getSpecies().name : nickname;
            case "ability":
                return poke.getAbility().getLocalizedName();
            case "islegendary":
                return String.valueOf(poke.isLegendary());
            case "isegg":
                return String.valueOf(poke.isEgg());
            case "isultrabeast":
                return String.valueOf(poke.getSpecies().isUltraBeast());
            case "ivs":
                return stats(args.get(1), poke.getStats().ivs);
            case "evs":
                return stats(args.get(1), poke.getStats().evs);
            case "level":
                return String.valueOf(poke.getLevel());
            case "shiny":
                return String.valueOf(poke.isShiny());
            case "inranch":
                return String.valueOf(poke.isInRanch());
            case "caughtball":
                return poke.getCaughtBall().getLocalizedName();
            case "form": {
                String s = args.get(1);
                return s.equalsIgnoreCase("number") ? String.valueOf(poke.getForm()) :
                        s.equalsIgnoreCase("localizedname") ? poke.getFormEnum().getLocalizedName() :
                                "UNKNOWN PARAMETERS";
            }
            case "nbt": {
                JsonElement json = gson.fromJson(poke.writeToNBT(new NBTTagCompound()).toString(), JsonObject.class);
                String path = args.get(1);
                String[] keys = path.split("\\.");
                for (String key : keys) {
                    if (json == null || json.isJsonNull()) return "NO DATA";
                    if (json.isJsonArray()) {
                        json = ((JsonArray) json).get(Integer.parseInt(key));
                        continue;
                    }
                    json = ((JsonObject) json).get(key);
                    if (json == null || json.isJsonNull()) return "NO DATA";
                }
                return json.getAsString();
            }
            default:
                return "UNKNOWN PARAMETERS";
        }
    }

    public static String stats(String arg, Object stats) {
        String lowerCase = arg.toLowerCase();
        StatsType type = getStatsType(lowerCase);
        if (type != null) {
            return String.valueOf(stats instanceof IStatStore ?
                    ((IStatStore) stats).getStat(type) : stats instanceof Stats ?
                    ((Stats) stats).get(type) :
                    ((BaseStats) stats).getStat(type));
        }
        return null;
    }

    private static StatsType getStatsType(String lowerCase) {
        switch (lowerCase) {
            case "hp":
                return StatsType.HP;
            case "sp":
                return StatsType.Speed;
            case "at":
                return StatsType.Attack;
            case "df":
                return StatsType.Defence;
            case "sd":
                return StatsType.SpecialDefence;
            case "sa":
                return StatsType.SpecialAttack;
            default:
                return null;
        }
    }

    private static Integer addUp(Collection<Integer> integers) {
        int x = 0;
        for (Integer i : integers) {
            x += i;
        }
        return x;
    }
}
