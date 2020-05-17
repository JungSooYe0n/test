package com.trs.netInsight.support.api.service.impl;

import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.api.service.IApiService;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * API业务层接口实现类
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/4/10 18:39.
 * @desc
 */
@Service
public class ApiServiceImpl implements IApiService {
    @Autowired
    private FullTextSearch hybase8SearchService;
    @Override
    public PagedList<FtsDocumentCommonVO> expertSearch(String trsl, String sources, String sort,String time, String emotion,String invitationCard,String forwarPrimary,int pageNo, int pageSize) throws OperationException {
        QueryCommonBuilder commonBuilder = new QueryCommonBuilder();
        commonBuilder.page(pageNo,pageSize);
        commonBuilder.filterByTRSL(trsl);
        commonBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time),
                Operator.Between);
        if (!"ALL".equals(emotion)) { // 情感
            commonBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
        }
        // 选择来源库
        if (StringUtils.isNotBlank(sources) && !sources.equals("ALL")) {
            String[] split = sources.split(";");
            String[] databases = TrslUtil.chooseDatabases(split);
            commonBuilder.setDatabase(databases);
        } else {// 数据源全选的时候
            if (StringUtil.isNotEmpty(sources) || "ALL".equals(sources)){
                sources = Const.ALL_GROUP;
            }
            commonBuilder.setDatabase(Const.MIX_DATABASE.split(";"));
        }
        //1、原发/转发
        String weiboTrsl = FtsFieldConst.FIELD_GROUPNAME + ":(\"微博\")";
        String weiboCountTrsl = FtsFieldConst.FIELD_GROUPNAME + ":(\"微博\")";
        if ("primary".equals(forwarPrimary)) {
            // 原发
            weiboTrsl = weiboTrsl + " AND "+ Const.PRIMARY_WEIBO;
            weiboCountTrsl = weiboCountTrsl + " AND "+ Const.PRIMARY_WEIBO;
        }else  if ("forward".equals(forwarPrimary)){
            //转发
            weiboTrsl = weiboTrsl + " NOT "+ Const.PRIMARY_WEIBO;
            weiboCountTrsl = weiboCountTrsl + " NOT "+ Const.PRIMARY_WEIBO;
        }
        //2、主贴/回帖
        String lunTanTrsl = FtsFieldConst.FIELD_GROUPNAME + ":(\"国内论坛\")";
        String lunTanCountTrsl = FtsFieldConst.FIELD_GROUPNAME + ":(\"国内论坛\")";
        if ("0".equals(invitationCard)){
            //主贴
            lunTanTrsl = lunTanTrsl + " AND " + FtsFieldConst.FIELD_NRESERVED1 + ":(0 OR \"\")";
            lunTanCountTrsl = lunTanCountTrsl + " AND " + FtsFieldConst.FIELD_NRESERVED1 + ":(0 OR \"\")";
        }else if ("1".equals(invitationCard)){
            lunTanTrsl = lunTanTrsl + " AND " + FtsFieldConst.FIELD_NRESERVED1 + ":" +invitationCard;
            lunTanCountTrsl = lunTanCountTrsl + " AND " + FtsFieldConst.FIELD_NRESERVED1 + ":" +invitationCard;
        }

        //3、除去论坛 和 微博
        if (StringUtil.isNotEmpty(forwarPrimary) && StringUtil.isNotEmpty(invitationCard)){
            sources = sources.replaceAll("微博;","").replaceAll(";微博","").replaceAll("微博","").replaceAll("国内论坛;","").replaceAll(";国内论坛","").replaceAll("国内论坛","");
        }else if (StringUtil.isNotEmpty(forwarPrimary)){
            sources = sources.replaceAll("微博;","").replaceAll(";微博","").replaceAll("微博","");
        }else if (StringUtil.isNotEmpty(invitationCard)){
            sources = sources.replaceAll("国内论坛;","").replaceAll(";国内论坛","").replaceAll("国内论坛","");
        }
        // 增加具体来源
        if (StringUtils.isNotBlank(sources) && "ALL".equals(sources)) {
            if (StringUtils.isNotBlank(sources)) {
                sources = sources.replaceAll(";", " OR ");
                if (sources.endsWith("OR ")) {
                    sources = sources.substring(0, sources.lastIndexOf("OR"));
                }
            }
        }
        sources = sources.replace("微信", "国内微信").replace("境外媒体", "国外新闻");
        String exGForOther = FtsFieldConst.FIELD_GROUPNAME + ":("+sources+")";
        if (StringUtil.isNotEmpty(forwarPrimary) && StringUtil.isNotEmpty(invitationCard)){
            String zongTrsl = "("+weiboTrsl + ") OR (" + lunTanTrsl + ")";

            if (StringUtil.isNotEmpty(sources)){
                zongTrsl += " OR (" + exGForOther+")";
            }

            commonBuilder.filterByTRSL(zongTrsl);
        }else if (StringUtil.isNotEmpty(forwarPrimary)){
            String twoTrsl = "("+weiboTrsl + ")";

            if (StringUtil.isNotEmpty(sources)){
                twoTrsl += " OR (" + exGForOther+")";
            }

            commonBuilder.filterByTRSL(twoTrsl);
        }else if (StringUtil.isNotEmpty(invitationCard)){
            String twoTrsl1 = "("+lunTanTrsl + ")";

            if (StringUtil.isNotEmpty(sources)){
                twoTrsl1 += " OR (" + exGForOther+")";
            }

            commonBuilder.filterByTRSL(twoTrsl1);
        }else if (StringUtil.isNotEmpty(sources)){
            commonBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, sources, Operator.Equal);
        }

        switch (sort) { // 排序
            case "desc":
                commonBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
                break;
            case "asc":
                commonBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
                break;
            case "hot":
                return getHotFtsDocumentVO(commonBuilder);
            case "relevance":// 相关性排序
                commonBuilder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
                break;
            default:
                commonBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
                commonBuilder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
                break;
        }
        PagedList<FtsDocumentCommonVO> ftsDocumentCommonVOPagedList = null;
        try {
            ftsDocumentCommonVOPagedList = hybase8SearchService.pageListCommon(commonBuilder, false, false,false,null);
        } catch (TRSException e) {
            e.printStackTrace();
        }
        return ftsDocumentCommonVOPagedList;
    }

    public PagedList<FtsDocumentCommonVO> getHotFtsDocumentVO(QueryCommonBuilder commonBuilder){
        QueryBuilder builder = new QueryBuilder();
        String asTRSL = commonBuilder.asTRSL();
        builder.filterByTRSL(asTRSL);
        int pageSize = commonBuilder.getPageSize();
        if (pageSize > 50){
            pageSize = 50;
        }
        builder.page(0,pageSize);
        String[] database = commonBuilder.getDatabase();
        GroupResult groupInfos = hybase8SearchService.categoryQuery(builder, false, false,false, FtsFieldConst.FIELD_MD5TAG, null,database);
        List<FtsDocumentCommonVO> commonVOS = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(groupInfos)){
            List<GroupInfo> groupList = groupInfos.getGroupList();
            for (GroupInfo groupInfo : groupList) {
                QueryBuilder builder1 = new QueryBuilder();
                builder1.filterByTRSL(asTRSL);
                builder1.page(0,1);
                builder1.filterField(FtsFieldConst.FIELD_MD5TAG,groupInfo.getFieldValue(),Operator.Equal);
                builder1.orderBy(FtsFieldConst.FIELD_URLTIME,true);
                try {
                    List<FtsDocumentCommonVO> ftsDocumentCommonVOS = hybase8SearchService.ftsQuery(builder1, FtsDocumentCommonVO.class, false,false, false,null);
                    if (ObjectUtil.isNotEmpty(ftsDocumentCommonVOS)){
                        FtsDocumentCommonVO ftsDocumentCommonVO = ftsDocumentCommonVOS.get(0);
                        commonVOS.add(ftsDocumentCommonVO);
                    }
                } catch (TRSException e) {
                    e.printStackTrace();
                }
            }
        }
        // 返回前端总页数
       int  pageListNo = commonVOS.size() % pageSize == 0 ? commonVOS.size() / pageSize
                : commonVOS.size() / pageSize + 1;
       int  pageListSize = commonVOS.size();
        PagedList<FtsDocumentCommonVO> pagedList = new PagedList<FtsDocumentCommonVO>(pageListNo,
                (int) (pageSize < 0 ? 15 : pageSize), pageListSize, commonVOS, 1);
        return pagedList;
    }
}
