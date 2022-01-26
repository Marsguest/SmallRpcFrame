package com.gscsd.smrpc.server;

import com.gscsd.smrpc.Request;
import com.gscsd.smrpc.ServiceDescriptor;
import com.gscsd.smrpc.common.utils.ReflectionUtils;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class ServiceManagerTest {

    ServiceManager sm;

    @Before
    public void init(){
        sm = new ServiceManager();
        TestClass t = new TestClass();
        sm.register(TestInterface.class,t);
    }

    @Test
    public void register() {
        TestClass t = new TestClass();
        sm.register(TestInterface.class,t);


    }

    @Test
    public void lookup() {

        Method[] methods = ReflectionUtils.getPublicMethods(TestInterface.class);
        ServiceDescriptor sdp = ServiceDescriptor.from(TestInterface.class,methods[0]);


        Request request = new Request();
        request.setService(sdp);

        ServiceInstance sis =  sm.lookup(request);
        assertNotNull(sis);

    }
}