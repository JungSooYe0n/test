
package com.trs.netInsight.widget.special.service;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.entity.FtsDocument;
import com.trs.netInsight.support.fts.entity.FtsDocumentStatus;
import com.trs.netInsight.support.fts.entity.FtsDocumentTF;
import com.trs.netInsight.support.fts.entity.FtsDocumentWeChat;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.model.result.IDocument;
import com.trs.netInsight.widget.alert.entity.AlertEntity;
import com.trs.netInsight.widget.analysis.entity.ClassInfo;
import com.trs.netInsight.widget.special.entity.InfoListResult;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.user.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 信息列表
 *
 * Created by ChangXiaoyang on 2017/5/4.
 */
public interface IInfoListService {
	
	/**
	 *  从缓存中获取下一页的信息
	 * @date Created at 2017年11月24日  下午3:59:39
	 * @Author 谷泽昊
	 * @param pageId
	 * @return
	 * @throws TRSException
	 * @throws InterruptedException
	 */
	public InfoListResult<?> getNextList(String pageId) throws TRSException, InterruptedException;
	
	/**
	 * 获取异步数据
	 * @date Created at 2017年11月24日  下午4:00:07
	 * @Author 谷泽昊
	 * @param pageId
	 * @return
	 * @throws TRSException
	 * @throws InterruptedException
	 */
	public List<?> getAsyncList(String pageId) throws TRSException, InterruptedException;

	/**
	 * 获取异步数据 - 相似文章数对应的发文网站信息
	 * @date
	 * @Author
	 * @param pageId
	 * @return
	 * @throws TRSException
	 * @throws InterruptedException
	 */
	public List<?> getAsySiteNameList(String pageId) throws TRSException, InterruptedException;

	/**
	 * 获取异步数据
	 * @date Created at 2017年11月24日  下午4:00:31
	 * @Author 谷泽昊
	 * @param sid
	 * @return
	 * @throws TRSException
	 * @throws InterruptedException
	 */
	public Object getOneAsy(String sid) throws TRSException, InterruptedException;
	
	/**
	 * 为了不改变之前的方法  新写了一个
	 * @date Created at 2017年11月24日  下午4:01:22
	 * @Author 谷泽昊
	 * @param builder
	 * @param specialId
	 * @param userId
	 * @return
	 * @throws TRSException
	 */
//	public InfoListResult<?> getHotListNew(QueryBuilder builder, String specialId,String userId) throws TRSException ;
	
	/**
	 * 将结果按照热度值排序
	 * @date Created at 2017年11月24日  下午4:01:47
	 * @Author 谷泽昊
	 * @param result
	 * @param md5List
	 * @param md5TAG
	 * @return
	 */
	public List<FtsDocument> resultByMd5(List<FtsDocument> result,List<String> md5List,GroupResult md5TAG);
	
	/**
	 * 热度排序
	 * @date Created at 2017年11月24日  下午4:02:24
	 * @Author 谷泽昊
	 * @param builder
	 * @param user
	 * @return
	 * @throws TRSException
	 */
	public InfoListResult<?> getHotList(QueryBuilder builder,QueryBuilder countBuilder,User user,String type) throws TRSException ;

	/**
	 * 获取按热度排序的列表页 - 根据搜索页面
	 * @param builder
	 * @param countBuilder
	 * @param user
	 * @param type
	 * @param searchPage
	 * @return
	 * @throws TRSException
	 */
	public InfoListResult getHotList(QueryBuilder builder, QueryBuilder countBuilder, User user,String type,String searchPage)
			throws TRSException;
	
	/**
	 * 热度排序
	 * @date Created at 2017年11月24日  下午4:02:43
	 * @Author 谷泽昊
	 * @param builder
	 * @param specialId
	 * @return
	 * @throws TRSException
	 */
//	public InfoListResult<?> listByHot(QueryBuilder builder, String specialId) throws TRSException ;
	
	/**
	 * 获取信息列表数据
	 * @date Created at 2017年11月24日  下午4:02:59
	 * @Author 谷泽昊
	 * @param builder
	 * @param user
	 * @param isExport 是否导出（导出就不算相似文章数了）
	 * @return
	 * @throws TRSException
	 * @throws com.trs.netInsight.handler.exception.TRSException 
	 * sim 来自于相似文章列表 true
	 */
	public InfoListResult<FtsDocument> getDocList(QueryBuilder builder,User user,boolean sim,boolean irSimflag,boolean irSimflagAll,boolean isExport,String type) throws TRSException;

