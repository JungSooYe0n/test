package com.trs.netInsight.widget.report.service.impl;

import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.jpa.utils.Criteria;
import com.trs.jpa.utils.Restrictions;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.*;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.SourceUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.util.favourites.FavouritesUtil;
import com.trs.netInsight.widget.report.entity.Favourites;
import com.trs.netInsight.widget.report.entity.MaterialLibraryNew;
import com.trs.netInsight.widget.report.entity.repository.FavouritesRepository;
import com.trs.netInsight.widget.report.entity.repository.MaterialLibraryNewRepository;
import com.trs.netInsight.widget.report.service.IMaterialLibraryNewService;
import com.trs.netInsight.widget.report.service.IReportService;
import com.trs.netInsight.widget.report.util.ReportUtil;
import com.trs.netInsight.widget.special.entity.InfoListResult;
import com.trs.netInsight.widget.user.entity.User;
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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.trs.netInsight.widget.report.constant.ReportConst.SEMICOLON;

/**
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/10/22.
 * @desc  舆情报告 极简模式  素材库分组服务层接口实现类
 */
@Service
public class MaterialLibraryNewServiceImpl implements IMaterialLibraryNewService {
    @Autowired
    private MaterialLibraryNewRepository materialLibraryNewRepository;

    @Autowired
    private FavouritesRepository favouritesRepository;

    @Autowired
    private FullTextSearch hybase8SearchService;

    @Autowired
    private IReportService reportService;

    /**
     * 是否走独立预警服务
     */
    @Value("${http.client}")
    private boolean httpClient;
    /**
     * 独立预警服务地址
     */
    @Value("${http.alert.netinsight.url}")
    private String alertNetinsightUrl;

    @Override
    public MaterialLibraryNew save(String id, String name) {
        MaterialLibraryNew materialLibraryNew = null;
        if (StringUtils.isNotEmpty(id)){
            materialLibraryNew = materialLibraryNewRepository.findOne(id);
        }else {
            materialLibraryNew = new MaterialLibraryNew();
        }
        materialLibraryNew.setSpecialName(name);
        return materialLibraryNewRepository.save(materialLibraryNew);
    }

    @Override
    public String delete(String id) {

        materialLibraryNewRepository.delete(id);
        //同时删除分组下的数据
        Criteria<Favourites> criteria = new Criteria<Favourites>();
        criteria.add(Restrictions.eq("libraryId", id));
        criteria.add(Restrictions.eq("userId", UserUtils.getUser().getId()));
        List<Favourites> favourites = favouritesRepository.findAll(criteria);
        if (!favourites.isEmpty()){
            favouritesRepository.delete(favourites);
        }

        return Const.SUCCESS;

    }

