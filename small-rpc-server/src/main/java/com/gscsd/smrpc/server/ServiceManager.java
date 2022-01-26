package com.gscsd.smrpc.server;

import com.gscsd.smrpc.Request;
import com.gscsd.smrpc.ServiceDescriptor;
import com.gscsd.smrpc.common.utils.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

/**
 * 管理Rpc暴露的服务
 */
@Slf4j
public class ServiceManager {
    //服务的描述到对应的服务的具体实现的映射
    private Map<ServiceDescriptor,ServiceInstance> services;


    public ServiceManager(){
        this.services = new ConcurrentHashMap<ServiceDescriptor, ServiceInstance>();
    }

    //下面实现注册和查找

    /**
     *
     * @param interfaceClass 接口的类
     * @param bean 接口子类的一个对象
     */
    public <T> void register(Class<T> interfaceClass,T bean){
        Method[] methods = ReflectionUtils.getPublicMethods(interfaceClass);
        for (Method method:methods){
            ServiceInstance sis = new ServiceInstance(bean,method);
            ServiceDescriptor sdp = ServiceDescriptor.from(interfaceClass,method);

            services.put(sdp,sis);
            log.info("register service:{} {}",sdp.getClazz(),sdp.getMethod());

        }

    }

    /**
     * 查找
     * @return
     */
    public ServiceInstance lookup(Request request){
        ServiceDescriptor sdp = request.getService();
        return services.get(sdp);
    }




}


