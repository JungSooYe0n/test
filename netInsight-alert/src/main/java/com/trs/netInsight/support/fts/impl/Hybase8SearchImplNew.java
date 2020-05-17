package com.trs.netInsight.support.fts.impl;

import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.hybase.client.*;
import com.trs.hybase.client.params.OperationParams;
import com.trs.hybase.client.params.SearchParams;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.cache.RedisFactory;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.annotation.parser.FtsParser;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.support.fts.model.factory.HybaseFactory;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.model.result.IDocument;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.support.hybaseShard.entity.HybaseShard;
import com.trs.netInsight.support.hybaseShard.service.IHybaseShardService;
import com.trs.netInsight.support.knowledgeBase.entity.KnowledgeBase;
import com.trs.netInsight.support.knowledgeBase.entity.KnowledgeClassify;
import com.trs.netInsight.support.knowledgeBase.service.IKnowledgeBaseService;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.article.entity.ArticleDelete;
import com.trs.netInsight.widget.article.entity.enums.ArticleDeleteGroupName;
import com.trs.netInsight.widget.article.service.IArticleDeleteService;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Hybase检索服务实现
 * <p>
 *
 * @author 北京拓尔思信息技术股份有限公司 Created by TRS on 2017/7/26.
 */
@Service("hybase8SearchServiceNew")
@Slf4j
public class Hybase8SearchImplNew implements FullTextSearch {

    @Autowired
    private IArticleDeleteService articleDeleteService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private IHybaseShardService hybaseShardService;

    @Autowired
    private IKnowledgeBaseService knowledgeBaseService;

    @Value("${system.hybase.search.irSimflag.open}")
    private boolean irSimflagOpen;

    private static final int MAX_PAGE_SIZE = 512 * 2;


    /*
     ****************************************************************************************************************************************************
     * 手工录入库的操作
     */

    @Override
    public void insertRecords(List<TRSInputRecord> records, String databaseName, boolean sameToMain,
                              TRSConnection connect) throws com.trs.netInsight.handler.exception.TRSException {
        TRSConnection connection = null;
        try {
            if (sameToMain) {
                connection = HybaseFactory.getClient();
                connection.executeInsert(databaseName, records);
            } else {
                connect.executeInsert(databaseName, records);
            }
        } catch (TRSException e) {
            throw new com.trs.netInsight.handler.exception.TRSException(e);
        } finally {
            HybaseFactory.clean();
            if (connect != null) {
                connect.close();
            }
        }

    }

    @Override
    public void insertRecords(TRSInputRecord record, String databaseName, boolean sameToMain, TRSConnection connect)
            throws com.trs.netInsight.handler.exception.TRSException {
        TRSConnection connection = null;
        List<TRSInputRecord> recordList = new ArrayList<>(1);
        recordList.add(record);
        try {
            if (sameToMain) {
                connection = HybaseFactory.getClient();
                connection.executeInsert(databaseName, recordList);
            } else {
                connect.executeInsert(databaseName, recordList);
            }
        } catch (TRSException e) {
            throw new com.trs.netInsight.handler.exception.TRSException(e);
        } finally {
            HybaseFactory.clean();
            if (connect != null) {
                connect.close();
            }
        }

    }


    @Override
    public void updateRecords(String DBName, List<TRSInputRecord> records)
            throws com.trs.netInsight.handler.exception.TRSException {

        try {
            HybaseFactory.getClient().executeUpdate(DBName, records);
        } catch (TRSException e) {
            throw new com.trs.netInsight.handler.exception.TRSException(e);
        } finally {
            HybaseFactory.clean();
        }
    }

    @Override
    public void updateRecords(String DBName, TRSInputRecord record)
            throws com.trs.netInsight.handler.exception.TRSException {
        List<TRSInputRecord> records = new ArrayList<>();
        records.add(record);
        try {
            HybaseFactory.getClient().executeUpdate(DBName, records);
        } catch (TRSException e) {
            throw new com.trs.netInsight.handler.exception.TRSException(e);
        } finally {
            HybaseFactory.clean();
        }

    }

    @Override
    public void updateRecords(String DBName, List<TRSInputRecord> records, OperationParams params, TRSReport report)
            throws com.trs.netInsight.handler.exception.TRSException {
        try {
            HybaseFactory.getClient().executeUpdate(DBName, records, params, report);
        } catch (TRSException e) {
            throw new com.trs.netInsight.handler.exception.TRSException(e);
        } finally {
            HybaseFactory.clean();
        }
    }

    @Override
    public void updateRecords(String DBName, String uniqueColumn, String fileName, String parserName,
                              OperationParams params, TRSReport report) throws com.trs.netInsight.handler.exception.TRSException {
        try {
            HybaseFactory.getClient().executeUpdate(DBName, uniqueColumn, fileName, parserName, params, report);
        } catch (TRSException e) {
            throw new com.trs.netInsight.handler.exception.TRSException(e);
        } finally {
            HybaseFactory.clean();
        }
    }

    @Override
    public void delete(String DBName, String uid) throws com.trs.netInsight.handler.exception.TRSException {
        try {
            HybaseFactory.getClient().executeDelete(DBName, uid);
        } catch (TRSException e) {
            throw new com.trs.netInsight.handler.exception.TRSException(e);
        } finally {
            HybaseFactory.clean();
        }
    }

    @Override
    public void delete(String DBName, String[] uids) throws com.trs.netInsight.handler.exception.TRSException {
        try {
            HybaseFactory.getClient().executeDelete(DBName, uids);
        } catch (TRSException e) {
            throw new com.trs.netInsight.handler.exception.TRSException(e);
        } finally {
            HybaseFactory.clean();
        }
    }

    @Override
    public void delete(String DBName, List<String> uids) throws com.trs.netInsight.handler.exception.TRSException {
        try {
            HybaseFactory.getClient().executeDelete(DBName, uids);
        } catch (TRSException e) {
            throw new com.trs.netInsight.handler.exception.TRSException(e);
        } finally {
            HybaseFactory.clean();
        }
    }

    @Override
    public void deleteQuery(String DBName, String query) throws com.trs.netInsight.handler.exception.TRSException {
        try {
            HybaseFactory.getClient().executeDeleteQuery(DBName, query);
        } catch (TRSException e) {
            throw new com.trs.netInsight.handler.exception.TRSException(e);
        } finally {
            HybaseFactory.clean();
        }
    }

    @Override
    public <T extends IDocument> List<T> ftsQuery(QueryBuilder query, Class<T> resultClass) throws com.trs.netInsight.handler.exception.TRSException, TRSSearchException {
        return null;
    }

    /*
     ************************************************************************************************************************************************************

     * 查列表和统计的操作
     * */

    @Override
    public boolean validFTSSQL(String strSQL) {
        return false;
    }

    @Override
    public List<String> ftsQueryIds(QueryBuilder query, int pageNo, int pageSize, boolean isSimilar) {
        return null;
    }

    @Override
    public <T extends IDocument> T ftsQuery(QueryBuilder query, int pageNo, int pageSize, boolean bSegment, boolean bHaveSameDocIds, boolean isSimilar) {
        return null;
    }

    @Override
    public <T extends IDocument> List<T> ftsQuery(QueryBuilder query, Class<T> resultClass, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String type) throws com.trs.netInsight.handler.exception.TRSException, TRSSearchException {
        return null;
    }

    @Override
    public <T extends IDocument> List<T> ftsQuery(QueryBuilder query, Class<T> resultClass, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, boolean isDataDateAndDataSources, String type) throws com.trs.netInsight.handler.exception.TRSException, TRSSearchException {
        return null;
    }

    @Override
    public <T extends IDocument> List<T> ftsQueryForExport(QueryBuilder query, Class<T> resultClass, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String type) throws com.trs.netInsight.handler.exception.TRSException, TRSSearchException {
        return null;
    }

    @Override
    public <T extends IDocument> PagedList<T> ftsPageList(QueryBuilder query, Class<T> resultClass, boolean isSimilar,
                                                          boolean irSimflag, boolean irSimflagAll, String type) throws com.trs.netInsight.handler.exception.TRSException, TRSSearchException {
        return ftsPageList(query.getKeyRedis(), query.asTRSL(), query.getOrderBy(), query.getPageSize(),
                query.getPageNo(), resultClass, isSimilar, irSimflag, irSimflagAll, query.isServer(), type);
    }