    @Override
    public List<MaterialLibraryNew> findByUser(User user) {
        if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
            return  materialLibraryNewRepository.findByUserId(user.getId(), new Sort(Sort.Direction.DESC, "lastModifiedTime"));
        }else {
            return materialLibraryNewRepository.findBySubGroupId(user.getSubGroupId(), new Sort(Sort.Direction.DESC, "lastModifiedTime"));
        }
    }

    @Override
    public List<MaterialLibraryNew> findBySubGroupId() {
        return materialLibraryNewRepository.findBySubGroupId(UserUtils.getUser().getSubGroupId(), new Sort(Sort.Direction.DESC, "lastModifiedTime"));
    }

    @Override
    public MaterialLibraryNew findOne(String id) {
        return materialLibraryNewRepository.findOne(id);
    }

    @Override
    public String saveMaterialResource(String sids, User user, String md5, String urlTime, String groupName, String libraryId, String name) throws OperationException {
        String[] sidArry = null;
        if (StringUtils.isNotEmpty(sids)) {
            sidArry = sids.split(SEMICOLON);
        }
        String[] md5Arry = null;
        if (StringUtils.isNotEmpty(md5)) {
            md5Arry = md5.split(SEMICOLON);
        }
        String[] groupNameArray = null;
        if (StringUtils.isNotEmpty(groupName)) {
            groupNameArray = groupName.split(SEMICOLON);
        }
        String[] timeArray = null;
        if (StringUtils.isNotEmpty(urlTime)) {
            timeArray = urlTime.split(SEMICOLON);
        }
        //校验时间格式
        for (String s : timeArray) {
            if (!DateUtil.isValidDate(s)) {
                return "时间格式错误！";
            }
        }
        if (groupNameArray.length != sidArry.length && groupNameArray.length != timeArray.length && sidArry.length != timeArray.length) {
            return "fail";
        }
        if (StringUtils.isEmpty(libraryId) && StringUtils.isNotEmpty(name)) {
            //新建素材分组  并将资源加入
            MaterialLibraryNew materialLibraryNew = new MaterialLibraryNew();
            materialLibraryNew.setSpecialName(name);
            MaterialLibraryNew libraryNew = materialLibraryNewRepository.save(materialLibraryNew);
            libraryId = libraryNew.getId();
        } else if (StringUtils.isEmpty(libraryId)) {
            throw new OperationException("请传素材库分组 id");
        }
        Favourites newAdd = null;
        List<Favourites> favouritesList = new ArrayList<Favourites>();
        String newLibraryId = libraryId;


        List<String> sidExist = new ArrayList<>();
        // 排重，该用户素材库列表中没有此文章（即sid）才执行add
        //原生sql
        String[] array = sidArry;
        Specification<Favourites> criteria = new Specification<Favourites>() {

            @Override
            public Predicate toPredicate(Root<Favourites> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Object> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("userId"), user.getId()));
                predicates.add(cb.equal(root.get("libraryId"), newLibraryId));
                //predicates.add(cb.equal(root.get("sid"), sid));
                CriteriaBuilder.In<String> in = cb.in(root.get("sid").as(String.class));
                for (int i = 0; i < array.length; i++) {
                    in.value(array[i]);
                }
                predicates.add(in);
                Predicate[] pre = new Predicate[predicates.size()];

                return query.where(predicates.toArray(pre)).getRestriction();
            }
        };

        List<Favourites> favourites = favouritesRepository.findAll(criteria);
        if (ObjectUtil.isNotEmpty(favourites) && favourites.size() > 0) {
            for (Favourites fa : favourites) {
                sidExist.add(fa.getSid());
            }
        }
        try {
            QueryBuilder builder = DateUtil.timeBuilder(urlTime);
            QueryBuilder builderWeiXin = DateUtil.timeBuilder(urlTime);
            List<String> sid_weixin = new ArrayList<>();
            List<String> sid_other = new ArrayList<>();
            for (int i = 0; i < sidArry.length; i++) {
                if (sidExist.contains(sidArry[i])) {
                    continue;
                } else {
                    if (groupNameArray[i].equals("微信") || groupNameArray[i].equals("国内微信")) {
                        sid_weixin.add(sidArry[i]);
                    } else {
                        sid_other.add(sidArry[i]);
                    }
                }
            }

            List<FtsDocumentCommonVO> result = new ArrayList<>();
            if (sid_other.size() > 0) {
                builder.filterField(FtsFieldConst.FIELD_SID, StringUtils.join(sid_other, " OR "), Operator.Equal);
                builder.setDatabase(Const.MIX_DATABASE);
                builder.page(0, sid_other.size() *2 );
                System.err.println("历史数据表达式："+builder.asTRSL());
                //log.info("选中导出查询数据表达式 - 全部：" + builder.asTRSL());
                PagedList<FtsDocumentCommonVO> pagedList = hybase8SearchService.ftsPageList(builder, FtsDocumentCommonVO.class, false, false, false, null);
                if(pagedList.getPageItems() != null && pagedList.getPageItems().size() > 0){
                    result.addAll(pagedList.getPageItems());
                }
            }
            if (sid_weixin.size() > 0) {
                String weixinids = StringUtils.join(sid_weixin, " OR ");
                builderWeiXin.filterField(FtsFieldConst.FIELD_HKEY, weixinids, Operator.Equal);
                builderWeiXin.filterField(FtsFieldConst.FIELD_GROUPNAME, "国内微信", Operator.Equal);
                builderWeiXin.setDatabase(Const.WECHAT);
                builderWeiXin.page(0, sid_weixin.size() * 2);
                System.err.println("历史数据表达式："+builderWeiXin.asTRSL());
                //log.info("选中导出查询数据表达式 - 微信：" + builderWeiXin.asTRSL());
                PagedList<FtsDocumentCommonVO> pagedListWeiXin = hybase8SearchService.ftsPageList(builderWeiXin, FtsDocumentCommonVO.class, false, false, false, null);
                if(pagedListWeiXin.getPageItems() != null && pagedListWeiXin.getPageItems().size() > 0){
                    result.addAll(pagedListWeiXin.getPageItems());
                }
            }

            if (ObjectUtil.isNotEmpty(result) && result.size() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.yyyyMMdd);
                String screenName = null;
                for (FtsDocumentCommonVO fav : result) {
                    fav.setFavourite(true);
                    // 去掉img标签
                    String content = fav.getContent();
                    if (StringUtil.isNotEmpty(content)) {
                        content = StringUtil.replaceImg(content);
                        content = StringUtil.filterEmoji(content);
                    }
                    fav.setContent(content);

                    if (fav.getGroupName().equals("Twitter") || fav.getGroupName().equals("Facebook") || fav.getGroupName().equals("国内微信")) {
                        screenName = fav.getAuthors();
                    }else{
                        screenName = fav.getScreenName();
                    }
                    screenName = StringUtil.replaceImg(screenName);
                    screenName = StringUtil.filterEmoji(screenName);
                    String sid = fav.getSid();
                    if(Const.GROUPNAME_WEIBO.equals(fav.getGroupName())){
                        sid = fav.getMid();
                    }else if(Const.GROUPNAME_WEIXIN.equals(fav.getGroupName())){
                        sid = fav.getHkey();
                    }
                    newAdd = new Favourites(sid, user.getId(), user.getSubGroupId(), fav.getGroupName(), fav.getUrlTime(),
                            sdf.format(fav.getUrlTime()), fav.getUrlName(), fav.getMd5Tag(), fav.getAuthors(),
                            StringUtil.cutContentPro(StringUtil.replaceImg(subString(fav.getTitle())), Const.CONTENT_LENGTH),
                            StringUtil.cutContentPro(StringUtil.replaceImg(subString(fav.getContent())), Const.CONTENT_LENGTH),
                            screenName, fav.getUrlDate(), fav.getSiteName(), fav.getSrcName(),
                            StringUtil.cutContentPro(StringUtil.replaceImg(subString(fav.getAbstracts())), Const.CONTENT_LENGTH), fav.getRetweetedMid(),
                            fav.getNreserved1(), fav.getCommtCount(), fav.getRttCount(), false, fav.getCreatedAt(),
                            subString(fav.getStatusContent()), StringUtil.cutContentPro(StringUtil.replaceImg(subString(fav.getUrlTitle())), Const.CONTENT_LENGTH));
                    newAdd.setLibraryId(newLibraryId);
                    favouritesList.add(newAdd);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        if (favouritesList.size() > 0) {
            favouritesRepository.save(favouritesList);
        }
        return Const.SUCCESS;
    }

    @Override
    public Object findMaterialResource(String libraryId, int pageNo, int pageSize,
                                                String groupName, String fuzzyValue, String invitationCard,
                                                String forwarPrimary,String time) throws  Exception{
        User loginUer = UserUtils.getUser();
        List<String> sidList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        Criteria<Favourites> criteria = new Criteria<>();
        if (UserUtils.ROLE_LIST.contains(loginUer.getCheckRole())){
            criteria.add(Restrictions.eq("userId",loginUer.getId()));
        }else {
            criteria.add(Restrictions.eq("subGroupId",loginUer.getSubGroupId()));
        }
        String start = DateUtil.formatTimeRange(time)[0];
        String end = DateUtil.formatTimeRange(time)[1];
        Date startTime = time == null?new Date():DateUtil.stringToDate(start,DateUtil.yyyyMMddHHmmss);
        Date endTime = time == null?new Date():DateUtil.stringToDate(end,DateUtil.yyyyMMddHHmmss);

        criteria.add(Restrictions.between("createdTime",startTime,endTime));
        if (StringUtils.isEmpty(libraryId)){
            throw new OperationException("请传入素材库id");
        }
        criteria.add(Restrictions.eq("libraryId",libraryId));
        criteria.add(Restrictions.eq("groupName", groupName));
        List<Favourites> findAll = favouritesRepository.findAll(criteria,new Sort(
                Sort.Direction.DESC, "urltime"));
        for (Favourites favourites : findAll) {
            sidList.add(favourites.getSid());
        }
        // 总条数
        int totalItemCount = findAll.size();
        // 总页数
        int totalList = totalItemCount % pageSize > 0 ? totalItemCount
                / pageSize + 1 : totalItemCount / pageSize;
        if (StringUtil.isNotEmpty(fuzzyValue) || StringUtil.isNotEmpty(forwarPrimary) || StringUtil.isNotEmpty(invitationCard)) {
            List<Favourites> favouriteList = favouritesRepository.findAll(
                    criteria, new Sort(Sort.Direction.DESC, "urltime"));
            if (ObjectUtil.isNotEmpty(favouriteList)) {
                favouriteList.forEach(item -> {
                    //   sidList.add(item.getSid());
                    sb.append(item.getSid()).append(",");
                });
            }
        } else {
            Page<Favourites> favouritesPage = favouritesRepository.findAll(
                    criteria, new PageRequest(pageNo, pageSize, new Sort(
                            Sort.Direction.DESC, "urltime")));
            if (ObjectUtil.isNotEmpty(favouritesPage)) {
                favouritesPage.forEach(item -> {
                    // sidList.add(item.getSid());
                    sb.append(item.getSid()).append(",");
                });
            }
        }
        String sids = "";
        if (sb.length() > 1){
            sids = sb.substring(0,sb.length()-1);
        }
         //传统库
        if (Const.MEDIA_TYPE_NEWS.contains(groupName)) {
            return favourite(loginUer,sids, sidList, pageNo, pageSize, totalItemCount,
                    totalList, fuzzyValue, invitationCard, groupName,time);
        } else if (Const.MEDIA_TYPE_WEIBO.contains(groupName)) {
            // 微博库
            return favouriteWeiBo(loginUer, sids,sidList, pageNo, pageSize, totalItemCount,
                    totalList, fuzzyValue, forwarPrimary,time);
        } else if (Const.MEDIA_TYPE_WEIXIN.contains(groupName)) {
            // 微信库
            return favouriteWeiXin(loginUer, sids,sidList, pageNo, pageSize,
                    totalItemCount, totalList, fuzzyValue,time);
        } else if (Const.MEDIA_TYPE_TF.contains(groupName)) {
            // FaceBook,Twitter
            return favouriteTF(loginUer, sids,sidList, 0, pageSize, totalItemCount,
                    totalList, fuzzyValue,time);
        }
        return null;
    }

    @Override
    public List<FtsDocumentCommonVO> findMaterialResourceForReport(String libraryId) throws TRSException {

        User loginUser = UserUtils.getUser();
        List<String> sidList = new ArrayList<>();
        Criteria<Favourites> criteria = new Criteria<>();
        if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
            criteria.add(Restrictions.eq("userId",loginUser.getId()));
        }else {
            criteria.add(Restrictions.eq("subGroupId",loginUser.getSubGroupId()));

        }
        criteria.add(Restrictions.eq("libraryId",libraryId));

        List<Favourites> favouriteList = favouritesRepository.findAll(
                criteria, new Sort(Sort.Direction.DESC, "createdTime"));
        List<String> dataBases = new ArrayList<>();
        List<FtsDocumentCommonVO> returnData = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(favouriteList)) {
            favouriteList.forEach(item -> {
                sidList.add(item.getSid());
            });
            //因为微信传入的是hkey 所以分开查
            List<FtsDocumentCommonVO> ftsDocumentCommonVOS = new ArrayList<>();
            List<FtsDocumentCommonVO> ftsWeChatDocumentCommonVOS = new ArrayList<>();
            Map<String, List<Favourites>> collect = favouriteList.stream().collect(Collectors.groupingBy(Favourites::getGroupName));
            for (String groupName : collect.keySet()){
                if (Const.MEDIA_TYPE_WEIXIN.contains(groupName)){
                    dataBases.add(Const.WECHAT);
                }else if (Const.MEDIA_TYPE_NEWS.contains(groupName)){
                        dataBases.add(Const.HYBASE_NI_INDEX);
                }else if (Const.MEDIA_TYPE_WEIBO.contains(groupName)){
                        dataBases.add(Const.WEIBO);
                }else if (Const.MEDIA_TYPE_TF.contains(groupName)){
                        dataBases.add(Const.HYBASE_OVERSEAS);
                }
            }
            if (dataBases.contains(Const.WECHAT)){
                List<String> weChat = new ArrayList<>();
                weChat.add(Const.WECHAT);
                ftsWeChatDocumentCommonVOS = favCommonList(sidList,weChat);
                if (ObjectUtil.isNotEmpty(dataBases) && dataBases.size()>1){
                    for (String dataBase : dataBases) {
                        if (Const.WECHAT.equals(dataBase)){
                            dataBases.remove(dataBase);
                            break;
                        }
                    }
                    ftsDocumentCommonVOS = favCommonList(sidList,dataBases);
                }
            }else {
                ftsDocumentCommonVOS = favCommonList(sidList,dataBases);
            }
            returnData.addAll(ftsDocumentCommonVOS);
            returnData.addAll(ftsWeChatDocumentCommonVOS);
        }
        return returnData;
    }

    @Override
    public String delLibraryResource(String sids, String libraryId) {
        String[] sidArry = sids.split(SEMICOLON);
        Criteria<Favourites> criteria = new Criteria<Favourites>();
        criteria.add(Restrictions.in("sid", Arrays.asList(sidArry)));
        User loginUser = UserUtils.getUser();
        if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
            criteria.add(Restrictions.eq("userId",loginUser.getId()));
        }else {
            criteria.add(Restrictions.eq("subGroupId",loginUser.getSubGroupId()));
        }
        criteria.add(Restrictions.eq("libraryId",libraryId));
        List<Favourites> all = favouritesRepository.findAll(criteria);
        favouritesRepository.delete(favouritesRepository.findAll(criteria));
        return Const.SUCCESS;
    }


    /**
     * 查询某素材库分组下对应的hybase数据
     * @param sidList
     * @param dataBases
     * @return
     */
    private List<FtsDocumentCommonVO> favCommonList(List<String> sidList,List<String> dataBases) throws TRSException{
        QueryCommonBuilder queryCommonBuilder = new QueryCommonBuilder();
        queryCommonBuilder.setDatabase(dataBases.toArray(new String[dataBases.size()]));
        String trsl = null;
        if (dataBases.contains(Const.WECHAT) && dataBases.size()==1){
            trsl = FavouritesUtil.buildSqlWeiXin(sidList);
        }else {
            trsl = FavouritesUtil.buildSql(sidList);
        }

        if (trsl == null) {
            return null;
        }
        queryCommonBuilder.filterByTRSL(trsl);
        queryCommonBuilder.page(0,sidList.size());
        PagedList<FtsDocumentCommonVO> commonVOPagedList = hybase8SearchService.pageListCommon(queryCommonBuilder, false, false,false,null);
        List<FtsDocumentCommonVO> pageItems = commonVOPagedList.getPageItems();
        return pageItems;
    }
    /**
     * 检索用户收藏 传统库
     *
     * @param sidList
     *            主键列表
     * @param pageNo
     *            第几页
     * @param pageSize
     *            一页几条
     * @return
     * @throws TRSException
     */
    public Object favourite(User user,String sids, List<String> sidList, int pageNo,
                            int pageSize, int totalItemCount, int totalList, String keywords,
                            String invitationCard, String groupName,String time) throws TRSException {
        String TRSL = FavouritesUtil.buildSql(sidList);
        // 把已经预警的装里边
       /* List<String> sidAlert = new ArrayList<>();
        List<AlertEntity> alertList = null;
        if (httpClient){
            alertList = AlertUtil.getAlerts(userId,sids,alertNetinsightUrl);
        }else {
            alertList = alertRepository.findByUserIdAndSidIn(
                    userId, sidList);
        }
        if (ObjectUtil.isNotEmpty(alertList)){
            for (AlertEntity alert : alertList) {
                sidAlert.add(alert.getSid());
            }
        }*/
        String[] split = groupName.split(";");
        for (int i = 0; i < split.length; i++) {
            if (split[i].equals("国内新闻_手机客户端") || split[i].equals("国内新闻_电子报")) {
                split[i] = split[i].substring(split[i].length() - 3,
                        split[i].length());
            }
        }
        groupName = org.apache.commons.lang.StringUtils.join(split, ";");
        String groupNameNew = org.apache.commons.lang.StringUtils.join(split, ";");
        //收藏
        List<String> sidFavourite = new ArrayList<>();
        //原生sql
        Specification<Favourites> criteria = new Specification<Favourites>() {

            @Override
            public Predicate toPredicate(Root<Favourites> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Object> predicates = new ArrayList<>();
                if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
                    predicates.add(cb.equal(root.get("userId"),user.getId()));
                }else {
                    predicates.add(cb.equal(root.get("subGroupId"),user.getSubGroupId()));
                }
                predicates.add(cb.isNull(root.get("libraryId")));
                predicates.add(cb.equal(root.get("groupName"), groupNameNew));

                Predicate[] pre = new Predicate[predicates.size()];

                return query.where(predicates.toArray(pre)).getRestriction();
            }
        };
