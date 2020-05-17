package com.trs.netInsight.support.fts;

import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.hybase.client.TRSConnection;
import com.trs.hybase.client.TRSInputRecord;
import com.trs.hybase.client.TRSReport;
import com.trs.hybase.client.params.OperationParams;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.model.result.IDocument;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 全文检索接口
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since slzs @ 2017年4月10日 下午1:39:14
 * @Auto mawen
 */
public interface FullTextSearch {

	/**
	 * 验证检索条件是否正确
	 *
	 * @param strSQL
	 * @return
	 */
	boolean validFTSSQL(String strSQL);

	/**
	 * 全文检索,查主键
	 *
	 * @param query：检索条件
	 * @param pageNo：页码
	 * @param pageSize：每页数据量
	 * @param isSimilar
	 * @return
	 *
	 */
	public List<String> ftsQueryIds(QueryBuilder query, int pageNo, int pageSize, boolean isSimilar);

	/**
	 * 全文检索,包含数据/总数/分页/相关文档等信息
	 *
	 * @param query：检索条件
	 * @param pageNo：页码
	 * @param pageSize：每页数据量
	 * @param bSegment：是否要动态摘要
	 * @param bHaveSameDocIds：是否要相似文档id
	 * @param isSimilar
	 * @return IDocument
	 */
	public <T extends IDocument> T ftsQuery(QueryBuilder query, int pageNo, int pageSize, boolean bSegment,
                                            boolean bHaveSameDocIds, boolean isSimilar);

	/**
	 * 全文检索接口
	 *
	 * @param query
	 * @param resultClass
	 * @param isSimilar
	 * @param irSimflag 是否Url排重
	 * @param <T>
	 * @return
	 * @throws TRSException
	 * @throws TRSSearchException
	 *             添加论坛的查询逻辑
	 */
	public <T extends IDocument> List<T> ftsQuery(QueryBuilder query, Class<T> resultClass, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String type)
			throws TRSException, TRSSearchException;

	/**
	 * 全文检索接口
	 * @date Created at 2018年10月25日  上午10:37:30
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param query
	 * @param resultClass
	 * @param isSimilar
	 * @param irSimflag
	 * @param isDataDateAndDataSources 是否根据机构限制时间和数据来源
	 * @return
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	public <T extends IDocument> List<T> ftsQuery(QueryBuilder query, Class<T> resultClass, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, boolean isDataDateAndDataSources, String type)
			throws TRSException, TRSSearchException;

	public <T extends IDocument> List<T> ftsQueryForExport(QueryBuilder query, Class<T> resultClass, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String type)
			throws TRSException, TRSSearchException;

	// public <T extends IDocument> List<T> ftsQueryNew(String trsl, String
	// orderBy, long pageSize, long pageNo, Class<T> resultClass, boolean
	// isSimilar) throws TRSSearchException;

	/**
	 * 分页
	 *
	 * @date Created at 2018年1月6日 上午11:17:04
	 * @Author 谷泽昊
	 * @param query
	 * @param isSimilar
	 * @param irSimflag url排重
	 * @return
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	public <T extends IDocument> PagedList<T> ftsPageList(QueryBuilder query, Class<T> resultClass, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String type)
			throws TRSException, TRSSearchException;

	/**
	 * 分页接口
	 * @param keyRedis 返回给前端的trslk  底层拼接完整表达式后存入redis
	 * @param trsl 表达式
	 * @param orderBy 排序字段
	 * @param pageSize 一页几条
	 * @param pageNo 第几页
	 * @param resultClass 返回实体
	 * @param isSimilar 1000 10000排重
	 * @param irSimflag url排重
	 * @param server 是否转换为server表达式
	 * @return
	 * @throws TRSSearchException
	 */
	public <T extends IDocument> PagedList<T> ftsPageList(String keyRedis, String trsl, String orderBy, long pageSize, long pageNo,
                                                          Class<T> resultClass, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, boolean server, String type) throws TRSSearchException;

//	public <T extends IDocument> PagedList<T> ftsPageListForExport(String keyRedis,String trsl, String orderBy, long pageSize, long pageNo,
//			Class<T> resultClass, boolean isSimilar,boolean irSimflag,boolean server) throws TRSSearchException;

	/**
	 * 获取指定信息类型在当前条件返回多少数量
	 *
	 * @param query：检索条件
	 * @return long
	 * @param isSimilar
	 * @param irSimflag url排重
	 */
	public long ftsCount(QueryBuilder query, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String type);

	/**
	 * 获取指定信息类型在当前条件返回多少数量
	 *
	 * @param query：检索条件
	 * @return long
	 * @param isSimilar
	 * @param irSimflag url排重
	 */
	public <T extends IDocument> long ftsCount(QueryBuilder query, Class<T> resultClass, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String type);

