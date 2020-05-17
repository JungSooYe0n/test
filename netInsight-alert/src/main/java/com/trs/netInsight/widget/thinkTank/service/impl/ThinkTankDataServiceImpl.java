package com.trs.netInsight.widget.thinkTank.service.impl;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.util.CodeUtils;
import com.trs.netInsight.util.FileUtil;
import com.trs.netInsight.widget.thinkTank.entity.ThinkTankData;
import com.trs.netInsight.widget.thinkTank.repository.ThinkTankDataRepository;
import com.trs.netInsight.widget.thinkTank.service.IThinkTankDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationHome;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 舆情智库业务层接口实现类
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/9/23 16:18.
 * @desc
 */
@Service
public class ThinkTankDataServiceImpl implements IThinkTankDataService {
    @Autowired
    private ThinkTankDataRepository thinkTankDataRepository;

    @Value("${pdf.file.path}")
    private String pdfPath;
    @Override
    public String saveReportPdf(String reportTitle, String reportTime, MultipartFile[] multipartFiles) throws TRSException {
        //上传文件
        String pdfName = "";
        String picName = "";
        String picDetailName = "";
        int picnum = 0;
        if (multipartFiles != null && multipartFiles.length > 0) {
            for (MultipartFile multipartFile : multipartFiles) {
                String originalFilename = multipartFile.getOriginalFilename();
                String[] split = originalFilename.split("\\.");

                String transferPath;
                String suffixName = split[split.length-1];
                String fileNameTemp = split[0]+"_"+UUID.randomUUID().toString() + ".";
                String fileName = fileNameTemp + suffixName;

                if ("pdf".equals(suffixName)){
                    pdfName = fileName;
                    transferPath = pdfPath + "/pdf";
                }else{
                    //针对疫情单独写上传图片(非pdf)  第二张图片分开写  第二张先传过来
                    if(picnum==0){
                        picDetailName = fileName;
                        transferPath = pdfPath + "/png";
                    }else{
                        picName = fileName;
                        transferPath = pdfPath + "/picture";
                    }
                }
                picnum++;

                //创建图片文件夹
                File filePathpng = new File(pdfPath + "/png");
                if (!filePathpng.exists()){
                    filePathpng.mkdirs();
                }

                File filePath = new File(transferPath);
                if (!filePath.exists()){
                    filePath.mkdirs();
                }

                try {
                    multipartFile.transferTo(new File(filePath, fileName));
                    //pdf转换为png
                    if ("pdf".equals(suffixName)){
                        FileUtil.pdfToImage(pdfPath+"/pdf/"+pdfName);
                    }

                } catch (IllegalStateException | IOException e) {
                   throw new TRSException(CodeUtils.FAIL,"上传文件失败！");
                }
            }
        }

        ThinkTankData thinkTankData = new ThinkTankData(reportTitle, picName, reportTime, pdfName,picDetailName);
        thinkTankDataRepository.save(thinkTankData);
        return "sussess";
    }

    @Override
    public Page<ThinkTankData> findAll(int pageNo,int pageSize) {

        //默认排序（创建日期降序排，即最新创建的在上面）
        Sort sort = new Sort(Sort.Direction.DESC, "createdTime");

        Pageable pageable = new PageRequest(pageNo, pageSize, sort);
        return thinkTankDataRepository.findAll(pageable);
    }
    @Override
    public Page<ThinkTankData> findByPdfNameNot(int pageNo,int pageSize, String pdfName) {

        //默认排序（创建日期降序排，即最新创建的在上面）
        Sort sort = new Sort(Sort.Direction.DESC, "createdTime");

        Pageable pageable = new PageRequest(pageNo, pageSize, sort);
        return thinkTankDataRepository.findByPdfNameNot(pdfName,pageable);
    }
    @Override
    public List<ThinkTankData> findByPicDetailNameNotAndPicDetailNameIsNotNull(int pageNo, int pageSize, String reportTitle) {

        //默认排序（创建日期降序排，即最新创建的在上面）
        Sort sort = new Sort(Sort.Direction.DESC, "createdTime");

        Pageable pageable = new PageRequest(pageNo, pageSize, sort);
        return thinkTankDataRepository.findByPicDetailNameNotAndPicDetailNameIsNotNull(reportTitle,pageable);
    }
    @Override
    public List<ThinkTankData> findByReportTitleLike(int pageNo, int pageSize, String reportTitle) {

        //默认排序（创建日期降序排，即最新创建的在上面）
        Sort sort = new Sort(Sort.Direction.DESC, "createdTime");

        Pageable pageable = new PageRequest(pageNo, pageSize, sort);
        return thinkTankDataRepository.findByReportTitleLike(reportTitle,pageable);
    }

    /**
     * 获取根目录+pathName路径
     *
     * @date Created at 2019年4月12日 下午2:35:55
     * @author 北京拓尔思信息技术股份有限公司
     * * @param pathName
     * @return
     */
    public static String getPath(String pathName) {
        ApplicationHome home = new ApplicationHome(ThinkTankDataServiceImpl.class);
        File jarFile = home.getSource();
        String path = jarFile.getParent();
        File file = new File(path, pathName);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }
}
