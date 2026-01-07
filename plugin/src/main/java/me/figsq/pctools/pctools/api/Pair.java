package me.figsq.pctools.pctools.api;

import lombok.Data;
import lombok.Getter;

@Data
public class Pair<K, V> {
    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public Pair(){}
}