    /**
     * 分页
     *
     * @param keyRedis    返回给前端的trslk 底层拼接完整表达式后存入redis
     * @param trsl        表达式
     * @param orderBy     排序字段
     * @param pageSize    一页几条
     * @param pageNo      第几页
     * @param resultClass 返回实体
     * @param isSimilar   1000 10000排重
     * @param irSimflag   url排重
     * @param server      是否转换为server表达式
     * @return
     * @throws TRSSearchException
     */
    @Override
    public <T extends IDocument> PagedList<T> ftsPageList(String keyRedis, String trsl, String orderBy, long pageSize,
                                                          long pageNo, Class<T> resultClass, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, boolean server, String type)
            throws TRSSearchException {
        queryCount();
        // 抽取数据库，要写到添加trsl之前
        String[] databaseArr = chooseDatabases(trsl, true);
        String databases = FtsParser.getDatabases(resultClass);
        if (databaseArr != null && databaseArr.length > 0) {
            databases = String.join(";", databaseArr);
        }
        String db = addHybaseInsert(databases);
        // 判断是否排重
        trsl = commonMonthd(trsl, isSimilar, irSimflag, irSimflagAll, true, resultClass);
        log.warn(trsl);
        TRSConnection connection = null;
        try {
            long startConnect = new Date().getTime();
            connection = HybaseFactory.getClient();
            long endConnect = new Date().getTime();
            int connectTime = (int) (endConnect - startConnect);
            SearchParams searchParams = new SearchParams();
            searchParams.setTimeOut(60);
            searchParams.setSortMethod(StringUtil.avoidNull(orderBy));  //排序字段
            searchParams.setReadColumns(String.join(";", FtsParser.getSearchField(resultClass)));
            searchParams.setColorColumns(String.join(";", FtsParser.getHighLightField(resultClass)));
            String search = extractByTrsl(trsl, true, type);

            if (search != null) {
                searchParams.setProperty("search.range.filter", search);
            }
            if (StringUtil.isNotEmpty(keyRedis)) {
                RedisUtil.setString(keyRedis, trsl, 60, TimeUnit.MINUTES);
            }
            long from = (pageNo < 0) ? 0 : pageNo * pageSize;
            long recordNum = (pageSize < 0) ? MAX_PAGE_SIZE : pageSize;
            if (recordNum == 0) {
                recordNum = 20;
            }

            long startHybase = new Date().getTime();
            TRSResultSet resultSet = connection.executeSelect(db, trsl, from, recordNum, searchParams);
            // 系统日志记录
            systemLogRecord(trsl);
            long endHybase = new Date().getTime();
            int queryTime = (int) (endHybase - startHybase);
            queryTime(startHybase, endHybase, trsl, queryTime, connectTime, db, connection.getURL());
            List<T> entities = new ArrayList<>();
            for (int i = 0; i < resultSet.size(); i++) {
                if (resultSet.moveTo(i)) {
                    TRSRecord trsRecord = resultSet.get();
                    entities.add(FtsParser.toEntity(trsRecord, resultClass));
                }
            }
            long count = resultSet.getNumFound();
            return new PagedList<T>(pageSize < 0 ? 0 : (int) pageNo, (int) (pageSize < 0 ? MAX_PAGE_SIZE : pageSize),
                    (int) (count - Math.max(0, 0)), entities, 1);
        } catch (Exception e) {
            log.error("fail to search by hybase: [" + trsl + "],order:[" + orderBy + "],page number:[" + pageNo
                    + "],size:[" + pageSize + "]", e);
            throw new TRSSearchException("检索异常" + e, e);
        } finally {
            HybaseFactory.clean();
        }
    }


    /**
     * 混合列表通用分页查询方法
     *
     * @param query     混合列表通用查询构造
     * @param isSimilar 是否排重
     * @return
     * @throws TRSSearchException
     */
    @Override
    public PagedList<FtsDocumentCommonVO> pageListCommon(QueryCommonBuilder query, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String type)
            throws com.trs.netInsight.handler.exception.TRSException {
        queryCount();
        String trsl = query.asTRSL();
        String orderBy = query.getOrderBy();
        String[] database = query.getDatabase();
        String db = addHybaseInsert(String.join(";", database));

        // 判断是否排重
        trsl = commonMonthd(trsl, isSimilar, irSimflag, irSimflagAll, true, database);
        long pageNo = query.getPageNo();
        int pageSize = query.getPageSize();
        if (pageSize > 10000) {
            return pageListCommonForExport(query, isSimilar, irSimflag, irSimflagAll, type);
        }
        log.info(trsl);
        TRSConnection connection = null;
        try {
            String startConnect = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
            connection = HybaseFactory.getClient();
            String endConnect = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
            int connectTime = Integer.parseInt(endConnect.substring(8, 17))
                    - Integer.parseInt(startConnect.substring(8, 17));
            SearchParams searchParams = new SearchParams();
            searchParams.setTimeOut(60);
            searchParams.setSortMethod(StringUtil.avoidNull(orderBy));
            searchParams.setReadColumns(String.join(";", FtsParser.getSearchField(FtsDocumentCommonVO.class)));
            searchParams.setColorColumns(String.join(";", FtsParser.getHighLightField(FtsDocumentCommonVO.class)));
            String searchParam = extractUrlTimeByCommonBuilder(query, true, type);
            searchParams.setProperty("search.range.filter", searchParam);
            long from = (pageNo < 0) ? 0 : pageNo * pageSize;
            long recordNum = (pageSize < 0) ? MAX_PAGE_SIZE : pageSize;
            if (recordNum == 0) {
                recordNum = 20;
            }

            log.error(trsl);
            long timeStart = new Date().getTime();
            TRSResultSet resultSet = connection.executeSelect(db, trsl, from, recordNum, searchParams);
            // 系统日志记录
            systemLogRecord(trsl);
            long timeEnd = new Date().getTime();
            int queryTime = (int) (timeEnd - timeStart);
            queryTime(timeStart, timeEnd, trsl, queryTime, connectTime, db, connection.getURL());
            if (StringUtil.isNotEmpty(query.getKeyRedis())) {
                RedisUtil.setString(query.getKeyRedis(), trsl, 60, TimeUnit.MINUTES);
            }
            List<FtsDocumentCommonVO> entities = new ArrayList<>();
            for (int i = 0; i < resultSet.size(); i++) {
                if (resultSet.moveTo(i)) {
                    TRSRecord trsRecord = resultSet.get();
                    entities.add(FtsParser.toEntity(trsRecord, FtsDocumentCommonVO.class));
                }
            }
            long count = resultSet.getNumFound();
            return new PagedList<FtsDocumentCommonVO>(pageSize < 0 ? 0 : (int) pageNo,
                    (int) (pageSize < 0 ? MAX_PAGE_SIZE : pageSize), (int) (count - Math.max(0, 0)), entities, 1);
        } catch (Exception e) {
            log.error("fail to search by hybase: [" + trsl + "],order:[" + orderBy + "],page number:[" + pageNo
                    + "],size:[" + pageSize + "]", e);
            throw new com.trs.netInsight.handler.exception.TRSException("检索异常", e);
        } finally {
            HybaseFactory.clean();
        }
    }

