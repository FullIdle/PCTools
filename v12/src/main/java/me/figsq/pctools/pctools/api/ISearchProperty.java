package me.figsq.pctools.pctools.api;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;

public interface ISearchProperty {
    public String getName();
    public boolean hasProperty(Pokemon poke,String arg);
}
