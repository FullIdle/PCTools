package me.figsq.pctools.pctools.api;

import de.tr7zw.nbtapi.NBT;
import lombok.val;
import me.figsq.pctools.pctools.PCTools;
import me.fullidle.ficore.ficore.FICoreAPI;
import me.fullidle.ficore.ficore.common.api.pokemon.Element;
import me.fullidle.ficore.ficore.common.api.pokemon.Gender;
import me.fullidle.ficore.ficore.common.api.pokemon.storage.StoragePos;
import me.fullidle.ficore.ficore.common.api.pokemon.wrapper.IPokeStorageWrapper;
import me.fullidle.ficore.ficore.common.api.pokemon.wrapper.IPokemonWrapper;
import me.fullidle.ficore.ficore.common.api.pokemon.wrapper.ISpeciesWrapper;
/*import me.towdium.pinin.Keyboard;
import me.towdium.pinin.PinIn;*/
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PokeUtil {
    public static Map<ISpeciesWrapper<?>, Pair<String, List<String>>> specialNAL = new HashMap<>();

    public static void init() {
        specialNAL.clear();


        val plugin = PCTools.INSTANCE;
        val logger = plugin.getLogger();

        FileConfiguration config = plugin.getConfig();

        //special name lore
        for (String key : config.getConfigurationSection("item.special").getKeys(false)) {
            try {
                ISpeciesWrapper<?> species = FICoreAPI.getSpeciesWrapperFactory().create(key);
                val pair = new Pair<>(config.getString("item.special." + key + ".name"),
                        config.getStringList("item.special." + key + ".lore"));
                specialNAL.put(
                        species,
                        pair
                );
            } catch (IllegalStateException e) {
                val message = e.getLocalizedMessage();
                logger.warning(message + " 配置中出现了无法被获取的物种! >>" + key);
            }
        }
    }

    public static ItemStack getFormatPokePhoto(IPokemonWrapper<?> pokemon) {
        if (pokemon == null) {
            return null;
        }
        val photo = pokemon.createPhotoItem();
        NBT.modify(photo, nbt -> {
            nbt.setString("pctoolsUUID", pokemon.getUUID().toString());
        });

        val owner = pokemon.getOwner();
        val player = owner.getPlayer();
        assert player != null;
        ItemMeta itemMeta = photo.getItemMeta();
        String name;
        List<String> lore;
        Pair<String, List<String>> pair = specialNAL.get(pokemon.getSpecies());
        if (pair == null) {
            name = pokemon.isEgg() ?
                    Config.eggName : pokemon.isLegend() ?
                    Config.legendName : pokemon.isUltra() ?
                    Config.uBeastName : Config.normalName;
            lore = pokemon.isEgg() ?
                    Config.eggLore : pokemon.isLegend() ?
                    Config.legendLore : pokemon.isUltra() ?
                    Config.uBeastLore : Config.normalLore;
        } else {
            name = pair.getKey();
            lore = pair.getValue();
        }

        val ps = pokemon.getStoragePos();
        Function<String, String> fun = s -> s.replace("{box}", String.valueOf(((int) (ps.getBox() - Config.papiIndexOffset))))
                .replace("{order}", String.valueOf(((int) (ps.getSlot() - Config.papiIndexOffset))));
        itemMeta.setDisplayName(PapiUtil.papi(player, fun.apply(name)));
        itemMeta.setLore(PapiUtil.papi(player, lore, fun));
        photo.setItemMeta(itemMeta);
        return photo;
    }

    public static UUID getFormatItemUUID(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().equals(Material.AIR)) return null;
        val str = NBT.get(itemStack, nbt -> nbt.hasTag("pctoolsUUID") ? nbt.getString("pctoolsUUID") : null);
        return str == null ? null : UUID.fromString(str);
    }

    public static Pair<IPokeStorageWrapper<?>, StoragePos> computeStorageAndPosition(int clickSlot, IPokeStorageWrapper<?> partyStorage, PCBox pcBox) {
        if (Config.invBackpackSlot.contains(clickSlot))
            return new Pair<>(partyStorage, new StoragePos(-1, Config.invBackpackSlot.indexOf(clickSlot)));
        if (Config.invPcSlot.contains(clickSlot))
            return new Pair<>(pcBox, new StoragePos(pcBox.getPage(), Config.invPcSlot.indexOf(clickSlot)));
        return null;
    }