    @Override
    public PagedList<FtsDocumentCommonVO> pageListCommonForExport(QueryCommonBuilder query, boolean isSimilar,
                                                                  boolean irSimflag, boolean irSimflagAll, String type) throws com.trs.netInsight.handler.exception.TRSException {
        queryCount();
        String trsl = query.asTRSL();
        String[] databases = query.getDatabase();
        String db = addHybaseInsert(String.join(";", databases));
        // 判断是否排重
        trsl = commonMonthd(trsl, isSimilar, irSimflag, irSimflagAll, true);
        long pageNo = query.getPageNo();
        int pageSize = query.getPageSize();

        log.info(trsl);
        TRSConnection connection = null;
        try {
            String startConnect = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
            connection = HybaseFactory.getClient();
            String endConnect = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
            int connectTime = Integer.parseInt(endConnect.substring(8, 17))
                    - Integer.parseInt(startConnect.substring(8, 17));
            SearchParams searchParams = new SearchParams();
            searchParams.setTimeOut(60);
            searchParams.setProperty("hybase.search.nosort", "true");// 不排序检索
            searchParams.setReadColumns(String.join(";", FtsParser.getSearchField(FtsDocumentCommonVO.class)));
            String searchParam = extractUrlTimeByCommonBuilder(query, true, type);
            searchParams.setProperty("search.range.filter", searchParam);
            long from = (pageNo < 0) ? 0 : pageNo * pageSize;
            long recordNum = (pageSize < 0) ? MAX_PAGE_SIZE : pageSize;
            if (recordNum == 0) {
                recordNum = 20;
            }

            log.error(trsl);
            long timeStart = new Date().getTime();
            TRSResultSet resultSet = connection.executeSelectNoSort(db, trsl, from, recordNum, searchParams);
            // 系统日志记录
            systemLogRecord(trsl);
            long timeEnd = new Date().getTime();
            int queryTime = (int) (timeEnd - timeStart);
            queryTime(timeStart, timeEnd, trsl, queryTime, connectTime, db, connection.getURL());
            if (StringUtil.isNotEmpty(query.getKeyRedis())) {
                RedisUtil.setString(query.getKeyRedis(), trsl, 60, TimeUnit.MINUTES);
            }
            List<FtsDocumentCommonVO> entities = new ArrayList<>();
            while (resultSet.moveNext()) {
                TRSRecord trsRecord = resultSet.get();
                entities.add(FtsParser.toEntity(trsRecord, FtsDocumentCommonVO.class));
            }
            long count = resultSet.getNumFound();
            return new PagedList<FtsDocumentCommonVO>(pageSize < 0 ? 0 : (int) pageNo,
                    (int) (pageSize < 0 ? MAX_PAGE_SIZE : pageSize), (int) (count - Math.max(0, 0)), entities, 1);
        } catch (Exception e) {
            log.error("fail to search by hybase: [" + trsl + "],page number:[" + pageNo + "],size:[" + pageSize + "]",
                    e);
            throw new com.trs.netInsight.handler.exception.TRSException("检索异常", e);
        } finally {
            HybaseFactory.clean();
        }
    }


    @Override
    public long ftsCountCommon(QueryCommonBuilder query, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String type) {
        queryCount();
        SearchParams param = new SearchParams();
        param.setTimeOut(60);
        String search = extractUrlTimeByCommonBuilder(query, true, type);
        if (search != null) {
            param.setProperty("search.range.filter", search);
        }
        try {
            String[] databases = query.getDatabase();
            String db = addHybaseInsert(String.join(",", databases));
            String similartrsl = commonMonthd(query.asTRSL(), isSimilar, irSimflag, irSimflagAll, true, query.getDatabase());
            long startConnect = new Date().getTime();
            TRSConnection client = HybaseFactory.getClient();
            long endConnect = new Date().getTime();
            int connectTime = (int) (endConnect - startConnect);

            // System.out.println(similartrsl);
            long startHybase = new Date().getTime();
            TRSResultSet resultSet = client.executeSelect(db, similartrsl, 0, 1, param);
            // 系统日志记录
            systemLogRecord(similartrsl);
            long endHybase = new Date().getTime();
            int queryTime = (int) (endHybase - startHybase);
            countTime(startHybase, endHybase, similartrsl, queryTime, connectTime, db, client.getURL());
            if (StringUtil.isNotEmpty(query.getKeyRedis())) {
                RedisUtil.setString(query.getKeyRedis(), similartrsl, 10, TimeUnit.MINUTES);
            }
            return resultSet.getNumFound();
        } catch (TRSException e) {
            log.error("Fail to count by hybase " + e.getMessage());
            return 0;
        } finally {
            HybaseFactory.clean();
        }
    }

    @Override
    public long ftsCount(QueryBuilder query, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String type) {
        queryCount();
        SearchParams param = new SearchParams();
        param.setTimeOut(60);
        String search = extractUrlTimeByBuilder(query, true, type);
        if (search != null) {
            param.setProperty("search.range.filter", search);
        }

        try {
            String databases = query.getDatabase();
            String db = addHybaseInsert(databases);
            String similarTrsl = commonMonthd(query.asTRSL(), isSimilar, irSimflag, irSimflagAll, true, query.getDatabase());
            log.warn(similarTrsl);
            long startConnect = new Date().getTime();
            TRSConnection client = HybaseFactory.getClient();
            long endConnect = new Date().getTime();
            int connectTime = (int) (endConnect - startConnect);

            long startHybase = new Date().getTime();
            TRSResultSet resultSet = client.executeSelect(db, similarTrsl, 0, 1, param);
            // 系统日志记录
            systemLogRecord(similarTrsl);
            long endHybase = new Date().getTime();
            int queryTime = (int) (endHybase - startHybase);
            countTime(startHybase, endHybase, similarTrsl, queryTime, connectTime, db, client.getURL());
            if (StringUtil.isNotEmpty(query.getKeyRedis())) {
                RedisUtil.setString(query.getKeyRedis(), similarTrsl, 10, TimeUnit.MINUTES);
            }
            return resultSet.getNumFound();
        } catch (TRSException e) {
            log.error("Fail to count by hybase " + e.getMessage());
            return 0;
        } finally {
            HybaseFactory.clean();
        }
    }

    @Override
    public <T extends IDocument> long ftsCount(QueryBuilder query, Class<T> resultClass, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String type) {
        return 0;
    }

    @Override
    public <T extends IDocument> long ftsCountNoRtt(QueryBuilder query, Class<T> resultClass, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String type) {
        return 0;
    }

    @Override
    public Long[] ftsCountsByInterval(QueryBuilder query, Date[] intervals) {
        return new Long[0];
    }


    @Override
    public GroupResult categoryQuery(QueryBuilder builder, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String groupField, String type,
                                     String... indices) throws TRSSearchException {
        log.info("indices--->" + indices);
        return categoryQuery( builder.isServer(),builder.asTRSL(), isSimilar, irSimflag, irSimflagAll, groupField,
                builder.getPageSize(), type, indices);
    }

    @Override
    public GroupResult categoryQueryNoSort(boolean server, String trsl, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String groupField, int limit, String type, String... indices) throws TRSSearchException {
        return null;
    }

    /**
     * 分类统计
     */
    @Override
    public GroupResult categoryQuery( boolean  isSever,String trsl, boolean isSimilar, boolean irSimflag, boolean irSimflagAll,
                                      String groupField, int limit, String type, String... indices) throws TRSSearchException {
        queryCount();
        String db = addHybaseInsert(String.join(";", indices));
        trsl = commonMonthd(trsl, isSimilar, irSimflag, irSimflagAll, true, indices);
        TRSConnection connection = null;
        try {
            String startConnect = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
            connection = HybaseFactory.getClient();
            String endConnect = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
            int connectTime = Integer.parseInt(endConnect.substring(8, 17))
                    - Integer.parseInt(startConnect.substring(8, 17));
            long startHybase = new Date().getTime();
            SearchParams searchParams = new SearchParams();
            searchParams.setTimeOut(60);
            String search = extractByTrsl(trsl, true, type);

            if (search != null && !db.contains(Const.SINAUSERS)) {
                searchParams.setProperty("search.range.filter", search);
            }
            if (StringUtil.getStringKBIsTooLongLast(trsl)) {
                log.error("海贝查询字符串大于16k,查询报错------------>");
            }
            TRSResultSet result = connection.categoryQuery(db, trsl, "", groupField, limit, searchParams);
            // 系统日志记录
            systemLogRecord(trsl);
            long endHybase = new Date().getTime();

            int queryTime = (int) (endHybase - startHybase);
            categoryTime(startHybase, endHybase, trsl, queryTime, connectTime, db, groupField, connection.getURL());
            GroupResult result1 = new GroupResult();
            if (result.getCategoryMap() != null) {
                for (Map.Entry<String, Long> m : result.getCategoryMap().entrySet()) {
                    if (m != null) {
                        result1.addGroup(m.getKey(), m.getValue());
                    }
                }
                result1.sort();
            }
            return result1;
        } catch (Exception e) {
            log.error("fail to statistic by Hybase: [" + trsl + "],statisticOnField:[" + groupField + "],topN:[" + limit
                    + "]", e);
            throw new TRSSearchException("分类统计失败", e);
        } finally {
            HybaseFactory.clean();
        }
    }

