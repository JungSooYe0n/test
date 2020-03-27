package com.trs.netInsight.widget.analysis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.cache.EnableRedis;
import com.trs.netInsight.widget.analysis.service.IRelationAnalyzeService;

/**
 * 关系挖掘controller
 * 
 * @author songbinbin 2017年5月10日
 */

@RestController
@RequestMapping("/analysis/relation")
public class RelationAnalyzeController {
	@Autowired
	private IRelationAnalyzeService relationAnalyzeService;

	/**
	 * 人物关系 基于微博 包括@某人， 被动@ ， 根据最相关排序出来 ，每次去前10条数据， 按天抽取， 支持最多4 个人物挖掘 本接口按天返回 ，
	 * 只返回在此时间段内的前20 条数据， 大于1天的挖掘， 可按天轮循调用此接口
	 */
	@EnableRedis
	@FormatResult
	@RequestMapping(value = "/relation", method = RequestMethod.GET)
	public Object peopleRelation(@RequestParam(value = "keywords", required = true) String keywords,
			@RequestParam(value = "type", defaultValue = "auto") String type,
			@RequestParam(value = "topNum", defaultValue = "10") String topNum,
			@RequestParam(value = "startTime", required = true) String startTime,
			@RequestParam(value = "endTime", required = true) String endTime) throws TRSException {
		String[] persons = keywords.split(";");
		if (persons.length > 4) {
			throw new TRSException("同时挖掘个数不能大于4");
		}
		if (Integer.valueOf(topNum) > 100) {
			throw new TRSException("单日返回关系人数不能大于100");
		}

		// 同时启动 persons.length 个线程去分析
		// 查询认证信息
		// 查询微博用户表 得到微博user信息 ，取出是否认证 现在没有user库 2017年5月10日15:40:29
		// String[] dates = {startTime, endTime};
		// for (String person : persons) {
		/*
		 * String uid = "12312312312312"; // 根据时间检索 ITRSSearchBuilder
		 * searchBuilder = SearchBuilderFactory.createNoPagedBuilder();
		 * searchBuilder.setPageNo(1);
		 * searchBuilder.setPageSize(Integer.valueOf(topNum));
		 * searchBuilder.filterField("IR_URLTIME", dates, Operator.Between);
		 * searchBuilder.filterField("IR_ME_TXT", person, Operator.Equal);
		 * searchBuilder.filterField("IR_MENTION", person, Operator.Equal);
		 * 
		 * List<String> userList = new ArrayList<>(); Map<String, Integer>
		 * userInfos = new HashMap<String, Integer>(); try {
		 * List<MBlogAnalyzeEntity> weiboList =
		 * mBlogAnalyzeRepository.pageList(searchBuilder).getPageItems();
		 * IEsSearchOpenService esSearchService =
		 * (IEsSearchOpenService)ESFactory.getSearchService("esSearchService");
		 * 
		 * for (MBlogAnalyzeEntity mbBlogAnalyzeEntity : weiboList) {
		 * mbBlogAnalyzeEntity.getAuthor(); mbBlogAnalyzeEntity.getUid();
		 * userList.add(mbBlogAnalyzeEntity.getAuthor()); }
		 * //到最相关的100个监测同一个人发的次数 Set<String> uniqueSet = new
		 * HashSet<String>(userList); for (String user : uniqueSet) {
		 * log.error(user + ": " + Collections.frequency(userList,
		 * user)); //装在map userInfos.put(user, Collections.frequency(userList,
		 * user)); }
		 * 
		 * } catch (Exception e) { e.printStackTrace(); }
		 */
		// }
		return relationAnalyzeService.getPersonList(persons, startTime, endTime, topNum);
	}

}
