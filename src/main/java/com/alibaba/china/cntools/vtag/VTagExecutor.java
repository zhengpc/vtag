package com.alibaba.china.cntools.vtag;

import com.alibaba.china.cntools.vtag.base.Lookup;

public interface VTagExecutor {

    Object evaluate(Lookup<?> ctx, String name, String script) throws Exception;
    
    Object evaluate(Object target, String name) throws Exception;
    
}
