package com.alibaba.china.cntools.vtag;

import com.alibaba.china.cntools.vtag.base.Lookup;

/**
 * Tag is a piece of velocity script which has the privilege to access beanfactory directly.
 * Tag is designed to wrap TOP LEVEL, RAW data (from DO or Domain objects) into human-readable,
 * human-understandable and human-usable strings, such as:
 * <ul>
 * <li>url of the member's homepage</li>
 * <li>phone number of the user</li>
 * <li>convert enums into readable strings</li>
 * <li>group objects by meaningful properties</li>
 * </ul>
 * Tag scripts can access all the pull tools as well as the following objects:
 * <ul>
 * <li>$this - the target object wrapped by the tag</li>
 * <li>$beans - the bean factory!</li>
 * <li>$result - to pass back the execution result</li>
 * <li>all the tools in the pull context</li>
 * </ul>
 * Sample tags definition:
 *
 * @author dafeng.chendf
 */

public class VTag implements Lookup<Object> {

    private final VTag parent;
    private final String name;
    private final String type;
    private final Object target;

    public VTag(VTag parent, String name, String type, Object target) {
        this.parent = parent;
        this.name = name;
        this.type = type;
        this.target = target;
    }

    @Override
    public Object get(String id) {
        VTag tag = this;
        while (tag.parent != null) {
            tag = tag.parent;
        }
        return ((VTagContext)(tag.target)).evaluate(this, id);
    }

    public VTag parent() {
        return parent;
    }

    public String name() {
        return name;
    }

    public String type() {
        return type;
    }

    public Object target() {
        return target;
    }

    public String fullname() {
        StringBuilder sb = new StringBuilder(name);
        for (VTag p = parent; p != null; p = p.parent) {
            sb.insert(0, ".").insert(0, p.name);
        }
        return sb.toString();
    }

    public String toString() {
        return fullname() + "[" + target + "]";
    }
}
