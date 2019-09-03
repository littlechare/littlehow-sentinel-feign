package com.littlehow.tool.sentinel.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResourceContext {
    private static final Map<String, String> resources = new ConcurrentHashMap<>();

    public static void addResource(String resource) {
        resources.put(resource, resource);
    }

    public static boolean contains(String resource) {
        return resources.containsKey(resource);
    }

    public static List<String> getCurrentResources() {
        return new ArrayList<>(resources.keySet());
    }
}
