package com.alibaba.china.cntools.vtag;

import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Within a type, there are three built-in objects:
 * <ul>
 * <li>$ctx - to access to Context object</li>
 * <li>$this - to access the target object wrapped by the tag</li>
 * <li>$result - to pass back the execution result</li>
 * </ul>
 * Sample tags definition:
 *
 * <pre>
 *   <type name="offer" class="com.alibaba.OfferDO, com.alibaba.OfferModel">
 *     <tag type="string" name="name" description="">#set($result = $this.name)</tag>
 *     <tag type="number" name="price" description="">#set($result = $this.price * $ctx.model.discount)</tag>
 *     <tag type="offer" name="offer" description="">#set($result = $this)</tag>
 *   </type>
 *   <tag type="string" name="keyword" description="">
 *     #set($result = 'keyword:' + $ctx.model.offer.name.trim())
 *   </tag>
 *   <tag type="offer" name="offer" description="">#set($result = $ctx.model.offer)</tag>
 *   <tag type="offer[]" name="relatedOffers" description="">#set($result = $ctx.model.relatedOffers)</tag>
 * </pre>
 *
 * @author dafeng.chendf
 */
public class VTagDef {

    private static Logger logger = LoggerFactory.getLogger(VTagDef.class);
    private final String type;
    private final byte dimensions;
    private final String name;
    private final String description;
    /**
     * for top-level tags, class is Context
      */
    private final Map<Class<?>, String> scripts;

    public VTagDef(String name, String type, byte dimensions, String description, Map<Class<?>, String> scripts) {
        this.name = name;
        this.description = description;
        this.scripts = scripts;
        this.type = type;
        this.dimensions = dimensions;
    }

    public byte getDimensions() {
        return dimensions;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getScript(Object target) {
        Class<?> clz = null;
        String script = null;

        // make sure it's the expected type
        for (Map.Entry<Class<?>, String> entry : scripts.entrySet()) {
            if (entry.getKey().isInstance(target)) {
                if (script == null) {
                    clz = entry.getKey();
                    script = entry.getValue();
                } else if (!script.equals(entry.getValue())) {
                    logger.warn("ambiguous tag: object of " + target.getClass() + " is recognized " + "by " + name
                        + " for both " + clz + " and " + entry.getKey() + ", the former is used.");
                }
            }
        }

        if (script == null && logger.isDebugEnabled()) {
            logger.debug("object of " + target.getClass() + " is not recognized by tag " + name);
        }

        return script;
    }

    public String getType() {
        return type;
    }

    public String getClasses() {
        return scripts.keySet().toString();
    }

    public Map<Class<?>, String> getScripts() {
        return Collections.unmodifiableMap(scripts);
    }
}