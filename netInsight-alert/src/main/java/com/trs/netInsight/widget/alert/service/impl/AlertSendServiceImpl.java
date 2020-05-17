package com.trs.netInsight.widget.alert.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.util.HttpUtil;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.alert.entity.AlertEntity;
import com.trs.netInsight.widget.alert.entity.AlertSend;
import com.trs.netInsight.widget.alert.entity.PageAlertSend;
import com.trs.netInsight.widget.alert.entity.repository.AlertSendRepository;
import com.trs.netInsight.widget.alert.service.IAlertSendService;
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

    @Value("${http.alert.netinsight.url}")
    private String alertNetinsightUrl;
    @Override
    public AlertSend add(AlertSend alertSend) throws OperationException {
        if (httpClient){
            String url = alertNetinsightUrl +"/alertSend/add";
            String doPost = HttpUtil.doPost(url, alertSendToMap(alertSend), "utf-8");
            ObjectMapper om = new ObjectMapper();
            AlertSend readValue = null;
            try {
                //json转实体
                readValue = om.readValue(doPost, AlertSend.class);
            } catch (IOException e) {
                throw new OperationException("app结果保存报错", e);
            }
            return readValue;
        }else {
            return alertSendRepository.save(alertSend);
        }
    }

    @Override
    public void delete(AlertSend alertSend) {
        if(httpClient){
            String url = alertNetinsightUrl+"/alertSend/delete";
            String doPost = HttpUtil.doPost(url, alertSendToMap(alertSend), "utf-8");
        }else{
            alertSendRepository.delete(alertSend);
        }
    }

    @Override
    public PageAlertSend findOne(String id,String userId) throws OperationException {
        PageAlertSend pageAlertSend = new PageAlertSend();
        AlertSend alertSend = null;;
        if(httpClient){
            String url = alertNetinsightUrl+"/alertSend/findOne?id="+id+"&userId="+userId;
            String doGet = HttpUtil.doGet(url, null);
            ObjectMapper om = new ObjectMapper();
            try {
                if (StringUtil.isNotEmpty(doGet)){
                    //json转实体
                    alertSend = om.readValue(doGet, AlertSend.class);
                }
            } catch (IOException e) {
                throw new OperationException("app预警查找报错", e);
            }
        }else{
            alertSend =  alertSendRepository.findOne(id);
        }

        if (alertSend != null) {
            Date alertTime = com.trs.netInsight.util.DateUtil.stringToDate(alertSend.getAlertTime(), com.trs.netInsight.util.DateUtil.yyyyMMdd);
            alertSend.setAlertTime(String.valueOf(alertTime.getTime()));
            String pushUserId = alertSend.getCreatedUserId();
            User user = userRepository.findOne(pushUserId);
            if (ObjectUtil.isNotEmpty(user)){
                alertSend.setPushHuman(user.getUserName());
            }
            // 选择查询的库
            List<Map<String, Object>> listMap = new ArrayList<>();

            String sids = alertSend.getIds();
            String createdUserId = alertSend.getCreatedUserId();
            String url = alertNetinsightUrl+"/alert/getAlertByUserIdAndId";
            String doPost = HttpUtil.sendPost(url, "userId="+createdUserId+"&ids="+sids);
            if(StringUtil.isEmpty(doPost)){
                alertSend.setAlertData(null);
            }else {
                //json转list
                List<AlertEntity> list = JSONArray.parseObject(doPost, new TypeReference<ArrayList<AlertEntity>>() {});
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
                if (ObjectUtil.isNotEmpty(list)){
                    for (AlertEntity alertEntity : list) {
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
                    alertSend.setAlertData(listMap);
                }else {
                    alertSend.setAlertData(null);
                }
            }
        }
        ArrayList<AlertSend> alertSends = new ArrayList<>();
        alertSends.add(alertSend);
        pageAlertSend.setContent(alertSends);
        return pageAlertSend;
    }

    @Override
    public PageAlertSend findByUserIdAndSendType(int pageNo, int pageSize, String userId, String sendType) {
        //把原本存储在criteria的查询条件存在Map里 不然没法从criteria取条件


        PageAlertSend allJsonFile = null;
        String url = alertNetinsightUrl+"/alertSend/findAllJsonFile?sendType="+sendType+"&userId="+userId+"&pageNo="+pageNo+"&pageSize="+pageSize;
        String doGet = HttpUtil.doGet(url, null);
        ObjectMapper om = new ObjectMapper();
        try {
            if (StringUtil.isNotEmpty(doGet)){
                //json转实体
                allJsonFile = om.readValue(doGet, PageAlertSend.class);
            }
        } catch (IOException e) {

        }
        return allJsonFile;
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
