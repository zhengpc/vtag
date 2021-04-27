package com.alibaba.china.cntools.vtag.impl;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.china.cntools.vtag.VTagExecutor;
import com.alibaba.china.cntools.vtag.base.Lookup;

import com.google.common.io.CharStreams;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.InternalContextAdapterImpl;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.VelPropertyGet;
import org.springframework.beans.factory.InitializingBean;

public class VelocityVTagExecutor implements VTagExecutor, InitializingBean {

    private Map<String, SimpleNode> cache = new ConcurrentHashMap<>();
    private Properties velocityProperties;
    private RuntimeInstance ri = new RuntimeInstance();

    private static final Writer nop = CharStreams.nullWriter();

    @Override
    public Object evaluate(final Lookup<?> ctx, String name, String script) throws Exception {
        InternalContextAdapterImpl ica = new InternalContextAdapterImpl(new VelocityContext() {

            @Override
            public boolean internalContainsKey(Object key) {
                boolean b = super.internalContainsKey(key);
                return b ? true : ctx.get((String)key) != null;
            }

            @Override
            public Object internalGet(String key) {
                Object obj = super.internalGet(key);
                return obj != null ? obj : ctx.get(key);
            }

        });

        ica.pushCurrentTemplateName(name);

        SimpleNode nodeTree = cache.get(script);
        if (nodeTree == null) {
            nodeTree = ri.parse(new StringReader(script), name);
            nodeTree.init(ica, ri);
            cache.put(script, nodeTree);
        }

        nodeTree.render(ica, nop);
        return ica.get("result");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ri.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new NullLogChute());
        if (velocityProperties != null) {
            ri.init(velocityProperties);
        }
    }

    public void setVelocityProperties(Properties velocityProperties) {
        this.velocityProperties = velocityProperties;
    }

    @Override
    public Object evaluate(Object target, String name) throws Exception {
        Info info = new Info(target.getClass().getName() + "." + name, 0, 0);
        VelPropertyGet get = ri.getUberspect().getPropertyGet(target, name, info);
        return get == null ? null : get.invoke(target);
    }
}
