package com.alibaba.china.cntools.vtag.velocityx;

import java.util.Map;

import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.node.AbstractExecutor;
import org.apache.velocity.runtime.parser.node.MapGetExecutor;
import org.apache.velocity.util.introspection.Introspector;

/**
 * �Զ���velocity MapGet������,�����velocity�Դ���{@link MapGetExecutor}
 * 
 * <pre>
 * ��Ҫ�������ж��Ƿ���map�������ʵ�ַ�ʽ��ͬ
 * 1.velocity�Դ���Ĭ�����Ȼ�ȡclass.getAllInterface()��Ȼ�����ӿ�����б�������������һ���Ƿ���Map��
 * 2.�Զ����MapGet�����������clazz.isAssignableFrom������native������������ת���ж�
 * </pre>
 * 
 * <strong>ע�⣺��Ҫ��CustomUberspectImpl�н�������</strong>
 * 
 * @author jianghang 2010-9-17 ����02:49:17
 */
public class CustomMapGetExecutor extends AbstractExecutor {

    private final Introspector introspector;
    private final String       property;

    public CustomMapGetExecutor(final Log log, Introspector introspector, final Class<?> clazz, final String property){
        this.log = log;
        this.introspector = introspector;
        this.property = property;
        discover(clazz);
    }

    protected void discover(final Class<?> clazz) {
        if (clazz.isAssignableFrom(Map.class)) { // ֱ�ӵ���native��������,Map�ӿ��ж�
            try {
                if (property != null) {
                    // ͨ��introspector����method cache��ͬʱֱ�Ӳ��Ҷ���clazzʵ����method����
                    setMethod(introspector.getMethod(clazz, "get", new Class[] { Object.class }));
                }
            }
            /**
             * pass through application level runtime exceptions
             */
            catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                String msg = "Exception while looking for get('" + property + "') method";
                log.error(msg, e);
                throw new VelocityException(msg, e);
            }
        }
    }

    public Object execute(final Object o) {
        return ((Map<?, ?>) o).get(property);
    }

}
