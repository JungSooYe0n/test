package com.trs.netInsight.widget.alert.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocumentAlert;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.UserHelp;
import com.trs.netInsight.widget.alert.entity.AlertAccount;
import com.trs.netInsight.widget.alert.entity.AlertWebSocket;
import com.trs.netInsight.widget.alert.entity.PageAlert;
import com.trs.netInsight.widget.alert.entity.repository.AlertAccountRepository;
import com.trs.netInsight.widget.alert.entity.repository.AlertWebSocketRepository;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.report.service.IFavouritesService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.trs.jpa.utils.Criteria;
import com.trs.jpa.utils.Criterion.MatchMode;
import com.trs.jpa.utils.Restrictions;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.widget.alert.entity.AlertEntity;
import com.trs.netInsight.widget.alert.entity.enums.SendWay;
import com.trs.netInsight.widget.alert.entity.repository.AlertRepository;
import com.trs.netInsight.widget.alert.service.IAlertService;
import com.trs.netInsight.widget.report.entity.Favourites;
import com.trs.netInsight.widget.report.entity.repository.FavouritesRepository;
import com.trs.netInsight.widget.user.entity.User;

import lombok.extern.slf4j.Slf4j;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * 预警服务接口实现类
 *
 * Create by yan.changjiang on 2017年11月20日
 */
@Service
@Slf4j
public class AlertServiceImpl implements IAlertService {

	@Autowired
	private AlertRepository alertRepository;
	@Autowired
	private FavouritesRepository favouritesRepository;
	@Autowired
	private UserHelp userService;
	@Autowired
	private FullTextSearch hybase8SearchServiceNew;
	@Autowired
	private IFavouritesService favouritesService;
	@Autowired
	private AlertAccountRepository alertAccountRepository;
	@Autowired
	private AlertWebSocketRepository alertWebSocketRepository;

	@Value("${http.alert.netinsight.url}")
	private String alertNetinsightUrl;

	@Override
	public List<AlertEntity> selectAll(String userId, int pageSize) throws TRSException {
		List<AlertEntity> countList;
		if (pageSize <= 0) {
			countList = alertRepository.findByUserId(userId, new Sort(Sort.Direction.DESC, "lastModifiedTime"));
		} else {
			countList = alertRepository.findByUserId(userId,
					new PageRequest(0, pageSize, new Sort(Sort.Direction.DESC, "lastModifiedTime")));
		}
		return countList;
	}

	@Override
	public void save(AlertEntity alertEntity) {
		alertRepository.save(alertEntity);
	}

	@Override
	public void delete(String alertId) {
		alertRepository.delete(alertId);
	}

	@Override
	public List<AlertEntity> findByUserId(String uid, Sort sort) {
		return alertRepository.findByUserId(uid, sort);
	}