    @Override
    public Iterator groupCount(QueryBuilder queryBuilder, String groupBy, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String type) throws TRSSearchException {
        return null;
    }

    /*
     ************************************************************************************************************************************************************
     */


    //TODO  --- 这个不知道干啥的
    public void queryCount() {
        long id = Thread.currentThread().getId();
        LogPrintUtil loginpool = RedisUtil.getLog(Thread.currentThread().getId());
        if (null != loginpool) {
            int count = loginpool.getCount();
            loginpool.setCount(++count);
        }
        RedisUtil.setLog(id, loginpool);
    }



    /**
     * 排除删除的文章
     *
     * @param indices
     * @return
     * @date Created at 2018年4月2日 下午3:55:05
     * @Author 谷泽昊
     */
    private String isArticleDelete(String... indices) {
        String join = String.join(";", indices);
        String[] split = join.split(";");
        String trsl = FtsFieldConst.FIELD_SID;

        List<String> list = Arrays.asList(split);
        int count = 200;
        if (join.contains(Const.HYBASE_OVERSEAS) && join.contains(Const.INSERT) && list.size() > 2) {
            count = 200 / (list.size() - 2);
        } else if ((join.contains(Const.HYBASE_OVERSEAS) || join.contains(Const.INSERT)) && list.size() > 1) {
            count = 200 / (list.size() - 1);
        }

        boolean isGroupNameWeixin = false;
        ArticleDeleteGroupName groupName = ArticleDeleteGroupName.chuantong;
        String resultTrsl = "";
        for (String db : list) {
            if (db.contains(Const.HYBASE_NI_INDEX)) {
                trsl = FtsFieldConst.FIELD_SID;
                groupName = ArticleDeleteGroupName.chuantong;
            } else if (db.contains(Const.WEIBO)) {
                trsl = FtsFieldConst.FIELD_MID;
                groupName = ArticleDeleteGroupName.weibo;
            } else if (db.contains(Const.WECHAT)) {
                trsl = FtsFieldConst.FIELD_HKEY;
                isGroupNameWeixin = true;
                groupName = ArticleDeleteGroupName.weixin;
            } else if (db.contains(Const.HYBASE_OVERSEAS)) {
                trsl = FtsFieldConst.FIELD_SID;
                groupName = ArticleDeleteGroupName.tf;
            } else {
                break;
            }
            String trslTmp = resultTrsl + articleDeleteTrsl(trsl, isGroupNameWeixin, groupName, count);
            //判断大于16kb,删除排重去掉
            if (!StringUtil.getStringKBIsTooLong(trslTmp)) resultTrsl = trslTmp;
        }

        return resultTrsl;
    }







    /*   *******************************************************************************************************************************************************************************************************
     *  处理查询表达式  添加排重以及删除文章
     */
    /**
     * 查询之前，处理trsl表达式的方法
     *
     * @param trsl
     * @param isSimilar
     * @param irSimflag
     * @param resultClass
     * @return
     * @throws TRSSearchException
     * @date Created at 2018年10月9日 下午6:02:46
     * @Author 谷泽昊
     */
    //TODO  ---  处理查询表达式
    private <T extends IDocument> String commonMonthd(String trsl, boolean isSimilar, boolean irSimflag, boolean irSimflagAll,
                                                      boolean isDataSources, Class<T> resultClass) throws TRSSearchException {
        String databases = FtsParser.getDatabases(resultClass);
        return commonMonthd(trsl, isSimilar, irSimflag, irSimflagAll, isDataSources, databases);
    }

    /**
     * 查询之前，处理trsl表达式的方法
     *
     * @param trsl
     * @param isSimilar
     * @param irSimflag
     * @param indices
     * @return
     * @date Created at 2018年10月9日 下午6:02:21
     * @Author 谷泽昊
     */
    //  TODO -- 处理表达式
    private String commonMonthd(String trsl, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, boolean isDataSources,
                                String... indices) {
        // 判断是否排重
        trsl = isSimilar(trsl, isSimilar, irSimflag, irSimflagAll, indices);

        // 去掉被删除的文章
        String trslDel;
        if (StringUtils.isNotBlank(trsl)) {
            trslDel = trsl + isArticleDelete(trsl, indices);
        } else {
            trslDel = isArticleDelete(trsl, indices);
        }
        // 是否限制
        if (isDataSources) {
            // 处理机构时间和机构数据来源
            trslDel = dataSources(trslDel);
        }
        if (StringUtil.getStringKBIsTooLong(trslDel)) {
            log.debug("-------->海贝查询字符串大于16k,被删除的文章除去省略");
            // 是否限制
            if (isDataSources) trsl = dataSources(trsl);
        } else {
            trsl = trslDel;
        }
        User user = UserUtils.getUser();
        String ownerId = user.getId();
        if (StringUtil.isNotEmpty(user.getSubGroupId())) {
            ownerId = user.getSubGroupId();
        }
        HybaseShard trsHybaseShard = null;
        if (UserUtils.isRolePlatform()) {
            //运维
            String valueFromRedis = "";
            valueFromRedis = RedisFactory.getValueFromRedis(ownerId + "xiaoku");
            if (StringUtil.isNotEmpty(valueFromRedis)) {
                trsHybaseShard = ObjectUtil.toObject(valueFromRedis, HybaseShard.class);
            } else {
                trsHybaseShard = hybaseShardService.findByOwnerUserId(ownerId);
            }
        } else {
            String valueFromRedis = "";
            valueFromRedis = RedisFactory.getValueFromRedis(user.getOrganizationId() + "xiaoku");
            if (StringUtil.isNotEmpty(valueFromRedis)) {
                trsHybaseShard = ObjectUtil.toObject(valueFromRedis, HybaseShard.class);
            } else {
                if (StringUtil.isNotEmpty(user.getOrganizationId()))
                    trsHybaseShard = hybaseShardService.findByOrganizationId(user.getOrganizationId());
            }

        }

        if (UserUtils.isRoleVisitor() && ObjectUtil.isNotEmpty(trsHybaseShard)) {
            //加入排除词
            List<KnowledgeBase> byEffective = knowledgeBaseService.findByClassify(KnowledgeClassify.Exclude);
            if (ObjectUtil.isNotEmpty(byEffective) && byEffective.size() > 0) {
                String keywordsTotal = "";
                for (KnowledgeBase knowledgeBase : byEffective) {
                    String keywords = knowledgeBase.getKeywords();
                    if (StringUtil.isNotEmpty(keywords)) {
                        if (keywords.startsWith(";")) {
                            keywords = keywords.substring(1, keywords.length());
                        }
                        if (keywords.endsWith(";")) {
                            keywords = keywords.substring(0, keywords.length() - 1);
                        }

                        keywordsTotal += ";" + keywords;
                    }
                }
                keywordsTotal = keywordsTotal + ";";
                if (StringUtil.isNotEmpty(keywordsTotal)) {
                    if (keywordsTotal.startsWith(";")) {
                        keywordsTotal = keywordsTotal.substring(1, keywordsTotal.length());
                    }
                    if (keywordsTotal.endsWith(";")) {
                        keywordsTotal = keywordsTotal.substring(0, keywordsTotal.length() - 1);
                    }
                    String trslEx = "";
                    StringBuilder exbuilder = new StringBuilder();
                    exbuilder.append("(\"")
                            .append(keywordsTotal.replaceAll("[;|；]+", "\" OR \"")).append("\")");
                    if (StringUtil.isNotEmpty(trsl)) {
                        trslEx = trsl + " AND (*:* -" + FtsFieldConst.FIELD_URLTITLE + ":" + exbuilder.toString() + ") AND (*:* -" + FtsFieldConst.FIELD_CONTENT + ":" + exbuilder.toString() + ")";
                    } else {
                        trslEx = "(*:* -" + FtsFieldConst.FIELD_URLTITLE + ":" + exbuilder.toString() + ") AND (*:* -" + FtsFieldConst.FIELD_CONTENT + ":" + exbuilder.toString() + ")";
                    }

                    if (!StringUtil.getStringKBIsTooLong(trslEx)) return trslEx;
                }

            }
        }

        return trsl;
    }