	/**
	 * 获取指定信息类型在当前条件返回多少数量
	 *
	 * @param query：检索条件
	 * @return long
	 * @param isSimilar
	 * @param irSimflag url排重
	 */
	public <T extends IDocument> long ftsCountNoRtt(QueryBuilder query, Class<T> resultClass, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String type);

	/**
	 * 统计间隔检索数量
	 *
	 * @param query：检索条件
	 * @param intervals
	 *            各时间点
	 *
	 * @return Vector<Long>
	 */
	public Long[] ftsCountsByInterval(QueryBuilder query, Date[] intervals);

	/**
	 * 分类统计
	 *
	 * @date Created at 2017年12月7日 上午11:18:58
	 * @Author 谷泽昊
	 * @param builder
	 * @param isSimilar
	 * @param irSimflag url排重
	 * @param groupField
	 * @param indices
	 * @return
	 * @throws TRSSearchException
	 */
	public GroupResult categoryQuery(QueryBuilder builder, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String groupField, String type, String... indices)
			throws TRSSearchException;

	/**
	 * 不按照从大到小排序的分类统计
	 *
	 * @param trsl
	 * @param isSimilar
	 * @param irSimflag url排重
	 * @param groupField
	 * @param limit
	 * @param indices
	 * @return
	 * @throws TRSSearchException
	 */
	public GroupResult categoryQueryNoSort(boolean server, String trsl, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String groupField, int limit, String type,
                                           String... indices) throws TRSSearchException;

	/**
	 * 分类统计
	 *
	 * @date Created at 2017年12月7日 上午11:19:02
	 * @Author 谷泽昊
	 * @param trsl
	 * @param isSimilar
	 * @param irSimflag  url排重
	 * @param groupField
	 * @param limit
	 * @param indices
	 * @return
	 * @throws TRSSearchException
	 */
	public GroupResult categoryQuery(boolean server, String trsl, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String groupField, int limit, String type, String... indices)
			throws TRSSearchException;

	/**
	 *
	 * @param queryBuilder:查询条件
	 * @param isSimilar
	 * @param irSimflag 是否url排重
	 * @return
	 * @throws TRSException
	 */
	@SuppressWarnings("rawtypes")
	public Iterator groupCount(QueryBuilder queryBuilder, String groupBy, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String type) throws TRSSearchException;

	/**
	 * 批量插入
	 *
	 * @param records
	 *            记录集,类属性详见《TRS Hybase JAVA开发接口》</br>
	 *            <font color='red'>
	 *            <code> TRSInputRecord 内包含 TRSInputColumn 集合,TRSInputColumn 的 name-value必须与 hybase的database类型相同,否则会抛出异常</code>
	 *            </font>
	 * @param databaseName
	 *            库名
	 * @param sameToMain
	 *            是否依赖于主hybase
	 * @param connect
	 *            依赖主hybase可传null;如果独立于主hybase,请保证connect连接正确
	 * @throws TRSException
	 */
	public void insertRecords(List<TRSInputRecord> records, String databaseName, boolean sameToMain,
                              TRSConnection connect) throws TRSException;

	/**
	 * 单条插入
	 *
	 * @param records
	 *            记录集,类属性详见《TRS Hybase JAVA开发接口》</br>
	 *            <font color='red'>
	 *            <code> TRSInputRecord 内包含 TRSInputColumn 集合,TRSInputColumn 的 name-value必须与 hybase的database类型相同,否则会抛出异常</code>
	 *            </font>
	 * @param databaseName
	 *            库名
	 * @param sameToMain
	 *            是否依赖于主hybase
	 * @param connect
	 *            依赖主hybase可传null;如果独立于主hybase,请保证connect连接正确
	 * @throws TRSException
	 */
	public void insertRecords(TRSInputRecord record, String databaseName, boolean sameToMain, TRSConnection connect)
			throws TRSException;

	/**
	 * 混合列表通用分页查询方法
	 *
	 * @param query
	 *            混合列表通用查询构造
	 * @param isSimilar
	 *            是否排重
	 * @return
	 * @throws TRSSearchException
	 */
	public PagedList<FtsDocumentCommonVO> pageListCommon(QueryCommonBuilder query, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String type)
			throws TRSException;

	public PagedList<FtsDocumentCommonVO> pageListCommonForExport(QueryCommonBuilder query, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String type)
			throws TRSException;

	/**
	 * 多表联合查询数量
	 *
	 * @param query
	 * @param isSimilar
	 * @return
	 */
	public long ftsCountCommon(QueryCommonBuilder builder, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String type);

