package com.trs.netInsight.widget.report.controller;

import com.alibaba.fastjson.JSONObject;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.cache.PerpetualPool;
import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;
import com.trs.netInsight.support.log.handler.Log;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.special.entity.CustomSpecial;
import com.trs.netInsight.widget.special.entity.enums.SearchScope;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import com.trs.netInsight.widget.special.service.ICustomSpecialService;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/11/5.
 * @desc  舆情报告 极简模式 自定义专题分析操作
 */
@Slf4j
@RestController
@RequestMapping("/customSpecial")
@Api(description = "舆情报告极简模式自定义专题分析接口")
public class CustomSpecialController {
    @Autowired
    private ICustomSpecialService customSpecialService;
    @Autowired
    private OrganizationRepository organizationRepository;

    /**
     *  添加自定义 专题
     * @param request
     * @param specialType 专家模式 | 普通模式
     * @param specialName  专题名称
     * @param anyKeywords  任意关键词
     * @param excludeWords  排除关键词
     * @param source       来源
     * @param searchScope  关键词搜索位置  标题  |  标题+正文
     * @param simflag      是否排重  netRemove 全网排重  urlRemove url排重   不排重
     * @param weight   排序规则
     * @param trsl     专家模式 下  表达式
     * @param timeRange  检索时间
     * @return
     * @throws Exception
     */

    @ApiOperation("新建自定义专题， 两种模式一个接口，通过 special_type 区分")
    @Log(systemLogOperation = SystemLogOperation.CUSTOM_SPECIAL_ADD, systemLogType = SystemLogType.CUSTOM_SPECIAL, systemLogOperationPosition = "")
    @FormatResult
    @ApiImplicitParams({
            @ApiImplicitParam(name = "specialType", value = "专项模式[COMMON, SPECIAL]", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "specialName", value = "专项名称", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "anyKeywords", value = "任意关键词", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "excludeWords", value = "排除词", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "excludeWeb", value = "排除网站", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "source", value = "来源", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "searchScope", value = "搜索范围[TITLE，TITLE_CONTENT]", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "simflag", value = "排重方式 不排，全网排 netRemove,url排重 urlRemove", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "weight", value = "是否按照权重查找", dataType = "boolean", paramType = "query", required = false),
            @ApiImplicitParam(name = "trsl", value = "专家模式传统库表达式", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "timeRange", value = "时间范围[yyyy-MM-dd HH:mm:ss;yyyy-MM-dd HH:mm:ss]", dataType = "String", paramType = "query")})
    @RequestMapping(value = "/addCustomSpecial", method = RequestMethod.POST)
    public Object addCustomSpecial(HttpServletRequest request,
                                   @RequestParam(value = "specialType", required = false, defaultValue = "COMMON") String specialType,
                                   @RequestParam("specialName") String specialName, // 验证空格
                                   @RequestParam(value = "anyKeywords", required = false) String anyKeywords,
                                   @RequestParam(value = "excludeWords", required = false) String excludeWords,
                                   @RequestParam(value = "excludeWeb", required = false) String excludeWeb,
                                   @RequestParam(value = "source", required = false) String source,
                                   @RequestParam(value = "searchScope", required = false, defaultValue = "TITLE") String searchScope,
                                   @RequestParam(value = "simflag", required = false) String simflag,
                                   @RequestParam(value = "weight", required = false) boolean weight,
                                   @RequestParam(value = "trsl", required = false) String trsl,
                                   @RequestParam("timeRange") String timeRange) throws Exception {
        //默认不排重
        boolean isSimilar = false;
        boolean irSimflag = false;
        boolean irSimflagAll = false;
        if ("netRemove".equals(simflag)){
            isSimilar = true;
        }else if ("urlRemove".equals(simflag)){
            irSimflag = true;
        }else if ("sourceRemove".equals(simflag)){
            irSimflagAll = true;
        }
        User loginUser = UserUtils.getUser();
        if (StringUtil.isNotEmpty(specialName) && StringUtil.isNotEmpty(timeRange)
                && StringUtil.isNotEmpty(specialType)) {
            int chineseCount = 0;
            SpecialType type = SpecialType.valueOf(specialType);
            // 专家模式
            if (SpecialType.SPECIAL.equals(type)) {
                if (StringUtil.isEmpty(trsl)) {
                    throw new OperationException("创建自定义专题分析失败，表达式不能为空");
                }
                if ((UserUtils.isRoleAdmin()|| UserUtils.isRoleOrdinary(loginUser))){
                    chineseCount = StringUtil.getChineseCount(trsl);
                }
            } else if (SpecialType.COMMON.equals(type)) {
                // 普通模式
                if (StringUtil.isEmpty(anyKeywords)) {
                    throw new OperationException("创建自定义专题分析失败，关键词不能为空");
                }
                if ((UserUtils.isRoleAdmin()|| UserUtils.isRoleOrdinary(loginUser))){
                    chineseCount = StringUtil.getChineseCountForSimple(anyKeywords);
                }
            }
            Organization organization = organizationRepository.findOne(loginUser.getOrganizationId());
            if ((UserUtils.isRoleAdmin()|| UserUtils.isRoleOrdinary(loginUser))){
                if (chineseCount > organization.getKeyWordsNum()){
                    throw new TRSException(CodeUtils.FAIL,"该专题暂时仅支持检索"+organization.getKeyWordsNum()+"个关键字，如需更多，请联系相关运维人员。");
                }
            }
            SearchScope scope = SearchScope.valueOf(searchScope);
            CustomSpecial customSpecial = new CustomSpecial(loginUser.getId(), type, specialName, anyKeywords,
                    excludeWords,excludeWeb, trsl,  scope, timeRange, source, irSimflag,isSimilar,irSimflagAll, weight);
            customSpecialService.save(customSpecial);
            PerpetualPool.put(loginUser.getId(), DateUtil.formatCurrentTime("yyyyMMddHHmmss"));

            // 新建专题成功,默认触发专题指数计算服务
            // fixedThreadPool.execute(() -> compute());
            return customSpecial;
        } else {
            throw new OperationException("创建监测方案失败");
        }
    }
    /**
     * 查询一个 自定义专题
     * @param customSpecialId
     * @return
     */
    @RequestMapping(value = "/detailCustomSpecial", method = RequestMethod.GET)
    @Log(systemLogOperation = SystemLogOperation.CUSTOM_SPECIAL_DETAIL, systemLogType = SystemLogType.CUSTOM_SPECIAL, systemLogOperationPosition = "")
    @ApiOperation("查询一个 自定义专题")
    @ApiImplicitParams({@ApiImplicitParam(name = "customSpecialId", value = "自定义专题ID", dataType = "String", paramType = "query",required = true)})
    @FormatResult
    public Object detailCustomSpecial(String customSpecialId) throws Exception {
        if (StringUtil.isEmpty(customSpecialId)) {
            throw new OperationException("查自定义专题分析详情，id不能为空");
        }
        return customSpecialService.findOne(customSpecialId);
    }
    /**
     * 3、修改自定义专题
     * @param request
     * @param specialId   要修改专题 id
     * @param specialType 专家模式 | 普通模式
     * @param specialName  专题名称
     * @param anyKeywords  任意关键词
     * @param excludeWords  排除关键词
     * @param source       来源
     * @param searchScope  关键词搜索位置  标题  |  标题+正文
     * @param simflag      是否排重  netRemove 全网排重  urlRemove url排重   不排重
     * @param weight   排序规则
     * @param trsl     专家模式 下  表达式
     * @param timeRange  检索时间
     * @return
     * @throws Exception
     */

