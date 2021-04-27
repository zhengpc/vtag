package com.alibaba.china.cntools.vtag.impl;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.alibaba.china.cntools.vtag.VTag;
import com.alibaba.china.cntools.vtag.VTagContext;
import com.alibaba.china.cntools.vtag.VTagDef;
import com.alibaba.china.cntools.vtag.VTagExecutor;
import com.alibaba.china.cntools.vtag.base.Lookup;
import com.alibaba.china.cntools.vtag.base.TypeUtil;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

/**
 * Major access point for templates and tags, providing a few key objects:
 * <ul>
 * <li>$beans - access model beans</li>
 * <li>$this - target object</li>
 * </ul>
 *
 * @author dafeng.chendf
 */

public class VTagContextImpl implements Lookup<Object>, VTagContext {

    private static final Logger logger = LoggerFactory.getLogger(VTagContextImpl.class);
    private final VTagExecutor vTagExecutor;
    private final VTag tagRoot;
    private SortedMap<String, VTagDef> tagDefs;
    // [type.]tag:TypedTagDef
    private Lookup<?> beanRoot;
    private Lookup<?> parentContext;

    public VTagContextImpl(Map<String, VTagDef> tagDefinition, VTagExecutor vTagExecutor, ApplicationContext beanFactory) {
        this.tagDefs = new TreeMap<>(tagDefinition);
        this.vTagExecutor = vTagExecutor;
        Assert.notNull(tagDefinition, "tagDefinition should not be null");
        Assert.notNull(vTagExecutor, "vTagExecutor should not be null");

        this.tagRoot = new VTag(null, "$tags", null, this);
        this.beanRoot = (String name) -> beanFactory.getBean(name);
    }

    @Override
    public Object evaluate(VTag parent, String id) {
        parent = parent == null ? tagRoot : parent;
        return internalEvaluate(parent, id, new TagSession(id));
    }

    protected Object internalEvaluate(VTag parent, String id, TagSession tags) {
        Object target = parent.target();
        String key = target == this ? id : (parent.type() + "." + id);
        VTagDef def = tagDefs.get(key);

        // if no tag is defined for this id, we use inspection directly
        if (def == null) {
            if (target == this) { // let's don't recursion
                return null;
            }

            try {
                return vTagExecutor.evaluate(target, id);
            } catch (Exception e) {
                logger.error("Error executing tag " + parent.fullname() + "." + id, e);
                return null;
            }
        }

        String script = def.getScript(target);
        if (script == null) {
            logger.warn("tag '" + key + "' does not accept object of " + target.getClass() + ", return null");
            return null;
        }
        Object result;
        try {
            result = evaluateScript(parent.target(), id, script, tags);
        } catch (Exception e) {
            logger.error("Error executing tag " + parent.fullname() + "." + id, e);
            return null;
        }

        if (result == null) {
            return null;
        }

        String type = def.getType();
        if (TypeUtil.isPrimitive(type)) {
            // TODO - verify primitive type
            return result;
        }

        try {
            return wrapObject(parent, id, type, result, def.getDimensions());
        } catch (RuntimeException e) {
            logger.error("fail to wrap object " + result + " into tag '" + key + "'", e);
            return null;
        }
    }

    @Override
    public Object evaluateScript(Object target, String id, String script) {
        try {
            return evaluateScript(target, id, script, new TagSession(null));
        } catch (Exception e) {
            logger.error("Error evaluating script " + script, e);
            return null;
        }
    }

    protected Object evaluateScript(Object target, String id, String script, TagSession tags) throws Exception {
        Lookup<Object> ctx = (name) -> {
            if ("this".equals(name)) {
                return target;
            } else if ("tags".equals(name)) {
                return tags;
            } else if ("beans".equals(name)) {
                return beanRoot;
            } else {
                return parentContext == null ? null : parentContext.get(name);
            }
        };

        return vTagExecutor.evaluate(ctx, id, script);
    }

    protected Object wrapObject(VTag parent, String id, String type, Object obj, byte dimensions) {
        if (obj == null) {
            return null;
        }

        if (dimensions == 0) {
            return new VTag(parent, id, type, obj);
        }

        List<Object> list = new ArrayList<>();
        if (obj instanceof Collection<?>) {
            for (Object tmp : ((Collection<?>)obj)) {
                list.add(wrapObject(parent, id, type, tmp, (byte)(dimensions - 1)));
            }
        } else if (obj.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(obj); i++) {
                list.add(wrapObject(parent, id, type, Array.get(obj, i), (byte)(dimensions - 1)));
            }
        } else {
            logger.error("tag result of " + parent.fullname() + "." + id
                + " does not conform to its dimension: result class is " + obj.getClass().getName()
                + ", defined type is " + type + StringUtils.repeat("[]", dimensions));
        }
        return list;
    }

    @Override
    public Object get(String name) {
        return evaluate(tagRoot, name);
    }

    // keep track of the calling path to detect recursion
    protected class TagSession implements Lookup<Object> {

        private List<String> visiting = new ArrayList<String>();

        TagSession(String id) {
            visiting.add(id);
        }

        @Override
        public Object get(String id) {
            if (visiting.contains(id)) {
                throw new IllegalArgumentException("Recursive tag path: " + visiting + " -> " + id);
            }
            visiting.add(id);
            try {
                return internalEvaluate(tagRoot, id, this);
            } finally {
                visiting.remove(id);
            }
        }

        @Override
        public String toString() {
            return visiting.toString();
        }
    }

}
