package com.trs.netInsight.support.kafka.serializer;

import com.trs.netInsight.support.kafka.util.ObjectSerializerUtil;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class DecodeObject implements Deserializer {
    @Override
    public void configure(Map map, boolean b) {

    }

    @Override
    public Object deserialize(String s, byte[] bytes) {
        return ObjectSerializerUtil.byte2Obj(bytes);
    }

    @Override
    public void close() {

    }
}
