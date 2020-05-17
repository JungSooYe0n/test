package com.trs.netInsight.support.appApi.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.appApi.entity.AppApiAccessToken;
import com.trs.netInsight.support.appApi.entity.repository.IAppAccessTokenRepository;
import com.trs.netInsight.support.appApi.result.ApiCommonResult;
import com.trs.netInsight.support.appApi.result.ApiResultType;
import com.trs.netInsight.support.appApi.service.IApiService;
import com.trs.netInsight.support.appApi.service.IOAuthService;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.UserHelp;
import com.trs.netInsight.widget.alert.entity.AlertEntity;
import com.trs.netInsight.widget.alert.entity.AlertSend;
import com.trs.netInsight.widget.alert.entity.PageAlertSend;
import com.trs.netInsight.widget.alert.service.IAlertSendService;
import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.column.service.IIndexPageService;
import com.trs.netInsight.widget.column.service.IIndexTabMapperService;
import com.trs.netInsight.widget.report.entity.Favourites;
import com.trs.netInsight.widget.report.entity.repository.FavouritesRepository;
import com.trs.netInsight.widget.special.entity.InfoListResult;
import com.trs.netInsight.widget.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * API业务层接口实现类
 * @author 北京拓尔思信息技术股份有限公司
 * Created by duhq on 2019/4/12
 * @desc
 */
@Service
@Slf4j
public class AppApiServiceImpl implements IApiService {
    @Autowired
    private UserHelp userService;
    @Autowired
    private IAppAccessTokenRepository appAccessTokenRepository;
    @Autowired
    private IOAuthService authService;
    @Autowired
    private IIndexTabMapperService indexTabMapperService;
    @Autowired
    private IIndexPageService indexPageService;
    @Autowired
    private FavouritesRepository favouritesRepository;
    @Autowired
    private FullTextSearch hybase8SearchService;
    @Autowired
    private IAlertSendService alertSendService;

    @Value("${http.alert.netinsight.url}")
    private String alertNetinsightUrl;

