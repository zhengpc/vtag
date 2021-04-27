package com.alibaba.china.cntools.vtag.velocityx;

import java.util.Map;

import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.node.AbstractExecutor;
import org.apache.velocity.runtime.parser.node.MapGetExecutor;
import org.apache.velocity.util.introspection.Introspector;

/**
 * 自定义velocity MapGet解析器,相比于velocity自带的{@link MapGetExecutor}
 * 
 * <pre>
 * 主要的区别：判定是否是map的子类的实现方式不同
 * 1.velocity自带的默认是先获取class.getAllInterface()，然后对其接口类进行遍历，看看其中一个是否是Map，
 * 2.自定义的MapGet解析器则采用clazz.isAssignableFrom，调用native方法进行向上转型判断
 * </pre>
 * 
 * <strong>注意：需要在CustomUberspectImpl中进行引入</strong>
 * 
 * @author jianghang 2010-9-17 下午02:49:17
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
        if (clazz.isAssignableFrom(Map.class)) { // 直接调用native方法进行,Map接口判断
            try {
                if (property != null) {
                    // 通过introspector进行method cache，同时直接查找对象clazz实例的method方法
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
