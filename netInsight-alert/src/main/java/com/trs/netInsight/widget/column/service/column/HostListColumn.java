package com.trs.netInsight.widget.column.service.column;

import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.RedisUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.column.factory.AbstractColumn;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.user.entity.User;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 热点栏目列表
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年4月8日
 */
@Slf4j
public class HostListColumn extends AbstractColumn {

    @Override
    public Object getColumnData(String timeRange) throws TRSSearchException {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = null;
        //用queryCommonBuilder和QueryBuilder 是一样的的
        QueryCommonBuilder builder = super.config.getCommonBuilder();
        try {
            builder.setPageSize(super.config.getMaxSize());
            String uid = UUID.randomUUID().toString();
            RedisUtil.setString(uid, builder.asTRSL());
            builder.setKeyRedis(uid);
            String source = super.config.getIndexTab().getGroupName();
            PagedList<FtsDocumentCommonVO> pagedList = commonListService.queryPageListForHotNoFormat(builder, "column", source);
            if (pagedList == null || pagedList.getPageItems() == null || pagedList.getPageItems().size() == 0) {
                return null;
            }
            List<FtsDocumentCommonVO> voList = pagedList.getPageItems();
            for (FtsDocumentCommonVO vo : voList) {
                map = new HashMap<>();
                map.put("trslk", uid);
                String groupName = CommonListChartUtil.formatPageShowGroupName(vo.getGroupName());
                map.put("id", vo.getSid());
                if (Const.PAGE_SHOW_WEIXIN.equals(groupName)) {
                    map.put("id", vo.getHkey());
                }
                map.put("groupName", groupName);
                map.put("time", vo.getUrlTime());
                map.put("md5", vo.getMd5Tag());
                String title = vo.getTitle();
                if (StringUtil.isNotEmpty(title)) {
                    title = StringUtil.replacePartOfHtml(StringUtil.cutContentByFont(StringUtil.replaceImg(title), Const.CONTENT_LENGTH));
                }
                map.put("title", title);
                String content = "";
                if (StringUtil.isNotEmpty(vo.getContent())) {
                    content = StringUtil.cutContentByFont(StringUtil.replaceImg(vo.getContent()), Const.CONTENT_LENGTH);
                }
                if (StringUtil.isNotEmpty(vo.getAbstracts())) {
                    vo.setAbstracts(StringUtil.cutContentByFont(StringUtil.replaceImg(vo.getAbstracts()), Const.CONTENT_LENGTH));
                }
                if("1".equals(super.config.getIndexTab().getKeyWordIndex())){
                    //摘要
                    map.put("abstracts", vo.getContent());
                }else{
                    //摘要
                    map.put("abstracts", vo.getAbstracts());
                }

                map.put("nreserved1", null);
                map.put("hkey", null);
                if (Const.PAGE_SHOW_LUNTAN.equals(groupName)) {
                    map.put("nreserved1", vo.getNreserved1());
                    map.put("hkey", vo.getHkey());
                }
                map.put("urlName", vo.getUrlName());
                map.put("siteName", vo.getSiteName());
                map.put("author", vo.getAuthors());
                //微博、Facebook、Twitter、短视频等没有标题，应该用正文当标题
                if (Const.PAGE_SHOW_WEIBO.equals(groupName)) {
                    map.put("title", content);
                    map.put("abstracts", content);

                    map.put("author", vo.getScreenName());
                } else if (Const.PAGE_SHOW_FACEBOOK.equals(groupName) || Const.PAGE_SHOW_TWITTER.equals(groupName)) {
                    map.put("title", content);
                    map.put("abstracts", content);
                    map.put("author", vo.getAuthors());
                } else if (Const.PAGE_SHOW_DUANSHIPIN.equals(groupName) || Const.PAGE_SHOW_CHANGSHIPIN.equals(groupName)) {
                    map.put("title", content);
                    map.put("abstracts", content);
                    map.put("author", vo.getAuthors());
                }
                map.put("commtCount", vo.getCommtCount());
                map.put("rttCount", vo.getRttCount());
                map.put("simNum", String.valueOf(vo.getSimCount()-1));
                // 获得时间差,三天内显示时间差,剩下消失urltime
                Map<String, String> timeDifference = DateUtil.timeDifference(vo);
                boolean isNew = false;
                if (ObjectUtil.isNotEmpty(timeDifference.get("timeAgo"))) {
                    isNew = true;
                    map.put("timeAgo", timeDifference.get("timeAgo"));
                } else {
                    map.put("timeAgo", timeDifference.get("urlTime"));
                }
                map.put("isNew", isNew);
                list.add(map);
            }
        } catch (TRSException | TRSSearchException e) {
            throw new TRSSearchException("HotColumn error:" + e);
        }
        return list;
    }

    @Override
    public Object getColumnCount() throws TRSSearchException {
        //用queryCommonBuilder和QueryBuilder 是一样的的
        QueryCommonBuilder builder = super.config.getCommonBuilder();
        // 选择数据库
        //selectDatabase(builder);
        String uid = UUID.randomUUID().toString();
        RedisUtil.setString(uid, builder.asTRSL());
        String source = super.config.getIndexTab().getGroupName();
        ;
        long countCommon = 0L;
        try {
            countCommon = commonListService.ftsCount(builder, false, false, false, "column", source);
        } catch (TRSException e) {
            throw new TRSSearchException(e);
        }
        return countCommon;
    }


    @Override
    public Object getSectionList() throws TRSSearchException {
        User user = UserUtils.getUser();
        //用queryCommonBuilder和QueryBuilder 是一样的的
        QueryCommonBuilder builder = super.config.getCommonBuilder();
        String checkGroupName = super.config.getGroupName();
        if ("ALL".equals(checkGroupName)) {
            checkGroupName = super.config.getIndexTab().getGroupName();
        }
        try {
            return commonListService.queryPageListForHot(builder, checkGroupName, user, "column", true);
        } catch (TRSException e) {
            throw new TRSSearchException(e);
        }
    }

    @Override
    public Object getAppSectionList(User user) throws TRSSearchException {
        return null;
    }

    /**
     * 信息列表统计 - 但是页面上的信息列表统计不受栏目类型影响，所以只需要用普通列表的这个方法即可
     * 对应为信息列表的数据源条数统计
     *
     * @return
     * @throws TRSSearchException
     */
    @Override
    public Object getListStattotal() throws TRSSearchException {
        return null;
    }

    @Override
    public QueryBuilder createQueryBuilder() {
        QueryBuilder builder = super.config.getQueryBuilder();
        return builder;

    }

    @Override
    public QueryCommonBuilder createQueryCommonBuilder() {
        QueryCommonBuilder builder = super.config.getCommonBuilder();
        return builder;
    }

}
