package com.trs.netInsight.support.appApi.controller;

import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.appApi.entity.ApkInfo;
import com.trs.netInsight.support.appApi.service.IApkInfoService;
import com.trs.netInsight.support.appApi.utils.ReadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Properties;

@Slf4j
@RestController
@RequestMapping("/apk")
public class ApkHandleController {
    @Value("${apk.save.url}")
    private String apkSaveUrl;

    @Autowired
    private IApkInfoService apkInfoService;

    @ResponseBody
    @FormatResult
    @PostMapping("/upLoad")
    public Object upLoad(
            @RequestParam("file") MultipartFile file,
            @RequestParam("isforce") int isforce,
            @RequestParam("description") String description){
        if (file.isEmpty()) {
            return "上传失败，请选择文件";
        }
        try{
            String filePath = apkSaveUrl;
            String fileName = file.getOriginalFilename();
            File dir = new File(filePath);
            if (!dir.exists()){
                dir.mkdirs();
            }
            File apk = new File(filePath+fileName);
            if(!apk.exists()){
                apk.createNewFile();
            }
            //获取输入流
            InputStream in = file.getInputStream();
            //输出流
            FileOutputStream out = new FileOutputStream(apk);
            //创建缓冲区
            byte buffer[] = new byte[1024];
            int len = 0;
            while ((len = in.read(buffer))>0){
                out.write(buffer, 0, len);
            }

            log.debug("上传成功");
            in.close();
            out.close();
            Properties PROPERTIES = new Properties(System.getProperties());
            String sep = PROPERTIES.getProperty("file.separator");
            HashMap map = (HashMap)ReadUtil.readAPK(filePath+fileName);
            String version = (String) map.get("versionName");
            int versionCode = Integer.valueOf((String) map.get("versionCode"));
            String bakFilePath = apkSaveUrl+versionCode+sep;
            String bakFileName = file.getOriginalFilename();
            File bakDir = new File(bakFilePath);
            if (!bakDir.exists()){
                bakDir.mkdirs();
            }
            File bakApk = new File(bakFilePath+bakFileName);
            if(!bakApk.exists()){
                bakApk.createNewFile();
            }
            //获取输入流
            InputStream bakIn = new FileInputStream(filePath+fileName);
            //输出流
            FileOutputStream bakOut = new FileOutputStream(bakApk);
            //创建缓冲区
            byte bakBuffer[] = new byte[1024];
            int bakLen = 0;
            while ((bakLen = bakIn.read(bakBuffer))>0){
                bakOut.write(bakBuffer, 0, bakLen);
            }

            log.debug("备份成功");
            bakIn.close();
            bakOut.close();
            apkInfoService.saveApkInfo(version,versionCode,isforce,filePath,fileName,description);

        }catch (IOException e){
            e.printStackTrace();
        }
        return "上传成功！";
    }

    /**
     * apk下载接口
     * @param res
     */
    @RequestMapping(value = "/appDownload", method = RequestMethod.GET)
    public void appDownload(HttpServletResponse res) {
        ApkInfo info = apkInfoService.getLatestVesion();
        String fileName = info.getFilename();
        res.setHeader("content-type", "application/octet-stream");
        res.setContentType("application/octet-stream");
        res.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        byte[] buff = new byte[1024];
        BufferedInputStream bis = null;
        OutputStream os = null;
        try {
            os = res.getOutputStream();
            File file = new File(info.getFilepath()+info.getFilename());
            res.setHeader("content-length",file.length()+"");
            bis = new BufferedInputStream(new FileInputStream(file));
            int i = 0;
            while ((i=bis.read(buff)) != -1) {
                os.write(buff, 0, i);
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        log.debug("下载成功！");
    }

    /**
     * apk最新版本检查
     * duhq 2019/05/10
     * @return map
     */
    @FormatResult
    @GetMapping("/isLatest")
    public Object isLatest(@RequestParam(value = "version") int version,
                           HttpServletRequest request
    ) {
        return apkInfoService.isLatest(version, request);
    }
}
