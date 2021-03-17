package com.trs.netInsight.widget.column.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.jpa.utils.Criteria;
import com.trs.jpa.utils.Restrictions;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.ESFieldConst;
import com.trs.netInsight.config.constant.ExcelConst;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.*;
import com.trs.netInsight.support.fts.model.result.IDocument;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.report.excel.DataRow;
import com.trs.netInsight.support.report.excel.ExcelData;
import com.trs.netInsight.support.report.excel.ExcelFactory;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.alert.entity.repository.AlertRepository;
import com.trs.netInsight.widget.analysis.entity.CategoryBean;
import com.trs.netInsight.widget.analysis.service.IDistrictInfoService;
import com.trs.netInsight.widget.analysis.service.impl.ChartAnalyzeService;
import com.trs.netInsight.widget.column.entity.*;
import com.trs.netInsight.widget.column.entity.emuns.ChartPageInfo;
import com.trs.netInsight.widget.column.entity.emuns.ColumnFlag;
import com.trs.netInsight.widget.column.entity.emuns.IndexFlag;
import com.trs.netInsight.widget.column.entity.emuns.StatisticalChartInfo;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.column.entity.pageShow.CustomChartDTO;
import com.trs.netInsight.widget.column.entity.pageShow.IndexPageDTO;
import com.trs.netInsight.widget.column.entity.pageShow.IndexTabDTO;
import com.trs.netInsight.widget.column.entity.pageShow.StatisticalChartDTO;
import com.trs.netInsight.widget.column.factory.AbstractColumn;
import com.trs.netInsight.widget.column.factory.ColumnConfig;
import com.trs.netInsight.widget.column.factory.ColumnFactory;
import com.trs.netInsight.widget.column.repository.*;
import com.trs.netInsight.widget.column.service.IColumnChartService;
import com.trs.netInsight.widget.column.service.IColumnService;
import com.trs.netInsight.widget.column.service.IIndexTabMapperService;
import com.trs.netInsight.widget.column.service.IIndexTabService;
import com.trs.netInsight.widget.common.service.ICommonChartService;
import com.trs.netInsight.widget.common.service.ICommonListService;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.special.entity.InfoListResult;
import com.trs.netInsight.widget.special.service.IInfoListService;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Sort.Order;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 栏目相关接口服务实现类
 * <p>
 *      @author 北京拓尔思信息技术股份有限公司
 * Create by yan.changjiang on 2017年11月27日
 */
@Service
@Slf4j
public class ColumnServiceImpl implements IColumnService {

	@Autowired
	private IndexTabRepository indexTabRepository;

	@Autowired
	private IndexPageRepository indexPageRepository;

	@Autowired
	private FullTextSearch hybase8SearchService;

	@Autowired
	private ChartAnalyzeService chartAnalyzeService;

	@Autowired
	private IDistrictInfoService districtInfoService;

	@Autowired
	private ColumnRepository columnRepository;

	@Autowired
	private IInfoListService infoListService;

	@Autowired
	private IIndexTabMapperService indexTabMapperService;

	@Autowired
	private IndexTabMapperRepository tabMapperRepository;
	@Autowired
	private IndexSequenceRepository sequenceRepository;
	@Autowired
	private ICommonListService commonListService;
	@Autowired
	private ICommonChartService commonChartService;
	@Autowired
	private UserRepository userService;
	@Autowired
	private IColumnChartService columnChartService;
	@Autowired
	private StatisticalChartRepository statisticalChartRepository;
	@Autowired
	private CustomChartRepository customChartRepository;


	public Object selectNextShowColumn(String id,ColumnFlag columnFlag){
		User user = UserUtils.getUser();
		IndexPage parent = null;
		String topFlag = null;
		Map<String,Object> map = new HashMap<>();
		if(ColumnFlag.IndexPageFlag.equals(columnFlag)){
			IndexPage indexPage = indexPageRepository.findOne(id);
			if(indexPage != null){
				if(StringUtil.isNotEmpty(indexPage.getParentId())){
					parent = indexPageRepository.findOne(indexPage.getParentId());
				}
			}
		}else if(ColumnFlag.IndexTabFlag.equals(columnFlag)){
			IndexTabMapper mapper = tabMapperRepository.findOne(id);
			if(mapper!=null){
				topFlag = mapper.getTopFlag();
				parent = mapper.getIndexPage();
			}
		}
		Object result = null;
		if(parent != null){  //在分组下

			List<IndexPage> oneIndexPage = parent.getChildrenPage();
			List<IndexTabMapper> oneIndexTab = parent.getIndexTabMappers();
			List<Object> sortColumn = this.sortColumn(new ArrayList<Object>(),oneIndexTab,oneIndexPage,false,false);
			if(sortColumn == null || sortColumn.size() <=1){
				result = parent;
			}else{
				result = getNextNode(sortColumn,id,columnFlag);
			}
		}else{ // 第一层的数据， 置顶的和不置顶的分开
			List<IndexPage> oneIndexPage = null;
			List<IndexTabMapper> oneIndexTab = null;
			List<IndexTabMapper> top = null;
			Map<String,Object> oneColumn = this.getOneLevelColumnForMap("",user);
			if (oneColumn.containsKey("page")) {
				oneIndexPage = (List<IndexPage>) oneColumn.get("page");
			}
			if (oneColumn.containsKey("tab")) {
				oneIndexTab = (List<IndexTabMapper>) oneColumn.get("tab");
			}
			if (oneColumn.containsKey("top")) {
				top = (List<IndexTabMapper>) oneColumn.get("top");
			}
			List<Object> sortColumn = this.sortColumn(new ArrayList<Object>(),oneIndexTab,oneIndexPage,false,false);
			if("top".equals(topFlag)){
				//List<IndexTabMapper> findTop = getTopIndexTabMapper(user);
				if(top != null && top.size() >1){
					int index = 0;
					for(int i = 0;i <top.size();i++){
						if(id.equals(top.get(i).getId())){
							index = i;
							break;
						}
					}
					if(index == top.size()-1){ // 这里是用的下标判断，判断当前这个是否是整个top的最后一个下标，是则拿前一个
						result = top.get(index-1);
					}else{
						result = top.get(index+1);
					}
				}else{
					// 找到所有元素中的第一个
					if(sortColumn == null || sortColumn.size() ==0){
						result = null;
					}else{
						result = sortColumn.get(0);
					}
				}
			}else{
				// 找到当前元素的下一个有东西的元素 下一个没有就找上一个 不直接用seq 判断是因为可能存在seq错误的情况，但是都是按照上面的方法排序后的数据，跟页面一致
				// 找到所有元素中的第一个
				if(sortColumn == null || sortColumn.size() <=1){
					//如果第一层只有当前一个，看他上面有没有置顶的
					if(top != null && top.size() >0){
						result = top.get(top.size() -1);
					}else{
						result = null;
					}
				}else{
					result = getNextNode(sortColumn,id,columnFlag);
				}
			}
		}
		if(result == null){
			map.put("id",null);
			map.put("flag",null);
			map.put("name",null);
		}else{
			if (result instanceof IndexTabMapper) {
				IndexTabMapper mapper = (IndexTabMapper)result;
				map.put("id",mapper.getId());
				map.put("flag",1);
				map.put("name",mapper.getIndexTab().getName());
			} else if (result instanceof IndexPage) {
				IndexPage page = (IndexPage)result;
				map.put("id",page.getId());
				map.put("flag",0);
				map.put("name",page.getName());
			}
		}
		return map;
	}

	private Object getNextNode(List<Object> sortColumn,String id,ColumnFlag columnFlag){
		if(sortColumn != null && sortColumn.size() >0){
			int index = -1;
			for(int i = 0;i <sortColumn.size();i++){
				if (sortColumn.get(i) instanceof IndexTabMapper) {
					IndexTabMapper mapper = (IndexTabMapper)sortColumn.get(i);
					if(id.equals(mapper.getId())  && ColumnFlag.IndexTabFlag.equals(columnFlag)){
						index = i;
						break;
					}
				} else if (sortColumn.get(i) instanceof IndexPage) {
					IndexPage page = (IndexPage)sortColumn.get(i);
					if(id.equals(page.getId())  && ColumnFlag.IndexPageFlag.equals(columnFlag)){
						index = i;
						break;
					}
				}
			}
			if(index != -1){
				if(index == sortColumn.size()-1){ // 这里是用的下标判断，判断当前这个是否是整个数据的最后一个下标，是则拿前一个
					return sortColumn.get(index-1);
				}else{
					return sortColumn.get(index+1);
				}
			}else{
				return null;
			}
		}
		return null;
	}


