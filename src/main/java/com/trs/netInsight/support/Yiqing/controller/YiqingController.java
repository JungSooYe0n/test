package com.trs.netInsight.support.Yiqing.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.Yiqing.entity.Yiqing;
import com.trs.netInsight.support.Yiqing.service.IYiqingService;
import com.trs.netInsight.support.api.entity.ApiAccessToken;
import com.trs.netInsight.support.api.handler.Api;
import com.trs.netInsight.support.api.result.ApiCommonResult;
import com.trs.netInsight.support.api.result.ApiResultType;
import com.trs.netInsight.support.api.service.IApiService;
import com.trs.netInsight.support.api.utils.constance.ApiMethod;
import com.trs.netInsight.support.cache.EnableRedis;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.support.fts.entity.FtsDocumentStatus;
import com.trs.netInsight.support.fts.entity.StatusUser;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.support.hybaseShard.entity.HybaseShard;
import com.trs.netInsight.support.hybaseShard.service.IHybaseShardService;
import com.trs.netInsight.support.knowledgeBase.entity.KnowledgeBase;
import com.trs.netInsight.support.knowledgeBase.entity.KnowledgeClassify;
import com.trs.netInsight.support.knowledgeBase.service.IKnowledgeBaseService;
import com.trs.netInsight.support.redis.RedisOperator;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.alert.entity.repository.AlertRepository;
import com.trs.netInsight.widget.analysis.controller.ChartAnalyzeController;
import com.trs.netInsight.widget.analysis.controller.SpecialChartAnalyzeController;
import com.trs.netInsight.widget.analysis.service.IDistrictInfoService;
import com.trs.netInsight.widget.analysis.service.impl.ChartAnalyzeService;
import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.factory.AbstractColumn;
import com.trs.netInsight.widget.column.factory.ColumnConfig;
import com.trs.netInsight.widget.column.factory.ColumnFactory;
import com.trs.netInsight.widget.column.service.IColumnService;
import com.trs.netInsight.widget.column.service.IIndexPageService;
import com.trs.netInsight.widget.column.service.IIndexTabService;
import com.trs.netInsight.widget.microblog.constant.MicroblogConst;
import com.trs.netInsight.widget.microblog.entity.SingleMicroblogData;
import com.trs.netInsight.widget.microblog.entity.SpreadObject;
import com.trs.netInsight.widget.microblog.service.ISingleMicroblogDataService;
import com.trs.netInsight.widget.report.entity.repository.FavouritesRepository;
import com.trs.netInsight.widget.special.controller.InfoListController;
import com.trs.netInsight.widget.special.service.IInfoListService;
import com.trs.netInsight.widget.special.service.ISpecialService;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.service.IUserService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * openApi暴露接口,接口内部无须处理异常,直接抛出
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年7月17日
 */
@Slf4j
@RestController
@RequestMapping("/yq")
public class YiqingController {

    @Autowired
    private IYiqingService iYiqingService;

    /**
     * 获取pageList
     *
     * @param request
     * @return
     * @Return : Object
     * http://localhost:28088/netInsight/yq/getTxt?name=403
     */
    @FormatResult
    @GetMapping(value = "/getTxt")
    public Object readFile(@RequestParam(value = "name") String name, HttpServletRequest request) {
        iYiqingService.readTxt(name,null);
        return "ok";
    }

    /**
     * http://localhost:28088/netInsight/yq/getData?name=403
     * @param name
     * @return
     */
    @FormatResult
    @GetMapping("/getData")
    public Object getData(@RequestParam(value = "name") String name) {
        Yiqing yiqing = iYiqingService.getData(name);
        if(yiqing!=null){
            JSONObject all = new JSONObject();
            all.put("name",yiqing.getName());
            JSONObject jsonObject = JSON.parseObject(yiqing.getValue());
            all.put("data",jsonObject);
            return all;
        }
        return null;
    }

}
