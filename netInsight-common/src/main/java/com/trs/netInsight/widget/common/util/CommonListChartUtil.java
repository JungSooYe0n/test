package com.trs.netInsight.widget.common.util;

import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.support.fts.model.result.IQueryBuilder;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.analysis.entity.CategoryBean;
import com.trs.netInsight.widget.report.util.ReportUtil;
import com.trs.netInsight.widget.special.entity.InfoListResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;

import java.util.*;

public class CommonListChartUtil {

    /**
     * 统一转化querybuilder
     * @param builder
     * @return
     */
    public static  <T extends IQueryBuilder> QueryBuilder formatQueryBuilder(T builder){
        QueryBuilder queryBuilder = new QueryBuilder();
        if (builder instanceof QueryBuilder) {
            QueryBuilder newBuilder = (QueryBuilder) builder;
            queryBuilder.filterByTRSL(newBuilder.asTRSL());
            queryBuilder.setDatabase(newBuilder.getDatabase());
            queryBuilder.setOrderBy(newBuilder.getOrderBy());
            queryBuilder.setStartTime(newBuilder.getStartTime());
            queryBuilder.setEndTime(newBuilder.getEndTime());
            queryBuilder.page(newBuilder.getPageNo(),newBuilder.getPageSize());
            queryBuilder.setKeyRedis(newBuilder.getKeyRedis());
            queryBuilder.setOrderBy(newBuilder.getOrderBy());
        } else if (builder instanceof QueryCommonBuilder) {
            QueryCommonBuilder commonBuilder = (QueryCommonBuilder) builder;
            queryBuilder.filterByTRSL(commonBuilder.asTRSL());
            if(commonBuilder.getDatabase() != null && commonBuilder.getDatabase().length >0){
                queryBuilder.setDatabase(StringUtil.join(commonBuilder.getDatabase(), ";"));
            }
            queryBuilder.setOrderBy(commonBuilder.getOrderBy());
            queryBuilder.setStartTime(commonBuilder.getStartTime());
            queryBuilder.setEndTime(commonBuilder.getEndTime());
            queryBuilder.page(commonBuilder.getPageNo(),commonBuilder.getPageSize());
            queryBuilder.setKeyRedis(commonBuilder.getKeyRedis());
            queryBuilder.setOrderBy(commonBuilder.getOrderBy());
        }
        queryBuilder.setPageSize(queryBuilder.getPageSize()>=1?queryBuilder.getPageSize():10);
        queryBuilder.setPageNo(queryBuilder.getPageNo()>=0?queryBuilder.getPageNo():0);
        String database = queryBuilder.getDatabase();
        if (database == null) {
            String[] groupNameArr = TrslUtil.getGroupNameByTrsl(queryBuilder.asTRSL());
            queryBuilder.setDatabase(StringUtils.join(groupNameArr,";"));
        }
        return queryBuilder;
    }

    /**
     * 统一转化querybuilder
     * @param builder
     * @return
     */
    public static  <T extends IQueryBuilder> QueryCommonBuilder formatQueryCommonBuilder(T builder){
        QueryCommonBuilder queryBuilder = new QueryCommonBuilder();
        if (builder instanceof QueryBuilder) {
            QueryBuilder formatBuilder = (QueryBuilder)builder;
            queryBuilder.filterByTRSL(formatBuilder.asTRSL());
            if(StringUtil.isNotEmpty(formatBuilder.getDatabase())){
                queryBuilder.setDatabase(formatBuilder.getDatabase().split(";"));
            }
            queryBuilder.setOrderBy(formatBuilder.getOrderBy());
            queryBuilder.setStartTime(formatBuilder.getStartTime());
            queryBuilder.setEndTime(formatBuilder.getEndTime());
            queryBuilder.page(formatBuilder.getPageNo(),formatBuilder.getPageSize());
            queryBuilder.setKeyRedis(formatBuilder.getKeyRedis());
            queryBuilder.setOrderBy(formatBuilder.getOrderBy());
        } else if (builder instanceof QueryCommonBuilder) {
            QueryCommonBuilder formatBuilder = (QueryCommonBuilder) builder;
            queryBuilder.filterByTRSL(formatBuilder.asTRSL());
            queryBuilder.setDatabase(formatBuilder.getDatabase());
            queryBuilder.setOrderBy(formatBuilder.getOrderBy());
            queryBuilder.setStartTime(formatBuilder.getStartTime());
            queryBuilder.setEndTime(formatBuilder.getEndTime());
            queryBuilder.page(formatBuilder.getPageNo(),formatBuilder.getPageSize());
            queryBuilder.setKeyRedis(formatBuilder.getKeyRedis());
            queryBuilder.setOrderBy(formatBuilder.getOrderBy());
        }
        queryBuilder.setPageSize(queryBuilder.getPageSize()>=1?queryBuilder.getPageSize():10);
        queryBuilder.setPageNo(queryBuilder.getPageNo()>=0?queryBuilder.getPageNo():0);
        String[] database = queryBuilder.getDatabase();
        if (database == null || database.length == 0) {
            String[] groupNameArr = TrslUtil.getGroupNameByTrsl(queryBuilder.asTRSL());
            queryBuilder.setDatabase(groupNameArr);
        }
        return queryBuilder;
    }


