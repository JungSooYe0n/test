package com.trs.netInsight.widget.special.controller;


import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;
import com.trs.netInsight.support.log.handler.Log;
import com.trs.netInsight.util.CodeUtils;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.column.entity.CustomChart;
import com.trs.netInsight.widget.column.entity.IndexTabType;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.column.factory.ColumnFactory;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.special.SpecialCustomChart;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import com.trs.netInsight.widget.special.service.ISpecialCustomChartService;
import com.trs.netInsight.widget.special.service.ISpecialProjectService;
import com.trs.netInsight.widget.special.service.ISpecialService;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 专题分析自定义图表接口
 */
@RestController
@RequestMapping("/special/chart")
@Api(description = "专题分析自定义图表接口")
@Slf4j
public class SpecialChartController {
    @Autowired
    private ISpecialCustomChartService specialCustomChartService;
    @Autowired
    private ISpecialProjectService specialProjectService;
    @Autowired
    private OrganizationRepository organizationRepository;


    /**
     * 查找当前专题分析栏目对应的自定义图表
     *
     * @param request
     * @param id
     * @return
     * @throws TRSException
     */
    @FormatResult
    @RequestMapping(value = "/selectSpecialChartList", method = RequestMethod.GET)
    @ApiOperation("查找当前专题分析对应的图表 - 自定义图表")
    public Object selectTabChartList(HttpServletRequest request,
                                     @ApiParam("专题分析栏目id") @RequestParam(value = "id") String id)
            throws TRSException {
        User user = UserUtils.getUser();
        SpecialProject specialProject = specialProjectService.findOne(id);
        if (ObjectUtil.isEmpty(specialProject)) {
            throw new TRSException("当前专题栏目不存在");
        }
        Object result = specialCustomChartService.getCustomChart(id);
        return result;
    }