    /**
     * 处理机构数据来源
     *
     * @param trsl
     * @return
     * @date Created at 2018年10月10日 上午10:22:43
     * @Author 谷泽昊
     */
    // TODO  ----  根据机构信息限制用户可查询数据源
    private String dataSources(String trsl) {
        String organizationId = UserUtils.getUser().getOrganizationId();
        if (StringUtils.isNotBlank(organizationId)) {
            Organization organization = organizationRepository.findOne(organizationId);
            if (organization != null) {
                // 数据来源来源
                String dataSources = organization.getDataSources();
                // 新闻,论坛,博客,微博,微信,客户端,电子报,Twitter
                if (StringUtils.isNotBlank(dataSources) && !StringUtils.equals(dataSources, "ALL")) {
                    String[] dataSourcesArr = dataSources.split(",");
                    StringBuffer buffer = new StringBuffer(trsl);
                    if (dataSourcesArr != null && dataSourcesArr.length > 0) {
                        if (buffer.length() > 0) {
                            buffer.append(" AND ");
                        }
                        buffer.append(FtsFieldConst.FIELD_GROUPNAME).append(":(");
                        int beginLength = buffer.length();
                        for (String dataSource : dataSourcesArr) {
                            buffer.append(Const.SOURCE_GROUPNAME_CONTRAST.get(dataSource)).append(" OR ");
                        }
                        int endLength = buffer.length();
                        // 去掉最后的OR
                        if (endLength >= beginLength + 4) {
                            buffer.delete(endLength - 4, endLength);
                        }
                        buffer.append(")");
                    }
                    trsl = buffer.toString();
                }
            }
        }
        return trsl;
    }

    /**
     * 判断是否排重
     *
     * @param trsl
     * @param isSimilar
     * @return
     * @date Created at 2018年1月2日 下午7:43:05
     * @Author 谷泽昊
     */
    // TODO  ------  这个是给表达式添加排重的  主要是上面的那个方法使用了
    private String isSimilar(String trsl, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String... indices) {
        // simflag排重 1000为不重复
        if (isSimilar) {
            if (StringUtils.isNotBlank(trsl)) {
                trsl = trsl + " AND " + FtsFieldConst.FIELD_SIMFLAG + ":(1000 OR \"\")";
            } else {
                trsl = FtsFieldConst.FIELD_SIMFLAG + ":(1000 OR \"\")";
            }
        }
        // 该配置默认清除历史配置
        if (irSimflagOpen) {
            irSimflag = true;
        }
        if (irSimflag) {
            if (StringUtils.isNotBlank(trsl)) {
                trsl = trsl + " AND (" + Const.IR_SIMFLAG_TRSL + ")";
            } else {
                trsl = Const.IR_SIMFLAG_TRSL;
            }
        }
        if (irSimflagAll) {
            if (StringUtils.isNotBlank(trsl)) {
                trsl = trsl + " AND (" + Const.IR_SIMFLAGALL_TRSL + ")";
            } else {
                trsl = Const.IR_SIMFLAGALL_TRSL;
            }
        }
        return trsl;
    }



    //TODO -----  通过表达式的时间和对应库，拼接用户删除的id    对查询表达式修饰时使用
    private String isArticleDelete(String startTrsl, String... indices) {
        String join = String.join(";", indices);
        String[] split = join.split(";");
        String trsl = FtsFieldConst.FIELD_SID;

        List<String> list = Arrays.asList(split);
        int count = 200;
        if (join.contains(Const.HYBASE_OVERSEAS) && join.contains(Const.INSERT) && list.size() > 2) {
            count = 200 / (list.size() - 2);
        } else if ((join.contains(Const.HYBASE_OVERSEAS) || join.contains(Const.INSERT)) && list.size() > 1) {
            count = 200 / (list.size() - 1);
        }
        String startTime = null;
        String endTime = null;
        //筛选时间 - 因为删除id拼接的逻辑为 删除时间为1个月之内，且在查询时间范围内， type为NULL，时，查询时间为1个月，范围相同
        String filterTime = extractByTrsl(startTrsl, true, null);
        if (StringUtil.isNotEmpty(filterTime) && filterTime.contains("IR_URLTIME:[")) {
            startTime = filterTime.substring(filterTime.indexOf("IR_URLTIME:[") + 12, filterTime.indexOf("IR_URLTIME:[") + 12 + 14);
            //结束时间是查询范围的结束时间，就会出现如果是指定时间，结束时间为几天前，且新删除的数据
            //endTime = filterTime.substring(filterTime.indexOf("IR_URLTIME:[") + 12 + 14 + 7, filterTime.indexOf("IR_URLTIME:[") + 12 + 14 + 7 + 14);
            SimpleDateFormat sdp = new SimpleDateFormat(DateUtil.yyyyMMddHHmmss);
            endTime = sdp.format(new Date());

        }
        boolean isGroupNameWeixin = false;
        ArticleDeleteGroupName groupName = ArticleDeleteGroupName.chuantong;
        String resultTrsl = "";
        for (String db : list) {
            if (db.contains(Const.HYBASE_NI_INDEX)) {
                trsl = FtsFieldConst.FIELD_SID;
                groupName = ArticleDeleteGroupName.chuantong;
            } else if (db.contains(Const.WEIBO)) {
                trsl = FtsFieldConst.FIELD_MID;
                groupName = ArticleDeleteGroupName.weibo;
            } else if (db.contains(Const.WECHAT)) {
                trsl = FtsFieldConst.FIELD_HKEY;
                isGroupNameWeixin = true;
                groupName = ArticleDeleteGroupName.weixin;
            } else if (db.contains(Const.HYBASE_OVERSEAS)) {
                trsl = FtsFieldConst.FIELD_SID;
                groupName = ArticleDeleteGroupName.tf;
            } else {
                break;
            }
            String trslTmp = null;
            if (startTime != null && endTime != null) {
                //只是多了个时间判断
                trslTmp = resultTrsl + articleDeleteTrslNew(trsl, isGroupNameWeixin, groupName, count, Long.valueOf(startTime), Long.valueOf(endTime));
            } else {
                trslTmp = resultTrsl + articleDeleteTrsl(trsl, isGroupNameWeixin, groupName, count);
            }
            //判断大于16kb,删除排重去掉
            if (!StringUtil.getStringKBIsTooLong(trslTmp)) resultTrsl = trslTmp;
        }
        return resultTrsl;
    }

    //TODO ----  给表达式拼接删除文章，限制删除文章的查询时间
    private String articleDeleteTrslNew(String trsl, boolean isGroupNameWeixin, ArticleDeleteGroupName groupName, int count, long startTime, long endTime) {
        User loginUser = UserUtils.getUser();
        if (ObjectUtil.isNotEmpty(loginUser)) {

            Page<ArticleDelete> articleDeletePage = articleDeleteService.findByGroupNameAndUserId(groupName, loginUser, 0,
                    count);
            long time = Long.valueOf(DateUtil.getTimeBeforeOneMonth().replaceAll("-", "").replaceAll(":", "").replaceAll(" ", ""));
            if (articleDeletePage != null) {
                List<ArticleDelete> articleDeleteList = articleDeletePage.getContent();
                if (articleDeleteList != null && articleDeleteList.size() > 0) {
                    StringBuffer buffer = new StringBuffer();
                    SimpleDateFormat sdp = new SimpleDateFormat(DateUtil.yyyyMMddHHmmss);
                    for (ArticleDelete articleDelete : articleDeleteList) {
                        Long deleteTime = Long.valueOf(sdp.format(articleDelete.getCreatedTime()));
                        //日期字符串的大小也会随时间不断增大
                        if (deleteTime >= startTime && deleteTime <= endTime && time <= deleteTime) {
                            String sid = articleDelete.getSid();
                            buffer.append(sid).append(" OR ");
                        }
                    }
                    if (buffer != null && buffer.length() > 4) {
                        buffer.delete(buffer.length() - 4, buffer.length());
                    }
                    String trslTmp;
                    if (buffer != null && buffer.length() > 4) {
                        trslTmp = " NOT " + trsl + ":(" + buffer.toString() + ")";
                        //判断大于16kb,删除排重去掉
                        if (StringUtil.getStringKBIsTooLong(trslTmp)) return trsl;
                        return trslTmp;
                    }
                }
            }
        }
        return "";
    }