    /**
     * 重载 为栏目模块的xy轴弄的
     *
     * @param trsl
     * @return
     */
    public static List<CategoryBean> getMediaType(String trsl) {
        List<CategoryBean> list = new ArrayList<>();
        String newXyTrsl = trsl.replaceAll("\n", "");
        String[] medias = newXyTrsl.split("[;|；]");
        if (medias.length > 0) {
            for (String str : medias) {
                if (StringUtils.isNotBlank(str)) {
                    String key = str.substring(0, str.indexOf("="));
                    String val = str.substring(str.indexOf("=") + 1, str.length());
                    list.add(new CategoryBean(key, val));
                }
            }
        }
        return list;
    }

    public static List<String> formatGroupName(String groupName) throws TRSSearchException{
        if("ALL".equals(groupName)){
            groupName = Const.ALL_GROUP;
        }
        String[] split = groupName.split("[;|；]");
        List<String> sourceList = new ArrayList<>();
        for (String str : split) {
            if (Const.SOURCE_GROUPNAME_CONTRAST.containsKey(str)) {
                String group = Const.SOURCE_GROUPNAME_CONTRAST.get(str);
                if( !sourceList.contains(group)){
                    sourceList.add(group);
                }
            } else if("传统媒体".equals(str)){
                str = Const.TYPE_NEWS;
                String[] newsArr = str.split(";");
                for(String news:newsArr){
                    if (Const.SOURCE_GROUPNAME_CONTRAST.containsKey(news)) {
                        String group = Const.SOURCE_GROUPNAME_CONTRAST.get(news);
                        if( !sourceList.contains(group)){
                            sourceList.add(group);
                        }
                    }
                }
            }
        }
        return sourceList;
    }


