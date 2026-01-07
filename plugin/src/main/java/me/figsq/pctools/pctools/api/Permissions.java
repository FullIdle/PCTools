package me.figsq.pctools.pctools.api;

public enum Permissions{
    RELOAD, OPEN, OPENOTHER, SEARCH;
    public String getPermission(){
        return Config.plugin.getDescription().getName().toLowerCase()+".cmd."+this.name().toLowerCase();
    }
}