    /**
     * 删除文章的表达式
     *
     * @param trsl
     * @param isGroupNameWeixin
     * @param groupName
     * @return
     * @date Created at 2018年10月9日 下午5:57:09
     * @Author 谷泽昊
     */
    //TODO  ----  删除文章，不限制时间，这个用户的就都拼接上
    private String articleDeleteTrsl(String trsl, boolean isGroupNameWeixin, ArticleDeleteGroupName groupName, int count) {
        User loginUser = UserUtils.getUser();
        if (ObjectUtil.isNotEmpty(loginUser)) {
            Page<ArticleDelete> articleDeletePage = articleDeleteService.findByGroupNameAndUserId(groupName, loginUser, 0,
                    count);
            if (articleDeletePage != null) {
                List<ArticleDelete> articleDeleteList = articleDeletePage.getContent();
                if (articleDeleteList != null && articleDeleteList.size() > 0) {
                    StringBuffer buffer = new StringBuffer();
                    for (ArticleDelete articleDelete : articleDeleteList) {
                        String sid = articleDelete.getSid();
                        buffer.append(sid).append(" OR ");
                    }
                    if (buffer != null && buffer.length() > 4) {
                        buffer.delete(buffer.length() - 4, buffer.length());
                    }
                    String trslTmp;
                    if (buffer != null && buffer.length() > 4) {
                        trslTmp = " NOT " + trsl + ":(" + buffer.toString() + ")";
                        //判断大于16kb,删除排重去掉
                        if (StringUtil.getStringKBIsTooLong(trslTmp)) return trsl;
                        return trslTmp;
                    }
                }
            }
        }
        return "";
    }







    /*   *******************************************************************************************************************************************************************************************************
     *  根据表达式抽取检索时间范围
     */

    /**
     * 根据builder抽取检索时间范围参数,以builder中的时间范围为准.如果为空,则以表达式中时间范围为准
     *
     * @param query
     * @return
     * @Return : String
     * @since changjiang @ 2018年6月13日
     */
    // TODO --------   根据builder抽取时间检索范围参数

    private String extractUrlTimeByBuilder(QueryBuilder query, boolean isDataDate, String type) {
        String searchParam = null;
        if (query != null) {
            String start = null;
            String end = null;
            // 判断builder中的开始及结束时间
            Date startTime = query.getStartTime();
            Date endTime = query.getEndTime();
            if (startTime != null && endTime != null) {
                Date[] dataDate = dataDate(startTime, endTime, isDataDate, type);
                start = DateUtil.format2String(dataDate[0], DateUtil.yyyyMMddHHmmssSSS);
                end = DateUtil.format2String(dataDate[1], DateUtil.yyyyMMddHHmmssSSS);
                searchParam = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "]";
            } else {
                // 从表达式中抽取
                searchParam = extractByTrsl(query.asTRSL(), isDataDate, type);
            }
        }
        if (StringUtils.isBlank(searchParam)) {
            searchParam = paramNull(type);
        }
        return searchParam;
    }


    /**
     * 根据builder抽取检索时间范围参数,以builder中的时间范围为准.如果为空,则以表达式中时间范围为准
     *
     * @param query
     * @return
     * @Return : String
     * @since changjiang @ 2018年6月13日
     */
    // TODO ---- 根据混合的列表的builder去抽取时间
    private String extractUrlTimeByCommonBuilder(QueryCommonBuilder query, boolean isDataDate, String type) {
        String searchParam = null;
        if (query != null) {
            String start = null;
            String end = null;
            // 判断builder中的开始及结束时间
            Date startTime = query.getStartTime();
            Date endTime = query.getEndTime();
            if (startTime != null && endTime != null) {
                Date[] dataDate = dataDate(startTime, endTime, isDataDate, type);
                start = DateUtil.format2String(dataDate[0], DateUtil.yyyyMMddHHmmssSSS);
                end = DateUtil.format2String(dataDate[1], DateUtil.yyyyMMddHHmmssSSS);
                searchParam = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "]";
            } else {
                // 从表达式中抽取
                searchParam = extractByTrsl(query.asTRSL(), isDataDate, type);
            }
        }
        if (StringUtils.isBlank(searchParam)) {
            searchParam = paramNull(type);
            log.error("filter param empity!");
        }
        //	System.err.println(searchParam);
        return searchParam;
    }


    /**
     * 根据表达式抽取检索时间范围
     *
     * @param trsl
     * @return
     * @Return : String
     * @since changjiang @ 2018年6月13日
     */
    // TODO ----  根据表达式抽取检索时间范围
    private String extractByTrsl(String trsl, boolean isDataDate, String type) {
        String param = null;
        if (StringUtils.isNotBlank(trsl)) {
            if (trsl.contains(FtsFieldConst.FIELD_URLTIME)) {
                int be = trsl.indexOf(FtsFieldConst.FIELD_URLTIME);
                param = trsl.substring(be, trsl.indexOf("]", be) + 1);
                if (StringUtils.isBlank(param)) {
                    param = paramNull(type);
                }
                String timeRangeByTrsl = TrslUtil.getTimeRangeByTrsl(param);
                if (StringUtils.isNotBlank(timeRangeByTrsl)) {
                    String[] times = timeRangeByTrsl.split(";");
                    if (times != null && times.length > 1) {
                        Date startDate = DateUtil.stringToDate(times[0], DateUtil.yyyyMMddHHmmss);
                        Date endDate = DateUtil.stringToDate(times[1], DateUtil.yyyyMMddHHmmss);
                        // 进行时间对比
                        Date[] dataDate = dataDate(startDate, endDate, isDataDate, type);
                        String start = DateUtil.date2String(dataDate[0], DateUtil.yyyyMMddHHmmssSSS);
                        String end = DateUtil.date2String(dataDate[1], DateUtil.yyyyMMddHHmmssSSS);
                        param = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "]";
                        return param;
                    }
                }

                return paramNull(type);
            } else if (trsl.contains(FtsFieldConst.FIELD_LOADTIME)) {
                int be = trsl.indexOf(FtsFieldConst.FIELD_LOADTIME);
                param = trsl.substring(be, trsl.indexOf("]", be) + 1);
                if (StringUtils.isBlank(param)) {
                    param = paramNull(type);
                }
                String timeRangeByTrsl = TrslUtil.getLoadTimeRangeByTrsl(param);
                if (StringUtils.isNotBlank(timeRangeByTrsl)) {
                    String[] times = timeRangeByTrsl.split(";");
                    if (times != null && times.length > 1) {
                        Date startDate = DateUtil.stringToDate(times[0], DateUtil.yyyyMMddHHmmss);
                        Date endDate = DateUtil.stringToDate(times[1], DateUtil.yyyyMMddHHmmss);
                        // 进行时间对比
                        Date[] dataDate = dataDate(startDate, endDate, isDataDate, type);
                        String start = DateUtil.date2String(dataDate[0], DateUtil.yyyyMMddHHmmssSSS);
                        String end = DateUtil.date2String(dataDate[1], DateUtil.yyyyMMddHHmmssSSS);
                        param = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "]";
                        return param;
                    }
                }

                return paramNull(type);
            } else if (trsl.contains(FtsFieldConst.FIELD_HYLOAD_TIME)) {
                int be = trsl.indexOf(FtsFieldConst.FIELD_HYLOAD_TIME);
                param = trsl.substring(be, trsl.indexOf("]", be) + 1);
                if (StringUtils.isBlank(param)) {
                    param = paramNull(type);
                }
                String timeRangeByTrsl = TrslUtil.getHybaseTimeRangeByTrsl(param);
                if (StringUtils.isNotBlank(timeRangeByTrsl)) {
                    String[] times = timeRangeByTrsl.split(";");
                    if (times != null && times.length > 1) {
                        Date startDate = DateUtil.stringToDate(times[0], DateUtil.yyyyMMddHHmmss);
                        Date endDate = DateUtil.stringToDate(times[1], DateUtil.yyyyMMddHHmmss);
                        // 进行时间对比
                        Date[] dataDate = dataDate(startDate, endDate, isDataDate, type);
                        String start = DateUtil.date2String(dataDate[0], DateUtil.yyyyMMddHHmmssSSS);
                        String end = DateUtil.date2String(dataDate[1], DateUtil.yyyyMMddHHmmssSSS);
                        param = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "]";
                        return param;
                    }
                }

                return paramNull(type);
            }

            try {

                // 切记不要擅自改动,如果后续参数抽取失败,请酌情增加判断及抽取方法,既有代码不允许擅自修改!!!!2018年8月10日
                if (trsl.contains(FtsFieldConst.FIELD_URLDATE) && !trsl.contains(FtsFieldConst.FIELD_URLTIME)) {
                    param = param(trsl, FtsFieldConst.FIELD_URLDATE, isDataDate, type);
                } else if (trsl.contains(FtsFieldConst.FIELD_URLDATE) && trsl.contains(FtsFieldConst.FIELD_URLTIME)) {
                    String start = DateUtil.formatCurrentTime(DateUtil.yyyyMMddHH);
                    start += "000000000";
                    String end = DateUtil.formatCurrentTime(DateUtil.yyyyMMddHHmmssSSS);
                    param = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "]";
                } else if (trsl.contains(FtsFieldConst.FIELD_HYLOAD_TIME)) {
                    param = param(trsl, FtsFieldConst.FIELD_HYLOAD_TIME, isDataDate, type);
                }else if (trsl.contains(FtsFieldConst.FIELD_LOADTIME)) {
                    param = param(trsl, FtsFieldConst.FIELD_LOADTIME, isDataDate, type);
                }
            } catch (Exception e) {
                param = paramNull(type);
                log.error("extractByTrsl error : e.message=" + e.getMessage(), e);
            }
        }
        if (StringUtils.isBlank(param)) {
            param = paramNull(type);
            log.error("filter param empity!");
        }
        return param;
    }