//        List<Favourites> favouritesList = favouritesRepository.findByUserIdAndSidIn(
//                userId, sidList);
        List<Favourites> favouritesList = favouritesRepository.findAll(criteria, new Sort(Sort.Direction.DESC, "urltime"));
        if(ObjectUtil.isNotEmpty(favouritesList)){
            for (Favourites faSidList : favouritesList) {
                //sidFavourite装载了所有已收藏的文章sid
                sidFavourite.add(faSidList.getSid());
            }
        }

        if (TRSL == null && StringUtil.isEmpty(sids)) {
            return null;
        }
        QueryBuilder searchBuilder = new QueryBuilder();
//        if(StringUtil.isNotEmpty(timeStart) && StringUtil.isNotEmpty(timeEnd)){
//            searchBuilder = DateUtil.timeBuilder(timeStart+";"+timeEnd);
//        }
      //  searchBuilder.filterByTRSL(TRSL);
        sids = sids.replaceAll(","," OR ");
        if (sids.endsWith(" OR ")){
            sids = sids.substring(0,sids.length()-4);
        }
        searchBuilder.filterField(FtsFieldConst.FIELD_SID,sids,Operator.Equal);
        String trsl = "";
        if (StringUtil.isNotEmpty(keywords)) {
            trsl = new StringBuffer().append(FtsFieldConst.FIELD_TITLE)
                    .append(":").append(keywords).append(" OR ")
                    .append(FtsFieldConst.FIELD_ABSTRACTS).append(":")
                    .append(keywords).append(" OR ")
                    .append(FtsFieldConst.FIELD_CONTENT).append(":")
                    .append(keywords).append(" OR ")
                    .append(FtsFieldConst.FIELD_KEYWORDS).append(":")
                    .append(keywords).toString();
            searchBuilder.filterByTRSL(trsl);
        }
        if (StringUtil.isNotEmpty(invitationCard) || StringUtil.isNotEmpty(keywords)){
            searchBuilder.page(pageNo, pageSize);
        }else {
            searchBuilder.page(0, pageSize);
        }
        try {
            String asTRSL = searchBuilder.asTRSL();
            String builderDatabase = searchBuilder.getDatabase();
            StringBuilder sb = new StringBuilder(asTRSL);
            StringBuilder countSB = new StringBuilder();
            if ("国内论坛".equals(groupName)) {
                if ("0".equals(invitationCard)) {// 主贴
                    searchBuilder = new QueryBuilder();
                    sb.append(" AND ").append(FtsFieldConst.FIELD_NRESERVED1)
                            .append(":0");
                    countSB.append(FtsFieldConst.FIELD_NRESERVED1)
                            .append(":0");
                } else if ("1".equals(invitationCard)) {// 回帖
                    searchBuilder = new QueryBuilder();
                    sb.append(" AND ").append(FtsFieldConst.FIELD_NRESERVED1)
                            .append(":1");
                    countSB.append(FtsFieldConst.FIELD_NRESERVED1)
                            .append(":1");
                }
                searchBuilder.setDatabase(builderDatabase);
                searchBuilder.filterByTRSL(sb.toString());
            }
            /*// 时间
            if (StringUtil.isNotEmpty(time)){
                searchBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
            }*/
            searchBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
            List<FtsDocument> ftsQuery = hybase8SearchService.ftsQuery(searchBuilder, FtsDocument.class, false,false,false,null);
            List<FtsDocument> result = new ArrayList<>();
            if (sidList.size() != ftsQuery.size()){
                sidList = new ArrayList<>();
            }
            for (FtsDocument chartAnalyze : ftsQuery) {
                String id = chartAnalyze.getSid();
                // 预警数据
               /* int indexOf = sidAlert.indexOf(id);
                if (indexOf < 0) {
                    chartAnalyze.setSend(false);
                } else {
                    chartAnalyze.setSend(true);
                }*/

                //是否收藏信息
                int indexOfFa = sidFavourite.indexOf(id);
                if (indexOfFa < 0) {
                    chartAnalyze.setFavourite(false);
                } else {
                    chartAnalyze.setFavourite(true);
                }

                // 我的收藏不算相似文章数
                // 去掉img标签
                String content = chartAnalyze.getContent();
                if (StringUtil.isNotEmpty(content)) {
                    content = StringUtil.replaceImg(content);
                }
                chartAnalyze.setContent(content);
                result.add(chartAnalyze);

                if (sidList.size() != ftsQuery.size()){
                    sidList.add(chartAnalyze.getSid());
                }
            }
            List<FtsDocument> resultByTime = FavouritesUtil.resultByTimeTrandition(result,
                    sidList);
            if(resultByTime==null || resultByTime.size()==0){
                return null;
            }
            if (StringUtil.isNotEmpty(keywords) || StringUtil.isNotEmpty(invitationCard)) {
                QueryBuilder countBuilder = new QueryBuilder();
                countBuilder.filterByTRSL(trsl);
                countBuilder.filterByTRSL(TRSL);
                countBuilder.filterByTRSL(countSB.toString());
                countBuilder.page(0,totalItemCount);
                if (StringUtil.isNotEmpty(time)) {
                    countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
                }
                countBuilder.setDatabase(Const.HYBASE_NI_INDEX);
                long ftsCount = hybase8SearchService.ftsCount(countBuilder, false, false,false,null);
                totalItemCount = new Long(ftsCount).intValue();
                totalList = totalItemCount % pageSize > 0 ? totalItemCount / pageSize + 1 : totalItemCount / pageSize;
            }
            return new InfoListResult<>(resultByTime, totalItemCount, totalList);

        } catch (Exception e) {
            throw new OperationException("检索异常：message:" + e);
        }
    }

    /**
     * 检索用户收藏 微博库
     *
     * @param sidList
     *            主键列表
     * @param pageNo
     *            第几页
     * @param pageSize
     *            一页几条
     * @return
     * @throws TRSException
     */
    public Object favouriteWeiBo(User user, String sids,List<String> sidList,
                                 int pageNo, int pageSize, int totalItemCount, int totalList,
                                 String keywords, String forwarPrimary,String time) throws TRSException {
        // 把已经预警的装里边
      /*  List<String> sidAlert = new ArrayList<>();
        List<AlertEntity> alertList = null;
        if (httpClient){
            alertList = AlertUtil.getAlerts(userId,sids,alertNetinsightUrl);
        }else {
            alertList = alertRepository.findByUserIdAndSidIn(
                    userId, sidList);
        }
        if (ObjectUtil.isNotEmpty(alertList)){
            for (AlertEntity alert : alertList) {
                sidAlert.add(alert.getSid());
            }
        }*/

        String TRSL = FavouritesUtil.buildSqlWeiBo(sidList);

        //收藏
        List<String> sidFavourite = new ArrayList<>();
        //原生sql
        Specification<Favourites> criteria = new Specification<Favourites>() {

            @Override
            public Predicate toPredicate(Root<Favourites> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Object> predicates = new ArrayList<>();
                if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
                    predicates.add(cb.equal(root.get("userId"),user.getId()));
                }else {
                    predicates.add(cb.equal(root.get("subGroupId"),user.getSubGroupId()));
                }
                predicates.add(cb.isNull(root.get("libraryId")));
                Predicate[] pre = new Predicate[predicates.size()];

                return query.where(predicates.toArray(pre)).getRestriction();
            }
        };
        List<Favourites> favouritesList = favouritesRepository.findAll(criteria, new Sort(Sort.Direction.DESC, "urltime"));
