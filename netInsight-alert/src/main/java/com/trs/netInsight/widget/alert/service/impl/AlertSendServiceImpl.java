package com.trs.netInsight.widget.alert.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocumentAlert;
import com.trs.netInsight.support.fts.entity.FtsDocumentAlertType;
import com.trs.netInsight.util.HttpUtil;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.alert.entity.AlertEntity;
import com.trs.netInsight.widget.alert.entity.AlertSend;
import com.trs.netInsight.widget.alert.entity.PageAlertSend;
import com.trs.netInsight.widget.alert.entity.enums.SendWay;
import com.trs.netInsight.widget.alert.entity.repository.AlertSendRepository;
import com.trs.netInsight.widget.alert.service.IAlertSendService;
import com.trs.netInsight.widget.alert.service.IAlertService;
import com.trs.netInsight.widget.report.entity.Favourites;
import com.trs.netInsight.widget.report.entity.repository.FavouritesRepository;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.util.*;

/**
 * app(目前针对app)预警推送提示业务层接口实现类
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/4/22 17:29.
 * @desc
 */
@Service
public class AlertSendServiceImpl implements IAlertSendService {
    @Value("${http.client}")
    private boolean httpClient;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AlertSendRepository alertSendRepository;
    @Autowired
    private FavouritesRepository favouritesRepository;
    @Autowired
    private FullTextSearch hybase8SearchServiceNew;
    @Autowired
    private IAlertService alertService;

    @Value("${http.alert.netinsight.url}")
    private String alertNetinsightUrl;
    @Override
    public AlertSend add(AlertSend alertSend) throws OperationException {
        return null;

    }

    @Override
    public void delete(AlertSend alertSend) {

    }

    @Override
    public PageAlertSend findOne(String id,String userId) throws TRSException {
        PageAlertSend pageAlertSend = new PageAlertSend();


        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.filterField(FtsFieldConst.FIELD_SEND_WAY, SendWay.APP.toString(), Operator.Equal);
        queryBuilder.filterField(FtsFieldConst.FIELD_USER_ID,userId,Operator.Equal);
        queryBuilder.filterField(FtsFieldConst.FIELD_ALERT_TYPE_ID,id,Operator.Equal);
        queryBuilder.setDatabase(Const.ALERTTYPE);
        queryBuilder.page(0,1);
        PagedList<FtsDocumentAlertType> pagedList = hybase8SearchServiceNew.ftsAlertList(queryBuilder, FtsDocumentAlertType.class);
        FtsDocumentAlertType ftsDocumentAlertType = null;
        if (ObjectUtil.isNotEmpty(pagedList) && ObjectUtil.isNotEmpty(pagedList.getPageItems().get(0))){
            ftsDocumentAlertType = pagedList.getPageItems().get(0);

            Date alertTime = com.trs.netInsight.util.DateUtil.stringToDate(ftsDocumentAlertType.getAlertTime(), com.trs.netInsight.util.DateUtil.yyyyMMdd);
            ftsDocumentAlertType.setAlertTime(String.valueOf(alertTime.getTime()));
            String pushUserId = ftsDocumentAlertType.getUserId();
            User user = userRepository.findOne(pushUserId);
            if (ObjectUtil.isNotEmpty(user)){
                ftsDocumentAlertType.setPushHuman(user.getUserName());
            }
            // 选择查询的库
            List<Map<String, Object>> listMap = new ArrayList<>();

            String alertIds = ftsDocumentAlertType.getIds();
            String createdUserId = ftsDocumentAlertType.getUserId();

            List<FtsDocumentAlert> ftsDocumentAlerts = alertService.findbyIds(createdUserId, alertIds);
            if(ObjectUtil.isEmpty(ftsDocumentAlerts)){
                ftsDocumentAlertType.setAlertData(null);
            }else {
                //收藏
                List<String> sidFavourite = new ArrayList<>();
                //原生sql
                Specification<Favourites> criteria = new Specification<Favourites>() {

                    @Override
                    public Predicate toPredicate(Root<Favourites> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                        List<Object> predicates = new ArrayList<>();
                        predicates.add(cb.equal(root.get("userId"),userId));
                        predicates.add(cb.isNull(root.get("libraryId")));
                        Predicate[] pre = new Predicate[predicates.size()];

                        return query.where(predicates.toArray(pre)).getRestriction();
                    }
                };
                List<Favourites> favouritesList = favouritesRepository.findAll(criteria, new Sort(Sort.Direction.DESC, "urltime"));

                if(ObjectUtil.isNotEmpty(favouritesList)){
                    for (Favourites faSidList : favouritesList) {
                        //sidFavourite装载了所有已收藏的文章sid
                        sidFavourite.add(faSidList.getSid());
                    }
                }
                for (FtsDocumentAlert alertEntity : ftsDocumentAlerts) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("title", StringUtil.replaceImg(alertEntity.getTitle()));
                    Date time = alertEntity.getTime();
                    map.put("urlTime", time.getTime());
                    String groupName = alertEntity.getGroupName();
                    String siteName = alertEntity.getSiteName();
                    if (Const.MEDIA_TYPE_WEIBO.contains(groupName) || Const.MEDIA_TYPE_TF.contains(groupName)){
                        siteName = alertEntity.getScreenName();
                    }else if (StringUtil.isEmpty(siteName)){
                        siteName = groupName;
                    }
                    map.put("siteName", siteName);
                    map.put("urlName", alertEntity.getUrlName());
                    String sid = alertEntity.getSid();
                    map.put("sid", sid);
                    map.put("groupName",alertEntity.getGroupName());
                    map.put("nreserved1",alertEntity.getNreserved1());
                    map.put("md5Tag",alertEntity.getMd5tag());
                    map.put("imageUrl",alertEntity.getImageUrl());
                    //是否收藏信息
                    int indexOfFa = sidFavourite.indexOf(sid);
                    if (indexOfFa < 0) {
                        map.put("favourite",false);
                    } else {
                        map.put("favourite",true);
                    }

                    map.put("time",time.getTime());
                    listMap.add(map);
                }
                ftsDocumentAlertType.setAlertData(listMap);
            }
        }
        ArrayList<FtsDocumentAlertType> alertSends = new ArrayList<>();
        alertSends.add(ftsDocumentAlertType);
        pageAlertSend.setContent(alertSends);
        return pageAlertSend;
    }

    @Override
    public PageAlertSend findByUserIdAndSendType(int pageNo, int pageSize, String userId, String sendType) {
        return null;
    }

    /**
     * post请求前实体转Map
     * @param sendAlert
     * @return
     */
    private Map<String,String> alertSendToMap(AlertSend sendAlert){
        User user = UserUtils.getUser();
        Map<String,String> map = new HashMap<String,String>();
        map.put("id", sendAlert.getId());
        map.put("organizationId", user.getOrganizationId());
        map.put("userId", user.getId());
        map.put("userAccount", user.getUserName());
        map.put("createdUserId", sendAlert.getCreatedUserId());
        map.put("ids", sendAlert.getIds());
        map.put("ruleName", sendAlert.getRuleName());
        map.put("alertTime", sendAlert.getAlertTime());
        map.put("size", String.valueOf(sendAlert.getSize()));
        map.put("alertSource",String.valueOf(sendAlert.getAlertSource()));
        map.put("recevierId",sendAlert.getRecevierId());
        return map;
    }
}
