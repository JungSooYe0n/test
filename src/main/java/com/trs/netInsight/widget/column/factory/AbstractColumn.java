package com.trs.netInsight.widget.column.factory;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.knowledgeBase.service.IKnowledgeBaseService;
import com.trs.netInsight.widget.alert.entity.repository.AlertRepository;
import com.trs.netInsight.widget.analysis.service.IDistrictInfoService;
import com.trs.netInsight.widget.analysis.service.impl.ChartAnalyzeService;
import com.trs.netInsight.widget.report.entity.repository.FavouritesRepository;
import com.trs.netInsight.widget.special.service.IInfoListService;

import com.trs.netInsight.widget.user.entity.User;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 栏目检索抽象类
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年4月4日
 */
@Setter
public abstract class AbstractColumn {

	/**
	 * 栏目配置
	 */
	protected ColumnConfig config;
	
	/**
	 * 列表只包含微博
	 */
	protected boolean listOnlyStatus=false;
	
	/**
	 * 检索服务
	 */
	protected IInfoListService infoListService;
	protected FullTextSearch hybase8SearchService;
	protected IDistrictInfoService districtInfoService;
	protected ChartAnalyzeService chartAnalyzeService;
	
	/**
	 * 预警 收藏
	 */
	protected FavouritesRepository favouritesRepository;
	protected AlertRepository alertRepository;
	
	/**
	 * @Desc : 获取栏目块数据
	 * @since changjiang @ 2018年4月4日
	 * @throws TRSSearchException
	 * @Return : Object
	 */
	public abstract Object getColumnData(String timeRange) throws TRSSearchException;

	/**
	 * @Desc : 获取栏目块数据
	 * @since changjiang @ 2018年4月4日
	 * @throws TRSSearchException
	 * @Return : Object
	 */
	public abstract Object getColumnCount() throws TRSSearchException;

	/**
	 * @Desc : 获取列表查询数据(包含列表跳转及列表内检索)
	 * @since changjiang @ 2018年4月4日
	 * @throws TRSSearchException
	 * @Return : Object
	 */
	public abstract Object getSectionList() throws TRSSearchException;

	/**
	 * @Desc : 获取App列表数据
	 * @since duhq @ 2019年4月29日
	 * @throws TRSSearchException
	 * @Return : Object
	 */
	public abstract Object getAppSectionList(User user) throws TRSSearchException;

	/**
	 * @Desc : 构造分来源检索条件
	 * @since changjiang @ 2018年4月4日
	 * @Return : QueryBuilder
	 */
	public abstract QueryBuilder createQueryBuilder();

	/**
	 * @Desc : 构造联合通用检索条件
	 * @since changjiang @ 2018年4月4日
	 * @Return : QueryCommonBuilder
	 */
	public abstract QueryCommonBuilder createQueryCommonBuilder();
	
}
