package com.trs.netInsight.widget.column.service.impl;

import com.trs.jpa.utils.Criteria;
import com.trs.jpa.utils.Restrictions;
import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.util.CollectionsUtil;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.analysis.entity.ClassInfo;
import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.entity.emuns.ColumnFlag;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.column.repository.IndexSequenceRepository;
import com.trs.netInsight.widget.column.repository.IndexTabMapperRepository;
import com.trs.netInsight.widget.column.repository.IndexTabRepository;
import com.trs.netInsight.widget.column.service.*;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import com.trs.netInsight.widget.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

/**
 * 栏目映射关系操作服务实现类
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年9月18日
 *
 */
@Service
@Slf4j
public class IndexTabMapperServiceImpl implements IIndexTabMapperService {

	@Autowired
	private IndexTabMapperRepository tabMapperRepository;

	@Autowired
	private IndexTabRepository indexTabRepository;

	@Autowired
	private IIndexTabService indexTabService;

	@Autowired
	private OrganizationRepository organizationService;

	@Autowired

	private IIndexPageService indexPageService;

	@Autowired
	private UserRepository userService;
	@Autowired
	private IColumnService columnService;

	@Autowired
	private IColumnChartService columnChartService;
	@Autowired
	private IndexSequenceRepository indexSequenceRepository;

	@Override
	public IndexTabMapper findOne(String indexTabMapperId) {
		return this.tabMapperRepository.findOne(indexTabMapperId);
	}

	@Override
	public List<IndexTabMapper> findByUserIdAndShare(String userId, boolean share) {
		String orgId = UserUtils.getUser().getOrganizationId();
		return tabMapperRepository.findByUserIdAndShareAndIsMeAndOrganizationId(userId, share, true,orgId,
				new Sort(Direction.DESC, "createdTime"));
	}

	@Override
	public List<Map<String, Object>> computeShareMapperNumber() {
		User user = UserUtils.getUser();
		// 计算数量
		List<ClassInfo> results = null;
		results = this.tabMapperRepository.computeCountByOrgIdGroupByUserId(user.getOrganizationId());

		List<Map<String, Object>> data = new ArrayList<>(results.size());
		Map<String, Object> userMap = null;
		// 排除自身并完成排序
		Organization organization = organizationService.findOne(user.getOrganizationId());
		if (organization != null) {
			// 获取机构管理员用户
			User admin = userService.findOne(organization.getAdminUserId());
			if (results != null && results.size() > 0) {
				for (ClassInfo classInfo : results) {
					userMap = new HashMap<>();
					User currcentUser = userService.findOne(classInfo.strValue);
					if (currcentUser == null) {
						continue;
					}
					userMap.put("name", currcentUser.getUserName());
					userMap.put("number", classInfo.iRecordNum);
					userMap.put("user", currcentUser);
					// 机构管理员排在第一位
					if (admin != null && user.getId().equals(admin.getId())) {
						data.add(0, userMap);
					} else {
						data.add(userMap);
					}
					if (user.getId().equals(classInfo.strValue)) {
						userMap.put("isMe", true);
					}
				}
			}
		}
		return data;
	}

	@Override
	public IndexTabMapper save(IndexTabMapper mapper) {

		return this.tabMapperRepository.save(mapper);
	}

	@Override
	public IndexTabMapper update(IndexTabMapper mapper) {

		return this.tabMapperRepository.saveAndFlush(mapper);
	}

	@Override
	public void delete(String mapperId) {
		Integer deleteColumnChart = columnChartService.deleteCustomChartForTabMapper(mapperId);
		log.info("删除当前栏目下统计和自定义图表共："+deleteColumnChart +"条");
		this.tabMapperRepository.delete(mapperId);
	}

	@Override
	public void deleteByUserId(String userId) throws TRSException {
		List<IndexTabMapper> indexTabMappers = tabMapperRepository.findByUserId(userId);
		if (ObjectUtil.isNotEmpty(indexTabMappers)){
			for (IndexTabMapper indexTabMapper : indexTabMappers) {
				this.deleteMapper(indexTabMapper.getId());
			}
		}
	}

