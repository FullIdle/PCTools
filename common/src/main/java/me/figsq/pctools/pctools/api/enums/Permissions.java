package me.figsq.pctools.pctools.api.enums;

import me.figsq.pctools.pctools.api.Cache;

public enum Permissions{
    RELOAD, OPEN, OPENOTHER, SEARCH;
    public String getPermission(){
        return Cache.plugin.getDescription().getName().toLowerCase()+".cmd."+this.name().toLowerCase();
    }
}