    /**
     * 处理时间(各个数据可检索范围)
     *
     * @param startTime
     * @param endTime
     * @param isDataDate 是否限制
     * @return
     * @date Created at 2018年11月9日 上午10:46:49
     * @author 北京拓尔思信息技术股份有限公司
     * @author 谷泽昊
     */
    // TODO  -------  是通过builder + commonbuilder   抽取表达式里面的时间时也用了，
    //  查询时用  给定了时间，去拿到最终的可检索时间
    private Date[] dataDate(Date startTime, Date endTime, boolean isDataDate, String type) {
        String organizationId = UserUtils.getUser().getOrganizationId();
        if (StringUtils.isNotBlank(organizationId) && isDataDate) {
            Organization organization = organizationRepository.findOne(organizationId);
            if (organization != null) {
                // 计算机构查询时间
                int dataDate = 30;
                if ("column".equals(type)) {
                    dataDate = organization.getColumnDateLimit();
                } else if ("special".equals(type)) {
                    dataDate = organization.getSpecialDateLimit();
                } else if ("advance".equals(type)) {
                    dataDate = organization.getASearchDateLimit();
                } else {
                    return new Date[]{startTime, endTime};
                }
                //int dataDate = organization.getDataDate();
                // 结束时间
                Date endDate = new Date();
                // 开始时间
                Date beginDate = DateUtil.dateAfter(endDate, -dataDate);

                if (beginDate != null) {
                    // 如果表达式的开始时间在机构限制时间之前，则选择机构限制时间
                    if (startTime.before(beginDate)) {
                        startTime = beginDate;
                    }
                    // 如果表达式的结束时间在机构限制时间之后，则选择机构限制时间
                    if (endTime.after(endDate)) {
                        endTime = endDate;
                    }
                    //判断开始时间和结束时间，如果开始时间在结束时间后面，则开始时间等于结束时间
                    if (startTime.after(endTime)) {
                        startTime = endTime;
                    }
                    return new Date[]{startTime, endTime};
                }
            }
        }
        return new Date[]{startTime, endTime};
    }



