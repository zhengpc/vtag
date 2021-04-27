package com.alibaba.china.cntools.vtag.impl;

import java.util.Map;

import com.alibaba.china.cntools.vtag.VTagContext;
import com.alibaba.china.cntools.vtag.VTagDef;
import com.alibaba.china.cntools.vtag.VTagExecutor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

public class VTagContextFactory implements ApplicationContextAware, InitializingBean, FactoryBean {

    private VTagExecutor vtagExecutor;
    private Map<String, VTagDef> tagDefinition;
    private volatile VTagContextImpl vTagContext;
    private ApplicationContext beanFactory;

    @Override
    public VTagContext getObject() throws Exception {
        if (vTagContext == null) {
            synchronized (this) {
                if (vTagContext == null) {
                    vTagContext = new VTagContextImpl(tagDefinition, vtagExecutor, beanFactory);
                }
            }
        }
        return vTagContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.beanFactory = applicationContext;
    }

    public void setVtagExecutor(VTagExecutor vtagExecutor) {
        this.vtagExecutor = vtagExecutor;
    }

    public void setTagDefinition(Map<String, VTagDef> tagDefinition) {
        this.tagDefinition = tagDefinition;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(tagDefinition, "tagDefinition should not be null");
        Assert.notNull(vtagExecutor, "tagExecutor should not be null");
    }

    @Override
    public Class<?> getObjectType() {
        return VTagContext.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
