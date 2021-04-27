package com.alibaba.china.cntools.vtag.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.china.cntools.vtag.VTagDef;
import com.alibaba.china.cntools.vtag.base.TypeUtil;
import com.alibaba.china.cntools.vtag.base.VTagConfigException;

import org.apache.commons.digester3.Digester;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.log.NullLogChute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Tag definition is supposed to change manually and frequently. So we need to tolerate problems and avoid prevent the
 * whole system from starting.
 *
 * @author dafeng.chendf
 */
public class VTagDefBuilder implements FactoryBean {

    private static final Logger logger = LoggerFactory.getLogger(VTagDefBuilder.class);

    private static final RuntimeInstance ri = new RuntimeInstance();
    private static final Digester digester = new DigesterEx("vtag-def-1.0.dtd");

    private Map<String, VTagRoot> roots = new HashMap<>();
    private Set<String> types;
    private Map<String, VTagDef> result;
    private Map<String, Map<Class<?>, String>> scriptPool;
    private Resource[] tagDefLocations;
    private boolean throwWarnings = false;

    public VTagDefBuilder() {
        digester.addObjectCreate("tags", VTagRoot.class);
        digester.addObjectCreate("tags/type", VTagType.class);
        digester.addSetProperties("tags/type");
        digester.addSetNext("tags/type", "addType");

        digester.addObjectCreate("tags/type/tag", VTag.class);
        digester.addSetProperties("tags/type/tag");
        digester.addBeanPropertySetter("tags/type/tag", "script");
        digester.addSetNext("tags/type/tag", "addTag");

        digester.addObjectCreate("tags/tag", VTag.class);
        digester.addSetProperties("tags/tag");
        digester.addBeanPropertySetter("tags/tag", "script");
        digester.addSetNext("tags/tag", "addTag");
    }

    public VTagDefBuilder setThrowWarnings(boolean trowWarnings) {
        this.throwWarnings = trowWarnings;
        return this;
    }

    public void parse(Resource resource) {
        VTagRoot root = null;
        try {
            root = digester.parse(resource.getInputStream());
            root.setConfigFile(resource.getURL().toExternalForm());
            roots.put(root.getConfigFile(), root);
        } catch (IOException e) {
            throw new VTagConfigException(e);
        } catch (SAXParseException e) {
            logger.error("line " + e.getLineNumber() + " column " + e.getColumnNumber() + ": " + e.getMessage());
            throw new VTagConfigException(e);
        } catch (SAXException e) {
            throw new VTagConfigException(e);
        }
    }

    public Map<String, VTagDef> getTagDefs() {
        result = new HashMap<>();
        scriptPool = new HashMap<>();
        types = new HashSet<>();

        for (VTagRoot root : roots.values()) {
            for (Map.Entry<String, List<VTagType>> entry : root.getTypes().entrySet()) {
                for (VTagType type : entry.getValue()) {
                    processType(root, type);
                }
            }
        }

        for (VTagRoot root : roots.values()) {
            for (Map.Entry<String, List<VTagType>> entry : root.getTypes().entrySet()) {
                for (VTagType type : entry.getValue()) {
                    for (VTag tag : type.getTags()) {
                        processTypeTag(root, type, tag);
                    }
                }
            }
        }

        for (VTagRoot root : roots.values()) {
            for (VTag tag : root.getTags()) {
                processTag(root, tag);
            }
        }

        Configurable.checkWarnings(roots.values(), throwWarnings, logger);

        return result;
    }

    private void processType(VTagRoot root, VTagType type) {
        String name = type.getName();
        // sanity check
        if (StringUtils.isEmpty(name)) {
            root.warn(type.getLine(), "<type> has no name attribute");
            return;
        }
        if (TypeUtil.isPrimitive(name)) {
            root.warn(type.getLine(), "type '" + name + "' is a built-in name");
            return;
        }
        if (!name.matches("\\w+")) {
            root.warn(type.getLine(), "type name '" + name + "' should only contain letters");
            return;
        }
        if (type.getClasses() == null) {
            root.warn(type.getLine(), "type '" + name + "' has no classes attribute");
            return;
        }
        types.add(name);

        // get classes
        String[] clzAttr = type.getClasses().split(",");
        for (int i = 0; i < clzAttr.length; i++) {
            String clz = clzAttr[i].trim();
            if (!clz.isEmpty()) {
                try {
                    // 适配Spring4.3.5 type.addClass(ClassUtils.forName(clz))
                    type.addClass(ClassUtils.forName(clz, ClassUtils.getDefaultClassLoader()));
                } catch (Exception e) {
                    root.warn(type.getLine(), "class " + clz + " specified in type '" + name + "' not found");
                    continue;
                }
            }
        }
    }