	@Override
	public void batchDelete(List<String> mapperIds) {

	}

	@Override
	@Transactional
	@SuppressWarnings("unchecked")
	public void unShare(String[] mapperIds) throws TRSException {
		List<String> asList = Arrays.asList(mapperIds);
		List<IndexTabMapper> changeMappers = tabMapperRepository.findAll(asList);
		if (CollectionsUtil.isNotEmpty(changeMappers)) {
			for (IndexTabMapper indexTabMapper : changeMappers) {
				List<IndexTabMapper> aboutMappers = null;
				indexTabMapper.setShare(false);
				// 删除除自身外所有相关的引用
				aboutMappers = tabMapperRepository.findByIndexTab(indexTabMapper.getIndexTab());
				if (aboutMappers.contains(indexTabMapper)) {
					aboutMappers.remove(indexTabMapper);
				}
				//引用栏目对应的图表信息也应该删除
				Integer deleteAbMapperColumnChart = 0;
				for(IndexTabMapper abMapper : aboutMappers){
					deleteAbMapperColumnChart+= columnChartService.deleteCustomChartForTabMapper(abMapper.getId());
				}
				log.info("删除当前栏目相关统计和自定义图表共："+deleteAbMapperColumnChart +"条");
				tabMapperRepository.delete(aboutMappers);
			}
			this.tabMapperRepository.save(changeMappers);
		}
		tabMapperRepository.flush();// 立即提交

		// 根据栏目id检索栏目组，并更改栏目组共享状态
		List<IndexTabMapper> mappers = this.tabMapperRepository.findAll(asList);
		List<IndexPage> indexPages = CollectionsUtil.getAttrList(mappers, "indexPage", IndexPage.class);
		if (CollectionsUtil.isNotEmpty(indexPages)) {
			boolean share = true;
			for (IndexPage indexPage : indexPages) {
				// 检索栏目组下是否仍存在共享栏目
				share = this.tabMapperRepository.existsByIndexPageAndShareAndIsMe(indexPage, true, true);
				if (!share) {
					indexPage.setShare(false);
				}
			}
			this.indexPageService.saveAndFulsh(indexPages);
		}
	}

	@Override
	@Transactional
	@SuppressWarnings("unchecked")
	public void share(String[] mapperIds) throws TRSException {
		// 修改栏目映射关系共享状态
		String userId = UserUtils.getUser().getId();
		List<String> asList = Arrays.asList(mapperIds);
		List<IndexTabMapper> changeMappers = tabMapperRepository.findAll(asList);
		if (CollectionsUtil.isNotEmpty(changeMappers)) {
			for (IndexTabMapper indexTabMapper : changeMappers) {
				indexTabMapper.setShare(true);
				indexTabMapper.setUserId(userId);
			}
			this.tabMapperRepository.save(changeMappers);
		}
		tabMapperRepository.flush();// 立即提交

		// 根据栏目id检索栏目组，并更改栏目组共享状态
		List<IndexTabMapper> mappers = this.tabMapperRepository.findAll(asList);
		List<IndexPage> indexPages = CollectionsUtil.getAttrList(mappers, "indexPage", IndexPage.class);
		if (CollectionsUtil.isNotEmpty(indexPages)) {
			for (IndexPage indexPage : indexPages) {
				indexPage.setShare(true);
//				indexPage.setUserId(userId);
			}
			this.indexPageService.saveAndFulsh(indexPages);
		}
	}

	@Override
	public List<IndexPage> searchOrgAdminSharePages() {
		// 检索当前机构
		User current = UserUtils.getUser();
		Organization organization = this.organizationService.findOne(current.getOrganizationId());
		return this.indexPageService.findByUserIdAndShare(organization.getAdminUserId(), true);
	}

