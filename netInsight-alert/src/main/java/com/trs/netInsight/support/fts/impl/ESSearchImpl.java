
package com.trs.netInsight.support.fts.impl;

import com.trs.dc.entity.TRSEsRecord;
import com.trs.dc.entity.TRSEsRecordSet;
import com.trs.dc.entity.TRSEsSearchParams;
import com.trs.dc.entity.TRSStatisticParams;
import com.trs.dc.entity.enums.SortOptionEnum;
import com.trs.dc.openservice.IEsSearchOpenService;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.hybase.client.TRSConnection;
import com.trs.hybase.client.TRSInputRecord;
import com.trs.hybase.client.TRSReport;
import com.trs.hybase.client.params.OperationParams;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.annotation.parser.FtsParser;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.support.fts.model.factory.ESFactory;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.model.result.IDocument;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.trs.netInsight.config.constant.Const.SUB_INDEX;

/**
 * ES全文检索实现
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since slzs @ 2017年4月10日 下午1:59:45
 */
@Slf4j
@Service("esSearchService")
public class ESSearchImpl implements FullTextSearch {
	/**
	 * 检验表达式是否有误
	 *
	 * @param trsl
	 *            表达式
	 * @return boolean
	 */
	@Override
	public boolean validFTSSQL(String trsl) {
		// String fmtTrsl = QueryBuilder.fmtTrsl(trsl);
		return true;
	}

	/**
	 * 
	 * @since slzs @ 2017年4月10日 下午3:11:49
	 * @param query
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	@Override
	public List<String> ftsQueryIds(QueryBuilder query, int pageNo, int pageSize, boolean isSimilar) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.trs.support.fts.FullTextSearch#ftsQuery(QueryBuilder, int, int,
	 *      boolean, boolean)
	 * @since slzs @ 2017年4月10日 下午3:11:49
	 * @param query
	 * @param pageNo
	 * @param pageSize
	 * @param bSegment
	 * @param bHaveSameDocIds
	 * @return
	 */
	@Override
	public <T extends IDocument> T ftsQuery(QueryBuilder query, int pageNo, int pageSize, boolean bSegment,
			boolean bHaveSameDocIds, boolean isSimilar) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends IDocument> List<T> ftsQuery(QueryBuilder query, Class<T> resultClass, boolean isSimilar,boolean irSimflag,boolean irSimflagAll,String type) throws TRSSearchException {
		String trsl = query.asTRSL();
		long pageSize = query.getPageSize();
		long pageNo = query.getPageNo();
		TRSEsSearchParams searchParams = new TRSEsSearchParams();
		/*
		 * 检索并迭代结果集
		 */
		try {
			long from = (query.getPageNo() < 0) ? 0 : (query.getPageNo() * query.getPageSize());
			long recordNum = (query.getPageSize() < 0 || query.getPageSize() > 1024) ? 1024 : query.getPageSize();
			searchParams.setResultSize(from, recordNum);
			if (StringUtil.isNotEmpty(query.getOrderBy()) && query.getOrderBy().startsWith("+")) {
				searchParams.addSortRule(query.getOrderBy().substring(1), SortOptionEnum.ASC.value);
			} else if (StringUtil.isNotEmpty(query.getOrderBy())) {
				searchParams.addSortRule(query.getOrderBy().substring(1), SortOptionEnum.DESC.value);
			}

			/*
			 * 设置高亮类型 <font style='background-color:Red'>
			 */
			searchParams.setResultFields(FtsParser.getSearchField(resultClass));
			searchParams.setHighLightFields(FtsParser.getHighLightField(resultClass));
			searchParams.setQuery(trsl); // 检索表达式
			String[] databases = FtsParser.getDatabases(resultClass).split(";");
			String[] subDBName = getSubDBName(trsl, databases);
			TRSEsRecordSet esResultSet = ESFactory.getSearchService().querySearch(searchParams, subDBName);// 报错
			long count = esResultSet.getNumFound();
			// 装载检索实体
			List<T> entities = new ArrayList<>();
			if (count > 0) {
				for (TRSEsRecord record : esResultSet.getResultSet()) {
					T t = FtsParser.toEntity(record, resultClass);
					entities.add(t);
				}
			}
			return entities;
		} catch (Exception e) {
			log.error("fail to search by ES+Mongo: [" + trsl + "],order:[" + query.getOrderBy()
					+ "],page from:,number:[" + pageNo + "],size:[" + pageSize + "]", e);
			throw new TRSSearchException("Fail to page list in  ES[" + e.getMessage() + "]. ");
		}
	}