	@Override
	public List<AlertEntity> findByUser(User user,List<String> sids) {
		String subGroupId = user.getSubGroupId();
		String userId = user.getId();
		if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
			return alertRepository.findByUserIdAndSidIn(userId, sids);
		}else {
			return alertRepository.findBySubGroupIdAndSidIn(subGroupId,sids);
		}
	}

	@Override
	public List<AlertEntity> findByOrganizationId(String organizationId, Sort sort) {
		return alertRepository.findByOrganizationId(organizationId, sort);
	}

	@Override
	public List<AlertEntity> selectByOrganizationId(String organizationId, Integer pageSize) {
		List<AlertEntity> countList;
		if (pageSize <= 0) {
			countList = alertRepository.findByOrganizationId(organizationId,
					new Sort(Sort.Direction.DESC, "lastModifiedTime"));
		} else {
			countList = alertRepository.findByOrganizationId(organizationId,
					new PageRequest(0, pageSize, new Sort(Sort.Direction.DESC, "lastModifiedTime")));
		}
		return countList;
	}

	@Override
	public void save(Iterable<AlertEntity> entities) {
		alertRepository.save(entities);
	}

	@Override
	public Page<AlertEntity> findAll(Criteria<AlertEntity> criteria, int pageNo, int pageSize) {
		Page<AlertEntity> findAll = alertRepository.findAll(criteria,
				new PageRequest(pageNo, pageSize, new Sort(Sort.Direction.DESC, "createdTime")));
		return findAll;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void delete(String[] ids) {
		if(ids!=null &&ids.length>0){
			List<AlertEntity> findAll = alertRepository.findAll(Arrays.asList(ids));
			alertRepository.delete(findAll);
		}
	}

	@Override
	public List<AlertEntity> findAll(Criteria<AlertEntity> criteria){
		return alertRepository.findAll(criteria);
	}

	@Override
	public List<FtsDocumentAlert> findbyIds(String userId, String listString) throws TRSException {
		QueryBuilder builder = new QueryBuilder();
		builder.setPageSize(-1);
		builder.setPageNo(0);
		builder.filterField(FtsFieldConst.FIELD_USER_ID,userId, Operator.Equal);
		builder.filterField(FtsFieldConst.FIELD_ALERT_ID,listString.split(";"),Operator.Equal);
		builder.setDatabase(Const.ALERT);
		PagedList<FtsDocumentAlert> ftsDocumentAlertPagedList = hybase8SearchServiceNew.ftsAlertList(builder, FtsDocumentAlert.class);
		if (ObjectUtil.isNotEmpty(ftsDocumentAlertPagedList) && ObjectUtil.isNotEmpty(ftsDocumentAlertPagedList.getPageItems())){
			List<FtsDocumentAlert> pageItems = ftsDocumentAlertPagedList.getPageItems();
			return pageItems;
		}
		return null;
	}

	@Override
	public Page<AlertEntity> findByReceiverAndSendWayAndAlertRuleBackupsIdAndCreatedTimeBetween(String userName,
			SendWay sendWay, String id, int pageNo, int pageSize, Date start, Date end) {
		Sort sort = new Sort(Direction.DESC, "createdTime");
		Pageable pageable = new PageRequest(pageNo, pageSize, sort);
		return alertRepository.findByReceiverAndSendWayAndAlertRuleBackupsId(userName,sendWay,id, pageable);
	}

	@Override
	public List<AlertEntity> findByReceiverAndSendWayAndAlertRuleBackupsIdAndCreatedTimeBetween(String userName,
			SendWay sendWay, String id, Date start, Date end) {
		return alertRepository.findByReceiverAndSendWayAndAlertRuleBackupsIdAndCreatedTimeBetween(userName,sendWay,id,start,end);
	}

	/**
	 * 原预警代码  不关联alert_netinsight工程
	 * @param pageNo 0开始 第几页
	 * @param pageSize 一页几条
	 * @param way 发送方式
	 * @param source 来源
	 * @param time 时间
	 * @param receivers 接收者
	 * @param invitationCard  论坛主贴 0 /回帖 1
	 * @param forwarPrimary 微博 原发 primary / 转发 forward
	 * @param keywords 结果中搜索
	 * @return
	 * @throws OperationException
	 */
	@Override
	public Object alertListLocal(int pageNo,int pageSize,String way,String source,String time,String receivers,String invitationCard,
			String forwarPrimary,String keywords,String fuzzyValueScope) throws OperationException{
		User user = UserUtils.getUser();
		String userId = user.getId();
		String userName = user.getUserName();
		String[] formatTimeRange = null;
		try {
			formatTimeRange = DateUtil.formatTimeRange(time);
		} catch (OperationException e1) {
			e1.printStackTrace();
		}
		// return
		// alertService.list(formatTimeRange,pageNo,pageSize,source,receivers);
		String start = formatTimeRange[0];
		String end = formatTimeRange[1];
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Criteria<AlertEntity> criteria = new Criteria<>();
		criteria.groupBy("sid");
		criteria.add(Restrictions.eq("groupName", source.replace("微信", "国内微信")));
		if ("微博".equals(source)){
			if ("primary".equals(forwarPrimary)){
				//原发
				criteria.add(Restrictions.eq("retweetedMid", "0"));

				//criteria.add(Restrictions.in("retweetedMid",Const.PRIMARY_WEIBO_VALUE));
			}else if ("forward".equals(forwarPrimary)){
				//转发
				//criteria.add(Restrictions.ne("retweetedMid",Const.PRIMARY_WEIBO_VALUE));
                criteria.add(Restrictions.ne("retweetedMid","0"));
			}
		}else if ("国内论坛".equals(source)){
			if ("0".equals(invitationCard)){
				criteria.add(Restrictions.eq("nreserved1","0"));
			}else if ("1".equals(invitationCard)){
				criteria.add(Restrictions.eq("nreserved1","1"));
			}
		}
		Date parseStart = null;
		Date parseEnd = null;
		try {
			parseStart = sdf.parse(start);
			parseEnd = sdf.parse(end);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		criteria.add(Restrictions.between("createdTime", parseStart, parseEnd));
		if(StringUtil.isNotEmpty(keywords) && !keywords.equals("null") && StringUtil.isNotEmpty(fuzzyValueScope)){
			if("fullText".equals(fuzzyValueScope)){
				criteria.add(Restrictions.or(Restrictions.like("content", keywords,MatchMode.ANYWHERE)));
				fuzzyValueScope = "title";
			}else if("source".equals(fuzzyValueScope)){
				fuzzyValueScope = "siteName";
			}
			criteria.add(Restrictions.or(Restrictions.like(fuzzyValueScope, keywords,MatchMode.ANYWHERE)));
		}
		if ("SMS".equals(way)) {
			// 站内预警 别人发给我
			criteria.add(Restrictions.eq("receiver", userName));
			criteria.add(Restrictions.eq("sendWay", SendWay.SMS));
			if (!"ALL".equals(receivers)) {
				User name = userService.findByUserName(receivers);
				if(name!=null){
					criteria.add(Restrictions.eq("userId", name.getId()));
				}
			}
		} else {
			// 已发预警 我发给谁
			criteria.add(Restrictions.eq("userId", userId));
			if (!"ALL".equals(receivers)) {
				criteria.add(Restrictions.eq("receiver", receivers));
			}
		}
		Page<AlertEntity> findAll = findAll(criteria, pageNo, pageSize);
		if(findAll!=null){
			List<AlertEntity> content = findAll.getContent();
			if(content!=null &&content.size()>0){
				for (AlertEntity alertEntity : content) {
					String content2 = alertEntity.getContent();
					if(StringUtils.isNotBlank(content2) && content2.length()>150){
						alertEntity.setContent(content2.substring(0, 150));
					}
					//根据userid和sid查收藏状态
					//原生sql
					String sid = alertEntity.getSid();
					Specification<Favourites> criteriaFav = new Specification<Favourites>() {

						@Override
						public Predicate toPredicate(Root<Favourites> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
							List<Object> predicates = new ArrayList<>();
							predicates.add(cb.equal(root.get("userId"),userId));
							predicates.add(cb.isNull(root.get("libraryId")));
							predicates.add(cb.equal(root.get("sid"), sid));
							Predicate[] pre = new Predicate[predicates.size()];

							return query.where(predicates.toArray(pre)).getRestriction();
						}
					};

					Favourites favourites = favouritesRepository.findOne(criteriaFav);
					//Favourites favourites = favouritesRepository.findByUserIdAndSid(userId, alertEntity.getSid());
					if(favourites!=null){
						alertEntity.setFavourite(true);
					}else{
						alertEntity.setFavourite(false);
					}
				}
			}
		}
		String uuid = UUID.randomUUID().toString();
		try {
			log.error("开始转换map"+DateUtil.formatCurrentTime(DateUtil.yyyyMMdd));
			Map<String, Object> putValue = MapUtil.putValue(new String[] { "pageId", "list" }, uuid, findAll);
			log.error("返回："+DateUtil.formatCurrentTime(DateUtil.yyyyMMdd));
			if(findAll!=null && (findAll.getContent()==null || findAll.getContent().size()==0)){
				return null;
			}
			return putValue;
		} catch (OperationException e) {
			throw new OperationException("预警已发送/站内查询失败,message:" + e,e);
		}
//		return null;
	}

	/**
	 * 关联alert_netinsight工程
	 * @param pageNo 0开始 第几页
	 * @param pageSize 一页几条
	 * @param way 发送方式
	 * @param source 来源
	 * @param time 时间
	 * @param receivers 接收者
	 * @param invitationCard  论坛主贴 0 /回帖 1
	 * @param forwarPrimary 微博 原发 primary / 转发 forward
	 * @param keywords 结果中搜索
	 * @return
	 * @throws OperationException
	 */
	@Override
	public Object alertListHttp(int pageNo, int pageSize, String way, String source, String time, String receivers, String invitationCard,
								String forwarPrimary, String keywords, String fuzzyValueScope) throws OperationException {
		User user = UserUtils.getUser();
		String userId = user.getId();
		String userName = user.getUserName();
		log.error("开始转换map" + DateUtil.formatCurrentTime(DateUtil.yyyyMMdd));
		String url = alertNetinsightUrl + "/alert/list?pageNo=" + pageNo + "&pageSize=" + pageSize + "&way=" + way + "&source=" + source + "&time="
				+ time + "&receivers=" + receivers + "&invitationCard=" + invitationCard + "&forwarPrimary=" + forwarPrimary + "&keywords="
				+ keywords + "&fuzzyValueScope=" + fuzzyValueScope + "&userId=" + userId + "&userName=" + userName;
		String doGet = HttpUtil.doGet(url, null);
		if (StringUtil.isEmpty(doGet)) {
			return null;
		} else if (doGet.contains("\"code\":500")) {
			Map<String, String> map = (Map<String, String>) JSON.parse(doGet);
			String message = map.get("message");
			throw new OperationException("预警弹窗获取失败,message:" + message, new Exception());
		}
		Map<String, Object> map = (Map<String, Object>) JSON.parse(CommonListChartUtil.StringShowGroupName(doGet));
		log.error("返回：" + DateUtil.formatCurrentTime(DateUtil.yyyyMMdd));
		return map;
	}
	@Override
	public PageAlert alertListHybase(int pageNo,int pageSize,String way,String source,String time,String receivers,String invitationCard,
									 String forwarPrimary,String keywords,String fuzzyValueScope) throws TRSException {
		User user = UserUtils.getUser();
		String userId = user.getId();
		String userName = user.getUserName();
		List<Favourites> favouritesList = favouritesService.findAll(user);
		QueryBuilder queryBuilder = new QueryBuilder();
		queryBuilder.filterField(FtsFieldConst.FIELD_USER_ID,userId,Operator.Equal);

		if ("SMS".equals(way)){ // 站内预警
			queryBuilder.filterField(FtsFieldConst.FIELD_SEND_WAY_LIST,SendWay.SMS.toString(), Operator.Equal);
			queryBuilder.filterField(FtsFieldConst.FIELD_SEND_RECEIVE,"receive",Operator.Equal);
			queryBuilder.filterField(FtsFieldConst.FIELD_RECEIVER_LIST,userName,Operator.Equal);
		}else {
			if (!"ALL".equals(receivers)) {
				List<AlertAccount> alertAccounts = alertAccountRepository.findByUserIdAndName(userId,receivers);
				if(alertAccounts.size()>0){
					receivers = alertAccounts.get(0).getAccount();
				}
				queryBuilder.filterField(FtsFieldConst.FIELD_RECEIVER_LIST,receivers,Operator.Equal);
			}
			queryBuilder.filterField(FtsFieldConst.FIELD_SEND_RECEIVE,"send",Operator.Equal);
		}

		if (!"ALL".equals(source)){
			queryBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,source.split(";"),Operator.Equal);
		}
//		时间  发预警的时间  这里应该是loadTime
//		queryBuilder.filterField(FtsFieldConst.FIELD_LOADTIME,DateUtil.formatTimeRange(time),Operator.Between);
		//时间 这里按需求改为urlTime
		queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME,DateUtil.formatTimeRange(time),Operator.Between);
		queryBuilder.orderBy(FtsFieldConst.FIELD_LOADTIME, true);

		if ("微博".equals(source)){
			if ("primary".equals(forwarPrimary)){
				//原发
				queryBuilder.filterField(FtsFieldConst.FIELD_RETWEETED_MID,"0 OR \"\"",Operator.Equal);//IR_RETWEETED_MID这个字段为空就是原发
			}else if ("forward".equals(forwarPrimary)){
				//转发
				queryBuilder.filterField(FtsFieldConst.FIELD_RETWEETED_MID,"0 OR \"\"",Operator.NotEqual);//IR_RETWEETED_MID这个字段为空就是原发
			}
		}else if ("国内论坛".equals(source)){
			if ("0".equals(invitationCard)){// 主贴
				//		主贴、回帖
				queryBuilder.filterField(FtsFieldConst.FIELD_NRESERVED1,"0 OR \"\"",Operator.Equal);
			}else if ("1".equals(invitationCard)){
				queryBuilder.filterField(FtsFieldConst.FIELD_NRESERVED1,"1",Operator.Equal);
			}
		}
		if (StringUtil.isNotEmpty(keywords) && StringUtil.isNotEmpty(fuzzyValueScope)){
//			String[] split = keywords.split(",");
			String[] split = keywords.split("\\s+|,");
			String splitNode = "";
			for (int i = 0; i < split.length; i++) {
				if (StringUtil.isNotEmpty(split[i])) {
					splitNode += split[i] + ",";
				}
			}
			keywords = splitNode.substring(0, splitNode.length() - 1);
			if (keywords.endsWith(";") || keywords.endsWith(",") || keywords.endsWith("；")
					|| keywords.endsWith("，")) {
				keywords = keywords.substring(0, keywords.length() - 1);

			}
			StringBuilder fuzzyBuilder  = new StringBuilder();
			String hybaseField = "fullText";
			switch (fuzzyValueScope){
				case "title":
					hybaseField = FtsFieldConst.FIELD_URLTITLE;
					break;
				case "source":
					hybaseField = FtsFieldConst.FIELD_SITENAME_LIKE;
					break;
				case "author":
					hybaseField = FtsFieldConst.FIELD_AUTHORS_LIKE;
					break;
			}
			if("fullText".equals(hybaseField)){
				fuzzyBuilder.append(FtsFieldConst.FIELD_TITLE).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))").append(" OR "+FtsFieldConst.FIELD_CONTENT).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))");
			}else {
				fuzzyBuilder.append(hybaseField).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))");
			}
			queryBuilder.filterByTRSL(fuzzyBuilder.toString());

		}
		queryBuilder.setPageNo(pageNo);
		queryBuilder.setPageSize(pageSize);
		queryBuilder.setDatabase(Const.ALERT);
		PagedList<FtsDocumentAlert> ftsDocumentAlertPagedList = hybase8SearchServiceNew.ftsAlertList(queryBuilder, FtsDocumentAlert.class);
		if (ObjectUtil.isNotEmpty(ftsDocumentAlertPagedList) && ObjectUtil.isNotEmpty(ftsDocumentAlertPagedList.getPageItems())
				&& ftsDocumentAlertPagedList.getPageItems().size() > 0) {
			List<FtsDocumentAlert> pageItems = ftsDocumentAlertPagedList.getPageItems();
			for (FtsDocumentAlert pageItem : pageItems) {
				if (ObjectUtil.isNotEmpty(pageItem.getContent())) {
					pageItem.setContent(pageItem.getContent().replaceAll("&lt;", "<").replaceAll("&nbsp;", " ").replaceAll("&gt;", ">"));
				}
				if (ObjectUtil.isNotEmpty(pageItem.getAppraise())) {
					pageItem.setAppraise(pageItem.getAppraise().replace("[","").replace("]", "").replace("\"",""));
				}
				// 检验收藏
				pageItem.setFavourite(favouritesList.stream().anyMatch(sid -> sid.getSid().equals(pageItem.getSid())));
				pageItem.setTitle(pageItem.getTitle().replaceAll("&lt;", "<").replaceAll("&nbsp;", " ").replaceAll("&gt;", ">"));
				//pageItem.setTitle(pageItem.getTitle().replaceAll("&lt;", "<").replaceAll("&nbsp;", " ").replaceAll("&gt;", ">").replaceAll("<font color=red>", "").replaceAll("<font color='red'>", "").replaceAll("</font>", ""));
				pageItem.setTitleWhole(pageItem.getTitleWhole().replaceAll("&lt;", "<").replaceAll("&nbsp;", " ").replaceAll("&gt;", ">"));
				pageItem.setGroupName(CommonListChartUtil.formatPageShowGroupName(pageItem.getGroupName()));
				String copyTitle = pageItem.getTitleWhole();
				if(StringUtil.isNotEmpty(copyTitle)){
					copyTitle = copyTitle.replaceAll("<font color=red>", "").replaceAll("</font>", "");
					pageItem.setCopyTitle(copyTitle);
				}
				List<String> sendWays = Arrays.asList(pageItem.getSendWay().split(";"));
				List<String> receiver = Arrays.asList(pageItem.getReceiver().split(";"));

				for(int i=0;i<sendWays.size();i++){
					if("WE_CHAT".equals(sendWays.get(i))){
						String account = receiver.get(i);
						List<AlertAccount> alertAccount =  alertAccountRepository.findByAccount(account);
						if(alertAccount.size()>0){
							receiver.set(i,alertAccount.get(0).getName());
						}
					}
				}
              pageItem.setReceiver(org.apache.commons.lang.StringUtils.join(receiver.toArray(), ";"));
			}
			PageAlert pageAlert = new PageAlert();
			pageAlert.setContent(ftsDocumentAlertPagedList.getPageItems());
			pageAlert.setFirst(pageNo == 0 ? true : false);
			pageAlert.setLast(pageNo == ftsDocumentAlertPagedList.getTotalPageCount() - 1 ? true : false);//pageno从0开始
			pageAlert.setPageId(UUID.randomUUID().toString());
			pageAlert.setSize(pageSize);
			pageAlert.setTotalPages(ftsDocumentAlertPagedList.getTotalPageCount());
			pageAlert.setTotalElements(ftsDocumentAlertPagedList.getTotalItemCount());
			pageAlert.setNumber(pageNo);
			return pageAlert;
		}
		return null;
	}

	@Override
	public PageAlert alertListHybaseNew(int pageNo, int pageSize, String way, String source, String time, String receivers, String invitationCard, String forwarPrimary, String keywords, String sort, String fuzzyValueScope) throws TRSException {
		User user = UserUtils.getUser();
		String userId = user.getId();
		String userName = user.getUserName();
		List<Favourites> favouritesList = favouritesService.findAll(user);
		QueryBuilder queryBuilder = new QueryBuilder();
		queryBuilder.filterField(FtsFieldConst.FIELD_USER_ID,userId,Operator.Equal);

		if ("SMS".equals(way)){ // 站内预警
			queryBuilder.filterField(FtsFieldConst.FIELD_SEND_WAY_LIST,SendWay.SMS.toString(), Operator.Equal);
			queryBuilder.filterField(FtsFieldConst.FIELD_SEND_RECEIVE,"receive",Operator.Equal);
			queryBuilder.filterField(FtsFieldConst.FIELD_RECEIVER_LIST,userName,Operator.Equal);
		}else {
			if (!"ALL".equals(receivers)) {
				List<AlertAccount> alertAccounts = alertAccountRepository.findByUserIdAndName(userId,receivers);
				if(alertAccounts.size()>0){
					receivers = alertAccounts.get(0).getAccount();
				}
				queryBuilder.filterField(FtsFieldConst.FIELD_RECEIVER_LIST,receivers,Operator.Equal);
			}
			queryBuilder.filterField(FtsFieldConst.FIELD_SEND_RECEIVE,"send",Operator.Equal);
		}

		if (!"ALL".equals(source)){
			queryBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,source.split(";"),Operator.Equal);
		}
