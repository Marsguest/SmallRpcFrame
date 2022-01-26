package com.gscsd.smrpc.codec;

import org.junit.Test;

import static org.junit.Assert.*;

public class JSONDecoderTest {

    @Test
    public void decode() {

        Encoder encoder = new JSONEncoder();

        TestBean bena = new TestBean();
        bena.setId(10);
        bena.setName("sdfsd");

        byte[] bytes = encoder.encode(bena);


        Decoder decoder = new JSONDecoder();
        TestBean bean2 = decoder.decode(bytes,TestBean.class);

        assertEquals(bena.getName(),bean2.getName());
        assertEquals(bena.getId(),bean2.getId());
    }
}