    @Override
    public Object loginAndGetToken(String userAccount, String passWord) throws TRSException{
        User user = userService.findByUserName(userAccount);
        ApiCommonResult result = null;
        if (user != null){
            if(passWord != null && passWord != ""){
                String enPas = UserUtils.getEncryptPsw(passWord,user.getSalt());
                if (enPas.equals(user.getPassword())){
                    String userId = user.getId();
                    String accountStatus = user.getStatus();
                    if (!"0".equals(accountStatus)){
                        throw new TRSException(CodeUtils.ACCOUNT_LOCKOUT, "账号已被锁定！");
                    }
                    if (!"0".equals(user.getExpireAt())) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {
                            Date dateExpire = sdf.parse(user.getExpireAt());
                            int rangBetweenNow = DateUtil.rangBetweenNow(dateExpire);
                            if (rangBetweenNow < 0){
                                throw new TRSException(CodeUtils.IS_EXPIRED, "["+userAccount+"]机构或账号已经过期！");
                            }
                        } catch (ParseException e) {
                            throw new TRSException(e);
                        }
                    }
                    String orgId = user.getOrganizationId();
                    AppApiAccessToken token = appAccessTokenRepository.findAppApiAccessTokensByGrantSourceOwnerId(userId);
                    HashMap hashMap = new HashMap();
                    if (token == null){
                        //token不存在，第一次登录
                        Date expireTime = null;
                        String dateAfter = DateUtil.formatDateAfter(new Date(), DateUtil.yyyyMMdd, 30);
                        expireTime = DateUtil.stringToDate(dateAfter, DateUtil.yyyyMMdd);
                        AppApiAccessToken apiAccessToken = authService.applyAccessToken(orgId,expireTime,userId,user.getSubGroupId(),"High","Max");
                        hashMap.put("user",user);
                        hashMap.put("token",apiAccessToken);
                        result = new ApiCommonResult(ApiResultType.Success, hashMap);
                    }else if (new Date().after(token.getExpireTime())){
                        //toke已过期，扩展token日期
                        Date oldDate = token.getExpireTime();
                        String dateAfter = DateUtil.formatDateAfter(oldDate, DateUtil.yyyyMMdd, 30);
                        Date expireTime = DateUtil.stringToDate(dateAfter, DateUtil.yyyyMMdd);
                        token.setSubGroupId(user.getSubGroupId());
                        token = this.authService.extendToken(token, expireTime);
                        hashMap.put("user",user);
                        hashMap.put("token",token);
                        result = new ApiCommonResult(ApiResultType.Success, hashMap);
                    }else{
                        //已存在token且token不过期
                        hashMap.put("user",user);
                        hashMap.put("token",token);
                        result = new ApiCommonResult(ApiResultType.Success, hashMap);
                    }
                    RedisUtil.setAppToken(userId+"app",token);

                }else{
                    throw new TRSException(CodeUtils.FAIL, "密码错误！");
                }
            }

        }else{
            throw new TRSException(CodeUtils.UNKNOWN_ACCOUNT,"账号不存在！");
        }
        return result;
    }

    @Override
    public Object thirdColumnOp(String showIds,String hideIds,String parentId) {
        String[] idShow = showIds.split(";");
        String[] idHide = hideIds.split(";");
        IndexPage indexPage = indexPageService.findOne(parentId);
        List<IndexTabMapper> list = indexTabMapperService.findByIndexPage(indexPage);
        int hideCount = 0;
        int showCount = 0;
        for (int i=0;i<idShow.length;i++){
            IndexTabMapper findOne = indexTabMapperService.findOne(idShow[i]);
            if (ObjectUtil.isNotEmpty(findOne)) {
                findOne.setAppsequence(i + 1);
                findOne.setHide(false);
                indexTabMapperService.save(findOne);
            }
        }
        for(int j = 0;j<idHide.length;j++){
            IndexTabMapper findOne = indexTabMapperService.findOne(idHide[j]);
            if (ObjectUtil.isNotEmpty(findOne)) {
                findOne.setAppsequence(j + 1 );
                findOne.setHide(true);
                indexTabMapperService.save(findOne);
            }
        }
        return "Success!";
    }

    @Override
    public Object getAllFavourites(String userId,String subGroupId, int pageNo, int pageSize) {
        //原生sql
        Specification<Favourites> criteria = new Specification<Favourites>() {
            @Override
            public Predicate toPredicate(Root<Favourites> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (UserUtils.ROLE_LIST.contains(userService.findById(userId).getCheckRole())) {
                    predicates.add(cb.equal(root.get("userId"), userId));
                }else {
                    predicates.add(cb.equal(root.get("subGroupId"),subGroupId));
                }
                predicates.add(cb.isNull(root.get("libraryId")));
                Predicate[] pre = new Predicate[predicates.size()];
                return query.where(predicates.toArray(pre)).getRestriction();
            }
        };
        List<String> sidList = new ArrayList<String>();
        List<String> groList = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        Page<Favourites> favouritesPage = favouritesRepository.findAll(criteria,
                new PageRequest(pageNo, pageSize, new Sort(Sort.Direction.DESC, "createdTime")));
        List favouritesTotal = favouritesRepository.findAll(criteria,new Sort(Sort.Direction.DESC, "urltime"));
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.yyyyMMdd2);
        /*Favourites startFav = (Favourites)favouritesTotal.get(favouritesTotal.size()-1);
        String timeStart = startFav.getUrltime();
        Favourites endFav = (Favourites)favouritesTotal.get(0);
        String timeEnd = endFav.getUrltime();*/
        String[] timeArr = new String[2];
        if (ObjectUtil.isNotEmpty(favouritesPage)) {
            timeArr[0] = favouritesPage.getContent().get(0).getUrltime().replace("-", "").replace(" ", "").replace(":", "").substring(0, 8);
            timeArr[1] = favouritesPage.getContent().get(0).getUrltime().replace("-", "").replace(" ", "").replace(":", "").substring(0, 8);
            favouritesPage.forEach(item -> {
                String timeStr = item.getUrltime().replace("-", "").replace(" ", "").replace(":", "").substring(0, 8);
                try{
                    Date time = sdf.parse(timeStr);
                    if (time.before(sdf.parse(timeArr[0]))){
                        timeArr[0] = timeStr;
                    }else if (time.after(sdf.parse(timeArr[1]))){
                        timeArr[1] = timeStr;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

                sidList.add(item.getSid());
                groList.add(item.getGroupName());
                sb.append(item.getSid()).append(",");
            });
        }
        if (sidList.size() == 0){
            return null;
        }
        QueryCommonBuilder queryBuilder = new QueryCommonBuilder();
        String TRSL = buildSqlAll(sidList,groList);
        queryBuilder.filterByTRSL(TRSL);
        queryBuilder.page(0,pageSize);
        queryBuilder.setDatabase(Const.MIX_DATABASE.split(";"));
        PagedList favList = null;
        try{
            String timeStart = timeArr[0];
            String timeEnd = timeArr[1];
            if (StringUtil.isNotEmpty(timeStart) && StringUtil.isNotEmpty(timeEnd)) {
                /*timeStart = timeStart.replace("-", "").replace(" ", "").replace(":", "").substring(0, 8);
                timeEnd = timeEnd.replace("-", "").replace(" ", "").replace(":", "").substring(0, 8);*/
                Date start = sdf.parse(timeStart);
                Date end = sdf.parse(timeEnd);
                queryBuilder.setStartTime(start);
                queryBuilder.setEndTime(end);
                queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME, new String[]{timeStart+"000000",timeEnd+"235959"}, Operator.Between);
            }
            favList = hybase8SearchService.pageListCommon(queryBuilder,false,false,false,null);
        }catch (Exception e){
            e.printStackTrace();
        }
        // 返回给前端当前页
        int pageListNo = 0;
        // 返回给前端当前页条数
        int pageListSize = 0;
        List list = favList.getPageItems();
        List<FtsDocumentCommonVO> ftsDocumentCommonVOS = new ArrayList<>();
        pageListNo = favouritesTotal.size() % pageSize == 0 ? favouritesTotal.size() / pageSize
                : favouritesTotal.size() / pageSize + 1;
        pageListSize = favouritesTotal.size();
        for (int i = 0; i < sidList.size(); i++) {
            for (int j = 0 ;j <list.size();j++){
                FtsDocumentCommonVO ftsDocument = (FtsDocumentCommonVO)list.get(j);
                String groupName = ftsDocument.getGroupName();
                if(StringUtils.equals("国内微信", groupName)){
                    ftsDocument.setGroupName("微信");
                    ftsDocument.setSid(ftsDocument.getHkey());
                }
                if (sidList.get(i).equals(ftsDocument.getSid())){
                    ftsDocument.setContent(StringUtil.cutContent(StringUtil.replaceImg(ftsDocument.getContent()), 160));
                    ftsDocument.setTitle(StringUtil.cutContent(ftsDocument.getTitle(), 160));
                    ftsDocument.setAbstracts(StringUtil.replaceImg(ftsDocument.getAbstracts()));
                    ftsDocument.setFavourite(true);
                    ftsDocumentCommonVOS.add(ftsDocument);
                }
            }
        }
        PagedList<FtsDocumentCommonVO> pagedList = new PagedList<FtsDocumentCommonVO>(pageListNo,
                (int) (pageSize < 0 ? 15 : pageSize), pageListSize, ftsDocumentCommonVOS, 1);
        InfoListResult infoListResult = new InfoListResult<>(pagedList,pageListSize,pageListNo);
        infoListResult.setTotalList((int) Math.ceil((float)favouritesTotal.size()/pageSize));
        return infoListResult;
    }

    @Override
    public PageAlertSend getAppAlertData(String userId, String alertSource, int pageNo, int pageSize) {

        PageAlertSend pageAlertSend = alertSendService.findByUserIdAndSendType(pageNo, pageSize, userId, alertSource);
        if (ObjectUtil.isNotEmpty(pageAlertSend)){
            List<AlertSend> alertSends = pageAlertSend.getContent();
            if (ObjectUtil.isNotEmpty(alertSends)){
                for (AlertSend alertSend : alertSends) {
                    if (alertSend != null) {
                        Date alertTime = com.trs.netInsight.util.DateUtil.stringToDate(alertSend.getAlertTime(), com.trs.netInsight.util.DateUtil.yyyyMMdd);
                        alertSend.setAlertTime(String.valueOf(alertTime.getTime()));
                        String pushUserId = alertSend.getCreatedUserId();
                        User user = userService.findById(pushUserId);
                        if (ObjectUtil.isNotEmpty(user)){
                            alertSend.setPushHuman(user.getUserName());
                        }
                        List<Map<String, Object>> listMap = new ArrayList<>();
                        String sids = alertSend.getIds();
                        String createdUserId = alertSend.getCreatedUserId();
                        String url = alertNetinsightUrl+"/alert/getAlertByUserIdAndId";
                        String doPost = HttpUtil.sendPost(url, "userId="+createdUserId+"&ids="+sids);
                        if(StringUtil.isEmpty(doPost)){
                            alertSend.setAlertData(null);
                        }else if(doPost.contains("\"code\":500")){
                            Map<String,String> map = (Map<String,String>)JSON.parse(doPost);
                            String message = map.get("message");
                            log.error("预警数据获取失败,message:"+message);
                        }else {
                            //json转list
                            List<AlertEntity> list = JSONArray.parseObject(doPost, new TypeReference<ArrayList<AlertEntity>>() {});
                            try {
                                if (ObjectUtil.isNotEmpty(list)){
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
                                        map.put("time", time.getTime());
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

                                        listMap.add(map);
                                    }
                                    alertSend.setAlertData(listMap);
                                }else {
                                    alertSend.setAlertData(null);
                                }
                            } catch (Exception e) {
                                log.error("查询APP端预警数据出错！",e);
                            }
                        }
                    }
                }
                return pageAlertSend;
            }

            return pageAlertSend;
        }

        return pageAlertSend;
    }

    @Override
    public PageAlertSend getOneAlertData(String alertId,String userId) {
        try {
            return alertSendService.findOne(alertId,userId);
        } catch (OperationException e) {
            log.error("",e);
            return null;
        }
    }

    /**
     * 构建收藏检索表达式
     */
    public static String buildSqlAll(List<String> sidList,List<String> groupList) {
        if (ObjectUtil.isNotEmpty(sidList)) {
            StringBuilder strb = new StringBuilder();
            StringBuilder strWx = new StringBuilder();
            strb.append("IR_SID:(");
            strWx.append("IR_HKEY:(");
            for (int i=0;i<sidList.size();i++){
                if ("微信".equals(groupList.get(i)) || "国内微信".equals(groupList.get(i))){
                    strWx.append(sidList.get(i)).append(" OR ");
                }else {
                    strb.append(sidList.get(i)).append(" OR ");
                }
            }
            StringBuilder trslBui = new StringBuilder();
            String trslstrb = strb.toString();
            if (trslstrb.indexOf("OR")>0){
                trslstrb = trslstrb.substring(0, trslstrb.lastIndexOf("OR") - 1) + ")";
                trslBui.append(trslstrb);
            }
            String trslstrWx = strWx.toString();
            if (trslstrWx.indexOf("OR")>0){
                trslstrWx = trslstrWx.substring(0, trslstrWx.lastIndexOf("OR") - 1) + ")";
                if (trslBui.toString().length()>0){
                    trslBui.append(" OR ").append(trslstrWx);
                }else{
                    trslBui.append(trslstrWx);
                }
            }
            return trslBui.toString();
        } else {
            return null;
        }
    }
}
