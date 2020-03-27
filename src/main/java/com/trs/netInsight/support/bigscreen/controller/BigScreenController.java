package com.trs.netInsight.support.bigscreen.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.bigscreen.service.IBigScreenService;
import com.trs.netInsight.widget.analysis.entity.BigScreenDistrictInfo;
import com.trs.netInsight.widget.analysis.service.IBigScreenDistrictInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 大屏 控制层
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/12/18.
 * @desc
 */
@Slf4j
@RestController
@RequestMapping("/bigScreen")
@Api(description = "大屏 接口")
public class BigScreenController {

    /**
     * 大屏地域信息json文件路径
     */
    @Value("${bigscreen.district.path}")
    private String districtPath;
    @Autowired
    private IBigScreenService bigScreenService;

    @Autowired
    private IBigScreenDistrictInfoService bigScreenDistrictInfoService;

    /**
     *  计算 今日、本周、本月数据总量
     * @return
     * @throws OperationException
     */
    @GetMapping(value = "/dataCount")
    @FormatResult
    //@EnableRedis(cacheMinutes=10)
    public Object dataCount(@ApiParam("搜索关键词") @RequestParam(value = "keyWords",required = false) String keyWords) throws OperationException {
       return bigScreenService.dataCount(keyWords);
    }

    /**
     * 数据类型对比
     * @return
     * @throws OperationException
     */
    @GetMapping(value = "/dataTypeAnalysis")
    @FormatResult
    //@EnableRedis(cacheMinutes=10)
    public Object dataTypeAnalysis(@ApiParam("搜索关键词") @RequestParam(value = "keyWords",required = false) String keyWords,
                                   @ApiParam("时间") @RequestParam(value = "timeRange",required = false,defaultValue = "7d") String timeRange,
                                   @ApiParam("数据来源类型") @RequestParam(value = "groupName",required = false) String groupName) throws OperationException {
        return bigScreenService.dataTypeAnalysis(keyWords,groupName,timeRange);
    }
    /**
     *  区域热力图
     * @return
     * @throws OperationException
     */
    @GetMapping(value = "/areaData")
    @FormatResult
    //@EnableRedis(cacheMinutes=10)
    public Object areaData(@ApiParam("时间") @RequestParam(value = "timeRange",required = false,defaultValue = "7d") String timeRange,
                           @ApiParam("搜索关键词") @RequestParam(value = "keyWords",required = false) String keyWords) throws OperationException {
        return bigScreenService.areaThermogram(keyWords,timeRange);
    }
    /**
     *  热点词云图
     * @return
     * @throws OperationException
     */
    @GetMapping(value = "/hotWordCloud")
    @FormatResult
    //@EnableRedis(cacheMinutes=10)
    public Object hotWordCloud(@ApiParam("时间") @RequestParam(value = "timeRange",required = false,defaultValue = "7d") String timeRange,
                               @ApiParam("搜索关键词") @RequestParam(value = "keyWords",required = false) String keyWords) throws OperationException {
        return bigScreenService.hotWordCloud(keyWords,timeRange);
    }

    /**
     *  行业声量对比图
     * @return
     * @throws OperationException
     */
    @GetMapping(value = "/industryVoice")
    @FormatResult
    //@EnableRedis(cacheMinutes=10)
    public Object industryVoice(@ApiParam("时间") @RequestParam(value = "timeRange",required = false,defaultValue = "0d") String timeRange,
                               @ApiParam("搜索关键词") @RequestParam(value = "keyWords",required = false) String keyWords) throws OperationException {
        return bigScreenService.industryVoice(keyWords,timeRange);
    }

    /**
     *  一周信息趋势图
     * @return
     * @throws OperationException
     */
    @GetMapping(value = "/messageOfWeek")
    @FormatResult
    //@EnableRedis(cacheMinutes=10)
    public Object messageOfWeek(@ApiParam("是否是第一次请求 0： 第一次   1： 不是第一次") @RequestParam(value = "flag",required = true,defaultValue = "0") String flag,
                                @ApiParam("搜索关键词") @RequestParam(value = "keyWords",required = false) String keyWords) throws OperationException {
        return bigScreenService.messageOfWeek(keyWords,flag);
    }

    /**
     *  话题类型排行
     * @return
     * @throws OperationException
     */
//    @GetMapping(value = "/topicType")
//    @FormatResult
//    //@EnableRedis(cacheMinutes=10)
//    public Object topicType(@ApiParam("时间") @RequestParam(value = "timeRange",required = false,defaultValue = "7d") String timeRange,
//                                @ApiParam("搜索关键词") @RequestParam(value = "keyWords",required = false) String keyWords) throws OperationException {
//        return bigScreenService.topicType(keyWords,timeRange);
//    }
    @GetMapping(value = "/emotionAnalysis")
    @FormatResult
    //@EnableRedis(cacheMinutes=10)
    public Object emotionAnalysis(@ApiParam("时间") @RequestParam(value = "timeRange",required = false,defaultValue = "7d") String timeRange,
                            @ApiParam("搜索关键词") @RequestParam(value = "keyWords",required = false) String keyWords) throws OperationException {
        return bigScreenService.emotionAnalysis(keyWords,timeRange);
    }

    /**
     *  添加大屏地域信息
     * @return
     * @throws OperationException
     */
    @PostMapping(value = "/addDistrictInfo")
    @FormatResult
    //@EnableRedis(cacheMinutes=10)
    public void addDistrictInfo() throws Exception {
        bigScreenDistrictInfoService.save(getListByJson());
    }

    private List<BigScreenDistrictInfo> getListByJson() {
        List<BigScreenDistrictInfo> list = new ArrayList<>();
        String jsonStr = "";
        try {
            File jsonFile = ResourceUtils.getFile(districtPath);
            FileReader fileReader = new FileReader(jsonFile);
            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            JSONArray parseArray = JSONObject.parseArray(jsonStr);
            for (Object object : parseArray) {
                JSONObject parseObject = JSONObject.parseObject(String.valueOf(object));
                BigScreenDistrictInfo bigScreenDistrictInfo = new BigScreenDistrictInfo(parseObject.getString("name"));
                list.add(bigScreenDistrictInfo);
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }
}
