package com.trs.netInsight.widget.column.controller;

import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.ESFieldConst;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.support.fts.entity.FtsRankList;
import com.trs.netInsight.support.fts.entity.FtsRankListHtb;
import com.trs.netInsight.support.fts.entity.FtsRankListRsb;
import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;
import com.trs.netInsight.support.log.handler.Log;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.common.service.ICommonListService;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 热榜操作接口
 *
 * @author 拓尔思信息技术股份有限公司
 * @since gao @ 2021年5月10日
 *
 */
@RestController
@RequestMapping("/hotTop")
@Api(description = "榜单接口")
@Slf4j
public class HotTopController {
    @Autowired
    private ICommonListService commonListService;
    @FormatResult
    @GetMapping(value = "/hotList")
    @ApiOperation("各榜单热度数据接口")
    public Object hotList(@ApiParam("时间区间") @RequestParam(value = "timeRange", required = true) String timeRange,
                                 @ApiParam("榜单类型") @RequestParam(value = "siteName", required = true) String siteName,
                                 @ApiParam("频道类型") @RequestParam(value = "channelName", required = false) String channelName,
                                 @ApiParam("关键词") @RequestParam(value = "keyword", required = false) String keyword) throws TRSException {
        QueryBuilder queryBuilder = new QueryBuilder();
        String[] timeArray = DateUtil.formatTimeRange(timeRange);
//        queryBuilder.filterField(ESFieldConst.IR_URLTIME, timeArray, Operator.Between);
        queryBuilder.filterField(FtsFieldConst.FIELD_LASTTIME, com.trs.netInsight.support.fts.util.DateUtil.formatTimeRangeMinus1(timeRange), Operator.Between);
        queryBuilder.setPageNo(0);
        queryBuilder.setPageSize(50);
        queryBuilder.orderBy("IR_LASTTIME", true);
        if (Const.HTB_SITENAME_ZHIHUHTB.equals(siteName) || Const.HTB_SITENAME_JINRI.equals(siteName)) {
            queryBuilder.orderBy("IR_RANK", false);
        }
        if (ObjectUtil.isNotEmpty(channelName)) queryBuilder.filterField(FtsFieldConst.FIELD_CHANNEL,channelName,Operator.Equal);
        if (ObjectUtil.isNotEmpty(siteName)) queryBuilder.filterField(FtsFieldConst.FIELD_SITENAME,siteName,Operator.Equal);
//        String trsl = "IR_URLTITLE:" + keyword;
//        queryBuilder.filterByTRSL(trsl);
        if (Const.GROUPNAME_WEIBO.equals(siteName)){
            queryBuilder.setDatabase("热搜榜".equals(channelName) ? Const.WEIBO_RSB : Const.WEIBO_HTB);
            if ("热搜榜".equals(channelName)){
                //微博热搜榜
                PagedList<FtsRankListRsb> ftsPageList = commonListService.queryPageListForClass(queryBuilder,FtsRankListRsb.class,false,false,false,"hotTop");
                List<FtsRankListRsb> list = ftsPageList.getPageItems();
                List<FtsRankListRsb> listTemp = new ArrayList();
                for(int i=0;i<list.size();i++){//排重取十条数据
                    boolean isAdd = true;
                    for (int j = 0; j < listTemp.size(); j++) {
                        if(listTemp.get(j).getHotWord().contains(list.get(i).getHotWord())){
                            isAdd = false;
                            break;
                        }
                    }
                    if (isAdd) listTemp.add(list.get(i));
                    if (listTemp.size() > 9) break;

                }
                SortListRsb sortList = new SortListRsb();
                //按时间排序
                Collections.sort(listTemp, sortList);
                List<Object> resultList = new ArrayList<>();
                for (FtsRankListRsb vo : listTemp) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("title",vo.getHotWord());
                    map.put("heat",vo.getDescExtr());
                    resultList.add(map);
                }
                return resultList;
            }else {
                //微博话题榜
//                queryBuilder.orderBy("IR_READNUM", true);
                PagedList<FtsRankListHtb> ftsPageList = commonListService.queryPageListForClass(queryBuilder,FtsRankListHtb.class,false,false,false,"weiboHtb");
                List<FtsRankListHtb> list = ftsPageList.getPageItems();
                SortListHtb sortList = new SortListHtb();
                Collections.sort(list, sortList);
                List<FtsRankListHtb> listTemp = new ArrayList();
                for(int i=0;i<list.size();i++){//排重取十条数据
                    boolean isAdd = true;
                    for (int j = 0; j < listTemp.size(); j++) {
                        if(listTemp.get(j).getTitle().contains(list.get(i).getTitle())){
                            isAdd = false;
                            break;
                        }
                    }
                    if (isAdd) listTemp.add(list.get(i));
                    if (listTemp.size() > 9) break;

                }
                List<Object> resultList = new ArrayList<>();
                for (FtsRankListHtb vo : listTemp) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("title",vo.getTitle());
                    map.put("heat",vo.getReadNum());
                    resultList.add(map);
                }
                return resultList;
            }
        }else {
            queryBuilder.setDatabase(Const.DC_BANGDAN);
            PagedList<FtsRankList> ftsPageList = commonListService.queryPageListForClass(queryBuilder,FtsRankList.class,false,false,false,"hotTop");
            List<FtsRankList> list = ftsPageList.getPageItems();
            List<FtsRankList> listTemp = new ArrayList();
            for(int i=0;i<list.size();i++){//排重取十条数据
                boolean isAdd = true;
                for (int j = 0; j < listTemp.size(); j++) {
                    if(listTemp.get(j).getTitle().contains(list.get(i).getTitle())){
                        isAdd = false;
                      break;
                    }
                }
                if (isAdd) listTemp.add(list.get(i));
                if (listTemp.size() > 9) break;

            }
            if (Const.HTB_SITENAME_BAIDU.equals(siteName) || Const.HTB_SITENAME_360.equals(siteName) || Const.HTB_SITENAME_soudog.equals(siteName)) {
                SortListBangdan sortList = new SortListBangdan();
                Collections.sort(listTemp, sortList);
            }else if (Const.HTB_SITENAME_ZHIHUHTB.equals(siteName) || Const.HTB_SITENAME_JINRI.equals(siteName)){

            }else {
                SortListBangdanHeat sortList = new SortListBangdanHeat();
                //按时间排序
                Collections.sort(listTemp, sortList);
            }

            List<Object> resultList = new ArrayList<>();
            for (FtsRankList vo : listTemp) {
                Map<String, Object> map = new HashMap<>();
                map.put("title",vo.getTitle());
                if (Const.HTB_SITENAME_BAIDU.equals(siteName) || Const.HTB_SITENAME_360.equals(siteName) || Const.HTB_SITENAME_soudog.equals(siteName)) {
                    map.put("heat",vo.getSearchIndex());
                }else if (Const.HTB_SITENAME_ZHIHUHTB.equals(siteName) || Const.HTB_SITENAME_JINRI.equals(siteName)){

                }else {
                    map.put("heat",vo.getHeat());
                }

                resultList.add(map);
            }
            return resultList;
        }



    }
    public static List removeDuplicate(List list){
        List listTemp = new ArrayList();
        for(int i=0;i<list.size();i++){
            if(!listTemp.contains(list.get(i))){
                listTemp.add(list.get(i));
            }
        }
        return listTemp;
    }
}