	/**
	 * 获取传统媒体信息列表的数据，根据搜索的页面
	 * @param builder
	 * @param user
	 * @param sim
	 * @param irSimflag
	 * @param irSimflagAll
	 * @param isExport
	 * @param type
	 * @param searchPage
	 * @return
	 * @throws TRSException
	 */
	public InfoListResult getDocList(QueryBuilder builder, User user, boolean sim, boolean irSimflag,boolean irSimflagAll,boolean isExport,String type,String searchPage)
			throws TRSException;

	/**
	 * 获取微信 相似文章列表数据
	 * @date Created at 2018年7月24日 
	 * @Author 梁新
	 * @param builder
	 * @param user
	 * @return
	 * @throws TRSException
	 * @throws com.trs.netInsight.handler.exception.TRSException 
	 * sim 来自于相似文章列表 true
	 */
	public InfoListResult<FtsDocument> getHotWechatSimList(QueryBuilder builder,User user,boolean sim,boolean irSimflag,boolean irSimflagAll,String type) throws TRSException;
	
	
	
	/**
	 * 获取信息列表数据(Twitter AND FaceBook ...)
	 * @since changjiang @ 2018年4月23日
	 * @param builder
	 * @param user
	 * @param sim
	 * @return
	 * @throws TRSException
	 * @Return : InfoListResult<FtsDocument>
	 */
	public InfoListResult<FtsDocumentTF> getDocTFList(QueryBuilder builder,User user,boolean sim,boolean irSimflag,boolean irSimflagAll,String type) throws TRSException;

	/**
	 * 获取信息列表数据(Twitter AND FaceBook ...) 根据不同搜索页面去调用
	 * @param builder
	 * @param user
	 * @param sim
	 * @return
	 * @throws TRSException
	 * @Return : InfoListResult<FtsDocument>
	 */
	public InfoListResult<FtsDocumentTF> getDocTFList(QueryBuilder builder,User user,boolean sim,boolean irSimflag,boolean irSimflagAll,String type,String searchPage) throws TRSException;

	/**
	 * 分类对比图跳转
	 * 
	 * @param builder
	 * @param user
	 * @param sim
	 * @return
	 * @throws TRSException
	 */
	public InfoListResult<IDocument> getDocListContrast(QueryBuilder builder,User user,boolean sim,boolean irSimflag,boolean irSimflagAll,String type) throws TRSException;

	/**
	 * 分类对比图跳转混合列表
	 * 
	 * @param builder
	 * @param user
	 * @param sim
	 * @return
	 * @throws TRSException
	 */
	public InfoListResult<IDocument> getDocListContrast(QueryCommonBuilder builder, User user, boolean sim, boolean irSimflag, boolean irSimflagAll, String type) throws TRSException;
	/**
	 * 列表查询 - 混合列表
	 *
	 * @param builder
	 * @param user
	 * @param sim
	 * @return
	 * @throws TRSException
	 */
	public InfoListResult<IDocument> getDocListContrast(QueryCommonBuilder builder, User user, boolean sim,
														boolean irSimflag, boolean irSimflagAll, String type,String searchPage) throws TRSException;
	
	/**
	 * 获取微博数据信息列表
	 * @date Created at 2017年11月29日  下午3:14:37
	 * @Author 谷泽昊
	 * @param builder
	 * @param user 用户
	 * @param sim 
	 * @param irSimflag
	 * @param isExport 是否导出（导出就不算相似文章数了）
	 * @return
	 * @throws TRSException
	 */
	public InfoListResult<FtsDocumentStatus> getStatusList(QueryBuilder builder,User user,boolean sim,boolean irSimflag,boolean irSimflagAll,boolean isExport,String type) throws TRSException;

