package com.trs.netInsight.widget.home.service;

import java.util.List;


import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.entity.FtsDocument;
import com.trs.netInsight.support.fts.entity.FtsDocumentStatus;
import com.trs.netInsight.support.fts.entity.FtsDocumentWeChat;
import com.trs.netInsight.widget.analysis.entity.ClassInfo;
import com.trs.netInsight.widget.home.entity.enums.ColumnType;
import com.trs.netInsight.widget.home.entity.enums.TabType;
import com.trs.netInsight.widget.special.entity.SpecialProject;

/**
 * 首页
 *
 * Created by trs on 2017/6/19.
 */
public interface IHomeService {


	/**
	 * 首页进入详情页
	 * @param keywords 关键词
	 * @param start 页数
	 * @param end 每页几条
	 * @param timeArray 时间数组
	 * @return
	 * @throws TRSSearchException
	 * @throws TRSException
	 * CreatedBy Xiaoying
	 */
	public Object hsInfo(String keywords,int start,int end,String[] timeArray) throws TRSSearchException, TRSException ;
	public Object save(String columnName, ColumnType type, String tabKeywords, TabType tabType, int position, String keywords) throws TRSException ;
	/**
	 * 热搜防止查询结果为空
	 * @param trsl 表达式
	 * @param timeRange 开始结束时间以;隔开字符串
	 * @param end 条数
	 * @return
	 * @throws TRSSearchException
	 * @throws TRSException
	 * CreatedBy Xiaoying
	 */
	public List<FtsDocument> notNull(String trsl,String timeRange,int end) throws TRSSearchException, TRSException;

	/**
	 * 首页热搜列表
	 * @throws TRSException 
	 * @throws TRSSearchException 
	 */
	public Object homeHsInfo(String keyword,int end,int start, String timeRange,String resultKeyword) throws TRSSearchException, TRSException ;
	/**
	 * 首页未知探索
	 * @throws TRSException 
	 * @throws TRSSearchException 
	 */
	public Object homeKnowInfo(String timeRange,int start,int end,String keyword,String resultKeyword) throws TRSSearchException, TRSException;
	/**
	 * 相似文章列表页   不排重
	 * @param md5
	 * @return
	 * @throws TRSSearchException
	 * @throws TRSException
	 */
	public Object simList(String md5,int start,int end,String resultKeyword) throws TRSSearchException, TRSException ;
	/**
	 * 首页地域列表
	 * @throws TRSException 
	 * @throws TRSSearchException 
	 */
	public Object homeAreaInfo(String area,String timeRange,int start,int end,String groupName,String resultKeyword) throws TRSSearchException, TRSException ;
	/**
	 * 查询相似文章数 传统库
	 */
	public List<FtsDocument> simCount(List<FtsDocument> ftsQuery,String resultKeyword) ;
	
	/**
	 * 查询相似文章数 微博库
	 */
	public List<FtsDocumentStatus> simCountWeibo(List<FtsDocumentStatus> ftsQuery,String resultKeyword) ;
	
	/**
	 * 查询相似文章数 微信库
	 */
	public List<FtsDocumentWeChat> simCountWeiXin(List<FtsDocumentWeChat> ftsQuery,String resultKeyword) ;
	/**
	 * 在结果中搜索  标题  摘要  正文  关键词 传统库
	 * @return
	 * CreatedBy xiaoying
	 */
	public List<FtsDocument> findInResult(List<FtsDocument> list,String keyWord);
	
	/**
	 *  在结果中搜索  标题  摘要  正文  关键词 微博库
	 * @param list hybase查询结果
	 * @param keyWord 结果中搜索的关键词
	 * @return
	 * CreatedBy xiaoying
	 */
	public List<FtsDocumentStatus> findInResultWeiBo(List<FtsDocumentStatus> list, String keyWord);
	
	/**
	 *  在结果中搜索  标题  摘要  正文  关键词 微信库
	 * @param list hybase查询结果
	 * @param keyWord 结果中搜索的关键词
	 * @return
	 * CreatedBy xiaoying
	 */
	public List<FtsDocumentWeChat> findInResultWeiXin(List<FtsDocumentWeChat> list, String keyWord);
	/**
	 * 关键词中搜索
	 * @param ftsQuery
	 * @param trsl
	 * @return
	 */
	public boolean findInKeyword(List<String> keyWordList,String keyWord) ;
	/**
	 * 过滤img标签   现在不搞图片就没用了
	 * @param ftsQuery
	 * @param trsl
	 * @return
	 * CreatedBy xiaoying
	 */
	public List<FtsDocument> noImg(List<FtsDocument> ftsQuery,String trsl);
	/**
	 * 过滤gif的方法  现在不搞图片就没用了
	 * @param mid 文章Id
	 * @param imageContent1 文章内容
	 * @param split1
	 * @param responseCode
	 * @param group
	 * @return
	 * CreatedBy xiaoying
	 */
	public String noGif(int mid,String imageContent1,String[] split1,int responseCode,String group);


	/**
	 * 获取新闻榜单
	 *
	 * @return Object
	 * @throws TRSException TRSException
	 */
	public Object newsFocus(int limit, String trsl) throws TRSException ;

	public Object trend(List<SpecialProject> findByUserId) throws TRSSearchException, OperationException ;
	public Object specialTrend(String start, String end) throws Exception ;

	/**
	 * 首页（热点）信息列表
	 *
	 * @param columnId
	 * @param limit
	 * @return
	 * @throws OperationException
	 */
	public Object hotInfo(String columnId, int limit) throws OperationException;
	
	public  Object areaCount(String grouName,QueryBuilder queryBuilder,String[] timeArray) throws TRSException ;

	/**
	 * 首页微博热点列表
	 *
	 * @param columnId
	 * @return
	 * @throws com.trs.dc.entity.TRSException
	 * 库名要改
	 * @throws TRSTRSSearchException 
	 * @throws TRSSearchException 
	 */
	public Object weibo(String columnId) throws com.trs.dc.entity.TRSException, TRSSearchException, TRSException;
	/**
	 * 查找图片方法  现在不搞图片就没用了
	 * CreatedBy xiaoying
	 */
	public  List<SpecialProject> specialImg(List<SpecialProject> list2) throws TRSSearchException, TRSException ;
	/**
	 * 查找图片方法  现在不搞图片就没用了
	 * CreatedBy xiaoying
	 */
	public  SpecialProject specialIdImg(SpecialProject special) throws TRSSearchException, TRSException ;

	/**
	 * 全网搜索 解析
	 * @param keyWords
	 * @return
	 * @throws OperationException,TRSSearchException
	 */
	public List<ClassInfo> netAnalysis(String keyWords) throws OperationException,TRSSearchException;
}