//        List<Favourites> favouritesList = favouritesRepository.findByUserIdAndSidIn(
//                userId, sidList);
        if(ObjectUtil.isNotEmpty(favouritesList)){
            for (Favourites faSidList : favouritesList) {
                //sidFavourite装载了所有已收藏的文章sid
                sidFavourite.add(faSidList.getSid());
            }
        }

        if (TRSL == null && StringUtil.isEmpty(sids)) {
            return null;
        }
        QueryBuilder searchBuilder = new QueryBuilder();
//        if(StringUtil.isNotEmpty(timeStart) && StringUtil.isNotEmpty(timeEnd)){
//            searchBuilder = DateUtil.timeBuilder(timeStart+";"+timeEnd);
//        }
       // searchBuilder.filterByTRSL(TRSL);
        sids = sids.replaceAll(","," OR ");
        if (sids.endsWith(" OR ")){
            sids = sids.substring(0,sids.length()-4);
        }
        searchBuilder.filterField(FtsFieldConst.FIELD_SID,sids,Operator.Equal);
        String trsl = "";
        if (StringUtil.isNotEmpty(keywords)) {
            trsl = new StringBuffer()
                    .append(FtsFieldConst.FIELD_STATUS_CONTENT).append(":\"")
                    .append(keywords).append("\"").toString();
            searchBuilder.filterByTRSL(trsl);
        }

        // 转发 / 原发
        String builderTRSL = searchBuilder.asTRSL();
        String builderDatabase = searchBuilder.getDatabase();
        StringBuilder builderTrsl = new StringBuilder(builderTRSL);
        if ("primary".equals(forwarPrimary)) {
            // 原发
            searchBuilder.filterByTRSL(Const.PRIMARY_WEIBO);
        } else if ("forward".equals(forwarPrimary)) {
            // 转发
            searchBuilder = new QueryBuilder();

            builderTrsl.append(" NOT ").append(Const.PRIMARY_WEIBO);
            searchBuilder.filterByTRSL(builderTrsl.toString());

            searchBuilder.setDatabase(builderDatabase);
        }
        if (StringUtil.isNotEmpty(forwarPrimary) || StringUtil.isNotEmpty(keywords)){
            searchBuilder.page(pageNo, pageSize);
        }else {
            searchBuilder.page(0, pageSize);
        }
        try {
           /* // 时间
            if (StringUtil.isNotEmpty(time)){
                searchBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
            }*/
            searchBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
            List<FtsDocumentStatus> ftsQuery = hybase8SearchService.ftsQuery(searchBuilder, FtsDocumentStatus.class,
                    false,false,false,null);
            List<FtsDocumentStatus> result = new ArrayList<>();
            if (sidList.size() != ftsQuery.size()){
                sidList = new ArrayList<>();
            }
            for (FtsDocumentStatus chartAnalyze : ftsQuery) {
                String id = chartAnalyze.getMid();
                // 预警数据
                /*int indexOf = sidAlert.indexOf(id);
                if (indexOf < 0) {
                    chartAnalyze.setSend(false);
                } else {
                    chartAnalyze.setSend(true);
                }*/

                //是否收藏信息
                int indexOfFa = sidFavourite.indexOf(id);
                if (indexOfFa < 0) {
                    chartAnalyze.setFavourite(false);
                } else {
                    chartAnalyze.setFavourite(true);
                }

                // 我的收藏不算相似文章数
                // 去掉img标签
                String content = chartAnalyze.getStatusContent();
                if (StringUtil.isNotEmpty(content)) {
                    content = StringUtil.replaceImg(content);
                }
                chartAnalyze.setStatusContent(content);
                result.add(chartAnalyze);
                if (sidList.size() != ftsQuery.size()) {
                    sidList.add(chartAnalyze.getMid());
                }

            }
            if (StringUtil.isNotEmpty(keywords) || StringUtil.isNotEmpty(forwarPrimary) || StringUtil.isNotEmpty(time)) {
                QueryBuilder countBuilder = new QueryBuilder();
                //countBuilder.filterByTRSL(searchBuilder.asTRSL());
                countBuilder.filterByTRSL(TRSL);
                countBuilder.filterByTRSL(trsl);
                if (StringUtil.isNotEmpty(time)){
                    countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
                }
                if ("primary".equals(forwarPrimary)) {
                    // 原发
                    countBuilder.filterByTRSL(Const.PRIMARY_WEIBO);
                } else if ("forward".equals(forwarPrimary)) {
                    // 转发
                    String s = countBuilder.asTRSL();
                    countBuilder = new QueryBuilder();
                    StringBuilder stringBuilder = new StringBuilder(s);
                    stringBuilder.append(" NOT ").append(Const.PRIMARY_WEIBO);
                    countBuilder.filterByTRSL(stringBuilder.toString());

                }
                countBuilder.page(0,totalItemCount);
                countBuilder.setDatabase(Const.WEIBO);
                long ftsCount = hybase8SearchService.ftsCount(countBuilder, false, false,false,null);
                totalItemCount = new Long(ftsCount).intValue();
                totalList = totalItemCount % pageSize > 0 ? totalItemCount / pageSize + 1 : totalItemCount / pageSize;
            }
            List<FtsDocumentStatus> resultByTime = FavouritesUtil.resultByTimeWeiBo(result,
                    sidList);
            return new InfoListResult<>(resultByTime, totalItemCount, totalList);
            // return resultByTime;
        } catch (Exception e) {
            throw new OperationException("检索异常：message:" + e);
        }
    }


    /**
     * 检索用户收藏 微信库
     *
     * @param sidList
     *            主键列表
     * @param pageNo
     *            第几页
     * @param pageSize
     *            一页几条
     * @param time
     *             这个应该为入库时间,添加素材库时间,不可以为文章时间
     * @return
     * @throws TRSException
     */
    public Object favouriteWeiXin(User user,String sids, List<String> sidList,
                                  int pageNo, int pageSize, int totalItemCount, int totalList,
                                  String keywords,String time) throws TRSException {
        // 把已经预警的装里边
       /* List<String> sidAlert = new ArrayList<>();
        List<AlertEntity> alertList = null;
        if (httpClient){
            alertList = AlertUtil.getAlerts(userId,sids,alertNetinsightUrl);
        }else {
            alertList = alertRepository.findByUserIdAndSidIn(
                    userId, sidList);
        }
        if (ObjectUtil.isNotEmpty(alertList)){
            for (AlertEntity alert : alertList) {
                sidAlert.add(alert.getSid());
            }
        }
        for (AlertEntity alert : alertList) {
            sidAlert.add(alert.getSid());
        }*/
        String TRSL = FavouritesUtil.buildSqlWeiXin(sidList);

        //收藏
        List<String> sidFavourite = new ArrayList<>();
        //原生sql
        Specification<Favourites> criteria = new Specification<Favourites>() {

            @Override
            public Predicate toPredicate(Root<Favourites> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Object> predicates = new ArrayList<>();
                if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
                    predicates.add(cb.equal(root.get("userId"),user.getId()));
                }else {
                    predicates.add(cb.equal(root.get("subGroupId"),user.getSubGroupId()));
                }
                predicates.add(cb.isNull(root.get("libraryId")));
                Predicate[] pre = new Predicate[predicates.size()];

                return query.where(predicates.toArray(pre)).getRestriction();
            }
        };
        List<Favourites> favouritesList = favouritesRepository.findAll(criteria, new Sort(Sort.Direction.DESC, "urltime"));