/*
    public static final PinIn pinIn;
*/

    static {
/*        pinIn = new PinIn();
        pinIn.config().fCh2C(true).commit();
        pinIn.config().fZh2Z(true).commit();
        pinIn.config().fSh2S(true).commit();
        pinIn.config().fAng2An(true).commit();
        pinIn.config().fEng2En(true).commit();
        pinIn.config().fIng2In(true).commit();
        pinIn.config().accelerate(true).commit();
        pinIn.config().keyboard(Keyboard.QUANPIN).commit();*/

        //searchProperty
        ISearchProperty.addSearchProperty("name", new ISearchProperty() {
            @Override
            public String getName() {
                return "name";
            }

            @Override
            public boolean hasProperty(IPokemonWrapper<?> poke, String arg) {
                val name = poke.getSpecies().getName();
                val localizedName = poke.getTranslatedName();
                return localizedName.equalsIgnoreCase(arg)
                        || name.equalsIgnoreCase(arg);
/*                        || pinIn.contains(name, arg)
                        || pinIn.contains(localizedName, arg);*/
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                val storageManager = FICoreAPI.getPokeStorageManager();
                val party = storageManager.getParty(player);
                val pc = storageManager.getPC(player);
                ArrayList<IPokemonWrapper<?>> pokemons = new ArrayList<>(pc.all());
                pokemons.addAll(party.all());
                pokemons.removeIf(Objects::isNull);
                List<String> collect = pokemons.stream().map(IPokemonWrapper::getTranslatedName).collect(Collectors.toList());
                if (value.isEmpty()) {
                    return collect;
                }
                return collect.stream().filter(s -> s.startsWith(value)).collect(Collectors.toList());
            }
        });
        //性别
        ISearchProperty.addSearchProperty("gender", new ISearchProperty() {
            @Override
            public String getName() {
                return "gender";
            }

            @Override
            public boolean hasProperty(IPokemonWrapper<?> poke, String arg) {
                val gender = poke.getGender();
                return gender.name().equalsIgnoreCase(arg);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                List<String> collect = Arrays.stream(Gender.values()).map(Enum::name).collect(Collectors.toList());
                return collect.stream().filter(s -> s.startsWith(value)).collect(Collectors.toList());
            }
        });
        //属性
        ISearchProperty.addSearchProperty("typeContains", new ISearchProperty() {
            @Override
            public String getName() {
                return "typeContains";
            }

            @Override
            public boolean hasProperty(IPokemonWrapper<?> poke, String arg) {
                val list = new ArrayList<Element>();
                if (arg.contains(",")) {
                    for (String s : arg.split(",")) {
                        val e = Element.fromString(s);
                        if (e == null) continue;
                        list.add(e);
                    }
                } else {
                    val e = Element.fromString(arg);
                    if (e != null) list.add(e);
                }
                if (list.isEmpty()) return false;
                return new HashSet<>(poke.getTypes()).containsAll(list);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                List<String> collect = Arrays.stream(Element.values()).map(Element::name).collect(Collectors.toList());
                if (value.isEmpty()) return collect;
                return collect.stream().filter(s -> s.startsWith(value)).collect(Collectors.toList());
            }
        });
        //特性
        ISearchProperty.addSearchProperty("ability", new ISearchProperty() {
            @Override
            public String getName() {
                return "ability";
            }

            @Override
            public boolean hasProperty(IPokemonWrapper<?> poke, String arg) {
                val ability = poke.getAbility();
                return ability.getName().equalsIgnoreCase(arg);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                return Collections.emptyList();
            }
        });
        //性格
        ISearchProperty.addSearchProperty("nature", new ISearchProperty() {
            @Override
            public String getName() {
                return "nature";
            }

            @Override
            public boolean hasProperty(IPokemonWrapper<?> poke, String arg) {
                val nature = poke.getNature();
                return nature.getName().equalsIgnoreCase(arg);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                return Collections.emptyList();
            }
        });
        //闪光
        ISearchProperty.addSearchProperty("shiny", new ISearchProperty() {
            @Override
            public String getName() {
                return "shiny";
            }

            @Override
            public boolean hasProperty(IPokemonWrapper<?> poke, String arg) {
                return poke.isShiny() == Boolean.parseBoolean(arg);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                return Arrays.asList("false", "true");
            }
        });
    }
}
