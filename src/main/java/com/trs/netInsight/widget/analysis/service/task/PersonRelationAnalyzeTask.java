/*package com.trs.widget.analysis.service.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import com.trs.dc.entity.TRSEsRecord;
import com.trs.dc.entity.TRSEsRecordSet;
import com.trs.dc.entity.TRSEsSearchParams;
import com.trs.dc.openservice.IEsSearchOpenService;
import com.trs.handler.exception.TRSException;
import com.trs.support.fts.FullTextSearch;
import com.trs.support.fts.builder.QueryBuilder;
import com.trs.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PersonRelationAnalyzeTask implements Runnable{
	private final CountDownLatch mDoneSignal;
	private String name;
	private String sql;
	private String topNum;
	private List<Map<String, Object>> resultsMap;
//	private IEsSearchOpenService esSearchService;
	private FullTextSearch esSearchService;
	public PersonRelationAnalyzeTask(IEsSearchOpenService esSearchService, CountDownLatch mDoneSignal,String name, String sql, String topNum, List<Map<String, Object>> resultsMap) {
		this.mDoneSignal = mDoneSignal;
		this.name = name;
		this.sql = sql;
		this.topNum = topNum;
		this.resultsMap = resultsMap;
		this.esSearchService = esSearchService;
	}
	public PersonRelationAnalyzeTask(FullTextSearch esSearchService, CountDownLatch mDoneSignal,String name, String sql, String topNum, List<Map<String, Object>> resultsMap) {
		this.mDoneSignal = mDoneSignal;
		this.name = name;
		this.sql = sql;
		this.topNum = topNum;
		this.resultsMap = resultsMap;
		this.esSearchService = esSearchService;
	}
	@Override
	public void run() {
		try {
			doWork();
		} catch (TRSException e) {
			e.printStackTrace();
		}
		mDoneSignal.countDown();
	}
	private void doWork() throws TRSException{
		// 得到 挖掘的人的信息
		TRSEsSearchParams params = new TRSEsSearchParams();
		params.setQuery(sql);
		params.setSize(5000);
		params.setResultFields("IR_AUTHORS");
		
		try {
//			TRSEsRecordSet recordSet = esSearchService.scan(params,
//					"dc_sina_weibo0711");
			QueryBuilder queryBuilder=new QueryBuilder();
			queryBuilder.filterByTRSL(sql);
			queryBuilder.page(0, 5000);
			queryBuilder
			log.error(recordSet.getNumFound()); // 本次数据导出请求命中的记录数
            HashMap<String, Integer> result = new HashMap<>();
            while (recordSet.getResultSize() > 0) {
                for (TRSEsRecord record : recordSet.getResultSet()) {
                    String key = record.getString("IR_SCREEN_NAME");
                    if (StringUtil.isNotEmpty(key))
                        result.put(key, result.containsKey(key) ? result.get(key) + 1 : 1);
                }
                recordSet = esSearchService.scanNextPage(recordSet.getScrollId());
            }
            List<Map.Entry<String, Integer>> list = new ArrayList<>(result.entrySet());
            list.sort((o1, o2) -> o2.getValue() - o1.getValue());
            List<Map.Entry<String, Integer>> sortData = list.size() > Integer.valueOf(topNum) ? list.subList(0, Integer.valueOf(topNum)) : list;
			Map<String,Object> singleData = new HashMap<>();
			singleData.put(name, sortData);
			resultsMap.add(singleData);
            for (Map.Entry<String, Integer> meEntry : sortData) {
            	log.error(meEntry.getKey() + "_" + meEntry.getValue());
            }
		} catch (Exception e) {
			throw new TRSException("任务关系挖掘，查询es出错:" + e);
		}
	}
}
*/