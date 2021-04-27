package com.alibaba.china.cntools.vtag;

import com.alibaba.china.cntools.vtag.base.Lookup;

public interface VTagContext extends Lookup<Object> {

    Object evaluate(VTag parent, String tag);

    Object evaluateScript(Object target, String name, String script);

}