	@Override
	public long countByIndexPage(IndexPage indexPage) {
		return this.tabMapperRepository.countByIndexPage(indexPage);
	}

	@Override
	public List<IndexTabMapper> findByIndexPageId(String indexPageId) {
		IndexPage indexPage = indexPageService.findOne(indexPageId);
		return tabMapperRepository.findByIndexPage(indexPage);
	}

	@Override
	public List<IndexTabMapper> findByIndexPage(IndexPage indexPage) {
		List<IndexTabMapper> indexTabList = tabMapperRepository.findByIndexPage(indexPage);
		// 判断当前的大栏目有没有被排序过
		if (indexPage.getOrderBefore() == null || !"after".equals(indexPage.getOrderBefore())) {
			List<IndexTabMapper> nohideList = new ArrayList<>();// 展示的
			List<IndexTabMapper> hideList = new ArrayList<>();// 隐藏的
			for (IndexTabMapper indextab : indexTabList) {
				if (indextab.isHide()) {
					hideList.add(indextab);
				} else {
					nohideList.add(indextab);
				}
			}
			indexTabList = new ArrayList<>();
			indexTabList.addAll(nohideList);
			indexTabList.addAll(hideList);
			// 根据各自隐藏展示的顺序排序
			for (int i = 0; i < hideList.size(); i++) {
				IndexTabMapper index = hideList.get(i);
				index.setSequence(i + 1);
				// tabMapperRepository.save(index);
			}
			tabMapperRepository.save(hideList);
			for (int j = 0; j < nohideList.size(); j++) {
				IndexTabMapper index = nohideList.get(j);
				index.setSequence(j + 1);
			}
			tabMapperRepository.save(nohideList);
			// 排序之后 存标示为已排序
			indexPage.setOrderBefore("after");
			indexPageService.save(indexPage);
		}
		return indexTabList;
	}

	@Override
	public List<IndexTabMapper> findByIndexPageOfApp(IndexPage indexPage) {
		List<IndexTabMapper> indexTabList = tabMapperRepository.findByIndexPageId(indexPage.getId(),new Sort(Direction.ASC,"appsequence"));
		List<IndexTabMapper> nohideList = new ArrayList<>();// 展示的
		List<IndexTabMapper> hideList = new ArrayList<>();// 隐藏的
		int showCount = 1;
		int hideCount = 1;
		for(int i = 0;i<indexTabList.size();i++){
			if (indexTabList.get(i).getAppsequence() != 0){
				if (indexTabList.get(i).isHide()){
					IndexTabMapper hideMapper = indexTabList.get(i);
					hideMapper.setAppsequence(hideCount);
					hideMapper=tabMapperRepository.save(hideMapper);
					hideList.add(hideMapper);
					hideCount++;

				}else{
					IndexTabMapper showMapper = indexTabList.get(i);
					showMapper.setAppsequence(showCount);
					showMapper=tabMapperRepository.save(showMapper);
					nohideList.add(showMapper);
					showCount++;
				}
			}
		}
		for(int i = 0;i<indexTabList.size();i++){
			if (indexTabList.get(i).getAppsequence() == 0){
				IndexTabMapper mapper = indexTabList.get(i);
				IndexTab indexTab = mapper.getIndexTab();
				String type = indexTab.getType();
				//app日常监测新增图表类栏目
				//if (type.indexOf(ColumnConst.LIST_STATUS_COMMON)!=-1||type.indexOf(ColumnConst.LIST_TWITTER)!=-1||type.indexOf(ColumnConst.LIST_FaceBook)!=-1
				//	||type.indexOf(ColumnConst.LIST_WECHAT_COMMON)!=-1||type.indexOf(ColumnConst.LIST_NO_SIM)!=-1||type.indexOf(ColumnConst.LIST_SIM)!=-1){
				if (Boolean.valueOf(mapper.isHide())){
					mapper.setAppsequence(hideList.size()+1);
					IndexTabMapper sortedMapper = tabMapperRepository.save(mapper);
					hideList.add(sortedMapper);
				}else{
					mapper.setAppsequence(nohideList.size()+1);
					IndexTabMapper sortedMapper = tabMapperRepository.save(mapper);
					nohideList.add(sortedMapper);
				}

			}
		}
		indexTabList = new ArrayList<>();
		indexTabList.addAll(nohideList);
		indexTabList.addAll(hideList);
		return indexTabList;
	}

