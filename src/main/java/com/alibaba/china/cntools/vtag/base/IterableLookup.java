package com.alibaba.china.cntools.vtag.base;

/**
 * 类IterableLookup.java的实现描述：迭代查找接口
 * 
 * @author dafeng.chendf 2010-08-08 下午5:01:21
 */
public interface IterableLookup<T> extends Lookup<T> {

    Iterable<String> keySet();

}
