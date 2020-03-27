package com.trs.netInsight.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.env.Environment;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 汉语转拼音
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/3/6 10:21.
 * @desc
 */
public class PinyinUtil {
    public static Map<String, String> dictionary = new HashMap<String, String>();
    //static String filePath = "D:\\netInsightWokeSpace\\netInsight\\pinyin\\duoyinzi_pinyin.txt";
    //打包路径
    //static String filePath = "/home/netinsight/data/java/pinyin/duoyinzi_pinyin.txt";
    // 加载多音字词典
    static {
        BufferedReader br = null;
        try {
            Environment env = SpringUtil.getBean(Environment.class);
            String filePath = env.getProperty("pinyin.dictionary");
            File file = new File(filePath);
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] arr = line.split("#");
                if (StringUtils.isNotEmpty(arr[1])) {
                    String[] sems = arr[1].split(" ");
                    for (String sem : sems) {
                        if (StringUtils.isNotEmpty(sem)) {
                            dictionary.put(sem, arr[0]);
                        }
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String toPinyinWithPolyphone(String str){
        if (StringUtil.isNotEmpty(str)){
            char[] t1 = null;
            t1 = str.toCharArray();
            String[] t2 = new String[t1.length];
            HanyuPinyinOutputFormat t3 = new HanyuPinyinOutputFormat();
            t3.setCaseType(HanyuPinyinCaseType.LOWERCASE);// 小写格式
            t3.setToneType(HanyuPinyinToneType.WITHOUT_TONE);// 有无音标
            t3.setVCharType(HanyuPinyinVCharType.WITH_V);
            String t4 = "";
            try {
                for (int i = 0; i < t1.length; i++) {
                    // 判断是否为汉字字符
                    // if(t1[i] >= 32 && t1[i] <= 125)//ASCII码表范围内直接返回
                    if (String.valueOf(t1[i]).matches("[\\u4E00-\\u9FA5]+")) {
                        t2 = PinyinHelper.toHanyuPinyinStringArray(t1[i], t3);// 转化为拼音
                        //如果是单个汉字，不处理，随机分配拼音
                        if (i != t1.length - 1 && t1.length != 1) {
                            String dic = String.valueOf(t1[i]) + String.valueOf(t1[i + 1]);
                            for (String py : t2) {
                                if (py.equals(dictionary.get(dic)) || py.equals(dictionary.get(String.valueOf(t1[i])))) {
                                    t2[0] = py;// 把t2[0]作为存放正确发音的区域
                                    break;
                                }
                            }
                        } else if (t1.length != 1) {
                            String dic = String.valueOf(t1[i - 1]) + String.valueOf(t1[i]);
                            for (String py : t2) {
                                String s = dictionary.get(dic);
                               // if (py.equals(dictionary.get(dic)) || py.equals(dictionary.get(String.valueOf(t1[i])))) {
                                if (py.equals(dictionary.get(dic))) {
                                    t2[0] = py;// 把t2[0]作为存放正确发音的区域
                                    break;
                                }
                            }
                        }
                       // t4 += t2[0].substring(0, 1).toUpperCase() + t2[0].substring(1);// 首字母大写
                        t4 += t2[0];
                    } else {
                        if ("haha".equals(t1[i])){
                            t4 += String.valueOf("hothaha");
                        }else {
                            t4 += String.valueOf(t1[i]);// 不是汉字不处理
                        }
                    }
                }
            } catch (BadHanyuPinyinOutputFormatCombination e1) {
                e1.printStackTrace();
            }
            return t4;
        }

        return null;
    }
    /**
     * 将汉语转化为拼音（不解决多音字问题）
     * @param str
     * @return
     */
    public static String toPinyin(String str){
        String pinyinStr = "";
        char[] chars = str.toCharArray();
        HanyuPinyinOutputFormat hanyuPinyinOutputFormat = new HanyuPinyinOutputFormat();
        hanyuPinyinOutputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        hanyuPinyinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] > 128){

                try {
                    pinyinStr += PinyinHelper.toHanyuPinyinStringArray(chars[i],hanyuPinyinOutputFormat)[0];
                } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                    badHanyuPinyinOutputFormatCombination.printStackTrace();
                }


            }else {
                pinyinStr += chars[i];
            }

        }

        return pinyinStr;
    }


}
