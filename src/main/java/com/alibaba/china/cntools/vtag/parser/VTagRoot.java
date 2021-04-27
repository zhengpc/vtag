package com.alibaba.china.cntools.vtag.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VTagRoot extends Configurable {

    private Map<String, List<VTagType>> types = new HashMap<>();

    private List<VTag> tags = new ArrayList<>();

    public Map<String, List<VTagType>> getTypes() {
        return types;
    }

    public List<VTag> getTags() {
        return tags;
    }

    public void addType(VTagType type) {
        List<VTagType> list = types.get(type.getName());
        if (list == null) {
            list = new ArrayList<>();
            types.put(type.getName(), list);
        }
        list.add(type);
    }

    public void addTag(VTag tag) {
        tags.add(tag);
    }
}