	/**
	 * 获取微博信息列表页 根据搜索页面
	 * @param builder
	 * @param user
	 * @param sim
	 * @param irSimflag
	 * @param irSimflagAll
	 * @param isExport
	 * @param type
	 * @param searchPage
	 * @return
	 * @throws TRSException
	 */
	public InfoListResult<FtsDocumentStatus> getStatusList(QueryBuilder builder, User user, boolean sim,
														   boolean irSimflag,boolean irSimflagAll,boolean isExport,String type,String searchPage) throws TRSException;
	
	/**
	 * 获取FaceBook AND Twitter信息列表
	 * @since changjiang @ 2018年4月19日
	 * @param builder
	 * @param user
	 * @param sim
	 * @return
	 * @throws TRSException
	 * @Return : InfoListResult<FtsDocumentStatus>
	 */
	public InfoListResult<FtsDocumentTF> getTFList(QueryBuilder builder,User user,boolean sim,String type) throws TRSException;
	
	
	/**
	 * 获取微信信息列表
	 * @date Created at 2017年11月29日  下午3:14:40
	 * @Author 谷泽昊
	 * @param builder
	 * @param user
	 * @param isExport 是否导出（导出就不算相似文章数了）
	 * @return
	 * @throws TRSException
	 */
	public InfoListResult<FtsDocumentWeChat> getWeChatList(QueryBuilder builder,User user,boolean sim,boolean irSimflag,boolean irSimflagAll,boolean isExport,String type) throws TRSException;

	/**
	 * 获取微信信息列表页 - 根据搜索页面
	 * @param builder
	 * @param user
	 * @param sim
	 * @param irSimflag
	 * @param irSimflagAll
	 * @param isExport
	 * @param type
	 * @param searchPage
	 * @return
	 * @throws TRSException
	 */
	public InfoListResult<FtsDocumentWeChat> getWeChatList(QueryBuilder builder, User user, boolean sim,
														   boolean irSimflag,boolean irSimflagAll,boolean isExport,String type,String searchPage) throws TRSException;
	
	/**
	 * 获取舆情报告
	 * @date Created at 2017年11月24日  下午4:03:18
	 * @Author 谷泽昊
	 * @param builder
	 * @param specialId
	 * @param ftsQuery
	 * @param userId
	 * @return
	 * @throws TRSException
	 * @throws SearchException
	 * @throws com.trs.netInsight.handler.exception.TRSException 
	 */
//	public Object getReportList(QueryBuilder builder, String specialId,List<FtsDocument> ftsQuery,String userId) throws TRSException, SearchException ;
	
	/**
	 * 查相似文章信息
	 * @param md5
	 * @return
	 * @throws
	 * @throws TRSException
	 */
	public void simCount(String sid,String md5,String type);

	/**
	 * 实时获取微博转评赞数据
	 */
	public void getRealTimeInfoOfStatus(String urlName,String sid);
	
	/**
	 * 查相似文章信息--微博
	 * @date Created at 2017年12月4日  下午5:11:49
	 * @Author 谷泽昊
	 * @param md5
	 */
	public void simCountStatus(String mid,String md5,String type);
	
	/**
	 * 查相似文章信息--微信
	 * @date Created at 2017年12月4日  下午5:11:52
	 * @Author 谷泽昊
	 * @param md5
	 */
	public void simCountWeChat(String hkey,String md5,String type);

	/**
	 * 查询微博热点
	 * @date Created at 2017年12月4日  上午9:25:30
	 * @Author 谷泽昊
	 * @param builder
	 * @param user
	 * @return
	 * @throws TRSException 
	 */
	public Object getHotListStatus(QueryBuilder builder,QueryBuilder countBuilder, User user,String type) throws TRSException;

	/**
	 * 获取微博热点列表 - 按搜索页面
	 * @param builder
	 * @param countBuilder
	 * @param user
	 * @param type
	 * @param searchPage
	 * @return
	 * @throws TRSException
	 */
	public Object getHotListStatus(QueryBuilder builder, QueryBuilder countBuilder, User user,String type,String searchPage) throws TRSException;
	/**
	 * 查询微信热点
	 * @date Created at 2017年12月4日  上午9:25:30
	 * @Author 谷泽昊
	 * @param builder
	 * @param user
	 * @return
	 * @throws TRSException 
	 */
	public Object getHotListWeChat(QueryBuilder builder,QueryBuilder countBuilder,User user,String type) throws TRSException;

