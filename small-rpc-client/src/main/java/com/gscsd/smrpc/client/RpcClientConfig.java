package com.gscsd.smrpc.client;

import com.gscsd.smrpc.Peer;
import com.gscsd.smrpc.codec.Decoder;
import com.gscsd.smrpc.codec.Encoder;
import com.gscsd.smrpc.codec.JSONDecoder;
import com.gscsd.smrpc.codec.JSONEncoder;
import com.gscsd.smrpc.transport.HTTPTransportClient;
import com.gscsd.smrpc.transport.TransportClient;
import lombok.Data;

import java.util.Arrays;
import java.util.List;
@Data
public class RpcClientConfig {

    private Class<? extends TransportClient> transportCLass =
            HTTPTransportClient.class;

    private Class<? extends Encoder> encoderClass = JSONEncoder.class;
    private Class<? extends Decoder> decoderClass = JSONDecoder.class;
    private Class<? extends TransportSelector> selectorClass = RandomTransportSelector.class;

    private int connectCount = 1;

    private List<Peer> servers = Arrays.asList(
            new Peer("127.0.0.1",3000)
    );

}
