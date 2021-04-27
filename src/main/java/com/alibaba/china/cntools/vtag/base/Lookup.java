package com.alibaba.china.cntools.vtag.base;

/**
 * 类Lookup.java的实现描述：查找接口定义
 * 
 * @author dafeng.chendf 2010-08-08 下午5:01:21
 */
public interface Lookup<T> {

    T get(String name);

}
