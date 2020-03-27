package com.trs.netInsight.util;

import org.springframework.core.env.Environment;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;

/**
 * Created by yangyanyan on 2018/9/18.
 */
public class PictureUtil {


    /**
     * 机构或分组 上传logo
     * @param filePic  要上传的logo图片
     * @param name  机构或者分组名称
     * @param type  机构org 或 分组group
     * @return
     */
    public static String transferLogo(MultipartFile filePic, String name, String type) throws IOException {
        if (ObjectUtil.isEmpty(filePic)){
            return null;
        }
        String originalFileName = null;

        byte[] bytes2 = filePic.getOriginalFilename().getBytes("ISO8859-1");
        originalFileName = URLDecoder.decode(new String(bytes2, "UTF-8"), "UTF-8");

        int lastIndexOf = originalFileName.lastIndexOf(".");
        String extendName = ".jpg";
        if (lastIndexOf != -1) {
            extendName = originalFileName.substring(lastIndexOf, originalFileName.length()).toLowerCase();
        }
        // 文件扩展名
        String fileName = name + "_" + System.currentTimeMillis() + extendName;

        // 上传图片
        Environment env = SpringUtil.getBean(Environment.class);
        String logoPicPath = env.getProperty("logo.picture.path");
        if (StringUtil.isNotEmpty(type)){
            logoPicPath = logoPicPath + "/" +type;
        }

        File file = new File(logoPicPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        filePic.transferTo(new File(logoPicPath, fileName));

        return fileName;
    }

    /**
     * 获取 logo图片
     * @param picName
     * @param type
     */
    public static File getLogoPic(String picName,String type){
        Environment env = SpringUtil.getBean(Environment.class);
        String logoPicPath = env.getProperty("logo.picture.path");
        if (StringUtil.isNotEmpty(type) && !"wangcha.png".equals(picName)){
            logoPicPath = logoPicPath + "/" +type;
        }/* else if ("wangcha.png".equals(picName)){
            logoPicPath = logoPicPath ;
        }*/
        return new File(logoPicPath, picName);
    }

    /**
     * 删除图片
     * @param picName
     * @param type
     */
    public static void deletePic(String picName,String type){
        // 上传图片
        Environment env = SpringUtil.getBean(Environment.class);
        String logoPicPath = env.getProperty("logo.picture.path");
        if (StringUtil.isNotEmpty(type)){
            logoPicPath = logoPicPath + "/" +type;
        }
        // 删除图片
        File file = new File(logoPicPath + "\\\\" + picName);
        if (file.exists()){
            file.delete();
        }
    }
}
