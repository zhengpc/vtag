package com.alibaba.china.cntools.vtag.base;

/**
 * ��IterableLookup.java��ʵ���������������ҽӿ�
 * 
 * @author dafeng.chendf 2010-08-08 ����5:01:21
 */
public interface IterableLookup<T> extends Lookup<T> {

    Iterable<String> keySet();

}
