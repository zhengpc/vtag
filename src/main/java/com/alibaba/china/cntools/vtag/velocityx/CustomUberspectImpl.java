/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.alibaba.china.cntools.vtag.velocityx;

import org.apache.velocity.runtime.parser.node.AbstractExecutor;
import org.apache.velocity.runtime.parser.node.BooleanPropertyExecutor;
import org.apache.velocity.runtime.parser.node.GetExecutor;
import org.apache.velocity.runtime.parser.node.PropertyExecutor;
import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.UberspectImpl;
import org.apache.velocity.util.introspection.VelPropertyGet;

/**
 * 模范webx3中对velocity UberspectImpl的实现,具体可见{@link CustomizedUberspectImpl}
 * 
 * <pre>
 * 通过设置：runtime.introspector.uberspect替换webx3默认的CustomizedUberspectImpl处理类
 * </pre>
 * 
 * 默认的get property方法的顺序：
 * <ul>
 * <li><code>getFoo()</code>或<code>getfoo()</code>。</li>
 * <li><code>isFoo()</code>或<code>isfoo()</code>。</li>
 * <li><code>Map.get(String) (特殊优化了MapGet处理,本次修改点)</code>。</li>
 * <li><code>AnyType.get(String)</code>。</li>
 * </ul>
 * 
 * @author jianghang 2010-9-17 下午02:53:36
 */
public class CustomUberspectImpl extends UberspectImpl {

    @Override
    public VelPropertyGet getPropertyGet(Object obj, String identifier, Info i) throws Exception {
        if (obj == null) {
            return null;
        }

        Class<?> claz = obj.getClass();

        /*
         * first try for a getFoo() type of property (also getfoo() )
         */
        AbstractExecutor executor = new PropertyExecutor(log, introspector, claz, identifier);

        /*
         * if that didn't work, look for boolean isFoo()
         */
        if (!executor.isAlive()) {
            executor = new BooleanPropertyExecutor(log, introspector, claz, identifier);
        }

        /*
         * Let's see if we are a map...
         */
        if (!executor.isAlive()) {
            // 本次修改点,引入自定义的CustomMapGetExecutor
            executor = new CustomMapGetExecutor(log, introspector, claz, identifier);
        }

        /*
         * finally, look for get("foo")
         */
        if (!executor.isAlive()) {
            executor = new GetExecutor(log, introspector, claz, identifier);
        }

        return executor.isAlive() ? new VelGetterImpl(executor) : null;
    }
}
