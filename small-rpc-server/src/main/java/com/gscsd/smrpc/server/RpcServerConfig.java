package com.gscsd.smrpc.server;

import com.gscsd.smrpc.codec.Decoder;
import com.gscsd.smrpc.codec.Encoder;
import com.gscsd.smrpc.codec.JSONDecoder;
import com.gscsd.smrpc.codec.JSONEncoder;
import com.gscsd.smrpc.transport.HTTPTransportServer;
import com.gscsd.smrpc.transport.TransportServer;
import lombok.Data;

/**
 * Server配置
 */
@Data
public class RpcServerConfig {

    private Class<? extends TransportServer> transportClass = HTTPTransportServer.class;

    private Class<? extends Encoder> encoderClass = JSONEncoder.class;

    private Class<? extends Decoder> decoderClass = JSONDecoder.class;

    private int port = 3000;

}