	@Override
	@Transactional
	public void deleteMapper(String indexMapperId) throws TRSException {
		try {
			if (StringUtils.isNotBlank(indexMapperId)) {
				// 多个删除用;分割
				String[] idsplit = indexMapperId.split(";");
				for (String ids : idsplit) {
					IndexTabMapper mapper = tabMapperRepository.findOne(ids);
					// 修改顺序
					if (mapper != null) {
						String typeId = mapper.getTypeId();
						User user = userService.findOne(mapper.getUserId());


						if(!"top".equals(mapper.getTopFlag())){
							//对同层级的数据重新排序 - 去掉自己
							//栏目类型为1，
							columnService.moveSequenceForColumn(mapper.getId(), ColumnFlag.IndexTabFlag,user);
						}
						//删除当前栏目对应的自定义图表
						Integer deleteColumnChart = columnChartService.deleteCustomChartForTabMapper(mapper.getId());
						log.info("删除当前栏目下统计和自定义图表共："+deleteColumnChart +"条");
						if (mapper.isMe()) {
							// 删除所有相关的mapper
							List<IndexTabMapper> aboutMapper = tabMapperRepository.findByIndexTab(mapper.getIndexTab());
							tabMapperRepository.delete(aboutMapper);
							Integer deleteAbMapperColumnChart = 0;
							for(IndexTabMapper abMapper : aboutMapper){
								deleteAbMapperColumnChart+= columnChartService.deleteCustomChartForTabMapper(abMapper.getId());
							}
							log.info("删除当前栏目相关统计和自定义图表共："+deleteAbMapperColumnChart +"条");
							indexTabService.delete(mapper.getIndexTab().getId());
						} else {
							// 引用栏目只删除本身
							tabMapperRepository.delete(mapper);
						}
						indexSequenceRepository.delete(indexSequenceRepository.findByIndexTabId(mapper.getId()));
					}
				}
			}
		} catch (Exception e) {
			throw new TRSException(e);
		}
	}

	@Override
	@Transactional
	public void hide(String indexMapperId, String hide) throws TRSException {
		if (StringUtils.isNotBlank(indexMapperId)) {
			String[] splitId = indexMapperId.split(";");
			String[] splitHide = hide.split(";");
			for (int i = 0; i < splitHide.length; i++) {
				List<String> idShow = new ArrayList<>();
				idShow.add(splitId[i]);
				this.hide(idShow, Boolean.valueOf(splitHide[i]));
			}
		}
	}