//		时间  发预警的时间  这里应该是loadTime
//		queryBuilder.filterField(FtsFieldConst.FIELD_LOADTIME,DateUtil.formatTimeRange(time),Operator.Between);
		//时间 这里按需求改为urlTime
//		queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME,DateUtil.formatTimeRange(time),Operator.Between);
		if("urltime".equals(sort)){
			queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME,DateUtil.formatTimeRange(time),Operator.Between);
			queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
		}else{
			queryBuilder.filterField(FtsFieldConst.FIELD_LOADTIME,DateUtil.formatTimeRange(time),Operator.Between);
			queryBuilder.orderBy(FtsFieldConst.FIELD_LOADTIME, true);
		}

		if ("微博".equals(source)){
			if ("primary".equals(forwarPrimary)){
				//原发
				queryBuilder.filterField(FtsFieldConst.FIELD_RETWEETED_MID,"0 OR \"\"",Operator.Equal);//IR_RETWEETED_MID这个字段为空就是原发
			}else if ("forward".equals(forwarPrimary)){
				//转发
				queryBuilder.filterField(FtsFieldConst.FIELD_RETWEETED_MID,"0 OR \"\"",Operator.NotEqual);//IR_RETWEETED_MID这个字段为空就是原发
			}
		}else if ("国内论坛".equals(source)){
			if ("0".equals(invitationCard)){// 主贴
				//		主贴、回帖
				queryBuilder.filterField(FtsFieldConst.FIELD_NRESERVED1,"0 OR \"\"",Operator.Equal);
			}else if ("1".equals(invitationCard)){
				queryBuilder.filterField(FtsFieldConst.FIELD_NRESERVED1,"1",Operator.Equal);
			}
		}
		if (StringUtil.isNotEmpty(keywords) && StringUtil.isNotEmpty(fuzzyValueScope)){
//			String[] split = keywords.split(",");
			String[] split = keywords.split("\\s+|,");
			String splitNode = "";
			for (int i = 0; i < split.length; i++) {
				if (StringUtil.isNotEmpty(split[i])) {
					splitNode += split[i] + ",";
				}
			}
			keywords = splitNode.substring(0, splitNode.length() - 1);
			if (keywords.endsWith(";") || keywords.endsWith(",") || keywords.endsWith("；")
					|| keywords.endsWith("，")) {
				keywords = keywords.substring(0, keywords.length() - 1);

			}
			StringBuilder fuzzyBuilder  = new StringBuilder();
			String hybaseField = "fullText";
			switch (fuzzyValueScope){
				case "title":
					hybaseField = FtsFieldConst.FIELD_URLTITLE;
					break;
				case "source":
					hybaseField = FtsFieldConst.FIELD_SITENAME_LIKE;
					break;
				case "author":
					hybaseField = FtsFieldConst.FIELD_AUTHORS_LIKE;
					break;
			}
			if("fullText".equals(hybaseField)){
				fuzzyBuilder.append(FtsFieldConst.FIELD_TITLE).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))").append(" OR "+FtsFieldConst.FIELD_CONTENT).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))").append(" OR "+FtsFieldConst.FIELD_URLTITLE_NEW).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))").append(" OR "+FtsFieldConst.FIELD_CONTENT_NEW).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))");
			}else {
				fuzzyBuilder.append(hybaseField).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))");
			}
			queryBuilder.filterByTRSL(fuzzyBuilder.toString());

		}
		queryBuilder.setPageNo(pageNo);
		queryBuilder.setPageSize(pageSize);
		queryBuilder.setDatabase(Const.ALERT);
		PagedList<FtsDocumentAlert> ftsDocumentAlertPagedList = hybase8SearchServiceNew.ftsAlertList(queryBuilder, FtsDocumentAlert.class);
		if (ObjectUtil.isNotEmpty(ftsDocumentAlertPagedList) && ObjectUtil.isNotEmpty(ftsDocumentAlertPagedList.getPageItems())
				&& ftsDocumentAlertPagedList.getPageItems().size() > 0) {
			List<FtsDocumentAlert> pageItems = ftsDocumentAlertPagedList.getPageItems();
			for (FtsDocumentAlert pageItem : pageItems) {
				if (ObjectUtil.isNotEmpty(pageItem.getContent())) {
					pageItem.setContent(pageItem.getContent().replaceAll("&lt;", "<").replaceAll("&nbsp;", " ").replaceAll("&gt;", ">"));
				}
				if (ObjectUtil.isNotEmpty(pageItem.getAppraise())) {
					pageItem.setAppraise(pageItem.getAppraise().replace("[","").replace("]", "").replace("\"",""));
				}
				// 检验收藏
				pageItem.setFavourite(favouritesList.stream().anyMatch(sid -> sid.getSid().equals(pageItem.getSid())));
				pageItem.setTitle(pageItem.getTitle().replaceAll("&lt;", "<").replaceAll("&nbsp;", " ").replaceAll("&gt;", ">"));
				//pageItem.setTitle(pageItem.getTitle().replaceAll("&lt;", "<").replaceAll("&nbsp;", " ").replaceAll("&gt;", ">").replaceAll("<font color=red>", "").replaceAll("<font color='red'>", "").replaceAll("</font>", ""));
				pageItem.setTitleWhole(pageItem.getTitleWhole().replaceAll("&lt;", "<").replaceAll("&nbsp;", " ").replaceAll("&gt;", ">"));
				pageItem.setGroupName(CommonListChartUtil.formatPageShowGroupName(pageItem.getGroupName()));
				String copyTitle = pageItem.getTitleWhole();
				if(StringUtil.isNotEmpty(copyTitle)){
					copyTitle = copyTitle.replaceAll("<font color=red>", "").replaceAll("</font>", "");
					pageItem.setCopyTitle(copyTitle);
				}
				List<String> sendWays = Arrays.asList(pageItem.getSendWay().split(";"));
				List<String> receiver = Arrays.asList(pageItem.getReceiver().split(";"));

				for(int i=0;i<sendWays.size();i++){
					if("WE_CHAT".equals(sendWays.get(i))){
						String account = receiver.get(i);
						List<AlertAccount> alertAccount =  alertAccountRepository.findByAccount(account);
						if(alertAccount.size()>0){
							receiver.set(i,alertAccount.get(0).getName());
						}
					}
				}
				pageItem.setReceiver(org.apache.commons.lang.StringUtils.join(receiver.toArray(), ";"));
				if (Const.MEDIA_TYPE_VIDEO.contains(pageItem.getGroupName()) || Const.MEDIA_TYPE_ZIMEITI_BOKE.contains(pageItem.getGroupName())) {
					pageItem.setSiteName(StringUtil.isNotEmpty(pageItem.getScreenName()) && !"undefined".equals(pageItem.getScreenName()) ? pageItem.getSiteName() + "-" + pageItem.getScreenName() : pageItem.getSiteName());
				}
			}
			PageAlert pageAlert = new PageAlert();
			pageAlert.setContent(ftsDocumentAlertPagedList.getPageItems());
			pageAlert.setFirst(pageNo == 0 ? true : false);
			pageAlert.setLast(pageNo == ftsDocumentAlertPagedList.getTotalPageCount() - 1 ? true : false);//pageno从0开始
			pageAlert.setPageId(UUID.randomUUID().toString());
			pageAlert.setSize(pageSize);
			pageAlert.setTotalPages(ftsDocumentAlertPagedList.getTotalPageCount());
			pageAlert.setTotalElements(ftsDocumentAlertPagedList.getTotalItemCount());
			pageAlert.setNumber(pageNo);
			return pageAlert;
		}
		return null;
	}


	/**
	 * 站内预警 查别人发给我的站内预警  同时返回收藏和预警状态
	 * @throws OperationException
	 */
	@Override
	public Object findSMS(String userId, Criteria<AlertEntity> criteria, int pageNo, int pageSize) throws OperationException {

		Page<AlertEntity> findAll = findAll(criteria, pageNo, pageSize);
		if(findAll!=null){
			List<AlertEntity> content = findAll.getContent();
			if(content!=null &&content.size()>0){
				List<String> sidList = new ArrayList<>();
				for (AlertEntity alertEntity : content) {
					String content2 = alertEntity.getContent();
					if(StringUtils.isNotBlank(content2) && content2.length()>150){
						alertEntity.setContent(content2.substring(0, 150));
					}
					//查看这篇文章是否收藏
					sidList.add(alertEntity.getSid());
				}
				List<String> sidAlert = new ArrayList<>();
				List<AlertEntity> alertList = alertRepository.findByUserIdAndSidIn(userId,sidList);

				for(AlertEntity alert : alertList){
					sidAlert.add(alert.getSid());
				}
				for (AlertEntity alertEntity : content) {
					alertEntity.setSend(sidAlert.indexOf(alertEntity.getSid())<0?false:true);
				}
			}
		}
		String uuid = UUID.randomUUID().toString();
		try {
			log.error("开始转换map"+DateUtil.formatCurrentTime(DateUtil.yyyyMMdd));
			Map<String, Object> putValue = MapUtil.putValue(new String[] { "pageId", "list" }, uuid, findAll);
			log.error("返回："+DateUtil.formatCurrentTime(DateUtil.yyyyMMdd));
			if(ObjectUtil.isNotEmpty(findAll) && ObjectUtil.isEmpty(findAll.getContent())){
				return null;
			}
			return putValue;
		} catch (OperationException e) {
			throw new OperationException("预警已发送/站内查询失败,message:" + e,e);
		}
	}

	/**
	 * 原预警删除  本地不用启动alert_netinsight
	 * @param id 要删除的id 多个时以分号分割
	 * @return
	 * @throws OperationException
	 */
	@Override
	public Object deleteLocal(String id) throws OperationException{
		try {
			String[] ids = id.split(";");
			delete(ids);
			return "删除预警成功！";
		} catch (Exception e) {
			throw new OperationException("预警删除失败,message:" + e,e);
		}
	}

	/**
	 * 关联alert_netinsight的删除预警
	 * @param id 要删除的id 多个时以分号分割
	 * @return
	 * @throws OperationException
	 */
	@Override
	public Object deleteHttp(String id) throws OperationException{
		if (StringUtil.isNotEmpty(id)){
			try {
                hybase8SearchServiceNew.delete(Const.ALERT,id.split(";"));
			} catch (TRSException e) {
				throw new OperationException("预警删除失败,message:" + e,e);
			}
			return "success";
		}
		return null;
	}

	@Override
	public List<AlertEntity> findByReceiverAndSendWayAndCreatedTimeBetween(String userName, SendWay sms,
			Date parseStart, Date parseEnd) {
		return alertRepository.findByReceiverAndSendWayAndCreatedTimeBetween(userName, sms,  parseStart, parseEnd);
	}
	/**
	 * 保存需要拉取的预警信息
	 * @param alertWebSocket 正常情况存数据，用完后删，保证每次都是最新数据
	 */
	@Override
	public void saveReceiveAlert(AlertWebSocket alertWebSocket) {
		alertWebSocketRepository.save(alertWebSocket);
	}

	/**
	 * 连接后主动拉取的预警信息
	 * @param receiveid 连接WebSocket用户的id
	 */
	@Override
	public List<AlertWebSocket> findReceiveAlert(String receiveid) {
		return alertWebSocketRepository.selectAlertWebSocket(receiveid);
	}
	/**
	 * 主动推送删除预警信息
	 * @param receiveid 连接WebSocket用户的id
	 */
	@Override
	public void deleteReceiveAlert(String receiveid) {
		alertWebSocketRepository.deleteAlertWebSocketByReceiveid(receiveid);
	}

}
