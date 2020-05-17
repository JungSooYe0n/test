package com.trs.netInsight.widget.thinkTank.service;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.widget.thinkTank.entity.ThinkTankData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

/**
 * 舆情智库业务层接口
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/9/23 15:53.
 * @desc
 */
public interface IThinkTankDataService {

    /**
     * 添加pdf报告
     * @param reportTitle   报告标题
     * @param reportTime    报告时间
     * @param multipartFiles  pdf报告和对应小图片
     * @return
     * @throws TRSException
     */
    public String saveReportPdf(String reportTitle, String reportTime, MultipartFile[] multipartFiles) throws TRSException;
    /**
     * 分页查询
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Page<ThinkTankData> findAll(int pageNo,int pageSize);
    Page<ThinkTankData> findByPdfNameNot(int pageNo,int pageSize,String pdfName);

    List<ThinkTankData> findByPicDetailNameNotAndPicDetailNameIsNotNull(int pageNo,int pageSize,String reportTitle);


    List<ThinkTankData> findByReportTitleLike(int pageNo,int pageSize,String reportTitle);

}
