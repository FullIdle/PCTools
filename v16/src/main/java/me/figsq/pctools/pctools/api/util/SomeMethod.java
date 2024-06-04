package me.figsq.pctools.pctools.api.util;

import me.figsq.pctools.pctools.api.ISearchProperty;

public class SomeMethod {
    /**
     * 注册/添加搜索条目
     */
    public static void addSearchProperty(String searchProperty_name, ISearchProperty searchProperty){
        PokeUtil.searchProperties.put(searchProperty_name,searchProperty);
    }
}