    public static <T extends IQueryBuilder> Object addGroupNameForQueryBuilder(T builder,String allGroupName,Integer type)throws TRSException{
        if(StringUtil.isEmpty(allGroupName)){
            throw new TRSException("没有传入要查询的groupName");
        }
        QueryBuilder queryBuilder = CommonListChartUtil.formatQueryBuilder(builder);
        QueryCommonBuilder queryCommonBuilder = CommonListChartUtil.formatQueryCommonBuilder(builder);
        List<String> commonSource = CommonListChartUtil.formatGroupName(allGroupName);
        if(commonSource.size() >0){
            String[] groupArray = commonSource.toArray(new String[commonSource.size()]);
            String groupTrsl = "(" + StringUtil.join(groupArray, " OR ") + ")";
            String trsl = queryBuilder.asTRSL();
            if(!trsl.contains(groupTrsl)){
                queryBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupTrsl, Operator.Equal);
                queryCommonBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupTrsl, Operator.Equal);
            }
            String[] database = TrslUtil.chooseDatabases(groupArray);
            queryBuilder.setDatabase(StringUtil.join(database, ";"));
            queryCommonBuilder.setDatabase(database);
            if(type == 0){
                return queryBuilder;
            }else if(type == 1){
                return  queryCommonBuilder;
            }else{
                return null;
            }
        }else{
            return null;
        }
    }

    /**
     *
     * 转化数据源格式
     * 将各种样子的数据源类型改为同hybase中相同的数据源值
     * @param groupName
     * @return
     */
    public static String changeGroupName(String groupName){
        if(StringUtil.isEmpty(groupName)){
            return "";
        }
        List<String> list = CommonListChartUtil.formatGroupName(groupName);
        if(list != null && list.size() >0){
            return StringUtils.join(list,";");
        }else{
            return null;
        }
    }

    /**
     *
     * 转化数据源格式
     * 将数据源转成页面中显示的
     * @param groupName
     * @return
     */
    public static String formatPageShowGroupName(String groupName){
        if(StringUtil.isEmpty(groupName)){
            return "";
        }
        List<String> list = CommonListChartUtil.formatGroupName(groupName);
        if(list != null && list.size() >0){
            List<String> result = new ArrayList<>();
            for(String group:list){
                group =  Const.PAGE_SHOW_GROUPNAME_CONTRAST.get(group);
                if(StringUtil.isNotEmpty(group)){
                    result.add(group);
                }
            }
            if(result != null && result.size() >0){
                return StringUtils.join(result,";");
            }else{
                return null;
            }
        }else{
            return null;
        }
    }
    public static String StringShowGroupName(String str) {
    return str.replaceAll("\"groupName\":\"国内微信\"","\"groupName\":\"微信\"").replaceAll("\"groupName\":\"国内新闻_手机客户端\"","\"groupName\":\"新闻app\"").replaceAll("\"groupName\":\"国内新闻\"","\"groupName\":\"新闻网站\"").replaceAll("\"groupName\":\"国内论坛\"","\"groupName\":\"论坛\"").replaceAll("\"groupName\":\"国外新闻\"","\"groupName\":\"境外\"").replaceAll("\"groupName\":\"国内新闻_电子报\"","\"groupName\":\"电子报\"").replaceAll("\"groupName\":\"国内博客\"","\"groupName\":\"博客\"").replaceAll("\"groupName\":\"twitter\"","\"groupName\":\"Twitter\"").replaceAll("\"groupName\":\"FaceBook\"","\"groupName\":\"Facebook\"");
    }


    public static PagedList<Object> formatListData(InfoListResult infoListResult,String trslk,String wordIndex){
        PagedList<Object> resultContent = null;
        List<Object> resultList = new ArrayList<>();
        PagedList<FtsDocumentCommonVO> pagedList = (PagedList<FtsDocumentCommonVO>) infoListResult.getContent();
        if (pagedList != null && pagedList.getPageItems() != null && pagedList.getPageItems().size() > 0) {
            List<FtsDocumentCommonVO> voList = pagedList.getPageItems();
            for (FtsDocumentCommonVO vo : voList) {
                Map<String, Object> map = new HashMap<>();
                String groupName = CommonListChartUtil.formatPageShowGroupName(vo.getGroupName());
                map.put("id", vo.getSid());
                map.put("groupName", groupName);
                map.put("time", vo.getUrlTime());
                map.put("md5", vo.getMd5Tag());
                String title= vo.getTitle();
                map.put("title", StringUtil.replaceImg(title));
                if(StringUtil.isNotEmpty(title)){
                    title = title.replaceAll("<font color=red>", "").replaceAll("</font>", "");
                }
                map.put("copyTitle", title); //前端复制功能需要用到
                if("1".equals(wordIndex)){
                    //摘要
                    map.put("abstracts", StringUtil.replaceImg(vo.getContent()));
                }else{
                    if(StringUtil.isEmpty(wordIndex)){
                        if(StringUtil.isEmpty(vo.getAbstracts())){
                            //摘要
                            map.put("abstracts", StringUtil.replaceImg(vo.getContent()));
                        }else{
                            //摘要
                            map.put("abstracts", StringUtil.replaceImg(vo.getAbstracts()));
                        }
                    }else{
                        //摘要
                        map.put("abstracts", StringUtil.replaceImg(vo.getAbstracts()));
                    }
                }
                if(vo.getKeywords() != null && vo.getKeywords().size() >3){
                    map.put("keyWordes", vo.getKeywords().subList(0,3));
                }else{
                    map.put("keyWordes", vo.getKeywords());
                }
                String voEmotion =  vo.getAppraise();
                if(StringUtil.isNotEmpty(voEmotion)){
                    map.put("emotion",voEmotion);
                }else{
                    map.put("emotion","中性");
                    map.put("isEmotion",null);
                }

                map.put("nreserved1", null);
                map.put("hkey", null);
                if (Const.PAGE_SHOW_LUNTAN.equals(groupName)) {
                    map.put("nreserved1", vo.getNreserved1());
                    map.put("hkey", vo.getHkey());
                }
                map.put("urlName", vo.getUrlName());
                map.put("favourite", vo.isFavourite());
                String fullContent = vo.getExportContent();
                if(StringUtil.isNotEmpty(fullContent)){
                    if(fullContent.indexOf("<font color=red>") != -1){
                        fullContent = ReportUtil.calcuHit("",fullContent,true);
                    }
                    fullContent = StringUtil.replaceImg(StringUtil.replaceFont(fullContent));
                }
                map.put("siteName", vo.getSiteName());
                map.put("author", vo.getAuthors());
                map.put("srcName", vo.getSrcName());
                map.put("imgOcr",false);
                //微博、Facebook、Twitter、短视频等没有标题，应该用正文当标题
                if (Const.PAGE_SHOW_WEIBO.equals(groupName)) {
                    String setContent = vo.getContent();
                    String ocrContent = vo.getOcrContent();
                    if (StringUtil.isNotEmpty(ocrContent)) {
                        map.put("imgOcr",true);
                        ocrContent = StringUtil.replaceImg(ocrContent);
                        fullContent = ocrContent;
                        if(fullContent.indexOf("<font color=red>") != -1){
                            fullContent = ReportUtil.calcuHit("",fullContent,true);
                        }
                        fullContent = StringUtil.replaceImg(StringUtil.replaceFont(fullContent));
                        setContent = StringUtil.cutContentByFont(ocrContent, Const.CONTENT_LENGTH);
                    }
                    map.put("title", StringUtil.replaceImg(setContent));
                    map.put("abstracts", StringUtil.replaceImg(setContent));
                    map.put("copyTitle", fullContent); //前端复制功能需要用到
                    map.put("author", vo.getScreenName());
                    map.put("srcName", vo.getRetweetedScreenName());
                } else if (Const.PAGE_SHOW_FACEBOOK.equals(groupName) || Const.PAGE_SHOW_TWITTER.equals(groupName)) {
                    map.put("title", StringUtil.replaceImg(vo.getContent()));
                    map.put("abstracts", StringUtil.replaceImg(vo.getContent()));
                    map.put("copyTitle", fullContent); //前端复制功能需要用到
                    map.put("author", vo.getAuthors());
                    map.put("srcName", vo.getRetweetedScreenName());
                } else if(Const.PAGE_SHOW_DUANSHIPIN.equals(groupName) || Const.PAGE_SHOW_CHANGSHIPIN.equals(groupName)){
                    if(StringUtil.isEmpty(title)){
                        map.put("title", vo.getContent());
                    }
                    map.put("abstracts", vo.getContent());
                    map.put("author", vo.getAuthors());
                    map.put("srcName", vo.getSrcName());
                    if(StringUtil.isEmpty(title)){
                        map.put("copyTitle", fullContent); //前端复制功能需要用到
                    }
                }
                map.put("trslk", trslk);
                map.put("channel", vo.getChannel());
                map.put("img", null);
                //前端页面显示需要，与后端无关
                map.put("isImg", false);
                map.put("simNum", 0);

                resultList.add(map);
            }
            resultContent = new PagedList<Object>(pagedList.getPageIndex(),
                    pagedList.getPageSize(), pagedList.getTotalItemCount(), resultList, 1);
        }
        return resultContent;
    }



}