	/**
	 * 获取微信热点列表 - 按搜索页面
	 * @param builder
	 * @param countBuilder
	 * @param user
	 * @param type
	 * @param searchPage
	 * @return
	 * @throws TRSException
	 */
	public Object getHotListWeChat(QueryBuilder builder, QueryBuilder countBuilder, User user,String type,String searchPage) throws TRSException;

	/**
	 * 微信列表
	 * @param specialId 专项id
	 * @param pageNo 第几页
	 * @param pageSize 一页几条
	 * @param source 来源
	 * @param time 时间
	 * @param area 地域
	 * @param industry 行业
	 * @param emotion 情感
	 * @param sort 排序方式
	 * @param keywords 关键词
	 * @param notKeyWords 排除词
	 * @param keyWordIndex 查询位置（标题/内容）
	 * @return
	 */
	public Object weChatSearch(String specialId, int pageNo, int pageSize, String source, String time, String area,
			String industry, String emotion, String sort, String keywords,String fuzzyValueScope,String notKeyWords,String keyWordIndex,String type)throws TRSException;
	
	/**
	 * 微信列表  对于高级搜索是否排重
	 * @param sim 是否排重
	 * @param pageNo 第几页
	 * @param pageSize 一页几条
	 * @param source 来源
	 * @param time 时间
	 * @param area 地域
	 * @param industry 行业
	 * @param emotion 情感
	 * @param sort 排序方式
	 * @param keywords 关键词
	 * @param notKeyWords 排除词
	 * @param keyWordIndex 查询位置（标题/内容）
	 * @return
	 */
	public Object weChatForSearchList(boolean sim,boolean irSimflag,boolean irSimflagAll, int pageNo, int pageSize, String source,String checkedSource, String time, String area,
			String industry, String emotion, String sort, String keywords,String notKeyWords,String keyWordIndex,boolean weight,String fuzzyValue,String fuzzyValueScope,String fromWebSite,String excludeWeb,
			String type,String searchPage ,String searchType)throws TRSException;
	
	/**
	 * 微博列表 
	* @param specialId 专项id
	 * @param pageNo 第几页
	 * @param pageSize 一页几条
	 * @param source 来源
	 * @param time 时间
	 * @param area 地域
	 * @param industry 行业
	 * @param emotion 情感
	 * @param sort 排序方式
	 * @param keywords 关键词
	 * @param notKeyWords 排除词
	 * @param keyWordIndex 查询位置（标题/内容）
	 * @return
	 * @throws TRSException
	 */
	public Object statusSearch(String specialId, int pageNo, int pageSize, String source, String time, String area,
			String industry, String emotion, String sort, String keywords,String fuzzyValueScope,String notKeyWords,String keyWordIndex,String forwarPrimary,String type) throws TRSException;
	