	private List<IndexTabMapper> getTopIndexTabMapper(User user){
		List<Sort.Order> orders=new ArrayList<Sort.Order>();
		orders.add( new Sort.Order(Sort.Direction.ASC, "sequence"));
		orders.add( new Sort.Order(Sort.Direction.ASC, "createdTime"));
		Criteria<IndexTabMapper> criteria = new Criteria<>();
		criteria.add(Restrictions.eq("topFlag", "top"));
		if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
			criteria.add(Restrictions.eq("userId", user.getId()));
		}else {
			criteria.add(Restrictions.eq("subGroupId", user.getSubGroupId()));
		}
		 return tabMapperRepository.findAll(criteria,new Sort(orders));
	}

	/**
	 * 置顶一个栏目
	 * @param user 当前用户信息
	 * @return
	 */
	public Object topColumn(String id,User user){
		List<IndexTabMapper> findTop = getTopIndexTabMapper(user);
		if(findTop != null && findTop.size() >0){
			int i =2;
			for (IndexTabMapper mapper : findTop) {
				mapper.setSequence(i);
				i++;
				tabMapperRepository.save(mapper);
			}
		}
		IndexTabMapper findOne = tabMapperRepository.findOne(id);
		if(findOne.getIndexPage()!= null){
			findOne.setBakParentId(findOne.getIndexPage().getId());
		}
		findOne.setIndexPage(null);
		findOne.setSequence(1);
		findOne.setTopFlag("top");
		tabMapperRepository.save(findOne);
		return findOne;
	}

	/**
	 * 取消置顶栏目
	 * @param user 当前用户信息
	 * @return
	 */
	public Object noTopColumn(String id,User user){
		IndexTabMapper findOne = tabMapperRepository.findOne(id);
		findOne.setTopFlag(null);
		String parentId = null;
		if(StringUtil.isNotEmpty(findOne.getBakParentId())){
			IndexPage parent = indexPageRepository.findOne(findOne.getBakParentId());
			if(parent != null && StringUtil.isNotEmpty(parent.getId())){
				parentId = parent.getId();
				findOne.setIndexPage(parent);
			}
		}
		Integer  seq = this.getMaxSequenceForColumn(parentId,"",user) +1;
		findOne.setSequence(seq);
		findOne.setBakParentId(null);
		// 放在原来列表最后一个 查找时按照sequence排列
		tabMapperRepository.save(findOne);
		return findOne;
	}


	/**
	 * 获取层级下最的排序值
	 * @param parentPageId 要获取排序值的对象的上级分组id ，如果没有上级分组，则为空
	 * @param navigationId 对应的模块id，默认日常监测模块为""，
	 * @param user 当前用户信息
	 * @return
	 */
	public Integer getMaxSequenceForColumn(String parentPageId,String navigationId,User user){
		Integer seq = 0;
		List<IndexPage> indexPage = new ArrayList<>();
		List<IndexTabMapper> indexTabMapper = new ArrayList<>();
		if(StringUtil.isEmpty(parentPageId)){
			Map<String,Object> oneColumn = this.getOneLevelColumnForMap(navigationId,user);
			if (oneColumn.containsKey("page")) {
				indexPage = (List<IndexPage>) oneColumn.get("page");
			}
			if (oneColumn.containsKey("tab")) {
				indexTabMapper = (List<IndexTabMapper>) oneColumn.get("tab");
			}
		}else{
			IndexPage parentPage =indexPageRepository.findOne(parentPageId);
			indexPage = parentPage.getChildrenPage();
			indexTabMapper = parentPage.getIndexTabMappers();
		}
		List<Object> sortColumn = this.sortColumnAll(new ArrayList<Object>(),indexTabMapper,indexPage,false,false);
		if(sortColumn != null && sortColumn.size() > 0){
			Object column = sortColumn.get(sortColumn.size()-1);
			if (column instanceof IndexTabMapper) {
				seq = ((IndexTabMapper)column).getSequence();
			} else if (column instanceof IndexPage) {
				seq = ((IndexPage)column).getSequence();
			}else if (column instanceof CustomChartDTO){
				seq = ((CustomChartDTO) column).getSequence();
			}else if (column instanceof StatisticalChartDTO){
				seq = ((StatisticalChartDTO) column).getSequence();
			}
		}
		return seq;
	}

	/**
	 * 重新排序
	 * @param mapperList
	 * @param indexPageList
	 * @param sortAll
	 * @param onlySortPage
	 * @return
	 */
	public void newSortColumnAll(List<IndexTabMapper> mapperList,List<IndexPage> indexPageList,Boolean sortAll,Boolean onlySortPage,String prentId){


		List<Map<String,Object>> sortList = new ArrayList<>();
		List<Object> listZi = new ArrayList<>();
		if(!onlySortPage){
			if(mapperList != null && mapperList.size() >0){
				for(int i =0;i<mapperList.size();i++){
					Map<String,Object> map = new HashMap<>();
					map.put("id",mapperList.get(i).getId());
//					IndexSequence indexSequence = sequenceRepository.findByIndexId(mapperList.get(i).getId());
					map.put("sequence",mapperList.get(i).getSequence());
					map.put("index",i);
					//栏目类型为1
					map.put("flag",IndexFlag.IndexTabFlag);
					sortList.add(map);
//					listZi.addAll(getTopColumnChartForTab(sortList,mapperList.get(i)));
				}
			}
		}
		if(indexPageList != null && indexPageList.size() > 0){
			for(int i =0;i<indexPageList.size();i++){
				Map<String,Object> map = new HashMap<>();
				map.put("id",indexPageList.get(i).getId());
				map.put("sequence",indexPageList.get(i).getSequence());
				map.put("index",i);
				//分组类型为0
				map.put("flag", IndexFlag.IndexPageFlag);
				sortList.add(map);
			}
		}
		if(sortList.size() >0){
			Collections.sort(sortList, (o1, o2) -> {
				Integer seq1 = (Integer) o1.get("sequence");
				Integer seq2 = (Integer) o2.get("sequence");
				return seq1.compareTo(seq2);
			});
			List<Map<String,Object>> newSortList = new ArrayList<>();//单层级顺序
			for(Map<String,Object> map : sortList){
				IndexFlag flag = (IndexFlag) map.get("flag");
				Integer index = (Integer) map.get("index");
				if(flag.equals(IndexFlag.IndexPageFlag)){
					newSortList.add(map);
				}else if(flag.equals(IndexFlag.IndexTabFlag)){
					newSortList.add(map);
					IndexTabMapper mapper = mapperList.get(index);
					//栏目有子节点
					List<Object> columnChartList = new ArrayList<>();
					String timeRange = mapper.getIndexTab().getTimeRange();
					Sort sort = new Sort(Sort.Direction.ASC, "topSequence");
					//获取统计分析中被置顶的数据
					List<StatisticalChart> statisticalChartList = statisticalChartRepository.findByParentIdAndIsTop(mapper.getId(), true, sort);
					int num = 0;
					if (statisticalChartList != null && statisticalChartList.size() > 0) {
						for (StatisticalChart oneSc : statisticalChartList) {
							num++;
							StatisticalChartInfo statisticalChartInfo = StatisticalChartInfo.getStatisticalChartInfo(oneSc.getChartType());
							StatisticalChartDTO statisticalChartDTO = new StatisticalChartDTO(oneSc,statisticalChartInfo,timeRange);
							//拿到统计分析图的基本数据
							columnChartList.add(statisticalChartDTO);
							Map<String,Object> newmap = new HashMap<>();
							newmap.put("id",statisticalChartDTO.getId());
							newmap.put("sequence",statisticalChartDTO.getSequence());
							newmap.put("index",num);
							//栏目类型为1
							newmap.put("flag",IndexFlag.StatisticalFlag);
							newmap.put("indexTabId",(String)map.get("id"));
							newSortList.add(newmap);
						}
					}
					//获取当前栏目被置顶的自定义图表
					List<CustomChart> customChartList = customChartRepository.findByParentIdAndIsTop(mapper.getId(), true, sort);
					if (customChartList != null && customChartList.size() > 0) {
						for (CustomChart oneCc : customChartList) {
							num++;
							CustomChartDTO customChartDTO = new CustomChartDTO(oneCc);
							columnChartList.add(customChartDTO);
							Map<String,Object> newmap = new HashMap<>();
							newmap.put("id",customChartDTO.getId());
							newmap.put("sequence",customChartDTO.getSequence());
							newmap.put("index",num);
							//栏目类型为1
							newmap.put("flag",IndexFlag.CustomFlag);
							newmap.put("indexTabId",(String)map.get("id"));
							newSortList.add(newmap);
						}
					}

				}
			}
			//排序过后存表
			Integer seq = 0;
			for(Map<String,Object> map : newSortList){
				seq++;
				IndexFlag flag = (IndexFlag) map.get("flag");
				String id = (String) map.get("id");
				if(flag.equals(IndexFlag.IndexPageFlag)){
					IndexPage indexPage = indexPageRepository.findOne(id);
					List<IndexSequence> indexSequenceList = sequenceRepository.findByIndexId(id);
					if (ObjectUtil.isEmpty(indexSequenceList)){
						IndexSequence indexSequence = new IndexSequence();
						indexSequence.setIndexId(id);
						indexSequence.setParentId(prentId);
						indexSequence.setSequence(seq);
						indexSequence.setIndexFlag(IndexFlag.IndexPageFlag);
						indexSequence.setIndexTabId((String) map.get("id"));
						sequenceRepository.save(indexSequence);
					}
					if(sortAll){
						//获取子类的数据进行排序
						List<IndexPage> child_page = indexPage.getChildrenPage();
						List<IndexTabMapper> child_mapper = null;
						if(!onlySortPage){
							child_mapper = indexPage.getIndexTabMappers();
						}
						if( (child_mapper != null && child_mapper.size()>0) || (child_page != null && child_page.size() >0) ){
//							indexPage.setColumnList(newSortColumnAll(child_mapper,child_page,sortAll,onlySortPage,id));
							newSortColumnAll(child_mapper,child_page,sortAll,onlySortPage,id);
						}
					}
				}else if(flag.equals(IndexFlag.IndexTabFlag)){
					IndexTabMapper mapper = indexTabMapperService.findOne(id);
					List<IndexSequence> indexSequenceList = sequenceRepository.findByIndexId(id);
					if (ObjectUtil.isEmpty(indexSequenceList)){
						IndexSequence indexSequence = new IndexSequence();
						indexSequence.setIndexId(id);
						if (ObjectUtil.isEmpty(mapper.getIndexPage())){
							indexSequence.setParentId("");
						}else {
							indexSequence.setParentId(mapper.getIndexPage().getId());
						}

						indexSequence.setSequence(seq);
						indexSequence.setIndexFlag(IndexFlag.IndexTabFlag);
						indexSequence.setIndexTabId((String) map.get("id"));
						sequenceRepository.save(indexSequence);
					}
				} else if (flag.equals(IndexFlag.StatisticalFlag)) {
					List<IndexSequence> indexSequenceList  = sequenceRepository.findByIndexId(id);
					if (ObjectUtil.isEmpty(indexSequenceList)){
						IndexSequence indexSequence = new IndexSequence();
//						StatisticalChart statisticalChart = statisticalChartRepository.findOne(id);
						indexSequence.setIndexId(id);
						indexSequence.setParentId(prentId);
						indexSequence.setSequence(seq);
						indexSequence.setIndexFlag(IndexFlag.StatisticalFlag);
						indexSequence.setIndexTabId((String) map.get("indexTabId"));
						sequenceRepository.save(indexSequence);
					}
				}else if (flag.equals(IndexFlag.CustomFlag)) {
					List<IndexSequence> indexSequenceList = sequenceRepository.findByIndexId(id);
					if (ObjectUtil.isEmpty(indexSequenceList)){
						IndexSequence indexSequence = new IndexSequence();
//						CustomChart customChart = customChartRepository.findOne(id);
						indexSequence.setIndexId(id);
						indexSequence.setParentId(prentId);
						indexSequence.setSequence(seq);
						indexSequence.setIndexFlag(IndexFlag.CustomFlag);
						indexSequence.setIndexTabId((String) map.get("indexTabId"));
						sequenceRepository.save(indexSequence);
					}
				}
			}
		}
sequenceRepository.flush();
	}
	public List<Object> sortColumnAll(List<Object> result,List<IndexTabMapper> mapperList,List<IndexPage> indexPageList,Boolean sortAll,Boolean onlySortPage){


		List<Map<String,Object>> sortList = new ArrayList<>();
		List<Object> listZi = new ArrayList<>();
		if(!onlySortPage){
			if(mapperList != null && mapperList.size() >0){
				for(int i =0;i<mapperList.size();i++){
					if (sortAll) {
						if (!mapperList.get(i).isHide()) {
							Map<String, Object> map = new HashMap<>();
							map.put("id", mapperList.get(i).getId());
							List<IndexSequence> indexSequenceList = sequenceRepository.findByIndexId(mapperList.get(i).getId());
							if (ObjectUtil.isNotEmpty(indexSequenceList)) {
								map.put("sequence", indexSequenceList.get(0).getSequence());
							} else {
								map.put("sequence", mapperList.get(i).getSequence());
							}
//							map.put("sequence", indexSequence.getSequence());
							map.put("index", i);
							//栏目类型为1
							map.put("flag", IndexFlag.IndexTabFlag);
							sortList.add(map);
							listZi.addAll(getTopColumnChartForTab(sortList, mapperList.get(i), listZi.size(),sortAll));
						}
					}else {
						Map<String, Object> map = new HashMap<>();
						map.put("id", mapperList.get(i).getId());
						List<IndexSequence> indexSequenceList = sequenceRepository.findByIndexId(mapperList.get(i).getId());
//						IndexSequence indexSequence = sequenceRepository.findByIndexId(mapperList.get(i).getId());
//						map.put("sequence", indexSequence.getSequence());
						if (ObjectUtil.isNotEmpty(indexSequenceList)) {
							map.put("sequence", indexSequenceList.get(0).getSequence());
						} else {
							map.put("sequence", mapperList.get(i).getSequence());
						}
						map.put("index", i);
						//栏目类型为1
						map.put("flag", IndexFlag.IndexTabFlag);
						sortList.add(map);
						listZi.addAll(getTopColumnChartForTab(sortList, mapperList.get(i), listZi.size(),sortAll));
					}
				}
			}
		}
		if(indexPageList != null && indexPageList.size() > 0){
			for(int i =0;i<indexPageList.size();i++){
				if (!indexPageList.get(i).isHide() || !sortAll) {
					Map<String, Object> map = new HashMap<>();
					map.put("id", indexPageList.get(i).getId());
//					IndexSequence indexSequence = sequenceRepository.findByIndexId(indexPageList.get(i).getId());
					List<IndexSequence> indexSequenceList = sequenceRepository.findByIndexId(indexPageList.get(i).getId());
					if (ObjectUtil.isNotEmpty(indexSequenceList)) {
						map.put("sequence", indexSequenceList.get(0).getSequence());
					}
					map.put("index", i);
					//分组类型为0
					map.put("flag", IndexFlag.IndexPageFlag);
					sortList.add(map);
				}
			}
		}
		if(sortList.size() >0){
			Collections.sort(sortList, (o1, o2) -> {
				Integer seq1 = (Integer) o1.get("sequence");
				Integer seq2 = (Integer) o2.get("sequence");
				return seq1.compareTo(seq2);
			});
			//sortList 排序过后的数据
			//只排序当前层，排序过后的数据，按顺序取出并返回
			for(Map<String,Object> map : sortList){
				IndexFlag flag = (IndexFlag) map.get("flag");
				Integer index = (Integer) map.get("index");
				if(flag.equals(IndexFlag.IndexPageFlag)){
					IndexPage indexPage = indexPageList.get(index);
					if(sortAll){
						//获取子类的数据进行排序
						List<IndexPage> child_page = indexPage.getChildrenPage();
						List<IndexTabMapper> child_mapper = null;
						if(!onlySortPage){
							child_mapper = indexPage.getIndexTabMappers();
						}
						if( (child_mapper != null && child_mapper.size()>0) || (child_page != null && child_page.size() >0) ){
							indexPage.setColumnList(sortColumnAll(new ArrayList<Object>(),child_mapper,child_page,sortAll,onlySortPage));
						}
					}
					result.add(indexPage);
				}else if(flag.equals(IndexFlag.IndexTabFlag)){
					IndexTabMapper mapper = mapperList.get(index);
					//栏目只有一层，直接添加就行
					result.add(mapper);
				} else if (flag.equals(IndexFlag.StatisticalFlag)) {
					result.add(listZi.get(index));
				}else if (flag.equals(IndexFlag.CustomFlag)) {
					result.add(listZi.get(index));
				}
			}
		}

		return result;
	}
	private List<Object> getTopColumnChartForTab(List<Map<String,Object>> sortList,IndexTabMapper mapper,int size,boolean sortAll){
		List<Object> result = new ArrayList<>();
		if (ObjectUtil.isEmpty(mapper)) {
			return result;
		}
		List<Object> columnChartList = new ArrayList<>();
		String timeRange = mapper.getIndexTab().getTimeRange();
		Sort sort = new Sort(Sort.Direction.ASC, "topSequence");
		//获取统计分析中被置顶的数据
		List<StatisticalChart> statisticalChartList = statisticalChartRepository.findByParentIdAndIsTop(mapper.getId(), true, sort);
		int num = size;
		if (statisticalChartList != null && statisticalChartList.size() > 0) {
			for (StatisticalChart oneSc : statisticalChartList) {
				StatisticalChartInfo statisticalChartInfo = StatisticalChartInfo.getStatisticalChartInfo(oneSc.getChartType());
				StatisticalChartDTO statisticalChartDTO = new StatisticalChartDTO(oneSc,statisticalChartInfo,timeRange);
				if (sortAll) {
					if (!oneSc.getHide()) {
						//拿到统计分析图的基本数据
						//为统计分析的折现图加上groupName
						if(mapper.getIndexTab() != null && !"".equals(mapper.getIndexTab()) && "brokenLineChart".equals(statisticalChartDTO.getChartType())) {
							statisticalChartDTO.setGroupName(mapper.getIndexTab().getGroupName());
						}
						columnChartList.add(statisticalChartDTO);
						Map<String, Object> map = new HashMap<>();
						map.put("id", statisticalChartDTO.getId());
						List<IndexSequence> indexSequenceList = sequenceRepository.findByIndexId(statisticalChartDTO.getId());
						if (ObjectUtil.isNotEmpty(indexSequenceList)) {
							map.put("sequence", indexSequenceList.get(0).getSequence());
						} else {
							map.put("sequence", statisticalChartDTO.getSequence());
						}

						map.put("index", num);
						//栏目类型为1
						map.put("flag", IndexFlag.StatisticalFlag);
						sortList.add(map);
						num++;
					}
				}else {
					columnChartList.add(statisticalChartDTO);
					Map<String, Object> map = new HashMap<>();
					map.put("id", statisticalChartDTO.getId());
					List<IndexSequence> indexSequenceList = sequenceRepository.findByIndexId(statisticalChartDTO.getId());
					if (ObjectUtil.isNotEmpty(indexSequenceList)) {
						map.put("sequence", indexSequenceList.get(0).getSequence());
					} else {
						map.put("sequence", statisticalChartDTO.getSequence());
					}

					map.put("index", num);
					//栏目类型为1
					map.put("flag", IndexFlag.StatisticalFlag);
					sortList.add(map);
					num++;
				}
			}
		}
		//获取当前栏目被置顶的自定义图表
		List<CustomChart> customChartList = customChartRepository.findByParentIdAndIsTop(mapper.getId(), true, sort);
		if (customChartList != null && customChartList.size() > 0) {
			for (CustomChart oneCc : customChartList) {
				if (sortAll) {
					if (!oneCc.isHide()) {
						CustomChartDTO customChartDTO = new CustomChartDTO(oneCc);
						columnChartList.add(customChartDTO);
						Map<String, Object> map = new HashMap<>();
						map.put("id", customChartDTO.getId());
						List<IndexSequence> indexSequenceList = sequenceRepository.findByIndexId(customChartDTO.getId());
						if (ObjectUtil.isNotEmpty(indexSequenceList)) {
							map.put("sequence", indexSequenceList.get(0).getSequence());
						} else {
							map.put("sequence", customChartDTO.getSequence());
						}

						map.put("index", num);
						//栏目类型为1
						map.put("flag", IndexFlag.CustomFlag);
						sortList.add(map);
						num++;
					}
				}else {
					CustomChartDTO customChartDTO = new CustomChartDTO(oneCc);
					columnChartList.add(customChartDTO);
					Map<String, Object> map = new HashMap<>();
					map.put("id", customChartDTO.getId());
					List<IndexSequence> indexSequenceList = sequenceRepository.findByIndexId(customChartDTO.getId());
					if (ObjectUtil.isNotEmpty(indexSequenceList)) {
						map.put("sequence", indexSequenceList.get(0).getSequence());
					} else {
						map.put("sequence", customChartDTO.getSequence());
					}

					map.put("index", num);
					//栏目类型为1
					map.put("flag", IndexFlag.CustomFlag);
					sortList.add(map);
					num++;
				}
			}
		}
		if(columnChartList.size() ==0 ){
			return result;
		}
		result.addAll(columnChartList);
		return result;
	}
	/**
	 * 对日常监测分组和栏目进行排序展示
	 * @param result 这是排序后的结果放到这个中，这个是结果对象，因为 top的东西肯定是在前面，是在外面添加的，写这个是因为方便把top的数据先放进去
	 * @param mapperList  栏目数据List
	 * @param indexPageList 分组数据List
	 * @param sortAll 是否对所有层进行排序，包括当前数据及其子类， 查询列表时对整个列表排序，则为true，如果只是拖拽修改顺序则只排一层即可，为false
	 * @param onlySortPage 是否只对分组进行排序，因为有时需要获取日常监测下的所有分组，但不需要栏目
	 * @return
	 */
	public List<Object> sortColumn(List<Object> result,List<IndexTabMapper> mapperList,List<IndexPage> indexPageList,Boolean sortAll,Boolean onlySortPage){


		List<Map<String,Object>> sortList = new ArrayList<>();
//		List<Map<String,Object>> sortList2 = new ArrayList<>();
		if(!onlySortPage){
			if(mapperList != null && mapperList.size() >0){
				for(int i =0;i<mapperList.size();i++){
					if (!mapperList.get(i).isHide()) {
						Map<String, Object> map = new HashMap<>();
						map.put("id", mapperList.get(i).getId());
						List<IndexSequence> indexSequenceList = sequenceRepository.findByIndexId(mapperList.get(i).getId());
						if (ObjectUtil.isNotEmpty(indexSequenceList)) {
							map.put("sequence", indexSequenceList.get(0).getSequence());
						} else {
							map.put("sequence", mapperList.get(i).getSequence());
						}

						map.put("index", i);
						//栏目类型为1
						map.put("flag", IndexFlag.IndexTabFlag);
						sortList.add(map);
//						getTopColumnChartForTab(sortList2, mapperList.get(i), 0);//这里把自定义以及统计的子节点加进来 但是不返回
					}
				}
			}
		}
		if(indexPageList != null && indexPageList.size() > 0){
			for(int i =0;i<indexPageList.size();i++){
				if (!indexPageList.get(i).isHide()) {
					Map<String, Object> map = new HashMap<>();
					map.put("id", indexPageList.get(i).getId());
					List<IndexSequence> indexSequenceList = sequenceRepository.findByIndexId(indexPageList.get(i).getId());
//					IndexSequence indexSequence = sequenceRepository.findByIndexId(indexPageList.get(i).getId());
					if (ObjectUtil.isNotEmpty(indexSequenceList)) {
						map.put("sequence", indexSequenceList.get(0).getSequence());
					} else {
						map.put("sequence", indexPageList.get(i).getSequence());
					}
					map.put("index", i);
					//分组类型为0
					map.put("flag", IndexFlag.IndexPageFlag);
					sortList.add(map);
				}
			}
		}
		if(sortList.size() >0){
			Collections.sort(sortList, (o1, o2) -> {
				Integer seq1 = (Integer) o1.get("sequence");
				Integer seq2 = (Integer) o2.get("sequence");
				return seq1.compareTo(seq2);
			});


			//sortList 排序过后的数据
			//只排序当前层，排序过后的数据，按顺序取出并返回
			for(Map<String,Object> map : sortList){
				IndexFlag flag = (IndexFlag) map.get("flag");
				Integer index = (Integer) map.get("index");
				if(flag.equals(IndexFlag.IndexPageFlag)){
					IndexPage indexPage = indexPageList.get(index);
					if(sortAll){
						//获取子类的数据进行排序
						List<IndexPage> child_page = indexPage.getChildrenPage();
						List<IndexTabMapper> child_mapper = null;
						if(!onlySortPage){
							child_mapper = indexPage.getIndexTabMappers();
						}
						if( (child_mapper != null && child_mapper.size()>0) || (child_page != null && child_page.size() >0) ){
							indexPage.setColumnList(sortColumn(new ArrayList<Object>(),child_mapper,child_page,sortAll,onlySortPage));
						}
					}
					result.add(indexPage);
				}else if(flag.equals(IndexFlag.IndexTabFlag)){
					IndexTabMapper mapper = mapperList.get(index);
					//栏目只有一层，直接添加就行
					result.add(mapper);
				}
			}
		}

		return result;
	}

	@Override
	public Object selectColumn(User user,String typeId) throws OperationException {
		List<IndexPage> oneIndexPage = null;
		List<IndexTabMapper> oneIndexTab = null;
		List<IndexTabMapper> top = null;
		List<IndexTabMapper> indexTab = new ArrayList<>();
		Map<String,Object> oneColumn = this.getOneLevelColumnForMap(typeId,user);
		if (oneColumn.containsKey("page")) {
			oneIndexPage = (List<IndexPage>) oneColumn.get("page");
		}
		if (oneColumn.containsKey("tab")) {
			oneIndexTab = (List<IndexTabMapper>) oneColumn.get("tab");
		}
		if (oneColumn.containsKey("top")) {
			top = (List<IndexTabMapper>) oneColumn.get("top");
		}
		List<Object> result = new ArrayList<>();
		if(top != null && top.size()>0){
			result.addAll(top);
		}
		User loginUser = UserUtils.getUser();
		List<IndexSequence> indexSequences = null;
		if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
			indexSequences = sequenceRepository.findByUserId(loginUser.getId());
		}else {
			indexSequences = sequenceRepository.findBySubGroupId(loginUser.getSubGroupId());
		}
		if (ObjectUtil.isEmpty(indexSequences)){
//没有 代表 第一次实行新排序逻辑 相当于历史数据处理
			if (oneIndexTab != null) indexTab.addAll(oneIndexTab);
			if (top != null) indexTab.addAll(top);
			newSortColumnAll(indexTab,oneIndexPage,true,false,"");
		}
		//获取到了第一层的栏目和分组信息，现在对信息进行排序
		List<Object> column =  sortColumn(result,oneIndexTab,oneIndexPage,true,false);
		List<Boolean> isGetOne = new ArrayList<>();
		isGetOne.add(false);
		return formatResultColumn(column,0,isGetOne,null);
	}

	/**
	 * 对日常监测左侧树形菜单进行格式统一，返回数据
	 * @param list  当前层级的数据
	 * @param level  当前的层级
	 * @param isGetOne  是否找到了要显示的第一个栏目
	 * @return
	 */
	private Object formatResultColumn(List<Object> list,Integer level,List<Boolean> isGetOne ,IndexPageDTO returnResult) {
		//前端需要字段
		/*
		id
		name
		flag
		flagSort
		show
		children
		 */
		List<Object> result = new ArrayList<>();
		if (list != null && list.size() > 0) {
			for (Object obj : list) {
				if (obj instanceof IndexTabMapper) {
					IndexTabMapper mapper = (IndexTabMapper) obj;
					IndexTabDTO indexTabDTO = new IndexTabDTO(mapper,level);
					if(!isGetOne.get(0) ){//之前还没找到一个要显示的 栏目数据
						isGetOne.set(0,true);
						if(returnResult == null){
							indexTabDTO.setActive(true);
							isGetOne.set(0,true);
						}else{
							returnResult.setActive(true);
						}
					}
					result.add(indexTabDTO);

				} else if (obj instanceof IndexPage) {
					IndexPage page = (IndexPage) obj;
					IndexPageDTO indexPageDTO = new IndexPageDTO(page,level);
					List<Object> childColumn = page.getColumnList();
					if (childColumn != null && childColumn.size() > 0) {
						Object child = this.formatResultColumn(childColumn,level+1,isGetOne,indexPageDTO);
						indexPageDTO.setChildren(child);
					}
					result.add(indexPageDTO);
				}
			}
		}
		return result;
	}

	public Map<String,Object> getOneLevelColumnForMap(String typeId,User loginUser){
		List<Order> orders=new ArrayList< Order>();
		orders.add( new Order(Sort.Direction.ASC, "sequence"));
		orders.add( new Order(Sort.Direction.ASC, "createdTime"));
		Specification<IndexPage> criteria_page = new Specification<IndexPage>(){

			@Override
			public Predicate toPredicate(Root<IndexPage> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> predicate = new ArrayList<>();
				if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
					predicate.add(cb.equal(root.get("userId"),loginUser.getId()));
					predicate.add(cb.isNull(root.get("subGroupId")));
				}else {
					predicate.add(cb.equal(root.get("subGroupId"),loginUser.getSubGroupId()));
				}
				//predicate.add(cb.equal(root.get("typeId"),typeId));
				List<Predicate> predicateParent = new ArrayList<>();
				predicateParent.add(cb.isNull(root.get("parentId")));
				predicateParent.add(cb.equal(root.get("parentId"),""));

				predicate.add(cb.or(predicateParent.toArray(new Predicate[predicateParent.size()])));
				Predicate[] pre = new Predicate[predicate.size()];
				return query.where(predicate.toArray(pre)).getRestriction();
			}
		};
		Specification<IndexTabMapper> criteria_tab_mapper = new Specification<IndexTabMapper>(){

			@Override
			public Predicate toPredicate(Root<IndexTabMapper> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> predicate = new ArrayList<>();
				if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
					predicate.add(cb.equal(root.get("userId"),loginUser.getId()));
					predicate.add(cb.isNull(root.get("subGroupId")));
				}else {
					predicate.add(cb.equal(root.get("subGroupId"),loginUser.getSubGroupId()));
				}
				//predicate.add(cb.equal(root.get("typeId"),typeId));
				predicate.add(cb.isNull(root.get("indexPage")));
				Predicate[] pre = new Predicate[predicate.size()];
				return query.where(predicate.toArray(pre)).getRestriction();
			}
		};

		Map<String,Object> result = new HashMap<>();
		//获取当前用户一层分组内的所有内容
		List<IndexPage> oneIndexPage = null;
		List<IndexTabMapper> oneIndexTab = null;

		oneIndexPage = indexPageRepository.findAll(criteria_page,new Sort(orders));
		oneIndexTab = tabMapperRepository.findAll(criteria_tab_mapper,new Sort(orders));
		if(oneIndexPage != null && oneIndexPage.size() >0){
			result.put("page",oneIndexPage);
		}
		if(oneIndexTab != null && oneIndexTab.size() >0){
			List<IndexTabMapper> topList = new ArrayList<>();
			List<IndexTabMapper> otherIndexTab = new ArrayList<>();
			for(IndexTabMapper mapper :oneIndexTab){
				if("top".equals(mapper.getTopFlag())){
					topList.add(mapper);
				}else{
					otherIndexTab.add(mapper);
				}
			}
			if(topList != null && topList.size() >0){
				result.put("top",topList);
			}
			if(otherIndexTab != null && otherIndexTab.size() >0){
				result.put("tab",otherIndexTab);
			}
		}
		return  result;
	}

	@Override
	public Object moveSequenceForColumn(String moveId,ColumnFlag flag, User user) throws OperationException{
		try {
			IndexPage movePage = null;
			IndexTabMapper moveMapper = null;
			String parentId = "";
			String typeId = "";
			if (flag.equals(ColumnFlag.IndexTabFlag)) {
				moveMapper = tabMapperRepository.findOne(moveId);
				if(ObjectUtil.isNotEmpty(moveMapper.getIndexPage())){
					parentId = moveMapper.getIndexPage().getId();
				}
				typeId = moveMapper.getTypeId();
			} else if (flag.equals(ColumnFlag.IndexPageFlag)) {
				movePage = indexPageRepository.findOne(moveId);
				parentId = movePage.getParentId();
				typeId = movePage.getTypeId();
			}

			List<IndexTabMapper> mapperList = null;
			List<IndexPage> pageList = null;
			if (StringUtil.isEmpty(parentId)) {
				//无父分组，则为一级，直接获取一级的内容
				Map<String, Object> oneColumn = getOneLevelColumnForMap(typeId, user);
				if (oneColumn.containsKey("page")) {
					pageList = (List<IndexPage>) oneColumn.get("page");
				}
				if (oneColumn.containsKey("tab")) {
					mapperList = (List<IndexTabMapper>) oneColumn.get("tab");
				}
			} else {
				IndexPage parentPage = indexPageRepository.findOne(parentId);
				pageList = parentPage.getChildrenPage();
				mapperList = parentPage.getIndexTabMappers();
			}
			List<Object> sortColumn = this.sortColumn(new ArrayList<>(),mapperList, pageList, false,false);
			int seq = 1;
			for (Object o : sortColumn) {
				if (o instanceof IndexTabMapper) {
					IndexTabMapper seqMapper = (IndexTabMapper) o;
					if ((flag.equals(ColumnFlag.IndexTabFlag) && !moveMapper.getId().equals(seqMapper.getId()))
							|| flag.equals(ColumnFlag.IndexPageFlag)) {
						seqMapper.setSequence(seq);
						if (seqMapper.isMe()) {
							IndexTab seqTab = seqMapper.getIndexTab();
							seqTab.setSequence(seq);
							indexTabRepository.saveAndFlush(seqTab);
						}
						seq++;
						tabMapperRepository.saveAndFlush(seqMapper);
					}
				} else if (o instanceof IndexPage) {
					IndexPage seqPage = (IndexPage) o;
					if ((flag.equals(ColumnFlag.IndexPageFlag) && !movePage.getId().equals(seqPage.getId()))
							|| flag.equals(ColumnFlag.IndexTabFlag)) {
						seqPage.setSequence(seq);
						seq++;
						indexPageRepository.saveAndFlush(seqPage);
					}
				}
			}
			return "success";
		} catch (Exception e) {
			throw new OperationException("重新对分组和栏目排序失败："+e.getMessage());
		}
	}


	@Override
	public Object moveIndexSequenceAll(String data, String parentId, User user) throws OperationException {
		try {

			IndexPage parent = null;
			if (StringUtil.isNotEmpty(parentId)) {
				parent = indexPageRepository.findOne(parentId);
			}
			String moveParentId = null;

			if (parentId == null) {
				parentId = "";
			}
			if (moveParentId == null) {
				moveParentId = "";
			}
			Boolean topFlag = false;
			JSONArray array = JSONArray.parseArray(data);
			List<Object> filter = array.stream().filter(json -> topFlag.equals(JSONObject.parseObject(String.valueOf(json)).getBoolean("topFlag"))).collect(Collectors.toList());
			Integer sequence = 0;
			StringBuilder deleteIds = new StringBuilder();
			for (Object json : filter) {
				sequence += 1;
				JSONObject parseObject = JSONObject.parseObject(String.valueOf(json));
				String id = parseObject.getString("id");
				//如果是分组为0 ，如果是栏目为1
				IndexFlag flag = IndexFlag.values()[parseObject.getInteger("flag")];
//				IndexSequence indexSequence = sequenceRepository.findByIndexId(id);
				List<IndexSequence> sequenceList =  sequenceRepository.findByIndexId(id);
				if (ObjectUtil.isEmpty(sequenceList)){
					throw new OperationException(parseObject.getString("name")+"已出现变更请刷新重试");
				}
				sequenceList.get(0).setSequence(sequence);
				if (parent != null) {
					sequenceList.get(0).setParentId(parentId);
				}
				sequenceRepository.save(sequenceList);
				if (flag.equals(IndexFlag.IndexPageFlag)) {

				}else if (flag.equals(IndexFlag.IndexTabFlag)) {
					if (parseObject.getBoolean("delete")) {
						deleteIds.append(id+";");
					}else {
						IndexTabMapper mapper = tabMapperRepository.findOne(id);
						mapper.setSequence(sequence);
						mapper.setHide(parseObject.getBoolean("hide"));
						mapper.setTabWidth(parseObject.getInteger("tabWidth"));
						IndexTab indexTab = mapper.getIndexTab();
						indexTab.setTabWidth(parseObject.getInteger("tabWidth"));
						indexTab.setHide(parseObject.getBoolean("hide"));
						if (mapper.isMe()) {
							indexTabRepository.save(indexTab);
						}
						tabMapperRepository.save(mapper);
					}


				}else if (flag.equals(IndexFlag.CustomFlag)){
//					if (parseObject.getBoolean("delete") || parseObject.getBoolean("hide")) sequenceRepository.delete(sequenceRepository.findByIndexId(id));
					//删除是取消分类
					CustomChart customChart = customChartRepository.findOne(id);
					customChart.setTabWidth(parseObject.getInteger("tabWidth"));
					if (parseObject.getBoolean("delete")){
						customChart.setIsTop(false);
						sequenceRepository.delete(sequenceRepository.findByIndexId(id));
					}
					customChart.setHide(parseObject.getBoolean("hide"));
					customChartRepository.save(customChart);

				}else if (flag.equals(IndexFlag.StatisticalFlag)){
//					if (parseObject.getBoolean("delete") || parseObject.getBoolean("hide")) sequenceRepository.delete(sequenceRepository.findByIndexId(id));

					StatisticalChart statisticalChart = columnChartService.findOneStatisticalChart(id);
					statisticalChart.setTabWidth(parseObject.getInteger("tabWidth"));
					if (parseObject.getBoolean("delete")){
						statisticalChart.setIsTop(false);
						sequenceRepository.delete(sequenceRepository.findByIndexId(id));
					}
					statisticalChart.setHide(parseObject.getBoolean("hide"));
					statisticalChartRepository.save(statisticalChart);

				}
//				if (flag.equals(IndexFlag.IndexPageFlag)) {
//					IndexPage indexPage = indexPageRepository.findOne(id);
//					indexPage.setParentId(null);
//					if (parent != null) {
//						indexPage.setParentId(parentId);
//					}
//					indexPage.setSequence(sequence);
//					indexPageRepository.save(indexPage);
//				} else if (flag.equals(IndexFlag.IndexTabFlag)) {
//					IndexTabMapper mapper = tabMapperRepository.findOne(id);
//					IndexTab indexTab = mapper.getIndexTab();
//					if (parent != null) {
//						mapper.setIndexPage(parent);
//						indexTab.setParentId(parent.getId());
//					} else {
//						mapper.setIndexPage(null);
//					}
//					mapper.setSequence(sequence);
//					if (mapper.isMe()) {
//						indexTab.setSequence(sequence);
//						indexTabRepository.save(indexTab);
//					}
//					tabMapperRepository.save(mapper);
//				}else if (flag.equals(IndexFlag.CustomFlag)){
//					CustomChart customChart = customChartRepository.findOne(id);
//					customChart.setSequence(sequence);
//					customChartRepository.save(customChart);
//				}else if (flag.equals(IndexFlag.StatisticalFlag)){
//					StatisticalChart statisticalChart = statisticalChartRepository.findOne(id);
//					statisticalChart.setSequence(sequence);
//					statisticalChartRepository.save(statisticalChart);
//				}
			}
			indexTabMapperService.deleteMapper(deleteIds.toString());
			indexPageRepository.flush();
			tabMapperRepository.flush();
			indexTabRepository.flush();
			statisticalChartRepository.flush();
			customChartRepository.flush();
			return "success";
		} catch (Exception e) {
			throw new OperationException("移动失败：" + e.getMessage());
		}
	}

	@Override
	public Object moveIndexSequence(String data, String moveData, String parentId, User user) throws OperationException {
		try {

			IndexPage parent = null;
			if (StringUtil.isNotEmpty(parentId)) {
				parent = indexPageRepository.findOne(parentId);
			}
			JSONObject move = JSONObject.parseObject(moveData);
			String moveId = move.getString("id");
			ColumnFlag moveFlag = ColumnFlag.values()[move.getInteger("flag")];
			boolean topFlag = move.getBoolean("topFlag");
			String moveParentId = null;

			//修改同级下的其他分组的顺序
			if (moveFlag.equals(ColumnFlag.IndexPageFlag)) {
				IndexPage indexPage = indexPageRepository.findOne(moveId);
				moveParentId = indexPage.getParentId();
			} else if (moveFlag.equals(ColumnFlag.IndexTabFlag)) {
				IndexTabMapper mapper = tabMapperRepository.findOne(moveId);
				if(ObjectUtil.isNotEmpty(mapper.getIndexPage())){
					moveParentId = mapper.getIndexPage().getId();
				}

			}
			if (parentId == null) {
				parentId = "";
			}
			if (moveParentId == null) {
				moveParentId = "";
			}
//			//被拖拽的元素更换了层级，需要对元素本来的层级的数据重新排序
//			if (!topFlag &&!parentId.equals(moveParentId)) {
//				//对原来的同层级的数据排序
//				this.moveSequenceForColumn(moveId,moveFlag, user);
//			}
//List<IndexSequence> indexSequenceList = sequenceRepository.findByParentIdOrderBySequence(parentId);
			User loginUser = UserUtils.getUser();
			List<IndexSequence> indexSequenceList = null;
			if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
				indexSequenceList = sequenceRepository.findByUserIdAndParentIdOrderBySequence(loginUser.getId(),parentId);
			}else {
				indexSequenceList = sequenceRepository.findBySubGroupIdAndParentIdOrderBySequence(loginUser.getSubGroupId(),parentId);
			}


			JSONArray array = JSONArray.parseArray(data);
			List<Object> filter = array.stream().filter(json -> topFlag == JSONObject.parseObject(String.valueOf(json)).getBoolean("topFlag")).collect(Collectors.toList());
			boolean isTheOne = false;
			String nextId = null;
			for (Object json : filter) {
				JSONObject parseObject = JSONObject.parseObject(String.valueOf(json));
				String id = parseObject.getString("id");
				//如果是分组为0 ，如果是栏目为1
				ColumnFlag flag = ColumnFlag.values()[parseObject.getInteger("flag")];
				if (isTheOne){
					//上一个是移动的点
					nextId = id;
					break;
				}
				if (moveId.equals(id)) isTheOne = true;

			}
			if (nextId != null) {
				boolean isChange = false;
				Integer point = null;
				point = sequenceRepository.findByIndexId(nextId).get(0).getSequence();
				Integer nextPoint = 1;//下一个需要添加的值
//				IndexSequence indexSeq = sequenceRepository.findByIndexId(moveId);
				List<IndexSequence> indexList = sequenceRepository.findByIndexId(moveId);
				IndexSequence indexSeq = indexList.get(0);
				if (moveId.equals(indexSeq.getIndexId())){
					//移动的点
					indexSeq.setSequence(point);
					if (IndexFlag.IndexTabFlag.equals(indexSeq.getIndexFlag())) {
						List<IndexSequence> sequences = sequenceRepository.findByIndexTabId(moveId);
						for (int i = 0; i < sequences.size(); i++) {
							sequences.get(i).setSequence(point+1+i);
							nextPoint++;
						}
						sequenceRepository.save(sequences);

//								List<indexSeq> sequences = sequenceRepository.findByIndexTabIdOrderBySequence(indexSeq.getIndexTabId());
						IndexTabMapper mapper = tabMapperRepository.findOne(indexSeq.getIndexId());
						IndexTab indexTab = mapper.getIndexTab();
						if (parent != null) {
							mapper.setIndexPage(parent);
							indexTab.setParentId(parent.getId());
						} else {
							mapper.setIndexPage(null);
						}
						indexSeq.setParentId(parentId);
						mapper.setSequence(indexSeq.getSequence());
						if (mapper.isMe()) {
							indexTab.setSequence(indexSeq.getSequence());
							indexTabRepository.save(indexTab);
						}
						tabMapperRepository.save(mapper);


					}else if (IndexFlag.IndexPageFlag.equals(indexSeq.getIndexFlag())){
						IndexPage indexPage = indexPageRepository.findOne(indexSeq.getIndexId());
						indexPage.setParentId(null);
						if (parent != null) {
							indexPage.setParentId(parentId);

						}
						indexSeq.setParentId(parentId);
						indexPage.setSequence(indexSeq.getSequence());
						indexPageRepository.save(indexPage);
					}
					sequenceRepository.save(indexSeq);
				}
				for (IndexSequence indexSequence : indexSequenceList) {

					if (nextId.equals(indexSequence.getIndexId())) {
						isChange = true;
					}
					if (isChange && !moveId.equals(indexSequence.getIndexId())) {
						if (IndexFlag.IndexTabFlag.equals(indexSequence.getIndexFlag())) {
//								List<IndexSequence> sequences = sequenceRepository.findByIndexTabIdOrderBySequence(indexSequence.getIndexTabId());
							IndexTabMapper mapper = tabMapperRepository.findOne(indexSequence.getIndexId());
							if (ObjectUtil.isNotEmpty(mapper)) {
								IndexTab indexTab = mapper.getIndexTab();
								if (parent != null) {
									mapper.setIndexPage(parent);
									indexTab.setParentId(parent.getId());

								} else {
									mapper.setIndexPage(null);
								}
								indexSequence.setParentId(parentId);
								mapper.setSequence(indexSequence.getSequence() + nextPoint);
								if (mapper.isMe()) {
									indexTab.setSequence(indexSequence.getSequence() + nextPoint);
									indexTabRepository.save(indexTab);
								}
								tabMapperRepository.save(mapper);
							}

						}else if (IndexFlag.IndexPageFlag.equals(indexSequence.getIndexFlag())){
							IndexPage indexPage = indexPageRepository.findOne(indexSequence.getIndexId());
							indexPage.setParentId(null);
							if (parent != null) {
								indexPage.setParentId(parentId);

							}
							indexSequence.setParentId(parentId);
							indexPage.setSequence(indexSequence.getSequence() + nextPoint);
							indexPageRepository.save(indexPage);
						}

						indexSequence.setSequence(indexSequence.getSequence() + nextPoint);
					}
				}
				sequenceRepository.save(indexSequenceList);

			}else {
				//移动到最后
				Integer moveSeq = 0;
				if (ObjectUtil.isNotEmpty(indexSequenceList)){
					IndexSequence indexSequence1 = indexSequenceList.get(indexSequenceList.size() - 1);
					moveSeq = indexSequence1.getSequence() + 1;
				}
				List<IndexSequence> indexList = sequenceRepository.findByIndexId(moveId);
//				IndexSequence indexSequence = sequenceRepository.findByIndexId(moveId);
				indexList.get(0).setSequence(moveSeq);
				sequenceRepository.save(indexList);
				if (IndexFlag.IndexTabFlag.equals(indexList.get(0).getIndexFlag())) {
					List<IndexSequence> sequences = sequenceRepository.findByIndexTabId(moveId);
					for (int i = 0; i < sequences.size(); i++) {
						sequences.get(i).setSequence(moveSeq+i+1);
					}
					sequenceRepository.save(sequences);
					IndexTabMapper mapper = tabMapperRepository.findOne(indexList.get(0).getIndexId());
					IndexTab indexTab = mapper.getIndexTab();
					indexList.get(0).setParentId(parentId);
					if (parent != null) {
						mapper.setIndexPage(parent);
						indexTab.setParentId(parent.getId());

					} else {
						mapper.setIndexPage(null);
					}
					mapper.setSequence(indexList.get(0).getSequence());
					if (mapper.isMe()) {
						indexTab.setSequence(indexList.get(0).getSequence());
						indexTabRepository.save(indexTab);
					}
					tabMapperRepository.save(mapper);

				}else if (IndexFlag.IndexPageFlag.equals(indexList.get(0).getIndexFlag())){
					IndexPage indexPage = indexPageRepository.findOne(indexList.get(0).getIndexId());
					indexPage.setParentId(null);
					if (parent != null) {
						indexPage.setParentId(parentId);

					}
					indexList.get(0).setParentId(parentId);
					indexPage.setSequence(indexList.get(0).getSequence());
					indexPageRepository.save(indexPage);
				}

			}
//			Integer sequence = 0;
//			for (Object json : filter) {
//				sequence += 1;
//				JSONObject parseObject = JSONObject.parseObject(String.valueOf(json));
//				String id = parseObject.getString("id");
//				//如果是分组为0 ，如果是栏目为1
//				ColumnFlag flag = ColumnFlag.values()[parseObject.getInteger("flag")];
//				if (flag.equals(ColumnFlag.IndexPageFlag)) {
//					IndexSequence indexSequence = sequenceRepository.findByIndexId(id);
//					indexSequence.setSequence(sequence);
//					indexSequence.setParentId(null);
//					if (parent != null) {
//						indexSequence.setParentId(parentId);
//					}
//					sequenceRepository.save(indexSequence);
////					IndexPage indexPage = indexPageRepository.findOne(id);
////					indexPage.setParentId(null);
////					if (parent != null) {
////						indexPage.setParentId(parentId);
////					}
////					indexPage.setSequence(sequence);
////					indexPageRepository.save(indexPage);
//				} else if (flag.equals(ColumnFlag.IndexTabFlag)) {
//					IndexTabMapper mapper = tabMapperRepository.findOne(id);
//					IndexTab indexTab = mapper.getIndexTab();
//					if (parent != null) {
//						mapper.setIndexPage(parent);
//						indexTab.setParentId(parent.getId());
//					} else {
//						mapper.setIndexPage(null);
//					}
//					mapper.setSequence(sequence);
//					if (mapper.isMe()) {
//						indexTab.setSequence(sequence);
//						indexTabRepository.save(indexTab);
//					}
//					tabMapperRepository.save(mapper);
//				}
//			}
			indexPageRepository.flush();
			tabMapperRepository.flush();
			indexTabRepository.flush();
			sequenceRepository.flush();
			return "success";
		} catch (Exception e) {
			throw new OperationException("移动失败：" + e.getMessage());
		}
	}


	@Override
	public Object updateTwo(String userId, String name, String twoId) throws OperationException {
		try {
			// 修改一级二级表中所有关于一级栏目的名字字段 不管他的二级栏目是否为空
			Criteria<IndexPage> criteria = new Criteria<>();
			criteria.add(Restrictions.eq("userId", userId));
			criteria.add(Restrictions.eq("sonId", twoId));
			List<IndexPage> findAll = indexPageRepository.findAll(criteria);
			for (IndexPage oneAndTwo : findAll) {
				indexPageRepository.save(oneAndTwo);
			}
			return "success";
		} catch (Exception e) {
			throw new OperationException("修改一级栏目出错");
		}
	}

	@Override
	public String deleteTwo(String twoId) throws OperationException {
		try {
			// 删除二级栏目下级联的三级栏目
			List<IndexTab> findByParentId = indexTabRepository.findByParentId(twoId);
			for (IndexTab three : findByParentId) {
				indexTabRepository.delete(three.getId());
			}
		} catch (Exception e) {
			throw new OperationException("删除二级栏目时出错");
		}
		return "success";
	}

	@Override
	@Transactional
	public Object deleteOne(String indexPageId) throws OperationException {

		try {
			// 删除栏目组及下级子栏目
			//删除时需要重新排序
			IndexPage indexPage = indexPageRepository.findOne(indexPageId);
			if(ObjectUtil.isEmpty(indexPage)){
				throw new OperationException("当前分组不存在");
			}
			//重新排序
			User user = UserUtils.getUser();
			this.moveSequenceForColumn(indexPageId, ColumnFlag.IndexPageFlag, user);
			List<IndexPage> list = new ArrayList<>();
			list.add(indexPage);
			//删除栏目 以及 栏目下对应的其他元素
			this.deleteIndexPage(list);
			//因为当删除元素过多，删除失败（原因可能是数据库连接的缓存占满，导致子元素被删除两次），所以上面只删除分类下的内容，再通过父节点删除子元素。
			//注解 CascadeType.REMOVE 会使删除父节点时，一起删除相关子元素
			indexPageRepository.delete(list);
			sequenceRepository.delete(sequenceRepository.findByIndexId(indexPageId));
			return "success";
		} catch (Exception e) {
			throw new OperationException("删除分组时出错", e);
		}
	}

	private Object deleteIndexPage(List<IndexPage> indexPages)throws OperationException{
		// 删除栏目组及下级子栏目
		try {
			if(indexPages != null && indexPages.size() >0){
				for(IndexPage indexPage : indexPages){
					List<IndexPage> chidPage = indexPage.getChildrenPage();
					List<IndexTabMapper> chidMapper = indexPage.getIndexTabMappers();
					//删除当前分组对应的栏目
					if (CollectionsUtil.isNotEmpty(chidMapper)) {
						for (IndexTabMapper mapper : chidMapper) {
							//删除当前栏目对应的自定义图表
							Integer deleteColumnChart = columnChartService.deleteCustomChartForTabMapper(mapper.getId());
							log.info("删除当前栏目下统计和自定义图表共："+deleteColumnChart +"条");
							if (mapper.isMe()) {
								// 删除栏目映射关系，isMe为true的栏目关系须级联删除栏目实体
								List<IndexTabMapper> findByIndexTab = indexTabMapperService.findByIndexTab(mapper.getIndexTab());
								//删除相关栏目映射的相关图表
								Integer deleteAbMapperColumnChart = 0;
								for(IndexTabMapper abMapper : findByIndexTab){
									deleteAbMapperColumnChart+= columnChartService.deleteCustomChartForTabMapper(abMapper.getId());
								}
								log.info("删除当前栏目相关统计和自定义图表共："+deleteAbMapperColumnChart +"条");
								//删除所有与indexTab关联的  否则剩余关联则删除indexTab时失败
								tabMapperRepository.delete(findByIndexTab);
								indexTabRepository.delete(mapper.getIndexTab());

							}else{
								//如果是引用，则只删除当前引用即可
								tabMapperRepository.delete(mapper);
							}

						}
					}
					sequenceRepository.delete(sequenceRepository.findByParentId(indexPage.getId()));
					//删除当前分组对应的子分组
					if (CollectionsUtil.isNotEmpty(chidPage)) {
						deleteIndexPage(chidPage);
					}
				}
			}
			return "success";
		} catch (Exception e) {
			throw new OperationException("删除分组失败",e);

		}
	}

	/**
	 * TODO 获取无相似文章列表数据,传统媒体类型
	 *
	 * @param queryBuilder
	 * @return
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	private List<Map<String, Object>> getDataNoSim(QueryBuilder queryBuilder) throws TRSSearchException, TRSException {
		List<Map<String, Object>> list = new ArrayList<>();
		Map<String, Object> map = null;
		log.info(queryBuilder.asTRSL());
		List<FtsDocument> ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, FtsDocument.class, true,false,false,"column");
		String uid = UUID.randomUUID().toString();
		RedisUtil.setString(uid, queryBuilder.asTRSL());
		for (FtsDocument document : ftsQuery) {
			map = new HashMap<>();
			map.put("sid", document.getSid());
			map.put("title", document.getTitle());
			map.put("siteName", document.getSiteName());
			map.put("trslk", uid);
			map.put("groupName", document.getGroupName());
			map.put("nreserved1", document.getNreserved1());
			map.put("urlTime", document.getUrlTime());
			map.put("appraise", document.getAppraise());
			map.put("screenName", document.getSiteName());
			map.put("urlName", document.getUrlName());
			map.put("nreserved1", document.getNreserved1());
			map.put("content", document.getContent());
			map.put("abstracts",document.getAbstracts());
			/*map.put("content", StringUtil.removeFourChar(StringUtil.replaceImg(document.getContent())));
			map.put("abstracts",StringUtil.removeFourChar(StringUtil.replaceImg(document.getAbstracts())));*/
			// 获得时间差,三天内显示时间差,剩下消失urltime
			Map<String, String> timeDifference = DateUtil.timeDifference(document);
			boolean isNew = false;
			if (ObjectUtil.isNotEmpty(timeDifference.get("timeAgo"))) {
				isNew = true;
				map.put("timeAgo", timeDifference.get("timeAgo"));
			} else {
				map.put("timeAgo", timeDifference.get("urlTime"));
			}
			map.put("isNew", isNew);
			map.put("md5Tag", document.getMd5Tag());
			list.add(map);
		}
		return list;
	}

	/**
	 * 根据关键词以及关键词位置生成trsl表达式
	 *
	 * @param indexTab
	 *            关键词,多值,以';'隔开
	 * @param queryBuilder
	 *            关键词位置,多值,以';'隔开
	 * @return
	 */
	private void appendTrsl(IndexTab indexTab, QueryBuilder queryBuilder, QueryBuilder queryBuilderStatus,
							QueryBuilder queryBuilderWeChat) {
		String keyWords = indexTab.getKeyWord();
		String keyWordindex = indexTab.getKeyWordIndex();
		String excludeWords = indexTab.getExcludeWords();
		StringBuilder childTrsl = new StringBuilder();
		// 判断关键词位置是否为空
		if (StringUtil.isNotEmpty(keyWordindex) && StringUtil.isNotEmpty(keyWords)) {
			// 切割关键词位置
			String[] sources = keyWordindex.split(";");
			for (String source : sources) {
				// 关键词是否为空
				// if (StringUtil.isNotEmpty(keyWords)) {
				// 判断是否为";"结尾
				// if (keyWords.endsWith(";")) {
				// keyWords = keyWords.substring(0, keyWords.length() - 1);
				// keyWords = keyWords.replaceAll("[;|；]+", "\" AND \"");
				// childTrsl.append("(\"" + keyWords + "\")");
				// } else {
				// keyWords = keyWords.replaceAll("[;|；]+", "\" AND \"");
				// childTrsl.append("(\"" + keyWords + "\")");
				// }
				String replaceAnyKey = "";
				if (keyWords.endsWith(";")) {
					replaceAnyKey = keyWords.substring(0, keyWords.length() - 1);
					childTrsl.append("((\"")
							.append(replaceAnyKey.replaceAll(",", "\" ) AND ( \"").replaceAll("[;|；]+", "\" OR \""))
							.append("\"))");
				} else {
					childTrsl.append("((\"")
							.append(keyWords.replaceAll(",", "\" ) AND ( \"").replaceAll("[;|；]+", "\" OR \""))
							.append("\"))");
				}
				// }
				if (StringUtil.isNotEmpty(excludeWords)) {
					childTrsl.append(" NOT (\"").append(excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
				}
				switch (source.trim()) {
					// 仅标题
					case "0":
						queryBuilder.filterChildField(FtsFieldConst.FIELD_URLTITLE, childTrsl.toString(), Operator.Equal);
						queryBuilderWeChat.filterChildField(FtsFieldConst.FIELD_URLTITLE, childTrsl.toString(),
								Operator.Equal);
						queryBuilderStatus.filterChildField(FtsFieldConst.FIELD_STATUS_CONTENT, childTrsl.toString(),
								Operator.Equal);
						queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);// 传统
						queryBuilderWeChat.setDatabase(Const.WECHAT);// 微信
						queryBuilderStatus.setDatabase(Const.WEIBO);// 微博
						queryBuilder.orderBy(ESFieldConst.IR_URLTIME, true);
						queryBuilderWeChat.orderBy(ESFieldConst.IR_URLTIME, true);
						queryBuilderStatus.orderBy(FtsFieldConst.FIELD_URLTIME, true);
						continue;
					case "1":// 标题 + 正文
						queryBuilder.filterChildField(FtsFieldConst.FIELD_URLTITLE, childTrsl.toString(), Operator.Equal);
						queryBuilder.filterChildField(FtsFieldConst.FIELD_CONTENT, childTrsl.toString(), Operator.Equal);
						queryBuilderWeChat.filterChildField(FtsFieldConst.FIELD_URLTITLE, childTrsl.toString(),
								Operator.Equal);
						queryBuilderWeChat.filterChildField(FtsFieldConst.FIELD_CONTENT, childTrsl.toString(),
								Operator.Equal);
						queryBuilderStatus.filterChildField(FtsFieldConst.FIELD_STATUS_CONTENT, childTrsl.toString(),
								Operator.Equal);
						queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);// 传统
						queryBuilderWeChat.setDatabase(Const.WECHAT);// 微信
						queryBuilderStatus.setDatabase(Const.WEIBO);// 微博
						queryBuilder.orderBy(ESFieldConst.IR_URLTIME, true);
						queryBuilderWeChat.orderBy(ESFieldConst.IR_URLTIME, true);
						queryBuilderStatus.orderBy(FtsFieldConst.FIELD_URLTIME, true);
						continue;
					default:// 仅标题
						queryBuilder.filterChildField(FtsFieldConst.FIELD_URLTITLE, childTrsl.toString(), Operator.Equal);
						queryBuilderWeChat.filterChildField(FtsFieldConst.FIELD_URLTITLE, childTrsl.toString(),
								Operator.Equal);
						queryBuilderStatus.filterChildField(FtsFieldConst.FIELD_STATUS_CONTENT, childTrsl.toString(),
								Operator.Equal);
						queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);// 传统
						queryBuilderWeChat.setDatabase(Const.WECHAT);// 微信
						queryBuilderStatus.setDatabase(Const.WEIBO);// 微博
						queryBuilder.orderBy(ESFieldConst.IR_URLTIME, true);
						queryBuilderWeChat.orderBy(ESFieldConst.IR_URLTIME, true);
						queryBuilderStatus.orderBy(FtsFieldConst.FIELD_URLTIME, true);
						continue;
				}
			}
		} else {// 时间倒序
			queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);// 传统
			queryBuilderWeChat.setDatabase(Const.WECHAT);// 微信
			queryBuilderStatus.setDatabase(Const.WEIBO);// 微博
			queryBuilder.orderBy(ESFieldConst.IR_URLTIME, true);
			queryBuilderWeChat.orderBy(ESFieldConst.IR_URLTIME, true);
			queryBuilderStatus.orderBy(FtsFieldConst.FIELD_URLTIME, true);
		}
	}

	@Override
	public void save(Columns column) {
		this.columnRepository.save(column);

	}

	@Override
	public List<Columns> findByUserId(String uid, Sort sort) {
		return this.columnRepository.findByUserId(uid, sort);
	}

	@Override
	public List<Columns> findByOrganizationId(Sort sort) {

		String userId = UserUtils.getUser().getId();
		return this.columnRepository.findByUserId(userId, sort);
	}

	@Override
	public Object selectColumnByOrganizationId(String organizationId) throws OperationException {
		// 从一级开始找
		// 把sonId为空的找出来 这是一级的
		Criteria<IndexPage> criteria = new Criteria<>();
		criteria.add(Restrictions.eq("organizationId", organizationId));
		// criteria.add(Restrictions.eq("sonId", ""));
		// criteria.orderByDESC("lastModifiedTime");
		criteria.orderByASC("sequence");
		List<IndexPage> list1 = indexPageRepository.findAll(criteria);
		List<IndexPage> oneList = new ArrayList<>();
		oneList = list1;
		// 通过一级找三级
		List<Map<String, Object>> addTwo = new ArrayList<>();
		for (IndexPage one : oneList) {
			String id = one.getId();
			Criteria<IndexTab> criteriaThree = new Criteria<>();
			criteriaThree.add(Restrictions.eq("organizationId", organizationId));
			if (StringUtil.isNotEmpty(id)) {
				criteriaThree.add(Restrictions.eq("parentId", id));
			}
			criteriaThree.orderByASC("sequence");
			List<IndexTab> threeList = indexTabRepository.findAll(criteriaThree);
			List<IndexTab> addThree = new ArrayList<>();
			if (ObjectUtil.isNotEmpty(threeList)) {
				for (IndexTab three : threeList) {
					addThree.add(three);
				}
			}
			Map<String, Object> putValue = MapUtil.putValue(new String[] { "threeList", "oneName", "oneId", "hide" },
					addThree, one.getName(), one.getId(), one.isHide());
			addTwo.add(putValue);
		}
		return addTwo;
	}

	private void getDataBarByList(QueryBuilder queryBuilder, IndexTab indexTab, String key) throws OperationException {
		List<CategoryBean> mediaType = chartAnalyzeService.getMediaType(indexTab.getXyTrsl());
		for (CategoryBean categoryBean : mediaType) {
			if (categoryBean.getKey().equals(key)) {
				// 取表达式
				String value = categoryBean.getValue();
				queryBuilder.filterByTRSL(value);
				// countBuiler.filterByTRSL(value);
			}
		}
	}

	@Override
	public Object list(IndexTab indexTab, QueryBuilder queryBuilder, QueryBuilder countBuiler, int pagesize, int pageno,
					   String fenlei, String sort, String key, String area) throws TRSException {
		//从实体里取是否排重
		//boolean sim = indexTab.isSimilar();
		boolean irSimflag = indexTab.isIrSimflag();
		boolean sim = indexTab.isIrSimflagAll();
		// 取地域名
		if (!"ALL".equals(area)) { // 地域

			String[] areaSplit = area.split(";");
			String contentArea = "";
			for (int i = 0; i < areaSplit.length; i++) {
				areaSplit[i] = "中国\\\\" + areaSplit[i] + "*";
				if (i != areaSplit.length - 1) {
					areaSplit[i] += " OR ";
				}
				contentArea += areaSplit[i];
			}
			queryBuilder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + contentArea);
			countBuiler.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + contentArea);
		}
		// 词云进去
		// 取userId
		User loginUser = UserUtils.getUser();
		// 为了调用人家写好的方法
		// QueryBuilder queryBuilderWeChat = queryBuilder;
		// QueryBuilder queryBuilderStatus = queryBuilder;
		QueryBuilder queryBuilderWeChat = new QueryBuilder();
		QueryBuilder queryBuilderStatus = new QueryBuilder();
		queryBuilderWeChat.filterByTRSL(queryBuilder.asTRSL());
		queryBuilderWeChat.page(pageno, pagesize);
		queryBuilderStatus.filterByTRSL(queryBuilder.asTRSL());
		queryBuilderStatus.page(pageno, pagesize);
		// queryBuilderStatus = queryBuilder;
		String trsl = indexTab.getTrsl();
		String statusTrsl = indexTab.getStatusTrsl();
		String weChatTrsl = indexTab.getWeChatTrsl();
		// 根据trsl字段,判别普通模式与专家模式
		if (StringUtil.isEmpty(trsl)) {
			// 将关键词与关键词位置,转换为trsl表达式,并保存到builder中
			appendTrsl(indexTab, queryBuilder, queryBuilderStatus, queryBuilderWeChat);
			if ("微博".equals(fenlei)) {
				queryBuilderStatus.filterByTRSL(statusTrsl);
				queryBuilder = queryBuilderStatus;
			} else if ("微信".equals(fenlei)) {
				queryBuilderWeChat.filterByTRSL(weChatTrsl);
				queryBuilder = queryBuilderWeChat;
			}
		} else {
			if ("微博".equals(fenlei)) {// 防止trsl不对
				String replace = statusTrsl.replace(FtsFieldConst.FIELD_TITLE, FtsFieldConst.FIELD_STATUS_CONTENT);
				replace = replace.replaceAll(FtsFieldConst.FIELD_CONTENT, FtsFieldConst.FIELD_STATUS_CONTENT);
				replace = replace.replaceAll(FtsFieldConst.FIELD_URLTIME, FtsFieldConst.FIELD_URLTIME);
				queryBuilder.filterByTRSL(replace);
				countBuiler.filterByTRSL(replace);
			} else if ("微信".equals(fenlei)) {
				queryBuilderWeChat.filterByTRSL(weChatTrsl);
				queryBuilder = queryBuilderWeChat;
				countBuiler = queryBuilderWeChat;
			} else {
				queryBuilder.filterByTRSL(indexTab.getTrsl());
				countBuiler.filterByTRSL(indexTab.getTrsl());
			}
		}
		// 如果有key值 就是分类对比图
		if (StringUtil.isNotEmpty(key)) {
			getDataBarByList(queryBuilder, indexTab, key);
		}
		// 分用哪个方法
		if ("微博".equals(fenlei)) {
			// 然后还得分是否是按照热度排序
			switch (sort) { // 排序
				case "desc":
					queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					countBuiler.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					break;
				case "asc":
					queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					countBuiler.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					break;
				case "hot":
					return infoListService.getHotListStatus(queryBuilder, countBuiler, loginUser,"column");
				case "commtCount": // 微博按照 评论数 排序
					queryBuilder.orderBy(FtsFieldConst.FIELD_COMMTCOUNT, true);
					countBuiler.orderBy(FtsFieldConst.FIELD_COMMTCOUNT, true);
					break;
				case "rttCount": // 微博按照评论数 转发数 排序
					queryBuilder.orderBy(FtsFieldConst.FIELD_RTTCOUNT, true);
					countBuiler.orderBy(FtsFieldConst.FIELD_RTTCOUNT, true);
					break;
				default:
					queryBuilder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					countBuiler.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					break;
			}
			return infoListService.getStatusList(queryBuilder, loginUser,sim,irSimflag,false,false,"column");
		} else if ("微信".equals(fenlei)) {
			switch (sort) { // 排序
				case "desc":
					queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					break;
				case "asc":
					queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					break;
				case "hot":
					return infoListService.getHotListWeChat(queryBuilder, countBuiler, loginUser,"column");
				default:
					queryBuilder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					break;
			}
			countBuiler.setDatabase(Const.WECHAT);
			String groupName = indexTab.getGroupName();
			return infoListService.getWeChatList(queryBuilder, loginUser,sim,irSimflag,false,false,"column");
		} else {// 传统
			queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);
			switch (sort) { // 排序
				case "desc":
					queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					countBuiler.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					break;
				case "asc":
					queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					countBuiler.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					break;
				case "hot":
					return infoListService.getHotList(queryBuilder, countBuiler, loginUser,"column");
				default:
					queryBuilder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					countBuiler.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					break;
			}
			countBuiler.setDatabase(Const.HYBASE_NI_INDEX);
			return infoListService.getDocList(queryBuilder, loginUser, true,false,false,false,"column");
		}
	}

	/**
	 * 首页地域
	 */
	@Override
	public Object arealist(QueryBuilder indexBuilder, QueryBuilder countBuiler, String sort, String area, String source,
						   String timeRange, String keywords) throws TRSException {
		// 取地域名
		if (StringUtil.isNotEmpty(area)) {
			String province = districtInfoService.province(area);
			// 地域只传一个 所以不用分割了
			area = "中国\\\\" + province + "*";
			indexBuilder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + area);
			countBuiler.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + area);
		}
		// loginUser
		User loginUser = UserUtils.getUser();
		// 分用哪个方法
		if ("微博".equals(source)) {
			if (StringUtil.isNotEmpty(keywords)) {
				String trsl = new StringBuffer().append(FtsFieldConst.FIELD_STATUS_CONTENT).append(":").append(keywords)
						.toString();
				indexBuilder.filterByTRSL(trsl);
				countBuiler.filterByTRSL(trsl);
			}
			indexBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(timeRange),
					Operator.Between);
			countBuiler.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(timeRange),
					Operator.Between);
			// 然后还得分是否是按照热度排序
			switch (sort) { // 排序
				case "desc":
					indexBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					countBuiler.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					break;
				case "asc":
					indexBuilder.orderBy(FtsFieldConst.FIELD_CREATED_AT, false);
					countBuiler.orderBy(FtsFieldConst.FIELD_CREATED_AT, false);
					break;
				case "hot":
					return infoListService.getHotListStatus(indexBuilder, countBuiler, loginUser,"column");
				default:
					indexBuilder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					countBuiler.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					break;
			}
			return infoListService.getStatusList(indexBuilder, loginUser,true,false,false,false,"column");
		} else if ("微信".equals(source)) {
			indexBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(timeRange),
					Operator.Between);
			countBuiler.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(timeRange), Operator.Between);
			// 现在在结果中搜索
			if (StringUtil.isNotEmpty(keywords)) {
				String trsl = new StringBuffer().append(FtsFieldConst.FIELD_TITLE).append(":").append(keywords)
						.append(" OR ").append(FtsFieldConst.FIELD_CONTENT).append(":").append(keywords).toString();
				indexBuilder.filterByTRSL(trsl);
				countBuiler.filterByTRSL(trsl);
			}
			switch (sort) { // 排序
				case "desc":
					indexBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					break;
				case "asc":
					indexBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					break;
				case "hot":
					return infoListService.getHotListWeChat(indexBuilder, countBuiler, loginUser,"column");
				default:
					indexBuilder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					break;
			}
			countBuiler.setDatabase(Const.WECHAT);
			log.info(indexBuilder.asTRSL());
			return infoListService.getWeChatList(indexBuilder, loginUser,true,false,false,false,"column");
		} else {// 传统
			indexBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(timeRange),
					Operator.Between);
			countBuiler.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(timeRange), Operator.Between);
			if (StringUtil.isNotEmpty(keywords)) {
				String trsl = new StringBuffer().append(FtsFieldConst.FIELD_TITLE).append(":").append(keywords)
						.append(" OR ").append(FtsFieldConst.FIELD_ABSTRACTS).append(":").append(keywords)
						.append(" OR ").append(FtsFieldConst.FIELD_CONTENT).append(":").append(keywords).append(" OR ")
						.append(FtsFieldConst.FIELD_KEYWORDS).append(":").append(keywords).toString();
				indexBuilder.filterByTRSL(trsl);
				countBuiler.filterByTRSL(trsl);
			}
			if (!"ALL".equals(source)) {
				// 单选状态
				if ("国内新闻".equals(source)) {
					String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻 NOT ")
							.append(FtsFieldConst.FIELD_SITENAME).append(":百度贴吧").toString();
					indexBuilder.filterByTRSL(trsl);
					countBuiler.filterByTRSL(trsl);
				} else if ("国内论坛".equals(source)) {
					String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内论坛 OR ")
							.append(FtsFieldConst.FIELD_SITENAME).append(":百度贴吧").toString();
					indexBuilder.filterByTRSL(trsl);
					countBuiler.filterByTRSL(trsl);
				} else {
					indexBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
					countBuiler.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
				}
			}
			switch (sort) { // 排序
				case "desc":
					indexBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					countBuiler.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					break;
				case "asc":
					indexBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					countBuiler.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					break;
				case "hot":
					return infoListService.getHotList(indexBuilder, countBuiler, loginUser,"column");
				default:
					indexBuilder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					countBuiler.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					break;
			}
			countBuiler.setDatabase(Const.HYBASE_NI_INDEX);
			indexBuilder.setDatabase(Const.HYBASE_NI_INDEX);
			return infoListService.getDocList(indexBuilder, loginUser, false,false,false,false,"column");
		}
	}

	@Override
	public Object hotKeywordList(QueryBuilder builder, String sort, String area, String source, String timeRange,
								 String hotKeywords, String keywords) throws TRSException {
		QueryBuilder countBuiler = new QueryBuilder();
		// 取地域名
		if (!"ALL".equals(area)) {
			String province = districtInfoService.province(area);
			// 地域只传一个 所以不用分割了
			area = "中国\\\\" + province + "*";
			builder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + area);
		}
		// 取loginUser
		User loginUser = UserUtils.getUser();
		// 分用哪个方法
		if ("微博".equals(source)) {
			if (StringUtil.isNotEmpty(hotKeywords)) {
				String trsl = new StringBuffer().append(FtsFieldConst.FIELD_STATUS_CONTENT).append(":")
						.append(hotKeywords).toString();
				builder.filterByTRSL(trsl);
			}
			// 结果中搜索
			if (StringUtil.isNotEmpty(keywords)) {
				String trsl = new StringBuffer().append(FtsFieldConst.FIELD_STATUS_CONTENT).append(":").append(keywords)
						.toString();
				builder.filterByTRSL(trsl);
			}
			builder.filterField(FtsFieldConst.FIELD_CREATED_AT, DateUtil.formatTimeRange(timeRange), Operator.Between);
			// 然后还得分是否是按照热度排序
			switch (sort) { // 排序
				case "desc":
					builder.orderBy(FtsFieldConst.FIELD_CREATED_AT, true);
					break;
				case "asc":
					builder.orderBy(FtsFieldConst.FIELD_CREATED_AT, false);
					break;
				case "hot":
					return infoListService.getHotListStatus(builder, countBuiler, loginUser,"column");
				default:
					builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					break;
			}
			return infoListService.getStatusList(builder, loginUser,true,false,false,false,"column");
		} else if ("微信".equals(source)) {
			builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(timeRange), Operator.Between);
			if (StringUtil.isNotEmpty(hotKeywords)) {
				String trsl = new StringBuffer().append(FtsFieldConst.FIELD_TITLE).append(":").append(hotKeywords)
						.append(" OR ").append(FtsFieldConst.FIELD_CONTENT).append(":").append(hotKeywords).toString();
				builder.filterByTRSL(trsl);
			}
			// 结果中搜索
			if (StringUtil.isNotEmpty(keywords)) {
				String trsl = new StringBuffer().append(FtsFieldConst.FIELD_TITLE).append(":").append(keywords)
						.append(" OR ").append(FtsFieldConst.FIELD_CONTENT).append(":").append(keywords).toString();
				builder.filterByTRSL(trsl);
			}
			switch (sort) { // 排序
				case "desc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					break;
				case "asc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					break;
				case "hot":
					return infoListService.getHotListWeChat(builder, countBuiler, loginUser,"column");
				default:
					builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					break;
			}
			log.info(builder.asTRSL());
			return infoListService.getWeChatList(builder, loginUser,true,false,false,false,"column");
		} else {// 传统
			if (StringUtil.isNotEmpty(timeRange)) {
				builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(timeRange), Operator.Between);
			}
			if (StringUtil.isNotEmpty(hotKeywords)) {
				String trsl = new StringBuffer().append(FtsFieldConst.FIELD_TITLE).append(":").append(hotKeywords)
						.append(" OR ").append(FtsFieldConst.FIELD_ABSTRACTS).append(":").append(hotKeywords)
						.append(" OR ").append(FtsFieldConst.FIELD_CONTENT).append(":").append(hotKeywords)
						.append(" OR ").append(FtsFieldConst.FIELD_KEYWORDS).append(":").append(hotKeywords).toString();
				builder.filterByTRSL(trsl);
			} // 结果中搜索
			if (StringUtil.isNotEmpty(keywords)) {
				String trsl = new StringBuffer().append(FtsFieldConst.FIELD_TITLE).append(":").append(keywords)
						.append(" OR ").append(FtsFieldConst.FIELD_ABSTRACTS).append(":").append(keywords)
						.append(" OR ").append(FtsFieldConst.FIELD_CONTENT).append(":").append(keywords).append(" OR ")
						.append(FtsFieldConst.FIELD_KEYWORDS).append(":").append(keywords).toString();
				builder.filterByTRSL(trsl);
			}
			// if (!"ALL".equals(source)) {
			// // 单选状态
			// if ("国内新闻".equals(source)) {
			// String trsl = new
			// StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻 NOT ")
			// .append(FtsFieldConst.FIELD_SITENAME).append(":百度贴吧").toString();
			// builder.filterByTRSL(trsl);
			// } else if ("国内论坛".equals(source)) {
			// String trsl = new
			// StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内论坛 OR ")
			// .append(FtsFieldConst.FIELD_SITENAME).append(":百度贴吧").toString();
			// builder.filterByTRSL(trsl);
			// } else {
			// builder.filterField(FtsFieldConst.FIELD_GROUPNAME, source,
			// Operator.Equal);
			// }
			// }
			switch (sort) { // 排序
				case "desc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					break;
				case "asc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					break;
				case "hot":
					return infoListService.getHotList(builder, countBuiler, loginUser,"column");
				default:
					// builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					break;
			}
			builder.setDatabase(Const.HYBASE_NI_INDEX);
			log.info("热搜列表" + builder.asTRSL());
			return infoListService.getDocList(builder, loginUser, false,false,false,false,"column");
		}
	}

	/**
	 * 转换 实体类型
	 *
	 * @param document
	 *            需转换类型 FtsDocument FtsDocumentWeChat FtsDocumentStatus
	 * @return 最终所需类型 FtsDocumentChaos
	 */
	private FtsDocumentChaos changeClass(IDocument document) {
		FtsDocumentChaos ftsDocumentChaos = new FtsDocumentChaos();

		Field[] declaredFields = document.getClass().getDeclaredFields();
		for (int i = 0; i < declaredFields.length; i++) {
			String name = declaredFields[i].getName();
			Class<?> type = declaredFields[i].getType();
			// 通过反射拼类中的get方法
			String getMehtodName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
			if (boolean.class.equals(type)) {
				getMehtodName = "is" + name.substring(0, 1).toUpperCase() + name.substring(1);
			}
			// 获取当前参数 类名
			String simpleName = document.getClass().getSimpleName();
			Method getMethod = null;
			try {
				if (simpleName.equals("FtsDocument")) {
					// 返回方法对象 参数一：方法的名字 参数二：方法的参数类型
					// 形参类型
					getMethod = FtsDocument.class.getDeclaredMethod(getMehtodName, new Class[] {});
				} else if (simpleName.equals("FtsDocumentWeChat")) {
					getMethod = FtsDocumentWeChat.class.getDeclaredMethod(getMehtodName, new Class[] {});
				} else if (simpleName.equals("FtsDocumentStatus")) {
					getMethod = FtsDocumentStatus.class.getDeclaredMethod(getMehtodName, new Class[] {});
				}

				// 执行方法 参数一：执行那个对象中的方法 参数二：该方法的参数
				Object invoke = getMethod.invoke(document, new Object[] {});

				// 实体类 字段匹配 获取相应值
				if ("sid".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setSid(invoke.toString());
					}
				} else if ("title".equals(name) || "urlTitle".equals(name) || "statusContent".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setUrlTitle(invoke.toString());
					}
				} else if ("md5Tag".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setMd5Tag(invoke.toString());
					}
				} else if ("appraise".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setAppraise(invoke.toString());
					}
				} else if ("screenName".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setScreenName(invoke.toString());
					}
				} else if ("rttCount".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setRttCount(Long.valueOf(invoke.toString()));
					}
				} else if ("commtCount".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setCommtCount(Long.valueOf(invoke.toString()));
					}
				} else if ("hkey".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setHkey(invoke.toString());
					}
				} else if ("mid".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setMid(invoke.toString());
					}
				} else if ("siteName".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setSiteName(invoke.toString());
					}
				} else if ("authors".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setAuthors(invoke.toString());
					}
				} else if ("urlName".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setUrlName(invoke.toString());
					}
				} else if ("nreserved1".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setNreserved1(invoke.toString());
					}
				} else if ("urlTime".equals(name) || "createdAt".equals(name)) {
					if (null != invoke) {
						// 日期类型
						// SimpleDateFormat sdf = new
						// SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
						// String format = sdf.format(invoke);
						ftsDocumentChaos.setUrlTime((Date) invoke);
						// ftsDocumentChaos.setUrlTime(sdf.parse(format));
					}
				}

				if (simpleName.equals("FtsDocument")) {
					if ("groupName".equals(name)) {
						ftsDocumentChaos.setGroupName(invoke.toString());
					}
					Map<String, String> timeDifference = DateUtil.timeDifference((FtsDocument) document);
					boolean isNew = false;
					if (ObjectUtil.isNotEmpty(timeDifference.get("timeAgo"))) {
						isNew = true;
						ftsDocumentChaos.setTimeAgo(timeDifference.get("timeAgo"));
						ftsDocumentChaos.setNew(isNew);
					} else {
						ftsDocumentChaos.setTimeAgo(timeDifference.get("urlTime"));
						ftsDocumentChaos.setNew(isNew);
					}
				} else if (simpleName.equals("FtsDocumentWeChat")) {
					if ("groupName".equals(name)) {
						ftsDocumentChaos.setGroupName("微信");
					}
					Map<String, String> timeDifference = DateUtil.timeDifference((FtsDocumentWeChat) document);
					boolean isNew = false;
					if (ObjectUtil.isNotEmpty(timeDifference.get("timeAgo"))) {
						isNew = true;
						ftsDocumentChaos.setTimeAgo(timeDifference.get("timeAgo"));
						ftsDocumentChaos.setNew(isNew);
					} else {
						ftsDocumentChaos.setTimeAgo(timeDifference.get("urlTime"));
						ftsDocumentChaos.setNew(isNew);
					}
				} else if (simpleName.equals("FtsDocumentStatus")) {
					if ("groupName".equals(name)) {
						ftsDocumentChaos.setGroupName("微博");
					}
					Map<String, String> timeDifference = DateUtil.timeDifference((FtsDocumentStatus) document);
					boolean isNew = false;
					if (ObjectUtil.isNotEmpty(timeDifference.get("timeAgo"))) {
						isNew = true;
						ftsDocumentChaos.setTimeAgo(timeDifference.get("timeAgo"));
						ftsDocumentChaos.setNew(isNew);
					} else {
						ftsDocumentChaos.setTimeAgo(timeDifference.get("createdAt"));
						ftsDocumentChaos.setNew(isNew);
					}
				}

			} catch (NoSuchMethodException e) {
				log.error("ColumnServiceImpl.java  changeClass（） fail: NoSuchMethodException" + e.getMessage());
			} catch (IllegalAccessException e) {
				log.error("ColumnServiceImpl.java  changeClass（） fail: IllegalAccessException" + e.getMessage());
			} catch (InvocationTargetException e) {
				log.error("ColumnServiceImpl.java  changeClass（） fail: InvocationTargetException" + e.getMessage());
			}
		}
		return ftsDocumentChaos;
	}

	private Map<String, String> timeDifference(FtsDocumentChaos doc) {
		Map<String, String> map = new HashMap<>();
		long nowTime = new Date().getTime();
		long lastTime = doc.getUrlTime().getTime();
		long result = nowTime - lastTime;
		int days = (int) (result / (1000 * 60 * 60 * 24));
		if (days < 1) {
			// 计算小时查
			int hours = (int) (result / (1000 * 60 * 60));
			// 计算分钟差
			int minutes = (int) (result / (1000 * 60));
			if (0 == minutes) {
				map.put("timeAgo", "刚刚");
			} else if (0 != minutes) {
				map.put("timeAgo", minutes + "分钟前");
			}
			if (0 != hours) {
				map.put("timeAgo", hours + "小时前");
			}
		} else {
			map.put("urlTime", DateUtil.date2String(doc.getUrlTime(), DateUtil.FMT_TRS_yMdhms));
		}

		return map;
	}

	/**
	 * 查询列表
	 */
	@Override
	public Object selectList(IndexTab indexTab, int pageNo, int pageSize, String source, String emotion, String entityType,
							 String dateTime, String key, String sort,  String invitationCard,
							 String forwarPrimary, String keywords, String fuzzyValueScope,String read,String mediaLevel,String mediaIndustry,String contentIndustry,String filterInfo,
							 String contentArea,String mediaArea,String preciseFilter,String imgOcr,ChartPageInfo chartPageInfo) {
		String userName = UserUtils.getUser().getUserName();
		long start = new Date().getTime();
		if (indexTab != null) {
			String timerange = indexTab.getTimeRange();
			try {
				AbstractColumn column = ColumnFactory.createColumn(indexTab.getType());
				ColumnConfig config = new ColumnConfig();
				if("hittitle".equals(sort)){
					indexTab.setWeight(true);
				}
				//config.addFilterCondition(read, mediaLevel, mediaIndustry, contentIndustry, filterInfo, contentArea, mediaArea, preciseFilter);
				config.initSection(indexTab, timerange, pageNo, pageSize, source, emotion, entityType, dateTime, key, sort,  invitationCard,
						keywords, fuzzyValueScope, forwarPrimary, read, mediaLevel, mediaIndustry, contentIndustry, filterInfo, contentArea, mediaArea, preciseFilter,imgOcr);
				config.setChartPage(chartPageInfo);
				column.setCommonListService(commonListService);
				column.setCommonChartService(commonChartService);
				column.setCommonListService(commonListService);
				column.setConfig(config);
				Object list = column.getSectionList();
				/*
				处理数据
				InfoListResult
				 */
				if (list != null) {
					String wordIndex = indexTab.getKeyWordIndex();
					InfoListResult infoListResult = (InfoListResult) list;
					String trslk = infoListResult.getTrslk();
					if (infoListResult.getContent() != null) {
						PagedList<Object> resultContent = CommonListChartUtil.formatListData(infoListResult,trslk,wordIndex);
						infoListResult.setContent(resultContent);
					}
					list = infoListResult;
				}
				return list;
			} catch (Exception e) {
				log.info(e.toString());
			} finally {
				long end = new Date().getTime();
				long timeApi = end - start;
				if (userName != null && userName.equals("xiaoying")) {
					log.info("xiaoying调用接口用了" + timeApi + "ms");
				}
			}
		}
		return null;
	}

	/**
	 * 日常监测图表数据导出
	 */
	@Override
	public ByteArrayOutputStream exportChartData(String data, IndexTabType indexTabType, String sheet) throws IOException {
		ExcelData content = new ExcelData();
		if (indexTabType != null) {
			if (StringUtil.isNotEmpty(data)) {
				if (IndexTabType.CHART_LINE.equals(indexTabType)) {
					JSONObject object = JSONObject.parseObject(data);
					exportChartLine(object, content);
				}else{
					JSONArray array = JSONObject.parseArray(data);
					if (IndexTabType.WORD_CLOUD.equals(indexTabType)) {
						exportWordCloud(array, content);
					} else if (IndexTabType.MAP.equals(indexTabType)) {
						exportMap(array, content);
					} else if (IndexTabType.CHART_BAR.equals(indexTabType) || IndexTabType.CHART_PIE.equals(indexTabType)) {
						exportData(array, content,ExcelConst.HEAD_PIE_BAR);
					}else if (IndexTabType.HOT_TOPIC_SORT.equals(indexTabType)) {
						exportData(array, content,ExcelConst.WEIBO_HOT_TOPIC);
					}else if (IndexTabType.CHART_PIE_EMOTION.equals(indexTabType)) {
						exportData(array, content,ExcelConst.EMOTION_DATA);
					}  else if (IndexTabType.CHART_BAR_CROSS.equals(indexTabType)) {
						exportDataBarCross(array, content);
						return ExcelFactory.getInstance().exportOfManySheet(content);
					}
				}
			}
		}
		return ExcelFactory.getInstance().export1(content,sheet);
	}

	/**
	 * 日常监测饼图和柱状图数据的导出
	 */
	private void exportData(JSONArray array,ExcelData content,String[] head ) throws IOException {

		content.setHead(head);  // { "媒体来源", "数值"}
		for (Object object : array) {
			JSONObject parseObject = JSONObject.parseObject(String.valueOf(object));

			String groupNameValue = parseObject.get("name").toString();
			String pageShowGroupName = CommonListChartUtil.formatPageShowGroupName(groupNameValue);
			if(StringUtil.isNotEmpty(pageShowGroupName)){
				groupNameValue =  pageShowGroupName;
			}
			String numValue = parseObject.get("value").toString();
			content.addRow(groupNameValue, numValue);
		}
	}

	private void exportDataBarCross(JSONArray array,ExcelData content) throws IOException {
		for(Object object : array){
			JSONObject jsonObject = JSONObject.parseObject(String.valueOf(object));
			String keyName = jsonObject.getString("name");
			JSONArray infoArr = jsonObject.getJSONArray("info");
			content.putHeadMap(keyName, Arrays.asList(ExcelConst.HEAD_PIE_BAR));
			if(infoArr != null && infoArr.size() >0 ){
				for(Object info : infoArr){
					JSONObject infoObject = JSONObject.parseObject(String.valueOf(info));
					String name = infoObject.getString("name");
					if(StringUtil.isNotEmpty(name)){
						String value = infoObject.getString("value");
						List<DataRow> rowList = new ArrayList<>();
						rowList.add(new DataRow(name));
						rowList.add(new DataRow(value));
						content.putSheet(keyName, rowList);
					}
				}
			}else{
				content.putSheet(keyName, new ArrayList<DataRow>());
			}
		}
	}

	private void exportChartLine(JSONObject jsonObject,ExcelData content) throws IOException {

		JSONObject single = jsonObject.getJSONObject("single");

		JSONArray groupNameArr = single.getJSONArray("legendData");
		JSONArray timeArr = single.getJSONArray("lineXdata");
		JSONArray countArr = single.getJSONArray("lineYdata");

		List<String> headList = new ArrayList<>();
		headList.add("");
		for (Object group : groupNameArr) {
			headList.add(String.valueOf(group));
		}
		String[] header = new String[headList.size()];
		header = headList.toArray(header);
		content.setHead(header);
		List<String[]> arrayList = new ArrayList<>();

		for(int i = 0;i<timeArr.size();i++){
			String[] rowData = new String[headList.size()];
			rowData[0] =  String.valueOf(timeArr.get(i));
			int j = 1;
			for(Object count : countArr){
				JSONArray oneCount = JSONObject.parseArray(String.valueOf(count));
				rowData[j] = String.valueOf(oneCount.get(i));
				j++;
			}
			arrayList.add(rowData);
		}
		for (String[] strings : arrayList) {
			content.addRow(strings);
		}
	}

	/**
	 * 词云数据的导出
	 */
	private void exportWordCloud(JSONArray array,ExcelData content) throws IOException {
		content.setHead(ExcelConst.HEAD_WORDCLOUD); // {"词语", "所属分组", "信息数量"}
		for (Object object : array) {
			JSONObject parseObject = JSONObject.parseObject(String.valueOf(object));

			String word = parseObject.get("name").toString();
			String count = parseObject.get("value").toString();
			String entityType = parseObject.get("entityType").toString();
			String group = "";
			if ("people".equals(entityType)){
				group = "人物";
			}else if("location".equals(entityType)){
				group = "地域";
			}else if( "agency".equals(entityType)){
				group = "机构";
			}
			content.addRow(word, group, count);
		}
	}

	/**
	 * 地域图数据的导出
	 */
	private void exportMap(JSONArray array,ExcelData content) throws IOException {
		content.setHead(ExcelConst.HEAD_MAP); // { "地域", "信息数量"};
		array.sort(Comparator.comparing(obj -> {
			JSONObject parseObject = JSONObject.parseObject(String.valueOf(obj));
			Long value = parseObject.getLongValue("value");
			return value;
		}).reversed());
		for (Object object : array) {
			JSONObject parseObject = JSONObject.parseObject(String.valueOf(object));
			String areaName = parseObject.get("name").toString();
			String areaCount = parseObject.get("value").toString();
			content.addRow(areaName, areaCount);
		}
	}
}
