package com.gscsd.smrpc.server;

import com.gscsd.smrpc.Request;
import com.gscsd.smrpc.common.utils.ReflectionUtils;

/**
 * 调用具体服务
 */
public class ServiceInvoker {

    public Object invoke(ServiceInstance sis,
                         Request req){
        return ReflectionUtils.invoke(
                sis.getTarget(),
                sis.getMethod(),
                req.getParameters());

    }
}