    /**
     * 栏目下自定义图表添加接口
     *
     * @param name
     * @param specialId
     * @param type
     * @param trsl
     * @param xyTrsl
     * @param keyWord
     * @param keyWordIndex
     * @param groupName
     * @param timeRange
     * @param request
     * @return
     * @throws OperationException
     */
    @FormatResult
    @Log(systemLogOperation = SystemLogOperation.SPECIAL_ADD, systemLogType = SystemLogType.SPECIAL,
            systemLogOperationPosition = "专题分析栏目下添加自定义图表：${specialId}/@{name}", methodDescription = "添加自定义图表:${name}")
    @RequestMapping(value = "/addCustomChart", method = RequestMethod.POST)
    @ApiOperation("专题分析栏目下自定义图表添加接口")
    @ApiImplicitParams({@ApiImplicitParam(name = "name", value = "自定义图表名", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "specialId", value = "对应专题分析栏目Id", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "specialType", value = "栏目模式类型：COMMON 普通模式、SPECIAL专家模式", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "type", value = "图表类型", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "contrast", value = "分类对比类型", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "trsl", value = "检索表达式", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "keyWord", value = "关键词", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "excludeWords", value = "排除词[雾霾;沙尘暴]", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "excludeWeb", value = "排除网站", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "keyWordIndex", value = "关键词位置(0:标题,1:标题+正文,2:标题+摘要)", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "xyTrsl", value = "XY轴检索表达式", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "groupName", value = "数据来源(可多值,中间以';'隔开", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "timeRange", value = "发布时间范围(2017-10-01 00:00:00;2017-10-20 00:00:00)", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "weight", value = "标题权重", dataType = "boolean", paramType = "query", required = false),
            @ApiImplicitParam(name = "simflag", value = "排重方式 不排 no，全网排 netRemove,url排 urlRemove,跨数据源排 sourceRemove", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "tabWidth", value = "栏目是不是通栏，50为半栏，100为通栏", dataType = "int", paramType = "query", required = false)})
    public Object addCustomChart(@RequestParam("name") String name, @RequestParam(value = "specialId") String specialId,
                                 @RequestParam("specialType") String specialType,
                                 @RequestParam("type") String type, @RequestParam(value = "contrast", required = false) String contrast,
                                 @RequestParam(value = "trsl", required = false) String trsl,
                                 @RequestParam(value = "xyTrsl", required = false) String xyTrsl,
                                 @RequestParam(value = "keyWord", required = false) String keyWord,
                                 @RequestParam(value = "excludeWords", required = false) String excludeWords,
                                 @RequestParam(value = "keyWordIndex", required = false) String keyWordIndex,
                                 @RequestParam(value = "groupName", required = false, defaultValue = "ALL") String groupName,
                                 @RequestParam(value = "timeRange", required = false) String timeRange,
                                 @RequestParam(value = "excludeWeb", required = false) String excludeWeb,
                                 @RequestParam(value = "weight", required = false) boolean weight,
                                 @RequestParam(value = "simflag", required = false) String simflag,
                                 @RequestParam(value = "tabWidth", required = false, defaultValue = "50") int tabWidth,
                                 HttpServletRequest request)
            throws TRSException {
        String[] typeArr = type.split(";");

        //首先判断下用户权限（若为机构管理员，只受新建与编辑的权限，不受可创建资源数量的限制）
        User loginUser = UserUtils.getUser();
        if ((UserUtils.isRoleAdmin() || UserUtils.isRoleOrdinary(loginUser)) && StringUtil.isNotEmpty(loginUser.getOrganizationId())) {

            Organization organization = organizationRepository.findOne(loginUser.getOrganizationId());
            //可创建自定义图表数量 暂时不与权限绑定，直接限定最多10个
            Integer count = specialCustomChartService.getCustomChartSize(specialId);
            if (count + typeArr.length > 10) {
                throw new TRSException(CodeUtils.FAIL, "当前栏目下自定义图表创建已达上限，如需更多，请联系相关运维人员。");
            }
            //若为机构管理员或者普通用户 若为普通模式，判断关键字字数
            if ((UserUtils.isRoleAdmin() || UserUtils.isRoleOrdinary(loginUser))) {
                int chineseCount = 0;
                if (StringUtil.isNotEmpty(keyWord)) {
                    chineseCount = StringUtil.getChineseCountForSimple(keyWord);
                } else if (StringUtil.isNotEmpty(trsl) || StringUtil.isNotEmpty(xyTrsl)) {
                    int trslCount = StringUtil.getChineseCount(trsl);
                    int xyTrslCount = StringUtil.getChineseCount(xyTrsl);
                    chineseCount = trslCount + xyTrslCount;
                }
                if (chineseCount > organization.getKeyWordsNum()) {
                    throw new TRSException(CodeUtils.FAIL, "该自定义图表暂时仅支持检索" + organization.getKeyWordsNum() + "个关键字，如需更多，请联系相关运维人员。");
                }
            }
        }
        SpecialProject specialProject = specialProjectService.findOne(specialId);
        if (ObjectUtil.isEmpty(specialProject)) {
            throw new TRSException("当前专题栏目不存在");
        }


        Integer seq = specialCustomChartService.getMaxChartSequence(specialId);
        Integer sequence = seq;
        if (StringUtil.isEmpty(timeRange)) {
            timeRange = "7d";
        }
        // 默认不排重
        boolean isSimilar = false;
        boolean irSimflag = false;
        boolean irSimflagAll = false;
        if ("netRemove".equals(simflag)) {
            isSimilar = true;   //单一媒体排重
        } else if ("urlRemove".equals(simflag)) {
            irSimflag = true;   //站内排重
        } else if ("sourceRemove".equals(simflag)) {
            irSimflagAll = true;//全网排重
        }
        List<SpecialCustomChart> result = new ArrayList<>();
        SpecialType specialType1 = SpecialType.valueOf(specialType);
        for (String oneType : typeArr) {
            sequence += 1;
            IndexTabType indexTabType = ColumnFactory.chooseType(oneType);
            if (ObjectUtil.isEmpty(indexTabType)) {
                throw new TRSException(CodeUtils.FAIL, "当前图表类型不存在");
            }

            // 有几个图专家模式下 必须传xy表达式
            if (SpecialType.SPECIAL.equals(specialType1)) {
                if (StringUtil.isNotEmpty(trsl)) {
                    contrast = null;
                    if (IndexTabType.CHART_BAR.equals(indexTabType) || IndexTabType.CHART_LINE.equals(indexTabType) || IndexTabType.CHART_PIE.equals(indexTabType)) {
                        if (StringUtil.isEmpty(xyTrsl)) {
                            throw new OperationException("专家模式下" + indexTabType.getTypeName() + "时必须传xy表达式");
                        }
                    }
                } else {
                    throw new OperationException("专家模式下必须填写检索表达式表达式");
                }
            } else {
                trsl = null;
                xyTrsl = null;
                if (StringUtil.isEmpty(contrast) && !IndexTabType.HOT_LIST.equals(indexTabType) && !IndexTabType.LIST_NO_SIM.equals(indexTabType)
                        && !IndexTabType.WORD_CLOUD.equals(indexTabType) && !IndexTabType.MAP.equals(indexTabType)) {
                    throw new OperationException("普通模式下" + indexTabType.getTypeName() + "时，必须传对比类型");
                }
            }
            if (ColumnConst.CONTRAST_TYPE_WECHAT.equals(contrast)) {
                groupName = Const.PAGE_SHOW_WEIXIN;
            }
            groupName = CommonListChartUtil.changeGroupName(groupName);
            SpecialCustomChart customChart = new SpecialCustomChart(name, trsl, xyTrsl, oneType, contrast, excludeWeb, timeRange, keyWord, excludeWords,
                    keyWordIndex, groupName, isSimilar, irSimflag, irSimflagAll, weight, tabWidth, specialId, sequence, specialType1);
            customChart = specialCustomChartService.saveSpecialCustomChart(customChart);
            result.add(customChart);
        }
        return result;

    }


