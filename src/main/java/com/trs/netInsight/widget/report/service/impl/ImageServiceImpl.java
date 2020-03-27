package com.trs.netInsight.widget.report.service.impl;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.template.GUIDGenerator;
import com.trs.netInsight.widget.report.service.IImageService;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;

/**
 * 图片文件服务类
 *
 * Created ChangXiaoyang on 2017/2/20.
 */
@Service
public class ImageServiceImpl implements IImageService{

    @Value("${smas.nginx.ip}")
    private String IP;
    @Value("${smas.nginx.uri}")
    private String NGINX_URI;
    @Value("${smas.nginx.username}")
    private String USERNAME;
    @Value("${smas.nginx.password}")
    private String PASSWORD;
    @Value("${smas.nginx.path}")
    private String PATH;

    //上传图片最大值
    private static final int MAX_IMAGE_SIZE = 1024 * 1024;
    //允许用户上传的图片扩展名
    private static final String[] EXTENDED_NAMES = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};

    private MultipartFile multipartFile;

    /**
     * 上传图片
     *
     * @param fileImage 文件
     * @return 文件URL
     * @throws TRSException 异常
     */
    public String uploadImage(MultipartFile fileImage) throws TRSException {
        multipartFile = fileImage;
        String fileName = multipartFile.getOriginalFilename(); //图片文件名
        String extendName = fileName.substring(fileName.lastIndexOf(".")).toLowerCase(); //扩展名
        int imageSize = (int) multipartFile.getSize(); //图片大小
        assertEligible(extendName, imageSize);

        String imageName = GUIDGenerator.generateName();
        String imageFile = "/home/" + imageName + extendName;
        uploadToNginx(imageFile);
        return NGINX_URI + imageName + extendName;
    }

    /**
     * 上传文件至nginx
     *
     * @param filePath 文件路径
     */
    public void uploadToNginx(String filePath) throws OperationException {
        try {
            File newFile = new File(filePath);
            InputStream inputStream = multipartFile.getInputStream();
            FileUtils.copyInputStreamToFile(inputStream, newFile);
            Connection conn = new Connection(IP);
            conn.connect();
            if (conn.authenticateWithPassword(this.USERNAME, this.PASSWORD)) {
                SCPClient spcClient = new SCPClient(conn);
                spcClient.put(filePath, PATH);
                conn.close();
            }
            newFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
            throw new OperationException(String.format("上传[%s]文件至nginx失败, message: %s", filePath, e));
        }
    }

    /**
     * 验证上传文件是否合格
     *
     * @param extendedName 扩展名
     * @param size         文件大小
     * @throws TRSException 415不支持请求项目格式
     */
    public void assertEligible(String extendedName, int size) throws TRSException {
        if (size >= MAX_IMAGE_SIZE || Arrays.stream(EXTENDED_NAMES).noneMatch(em ->
                em.equals(extendedName)))
            throw new TRSException("格式不支持", 415);
    }
}
