package com.alibaba.china.cntools.vtag.parser;

import java.util.ArrayList;
import java.util.List;

public class VTagType extends Locatable {

    private String name;
    private String classes = "java.lang.Object";
    private List<VTag> tags = new ArrayList<>();
    private List<Class<?>> classList = new ArrayList<>();

    public List<Class<?>> getClassList() {
        return classList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public String getClasses() {
        return classes;
    }

    public void setClasses(String classes) {
        this.classes = classes;
    }

    public List<VTag> getTags() {
        return tags;
    }

    public void addTag(VTag tag) {
        this.tags.add(tag);
    }

    public void addClass(Class<?> clz) {
        classList.add(clz);
    }
}
