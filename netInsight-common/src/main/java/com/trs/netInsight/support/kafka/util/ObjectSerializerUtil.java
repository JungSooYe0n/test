package com.trs.netInsight.support.kafka.util;

import java.io.*;

public class ObjectSerializerUtil {

    /**
     * 将Object对象转化成为字节数组
     * @param obj
     * @return
     */
    public static byte[] obj2Byte(Object obj){
        ByteArrayOutputStream byteArray = null;
        ObjectOutputStream outputStream = null;
        byte[] bytes = null;
        try {
            byteArray = new ByteArrayOutputStream();
            outputStream = new ObjectOutputStream(byteArray);
            outputStream.writeObject(obj);
            bytes = byteArray.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (byteArray != null) byteArray.close();
                if (outputStream != null) outputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return bytes;
    }

    /**
     * 从字节数组中获取对象
     * @param bytes
     * @return
     */
    public static Object byte2Obj(byte bytes[]){
        Object obj = null;
        ByteArrayInputStream bi = null;
        ObjectInputStream oi = null;
        try {
            bi = new ByteArrayInputStream(bytes);
            oi = new ObjectInputStream(bi);
            obj = oi.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (bi != null) bi.close();
                if (oi != null) oi.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }
}
