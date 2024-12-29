package me.figsq.pctools.pctools;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.pokemon.egg.EggGroup;
import com.pixelmonmod.pixelmon.api.pokemon.species.Stats;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.api.pokemon.stats.IStatStore;
import com.pixelmonmod.pixelmon.api.pokemon.stats.PermanentStats;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.api.util.ITranslatable;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.items.HeldItem;
import com.pixelmonmod.pixelmon.items.heldItems.NoItem;
import lombok.SneakyThrows;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.text.TranslationTextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static me.figsq.pctools.pctools.api.Cache.*;

public class Papi extends PlaceholderExpansion{
    @Override
    public @NotNull String getIdentifier() {
        return plugin.getDescription().getName().toLowerCase();
    }

    @Override
    public boolean persist() {
        return true;
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
        if (params.contains("{") && params.contains("}")) {
            int start = params.indexOf('{');
            int end = params.lastIndexOf('}') + 1;
            if (pokeO.length() != 2) {
                String substring = params.substring(start, end);
                poke = PokemonFactory.create(JsonToNBT.func_180713_a(substring));
                args = Lists.newArrayList(params.replace(substring,"").split("_"));
                //删一
            }
        } else if (pokeO.startsWith("pokedex:")) {
            poke = PokemonFactory.create(
                    PixelmonSpecies.fromDex(Integer.parseInt(pokeO.substring(8))).get());
        } else if (pokeO.startsWith("species:")) {
            poke = PokemonFactory.create(PixelmonSpecies.fromName(pokeO.substring(8)).getValue().get());
        } else {
            int box = (int) (Double.parseDouble(pokeO) + papiIndexOffset);
            int order = (int) (Double.parseDouble(args.get(1)) + papiIndexOffset);
            poke = StorageProxy.getPokemon(
                    (ServerPlayerEntity) ((Object) ((CraftEntity) player).getHandle()),
                    new StoragePosition(box, order));
            //删两
            args.remove(0);
        }
        args.remove(0);
        return papiReplace(parsePoke(player, poke, args), args);
    }

