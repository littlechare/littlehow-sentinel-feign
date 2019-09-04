package com.littlehow.tool.sentinel.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Pair<K, V> {
    private K k;

    private V v;
}