    private boolean checkTag(VTagRoot root, VTag tag) {
        String name = tag.getName();
        String type = tag.getType();
        String ref = tag.getRef();
        String script = tag.getScript();
        int line = tag.getLine();
        boolean valid = true;

        if (StringUtils.isEmpty(name)) {
            root.warn(line, "<tag> has no name attribute");
            valid = false;
        }
        if (StringUtils.isEmpty(script) && StringUtils.isEmpty(ref)) {
            root.warn(line, "tag '" + name + "' has no script or ref attribute");
            valid = false;
        }
        if (!StringUtils.isEmpty(script) && !StringUtils.isEmpty(ref)) {
            root.warn(line, "tag '" + name + "' should not use script and ref attributes in the same time");
            valid = false;
        }
        if (!StringUtils.isEmpty(ref)) {
            tag.setScript("#set($result = " + ref + ")");
        }
        if (StringUtils.isEmpty(type)) {
            root.warn(line, "tag '" + name + "' has no type attribute");
            valid = false;
        }
        if (!TypeUtil.isPrimitive(type) && !types.contains(type)) {
            root.warn(line, "tag " + name + "'s type " + tag.getType() + " is undefined");
            valid = false;
        }
        try {
            ri.parse(new StringReader(script), "tag " + tag.getName());
        } catch (Exception e) {
            String s = ref != null ? "ref attribute" : "script";
            root.warn(line, "tag " + name + "'s" + s + " failed to parse\n" + e.toString());
            valid = false;
        }

        return valid;
    }

    private void processTag(VTagRoot root, VTag tag) {
        if (!checkTag(root, tag)) {
            return;
        }

        // create the tag
        String name = tag.getName();
        VTagDef def = result.get(name);
        if (def != null) {
            root.warn(tag.getLine(), "tag '" + name + "' is already defined in " + findTag(tag));
            return;
        }

        // set the script
        Map<Class<?>, String> scripts = new HashMap<Class<?>, String>(1);
        scripts.put(Object.class, tag.getScript());
        def = createVTagDef(root, tag, name, scripts);
        result.put(name, def);
    }

    private void processTypeTag(VTagRoot root, VTagType type, VTag tag) {
        if (!checkTag(root, tag)) {
            return;
        }

        // create the tag
        String name = type.getName() + "." + tag.getName();
        VTagDef def = result.get(name);
        Map<Class<?>, String> scripts = scriptPool.get(name);
        if (def == null) {
            scripts = new HashMap<Class<?>, String>();
            def = createVTagDef(root, tag, name, scripts);
            result.put(name, def);
            scriptPool.put(name, scripts);
        }

        // set the script
        for (Class<?> clz : type.getClassList()) {
            String script = scripts.get(clz);
            if (script != null && !script.equals(tag.getScript())) {
                root.warn(tag.getLine(), "tag '" + name + "' for " + clz + " is already defined in "
                    + findScript(tag, type.getName(), tag.getName(), clz, script));
                continue;
            }
            scripts.put(clz, tag.getScript());
        }
    }

    private VTagDef createVTagDef(VTagRoot root, VTag tag, String name, Map<Class<?>, String> scripts) {
        String type = tag.getType();
        String desc = tag.getDescription();
        byte dimensions = tag.getDimensions();
        return new VTagDef(name, type, dimensions, desc, scripts);
    }

    private String findTag(VTag me) {
        for (VTagRoot root : roots.values()) {
            for (VTag tag : root.getTags()) {
                if (tag == me) {
                    continue;
                }
                if (!tag.getName().equals(me.getName())) {
                    continue;
                }
                return root.getConfigFile() + " line " + tag.getLine();
            }
        }
        throw new RuntimeException("BUG: no duplicated tag found??!");
    }

    private String findScript(VTag me, String typeName, String tagName, Class<?> clz, String script) {
        for (VTagRoot root : roots.values()) {
            for (Map.Entry<String, List<VTagType>> entry : root.getTypes().entrySet()) {
                if (!entry.getKey().equals(typeName)) {
                    continue;
                }

                for (VTagType type : entry.getValue()) {
                    boolean typeMatches = false;
                    for (Class<?> typeClz : type.getClassList()) {
                        if (typeClz == clz) {
                            typeMatches = true;
                            break;
                        }
                    }

                    if (!typeMatches) {
                        continue;
                    }

                    for (VTag tag : type.getTags()) {
                        if (tag == me) {
                            continue;
                        }
                        if (!tag.getName().equals(tagName)) {
                            continue;
                        }
                        if (!tag.getScript().equals(script)) {
                            continue;
                        }

                        return root.getConfigFile() + " line " + tag.getLine();
                    }
                }
            }
        }
        throw new RuntimeException("BUG: no duplicated script found??!");
    }

    public void setTagDefLocations(Resource[] tagDefLocations) {
        this.tagDefLocations = tagDefLocations;
    }

    @Override
    public Map<String, VTagDef> getObject() throws Exception {
        if (!ri.isInitialized()) {
            // 设置空的log，避免使用velocity默认的veloicyt.log
            // 因为使用jetty后，对应的user.dir会被替换成$jetty.home，会导致没有velocity.log的创建权限，导致出问题
            // webx2 和 web3的velocityService宝宝都手工设置了log，避免使用默认的velocity日志处理
            ri.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new NullLogChute());
            ri.init();
        }
        for (Resource r : tagDefLocations) {
            parse(r);
        }

        return getTagDefs();
    }

    @Override
    public Class<?> getObjectType() {
        return null;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
