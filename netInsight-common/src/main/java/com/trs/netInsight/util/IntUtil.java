package com.trs.netInsight.util;

/**
 * @author 李可南
 * @version 1.0.0
 * @ClassName IntUtil.java
 * @Description TODO
 * @createTime 2021年01月13日 11:39:00
 */
public final class IntUtil {
    public static String toChinese(int in) {
        String[] s1 = { "零", "一", "二", "三", "四", "五", "六", "七", "八", "九" };
        String[] s2 = { "十", "百", "千", "万", "十", "百", "千", "亿", "十", "百", "千" };

        String result = "";
        String string = Integer.toString(in);
        int n = string.length();
        if(string.equals("10")){
            result = "十";
            return result;
        }
        if(string.equals("20")){
            result = "二十";
            return result;
        }
        if(string.equals("30")){
            result = "三十";
            return result;
        }
        if(string.equals("40")){
            result = "四十";
            return result;
        }
        if(string.equals("50")){
            result = "五十";
            return result;
        }
        if(string.equals("60")){
            result = "六十";
            return result;
        }
        if(string.equals("70")){
            result = "七十";
            return result;
        }
        if(string.equals("80")){
            result = "八十";
            return result;
        }
        if(string.equals("90")){
            result = "九十";
            return result;
        }
        if(string.equals("100")){
            result = "一百";
            return result;
        }
        if(string.equals("1000")){
            result = "一千";
            return result;
        }
        for (int i = 0; i < n; i++) {

            int num = string.charAt(i) - '0';

            if (i != n - 1 && num != 0) {
                result += s1[num] + s2[n - 2 - i];
                if(result.equals("一十")){
                    result="十";
                }
            } else {
                result += s1[num];
            }
            //System.out.println("  "+result);
        }

        //System.out.println("----------------");
        //System.out.println(result);
        return result;

    }

//    public static void main(String[] args) {
//        IntUtil intUtil = new IntUtil();
//        System.out.println(intUtil.toChinese(20));
//    }
}
