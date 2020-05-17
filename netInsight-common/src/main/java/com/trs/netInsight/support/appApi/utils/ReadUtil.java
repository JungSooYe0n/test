package com.trs.netInsight.support.appApi.utils;

import android.content.res.AXmlResourceParser;
import android.util.TypedValue;
import org.apache.commons.lang.StringUtils;
import org.xmlpull.v1.XmlPullParser;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
/**
 *
 * @author laijunfeng816
 *
 */
public final class ReadUtil {
    /**
     * 读取apk
     * @param apkUrl
     * @return
     */
    public static Map readAPK(String apkUrl){
        ZipFile zipFile;
        Map map = new HashMap();
        try {
            zipFile = new ZipFile(apkUrl);
            Enumeration enumeration = zipFile.entries();
            ZipEntry zipEntry = null;
            while (enumeration.hasMoreElements()) {
                zipEntry = (ZipEntry) enumeration.nextElement();
                if (zipEntry.isDirectory()) {

                } else {
                    if (StringUtils.isNotBlank(zipEntry.getName().toLowerCase())) {
                        AXmlResourceParser parser = new AXmlResourceParser();
                        parser.open(zipFile.getInputStream(zipEntry));
                        while (true) {
                            int type = parser.next();
                            if (type == XmlPullParser.END_DOCUMENT) {
                                break;
                            }
                            String name = parser.getName();
                            if(null != name && StringUtils.isNotBlank(name)){
                                for (int i = 0; i != parser.getAttributeCount(); i++) {
                                    String sss = getAttributeValue(parser, i);
                                    System.out.println("sss==="+sss);
                                    if ("versionName".equals(parser.getAttributeName(i))) {
                                        String versionName = getAttributeValue(parser, i);
                                        if(null == versionName){
                                            versionName ="" ;
                                        }
                                        map.put("versionName", versionName);
                                    } else if ("package".equals(parser.getAttributeName(i))) {
                                        String packageName = getAttributeValue(parser, i);
                                        if(null == packageName){
                                            packageName = "";
                                        }
                                        map.put("packageName", packageName);
                                    } else if("versionCode".equals(parser.getAttributeName(i))){
                                        String versionCode = getAttributeValue(parser, i);
                                        if(null == versionCode){
                                            versionCode = "";
                                        }
                                        map.put("versionCode", versionCode);
                                    }
                                }
                                break;
                            }
                        }
                    }
                    if(map != null){
                        return map;
                    }
                }
            }
            zipFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    private static String getAttributeValue(AXmlResourceParser parser,int index) {
        int type=parser.getAttributeValueType(index);
        int data=parser.getAttributeValueData(index);
        if (type==TypedValue.TYPE_STRING) {
            return parser.getAttributeValue(index);
        }
        if (type==TypedValue.TYPE_ATTRIBUTE) {
            return String.format("?%s%08X",getPackage(data),data);
        }
        if (type==TypedValue.TYPE_REFERENCE) {
            return String.format("@%s%08X",getPackage(data),data);
        }
        if (type==TypedValue.TYPE_FLOAT) {
            return String.valueOf(Float.intBitsToFloat(data));
        }
        if (type==TypedValue.TYPE_INT_HEX) {
            return String.format("0x%08X",data);
        }
        if (type==TypedValue.TYPE_INT_BOOLEAN) {
            return data!=0?"true":"false";
        }
        if (type==TypedValue.TYPE_DIMENSION) {
            return Float.toString(complexToFloat(data))+
                    DIMENSION_UNITS[data & TypedValue.COMPLEX_UNIT_MASK];
        }
        if (type==TypedValue.TYPE_FRACTION) {
            return Float.toString(complexToFloat(data))+
                    FRACTION_UNITS[data & TypedValue.COMPLEX_UNIT_MASK];
        }
        if (type>=TypedValue.TYPE_FIRST_COLOR_INT && type<=TypedValue.TYPE_LAST_COLOR_INT) {
            return String.format("#%08X",data);
        }
        if (type>=TypedValue.TYPE_FIRST_INT && type<=TypedValue.TYPE_LAST_INT) {
            return String.valueOf(data);
        }
        return String.format("<0x%X, type 0x%02X>",data,type);
    }

    private static String getPackage(int id) {
        if (id>>>24==1) {
            return "android:";
        }
        return "";
    }


    public static float complexToFloat(int complex) {
        return (float) (complex & 0xFFFFFF00) * RADIX_MULTS[(complex >> 4) & 3];
    }

    private static final float RADIX_MULTS[]={
            0.00390625F,3.051758E-005F,1.192093E-007F,4.656613E-010F
    };
    private static final String DIMENSION_UNITS[]={
            "px","dip","sp","pt","in","mm","",""
    };
    private static final String FRACTION_UNITS[]={
            "%","%p","","","","","",""
    };
}