	/**
	 * @see com.trs.support.fts.FullTextSearch#ftsCount(QueryBuilder)
	 * @since slzs @ 2017年4月10日 下午3:11:49
	 * @param query
	 * @return
	 */
	@Override
	public long ftsCount(QueryBuilder query, boolean isSimilar,boolean irSimflag,boolean irSimflagAll,String type) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see com.trs.support.fts.FullTextSearch#ftsCountsByInterval(QueryBuilder,
	 *      Date[])
	 * @since slzs @ 2017年4月10日 下午3:11:49
	 * @param query
	 * @param intervals
	 * @return
	 */
	@Override
	public Long[] ftsCountsByInterval(QueryBuilder query, Date[] intervals) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 分类统计ES实现
	 *
	 * @return GroupResult
	 * @throws TRSSearchException
	 */
	@Override
	public GroupResult categoryQuery(QueryBuilder builder, boolean isSimilar,boolean irSimflag,boolean irSimflagAll, String field,String type, String... indices) throws TRSSearchException {
		return categoryQuery(builder.isServer(), builder.asTRSL(),isSimilar,irSimflag,irSimflagAll, field, builder.getPageSize(),type, indices);
	}

	@Override
	public GroupResult categoryQuery(boolean server,String trsl, boolean isSimilar,boolean irSimflag, boolean irSimflagAll,String statisticOnField, int limit,String type, String... indices)
			throws TRSSearchException {
		TRSStatisticParams searchParams = new TRSStatisticParams();
		searchParams.setGroupTerm(statisticOnField);
		searchParams.setOrderOption(SortOptionEnum.DESC.toString());
		searchParams.setQuery(trsl);
		searchParams.setResultSize(limit);

		GroupResult result = new GroupResult();
		try {
			String[] subDBName = this.getSubDBName(trsl, indices);
			IEsSearchOpenService searchService = ESFactory.getSearchService();
			TRSEsRecordSet recordSet = searchService.classifiedStatistic(searchParams, subDBName);
			Iterator<TRSEsRecord> recordIterator = recordSet.iterator();
			int i = 0;
			while (recordIterator.hasNext() && i++ < limit) {
				TRSEsRecord record = recordIterator.next();
				for (String key : record.keySet()) {
					if (StringUtil.isEmpty(key)) {
						continue;
					}
					if (TrslUtil.isDate(key)) {
						key = key.replaceAll("[/.]", "-");
					}
					result.addGroup(key, Integer.parseInt(record.getString(key)));
				}
			}
			return result;
		} catch (Exception e) {
			log.error("fail to statistic by ES: [" + trsl + "],statisticOnField:[" + statisticOnField + "], topN:["
					+ limit + "]", e);
			throw new TRSSearchException(
					"Fail to statistic by ES on field " + statisticOnField + "[" + e.getMessage() + "]");
		}
	}


	/**
	 * 获取分裂字库
	 *
	 * @param trsl
	 *            表达式
	 * @param indices
	 *            索引名
	 * @return StringArray
	 */
	private String[] getSubDBName(String trsl, String... indices) {
		Date[] dateRange = TrslUtil.getDateRange(trsl);
		if (dateRange == null) {
			return indices;
		} else {
			dateRange[1] = dateRange[1].after(new Date()) ? new Date() : dateRange[1];
			List<String> monthList = DateUtil.getMonthBetween(dateRange[0], dateRange[1]);
			List<String> result = new ArrayList<>();
			for (String database : indices) {
				if (SUB_INDEX.contains(database)) {
					for (Object month : monthList) {
						result.add(database + "@" + month);
					}
				} else {
					result.add(database);
				}
			}
			return result.toArray(new String[result.size()]);
		}
	}

	@Override
	public <T extends IDocument> long ftsCount(QueryBuilder query, Class<T> resultClass, boolean isSimilar,boolean irSimflag,boolean irSimflagAll,String type) {
		// TODO Auto-generated method stub
		return 0;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Iterator groupCount(QueryBuilder queryBuilder, String groupBy, boolean isSimilar,boolean irSimflag,boolean irSimflagAll,String type) throws TRSSearchException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends IDocument> PagedList<T> ftsPageList(QueryBuilder query, Class<T> resultClass, boolean isSimilar,boolean irSimflag,boolean irSimflagAll,String type)
			throws TRSException, TRSSearchException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends IDocument> PagedList<T> ftsPageList(String keyRedis,String trsl, String orderBy, long pageSize, long pageNo,
			Class<T> resultClass, boolean isSimilar,boolean irSimflag,boolean irSimflagAll,boolean server,String type) throws TRSSearchException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insertRecords(List<TRSInputRecord> records, String databaseName, boolean sameToMain,
			TRSConnection connect) throws TRSException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PagedList<FtsDocumentCommonVO> pageListCommon(QueryCommonBuilder query, boolean isSimilar,boolean irSimflag,boolean irSimflagAll,String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long ftsCountCommon(QueryCommonBuilder builder, boolean b,boolean irSimflag,boolean irSimflagAll,String type) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public GroupResult categoryQueryNoSort(boolean server,String trsl, boolean isSimilar,boolean irSimflag, boolean irSimflagAll,String groupField, int limit,String type,
			String... indices) throws TRSSearchException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insertRecords(TRSInputRecord record, String databaseName, boolean sameToMain, TRSConnection connect)
			throws TRSException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateRecords(String DBName, List<TRSInputRecord> records) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateRecords(String DBName, TRSInputRecord record) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateRecords(String DBName, List<TRSInputRecord> records, OperationParams params, TRSReport report) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateRecords(String DBName, String uniqueColumn, String fileName, String parserName,
			OperationParams params, TRSReport report) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(String DBName, String uid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(String DBName, String[] uids) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(String DBName, List<String> uids) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteQuery(String DBName, String query) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T extends IDocument> List<T> ftsQuery(QueryBuilder query, Class<T> resultClass) throws TRSException, TRSSearchException {
		return null;
	}

	@Override
	public <T extends IDocument> long ftsCountNoRtt(QueryBuilder query, Class<T> resultClass, boolean isSimilar,boolean irSimflag,boolean irSimflagAll,String type) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T extends IDocument> List<T> ftsQueryForExport(QueryBuilder query, Class<T> resultClass, boolean isSimilar,
			boolean irSimflag,boolean irSimflagAll,String type) throws TRSException, TRSSearchException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PagedList<FtsDocumentCommonVO> pageListCommonForExport(QueryCommonBuilder query, boolean isSimilar,
			boolean irSimflag,boolean irSimflagAll,String type) throws TRSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends IDocument> List<T> ftsQuery(QueryBuilder query, Class<T> resultClass, boolean isSimilar,
			boolean irSimflag,boolean irSimflagAll, boolean isDataDateAndDataSources,String type) throws TRSException, TRSSearchException {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public <T extends IDocument> PagedList<T> ftsPageListForExport(String keyRedis, String trsl, String orderBy,
//			long pageSize, long pageNo, Class<T> resultClass, boolean isSimilar, boolean irSimflag, boolean server)
//			throws TRSSearchException {
//		// TODO Auto-generated method stub
//		return null;
//	}

}