//        List<Favourites> favouritesList = favouritesRepository.findByUserIdAndSidIn(
//                userId, sidList);
        if(ObjectUtil.isNotEmpty(favouritesList)){
            for (Favourites faSidList : favouritesList) {
                //sidFavourite装载了所有已收藏的文章sid
                sidFavourite.add(faSidList.getSid());
            }
        }

        if (TRSL == null && StringUtil.isEmpty(sids)) {
            return null;
        }
        QueryBuilder searchBuilder = new QueryBuilder();
        //加入时间段
//        if(StringUtil.isNotEmpty(timeStart) && StringUtil.isNotEmpty(timeEnd)){
//            searchBuilder = DateUtil.timeBuilder(timeStart+";"+timeEnd);
//        }
      //  searchBuilder.filterByTRSL(TRSL);
        sids = sids.replaceAll(","," OR ");
        if (sids.endsWith(" OR ")){
            sids = sids.substring(0,sids.length()-4);
        }
        searchBuilder.filterField(FtsFieldConst.FIELD_HKEY,sids,Operator.Equal);
        String trsl = "";
        if (StringUtil.isNotEmpty(keywords)) {
             trsl = new StringBuffer().append(FtsFieldConst.FIELD_TITLE)
                    .append(":\"").append(keywords).append("\" OR ")
                    .append(FtsFieldConst.FIELD_CONTENT).append(":\"")
                    .append(keywords).append("\"").toString();
            searchBuilder.filterByTRSL(trsl);
        }
        if (StringUtil.isNotEmpty(keywords)){
            searchBuilder.page(pageNo, pageSize);
        }else {
            searchBuilder.page(0, pageSize);
        }
        try {
           /* // 时间
            if (StringUtil.isNotEmpty(time)){
                searchBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
            }*/
            List<FtsDocumentWeChat> ftsQuery = hybase8SearchService.ftsQuery(searchBuilder, FtsDocumentWeChat.class,
                    false,false,false,null);
            List<FtsDocumentWeChat> result = new ArrayList<>();
            if (sidList.size() != ftsQuery.size()){
                sidList = new ArrayList<>();
            }
            for (FtsDocumentWeChat chartAnalyze : ftsQuery) {
                String id = chartAnalyze.getHkey();
                chartAnalyze.setId(id);
                chartAnalyze.setSid(id);
                // 预警数据
               /* int indexOf = sidAlert.indexOf(id);
                if (indexOf < 0) {
                    chartAnalyze.setSend(false);
                } else {
                    chartAnalyze.setSend(true);
                }*/
                //是否收藏信息
                int indexOfFa = sidFavourite.indexOf(id);
                if (indexOfFa < 0) {
                    chartAnalyze.setFavourite(false);
                } else {
                    chartAnalyze.setFavourite(true);
                }

                // 我的收藏里边不用显示相似文章数了
                // 去掉img标签
                String content = chartAnalyze.getContent();
                if (StringUtil.isNotEmpty(content)) {
                    content = StringUtil.replaceImg(content).replace("　　", "");// 过滤空格
                    // img标签
                }
                //解决收藏列表中微信摘要太长的问题，现取两行
                if(content.length() > 160){
                    content = content.substring(0, 160) + "...";
                }
                chartAnalyze.setContent(content);
                result.add(chartAnalyze);

                if (sidList.size() != ftsQuery.size()){
                    sidList.add(chartAnalyze.getHkey());
                }
            }
            if (StringUtil.isNotEmpty(keywords) || StringUtil.isNotEmpty(time)) {
                QueryBuilder countBuilder = new QueryBuilder();
                countBuilder.filterByTRSL(trsl);
                countBuilder.filterByTRSL(TRSL);
                if (StringUtil.isNotEmpty(time)){
                    countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
                }
                countBuilder.page(0,totalItemCount);
                countBuilder.setDatabase(Const.WECHAT);
                long ftsCount = hybase8SearchService.ftsCount(countBuilder, false, false,false,null);
                totalItemCount = new Long(ftsCount).intValue();
                totalList = totalItemCount % pageSize > 0 ? totalItemCount / pageSize + 1 : totalItemCount / pageSize;
            }
            List<FtsDocumentWeChat> resultByTime = FavouritesUtil.resultByTimeWeiXin(result,
                    sidList);
            return new InfoListResult<>(resultByTime, totalItemCount, totalList);
        } catch (Exception e) {
            throw new OperationException("检索异常：message:" + e.getMessage(), e);
        }
    }

    /**
     * 检索用户收藏 TF
     *
     * @param sidList
     *            主键列表
     * @param pageNo
     *            第几页
     * @param pageSize
     *            一页几条
     * @return
     * @throws TRSException
     */
    public Object favouriteTF(User user,String sids, List<String> sidList, int pageNo,
                              int pageSize, int totalItemCount, int totalList, String keywords,String time)
            throws TRSException {
        //收藏
        List<String> sidFavourite = new ArrayList<>();
        //原生sql
        Specification<Favourites> criteria = new Specification<Favourites>() {

            @Override
            public Predicate toPredicate(Root<Favourites> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Object> predicates = new ArrayList<>();
                if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
                    predicates.add(cb.equal(root.get("userId"),user.getId()));
                }else {
                    predicates.add(cb.equal(root.get("subGroupId"),user.getSubGroupId()));
                }                predicates.add(cb.isNull(root.get("libraryId")));
                Predicate[] pre = new Predicate[predicates.size()];

                return query.where(predicates.toArray(pre)).getRestriction();
            }
        };
        List<Favourites> favouritesList = favouritesRepository.findAll(criteria, new Sort(Sort.Direction.DESC, "lastModifiedTime"));