	/**
	 * 更新数据库里指定的记录
	 *
	 * @date Created at 2018年4月12日 上午11:39:48
	 * @Author 谷泽昊
	 * @param DBName
	 *            数据库名称
	 * @param records
	 *            新记录
	 * @throws com.trs.hybase.client.TRSException
	 */
	public void updateRecords(String DBName, List<TRSInputRecord> records) throws TRSException;

	/**
	 * 更新数据库里指定的记录
	 *
	 * @date Created at 2018年4月12日 上午11:39:51
	 * @Author 谷泽昊
	 * @param DBName
	 *            数据库名称
	 * @param record
	 *            新记录, records必须设置uid，用于指定被更新记录
	 * @throws com.trs.hybase.client.TRSException
	 */
	public void updateRecords(String DBName, TRSInputRecord record) throws TRSException;

	/**
	 * 更新数据库里指定的记录
	 *
	 * @date Created at 2018年4月12日 上午11:39:56
	 * @Author 谷泽昊
	 * @param DBName
	 *            数据库名称
	 * @param records
	 *            新记录, records必须设置uid，用于指定被更新记录
	 * @param params
	 *            更新操作相关参数，可用参数如下：update.mode.replace，是否进行覆盖更新。false表示不进行覆盖更新，
	 *            这时候只需要提供更新字段，其他字段会自动由原值进行回填。true表示进行覆盖更新，原值不进行回填。默认为false
	 * @param report
	 *            更新结果报告
	 * @throws com.trs.hybase.client.TRSException
	 */
	public void updateRecords(String DBName, List<TRSInputRecord> records, OperationParams params, TRSReport report) throws TRSException;

	/**
	 * 根据TRS文件更新数据库里指定的记录
	 *
	 * @date Created at 2018年4月12日 上午11:39:59
	 * @Author 谷泽昊
	 * @param DBName
	 *            数据库名称
	 * @param uniqueColumn
	 *            唯一值字段
	 * @param fileName
	 *            TRS文件地址
	 * @param parserName
	 *            分词器
	 * @param params
	 *            更新操作相关参数，可用参数如下：update.mode.replace，是否进行覆盖更新。false表示不进行覆盖更新，
	 *            这时候只需要提供更新字段，其他字段会自动由原值进行回填。true表示进行覆盖更新，原值不进行回填。默认为false
	 *            。update.insert.nonexistent，是否插入不存在的记录。false表示不插入，直接报错，
	 *            true表示插入不存在的记录。默认为false
	 * @param report
	 *            更新结果报告
	 * @throws com.trs.hybase.client.TRSException
	 */
	public void updateRecords(String DBName, String uniqueColumn, String fileName, String parserName,
                              OperationParams params, TRSReport report) throws TRSException;

	/**
	 * 通过指定id删除数据库里的记录
	 *
	 * @date Created at 2018年4月12日 上午11:44:21
	 * @Author 谷泽昊
	 * @param DBName
	 *            数据库名称
	 * @param uid
	 *            需要删除的记录id列表
	 * @throws com.trs.hybase.client.TRSException
	 */
	public void delete(String DBName, String uid) throws TRSException;

	/**
	 * 通过指定id删除数据库里的记录
	 *
	 * @date Created at 2018年4月12日 上午11:44:24
	 * @Author 谷泽昊
	 * @param DBName
	 *            数据库名称
	 * @param uids
	 *            需要删除的记录id列表
	 * @throws com.trs.hybase.client.TRSException
	 */
	public void delete(String DBName, String[] uids) throws TRSException;

	/**
	 * 通过指定id删除数据库里的记录
	 *
	 * @date Created at 2018年4月12日 上午11:44:27
	 * @Author 谷泽昊
	 * @param DBName
	 *            数据库名称
	 * @param uids
	 *            需要删除的记录id列表
	 * @throws com.trs.hybase.client.TRSException
	 */
	public void delete(String DBName, List<String> uids) throws TRSException;

	/**
	 * 通过指定查询条件删除数据库里的记录
	 *
	 * @date Created at 2018年4月12日 上午11:44:30
	 * @Author 谷泽昊
	 * @param DBName
	 *            数据库名称
	 * @param query
	 *            查询条件,不能为空
	 * @throws TRSException
	 */
	public void deleteQuery(String DBName, String query) throws TRSException;

	/**
	 * 为单条微博查询传播路径，核心转发，微博用户而写
	 * @param query
	 * @param resultClass
	 * @param <T>
	 * @return
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	public <T extends IDocument> List<T> ftsQuery(QueryBuilder query, Class<T> resultClass) throws TRSException, TRSSearchException;


}