	/**
	 * 微博列表 对于高级搜索是否排重
	* @param sim 是否排重
	 * @param pageNo 第几页
	 * @param pageSize 一页几条
	 * @param source 来源
	 * @param time 时间
	 * @param area 地域
	 * @param industry 行业
	 * @param emotion 情感
	 * @param sort 排序方式
	 * @param keywords 关键词
	 * @param notKeyWords 排除词
	 * @param keyWordIndex 查询位置（标题/内容）
	 * @return
	 * @throws TRSException
	 */
	public Object statusForSearchList(boolean sim, boolean irSimflag,boolean irSimflagAll,int pageNo, int pageSize, String source, String checkedSource,String time, String area,
			String industry, String emotion, String sort, String keywords,String notKeyWords,String keyWordIndex,String forwarPrimary,String forwarPrimary1,boolean weight,
									  String fuzzyValue,String fuzzyValueScope,String fromWebSite,String excludeWeb,String type,String searchPage,String searchType) throws TRSException;
	/**
	 * 传统列表
	* @param specialId 专项id
	 * @param pageNo 第几页
	 * @param pageSize 一页几条
	 * @param source 来源
	 * @param time 时间
	 * @param area 地域
	 * @param industry 行业
	 * @param emotion 情感
	 * @param sort 排序方式
	 * @param keywords 关键词
	 * @param notKeyWords 排除词
	 * @param keyWordIndex 查询位置（标题/内容）
	 * @return
	 * @throws TRSException
	 */
	public Object documentSearch(String specialId, int pageNo, int pageSize, String source, String time, String area,
			String industry, String emotion, String sort, String invitationCard,String keywords,String fuzzyValueScope,String notKeyWords,String keyWordIndex,String foreign,String type) throws TRSException;
	
	
	/**
	 * 传统列表   对于高级搜索是否排重
	* @param sim 是否排重
	 * @param pageNo 第几页
	 * @param pageSize 一页几条
	 * @param source 来源
	 * @param time 时间
	 * @param area 地域
	 * @param mediaIndustry 媒体行业
	 * @param emotion 情感
	 * @param sort 排序方式
	 * @param keywords 关键词
	 * @param notKeyWords 排除词
	 * @param keyWordIndex 查询位置（标题/内容）
	 * @param fromWebSite 来源网站
	 * @param excludeWeb 排除网站
	 * @param newsInformation 新闻信息资质
	 * @param reprintPortal 可供转载网站/门户类型
	 * @param siteType 网站类型
	 * @return
	 * @throws TRSException
	 */
	public Object documentForSearchList(boolean sim, boolean irSimflag,boolean irSimflagAll,int pageNo, int pageSize, String source, String checkedSource,String time, String area,
			String mediaIndustry, String emotion, String sort, String invitationCard,String invitationCard1,String keywords,String notKeyWords,String keyWordIndex,boolean weight,
										String fuzzyValue,String fuzzyValueScope,String fromWebSite,String excludeWeb,String newsInformation,String reprintPortal,String siteType,String type,String searchPage,String searchType) throws TRSException;

	/**
	 * TF   对于高级搜索是否排重
	 * @param sim 是否排重
	 * @param pageNo 第几页
	 * @param pageSize 一页几条
	 * @param source 来源
	 * @param time 时间
	 * @param area 地域
	 * @param mediaIndustry 媒体行业
	 * @param emotion 情感
	 * @param sort 排序方式
	 * @param keywords 关键词
	 * @param notKeyWords 排除词
	 * @param keyWordIndex 查询位置（标题/内容）
	 * @param fromWebSite 来源网站
	 * @param excludeWeb 排除网站
	 * @param newsInformation 新闻信息资质
	 * @param reprintPortal 可供转载网站/门户类型
	 * @param siteType 网站类型
	 * @return
	 * @throws TRSException
	 */
	public Object documentTFForSearchList(boolean sim, boolean irSimflag,boolean irSimflagAll,int pageNo, int pageSize, String source, String checkedSource,String time, String area,
										String mediaIndustry, String emotion, String sort, String keywords,String notKeyWords,String keyWordIndex,boolean weight,
										  String fuzzyValue,String fuzzyValueScope,String fromWebSite,String excludeWeb,String newsInformation,String reprintPortal,String siteType,String type,String searchPage,String searchType) throws TRSException;

	public Object documentCommonVOForSearchList(boolean sim, boolean irSimflag,boolean irSimflagAll,int pageNo, int pageSize, String source, String time, String area,
												String mediaIndustry, String emotion, String sort, String invitationCard,String invitationCard1, String forwarPrimary, String forwarPrimary1,String keywords,
												String notKeyWords,String keyWordIndex,boolean weight,String fuzzyValue,String fuzzyValueScope,String fromWebSite,String excludeWeb,String newsInformation,
												String reprintPortal,String siteType,boolean isExport,String type,String keyName,String searchPage,String searchType) throws TRSException;

	/**
	 * 普通搜索 传统列表
	 * @param sim 是否排重
	 * @param irSimflag  是否排重
	 * @param pageNo  第几页
	 * @param pageSize   一页几条
	 * @param source  来源
	 * @param time    时间
	 * @param emotion  情感
	 * @param sort   排序方式
	 * @param invitationCard
	 * @param keywords  关键词
	 * @param keyWordIndex  查询位置（标题/内容）
	 * @param weight
	 * @param fuzzyValue   结果中搜索
	 * @return
	 * @throws TRSException
	 */
	public Object documentForOrdinarySearch(boolean sim, boolean irSimflag,int pageNo, int pageSize, String source, String time,
										 String emotion, String sort, String invitationCard,String keywords,String keyWordIndex,boolean weight,String fuzzyValue,String type) throws TRSException;
	/**
	 * FaceBook And Twitter List
	 * @since changjiang @ 2018年4月23日
	 * @param specialId
	 * @param pageNo
	 * @param pageSize
	 * @param source
	 * @param time
	 * @param area
	 * @param industry
	 * @param emotion
	 * @param sort
	 * @param invitationCard
	 * @param keywords
	 * @param notKeyWords
	 * @param keyWordIndex
	 * @return
	 * @throws TRSException
	 * @Return : Object
	 */
	public Object documentTFSearch(String specialId, int pageNo, int pageSize, String source, String time, String area,
			String industry, String emotion, String sort, String invitationCard,String keywords,String fuzzyValueScope,String notKeyWords,String keyWordIndex,String type) throws TRSException;
	
