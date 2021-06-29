package com.trs.netInsight.support.bigscreen.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.bigscreen.service.IBigScreenService;
import com.trs.netInsight.support.cache.EnableRedis;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.MD5;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.RedisUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.analysis.controller.SpecialChartAnalyzeController;
import com.trs.netInsight.widget.analysis.entity.BigScreenDistrictInfo;
import com.trs.netInsight.widget.analysis.entity.ChartResultField;
import com.trs.netInsight.widget.analysis.service.IBigScreenDistrictInfoService;
import com.trs.netInsight.widget.analysis.service.IChartAnalyzeService;
import com.trs.netInsight.widget.column.controller.HotTopController;
import com.trs.netInsight.widget.common.service.ICommonChartService;
import com.trs.netInsight.widget.common.service.ICommonListService;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.special.entity.InfoListResult;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.service.IInfoListService;
import com.trs.netInsight.widget.special.service.ISpecialProjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    @Autowired
    private IInfoListService infoListService;
    @Autowired
    private ISpecialProjectService specialProjectService;
    @Autowired
    private SpecialChartAnalyzeController specialChartAnalyzeController;
    @Autowired
    private IChartAnalyzeService specialChartAnalyzeService;
    @Autowired
    private ICommonListService commonListService;
    @Autowired
    private ICommonChartService commonChartService;
    @Autowired
    private HotTopController hotTopController;
    private String slat = "hashKw9d$wx[NNL2RBPT0=";

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
    @ApiOperation("宿州涉市舆情事件列表接口")
    @FormatResult
    @RequestMapping(value = "/getSentimentInfoList", method = RequestMethod.POST)
    public Object getSentimentInfoList(@ApiParam("来源id") @RequestParam(value = "id", required = false) String specialId,
                                       @ApiParam("时间戳") @RequestParam("msec") String msec,
                                       @ApiParam("加密后的密文") @RequestParam("encrypting") String encrypting) throws TRSException {
        String passNum = MD5.getBase64(msec+slat);
        if(!encrypting.equals(passNum)){
            log.error("验证失败");
            throw new TRSException("密文验证失败!");
        }
        SpecialProject specialProject = specialProjectService.findOne(specialId);
        if (ObjectUtil.isEmpty(specialProject))  throw new TRSException("没有此id");
        List<Map<String, Object>> result = specialChartAnalyzeService.getHotListMessage(specialProject.getSource(),
                specialProject, specialProject.getTimeRange(),24);
        return result;
    }
    @ApiOperation("报道量走势接口")
    @FormatResult
    @RequestMapping(value = "/sentimentLine", method = RequestMethod.POST)
    public Object sentimentLine(@ApiParam("来源id") @RequestParam(value = "id", required = false) String specialId ,
                                @ApiParam("时间戳") @RequestParam("msec") String msec,
                                @ApiParam("加密后的密文") @RequestParam("encrypting") String encrypting) throws TRSException, ParseException {
        String passNum = MD5.getBase64(msec+slat);
        if(!encrypting.equals(passNum)){
            log.error("验证失败");
            throw new TRSException("密文验证失败!");
        }
        SpecialProject specialProject = specialProjectService.findOne(specialId);
        return specialChartAnalyzeController.webCountLine(null,specialId, "day", false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
    @ApiOperation("舆情态势指数接口")
    @FormatResult
    @RequestMapping(value = "/situationAssessment", method = RequestMethod.POST)
    public Object situationAssessment(@ApiParam("来源id") @RequestParam(value = "id", required = false) String specialId,
                                      @ApiParam("时间戳") @RequestParam("msec") String msec,
                                      @ApiParam("加密后的密文") @RequestParam("encrypting") String encrypting) throws TRSException, ParseException {
        String passNum = MD5.getBase64(msec+slat);
        if(!encrypting.equals(passNum)){
            log.error("验证失败");
            throw new TRSException("密文验证失败!");
        }
        SpecialProject specialProject = specialProjectService.findOne(specialId);
        if (ObjectUtil.isEmpty(specialProject))  throw new TRSException("没有此id");
        QueryBuilder searchBuilder = specialProject.toNoPagedBuilder();
        Object object = specialChartAnalyzeService.getSituationAssessment(searchBuilder,specialProject);
        return object;
       // return specialChartAnalyzeController.situationAssessment("0d", specialId, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
    @ApiOperation("情感分析接口")
    @FormatResult
    @RequestMapping(value = "/emotionOption", method = RequestMethod.POST)
    public Object emotionOption(@ApiParam("来源id") @RequestParam(value = "id", required = false) String specialId,
                                @ApiParam("时间戳") @RequestParam("msec") String msec,
                                @ApiParam("加密后的密文") @RequestParam("encrypting") String encrypting) throws Exception {
        String passNum = MD5.getBase64(msec+slat);
        if(!encrypting.equals(passNum)){
            log.error("验证失败");
            throw new TRSException("密文验证失败!");
        }
        SpecialProject specialProject = specialProjectService.findOne(specialId);
        if (ObjectUtil.isEmpty(specialProject))  throw new TRSException("没有此id");
        QueryBuilder searchBuilder = specialProject.toNoPagedBuilder();
        List<Map<String, String>> list =specialChartAnalyzeService.emotionOption(searchBuilder,specialProject);
        return list;
    }
    @ApiOperation("活跃媒体排行接口")
    @FormatResult
    @RequestMapping(value = "/active_account", method = RequestMethod.POST)
    public Object getActiveAccount(@ApiParam("来源id") @RequestParam(value = "id", required = false) String specialId,
                                   @ApiParam("时间戳") @RequestParam("msec") String msec,
                                   @ApiParam("加密后的密文") @RequestParam("encrypting") String encrypting) throws Exception {
        String passNum = MD5.getBase64(msec+slat);
        if(!encrypting.equals(passNum)){
            log.error("验证失败");
            throw new TRSException("密文验证失败!");
        }
        SpecialProject specialProject = specialProjectService.findOne(specialId);
        if (ObjectUtil.isEmpty(specialProject))  throw new TRSException("没有此id");
        QueryBuilder builder = specialProject.toNoPagedBuilder();
        String timeRange = specialProject.getTimeRange();
        if (StringUtils.isBlank(timeRange)) {
            timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
            timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
        }
        String[] range = DateUtil.formatTimeRange(timeRange);
        Object mediaActiveAccount = bigScreenService.mediaActiveAccount(builder,specialProject.getSource(), range, specialProject.isSimilar(),
                specialProject.isIrSimflag(),specialProject.isIrSimflagAll());
        return mediaActiveAccount;
    }
    @ApiOperation("宿州地图信息接口")
    @FormatResult
    @RequestMapping(value = "/mapInfo", method = RequestMethod.POST)
    public Object getMapInfo(@ApiParam("时间区间") @RequestParam(value = "timeRange", required = false,defaultValue = "0d") String timeRange,
                             @ApiParam("时间戳") @RequestParam("msec") String msec,
                             @ApiParam("加密后的密文") @RequestParam("encrypting") String encrypting) throws Exception {
        String passNum = MD5.getBase64(msec+slat);
        if(!encrypting.equals(passNum)){
            log.error("验证失败");
            throw new TRSException("密文验证失败!");
        }

        return bigScreenService.mapInfo(timeRange);
    }
    @ApiOperation("涉市热点预警接口")
    @FormatResult
    @RequestMapping(value = "/hotList", method = RequestMethod.POST)
    public Object getHotList(@ApiParam("来源id") @RequestParam(value = "id", required = false) String specialId,
                             @ApiParam("时间戳") @RequestParam("msec") String msec,
                             @ApiParam("加密后的密文") @RequestParam("encrypting") String encrypting) throws Exception {
        String passNum = MD5.getBase64(msec+slat);
        if(!encrypting.equals(passNum)){
            log.error("验证失败");
            throw new TRSException("密文验证失败!");
        }
        SpecialProject specialProject = specialProjectService.findOne(specialId);
        if (ObjectUtil.isEmpty(specialProject))  throw new TRSException("没有此id");
        QueryBuilder builder = specialProject.toNoPagedBuilder();
        String timeRange = specialProject.getTimeRange();
        if (StringUtils.isBlank(timeRange)) {
            timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
            timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
        }
        String[] range = DateUtil.formatTimeRange(timeRange);
        List<Map<String, Object>> result = specialChartAnalyzeService.getHotListMessage(specialProject.getSource(),
                specialProject, specialProject.getTimeRange(),21);
        return result;
    }
    @ApiOperation("涉市敏感预警接口")
    @FormatResult
    @RequestMapping(value = "/sensitiveList", method = RequestMethod.POST)
    public Object getSensitiveList(@ApiParam("来源id") @RequestParam(value = "id", required = false) String specialId,
                                   @ApiParam("时间戳") @RequestParam("msec") String msec,
                                   @ApiParam("加密后的密文") @RequestParam("encrypting") String encrypting) throws Exception {
        String passNum = MD5.getBase64(msec+slat);
        if(!encrypting.equals(passNum)){
            log.error("验证失败");
            throw new TRSException("密文验证失败!");
        }
        SpecialProject specialProject = specialProjectService.findOne(specialId);
        if (ObjectUtil.isEmpty(specialProject))  throw new TRSException("没有此id");
        QueryBuilder builder = specialProject.toSearchBuilder(0, 21, true);
        String timeRange = specialProject.getTimeRange();
        if (StringUtils.isBlank(timeRange)) {
            timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
            timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
        }
        String[] range = DateUtil.formatTimeRange(timeRange);
        InfoListResult infoListResult = null;
        builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
        infoListResult = commonListService.queryPageList(builder, specialProject.isSimilar(), specialProject.isIrSimflag(), specialProject.isIrSimflagAll(), specialProject.getSource(), "special", null, false);
        if (infoListResult != null) {
            if (infoListResult.getContent() != null) {
                String trslk = infoListResult.getTrslk();
                String wordIndex = String.valueOf(specialProject.getSearchScope().ordinal());
                PagedList<Object> resultContent = CommonListChartUtil.formatListData(infoListResult, trslk, wordIndex);
                infoListResult.setContent(resultContent);
            }
        }
        return infoListResult;
    }
    @ApiOperation("舆论场统计接口")
    @FormatResult
    @EnableRedis
    @RequestMapping(value = "/webCommitCount", method = RequestMethod.POST)
    public Object webCommitCount(@ApiParam("来源id") @RequestParam(value = "id", required = false) String specialId,
                                 @ApiParam("时间区间(根据模块选择是否传)") @RequestParam(value = "timeRange", required = false,defaultValue = "0d") String timeRange,
                                   @ApiParam("时间戳") @RequestParam("msec") String msec,
                                   @ApiParam("加密后的密文") @RequestParam("encrypting") String encrypting) throws Exception {
        String passNum = MD5.getBase64(msec + slat);
        if (!encrypting.equals(passNum)) {
            log.error("验证失败");
            throw new TRSException("密文验证失败!");
        }
        SpecialProject specialProject = specialProjectService.findOne(specialId);
        if (ObjectUtil.isEmpty(specialProject))  throw new TRSException("没有此id");
        if (StringUtil.isNotEmpty(timeRange)){
            String[] timeArray = DateUtil.formatTimeRange(timeRange);
            if (timeArray != null && timeArray.length == 2) {
                specialProject.setStart(timeArray[0]);
                specialProject.setEnd(timeArray[1]);
            }
        }
        String redisKey = "bigScreenRedis_365d";
        String redisKeyAddTime = "bigScreenRedisAddTime_365d";
        if ("365d".equals(timeRange)) {//查询一年数据走redis
            Object rt = RedisUtil.getObject(redisKey);
            String addTime = RedisUtil.getString(redisKeyAddTime);
            //key存放redis中的时间(分)
            long alreadyAddMin = 1000l;
            if (addTime != null)
                alreadyAddMin = com.trs.netInsight.util.DateUtil.getDateTimeMin(addTime, com.trs.netInsight.util.DateUtil.formatCurrentTime("yyyy-MM-dd HH:mm:ss"), "min");
//          // redis有数据并且小于10分钟直接去redis数据
            if (rt != null && alreadyAddMin < 61) {
                //log.info("从redis获取该信息----------");
                return rt;
            }
        }
        // 排重
        boolean sim = specialProject.isSimilar();
        // url排重
        boolean irSimflag = specialProject.isIrSimflag();
        //跨数据源排重
        boolean irSimflagAll = specialProject.isIrSimflagAll();
        QueryBuilder builder = specialProject.toNoPagedBuilder();
        String contrastField = FtsFieldConst.FIELD_GROUPNAME;
        builder.setPageSize(20);
        ChartResultField resultField = new ChartResultField("name", "value");
        List<Map<String, Object>> list = new ArrayList<>();
        list = (List<Map<String, Object>>)commonChartService.getPieColumnData(builder,sim,irSimflag,irSimflagAll,CommonListChartUtil.changeGroupName(specialProject.getSource()),null,contrastField,"special",resultField);
        if(list != null && "365d".equals(timeRange)){
            RedisUtil.setObject(redisKey,list);
            RedisUtil.expire(redisKey,60,TimeUnit.MINUTES);
            RedisUtil.setString(redisKeyAddTime, com.trs.netInsight.util.DateUtil.formatCurrentTime("yyyy-MM-dd HH:mm:ss"));
        }
        return list;
    }
    @ApiOperation("涉市账号预警接口")
    @FormatResult
    @RequestMapping(value = "/accountAlertList", method = RequestMethod.POST)
    public Object accountAlertList(@ApiParam("来源id") @RequestParam(value = "id", required = false) String specialId,
                                  @ApiParam("时间戳") @RequestParam("msec") String msec,
                                   @ApiParam("加密后的密文") @RequestParam("encrypting") String encrypting) throws Exception {
        String passNum = MD5.getBase64(msec+slat);
        if(!encrypting.equals(passNum)){
            log.error("验证失败");
            throw new TRSException("密文验证失败!");
        }
        SpecialProject specialProject = specialProjectService.findOne(specialId);
        specialProject.setSource("微博;微信;自媒体号;短视频");
        QueryBuilder builder = specialProject.toSearchBuilder(0, 21, true);
        String timeRange = specialProject.getTimeRange();
        if (StringUtils.isBlank(timeRange)) {
            timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
            timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
        }
        String[] range = DateUtil.formatTimeRange(timeRange);
        InfoListResult infoListResult = null;
        builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
        infoListResult = commonListService.queryPageList(builder, specialProject.isSimilar(), specialProject.isIrSimflag(), specialProject.isIrSimflagAll(), specialProject.getSource(), "special", null, false);
        if (infoListResult != null) {
            if (infoListResult.getContent() != null) {
                String trslk = infoListResult.getTrslk();
                String wordIndex = String.valueOf(specialProject.getSearchScope().ordinal());
                PagedList<Object> resultContent = CommonListChartUtil.formatListData(infoListResult, trslk, wordIndex);
                infoListResult.setContent(resultContent);
            }
        }
        return infoListResult;
    }
    @ApiOperation("热榜接口")
    @FormatResult
    @RequestMapping(value = "/hotTopList", method = RequestMethod.POST)
    public Object getHotList(@ApiParam("时间区间") @RequestParam(value = "timeRange", required = true) String timeRange,
                             @ApiParam("榜单类型") @RequestParam(value = "siteName", required = true) String siteName,
                             @ApiParam("频道类型") @RequestParam(value = "channelName", required = false) String channelName,
                             @ApiParam("关键词") @RequestParam(value = "keyword", required = false) String keyword,
                             @ApiParam("时间戳") @RequestParam("msec") String msec,
                             @ApiParam("加密后的密文") @RequestParam("encrypting") String encrypting) throws Exception {
        String passNum = MD5.getBase64(msec+slat);
        if(!encrypting.equals(passNum)){
            log.error("验证失败");
            throw new TRSException("密文验证失败!");
        }

        return bigScreenService.hotList(timeRange,siteName,channelName,keyword);
    }
}