    public static String papiReplace(String request, ArrayList<String> args) {
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
    public static String parsePoke(OfflinePlayer player, Pokemon poke, List<String> args) {
        if (args.isEmpty()) {
            return "WRONG FORMAT";
        }
        String arg = args.get(0).toLowerCase();
        if (poke == null) {
            //无宝可梦变量
            if (arg.equalsIgnoreCase("pokeslot") ||
                    arg.equalsIgnoreCase("pcslot") ||
                    arg.equalsIgnoreCase("partyslot")
            ) {
                PCStorage pc = StorageProxy.getPCForPlayer(player.getUniqueId());
                PlayerPartyStorage party = StorageProxy.getParty(player.getUniqueId());
                ArrayList<Pokemon> list = null;
                switch (arg) {
                    case "pokeslot": {
                        list = Lists.newArrayList(pc.getAll());
                        list.addAll(Arrays.asList(party.getAll()));
                        break;
                    }
                    case "pcslot": {
                        list = Lists.newArrayList(pc.getAll());
                        break;
                    }
                    case "partyslot": {
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
            case "description":{
                return (new TranslationTextComponent("pixelmon." + poke.getSpecies().getName().toLowerCase() + ".description")).getString();
            }
            case "hypertrained": {
                BattleStatsType type = BattleStatsType.getStatsEffect(getStatsType(args.get(1).toLowerCase()).name());
                if (type == null) return "UNKNOWN PARAMETERS";
                return String.valueOf(poke.getIVs().isHyperTrained(type));
            }
            case "mintnature": {
                return poke.getMintNature().getLocalizedName();
            }
            case "catchrate": {
                return String.valueOf(poke.getForm().getCatchRate());
            }
            case "weight": {
                return String.valueOf(poke.getForm().getWeight());
            }
            case "malepercent": {
                return String.valueOf(((int) poke.getForm().getMalePercentage()));
            }
            case "types": {
                ArrayList<String> list = new ArrayList<>();
                for (Element element : poke.getForm().getTypes()) {
                    list.add(element.getLocalizedName());
                }
                return list.toString();
            }
            case "formtypes":{
                return poke.getSpecies().getForms(s->s.getName().equalsIgnoreCase(args.get(1)))
                        .get(0).getTypes()
                        .stream().map(Element::getLocalizedName).
                        collect(Collectors.toCollection(ArrayList::new))
                        .toString();
            }
            case "egggroup": {
                ArrayList<String> list = new ArrayList<>();
                for (EggGroup eggGroup : poke.getForm().getEggGroups()) {
                    list.add(eggGroup.getLocalizedName());
                }
                return list.toString();
            }
            case "position": {
                String o = args.get(1);
                StoragePosition position = poke.getPosition();
                return o.equalsIgnoreCase("box") ?
                        String.valueOf(((int) (position.box - papiIndexOffset))) : o.equalsIgnoreCase("order") ?
                        String.valueOf(((int) (position.order - papiIndexOffset))) : "UNKNOWN PARAMETERS";
            }
            case "statstotal": {
                PermanentStats stats = poke.getStats();
                return String.valueOf(addUp(Lists.newArrayList(
                        stats.getHP(),
                        stats.getAttack(),
                        stats.getDefense(),
                        stats.getSpecialAttack(),
                        stats.getSpecialDefense(),
                        stats.getSpeed()
                )));
            }
            case "stats":
                return stats(args.get(1), poke.getStats());
            case "basestats":
                return stats(args.get(1), poke.getForm());
            case "formstats":{
                return stats(args.get(2),poke.getSpecies()
                        .getForms(s->s.getName().equalsIgnoreCase(args.get(1))).get(0));
            }
            case "basetotal": {
                return String.valueOf(addUp(poke.getStats().toArray()));
            }
            case "ivstotal":
                return String.valueOf(poke.getStats().getIVs().getTotal());
            case "evstotal":
                return String.valueOf(poke.getStats().getEVs().getTotal());
            case "eggsteps":
                return String.valueOf(poke.getEggSteps());
            case "eggcycles":
                return String.valueOf(poke.getEggCycles());
            case "growth":
                return poke.getGrowth().getLocalizedName();
            case "helditem":
                HeldItem held = poke.getHeldItemAsItemHeld();
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
                return String.valueOf(poke.hasFlag(args.get(1).toLowerCase()));
            case "uuid":
                return poke.getUUID().toString();
            case "moveset": {
                int i = Integer.parseInt(args.get(1));
                Attack attack = poke.getMoveset().attacks[i];

                if (attack == null) return "NOTE";

                if (args.size() <= 2){
                    return attack.getMove().getLocalizedName();
                }
                //描述
                String sub_arg = args.get(2).toLowerCase();
                switch (sub_arg) {
                    case "id": {
                        return String.valueOf(attack.getMove().getAttackIndex());
                    }
                    case "type": {
                        return attack.getMove().getAttackType().name();
                    }
                    case "ac": {
                        return attack.getMove().getAttackCategory().name();
                    }
                    case "bp": {
                        return String.valueOf(attack.getMove().getBasePower());
                    }
                    case "ppb": {
                        return String.valueOf(attack.getMove().getPPBase());
                    }
                    case "ppm": {
                        return String.valueOf(attack.getMove().getPPMax());
                    }
                    case "acc": {
                        return String.valueOf(attack.getMove().getAccuracy());
                    }
                    case "mc": {
                        return String.valueOf(attack.getMove().getMakesContact());
                    }
                    case "desc": {
                        String key = "attack." + attack.getMove().getAttackName().toLowerCase().replace(" ", "_") + args.get(2);
                        return (new TranslationTextComponent(key)).getString();
                    }
                }
            }
            case "originalname":
                return poke.getSpecies().getName();
            case "localizedname":
                return poke.getLocalizedName();
            case "nickname":
                String nickname = poke.getNickname();
                return nickname == null ? poke.getSpecies().getLocalizedName() : nickname;
            case "ability":
                return poke.getAbility().getLocalizedName();
            case "formability":
                return Arrays.stream(poke.getSpecies()
                        .getForms(s->s.getName().equalsIgnoreCase(args.get(1))).get(0)
                        .getAbilities().getAll())
                        .map(ITranslatable::getLocalizedName)
                        .collect(Collectors.toCollection(ArrayList::new)).toString();
            case "islegendary":
                return String.valueOf(poke.isLegendary());
            case "isegg":
                return String.valueOf(poke.isEgg());
            case "isultrabeast":
                return String.valueOf(poke.getSpecies().isUltraBeast());
            case "ivs":
                return stats(args.get(1), poke.getStats().getIVs());
            case "evs":
                return stats(args.get(1), poke.getStats().getEVs());
            case "level":
                return String.valueOf(poke.getPokemonLevel());
            case "shiny":
                return String.valueOf(poke.isShiny());
            case "inranch":
                return "1.16.5 No ranch";
            case "caughtball":
                return poke.getBall().getLocalizedName();
            case "form": {
                String s = args.get(1);
                return s.equalsIgnoreCase("number") ? String.valueOf(poke.getForm()) :
                        s.equalsIgnoreCase("localizedname") ? poke.getForm().getLocalizedName() :
                                "UNKNOWN PARAMETERS";
            }
            case "nbt": {
                JsonElement json = gson.fromJson(poke.writeToNBT(new CompoundNBT()).toString(), JsonObject.class);
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
        BattleStatsType type = getStatsType(lowerCase);
        if (type != null) {
            return String.valueOf(stats instanceof IStatStore ?
                    ((IStatStore) stats).getStat(type) : stats instanceof PermanentStats ?
                    ((PermanentStats) stats).get(type) :
                    ((Stats) stats).getBattleStats().getStat(type));
        }
        return null;
    }

    private static BattleStatsType getStatsType(String lowerCase) {
        switch (lowerCase) {
            case "hp":
                return BattleStatsType.HP;
            case "sp":
            case "speed":
                return BattleStatsType.SPEED;
            case "at":
            case "attack":
                return BattleStatsType.ATTACK;
            case "df":
            case "defence":
                return BattleStatsType.DEFENSE;
            case "sd":
            case "specialdefence":
                return BattleStatsType.SPECIAL_DEFENSE;
            case "sa":
            case "specialattack":
                return BattleStatsType.SPECIAL_ATTACK;
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

    private static Integer addUp(int[] ints) {
        int x = 0;
        for (int anInt : ints) {
            x += anInt;
        }
        return x;
    }
}