	public void simForAlert(String pageId, final Page<AlertEntity> alertList,User user,String groupName);

	/**
	 * 专题分析混合列表
	 * @since changjiang @ 2018年4月25日
	 * @param pageNo
	 * @param pageSize
	 * @param source
	 * @param time
	 * @param emotion
	 * @param sort
	 * @param invitationCard
	 * @param keywords
	 * @param
	 * @return
	 * @throws TRSException
	 * @Return : Object
	 */
	public Object documentCommonSearch(SpecialProject specialProject, int pageNo, int pageSize, String source, String time, String emotion, String sort,
									   String invitationCard, String forwarPrimary, String keywords, String fuzzyValueScope,
									   String type,String read,String preciseFilter,String imgOcr) throws TRSException;
	
	public void setForeignData(String foreign,QueryBuilder builder, QueryBuilder countBuilder,QueryCommonBuilder builderCom,QueryCommonBuilder countBuilderCom);

	/**
	 * 
	 * @param builder
	 * @param user
	 * @param sim
	 * @param irSimflag
	 * @return
	 * @throws TRSException
	 */
	public Object getHotWechatSimListDetail(QueryBuilder builder, User user, boolean sim, boolean irSimflag,boolean irSimflagAll,String type)
			throws TRSException;

	public Object getHotStatusSimListDetail(QueryBuilder builder, User user, boolean sim, boolean irSimflag,boolean irSimflagAll,String type)throws TRSException;
	
	/**
	 * 文章详情页的推荐文章列表
	 * @param sid 文章主键
	 * @param source 来源
	 * @return
	 * @throws
	 * @throws TRSException
	 */
	public Object simlist(String sid,String source) throws TRSSearchException, TRSException;

	/**
	 * 获取网察原数据
	 * @param trsl 传统检索表达式
	 * @param statusTrsl 微博检索表达式
	 * @param weChatTrsl 微信检索表达式
	 * @param requestTime 请求时间
	 * @param period  时间间隔
	 * @param isSimilar
	 * @param irSimflag
	 * @param irSimflagAll
	 * @return
	 * @throws TRSSearchException
	 * @throws TRSException
	 */
	public List<Object> getOriginalData(String trsl,String statusTrsl,String weChatTrsl,String requestTime,Integer period,
										boolean isSimilar, boolean irSimflag,boolean irSimflagAll)throws TRSException, TRSSearchException;

	public Object searchstattotal(boolean sim, boolean irSimflag, boolean irSimflagAll, String source, String searchType,
								  String keywords, String keyWordIndex, String time, boolean weight, String fromWebSite, String excludeWeb,
								  String emotion, String read, String notKeyWords, String excludeWordIndex, String mediaLevel,
								  String mediaIndustry, String contentIndustry, String filterInfo, String contentArea,
								  String mediaArea, String preciseFilter, String imgOcr,String type) throws TRSException;

	public Object advancedSearchList(boolean sim, boolean irSimflag, boolean irSimflagAll, int pageNo, int pageSize, String sort,
									 String keywords, String searchType, String time, String keyWordIndex, boolean weight, String monitorSite,
									 String excludeWeb, String emotion, String read, String excludeWords, String excludeWordsIndex, String source,
									 String mediaLevel, String mediaIndustry, String contentIndustry, String filterInfo, String contentArea, String mediaArea,
									 String preciseFilter, String invitationCard, String forwardPrimary, String fuzzyValue, String fuzzyValueScope,String imgOcr, String type) throws TRSException;
}

