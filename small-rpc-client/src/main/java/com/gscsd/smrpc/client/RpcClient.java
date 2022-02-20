package com.gscsd.smrpc.client;

import com.gscsd.smrpc.codec.Decoder;
import com.gscsd.smrpc.codec.Encoder;
import com.gscsd.smrpc.common.utils.ReflectionUtils;

import java.lang.reflect.Proxy;

public class RpcClient {

    private RpcClientConfig rpcClientConfig;
    private Encoder encoder;
    private Decoder decoder;
    private TransportSelector transportSelector;

    public RpcClient() {
        this(new RpcClientConfig());
    }

    public RpcClient(RpcClientConfig rpcClientConfig) {
        this.rpcClientConfig = rpcClientConfig;
        this.encoder = ReflectionUtils.newInstance(this.rpcClientConfig.getEncoderClass());
        this.decoder = ReflectionUtils.newInstance(this.rpcClientConfig.getDecoderClass());
        this.transportSelector = ReflectionUtils.newInstance(this.rpcClientConfig.getSelectorClass());

        this.transportSelector.init(
                this.rpcClientConfig.getServers(),//这里目前是写死的 如果添加注册中心就是加在这个地方
                this.rpcClientConfig.getConnectCount(),
                this.rpcClientConfig.getTransportCLass()
        );
    }

    public <T> T getProxy(Class<T> clazz){
        return (T)Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{clazz},
                new RemoteInvoker(clazz,encoder,decoder,transportSelector)
        );
    }
}