    @ApiOperation("修改自定义专题， 两种模式一个接口，通过 special_type 区分")
    @Log(systemLogOperation = SystemLogOperation.CUSTOM_SPECIAL_UPDATE, systemLogType = SystemLogType.CUSTOM_SPECIAL, systemLogOperationPosition = "")
    @FormatResult
    @ApiImplicitParams({
            @ApiImplicitParam(name = "specialId", value = "要修改专题 id", dataType = "String", paramType = "query", required = true),
            @ApiImplicitParam(name = "specialType", value = "专项模式[COMMON, SPECIAL]", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "specialName", value = "专项名称", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "anyKeywords", value = "任意关键词", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "excludeWords", value = "排除词", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "excludeWeb", value = "排除网站", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "source", value = "来源", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "searchScope", value = "搜索范围[TITLE，TITLE_CONTENT]", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "simflag", value = "排重方式 不排，全网排 netRemove,url排重 urlRemove", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "weight", value = "是否按照权重查找", dataType = "boolean", paramType = "query", required = false),
            @ApiImplicitParam(name = "trsl", value = "专家模式传统库表达式", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "timeRange", value = "时间范围[yyyy-MM-dd HH:mm:ss;yyyy-MM-dd HH:mm:ss]", dataType = "String", paramType = "query")})
    @RequestMapping(value = "/updateCustomSpecial", method = RequestMethod.POST)
    public Object updateCustomSpecial(HttpServletRequest request,@RequestParam(value = "specialId",required = true) String specialId,
                                      @RequestParam(value = "specialType", required = false, defaultValue = "COMMON") String specialType,
                                      @RequestParam("specialName") String specialName, // 验证空格
                                      @RequestParam(value = "anyKeywords", required = false) String anyKeywords,
                                      @RequestParam(value = "excludeWords", required = false) String excludeWords,
                                      @RequestParam(value = "excludeWeb", required = false) String excludeWeb,
                                      @RequestParam(value = "source", required = false) String source,
                                      @RequestParam(value = "searchScope", required = false, defaultValue = "TITLE") String  searchScope,
                                      @RequestParam(value = "simflag", required = false) String simflag,
                                      @RequestParam(value = "weight", required = false) boolean weight,
                                      @RequestParam(value = "trsl", required = false) String trsl,
                                      @RequestParam("timeRange") String timeRange) throws Exception {
        //默认不排重
        boolean isSimilar = false;
        boolean irSimflag = false;
        boolean irSimflagAll = false;
        if ("netRemove".equals(simflag)){
            isSimilar = true;
        }else if ("urlRemove".equals(simflag)){
            irSimflag = true;
        }else if ("sourceRemove".equals(simflag)){
            irSimflagAll = true;
        }

        if (StringUtil.isNotEmpty(specialName) && StringUtil.isNotEmpty(timeRange)
                && StringUtil.isNotEmpty(specialType)) {
            User loginUser = UserUtils.getUser();
            SpecialType type = SpecialType.valueOf(specialType);
            // 专家模式
            int chineseCount = 0;
            if (SpecialType.SPECIAL.equals(type)) {
                if (StringUtil.isEmpty(trsl)) {
                    throw new OperationException("修改自定义专题分析失败，表达式不能为空");
                }
                if ((UserUtils.isRoleAdmin()|| UserUtils.isRoleOrdinary(loginUser))){
                    chineseCount = StringUtil.getChineseCount(trsl);
                }
            } else if (SpecialType.COMMON.equals(type)) {
                // 普通模式
                if (StringUtil.isEmpty(anyKeywords)) {
                    throw new OperationException("修改自定义专题分析失败，关键词不能为空");
                }
                if ((UserUtils.isRoleAdmin()|| UserUtils.isRoleOrdinary(loginUser))){
                    chineseCount = StringUtil.getChineseCountForSimple(anyKeywords);
                }
            }
            Organization organization = organizationRepository.findOne(loginUser.getOrganizationId());
            if ((UserUtils.isRoleAdmin()|| UserUtils.isRoleOrdinary(loginUser))){
                if (chineseCount > organization.getKeyWordsNum()){
                    throw new TRSException(CodeUtils.FAIL,"该专题暂时仅支持检索"+organization.getKeyWordsNum()+"个关键字，如需更多，请联系相关运维人员。");
                }
            }
            SearchScope scope = SearchScope.valueOf(searchScope);
            CustomSpecial customSpecial = customSpecialService.update(specialId, type, specialName, anyKeywords, excludeWords,excludeWeb, trsl, scope, timeRange, source, irSimflag,irSimflagAll,isSimilar,weight);
            PerpetualPool.put(loginUser.getId(), DateUtil.formatCurrentTime("yyyyMMddHHmmss"));

            return customSpecial;
        } else {
            throw new OperationException("修改监测方案失败");
        }
    }
    /**
     * 删除 自定义专题
     * @param customSpecialId
     * @return
     */
    @RequestMapping(value = "/deleteCustomSpecial", method = RequestMethod.POST)
    @Log(systemLogOperation = SystemLogOperation.CUSTOM_SPECIAL_DELETE, systemLogType = SystemLogType.CUSTOM_SPECIAL, systemLogOperationPosition = "")
    @ApiOperation("删除 自定义专题")
    @ApiImplicitParams({@ApiImplicitParam(name = "customSpecialId", value = "自定义专题ID", dataType = "String", paramType = "query",required = true)})
    @FormatResult
    public Object deleteCustomSpecial(@RequestParam(value = "customSpecialId",required = true) String customSpecialId) {
        customSpecialService.delete(customSpecialId);
        return Const.SUCCESS;
    }

