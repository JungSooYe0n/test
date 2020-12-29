package com.trs.netInsight.widget.thinkTank.controller;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.util.FileUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.thinkTank.entity.emnus.ThinkTankType;
import com.trs.netInsight.widget.thinkTank.service.IThinkTankDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;


/**
 * 舆情智库 控制层
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/9/24 11:05.
 * @desc
 */
@Slf4j
@RestController
@Api(description = "舆情智库控制层接口")
@RequestMapping(value = { "/thinkTank" })
public class ThinkTankDataController {
    @Autowired
    private IThinkTankDataService thinkTankDataService;

    @ApiOperation("上传pdf报告及相关信息")
    @FormatResult
    @PostMapping(value = "/uploadData")
    public Object uploadData(@ApiParam("pdf报告标题") @RequestParam(value = "reportTitle") String reportTitle,
                             @ApiParam("报告时间") @RequestParam(value = "reportTime") String reportTime,
                             @ApiParam("报告类型") @RequestParam(value = "reportType") String reportType,
                            // @ApiParam("上传pdf报告对应的图片") @RequestParam(value = "pdfPicture",required = false) MultipartFile pdfPicture,
                             @ApiParam("上传pdf报告文件") @RequestParam(value = "multipartFiles") MultipartFile[] multipartFiles) throws TRSException {
        ThinkTankType thinkTankType = ThinkTankType.valueOf(reportType);
        return thinkTankDataService.saveReportPdf(reportTitle,reportTime,multipartFiles,thinkTankType);
    }

    @ApiOperation("查询舆情智库报告信息")
    @FormatResult
    @GetMapping(value = "/pageList")
    public Object pageList(@ApiParam("页码") @RequestParam(value = "pageNo", required = false, defaultValue = "0") int pageNo,
                           @ApiParam("页长") @RequestParam(value = "pageSize", required = false, defaultValue = "20") int pageSize,
                           @ApiParam("类型") @RequestParam(value = "reportType", required = false, defaultValue = "ALL") String reportType){
        //为防止前端乱输入
        pageSize = pageSize>=1?pageSize:20;

        if (StringUtil.isNotEmpty(reportType) && !"ALL".equals(reportType)) {
            ThinkTankType thinkTankType = ThinkTankType.valueOf(reportType);
            return thinkTankDataService.findByReportType(pageNo,pageSize,thinkTankType);
        }
        return thinkTankDataService.findByPdfNameNot(pageNo,pageSize,"");
    }
    @ApiOperation("查询舆情智库报告信息条数")
    @FormatResult
    @GetMapping(value = "/totalSize")
    public Object totalSize(){
        //为防止前端乱输入
        HashMap<String,Integer> hashMap = new HashMap<>();
        hashMap.put("PoliticalEnergy",thinkTankDataService.getCountByReportType(ThinkTankType.valueOf("PoliticalEnergy")));
        hashMap.put("OpinionObservation",thinkTankDataService.getCountByReportType(ThinkTankType.valueOf("OpinionObservation")));
        hashMap.put("HotEventAnalysis",thinkTankDataService.getCountByReportType(ThinkTankType.valueOf("HotEventAnalysis")));
        hashMap.put("IndustrySpecialReport",thinkTankDataService.getCountByReportType(ThinkTankType.valueOf("IndustrySpecialReport")));
        hashMap.put("EpidemicSpecial",thinkTankDataService.getCountByReportType(ThinkTankType.valueOf("EpidemicSpecial")));
        hashMap.put("TwoSessionsSpecial",thinkTankDataService.getCountByReportType(ThinkTankType.valueOf("TwoSessionsSpecial")));
        hashMap.put("HotOpinionsAnalysis",thinkTankDataService.getCountByReportType(ThinkTankType.valueOf("HotOpinionsAnalysis")));
        return hashMap;
    }
    public static void main(String[] args) {

       // String pdfBinary = FileUtil.getPDFBinary("D:/netInsightWokeSpace/pdf/7月汽车行业大数据报告(1)_70ff686d-c464-4412-b280-93a60fb844c4.pdf");
        //pdfBinary = pdfBinary.replaceAll("\r|\n", "");
        //FileUtil.GenerateImage(pdfBinary,"D:/netInsightWokeSpace/pdf/test.jpg");

        //FileUtil.pdf2png("D:/netInsightWokeSpace/pdf","7月汽车行业大数据报告(1)_70ff686d-c464-4412-b280-93a60fb844c4","jpg");
        //FileUtil.pdf2multiImage("D:/netInsightWokeSpace/pdf/7月汽车行业大数据报告(1)_70ff686d-c464-4412-b280-93a60fb844c4.pdf","D:/netInsightWokeSpace/pdf/tetettetet.jpg");
        try {
            FileUtil.pdfToImage("D:/netInsightWokeSpace/pdf/张扣扣报杀母之仇连杀三人被判死刑事件舆情分析.pdf");
        } catch (IOException e) {
            System.err.println("结束："+e);
            e.printStackTrace();
        }
        System.err.println("结束");
    }
        //支持app首页详情
        String homeDetailsUrl(HttpServletRequest request){
        StringBuffer requestUrl = request.getRequestURL();
        String url = requestUrl.toString();
        int index = url.indexOf("/netInsight/");
        String mainUrl = url.substring(0,index);
        String Url = mainUrl+"/netInsight/apk/index.html";
        return Url;
    }
}