    /**
     * 当param为空时返回 机构所配置的查询时间
     *
     * @return
     */
    // TODO --- 处理检索时间范围时使用  没拿到时间时，就把时间范围设置成默认的
    private String paramNull(String type) {
        String organizationId = UserUtils.getUser().getOrganizationId();
        if (StringUtils.isNotBlank(organizationId)) {
            Organization organization = organizationRepository.findOne(organizationId);
            if (organization != null) {
                // 计算机构查询时间
                //int dataDate = organization.getDataDate();
                int dataDate = 30;
                if ("column".equals(type)) {
                    dataDate = organization.getColumnDateLimit();
                } else if ("special".equals(type)) {
                    dataDate = organization.getSpecialDateLimit();
                } else if ("advance".equals(type)) {
                    dataDate = organization.getASearchDateLimit();
                } else if ("detail".equals(type)) {
                    //查文章详情
                    dataDate = 365;
                }
                // 结束时间
                Date endDate = new Date();
                // 开始时间
                Date beginDate = DateUtil.dateAfter(endDate, -dataDate);
                SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.yyyyMMddHHmmssSSS);
                // 获取String类型的时间
                String enddate = sdf.format(endDate);
                String startdate = sdf.format(beginDate);
                return FtsFieldConst.FIELD_URLTIME + ":[" + startdate + " TO " + enddate + "]";
            }
        } else {
            int dataDate = 30;
            if ("detail".equals(type)) {
                //查文章详情
                dataDate = 365;
            }
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.yyyyMMddHHmmssSSS);
            // 获取String类型的时间
            String endCatch = sdf.format(date);
            String startCatch = DateUtil.formatDateAfter(date, DateUtil.yyyyMMddHHmmssSSS, -dataDate);
            return FtsFieldConst.FIELD_URLTIME + ":[" + startCatch + " TO " + endCatch + "]";
        }
        return null;
    }

    /**
     * 谷泽昊修改
     * <p>
     * 抽取时间拼成表达式
     * </p>
     *
     * @param trsl
     * @param spiltStr
     * @return
     * @date Created at 2018年11月9日 上午10:52:29
     * @author 北京拓尔思信息技术股份有限公司
     * @author 谷泽昊
     */
    // TODO -----  拿表达式中的时间字段  其他时间字段， 修改为 IR_URLTIME  主要是各种除了时间的方法用了
    private String param(String trsl, String spiltStr, boolean isDataDate, String type) {
        String param = "";
        int be = trsl.indexOf(spiltStr);
        String date = trsl.substring(trsl.indexOf(":", be) + 2, trsl.indexOf(")", be));
        Date startDate = DateUtil.stringToDate(date, DateUtil.yyyyMMdd3);
        long endTime = DateUtil.getDate(startDate.getTime(), 1);
        Date endDate = new Date(endTime);
        Date[] dataDate = dataDate(startDate, endDate, isDataDate, type);
        String start = DateUtil.date2String(dataDate[0], DateUtil.yyyyMMddHHmmssSSS);
        String end = DateUtil.date2String(dataDate[1], DateUtil.yyyyMMddHHmmssSSS);
        param = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "]";
        return param;

    }

    /*   *******************************************************************************************************************************************************************************************************
     *  给查询库加上手工录入库，并且替换小库信息
     */

    /***
     * 所有的库都要加上插入库
     *
     * @param indices
     * @return
     */
    //TODO --- 给要查询的库加上手写库 + 将对应的库的表名替换为对应小库名
    private String addHybaseInsert(String indices) {
        User user = UserUtils.getUser();

        log.debug("------>" + StringUtil.isNotEmpty(indices));
        log.debug("------>" + !indices.contains(Const.SINAUSERS));
        if (StringUtil.isNotEmpty(indices) && !indices.contains(Const.SINAUSERS))
            indices = indices + ";" + Const.INSERT;
        String ownerId = user.getId();
        if (StringUtil.isNotEmpty(user.getSubGroupId())) {
            ownerId = user.getSubGroupId();
        }
        HybaseShard trsHybaseShard = null;
        if (UserUtils.isRolePlatform()) {
            //运维
            String valueFromRedis = "";
            valueFromRedis = RedisFactory.getValueFromRedis(ownerId + "xiaoku");
            if (StringUtil.isNotEmpty(valueFromRedis)) {
                trsHybaseShard = ObjectUtil.toObject(valueFromRedis, HybaseShard.class);
            } else {
                trsHybaseShard = hybaseShardService.findByOwnerUserId(ownerId);
            }
        } else {
            String valueFromRedis = "";
            valueFromRedis = RedisFactory.getValueFromRedis(user.getOrganizationId() + "xiaoku");
            if (StringUtil.isNotEmpty(valueFromRedis)) {
                trsHybaseShard = ObjectUtil.toObject(valueFromRedis, HybaseShard.class);
            } else {
                if (StringUtil.isNotEmpty(user.getOrganizationId()))
                    trsHybaseShard = hybaseShardService.findByOrganizationId(user.getOrganizationId());
            }

        }
        if (ObjectUtil.isNotEmpty(trsHybaseShard)) {

            if (indices.contains(Const.HYBASE_NI_INDEX) && StringUtil.isNotEmpty(trsHybaseShard.getTradition())) {
                indices = indices.replaceAll(Const.HYBASE_NI_INDEX, trsHybaseShard.getTradition());
            }
            if (indices.contains(Const.WEIBO) && StringUtil.isNotEmpty(trsHybaseShard.getWeiBo())) {
                indices = indices.replaceAll(Const.WEIBO, trsHybaseShard.getWeiBo());
            }
            if (indices.contains(Const.WECHAT) && StringUtil.isNotEmpty(trsHybaseShard.getWeiXin())) {
                indices = indices.replaceAll(Const.WECHAT, trsHybaseShard.getWeiXin());
            }
            if (indices.contains(Const.HYBASE_OVERSEAS) && StringUtil.isNotEmpty(trsHybaseShard.getOverseas())) {
                indices = indices.replaceAll(Const.HYBASE_OVERSEAS, trsHybaseShard.getOverseas());
            }
        }
        return indices;
    }


    /*   *******************************************************************************************************************************************************************************************************
     * 抽取数据库
     */

    /**
     * 根据表达式和是否限制系统抽取数据库
     *
     * @param trsl
     * @param isDataSources
     * @return
     * @date Created at 2018年11月12日 下午3:00:16
     * @author 北京拓尔思信息技术股份有限公司
     * @author 谷泽昊
     */
    //TODO --- 根据表达式中的数据源 抽取对应数据库
    public String[] chooseDatabases(String trsl, boolean isDataSources) {
        String[] groupNameArr = TrslUtil.getGroupNameByTrsl(trsl);
        if (groupNameArr != null && groupNameArr.length > 0) {
            String organizationId = UserUtils.getUser().getOrganizationId();
            if (StringUtils.isNotBlank(organizationId) && isDataSources) {
                Organization organization = organizationRepository.findOne(organizationId);
                if (organization != null) {
                    // 数据来源来源
                    String dataSources = organization.getDataSources();
                    // 新闻,论坛,博客,微博,微信,客户端,电子报,Twitter
                    if (StringUtils.isNotBlank(dataSources) && !StringUtils.equals(dataSources, "ALL")) {
                        String[] dataSourcesArr = dataSources.split(",");
                        Set<String> groupNameSet = new HashSet<>();
                        for (String dataSource : dataSourcesArr) {
                            for (String groupName : groupNameArr) {
                                if (groupName.startsWith("\"")) {
                                    groupName = groupName.substring(1, groupName.length());
                                }
                                if (groupName.endsWith("\"")) {
                                    groupName = groupName.substring(0, groupName.length() - 1);
                                }
                                if (StringUtils.equals(Const.SOURCE_GROUPNAME_CONTRAST.get(dataSource), groupName)) {
                                    groupNameSet.add(groupName);
                                }
                            }
                        }
                        if (groupNameSet != null && groupNameSet.size() > 0) {
                            return TrslUtil.chooseDatabases(groupNameSet.toArray(new String[]{}));
                        }
                        return null;
                    }
                }
            }

            return TrslUtil.chooseDatabases(groupNameArr);
        }
        return groupNameArr;
    }

    /*   *******************************************************************************************************************************************************************************************************
     * 下面的为打印日志
     */

    /***
     * 系统日志记录
     *
     * @param trsl
     *            查询语句
     */
    //TODO -- 记录查询表达式
    private void systemLogRecord(String trsl) {
        try {
            RequestAttributes ras = RequestContextHolder.getRequestAttributes();
            if (ras == null)
                return;
            HttpServletRequest request = ((ServletRequestAttributes) ras).getRequest();
            request.setAttribute("system-log-trsl", trsl);
        } catch (Exception e) {
            log.error("获取当前HttpServletRequest失败！");
            e.printStackTrace();
        }
    }

    /**
     * 打印查询时间 只打印超过1000ms的
     *
     * @return
     */
    //TODO  ---- 打印查询时间 --- 打印查询列表的
    public void queryTime(long startHybase, long endHybase, String trsl, int time, int connectTime, String db,
                          String linked) {
        long id = Thread.currentThread().getId();

        LogPrintUtil loginpool = RedisUtil.getLog(Thread.currentThread().getId());
        if (null != loginpool) {
            List<LogEntity> logList = loginpool.getLogList();
            LogEntity entity = new LogEntity();
            entity.setComeHybase(startHybase);
            entity.setFinishHybase(endHybase);
            entity.setConnectHybase(connectTime);
            entity.setDb(db);
            loginpool.setFullHybase(loginpool.getFullHybase() + time);
            entity.setTrsl(trsl);
            entity.setType("查询");
            entity.setLink(linked);
            entity.setEveryHybase(time);
            logList.add(entity);
            loginpool.setLogList(logList);
        }
        RedisUtil.setLog(id, loginpool);
        log.warn(trsl);
        log.warn("连接hybase  耗时" + connectTime + "ms");
        log.warn("原生查询hybase方法   耗时" + time + "ms");
    }

    /**
     * 打印分类统计时间 只打印超过1000ms的
     *
     * @return
     */
    // TODO ----  打印分类统计的时间
    public void categoryTime(long startHybase, long endHybase, String trsl, int time, int connectTime, String db,
                             String field, String linked) {
        long id = Thread.currentThread().getId();

        LogPrintUtil loginpool = RedisUtil.getLog(id);
        if (null != loginpool) {
            List<LogEntity> logList = loginpool.getLogList();
            LogEntity entity = new LogEntity();
            entity.setComeHybase(startHybase);
            entity.setFinishHybase(endHybase);
            entity.setConnectHybase(connectTime);
            entity.setDb(db);
            loginpool.setFullHybase(loginpool.getFullHybase() + time);
            entity.setTrsl(trsl);
            entity.setType("分类统计");
            entity.setLink(linked);
            entity.setField(field);
            entity.setEveryHybase(time);
            logList.add(entity);
            loginpool.setLogList(logList);
        }
        RedisUtil.setLog(id, loginpool);
        log.warn(trsl);
        log.warn("连接hybase  耗时" + connectTime + "ms");
        log.warn("分类统计hybase方法   耗时" + time + "ms");
    }

    /**
     * 打印计数时间
     *
     * @param trsl
     * @param time
     */
    //TODO  -----   打印计算总数查询的查询时间
    public void countTime(long startHybase, long endHybase, String trsl, int time, int connectTime, String db,
                          String linked) {

        long id = Thread.currentThread().getId();

        LogPrintUtil loginpool = RedisUtil.getLog(id);
        if (null != loginpool) {
            List<LogEntity> logList = loginpool.getLogList();
            LogEntity entity = new LogEntity();
            entity.setComeHybase(startHybase);
            entity.setFinishHybase(endHybase);
            entity.setConnectHybase(connectTime);
            entity.setEveryHybase(time);
            entity.setDb(db);
            loginpool.setFullHybase(loginpool.getFullHybase() + time);
            entity.setTrsl(trsl);
            entity.setType("计数");
            entity.setLink(linked);
            logList.add(entity);
            loginpool.setLogList(logList);
        }
        RedisUtil.setLog(id, loginpool);
        log.warn(trsl);
        log.warn("hybase计数   耗时" + time + "ms");
        HybaseFactory.setQueryTime(time);
    }

}
