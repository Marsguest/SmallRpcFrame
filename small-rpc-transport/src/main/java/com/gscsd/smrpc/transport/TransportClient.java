package com.gscsd.smrpc.transport;

import com.gscsd.smrpc.Peer;

import java.io.InputStream;

/**
 * Client
 * 1.创建连接
 * 2.发送数据，并且等待相应
 * 3.关闭连接
 */
public interface TransportClient {
    void connect(Peer peer);

    InputStream write(InputStream data);

    void close();

}
