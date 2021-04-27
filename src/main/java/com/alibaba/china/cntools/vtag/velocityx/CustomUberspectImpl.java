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
 * ģ��webx3�ж�velocity UberspectImpl��ʵ��,����ɼ�{@link CustomizedUberspectImpl}
 * 
 * <pre>
 * ͨ�����ã�runtime.introspector.uberspect�滻webx3Ĭ�ϵ�CustomizedUberspectImpl������
 * </pre>
 * 
 * Ĭ�ϵ�get property������˳��
 * <ul>
 * <li><code>getFoo()</code>��<code>getfoo()</code>��</li>
 * <li><code>isFoo()</code>��<code>isfoo()</code>��</li>
 * <li><code>Map.get(String) (�����Ż���MapGet����,�����޸ĵ�)</code>��</li>
 * <li><code>AnyType.get(String)</code>��</li>
 * </ul>
 * 
 * @author jianghang 2010-9-17 ����02:53:36
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
            // �����޸ĵ�,�����Զ����CustomMapGetExecutor
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