//        List<Favourites> favouritesList = favouritesRepository.findByUserIdAndSidIn(
//                userId, sidList);
        if(ObjectUtil.isNotEmpty(favouritesList)){
            for (Favourites faSidList : favouritesList) {
                //sidFavourite装载了所有已收藏的文章sid
                sidFavourite.add(faSidList.getSid());
            }
        }
        // 把已经预警的装里边
        /*List<String> sidAlert = new ArrayList<>();
        List<AlertEntity> alertList = null;
        if (httpClient){
            alertList = AlertUtil.getAlerts(userId,sids,alertNetinsightUrl);
        }else {
            alertList = alertRepository.findByUserIdAndSidIn(
                    userId, sidList);
        }
        if (ObjectUtil.isNotEmpty(alertList)){
            for (AlertEntity alert : alertList) {
                sidAlert.add(alert.getSid());
            }
        }*/
        String TRSL = FavouritesUtil.buildSqlTF(sidList);
        if (TRSL == null) {
            return null;
        }
        QueryBuilder searchBuilder = new QueryBuilder();
//        if(timeStart!=null && timeEnd!=null){
//            searchBuilder = DateUtil.timeBuilder(timeStart+";"+timeEnd);
//        }
        sids = sids.replaceAll(","," OR ");
        if (sids.endsWith(" OR ")){
            sids = sids.substring(0,sids.length()-4);
        }
        searchBuilder.filterField(FtsFieldConst.FIELD_SID,sids,Operator.Equal);
        searchBuilder.filterByTRSL(TRSL);
        String trsl = "";
        if (StringUtil.isNotEmpty(keywords)) {
           trsl = new StringBuffer().append(FtsFieldConst.FIELD_TITLE)
                    .append(":\"").append(keywords).append("\" OR ")
                    .append(FtsFieldConst.FIELD_CONTENT).append(":\"")
                    .append(keywords).append("\"").toString();
            searchBuilder.filterByTRSL(trsl);
        }
        if (StringUtil.isNotEmpty(keywords)){
            searchBuilder.page(pageNo, pageSize);
        }else {
            searchBuilder.page(0, pageSize);
        }
        try {
           /* // 时间
            if (StringUtil.isNotEmpty(time)){
                searchBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
            }*/
            List<FtsDocumentTF> ftsQuery = hybase8SearchService.ftsQuery(searchBuilder, FtsDocumentTF.class,
                    false,false,false,null);
            List<FtsDocumentTF> result = new ArrayList<>();
            if (sidList.size() != ftsQuery.size()){
                sidList = new ArrayList<>();
            }
            for (FtsDocumentTF chartAnalyze : ftsQuery) {
                String id = chartAnalyze.getHkey();
                // 预警数据
               /* int indexOf = sidAlert.indexOf(id);
                if (indexOf < 0) {
                    chartAnalyze.setSend(false);
                } else {
                    chartAnalyze.setSend(true);
                }*/

                //是否收藏信息
                int indexOfFa = sidFavourite.indexOf(id);
                if (indexOfFa < 0) {
                    chartAnalyze.setFavourite(false);
                } else {
                    chartAnalyze.setFavourite(true);
                }

                // 我的收藏里边不用显示相似文章数了
                // 去掉img标签
                String content = chartAnalyze.getContent();
                if (StringUtil.isNotEmpty(content)) {
                    content = StringUtil.replaceImg(content).replace("　　", "");// 过滤空格
                    // img标签
                }
                chartAnalyze.setContent(content);
                result.add(chartAnalyze);

                if (sidList.size() != ftsQuery.size()){
                    sidList.add(chartAnalyze.getHkey());
                }
            }
            if (StringUtil.isNotEmpty(keywords) || StringUtil.isNotEmpty(time)) {
                QueryBuilder countBuilder = new QueryBuilder();
                countBuilder.filterByTRSL(TRSL);
                countBuilder.filterByTRSL(trsl);
                countBuilder.page(0,totalItemCount);
                countBuilder.setDatabase(Const.HYBASE_OVERSEAS);
                long ftsCount = hybase8SearchService.ftsCount(countBuilder, false, false,false,null);
                totalItemCount = new Long(ftsCount).intValue();
                totalList = totalItemCount % pageSize > 0 ? totalItemCount / pageSize + 1 : totalItemCount / pageSize;
            }
            List<FtsDocumentTF> resultByTime = FavouritesUtil.resultByTimeTF(result, sidList);
            return new InfoListResult<>(resultByTime, totalItemCount, totalList);
        } catch (Exception e) {
            throw new OperationException("检索异常：message:" + e.getMessage(), e);
        }
    }

    public Object findMaterialSourceByCondition(String libraryId, int pageNo, int pageSize,
                                          List<String> groupNameList, String keyword,String fuzzyValueScope, String invitationCard,
                                          String forwarPrimary,String time) throws TRSException{
        User loginUer = UserUtils.getUser();
        String start = DateUtil.formatTimeRange(time)[0];
        String end = DateUtil.formatTimeRange(time)[1];
        Date startTime = time == null?new Date():DateUtil.stringToDate(start,DateUtil.yyyyMMddHHmmss);
        Date endTime = time == null?new Date():DateUtil.stringToDate(end,DateUtil.yyyyMMddHHmmss);
        //获取用户可查询的数据源
        String groupNames = StringUtils.join(groupNameList,";");
        List<String> groupName = SourceUtil.getGroupNameList(groupNames);
        if(groupName == null || groupName.size() ==0){
            return null;
        }
        String source = StringUtils.join(groupName,";");
        Sort sort = new Sort(Sort.Direction.DESC, "urltime");
        PageRequest pageable = new PageRequest(pageNo, pageSize, sort);
        Page<Favourites> list = null;
        Specification<Favourites> criteria = new Specification<Favourites>() {
            @Override
            public Predicate toPredicate(Root<Favourites> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicate = new ArrayList<>();
                predicate.add(cb.equal(root.get("libraryId"), libraryId));
                predicate.add(cb.between(root.get("createdTime"),startTime,endTime));
                if(StringUtil.isNotEmpty(keyword) && StringUtil.isNotEmpty(fuzzyValueScope)){
                    List<Predicate> predicateKeyWord = new ArrayList<>();

                    switch (fuzzyValueScope){
                        case "title":
                            predicateKeyWord.add(cb.like(root.get("title"),"%"+keyword+"%"));
                            break;
                        case "source":
                            predicateKeyWord.add(cb.like(root.get("siteName"),"%"+keyword+"%"));
                            break;
                        case "author":
                            predicateKeyWord.add(cb.like(root.get("authors"),"%"+keyword+"%"));
                            break;
                        case "fullText":
                            predicateKeyWord.add(cb.like(root.get("title"),"%"+keyword+"%"));
                            predicateKeyWord.add(cb.like(root.get("content"),"%"+keyword+"%"));
                            break;
                    }

                    predicate.add(cb.or(predicateKeyWord.toArray(new Predicate[predicateKeyWord.size()])));
                }
                if( (StringUtil.isNotEmpty(forwarPrimary) && source.contains("微博")) ||
                        StringUtil.isNotEmpty(invitationCard) && source.contains("国内论坛")){
                    List<Predicate> predicateGroupName = new ArrayList<>();

                    if(StringUtil.isNotEmpty(forwarPrimary) && source.contains("微博")){
                        List<Predicate> predicateWeibo = new ArrayList<>();
                        predicateWeibo.add(cb.equal(root.get("groupName"), "微博"));
                        if ("primary".equals(forwarPrimary)) {
                            // 原发
                            predicateWeibo.add(cb.isNull(root.get("retweetedMid")));
                        }else  if ("forward".equals(forwarPrimary)){
                            //转发
                            predicateWeibo.add(cb.isNotNull(root.get("retweetedMid")));
                        }
                        for(int i = 0;i < groupName.size() ;i++){
                            if("微博".equals(groupName.get(i))){
                                groupName.remove(i);
                            }
                        }
                        predicateGroupName.add(cb.and(predicateWeibo.toArray(new Predicate[predicateWeibo.size()])));
                    }

                    if(StringUtil.isNotEmpty(invitationCard)  && source.contains("国内论坛")){
                        List<Predicate> predicateLuntan = new ArrayList<>();
                        predicateLuntan.add(cb.equal(root.get("groupName"), "国内论坛"));
                        if ("0".equals(invitationCard)) {
                            // 主贴
                            List<Predicate> predicateLuntan_zhutie = new ArrayList<>();
                            predicateLuntan_zhutie.add(cb.isNull(root.get("nreserved1")));
                            predicateLuntan_zhutie.add(cb.equal(root.get("nreserved1"),"0"));
                            predicateLuntan_zhutie.add(cb.equal(root.get("nreserved1"),""));
                            predicateLuntan.add(cb.or(predicateLuntan_zhutie.toArray(new Predicate[predicateLuntan_zhutie.size()])));
                        }else  if ("1".equals(invitationCard)){
                            //回帖
                            predicateLuntan.add(cb.equal(root.get("nreserved1"),"1"));
                        }
                        for(int i = 0;i < groupName.size() ;i++){
                            if("国内论坛".equals(groupName.get(i))){
                                groupName.remove(i);
                            }
                        }
                        predicateGroupName.add(cb.and(predicateLuntan.toArray(new Predicate[predicateLuntan.size()])));
                    }

                    if(groupName.size() > 0){
                        List<Predicate> predicatOtherGroupName = new ArrayList<>();
                        CriteriaBuilder.In<String> in = cb.in(root.get("groupName").as(String.class));
                        for(int i = 0; i<groupName.size();i++){
                            in.value(groupName.get(i));
                        }
                        predicatOtherGroupName.add(in);
                        predicateGroupName.add(cb.or(predicatOtherGroupName.toArray(new Predicate[predicatOtherGroupName.size()])));
                    }
                    predicate.add(cb.or(predicateGroupName.toArray(new Predicate[predicateGroupName.size()])));

                }else{
                    CriteriaBuilder.In<String> in = cb.in(root.get("groupName").as(String.class));
                    for(int i = 0; i<groupName.size();i++){
                        in.value(groupName.get(i));
                    }
                    predicate.add(in);
                }
                Predicate[] pre = new Predicate[predicate.size()];
                return query.where(predicate.toArray(pre)).getRestriction();
            }
        };

        list = favouritesRepository.findAll(criteria,pageable);

        if (ObjectUtil.isNotEmpty(list)) {
            list.forEach(item -> {
                if (item.getGroupName().equals("Twitter") || item.getGroupName().equals("Facebook") || item.getGroupName().equals("国内微信")){
                    item.setScreenName(item.getAuthors());
                }
                if (item.getGroupName().equals("微博")){
                    item.setAbstracts(item.getTitle());
                    item.setAuthor(item.getAuthors());
                }
                if(StringUtil.isEmpty(item.getUrltime()) && item.getUrlTime() != null) item.setUrltime(DateUtil.getDataToTime(item.getUrlTime()));//前端需要Urltime
                item.setUrlTitle(StringUtil.cutContentPro(StringUtil.replaceImg(subString(item.getUrlTitle())),Const.CONTENT_LENGTH));
                item.setStatusContent(StringUtil.cutContentPro(StringUtil.replaceImg(subString(item.getStatusContent())),Const.CONTENT_LENGTH));
                item.setContent(StringUtil.cutContentPro(StringUtil.replaceImg(subString(item.getContent())),Const.CONTENT_LENGTH));
                item.setTitle(StringUtil.cutContentPro(StringUtil.replaceImg(subString(item.getTitle())),Const.CONTENT_LENGTH));
            });
        }

        return new InfoListResult<>(list.getContent(), (int)list.getTotalElements(), list.getTotalPages());
    }

    public String subString(String str){
        if (StringUtil.isNotEmpty(str) && str.length() > 200) {
            str = str.substring(0, 200);
        }
        if (StringUtil.isEmpty(str)) str="";
        return str;
    }

    public Object changeHistoryMaterial() {
        Specification<Favourites> criteria = new Specification<Favourites>() {

            @Override
            public Predicate toPredicate(Root<Favourites> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Object> predicates = new ArrayList<>();
                predicates.add(cb.isNotNull(root.get("libraryId")));
                Predicate[] pre = new Predicate[predicates.size()];
                return query.where(predicates.toArray(pre)).getRestriction();
            }
        };

        List<Favourites> favouritesList = favouritesRepository.findAll(criteria);
        for (Favourites favourites : favouritesList) {
            if (ObjectUtil.isNotEmpty(favourites)) {

                if (Const.SOURCE_GROUPNAME_CONTRAST.containsKey(favourites.getGroupName())) {
                    String groupName = Const.SOURCE_GROUPNAME_CONTRAST.get(favourites.getGroupName());
                    favourites.setGroupName(groupName);
                }

                List<String> sidList = new ArrayList<>();
                sidList.add(favourites.getSid());
                List<FtsDocumentCommonVO> listF = new ArrayList<>();
                if (StringUtil.isNotEmpty(favourites.getContent())) {
                    favouritesRepository.save(favourites);
                } else {
                    try {
                        listF = (List<FtsDocumentCommonVO>) reportService.favouriteHybase(favourites.getSid(), sidList, favourites.getGroupName(), 0, 1);
                        if (listF.size() > 0) {
                            favourites.setTitle(subString(listF.get(0).getTitle()));
                            favourites.setContent(subString(listF.get(0).getContent()));
                            favourites.setUrlName(listF.get(0).getUrlName());
                            favourites.setUrlDate(listF.get(0).getUrlDate());
                            favourites.setAuthors(listF.get(0).getAuthors());
                            favourites.setSiteName(listF.get(0).getSiteName());
                            favourites.setNreserved1(listF.get(0).getNreserved1());
                            favourites.setAbstracts(subString(listF.get(0).getAbstracts()));
                            favourites.setCommtCount(listF.get(0).getCommtCount());
                            if(StringUtil.isNotEmpty(listF.get(0).getSrcName()) && listF.get(0).getSrcName().length() > 200){
                                listF.get(0).setSrcName(listF.get(0).getSrcName().substring(0,125));
                            }
                            favourites.setSrcName(listF.get(0).getSrcName());
                            favourites.setScreenName(listF.get(0).getScreenName());
                            favourites.setRttCount(listF.get(0).getRttCount());
                            favourites.setCreatedAt(listF.get(0).getCreatedAt());
                            favourites.setStatusContent(subString(listF.get(0).getStatusContent()));
                            favourites.setUrlTitle(subString(listF.get(0).getUrlTitle()));
                            favourites.setRetweetedMid(listF.get(0).getRetweetedMid());
                            favourites.setMdsTag(listF.get(0).getMd5Tag());
                            favourites.setUrlTime(listF.get(0).getUrlTime());
                            favouritesRepository.save(favourites);
                        } else {
                            favouritesRepository.delete(favourites);
                        }
                    } catch (TRSException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return Const.SUCCESS;
    }
}