    /**
     * 栏目下自定义图表修改接口
     *
     * @param id
     * @param name
     * @param type
     * @param contrast
     * @param trsl
     * @param xyTrsl
     * @param keyWord
     * @param excludeWords
     * @param keyWordIndex
     * @param groupName
     * @param timeRange
     * @param excludeWeb
     * @param weight
     * @param simflag
     * @param tabWidth
     * @param request
     * @return
     * @throws OperationException
     */
    @FormatResult
    @RequestMapping(value = "/updateCustomChart", method = RequestMethod.POST)
    @Log(systemLogOperation = SystemLogOperation.SPECIAL_UPDATE, systemLogType = SystemLogType.SPECIAL,
            systemLogOperationPosition = "修改专题分析栏目下自定义图表：${id}", methodDescription = "${name}")
    @ApiOperation("专题分析栏目下自定义图表修改接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "自定义图表id", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "name", value = "自定义图表名", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "specialType", value = "栏目模式类型：COMMON 普通模式、SPECIAL专家模式", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "type", value = "图表类型", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "contrast", value = "分类对比类型", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "trsl", value = "检索表达式", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "keyWord", value = "关键词", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "excludeWords", value = "排除词[雾霾;沙尘暴]", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "excludeWeb", value = "排除网站", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "keyWordIndex", value = "关键词位置(0:标题,1:标题+正文,2:标题+摘要)", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "xyTrsl", value = "XY轴检索表达式", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "groupName", value = "数据来源(可多值,中间以';'隔开,默认为新闻)", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "timeRange", value = "发布时间范围(2017-10-01 00:00:00;2017-10-20 00:00:00)", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "weight", value = "标题权重", dataType = "boolean", paramType = "query", required = false),
            @ApiImplicitParam(name = "simflag", value = "排重方式 不排 no，全网排 netRemove,url排 urlRemove,跨数据源排 sourceRemove", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "tabWidth", value = "栏目是不是通栏，50为半栏，100为通栏", dataType = "int", paramType = "query", required = false)})
    public Object updateCustomChart(@RequestParam("id") String id, @RequestParam("name") String name,
                                    @RequestParam("specialType") String specialType,
                                    @RequestParam("type") String type, @RequestParam(value = "contrast", required = false) String contrast,
                                    @RequestParam(value = "trsl", required = false) String trsl,
                                    @RequestParam(value = "xyTrsl", required = false) String xyTrsl,
                                    @RequestParam(value = "keyWord", required = false) String keyWord,
                                    @RequestParam(value = "excludeWords", required = false) String excludeWords,
                                    @RequestParam(value = "keyWordIndex", required = false) String keyWordIndex,
                                    @RequestParam(value = "groupName", required = false, defaultValue = "ALL") String groupName,
                                    @RequestParam(value = "timeRange", required = false) String timeRange,
                                    @RequestParam(value = "excludeWeb", required = false) String excludeWeb,
                                    @RequestParam(value = "weight", required = false) boolean weight,
                                    @RequestParam(value = "simflag", required = false) String simflag,
                                    @RequestParam(value = "tabWidth", required = false, defaultValue = "50") int tabWidth,
                                    HttpServletRequest request)
            throws TRSException {

        User loginUser = UserUtils.getUser();
        //若为机构管理员或者普通用户 若为普通模式，判断关键字字数
        if ((UserUtils.isRoleAdmin() || UserUtils.isRoleOrdinary(loginUser)) && StringUtil.isNotEmpty(loginUser.getOrganizationId())) {
            Organization organization = organizationRepository.findOne(loginUser.getOrganizationId());
            int chineseCount = 0;
            if (StringUtil.isNotEmpty(keyWord)) {
                chineseCount = StringUtil.getChineseCountForSimple(keyWord);
            } else if (StringUtil.isNotEmpty(trsl) || StringUtil.isNotEmpty(xyTrsl)) {
                int trslCount = StringUtil.getChineseCount(trsl);
                int xyTrslCount = StringUtil.getChineseCount(xyTrsl);
                chineseCount = trslCount + xyTrslCount;
            }

            if (chineseCount > organization.getKeyWordsNum()) {
                throw new TRSException(CodeUtils.FAIL, "所修改自定义图表的关键字字数 已超出 该机构关键字数限制的上限!");
            }
        }
        try {
            IndexTabType indexTabType = ColumnFactory.chooseType(type);
            if (ObjectUtil.isEmpty(indexTabType)) {
                throw new TRSException(CodeUtils.FAIL, "当前自定义图表类型不存在");
            }
            // 默认不排重
            boolean isSimilar = false;
            boolean irSimflag = false;
            boolean irSimflagAll = false;
            if ("netRemove".equals(simflag)) {
                isSimilar = true;
                //irSimflagAll = true;
            } else if ("urlRemove".equals(simflag)) {
                irSimflag = true;
            } else if ("sourceRemove".equals(simflag)) {
                irSimflagAll = true;
            }
            if (StringUtil.isEmpty(timeRange)) {
                timeRange = "7d";
            }
            SpecialType specialType1 = SpecialType.valueOf(specialType);
            // 有几个图专家模式下 必须传xy表达式
            if (SpecialType.SPECIAL.equals(specialType1)) {
                if (StringUtil.isNotEmpty(trsl)) {
                    contrast = null;
                    if (IndexTabType.CHART_BAR.equals(indexTabType) || IndexTabType.CHART_LINE.equals(indexTabType) || IndexTabType.CHART_PIE.equals(indexTabType)) {
                        if (StringUtil.isEmpty(xyTrsl)) {
                            throw new OperationException("专家模式下" + indexTabType.getTypeName() + "时必须传xy表达式");
                        }
                    }
                } else {
                    throw new OperationException("专家模式下必须填写检索表达式表达式");
                }
            } else {
                trsl = null;
                xyTrsl = null;
                if (StringUtil.isEmpty(contrast) && !IndexTabType.HOT_LIST.equals(indexTabType) && !IndexTabType.LIST_NO_SIM.equals(indexTabType)
                        && !IndexTabType.WORD_CLOUD.equals(indexTabType) && !IndexTabType.MAP.equals(indexTabType)) {
                    throw new OperationException("普通模式下" + indexTabType.getTypeName() + "时，必须传对比类型");
                }
            }
            if (ColumnConst.CONTRAST_TYPE_WECHAT.equals(contrast)) {
                groupName = Const.PAGE_SHOW_WEIXIN;
            }
            SpecialCustomChart customChart = specialCustomChartService.findOneSpecialCustomChart(id);
            if (ObjectUtil.isEmpty(customChart)) {
                throw new TRSException("当前自定义图表不存在");
            }
            customChart.setName(name);
            customChart.setTrsl(trsl);
            customChart.setSpecialType(specialType1);
            customChart.setXyTrsl(xyTrsl);
            customChart.setType(type);
            customChart.setContrast(contrast);
            customChart.setExcludeWeb(excludeWeb);
            customChart.setTimeRange(timeRange);
            customChart.setKeyWord(keyWord);
            customChart.setExcludeWords(excludeWords);
            customChart.setKeyWordIndex(keyWordIndex);
            customChart.setGroupName(groupName);
            customChart.setSimilar(isSimilar);
            customChart.setIrSimflag(irSimflag);
            customChart.setIrSimflagAll(irSimflagAll);

            // 栏目从标题+正文修改为仅标题的时候不设置权重，但传的weight还是=true
            if ("0".equals(keyWordIndex)) {
                customChart.setWeight(false);
            } else {
                customChart.setWeight(weight);
            }
            customChart.setTabWidth(tabWidth);

            return specialCustomChartService.saveSpecialCustomChart(customChart);
        } catch (Exception e) {
            log.error("栏目下自定义图表修改报错", e);
            throw new OperationException("栏目下自定义图表修改报错", e);
        }
    }

    /**
     * 栏目下自定义图表删除
     *
     * @param id
     * @param request
     * @return
     * @throws TRSException
     */
    @FormatResult
    @Log(systemLogOperation = SystemLogOperation.SPECIAL_DELETE, systemLogType = SystemLogType.SPECIAL, systemLogOperationPosition = "删除专题分析栏目下自定义图表（图表）：${id}")
    @RequestMapping(value = "/deleteCustomChart", method = RequestMethod.POST)
    @ApiOperation("专题分析栏目下自定义图表删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "自定义图表id", dataType = "String", paramType = "query")})
    public Object deleteCustomChart(@RequestParam("id") String id, HttpServletRequest request)
            throws TRSException {
        specialCustomChartService.deleteSpecialCustomChart(id);
        return "success";
    }

    /**
     * 获取自定义图表的数据
     *
     * @param request
     * @param id
     * @return
     * @throws TRSException
     */
    @FormatResult
    @RequestMapping(value = "/selectChart", method = RequestMethod.POST)
    @ApiOperation("获取自定义图表的数据")
    public Object selectChart(HttpServletRequest request,
                              @ApiParam("自定义图表的id") @RequestParam(value = "id") String id,
                              @ApiParam("时间") @RequestParam(value = "timeRange", required = false) String timeRange,
                              @ApiParam("折线图展示类型") @RequestParam(value = "showType", required = false) String showType,
                              @ApiParam("词云的类型") @RequestParam(value = "entityType", defaultValue = "keywords", required = false) String entityType,
                              @ApiParam("对比类型主要是针对地图") @RequestParam(value = "contrast", required = false) String contrast)
            throws TRSException {
        User user = UserUtils.getUser();
        SpecialCustomChart customChart = specialCustomChartService.findOneSpecialCustomChart(id);
        if (ObjectUtil.isEmpty(customChart)) {
            throw new TRSException("当前自定义图表不存在");
        }
        Object result = specialCustomChartService.selectChartData(customChart, timeRange,showType, entityType, contrast);
        return result;
    }


}