	/**
	 * 批量修改隐藏状态
	 *
	 * @since changjiang @ 2018年10月10日
	 * @param mapperIds
	 * @param hide
	 * @throws TRSException
	 * @Return : void
	 */
	private void hide(List<String> mapperIds, boolean hide) throws TRSException {
		/*
		 * if (CollectionsUtil.isNotEmpty(mapperIds)) { List<IndexTabMapper>
		 * mappers = tabMapperRepository.findAll(mapperIds); if
		 * (CollectionsUtil.isNotEmpty(mappers)) {
		 * CollectionsUtil.batchUpdateAttr(mappers, "hide", hide); }
		 * tabMapperRepository.save(mappers); }
		 */
		// 调整后续数据的排序 隐藏和展示都放到最后一位
		for (String id : mapperIds) {
			IndexTabMapper mapper = tabMapperRepository.findOne(id);
			// List<IndexTabMapper> indexpageList =
			// tabMapperRepository.findByIndexPage(mapper.getIndexPage());
			if (mapper != null) {
				Criteria<IndexTabMapper> criteria = new Criteria<>();
				Criteria<IndexTabMapper> criteriaAfter = new Criteria<>();
				criteria.add(Restrictions.eq("indexPage", mapper.getIndexPage()));
				criteria.add(Restrictions.gt("sequence", mapper.getSequence()));
				criteria.add(Restrictions.eq("hide", !hide));
				List<IndexTabMapper> indexpageList = tabMapperRepository.findAll(criteria);
				for (IndexTabMapper tab : indexpageList) {
					tab.setSequence(tab.getSequence() - 1);
					// 考虑加锁 比较lastupdatetime
					tabMapperRepository.save(tab);
				}
				criteriaAfter.add(Restrictions.eq("hide", hide));
				criteriaAfter.add(Restrictions.eq("indexPage", mapper.getIndexPage()));
				mapper.setSequence(tabMapperRepository.findAll(criteriaAfter).size() + 1);
				mapper.setHide(hide);
				// 考虑加锁
				tabMapperRepository.save(mapper);// 排序后后放到即将隐藏/展示的的最后一位
			}
		}
	}

	@Override
	public long computeShareByOrg() {
		User user = UserUtils.getUser();
		if (UserUtils.isRoleOrdinary(user) || UserUtils.isRoleAdmin(user)){
			return tabMapperRepository.countByOrganizationIdAndIsMeAndShare(user.getOrganizationId(), true, true);
		}
		return 0;
	}

	@Override
	public void delete(IndexTabMapper mapper) {
		Integer deleteColumnChart = columnChartService.deleteCustomChartForTabMapper(mapper.getId());
		log.info("删除当前栏目下统计和自定义图表共："+deleteColumnChart +"条");
		tabMapperRepository.delete(mapper);
	}

	@Override
	public List<IndexTabMapper> findByIndexTab(IndexTab indexTab) {
		return tabMapperRepository.findByIndexTab(indexTab);
	}

	@Override
	public IndexTabMapper findByIdAndUserId(String id, String userId) {
		return tabMapperRepository.findByIdAndUserId(id, userId);
	}

	@Override
	public List<IndexTabMapper> findByUserId(String userId) {
		return tabMapperRepository.findByUserIdAndSubGroupIdIsNull(userId);
	}

	@Override
	public void changeTabWidth(String indexMapperId, String tabWidth) {
		// 修改三级栏目的半栏通栏属性 id和tabWidth都用;分割 顺序一一对应
		String[] idsplit = indexMapperId.split(";");
		String[] widthsplit = tabWidth.split(";");
		for (int i = 0; i < idsplit.length; i++) {
			IndexTabMapper findOne = tabMapperRepository.findOne(idsplit[i]);
			// 有可能同时删除和修改半栏通栏
			if (findOne != null) {
				findOne.setTabWidth(Integer.valueOf(widthsplit[i]));
				tabMapperRepository.save(findOne);
			}
		}

	}

	public Object updateHistortIndexTab(){
		try{
			List<IndexTabMapper> list = tabMapperRepository.findAll();
			if(list != null && list.size() >0){
				int n = 0;
				for(IndexTabMapper indexTabMapper : list){
					IndexPage indexPage = indexTabMapper.getIndexPage();
					IndexTab indexTab = indexTabMapper.getIndexTab();
					indexTab.setTypeId(indexPage.getTypeId());
					indexTabMapper.setTypeId(indexPage.getTypeId());
					indexTabMapper.setIndexTab(indexTab);
					tabMapperRepository.save(indexTabMapper);
					indexTabRepository.save(indexTab);
					n++;
					System.out.println("当前执行为第"+n + "个，名字为："+indexTab.getName());

				}
				tabMapperRepository.flush();
				indexTabRepository.flush();
			}
			return "没毛病，你就放心吧";
		}catch (Exception e){

			return "修改失败了哦" + e.getMessage();
		}
	}



}
