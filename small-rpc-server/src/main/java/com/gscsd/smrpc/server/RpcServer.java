package com.gscsd.smrpc.server;

import com.gscsd.smrpc.Request;
import com.gscsd.smrpc.Response;
import com.gscsd.smrpc.ServiceDescriptor;
import com.gscsd.smrpc.codec.Decoder;
import com.gscsd.smrpc.codec.Encoder;
import com.gscsd.smrpc.common.utils.ReflectionUtils;
import com.gscsd.smrpc.transport.RequestHandler;
import com.gscsd.smrpc.transport.TransportServer;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.sql.Ref;
@Slf4j
public class RpcServer {

    private RpcServerConfig config;
    private TransportServer net;
    private Encoder encoder;
    private Decoder decoder;
    private ServiceManager serviceManager;
    private ServiceInvoker serviceInvoker;

    private RequestHandler requestHandler = new RequestHandler() {
        public void onRequest(InputStream recive, OutputStream toResp) {

            Response resp = new Response();

            try {
                byte[] inBytes = IOUtils.readFully(recive,recive.available());
                Request request = decoder.decode(inBytes,Request.class);
                log.info("get request: {}",request);

                ServiceInstance sis = serviceManager.lookup(request);
                Object ret = serviceInvoker.invoke(sis,request);
                resp.setData(ret);



            } catch (Exception e) {
                log.warn(e.getMessage(),e);
                resp.setCode(1);
                resp.setMessage("RpcServer got error"
                        +e.getClass().getName()+" : "
                        +e.getMessage());
            }finally {
                try {
                    byte[] outBytes = encoder.encode(resp);
                    toResp.write(outBytes);
                    log.info("response client");
                } catch (IOException e) {
                    log.warn(e.getMessage(),e);
                }
            }
        }
    };

    public RpcServer() {
        this(new RpcServerConfig());
    }

    public RpcServer(RpcServerConfig config) {
        this.config = config;

        //net
        this.net = ReflectionUtils.newInstance(
                config.getTransportClass()
        );
        this.net.init(config.getPort(),requestHandler);

        //codec
        this.encoder = ReflectionUtils.newInstance(
                config.getEncoderClass()
        );

        this.decoder = ReflectionUtils.newInstance(
                config.getDecoderClass()
        );
        //service
        this.serviceManager = new ServiceManager();
        this.serviceInvoker = new ServiceInvoker();
    }

    public <T> void register(Class<T> interfaceClass,T bean){
        serviceManager.register(interfaceClass,bean);
    }

    public void start(){
        this.net.start();
    }

    public void stop(){
        this.net.stop();
    }

}
