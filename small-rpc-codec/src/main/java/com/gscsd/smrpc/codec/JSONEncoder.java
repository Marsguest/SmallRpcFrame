package com.gscsd.smrpc.codec;

import com.alibaba.fastjson.JSON;

/**
 * 基于json的序列化实现
 */
public class JSONEncoder implements Encoder {
    public byte[] encode(Object o) {
        return JSON.toJSONBytes(o);
    }
}