    /**
     * 修改历史数据  词距
     * @param request
     * @param response
     */
    @ApiOperation("修改历史数据 专题分析关键词")
    @PostMapping(value = "/changHistoryDataForCusSpecialProjectWordSpacing")
    public void changHistoryDataForSpecialProject(javax.servlet.http.HttpServletRequest request, HttpServletResponse response) {
        //查询所有用户分组
        List<CustomSpecial> specialProjects = customSpecialService.findBySimple(SpecialType.COMMON);
        if (ObjectUtil.isNotEmpty(specialProjects)){
            System.err.println("普通模式专题数："+specialProjects.size());
            for (CustomSpecial specialProject : specialProjects) {
                String anyKeywords = specialProject.getAnyKeyWords();
                System.err.println("自定义专题关键字："+anyKeywords);
                if (StringUtil.isNotEmpty(anyKeywords)){
                    Map<String, Object> hashMap = new HashMap<>();
                    hashMap.put("wordSpace",0);
                    hashMap.put("wordOrder",false);
                    hashMap.put("keyWords",anyKeywords);
                    String toJSONString = JSONObject.toJSONString(hashMap);
                    specialProject.setAnyKeyWords("["+toJSONString+"]");
                    customSpecialService.save(specialProject);
                }
            }
        }
        System.err.println("自定义专题分析修改成功！");
    }
}
