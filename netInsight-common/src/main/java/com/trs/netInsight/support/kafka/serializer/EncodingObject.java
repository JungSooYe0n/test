package com.trs.netInsight.support.kafka.serializer;

import com.trs.netInsight.support.kafka.util.ObjectSerializerUtil;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class EncodingObject implements Serializer<Object> {
    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public byte[] serialize(String s, Object o) {
        return ObjectSerializerUtil.obj2Byte(o);
    }

    @Override
    public void close() {

    }
}
