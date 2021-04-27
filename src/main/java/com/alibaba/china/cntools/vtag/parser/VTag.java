package com.alibaba.china.cntools.vtag.parser;


public class VTag extends Locatable {

    public static final String ARRAY = "[]";
    private String name;
    private String type = "object";
    private String description;
    private String script;
    private String ref;
    private byte   dimensions = 0;

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name.trim();
    }
    
    public byte getDimensions() {
        return dimensions;
    }

    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        type = type.replaceAll(" ", "");
        while(type.endsWith(ARRAY)) {
            type = type.substring(0, type.length() - 2);
            dimensions++;
        }
        this.type = type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getScript() {
        return script;
    }
    
    public void setScript(String script) {
        this.script = script;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }
}
