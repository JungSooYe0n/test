package com.trs.netInsight.widget.report.service;

import org.springframework.web.multipart.MultipartFile;

import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;

/**
 * 图片文件服务类
 *
 * Created xiaoying on 2017/12/13.
 */
public interface IImageService {

    /**
     * 上传图片
     *
     * @param fileImage 文件
     * @return 文件URL
     * @throws TRSException 异常
     */
    public String uploadImage(MultipartFile fileImage) throws TRSException ;

    /**
     * 上传文件至nginx
     *
     * @param filePath 文件路径
     */
    void uploadToNginx(String filePath) throws OperationException ;

    /**
     * 验证上传文件是否合格
     *
     * @param extendedName 扩展名
     * @param size         文件大小
     * @throws TRSException 415不支持请求项目格式
     */
    void assertEligible(String extendedName, int size) throws TRSException ;
}
