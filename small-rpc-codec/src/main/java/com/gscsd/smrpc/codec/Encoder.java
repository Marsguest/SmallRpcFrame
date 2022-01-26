package com.gscsd.smrpc.codec;

/**
 * 序列化
 */
public interface Encoder {
    byte[] encode(Object o);
}
