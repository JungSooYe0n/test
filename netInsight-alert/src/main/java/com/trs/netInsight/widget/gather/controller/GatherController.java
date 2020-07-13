package com.trs.netInsight.widget.gather.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonArray;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.template.GUIDGenerator;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.gather.entity.GatherPoint;
import com.trs.netInsight.widget.gather.entity.GatherPointOa;
import com.trs.netInsight.widget.gather.entity.repository.GatherRepository;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.httpclient.params.HostParams;
import org.apache.commons.httpclient.params.HttpParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * 信息采集controller
 */
@RestController
@RequestMapping("/gather")
@Api(description = "采集接口")
@Slf4j
public class GatherController {
    @Autowired
    private GatherRepository gatherRepository;
    @Value("${oa.uploadpoint}")
    private String uploadpoint;
    @Value("${oa.gatherinfo}")
    private String gatherInfo;
    @Value("${oa.file.path.news}")
    private String newsFile;
    @Value("${oa.file.path.newsapp}")
    private String newsappFile;

    @Value("${oa.file.path.dianzibao}")
    private String dianzibaoFile;

    @Value("${oa.file.path.media}")
    private String mediaFile;

    @Value("${oa.file.path.twitter}")
    private String twitterFile;

    @Value("${oa.file.path.weibo}")
    private String weiboFile;

    @Value("${oa.file.path.weixin}")
    private String weixinFile;
    @Value("${oa.file.path.yuan}")
    private String yuanFile;
    @Value("${oa.file.path.zimeiti}")
    private String zimeitiFile;
    @Value("${oa.file.path.all}")
    private String allFile;

    @ApiOperation("添加采集接口")
    @FormatResult
    @RequestMapping(value = "/addGatherPoint", method = RequestMethod.POST)
    public Object addGatherPoint(@ApiParam("任务名称") @RequestParam(value = "taskName", required = true) String taskName,
                                 @ApiParam("机构名称（运维账户才需添加）") @RequestParam(value = "organizationName", required = false) String organizationName,
                                 @ApiParam("数据类型") @RequestParam(value = "dataType", required = false) String[] dataType,
                                 @ApiParam("站点名称") @RequestParam(value = "siteName", required = false) String[] siteName,
                                 @ApiParam("频道名称") @RequestParam(value = "channelName", required = false) String[] channelName,
                                 @ApiParam("URL") @RequestParam(value = "urlName", required = false) String[] urlName,
                                 @ApiParam("账号名称") @RequestParam(value = "accountName", required = false) String[] accountName,
                                 @ApiParam("账号id") @RequestParam(value = "accountId", required = false) String[] accountId,
                                 @ApiParam("元搜索关键词") @RequestParam(value = "keyWord", required = false) String[] keyWord,
                                 @ApiParam("优先级") @RequestParam(value = "level", required = true) String[] level,
                                 @ApiParam("状态") @RequestParam(value = "status") String status
    ) throws TRSException {
        if (StringUtil.isEmpty(taskName)) {
            throw new TRSException("任务名称不能为空");
        }

        if (siteName.length > 0 && dataType.length != siteName.length) {
            throw new OperationException("所传dataType和siteName的个数不相同");
        }
        if (channelName.length > 0 && dataType.length != channelName.length) {
            throw new OperationException("所传dataType和channelName的个数不相同");
        }
        if (urlName.length > 0 && dataType.length != urlName.length) {
            throw new OperationException("所传dataType和urlName的个数不相同");
        }
        if (ObjectUtil.isNotEmpty(accountName) && dataType.length != accountName.length) {
            throw new OperationException("所传dataType和accountName的个数不相同");
        }
        if (ObjectUtil.isNotEmpty(accountId) && dataType.length != accountId.length) {
            throw new OperationException("所传dataType和accountId的个数不相同");
        }
        if (dataType.length != level.length) {
            throw new OperationException("所传dataType和level的个数不相同");
        }
            List<GatherPoint> gatherPointList = new ArrayList<>();
            User user = UserUtils.getUser();
            boolean isAdmin = false;
            if (UserUtils.ROLE_PLATFORM_SUPER_LIST.contains(user.getCheckRole())) {
                isAdmin = true;
                if (StringUtil.isEmpty(organizationName))
                    throw new OperationException("机构名称不能为空");
            }
            String taskId = GUIDGenerator.generate(GatherPoint.class);
            for (int i = 0; i < dataType.length; i++) {
                if (Const.GATHER_TYPE_NEWS.contains(dataType[i])) {
                    //新闻
                    if (StringUtil.isEmpty(siteName[i]) || StringUtil.isEmpty(channelName[i]) || StringUtil.isEmpty(urlName[i])) {
                        throw new TRSException(dataType[i] + ": 必填选项不能为空",1001);
                    }
                    GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType[i], taskName, taskId, siteName[i], channelName[i], urlName[i], "", "", new Date(), "", level[i]);
                    gatherPoint.setStatus(status);
                    gatherPoint.setGatherPointName(siteName[i] + "-" + channelName[i]);
                    gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                    gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                    gatherPointList.add(gatherPoint);
                } else if (Const.PAGE_SHOW_KEHUDUAN.equals(dataType[i])) {
                    //新闻App
                    if (StringUtil.isEmpty(siteName[i]) || StringUtil.isEmpty(channelName[i])) {
                        throw new TRSException(dataType[i] + ": 必填选项不能为空",1001);
                    }
                    GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType[i], taskName, taskId, siteName[i], channelName[i], "", "", "", new Date(), "", level[i]);
                    gatherPoint.setStatus(status);
                    gatherPoint.setGatherPointName(siteName[i] + "-" + channelName[i]);
                    gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                    gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                    gatherPointList.add(gatherPoint);
                } else if (Const.GATHER_MEDIA.contains(dataType[i])) {
                    if (StringUtil.isEmpty(siteName[i]) || StringUtil.isEmpty(accountId[i]) || StringUtil.isEmpty(urlName[i]) || StringUtil.isEmpty(accountName[i])) {
                        throw new TRSException(dataType[i] + ": 必填选项不能为空",1001);
                    }
                    GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType[i], taskName, taskId, siteName[i], "", urlName[i], accountName[i], accountId[i], new Date(), "", level[i]);
                    gatherPoint.setStatus(status);
                    gatherPoint.setGatherPointName(siteName[i] + "-" + accountName[i]);
                    gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                    gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                    gatherPointList.add(gatherPoint);
                } else if (Const.GATHER_ZIMEITI.contains(dataType[i])) {
                    if (StringUtil.isEmpty(siteName[i]) || StringUtil.isEmpty(accountName[i]) || StringUtil.isEmpty(urlName[i])) {
                        throw new TRSException(dataType[i] + ": 必填选项不能为空",1001);
                    }
                    GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType[i], taskName, taskId, siteName[i], "", urlName[i], accountName[i], "", new Date(), "", level[i]);
                    gatherPoint.setStatus(status);
                    gatherPoint.setGatherPointName(siteName[i] + "-" + accountName[i]);
                    gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                    gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                    gatherPointList.add(gatherPoint);
                } else if (Const.PAGE_SHOW_WEIBO.contains(dataType[i])) {
                    if (StringUtil.isEmpty(accountName[i]) || StringUtil.isEmpty(urlName[i])) {
                        throw new TRSException(dataType[i] + ": 必填选项不能为空",1001);
                    }
                    GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType[i], taskName, taskId, "", "", urlName[i], accountName[i], "", new Date(), "", level[i]);
                    gatherPoint.setStatus(status);
                    gatherPoint.setGatherPointName(accountName[i]);
                    gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                    gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                    gatherPointList.add(gatherPoint);
                } else if (Const.PAGE_SHOW_WEIXIN.contains(dataType[i])) {
                    if (StringUtil.isEmpty(accountId[i]) || StringUtil.isEmpty(accountName[i]) || StringUtil.isEmpty(urlName[i])) {
                        throw new TRSException(dataType[i] + ": 必填选项不能为空",1001);
                    }
                    GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType[i], taskName, taskId, "", "", urlName[i], accountName[i], accountId[i], new Date(), "", level[i]);
                    gatherPoint.setStatus(status);
                    gatherPoint.setGatherPointName(accountName[i] + "-" + accountId[i]);
                    gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                    gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                    gatherPointList.add(gatherPoint);
                } else if (Const.GATHER_TYPE_TWITTER.contains(dataType[i])) {
                    if(StringUtil.isEmpty(accountName[i]) || StringUtil.isEmpty(urlName[i])) {
                        throw new TRSException(dataType[i] + ": 必填选项不能为空",1001);
                    }
                    GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType[i], taskName, taskId, "", "", urlName[i], accountName[i], "", new Date(), "", level[i]);
                    gatherPoint.setStatus(status);
                    gatherPoint.setGatherPointName(accountName[i]);
                    gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                    gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                    gatherPointList.add(gatherPoint);
                } else if ("元搜索".equals(dataType[i])) {
                    if (StringUtil.isEmpty(siteName[i]) || StringUtil.isEmpty(keyWord[i])) {
                        throw new TRSException(dataType[i] + ": 必填选项不能为空",1001);
                    }
                    GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType[i], taskName, taskId, siteName[i], "", "", "", "", new Date(), keyWord[i], level[i]);
                    gatherPoint.setStatus(status);
                    gatherPoint.setGatherPointName(keyWord[i] + "-" + siteName[i]);
                    gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                    gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                    gatherPointList.add(gatherPoint);
                } else {
                    if (StringUtil.isEmpty(siteName[i]) || StringUtil.isEmpty(channelName[i]) || StringUtil.isEmpty(urlName[i])) {
                        throw new TRSException(dataType[i] + ": 必填选项不能为空",1001);
                    }
                    GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType[i], taskName, taskId, siteName[i], channelName[i], urlName[i], "", "", new Date(), "", level[i]);
                    gatherPoint.setStatus(status);
                    gatherPoint.setGatherPointName(siteName[i]);
                    gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                    gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                    gatherPointList.add(gatherPoint);
                }

            }
            gatherRepository.save(gatherPointList);
        return Const.SUCCESS;
    }

    @ApiOperation("删除采集点")
    @FormatResult
    @RequestMapping(value = "/delGatherPoint", method = RequestMethod.GET)
    public Object deleteGather(@RequestParam(value = "ids") String ids,
                               HttpServletRequest request) throws OperationException {
        try {
            String[] sidArry = ids.split(";");
            User loginUser = UserUtils.getUser();
            //原生sql
            Specification<GatherPoint> criteria = new Specification<GatherPoint>() {
                @Override
                public Predicate toPredicate(Root<GatherPoint> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

                    List<Predicate> predicates = new ArrayList<>();
                    if (UserUtils.ROLE_PLATFORM_SUPER_LIST.contains(loginUser.getCheckRole())) {
                        predicates.add(cb.equal(root.get("userId"), loginUser.getId()));
                    } else if (UserUtils.ROLE_ADMIN.equals(loginUser.getCheckRole())){
                        predicates.add(cb.equal(root.get("userId"), loginUser.getId()));
                    }else {
                        predicates.add(cb.equal(root.get("subGroupId"), loginUser.getSubGroupId()));
                    }
//                    predicates.add(cb.isNull(root.get("libraryId")));

                    CriteriaBuilder.In<Object> sid = cb.in(root.get("id"));
                    List<String> sids = Arrays.asList(sidArry);
                    if (ObjectUtil.isNotEmpty(sids)) {
                        for (String str : sids) {
                            if (StringUtil.isNotEmpty(str)) {
                                sid.value(str);
                            }
                        }
                        predicates.add(sid);
                    }

                    Predicate[] pre = new Predicate[predicates.size()];
                    return query.where(predicates.toArray(pre)).getRestriction();
                }
            };

            List<GatherPoint> findAll = gatherRepository.findAll(criteria);
            gatherRepository.delete(findAll);
            return Const.SUCCESS;
        } catch (Exception e) {
            log.error("删除采集点失败", e);
            throw new OperationException("删除采集点失败,message" + e);
        }
    }

    @ApiOperation("修改采集接口")
    @FormatResult
    @RequestMapping(value = "/updateGatherPoint", method = RequestMethod.GET)
    public Object updateGatherPoint(@ApiParam("采集点id") @RequestParam(value = "gatherId") String[] gatherId,
                                    @ApiParam("任务名称") @RequestParam(value = "taskName") String taskName,
                                    @ApiParam("任务id") @RequestParam(value = "taskId") String taskId,
                                    @ApiParam("是否是审核/true/false") @RequestParam(value = "isGoAdopt", defaultValue = "false", required = false) boolean isGoAdopt,
                                    @ApiParam("数据类型") @RequestParam(value = "dataType", required = false) String[] dataType,
                                    @ApiParam("站点名称") @RequestParam(value = "siteName", required = false) String[] siteName,
                                    @ApiParam("频道名称") @RequestParam(value = "channelName", required = false) String[] channelName,
                                    @ApiParam("URL") @RequestParam(value = "urlName", required = false) String[] urlName,
                                    @ApiParam("账号名称") @RequestParam(value = "accountName", required = false) String[] accountName,
                                    @ApiParam("账号id") @RequestParam(value = "accountId", required = false) String[] accountId,
                                    @ApiParam("元搜索关键词") @RequestParam(value = "keyWord", required = false) String[] keyWord,
                                    @ApiParam("优先级") @RequestParam(value = "level", required = false) String[] level,
                                    @ApiParam("状态") @RequestParam(value = "status") String[] status,
                                    HttpServletRequest request
    ) throws OperationException {
        if (siteName.length > 0 && dataType.length != siteName.length) {
            throw new OperationException("所传dataType和siteName的个数不相同");
        }
        if (channelName.length > 0 && dataType.length != channelName.length) {
            throw new OperationException("所传dataType和channelName的个数不相同");
        }
        if (urlName.length > 0 && dataType.length != urlName.length) {
            throw new OperationException("所传dataType和urlName的个数不相同");
        }
        if (accountName.length > 0 && dataType.length != accountName.length) {
            throw new OperationException("所传dataType和accountName的个数不相同");
        }
        if (accountId.length > 0 && dataType.length != accountId.length) {
            throw new OperationException("所传dataType和accountId的个数不相同");
        }
        if (dataType.length != level.length) {throw new OperationException("所传dataType和level的个数不相同");
        }
        try {
            List<GatherPoint> gatherPointList = new ArrayList<>();
            User loginUser = UserUtils.getUser();

            //原生sql
            Specification<GatherPoint> criteria = new Specification<GatherPoint>() {
                @Override
                public Predicate toPredicate(Root<GatherPoint> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

                    List<Predicate> predicates = new ArrayList<>();
                    if (UserUtils.ROLE_PLATFORM_SUPER_LIST.contains(loginUser.getCheckRole())) {
                        List<Predicate> predicateOrg = new ArrayList<>();
                        predicateOrg.add(cb.equal(root.get("userId"), loginUser.getId()));
                        if (isGoAdopt) {
                            Set<Organization> organizations = loginUser.getOrganizations();
                            if (organizations != null && organizations.size() > 0) {
                                for (Organization organization : organizations) {
                                    predicateOrg.add(cb.equal(root.get("organizationId"), organization.getId()));
                                }
                            }
                        }
                        predicates.add(cb.or(predicateOrg.toArray(new Predicate[predicateOrg.size()])));
                    } else if (UserUtils.ROLE_ADMIN.equals(loginUser.getCheckRole())){
                        predicates.add(cb.equal(root.get("userId"), loginUser.getId()));
                    }else {
                        predicates.add(cb.equal(root.get("subGroupId"), loginUser.getSubGroupId()));
                    }
//                    predicates.add(cb.isNull(root.get("libraryId")));
                    predicates.add(cb.equal(root.get("taskId"), taskId));

                    CriteriaBuilder.In<Object> sid = cb.in(root.get("id"));
                    List<String> sids = Arrays.asList(gatherId);
                    if (ObjectUtil.isNotEmpty(sids)) {
                        for (String str : sids) {
                            if (StringUtil.isNotEmpty(str)) {
                                sid.value(str);
                            }
                        }
                        predicates.add(sid);
                    }

                    Predicate[] pre = new Predicate[predicates.size()];
                    return query.where(predicates.toArray(pre)).getRestriction();
                }
            };

            List<GatherPoint> findAll = gatherRepository.findAll(criteria);

            for (GatherPoint gatherPoint : findAll) {
                for (int i = 0; i < dataType.length; i++) {
                    if (gatherId[i].equals(gatherPoint.getId())) {
                        if (Const.GATHER_TYPE_NEWS.contains(dataType[i])) {
                            //新闻
                            gatherPoint.setTaskName(taskName);
                            gatherPoint.setSiteName(siteName[i]);
                            gatherPoint.setChannelName(channelName[i]);
                            gatherPoint.setUrlName(urlName[i]);
                            gatherPoint.setStatus(status[i]);
                            gatherPoint.setCommitTime(new Date());
                            gatherPoint.setDataType(dataType[i]);
                            gatherPoint.setGatherPointName(siteName[i] + "-" + channelName[i]);
                            gatherPointList.add(gatherPoint);
                        } else if (Const.PAGE_SHOW_KEHUDUAN.equals(dataType[i])) {
                            //新闻App
                            gatherPoint.setTaskName(taskName);
                            gatherPoint.setSiteName(siteName[i]);
                            gatherPoint.setChannelName(channelName[i]);
                            gatherPoint.setGatherPointName(siteName[i] + "-" + channelName[i]);
                            gatherPoint.setStatus(status[i]);
                            gatherPoint.setCommitTime(new Date());
                            gatherPoint.setDataType(dataType[i]);
                            gatherPointList.add(gatherPoint);
                        } else if (Const.GATHER_MEDIA.contains(dataType[i])) {
                            gatherPoint.setTaskName(taskName);
                            gatherPoint.setSiteName(siteName[i]);
                            gatherPoint.setUrlName(urlName[i]);
                            gatherPoint.setAccountName(accountName[i]);
                            gatherPoint.setGatherPointName(siteName[i] + "-" + accountName[i]);
                            gatherPoint.setAccountId(accountId[i]);
                            gatherPoint.setLevel(level[i]);
                            gatherPoint.setStatus(status[i]);
                            gatherPoint.setCommitTime(new Date());
                            gatherPoint.setDataType(dataType[i]);
                            gatherPointList.add(gatherPoint);
                        } else if (Const.GATHER_ZIMEITI.contains(dataType[i])) {
                            gatherPoint.setTaskName(taskName);
                            gatherPoint.setSiteName(siteName[i]);
                            gatherPoint.setUrlName(urlName[i]);
                            gatherPoint.setAccountName(accountName[i]);
                            gatherPoint.setGatherPointName(siteName[i] + "-" + accountName[i]);
                            gatherPoint.setLevel(level[i]);
                            gatherPoint.setStatus(status[i]);
                            gatherPoint.setCommitTime(new Date());
                            gatherPoint.setDataType(dataType[i]);
                            gatherPointList.add(gatherPoint);
                        } else if (Const.PAGE_SHOW_WEIBO.contains(dataType[i])) {
                            gatherPoint.setTaskName(taskName);
                            gatherPoint.setUrlName(urlName[i]);
                            gatherPoint.setAccountName(accountName[i]);
                            gatherPoint.setGatherPointName(accountName[i]);
                            gatherPoint.setLevel(level[i]);
                            gatherPoint.setStatus(status[i]);
                            gatherPoint.setCommitTime(new Date());
                            gatherPoint.setDataType(dataType[i]);
                            gatherPointList.add(gatherPoint);
                        } else if (Const.PAGE_SHOW_WEIXIN.contains(dataType[i])) {
                            gatherPoint.setTaskName(taskName);
                            gatherPoint.setUrlName(urlName[i]);
                            gatherPoint.setAccountName(accountName[i]);
                            gatherPoint.setAccountId(accountId[i]);
                            gatherPoint.setLevel(level[i]);
                            gatherPoint.setStatus(status[i]);
                            gatherPoint.setGatherPointName(accountName[i] + "-" + accountId[i]);
                            gatherPoint.setCommitTime(new Date());
                            gatherPoint.setDataType(dataType[i]);
                            gatherPointList.add(gatherPoint);
                        } else if (Const.GATHER_TYPE_TWITTER.contains(dataType[i])) {
                            gatherPoint.setTaskName(taskName);
                            gatherPoint.setUrlName(urlName[i]);
                            gatherPoint.setAccountName(accountName[i]);
                            gatherPoint.setLevel(level[i]);
                            gatherPoint.setStatus(status[i]);
                            gatherPoint.setGatherPointName(accountName[i]);
                            gatherPoint.setCommitTime(new Date());
                            gatherPoint.setDataType(dataType[i]);
                            gatherPointList.add(gatherPoint);
                        } else if (Const.GATHER_YUAN.contains(dataType[i])) {
                            gatherPoint.setTaskName(taskName);
                            gatherPoint.setSiteName(siteName[i]);
                            gatherPoint.setKeyWord(keyWord[i]);
                            gatherPoint.setLevel(level[i]);
                            gatherPoint.setStatus(status[i]);
                            gatherPoint.setCommitTime(new Date());
                            gatherPoint.setGatherPointName(keyWord[i] + "-" + siteName[i]);
                            gatherPoint.setDataType(dataType[i]);
                            gatherPointList.add(gatherPoint);
                        }else {
                            gatherPoint.setTaskName(taskName);
                            gatherPoint.setSiteName(siteName[i]);
                            gatherPoint.setChannelName(channelName[i]);
                            gatherPoint.setUrlName(urlName[i]);
                            gatherPoint.setLevel(level[i]);
                            gatherPoint.setStatus(status[i]);
                            gatherPoint.setCommitTime(new Date());
                            gatherPoint.setDataType(dataType[i]);
                            gatherPoint.setGatherPointName(siteName[i]);
                            gatherPointList.add(gatherPoint);

                        }
                    } else {
                        gatherPoint.setTaskName(taskName);
                        gatherPointList.add(gatherPoint);
                    }
                }


            }
            gatherRepository.save(gatherPointList);
        } catch (Exception e) {
            log.error("修改采集失败", e);
            throw new OperationException("修改采集失败,message" + e);
        }
        return Const.SUCCESS;
    }

    @ApiOperation("采集列表接口")
    @FormatResult
    @RequestMapping(value = "/gatherList", method = RequestMethod.GET)
    public Object gatherList(
            @ApiParam("页数 从0开始") @RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
            @ApiParam("一页几条") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @ApiParam("机构名称（运维账户才需添加）") @RequestParam(value = "organizationName", required = false) String organizationName,
            @ApiParam("是否是审核/true/false") @RequestParam(value = "isGoAdopt", defaultValue = "false", required = false) boolean isGoAdopt,
            @ApiParam("任务名称") @RequestParam(value = "taskName", required = false) String taskName,
            @ApiParam("数据类型/ALL") @RequestParam(value = "dataType", required = false) String dataType,
            @ApiParam("优先级") @RequestParam(value = "level", required = false) String level,
            @ApiParam("状态") @RequestParam(value = "status", required = false) String status,
            @ApiParam("排序/desc/asc") @RequestParam(value = "sort", defaultValue = "desc", required = false) String sort,
            @ApiParam("在结果中搜索") @RequestParam(value = "fuzzyValue", required = false) String fuzzyValue,
            @ApiParam("状态") @RequestParam(value = "auditStatus", required = false) String auditStatus
    ) throws TRSException {
//防止前端乱输入
        pageSize = pageSize >= 1 ? pageSize : 10;
        User user = UserUtils.getUser();
        PageRequest pageable;
        if (sort.equals("desc")) {
            pageable = new PageRequest(pageNo, pageSize, new Sort(Sort.Direction.DESC, "commitTime"));
        } else {
            pageable = new PageRequest(pageNo, pageSize, new Sort(Sort.Direction.ASC, "commitTime"));
        }
        Page<GatherPoint> list = null;
        Specification<GatherPoint> criteria = new Specification<GatherPoint>() {
            @Override
            public Predicate toPredicate(Root<GatherPoint> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicate = new ArrayList<>();

                if (UserUtils.ROLE_PLATFORM_SUPER_LIST.contains(user.getCheckRole())) {
                    List<Predicate> predicateOrg = new ArrayList<>();
                    predicateOrg.add(cb.equal(root.get("userId"), user.getId()));
                    if (isGoAdopt) {
                        Set<Organization> organizations = user.getOrganizations();
                        if (organizations != null && organizations.size() > 0) {
                            for (Organization organization : organizations) {
                                predicateOrg.add(cb.equal(root.get("organizationId"), organization.getId()));
                            }
                        }
                    }
                    predicate.add(cb.or(predicateOrg.toArray(new Predicate[predicateOrg.size()])));
                    if (isGoAdopt)
                        predicate.add(cb.or(cb.equal(root.get("status"), "已提交"), cb.equal(root.get("status"), "采集中")));
                } else if (UserUtils.ROLE_ADMIN.equals(user.getCheckRole())){
                        predicate.add(cb.equal(root.get("userId"), user.getId()));
                }else {
                    predicate.add(cb.equal(root.get("subGroupId"), user.getSubGroupId()));
                }

//            predicate.add(cb.isNull(root.get("libraryId")));
                if (StringUtil.isNotEmpty(taskName) && !taskName.equals("ALL")) {
                    predicate.add(cb.equal(root.get("taskName"), taskName));
                }
                if (StringUtil.isNotEmpty(organizationName) && !organizationName.equals("ALL")) {
                    predicate.add(cb.equal(root.get("organizationName"), organizationName));
                }
                if (StringUtil.isNotEmpty(dataType) && !dataType.equals("ALL")) {
                    predicate.add(cb.equal(root.get("dataType"), dataType));
                }
                if (StringUtil.isNotEmpty(level) && !level.equals("ALL")) {
                    predicate.add(cb.equal(root.get("level"), level));
                }
                if (StringUtil.isNotEmpty(status) && !status.equals("ALL")) {
                    predicate.add(cb.equal(root.get("status"), status));
                }
                if (StringUtil.isNotEmpty(auditStatus) && !auditStatus.equals("ALL")) {
                    predicate.add(cb.equal(root.get("auditStatus"), auditStatus));
                }

                if (StringUtil.isNotEmpty(fuzzyValue)) {
                    List<Predicate> predicateKeyWord = new ArrayList<>();
                    predicateKeyWord.add(cb.like(root.get("taskName"), "%" + fuzzyValue + "%"));
                    predicateKeyWord.add(cb.like(root.get("siteName"), "%" + fuzzyValue + "%"));
                    predicateKeyWord.add(cb.like(root.get("dataType"), "%" + fuzzyValue + "%"));
                    predicateKeyWord.add(cb.like(root.get("level"),"%"+fuzzyValue+"%"));
                    predicateKeyWord.add(cb.like(root.get("status"), "%" + fuzzyValue + "%"));
                    predicateKeyWord.add(cb.like(root.get("remarks"), "%" + fuzzyValue + "%"));
                    if(isGoAdopt){
                        predicateKeyWord.add(cb.like(root.get("auditStatus"), "%" + fuzzyValue + "%"));
                        predicateKeyWord.add(cb.like(root.get("organizationName"), "%" + fuzzyValue + "%"));
                    }
                    predicate.add(cb.or(predicateKeyWord.toArray(new Predicate[predicateKeyWord.size()])));
                }

                Predicate[] pre = new Predicate[predicate.size()];
                return query.where(predicate.toArray(pre)).getRestriction();
            }
        };

        list = gatherRepository.findAll(criteria, pageable);
        return list;

    }
    @ApiOperation("提醒列表接口")
    @FormatResult
    @RequestMapping(value = "/remindList", method = RequestMethod.GET)
    public Object remindList(
            @ApiParam("页数 从0开始") @RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
            @ApiParam("一页几条") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) throws TRSException {
//防止前端乱输入
        pageSize = pageSize >= 1 ? pageSize : 10;
        User user = UserUtils.getUser();
        PageRequest pageable;
            pageable = new PageRequest(pageNo, pageSize, new Sort(Sort.Direction.DESC, "commitTime"));

        Page<GatherPoint> list = null;
        Specification<GatherPoint> criteria = new Specification<GatherPoint>() {
            @Override
            public Predicate toPredicate(Root<GatherPoint> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicate = new ArrayList<>();

                if (UserUtils.ROLE_PLATFORM_SUPER_LIST.contains(user.getCheckRole())) {
                    List<Predicate> predicateOrg = new ArrayList<>();
                    predicateOrg.add(cb.equal(root.get("userId"), user.getId()));

                    predicate.add(cb.or(predicateOrg.toArray(new Predicate[predicateOrg.size()])));

                } else if (UserUtils.ROLE_ADMIN.equals(user.getCheckRole())){
                    predicate.add(cb.equal(root.get("userId"), user.getId()));
                }else {
                    predicate.add(cb.equal(root.get("subGroupId"), user.getSubGroupId()));
                }
                predicate.add(cb.or(cb.equal(root.get("status"), "草稿"), cb.equal(root.get("status"), "采集中")));
                predicate.add(cb.equal(root.get("isNeedRemind"), true));
                Predicate[] pre = new Predicate[predicate.size()];
                return query.where(predicate.toArray(pre)).getRestriction();
            }
        };

        list = gatherRepository.findAll(criteria, pageable);
        List<GatherPoint> gatherPointList = new ArrayList<>();
        if (UserUtils.ROLE_PLATFORM_SUPER_LIST.contains(user.getCheckRole())) {
            gatherPointList = gatherRepository.findByUserId(user.getId());
        } else if (UserUtils.ROLE_ADMIN.equals(user.getCheckRole())){
            gatherPointList = gatherRepository.findByUserId(user.getId());
        }else {
            gatherPointList = gatherRepository.findBySubGroupId(user.getSubGroupId());
        }
        for (GatherPoint gatherPoint: gatherPointList) {
            gatherPoint.setNeedRemind(false);
        }
        gatherRepository.save(gatherPointList);
        return list;

    }

    @ApiOperation("采集点详情接口")
    @FormatResult
    @RequestMapping(value = "/gatherDetail", method = RequestMethod.GET)
    public Object gatherDetail(@ApiParam("采集点id") @RequestParam(value = "gatherId", required = true) String[] gatherId,
                               HttpServletRequest request
    ) throws OperationException {

        try {
            List<GatherPoint> gatherPointList = new ArrayList<>();
            User user = UserUtils.getUser();
            HashMap hashMap = new HashMap();

            GatherPoint gatherPoint = gatherRepository.findOne(gatherId[0]);
            hashMap.put("gatherInfo", gatherInfo);
            return hashMap;

        } catch (Exception e) {
            log.error("修改采集失败", e);
            throw new OperationException("修改采集失败,message" + e);
        }

    }

    @ApiOperation("是否通过采集接口")
    @FormatResult
    @RequestMapping(value = "/isAdoptGather", method = RequestMethod.GET)
    public Object isAdoptGather(@ApiParam("采集点id") @RequestParam(value = "gatherId", required = true) String[] gatherId,
                                @ApiParam("是否通过/true/false") @RequestParam(value = "isAdopt", required = true) boolean isAdopt,
                                @ApiParam("备注/退回时候添加") @RequestParam(value = "remarks", defaultValue = "", required = false) String remarks,
                                HttpServletRequest request
    ) throws TRSException {

            List<GatherPoint> gatherPointList = new ArrayList<>();
            User user = UserUtils.getUser();
            if (!UserUtils.ROLE_PLATFORM_SUPER_LIST.contains(user.getCheckRole())) {
                throw new TRSException("您没有权限进行该操作");
            }

            //原生sql
            Specification<GatherPoint> criteria = new Specification<GatherPoint>() {
                @Override
                public Predicate toPredicate(Root<GatherPoint> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

                    List<Predicate> predicates = new ArrayList<>();
                    CriteriaBuilder.In<Object> sid = cb.in(root.get("id"));
                    List<String> sids = Arrays.asList(gatherId);
                    if (ObjectUtil.isNotEmpty(sids)) {
                        for (String str : sids) {
                            if (StringUtil.isNotEmpty(str)) {
                                sid.value(str);
                            }
                        }
                        predicates.add(sid);
                    }

                    Predicate[] pre = new Predicate[predicates.size()];
                    return query.where(predicates.toArray(pre)).getRestriction();
                }
            };

            List<GatherPoint> findAll = gatherRepository.findAll(criteria);
            List<GatherPointOa> gatherPointOaList = new ArrayList<>();

            List<HashMap<String, Object>> hashMaps = new ArrayList<>();
        List<GatherPointOa> newsoalist = new ArrayList<>();
        List<GatherPointOa> luntanoalist = new ArrayList<>();
        List<GatherPointOa> bokeoalist = new ArrayList<>();
        List<GatherPointOa> dianzibaooalist = new ArrayList<>();
        List<GatherPointOa> appoalist = new ArrayList<>();
        List<GatherPointOa> mediaoalist = new ArrayList<>();
        List<GatherPointOa> duanmediaoalist = new ArrayList<>();
        List<GatherPointOa> zimeitioalist = new ArrayList<>();
        List<GatherPointOa> weibooalist = new ArrayList<>();
        List<GatherPointOa> weixinoalist = new ArrayList<>();
        List<GatherPointOa> twitteroalist = new ArrayList<>();
        List<GatherPointOa> yuanoalist = new ArrayList<>();
        List<GatherPointOa> facebookoalist = new ArrayList<>();
        List<GatherPointOa> jingwaioalist = new ArrayList<>();
        List<GatherPointOa> qitaoalist = new ArrayList<>();

        List<GatherPoint> newslist = new ArrayList<>();
        List<GatherPoint> luntanlist = new ArrayList<>();
        List<GatherPoint> bokelist = new ArrayList<>();
        List<GatherPoint> dianzibaolist = new ArrayList<>();
        List<GatherPoint> applist = new ArrayList<>();
        List<GatherPoint> medialist = new ArrayList<>();
        List<GatherPoint> duanmedialist = new ArrayList<>();
        List<GatherPoint> zimeitilist = new ArrayList<>();
        List<GatherPoint> weibolist = new ArrayList<>();
        List<GatherPoint> weixinlist = new ArrayList<>();
        List<GatherPoint> twitterlist = new ArrayList<>();
        List<GatherPoint> yuanlist = new ArrayList<>();
        List<GatherPoint> facebooklist = new ArrayList<>();
        List<GatherPoint> jingwailist = new ArrayList<>();
        List<GatherPoint> qitalist = new ArrayList<>();


            for (GatherPoint gatherPoint : findAll) {
                if (isAdopt) {
//                    gatherPoint.setStatus("采集中");
//                    gatherPoint.setAuditStatus(Const.GATHER_AUDITED);
//                    gatherPoint.setAuditUserName(user.getUserName());
//                    gatherPoint.setNeedRemind(true);
//                    gatherPoint.setAuditTime(new Date());
                    gatherPointList.add(gatherPoint);
                        if (Const.PAGE_SHOW_XINWEN.equals(gatherPoint.getDataType())){
                            GatherPointOa gatherPointOa = new GatherPointOa(gatherPoint.getSiteName(), gatherPoint.getChannelName(), gatherPoint.getUrlName(), "", gatherPoint.getLevel(), gatherPoint.getId(),gatherPoint.getOrganizationName(),gatherPoint.getUserAccount());
                            gatherPointOaList.add(gatherPointOa);
                            newslist.add(gatherPoint);
                            newsoalist.add(gatherPointOa);
                        }else if (Const.PAGE_SHOW_LUNTAN.equals(gatherPoint.getDataType())){
                            GatherPointOa gatherPointOa = new GatherPointOa(gatherPoint.getSiteName(), gatherPoint.getChannelName(), gatherPoint.getUrlName(), "", gatherPoint.getLevel(), gatherPoint.getId(),gatherPoint.getOrganizationName(),gatherPoint.getUserAccount());
                            gatherPointOaList.add(gatherPointOa);
                            luntanlist.add(gatherPoint);
                            luntanoalist.add(gatherPointOa);
                        }else if (Const.PAGE_SHOW_BOKE.equals(gatherPoint.getDataType())){
                            GatherPointOa gatherPointOa = new GatherPointOa(gatherPoint.getSiteName(), gatherPoint.getChannelName(), gatherPoint.getUrlName(), "", gatherPoint.getLevel(), gatherPoint.getId(),gatherPoint.getOrganizationName(),gatherPoint.getUserAccount());
                            gatherPointOaList.add(gatherPointOa);
                            bokelist.add(gatherPoint);
                            bokeoalist.add(gatherPointOa);
                        }else if (Const.PAGE_SHOW_DIANZIBAO.equals(gatherPoint.getDataType())){
                            GatherPointOa gatherPointOa = new GatherPointOa(gatherPoint.getSiteName(), gatherPoint.getChannelName(), gatherPoint.getUrlName(), "", gatherPoint.getLevel(), gatherPoint.getId(),gatherPoint.getOrganizationName(),gatherPoint.getUserAccount());
                            gatherPointOaList.add(gatherPointOa);
                            dianzibaolist.add(gatherPoint);
                            dianzibaooalist.add(gatherPointOa);
                        } else if (Const.PAGE_SHOW_KEHUDUAN.equals(gatherPoint.getDataType())) {
                        //新闻App
                        GatherPointOa gatherPointOa = new GatherPointOa(gatherPoint.getSiteName(), gatherPoint.getChannelName(), "", "", gatherPoint.getLevel(), gatherPoint.getId(),gatherPoint.getOrganizationName(),gatherPoint.getUserAccount());
                        gatherPointOaList.add(gatherPointOa);
                        applist.add(gatherPoint);
                            appoalist.add(gatherPointOa);

                    } else if (Const.GATHER_MEDIA.contains(gatherPoint.getDataType())) {
                        GatherPointOa gatherPointOa = new GatherPointOa(gatherPoint.getSiteName(), gatherPoint.getAccountName(), gatherPoint.getAccountId(), gatherPoint.getUrlName(), gatherPoint.getLevel(), gatherPoint.getId(),gatherPoint.getOrganizationName(),gatherPoint.getUserAccount());
                        gatherPointOaList.add(gatherPointOa);

                        if (Const.PAGE_SHOW_CHANGSHIPIN.equals(gatherPoint.getDataType())){
                            medialist.add(gatherPoint);
                            mediaoalist.add(gatherPointOa);
                        }else {
                            duanmedialist.add(gatherPoint);
                            duanmediaoalist.add(gatherPointOa);
                        }

                    } else if (Const.GATHER_ZIMEITI.contains(gatherPoint.getDataType())) {
                        GatherPointOa gatherPointOa = new GatherPointOa(gatherPoint.getSiteName(), gatherPoint.getAccountName(), gatherPoint.getUrlName(), "", gatherPoint.getLevel(), gatherPoint.getId(),gatherPoint.getOrganizationName(),gatherPoint.getUserAccount());
                        gatherPointOaList.add(gatherPointOa);
                        zimeitilist.add(gatherPoint);
                            zimeitioalist.add(gatherPointOa);


                    } else if (Const.PAGE_SHOW_WEIBO.contains(gatherPoint.getDataType())) {
                        GatherPointOa gatherPointOa = new GatherPointOa(gatherPoint.getAccountName(), gatherPoint.getUrlName(), "", "", gatherPoint.getLevel(), gatherPoint.getId(),gatherPoint.getOrganizationName(),gatherPoint.getUserAccount());
                        gatherPointOaList.add(gatherPointOa);
                        weibolist.add(gatherPoint);
                            weibooalist.add(gatherPointOa);

                    } else if (Const.PAGE_SHOW_WEIXIN.contains(gatherPoint.getDataType())) {
                        GatherPointOa gatherPointOa = new GatherPointOa(gatherPoint.getAccountName(), gatherPoint.getAccountId(), gatherPoint.getUrlName(), "", gatherPoint.getLevel(), gatherPoint.getId(),gatherPoint.getOrganizationName(),gatherPoint.getUserAccount());
                        gatherPointOaList.add(gatherPointOa);

                        weixinlist.add(gatherPoint);
                            weixinoalist.add(gatherPointOa);
                    } else if (Const.GATHER_TYPE_TWITTER.contains(gatherPoint.getDataType())) {
                        GatherPointOa gatherPointOa = new GatherPointOa(gatherPoint.getAccountName(), gatherPoint.getUrlName(), "", "", gatherPoint.getLevel(), gatherPoint.getId(),gatherPoint.getOrganizationName(),gatherPoint.getUserAccount());
                        gatherPointOaList.add(gatherPointOa);
                        twitterlist.add(gatherPoint);
                            twitteroalist.add(gatherPointOa);

                    } else if ("元搜索".equals(gatherPoint.getDataType())) {
                        GatherPointOa gatherPointOa = new GatherPointOa(gatherPoint.getKeyWord(), gatherPoint.getSiteName(), "", "", gatherPoint.getLevel(), gatherPoint.getId(),gatherPoint.getOrganizationName(),gatherPoint.getUserAccount());
                        gatherPointOaList.add(gatherPointOa);
                        yuanlist.add(gatherPoint);
                            yuanoalist.add(gatherPointOa);
                    } else {
                        //境外 facebook 其他
                        GatherPointOa gatherPointOa = new GatherPointOa(gatherPoint.getSiteName(), gatherPoint.getChannelName(), gatherPoint.getUrlName(), "", gatherPoint.getLevel(), gatherPoint.getId(),gatherPoint.getOrganizationName(),gatherPoint.getUserAccount());
                        gatherPointOaList.add(gatherPointOa);
                        if (Const.PAGE_SHOW_GUOWAIXINWEN.equals(gatherPoint.getDataType())){
                            jingwailist.add(gatherPoint);
                            jingwaioalist.add(gatherPointOa);
                        }else if (Const.PAGE_SHOW_FACEBOOK.equals(gatherPoint.getDataType())){
                            facebooklist.add(gatherPoint);
                            facebookoalist.add(gatherPointOa);
                        }else {
                            qitalist.add(gatherPoint);
                            qitaoalist.add(gatherPointOa);
                        }
                    }

                } else {
                    if ("采集中".equals(gatherPoint.getStatus())){
                        throw new TRSException("“采集中”状态下无法退回");
                    }
                    gatherPoint.setStatus("草稿");
                    gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                    gatherPoint.setRemarks(remarks);
                    gatherPoint.setAuditUserName(user.getUserName());
                    gatherPoint.setAuditTime(new Date());
                    gatherPoint.setNeedRemind(true);
                    gatherPointList.add(gatherPoint);
                }
            }
            if (isAdopt) {
                if (ObjectUtil.isNotEmpty(newsoalist)){
                    HashMap hashMap = new HashMap();
                    hashMap.put("radarType", CommonListChartUtil.changeGroupName(Const.PAGE_SHOW_XINWEN));
                    hashMap.put("dateStr", newsoalist);
                    hashMaps.add(hashMap);
                }
                if (ObjectUtil.isNotEmpty(luntanoalist)){
                    HashMap hashMap = new HashMap();
                    hashMap.put("radarType", CommonListChartUtil.changeGroupName(Const.PAGE_SHOW_LUNTAN));
                    hashMap.put("dateStr", luntanoalist);
                    hashMaps.add(hashMap);
                }
                if (ObjectUtil.isNotEmpty(bokeoalist)){
                    HashMap hashMap = new HashMap();
                    hashMap.put("radarType", CommonListChartUtil.changeGroupName(Const.PAGE_SHOW_BOKE));
                    hashMap.put("dateStr", bokeoalist);
                    hashMaps.add(hashMap);
                }
                if (ObjectUtil.isNotEmpty(dianzibaooalist)){
                    HashMap hashMap = new HashMap();
                    hashMap.put("radarType", CommonListChartUtil.changeGroupName(Const.PAGE_SHOW_DIANZIBAO));
                    hashMap.put("dateStr", dianzibaooalist);
                    hashMaps.add(hashMap);
                }
                if (ObjectUtil.isNotEmpty(appoalist)){
                    HashMap hashMap = new HashMap();
                    hashMap.put("radarType", CommonListChartUtil.changeGroupName(Const.PAGE_SHOW_KEHUDUAN));
                    hashMap.put("dateStr", appoalist);
                    hashMaps.add(hashMap);
                }
                if (ObjectUtil.isNotEmpty(mediaoalist)){
                    HashMap hashMap = new HashMap();
                    hashMap.put("radarType", CommonListChartUtil.changeGroupName(Const.PAGE_SHOW_CHANGSHIPIN));
                    hashMap.put("dateStr", mediaoalist);
                    hashMaps.add(hashMap);
                }
                if (ObjectUtil.isNotEmpty(duanmediaoalist)){
                    HashMap hashMap = new HashMap();
                    hashMap.put("radarType", CommonListChartUtil.changeGroupName(Const.PAGE_SHOW_DUANSHIPIN));
                    hashMap.put("dateStr", duanmediaoalist);
                    hashMaps.add(hashMap);
                }
                if (ObjectUtil.isNotEmpty(zimeitioalist)){
                    HashMap hashMap = new HashMap();
                    hashMap.put("radarType", CommonListChartUtil.changeGroupName(Const.PAGE_SHOW_ZIMEITI));
                    hashMap.put("dateStr", zimeitioalist);
                    hashMaps.add(hashMap);
                }
                if (ObjectUtil.isNotEmpty(weibooalist)){
                    HashMap hashMap = new HashMap();
                    hashMap.put("radarType", CommonListChartUtil.changeGroupName(Const.PAGE_SHOW_WEIBO));
                    hashMap.put("dateStr", weibooalist);
                    hashMaps.add(hashMap);
                }
                if (ObjectUtil.isNotEmpty(weixinoalist)){
                    HashMap hashMap = new HashMap();
                    hashMap.put("radarType", CommonListChartUtil.changeGroupName(Const.PAGE_SHOW_WEIXIN));
                    hashMap.put("dateStr", weixinoalist);
                    hashMaps.add(hashMap);
                }
                if (ObjectUtil.isNotEmpty(twitteroalist)){
                    HashMap hashMap = new HashMap();
                    hashMap.put("radarType", CommonListChartUtil.changeGroupName(Const.PAGE_SHOW_TWITTER));
                    hashMap.put("dateStr", twitteroalist);
                    hashMaps.add(hashMap);
                }
                if (ObjectUtil.isNotEmpty(yuanoalist)){
                    HashMap hashMap = new HashMap();
                    hashMap.put("radarType", "元搜索");
                    hashMap.put("dateStr", yuanoalist);
                    hashMaps.add(hashMap);
                }
                if (ObjectUtil.isNotEmpty(jingwaioalist)){
                    HashMap hashMap = new HashMap();
                    hashMap.put("radarType", CommonListChartUtil.changeGroupName(Const.PAGE_SHOW_GUOWAIXINWEN));
                    hashMap.put("dateStr", jingwaioalist);
                    hashMaps.add(hashMap);
                }
                if (ObjectUtil.isNotEmpty(facebookoalist)){
                    HashMap hashMap = new HashMap();
                    hashMap.put("radarType", CommonListChartUtil.changeGroupName(Const.PAGE_SHOW_FACEBOOK));
                    hashMap.put("dateStr", facebookoalist);
                    hashMaps.add(hashMap);
                }
                if (ObjectUtil.isNotEmpty(qitaoalist)){
                    HashMap hashMap = new HashMap();
                    hashMap.put("radarType", Const.GATHER_QITA);
                    hashMap.put("dateStr", qitaoalist);
                    hashMaps.add(hashMap);
                }
                JSONArray js = JSONArray.fromObject(hashMaps);
                log.error(js.toString());
//                String sendPost = HttpUtil.sendPost(uploadpoint, "data="+js.toString());
                Map<String,String> map = new HashMap<>();
                map.put("data",js.toString());
//                params.setParameter("data",js.toString());
                String sendPost = HttpUtil.oaRelateEtl(uploadpoint, map);
                JSONObject jsonObject = JSONObject.parseObject(sendPost);
                if (jsonObject.getBoolean("success")) {
                    for (GatherPoint gatherPoint : gatherPointList){
                        gatherPoint.setStatus("采集中");
                        gatherPoint.setAuditStatus(Const.GATHER_AUDITED);
                        gatherPoint.setAuditUserName(user.getUserName());
                        gatherPoint.setNeedRemind(true);
                        gatherPoint.setAuditTime(new Date());
                    }
                    gatherRepository.save(gatherPointList);
                    return jsonObject.getString("msg");
                } else {
                com.alibaba.fastjson.JSONArray dataIds = jsonObject.getJSONArray("data");
                    List<GatherPoint> gatherPointList1 = new ArrayList<>();
                    List<GatherPoint> gatherPointList2 = new ArrayList<>();
                    for (GatherPoint gatherPoint: gatherPointList) {
                        int is = 0;
                        for (int i = 0; i < dataIds.size(); i++) {
                            if (gatherPoint.getId().equals(dataIds.getString(i))){
                                is = 1;
                                continue;
                            }
                        }
                        if (is == 1){
                            gatherPoint.setStatus("采集中");
                            gatherPoint.setAuditStatus(Const.GATHER_AUDITED);
                            gatherPoint.setAuditUserName(user.getUserName());
                            gatherPoint.setNeedRemind(true);
                            gatherPoint.setAuditTime(new Date());
                            gatherPointList1.add(gatherPoint);
                        }else {
//                            gatherPoint.setStatus("采集中");
                            gatherPoint.setAuditStatus(Const.GATHER_AUDITING);
                            gatherPoint.setAuditUserName(user.getUserName());
                            gatherPoint.setNeedRemind(true);
                            gatherPoint.setAuditTime(new Date());
                            gatherPointList2.add(gatherPoint);
                        }
                    }
                    gatherRepository.save(gatherPointList1);
                    gatherRepository.save(gatherPointList2);
                    String msg = jsonObject.getString("msg");
                    throw new TRSException(msg,1001);
                  /*  String[] msgs = msg.split(";");
                    for (String str: msgs) {
                        if (str.contains("需求上传成功")) {
                            String dataString = str.substring(0, str.length() - 7);
                            if (Const.PAGE_SHOW_XINWEN.equals(CommonListChartUtil.formatPageShowGroupName(dataString))) {
                                gatherRepository.save(newslist);
                            } else if (Const.PAGE_SHOW_LUNTAN.equals(CommonListChartUtil.formatPageShowGroupName(dataString))) {
                                gatherRepository.save(luntanlist);
                            } else if (Const.PAGE_SHOW_BOKE.equals(CommonListChartUtil.formatPageShowGroupName(dataString))) {
                                gatherRepository.save(bokelist);
                            } else if (Const.PAGE_SHOW_DIANZIBAO.equals(CommonListChartUtil.formatPageShowGroupName(dataString))) {
                                gatherRepository.save(dianzibaolist);
                            } else if (Const.PAGE_SHOW_KEHUDUAN.equals(CommonListChartUtil.formatPageShowGroupName(dataString))) {
                                //新闻App
                                gatherRepository.save(applist);
                            } else if (Const.GATHER_MEDIA.contains(CommonListChartUtil.formatPageShowGroupName(dataString))) {
                                if (Const.PAGE_SHOW_CHANGSHIPIN.equals(CommonListChartUtil.formatPageShowGroupName(dataString))) {
                                    gatherRepository.save(medialist);
                                } else {
                                    gatherRepository.save(duanmedialist);
                                }

                            } else if (Const.GATHER_ZIMEITI.contains(CommonListChartUtil.formatPageShowGroupName(dataString))) {
                                gatherRepository.save(zimeitilist);

                            } else if (Const.PAGE_SHOW_WEIBO.contains(CommonListChartUtil.formatPageShowGroupName(dataString))) {
                                gatherRepository.save(weibolist);
                            } else if (Const.PAGE_SHOW_WEIXIN.contains(CommonListChartUtil.formatPageShowGroupName(dataString))) {
                                gatherRepository.save(weixinlist);
                            } else if (Const.GATHER_TYPE_TWITTER.contains(CommonListChartUtil.formatPageShowGroupName(dataString))) {
                                gatherRepository.save(twitterlist);
                            } else if ("元搜索".equals(dataString)) {
                                gatherRepository.save(yuanlist);
                            } else {
                                //境外 facebook 其他
                                if (Const.PAGE_SHOW_GUOWAIXINWEN.equals(CommonListChartUtil.formatPageShowGroupName(dataString))) {
                                    gatherRepository.save(jingwailist);
                                } else if (Const.PAGE_SHOW_FACEBOOK.equals(CommonListChartUtil.formatPageShowGroupName(dataString))) {
                                    gatherRepository.save(facebooklist);
                                } else if (Const.GATHER_QITA.equals(dataString)){
                                    gatherRepository.save(qitalist);
                                }
                            }
                        }
                    }
                   throw new TRSException(msg,1001);*/
                }
            }else {
                gatherRepository.save(gatherPointList);
            }
        return Const.SUCCESS;
    }

    @ApiOperation("查看采集点变更为审核中接口")
    @FormatResult
    @RequestMapping(value = "/auditIngGather", method = RequestMethod.GET)
    public Object auditIngGather(@ApiParam("采集点id") @RequestParam(value = "gatherId", required = true) String[] gatherId,
                                 HttpServletRequest request
    ) throws OperationException {

        try {
            List<GatherPoint> gatherPointList = new ArrayList<>();
            User user = UserUtils.getUser();
            if (!UserUtils.ROLE_PLATFORM_SUPER_LIST.contains(user.getCheckRole())) {
                throw new OperationException("您没有权限进行该操作");
            }
            //原生sql
            Specification<GatherPoint> criteria = new Specification<GatherPoint>() {
                @Override
                public Predicate toPredicate(Root<GatherPoint> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

                    List<Predicate> predicates = new ArrayList<>();
                    CriteriaBuilder.In<Object> sid = cb.in(root.get("id"));
                    List<String> sids = Arrays.asList(gatherId);
                    if (ObjectUtil.isNotEmpty(sids)) {
                        for (String str : sids) {
                            if (StringUtil.isNotEmpty(str)) {
                                sid.value(str);
                            }
                        }
                        predicates.add(sid);
                    }

                    Predicate[] pre = new Predicate[predicates.size()];
                    return query.where(predicates.toArray(pre)).getRestriction();
                }
            };

            List<GatherPoint> findAll = gatherRepository.findAll(criteria);

            for (GatherPoint gatherPoint : findAll) {
                gatherPoint.setAuditStatus(Const.GATHER_AUDITING);
                gatherPointList.add(gatherPoint);
            }
            gatherRepository.save(gatherPointList);
        } catch (Exception e) {
            log.error("修改采集失败", e);
            throw new OperationException("修改采集失败,message" + e);
        }
        return Const.SUCCESS;
    }

    @ApiOperation("获取任务名称")
    @FormatResult
    @RequestMapping(value = "/getTaskNameList", method = RequestMethod.GET)
    public Object getTaskNameList(HttpServletRequest request) throws OperationException {

        try {
            User user = UserUtils.getUser();
            //原生sql
            Specification<GatherPoint> criteria = new Specification<GatherPoint>() {
                @Override
                public Predicate toPredicate(Root<GatherPoint> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

                    List<Predicate> predicates = new ArrayList<>();
                    if (UserUtils.ROLE_PLATFORM_SUPER_LIST.contains(user.getCheckRole())) {
                        List<Predicate> predicateOrg = new ArrayList<>();
                        predicateOrg.add(cb.equal(root.get("userId"), user.getId()));
                        Set<Organization> organizations = user.getOrganizations();
                        if (organizations != null && organizations.size() > 0) {
                            for (Organization organization : organizations) {
                                predicateOrg.add(cb.equal(root.get("organizationId"), organization.getId()));
                            }
                        }
                        predicates.add(cb.or(predicateOrg.toArray(new Predicate[predicateOrg.size()])));
                        predicates.add(cb.or(cb.equal(root.get("status"), "已提交"), cb.equal(root.get("status"), "采集中")));
                    }else if (UserUtils.ROLE_ADMIN.equals(user.getCheckRole())){
                        predicates.add(cb.equal(root.get("userId"), user.getId()));
                    }else {
                        predicates.add(cb.equal(root.get("subGroupId"), user.getSubGroupId()));
                    }
                    Predicate[] pre = new Predicate[predicates.size()];
                    return query.where(predicates.toArray(pre)).getRestriction();
                }
            };

            List<GatherPoint> findAll = gatherRepository.findAll(criteria);
            List<String> taskNames = new ArrayList<>();
            for (GatherPoint gatherPoint : findAll) {
                boolean isSame = false;
                for (int i = 0; i < taskNames.size(); i++) {
                    if (gatherPoint.getTaskName().equals(taskNames.get(i))) {
                        isSame = true;
                        continue;
                    }
                }
                if (!isSame) {
                    taskNames.add(gatherPoint.getTaskName());
                }
            }
            return taskNames;

        } catch (Exception e) {
            log.error("获取失败", e);
            throw new OperationException("获取失败,message" + e);
        }
    }

    @ApiOperation("获取机构名称")
    @FormatResult
    @RequestMapping(value = "/getOrganizationNameList", method = RequestMethod.GET)
    public Object getOrganizationNameList(HttpServletRequest request) throws OperationException {

        try {
            User user = UserUtils.getUser();
            //原生sql
            Specification<GatherPoint> criteria = new Specification<GatherPoint>() {
                @Override
                public Predicate toPredicate(Root<GatherPoint> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

                    List<Predicate> predicates = new ArrayList<>();
                    if (UserUtils.ROLE_PLATFORM_SUPER_LIST.contains(user.getCheckRole())) {
                        List<Predicate> predicateOrg = new ArrayList<>();
                        predicateOrg.add(cb.equal(root.get("userId"), user.getId()));
                        Set<Organization> organizations = user.getOrganizations();
                        if (organizations != null && organizations.size() > 0) {
                            for (Organization organization : organizations) {
                                predicateOrg.add(cb.equal(root.get("organizationId"), organization.getId()));
                            }
                        }
                        predicates.add(cb.or(predicateOrg.toArray(new Predicate[predicateOrg.size()])));
                        predicates.add(cb.or(cb.equal(root.get("status"), "已提交"), cb.equal(root.get("status"), "采集中")));
                    }else if (UserUtils.ROLE_ADMIN.equals(user.getCheckRole())){
                        predicates.add(cb.equal(root.get("userId"), user.getId()));
                    } else {
                        predicates.add(cb.equal(root.get("subGroupId"), user.getSubGroupId()));
                    }
                    Predicate[] pre = new Predicate[predicates.size()];
                    return query.where(predicates.toArray(pre)).getRestriction();
                }
            };

            List<GatherPoint> findAll = gatherRepository.findAll(criteria);
            List<String> taskNames = new ArrayList<>();
            for (GatherPoint gatherPoint : findAll) {
                boolean isSame = false;
                for (int i = 0; i < taskNames.size(); i++) {
                    if (gatherPoint.getOrganizationName().equals(taskNames.get(i))) {
                        isSame = true;
                        continue;
                    }
                }
                if (!isSame) {
                    taskNames.add(gatherPoint.getOrganizationName());
                }
            }
            return taskNames;

        } catch (Exception e) {
            log.error("获取失败", e);
            throw new OperationException("获取失败,message" + e);
        }
    }

    @ApiOperation("获取账户下机构名称")
    @FormatResult
    @RequestMapping(value = "/getAccountOrganizationNameList", method = RequestMethod.GET)
    public Object getAccountOrganizationNameList(HttpServletRequest request) throws OperationException {

        try {
            User user = UserUtils.getUser();
            List<String> organizationNames = new ArrayList<>();
            //原生sql
            if (UserUtils.ROLE_PLATFORM_SUPER_LIST.contains(user.getCheckRole())) {
                Set<Organization> organizations = user.getOrganizations();
                for (Organization or : organizations) {
                    organizationNames.add(or.getOrganizationName());
                }
            }
            return organizationNames;

        } catch (Exception e) {
            log.error("获取失败", e);
            throw new OperationException("获取失败,message" + e);
        }
    }

    @ApiOperation("上传采集点列表")
    @FormatResult
    @RequestMapping(value = "/uploadGatherFile", method = RequestMethod.POST)
    public Object uploadGatherFile(@ApiParam("采集点文件") @RequestParam(value = "file", required = true) MultipartFile file,
                                   @ApiParam("数据类型") @RequestParam(value = "dataType", required = true) String dataType,
                                   @ApiParam("任务名称") @RequestParam(value = "taskName", required = true) String taskName,
                                   @ApiParam("机构名称（运维账户才需添加）") @RequestParam(value = "organizationName", required = false) String organizationName,
                                   HttpServletRequest request) throws TRSException {
        try {
            List<List<HashMap<String, String>>> list = ExcelUtil.readExcelWithTitleFile(FileUtil.multipartFileToFile(file));
            int num = 0;
            for (List<HashMap<String, String>> maplist: list) {
                num += maplist.size();
            }
            if (num > 200){
                throw new TRSException("当前任务采集点数量已超过限制");
            }

            List<GatherPoint> gatherPointList = new ArrayList<>();

            User user = UserUtils.getUser();
            boolean isAdmin = false;
            if (UserUtils.ROLE_PLATFORM_SUPER_LIST.contains(user.getCheckRole())) {
                isAdmin = true;
                if (StringUtil.isEmpty(organizationName))
                    throw new TRSException("机构名称不能为空");
            }
            String taskId = GUIDGenerator.generate(GatherPoint.class);
            String status = "已提交";
            if (ObjectUtil.isNotEmpty(list)) {
                try {
                    if (!dataType.equals("ALL")) {
                        if (Const.GATHER_TYPE_NEWS.contains(dataType)) {
                            //新闻
                            for (HashMap<String, String> hashMap : list.get(0)) {
                                String siteName = hashMap.get("站点名*");
                                String channelName = dataType.equals(Const.PAGE_SHOW_DIANZIBAO) ? hashMap.get("频道名*\n" +
                                        "【固定为电子报】") : hashMap.get("频道名称*");
//                                String urlName = hashMap.get("信息列表页链接*");
                                String urlName = dataType.equals(Const.PAGE_SHOW_DIANZIBAO) ? hashMap.get("链接*") : hashMap.get("信息列表页链接*");
                                String level = hashMap.get("优先级*");
                                if (StringUtil.isEmpty(siteName) || StringUtil.isEmpty(channelName) || StringUtil.isEmpty(urlName) || StringUtil.isEmpty(level)) {
//                                    throw new OperationException(dataType + "解析异常");
                                    throw new TRSException("模板数据不能有空选项");
                                }
                                GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType, taskName, taskId, siteName, channelName, urlName, "", "", new Date(), "", level);
                                gatherPoint.setStatus(status);
                                gatherPoint.setGatherPointName(siteName + "-" + channelName);
                                gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                                gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                                gatherPointList.add(gatherPoint);
                            }
                        } else if (Const.PAGE_SHOW_KEHUDUAN.equals(dataType)) {
                            //新闻App
                            for (HashMap<String, String> hashMap : list.get(0)) {
                                String siteName = hashMap.get("站点名*\n" +
                                        "【站点名后增加“客户端”字样】");
                                String channelName = hashMap.get("频道名*");
                                String level = hashMap.get("优先级*");
                                if (StringUtil.isEmpty(siteName) || StringUtil.isEmpty(channelName) || StringUtil.isEmpty(level)) {
                                    throw new TRSException(dataType + "解析异常");
                                }

                                GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType, taskName, taskId, siteName, channelName, "", "", "", new Date(), "", level);
                                gatherPoint.setStatus(status);
                                gatherPoint.setGatherPointName(siteName + "-" + channelName);
                                gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                                gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                                gatherPointList.add(gatherPoint);
                            }
                        } else if (Const.GATHER_MEDIA.contains(dataType)) {
                            for (HashMap<String, String> hashMap : list.get(0)) {
                                String siteName = hashMap.get("站点名*");
                                String accountName = hashMap.get("账号名称*");
                                String accountId = hashMap.get("账号ID*");
                                String urlName = hashMap.get("主页链接*");
                                String level = hashMap.get("优先级*");
                                if (StringUtil.isEmpty(siteName) || StringUtil.isEmpty(accountName) || StringUtil.isEmpty(accountId) || StringUtil.isEmpty(urlName) || StringUtil.isEmpty(level)) {
                                    throw new TRSException(dataType + "解析异常");
                                }

                                GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType, taskName, taskId, siteName, "", urlName, accountName, accountId, new Date(), "", level);
                                gatherPoint.setStatus(status);
                                gatherPoint.setGatherPointName(siteName + "-" + accountName);
                                gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                                gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                                gatherPointList.add(gatherPoint);
                            }
                        } else if (Const.GATHER_ZIMEITI.contains(dataType)) {
                            for (HashMap<String, String> hashMap : list.get(0)) {
                                String siteName = hashMap.get("站点名*");
                                String accountName = hashMap.get("账号*");
                                String urlName = hashMap.get("链接*");
                                String level = hashMap.get("优先级*");
                                if (StringUtil.isEmpty(siteName) || StringUtil.isEmpty(accountName) || StringUtil.isEmpty(urlName) || StringUtil.isEmpty(level)) {
                                    throw new TRSException(dataType + "解析异常");
                                }

                                GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType, taskName, taskId, siteName, "", urlName, accountName, "", new Date(), "", level);
                                gatherPoint.setStatus(status);
                                gatherPoint.setGatherPointName(siteName + "-" + accountName);
                                gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                                gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                                gatherPointList.add(gatherPoint);
                            }
                        } else if (Const.PAGE_SHOW_WEIBO.contains(dataType)) {
                            for (HashMap<String, String> hashMap : list.get(0)) {
                                String accountName = hashMap.get("账号名称*");
                                String urlName = hashMap.get("首页地址*");
                                String level = hashMap.get("优先级*");
                                if (StringUtil.isEmpty(accountName) || StringUtil.isEmpty(urlName) || StringUtil.isEmpty(level)) {
                                    throw new TRSException(dataType + "解析异常");
                                }

                                GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType, taskName, taskId, "", "", urlName, accountName, "", new Date(), "", level);
                                gatherPoint.setStatus(status);
                                gatherPoint.setGatherPointName(accountName);
                                gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                                gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                                gatherPointList.add(gatherPoint);
                            }
                        } else if (Const.PAGE_SHOW_WEIXIN.contains(dataType)) {
                            for (HashMap<String, String> hashMap : list.get(0)) {
                                String accountName = hashMap.get("账号名称*");
                                String urlName = hashMap.get("文章地址*");
                                String level = hashMap.get("优先级*");
                                String accountId = hashMap.get("微信号*");
                                if (StringUtil.isEmpty(accountName) || StringUtil.isEmpty(accountId) || StringUtil.isEmpty(urlName) || StringUtil.isEmpty(level)) {
                                    throw new TRSException(dataType + "解析异常");
                                }

                                GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType, taskName, taskId, "", "", urlName, accountName, accountId, new Date(), "", level);
                                gatherPoint.setStatus(status);
                                gatherPoint.setGatherPointName(accountName + "-" + accountId);
                                gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                                gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                                gatherPointList.add(gatherPoint);
                            }
                        } else if (Const.GATHER_TYPE_TWITTER.contains(dataType)) {
                            for (HashMap<String, String> hashMap : list.get(0)) {
                                String accountName = hashMap.get("用户名称*");
                                String urlName = hashMap.get("地址*");
                                String level = hashMap.get("优先级*");
                                if (StringUtil.isEmpty(accountName) || StringUtil.isEmpty(urlName) || StringUtil.isEmpty(level)) {
                                    throw new TRSException(dataType + "解析异常");
                                }

                                GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType, taskName, taskId, "", "", urlName, accountName, "", new Date(), "", level);
                                gatherPoint.setStatus(status);
                                gatherPoint.setGatherPointName(accountName);
                                gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                                gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                                gatherPointList.add(gatherPoint);
                            }
                        } else if ("元搜索".equals(dataType)) {
                            for (HashMap<String, String> hashMap : list.get(0)) {
                                String keyWord = hashMap.get("关键词*");
                                String siteName = hashMap.get("网站*");
                                String level = hashMap.get("优先级*");
                                if (StringUtil.isEmpty(keyWord) || StringUtil.isEmpty(siteName) || StringUtil.isEmpty(level)) {
                                    throw new TRSException(dataType + "解析异常");
                                }

                                GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType, taskName, taskId, siteName, "", "", "", "", new Date(), keyWord, level);
                                gatherPoint.setStatus(status);
                                gatherPoint.setGatherPointName(keyWord + "-" + siteName);
                                gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                                gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                                gatherPointList.add(gatherPoint);
                            }
                        } else {
                            int p = 0;
                            if (Const.PAGE_SHOW_GUOWAIXINWEN.equals(dataType)) {
                                p = 12;
                            } else if (Const.PAGE_SHOW_FACEBOOK.equals(dataType)) {
                                p = 13;
                            } else if (Const.GATHER_QITA.equals(dataType)) {
                                p = 14;
                            }
                            for (HashMap<String, String> hashMap : list.get(p)) {
                                String siteName = hashMap.get("关键词1*");
                                String channelName = hashMap.get("关键词2*");
                                String urlName = hashMap.get("关键词3*");
                                String level = hashMap.get("优先级*");
                                if (StringUtil.isEmpty(siteName) || StringUtil.isEmpty(channelName) || StringUtil.isEmpty(urlName) || StringUtil.isEmpty(level)) {
                                    throw new TRSException(dataType + "解析异常");
                                }

                                GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType, taskName, taskId, siteName, channelName, urlName, "", "", new Date(), "", level);
                                gatherPoint.setStatus(status);
                                gatherPoint.setGatherPointName(siteName);
                                gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                                gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                                gatherPointList.add(gatherPoint);
                            }

                        }

                    } else {
                        for (int i = 0; i < list.size(); i++) {
                            if (i == 0 || i == 2 || i == 6 || i == 9) {
                                if (i == 0){
                                    dataType = Const.PAGE_SHOW_XINWEN;
                                }else if (i == 2){
                                    dataType = Const.PAGE_SHOW_DIANZIBAO;
                                }else if (i == 6){
dataType = Const.PAGE_SHOW_LUNTAN;
                                }else if (i == 9){
dataType = Const.PAGE_SHOW_BOKE;
                                }
                                //新闻
                                for (HashMap<String, String> hashMap : list.get(i)) {
                                    String siteName = hashMap.get("站点名*");
                                    String channelName = i==2 ? hashMap.get("频道名*\n" +
                                            "【固定为电子报】") : hashMap.get("频道名称*");
                                    String urlName = i==2 ? hashMap.get("链接*") : hashMap.get("信息列表页链接*");
                                    String level = hashMap.get("优先级*");
                                    if (StringUtil.isEmpty(siteName) || StringUtil.isEmpty(channelName) || StringUtil.isEmpty(urlName) || StringUtil.isEmpty(level)) {
                                        throw new TRSException(dataType + "解析异常");
                                    }

                                    GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType, taskName, taskId, siteName, channelName, urlName, "", "", new Date(), "", level);
                                    gatherPoint.setStatus(status);
                                    gatherPoint.setGatherPointName(siteName + "-" + channelName);
                                    gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                                    gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                                    gatherPointList.add(gatherPoint);
                                }
                            } else if (i == 1) {
                                //新闻App
                                dataType = "新闻App";
                                for (HashMap<String, String> hashMap : list.get(i)) {
                                    String siteName = hashMap.get("站点名*\n" +
                                            "【站点名后增加“客户端”字样】");
                                    String channelName = hashMap.get("频道名*");
                                    String level = hashMap.get("优先级*");
                                    if (StringUtil.isEmpty(siteName) || StringUtil.isEmpty(channelName) || StringUtil.isEmpty(level)) {
                                        throw new TRSException(dataType + "解析异常");
                                    }
                                    GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType, taskName, taskId, siteName, channelName, "", "", "", new Date(), "", level);
                                    gatherPoint.setStatus(status);
                                    gatherPoint.setGatherPointName(siteName + "-" + channelName);
                                    gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                                    gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                                    gatherPointList.add(gatherPoint);
                                }
                            } else if (i == 7 || i == 8) {
                                if (i == 7){
                                    dataType = "短视频";
                                }else {
                                    dataType = "视频";
                                }
                                for (HashMap<String, String> hashMap : list.get(i)) {
                                    String siteName = hashMap.get("站点名*");
                                    String accountName = hashMap.get("账号名称*");
                                    String accountId = hashMap.get("账号ID*");
                                    String urlName = hashMap.get("主页链接*");
                                    String level = hashMap.get("优先级*");
                                    if (StringUtil.isEmpty(siteName) || StringUtil.isEmpty(accountName) || StringUtil.isEmpty(accountId) || StringUtil.isEmpty(urlName) || StringUtil.isEmpty(level)) {
                                        throw new TRSException(dataType + "解析异常");
                                    }
                                    GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType, taskName, taskId, siteName, "", urlName, accountName, accountId, new Date(), "", level);
                                    gatherPoint.setStatus(status);
                                    gatherPoint.setGatherPointName(siteName + "-" + accountName);
                                    gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                                    gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                                    gatherPointList.add(gatherPoint);
                                }
                            } else if (i == 5) {
                                    dataType = "自媒体号";
                                for (HashMap<String, String> hashMap : list.get(i)) {
                                    String siteName = hashMap.get("站点名*");
                                    String accountName = hashMap.get("账号*");
                                    String urlName = hashMap.get("链接*");
                                    String level = hashMap.get("优先级*");
                                    if (StringUtil.isEmpty(siteName) || StringUtil.isEmpty(accountName) || StringUtil.isEmpty(urlName) || StringUtil.isEmpty(level)) {
                                        throw new TRSException(dataType + "解析异常");
                                    }
                                    GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType, taskName, taskId, siteName, "", urlName, accountName, "", new Date(), "", level);
                                    gatherPoint.setStatus(status);
                                    gatherPoint.setGatherPointName(siteName + "-" + accountName);
                                    gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                                    gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                                    gatherPointList.add(gatherPoint);
                                }
                            } else if (i == 3) {
                                //微博
                                dataType = "微博";
                                for (HashMap<String, String> hashMap : list.get(i)) {
                                    String accountName = hashMap.get("账号名称*");
                                    String urlName = hashMap.get("首页地址*");
                                    String level = hashMap.get("优先级*");
                                    if (StringUtil.isEmpty(accountName) || StringUtil.isEmpty(urlName) || StringUtil.isEmpty(level)) {
                                        throw new TRSException(dataType + "解析异常");
                                    }
                                    GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType, taskName, taskId, "", "", urlName, accountName, "", new Date(), "", level);
                                    gatherPoint.setStatus(status);
                                    gatherPoint.setGatherPointName(accountName);
                                    gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                                    gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                                    gatherPointList.add(gatherPoint);
                                }
                            } else if (i == 4) {
                                dataType = "微信";
                                for (HashMap<String, String> hashMap : list.get(i)) {
                                    String accountName = hashMap.get("账号名称*");
                                    String urlName = hashMap.get("文章地址*");
                                    String level = hashMap.get("优先级*");
                                    String accountId = hashMap.get("微信号*");
                                    if (StringUtil.isEmpty(accountName) || StringUtil.isEmpty(accountId) || StringUtil.isEmpty(urlName) || StringUtil.isEmpty(level)) {
                                        throw new TRSException(dataType + "解析异常");
                                    }
                                    GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType, taskName, taskId, "", "", urlName, accountName, accountId, new Date(), "", level);
                                    gatherPoint.setStatus(status);
                                    gatherPoint.setGatherPointName(accountName + "-" + accountId);
                                    gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                                    gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                                    gatherPointList.add(gatherPoint);
                                }
                            } else if (i == 11) {
                                //twitter
                                dataType = Const.PAGE_SHOW_TWITTER;
                                for (HashMap<String, String> hashMap : list.get(i)) {
                                    String accountName = hashMap.get("用户名称*");
                                    String urlName = hashMap.get("地址*");
                                    String level = hashMap.get("优先级*");
                                    if (StringUtil.isEmpty(accountName) || StringUtil.isEmpty(urlName) || StringUtil.isEmpty(level)) {
                                        throw new TRSException(dataType + "解析异常");
                                    }
                                    GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType, taskName, taskId, "", "", urlName, accountName, "", new Date(), "", level);
                                    gatherPoint.setStatus(status);
                                    gatherPoint.setGatherPointName(accountName);
                                    gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                                    gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                                    gatherPointList.add(gatherPoint);
                                }
                            } else if (i == 10) {
                                dataType = "元搜索";
                                for (HashMap<String, String> hashMap : list.get(i)) {
                                    String keyWord = hashMap.get("关键词*");
                                    String siteName = hashMap.get("网站*");
                                    String level = hashMap.get("优先级*");
                                    if (StringUtil.isEmpty(siteName) || StringUtil.isEmpty(keyWord) || StringUtil.isEmpty(level)) {
                                        throw new OperationException(dataType + "解析异常");
                                    }
                                    GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType, taskName, taskId, siteName, "", "", "", "", new Date(), keyWord, level);
                                    gatherPoint.setStatus(status);
                                    gatherPoint.setGatherPointName(keyWord + "-" + siteName);
                                    gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                                    gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                                    gatherPointList.add(gatherPoint);
                                }
                            } else {
                                    if (i == 12) {
                                        dataType = "境外";
                                    } else if (i == 13) {
                                        dataType = Const.PAGE_SHOW_FACEBOOK;
                                    } else if (i == 14) {
                                        dataType = Const.GATHER_QITA;
                                    }
                                for (HashMap<String, String> hashMap : list.get(i)) {
                                    String siteName = hashMap.get("关键词1*");
                                    String channelName = hashMap.get("关键词2*");
                                    String urlName = hashMap.get("关键词3*");
                                    String level = hashMap.get("优先级*");
                                    if (StringUtil.isEmpty(siteName) || StringUtil.isEmpty(channelName) || StringUtil.isEmpty(urlName) || StringUtil.isEmpty(level)) {
                                        throw new TRSException(dataType + "解析异常");
                                    }
                                    GatherPoint gatherPoint = new GatherPoint(user.getId(), dataType, taskName, taskId, siteName, channelName, urlName, "", "", new Date(), "", level);
                                    gatherPoint.setStatus(status);
                                    gatherPoint.setGatherPointName(siteName);
                                    gatherPoint.setOrganizationName(isAdmin ? organizationName : user.getOrganizationName());
                                    gatherPoint.setAuditStatus(Const.GATHER_NO_AUDIT);
                                    gatherPointList.add(gatherPoint);
                                }
                            }

                        }
                    }
                    gatherRepository.save(gatherPointList);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new TRSException("解析失败" + e);
                }
//                JSONArray js = JSONArray.fromObject(hashMaps);
//                String sendPost = HttpUtil.sendPost(uploadpoint, js.toString());
            }

            return Const.SUCCESS;
        } catch (Exception e) {
            throw new TRSException("解析失败" + e);
//            e.printStackTrace();
        }
    }

    @ApiOperation("下载对应数据类型的模板")
    @FormatResult
    @RequestMapping(value = "/downLoadGatherFile", method = RequestMethod.POST)
    public Object downloadGatherFile(@ApiParam("数据类型") @RequestParam(value = "dataType", required = true) String dataType,
                                     HttpServletResponse res) throws TRSException {
        try {
            String heads;
            if (Const.GATHER_TYPE_NEWS.contains(dataType)) {
                //新闻
                heads = "站点名*;频道名称*;信息列表页链接*;优先级*;备注";
                if (Const.PAGE_SHOW_DIANZIBAO.equals(dataType)){
                    ExcelUtil.downExcel(res, dianzibaoFile, dataType);
                    log.debug("下载成功！");
                    return Const.SUCCESS;
                }
                ExcelUtil.downExcel(res, newsFile, dataType);
                log.debug("下载成功！");
                return Const.SUCCESS;
            } else if (Const.GATHER_MEDIA.contains(dataType)) {
                heads = "站点名*;账号名称*;账号ID*;主页链接*;优先级*;备注";
                ExcelUtil.downExcel(res, mediaFile, dataType);
                log.debug("下载成功！");
                return Const.SUCCESS;
            } else if (Const.GATHER_ZIMEITI.contains(dataType)) {
                heads = "站点名*;账号*;链接*;优先级*;备注";
                ExcelUtil.downExcel(res, zimeitiFile, dataType);
                log.debug("下载成功！");
                return Const.SUCCESS;
            } else if (Const.PAGE_SHOW_WEIBO.contains(dataType)) {
                heads = "账号名称*;首页地址*;优先级*;备注";
                ExcelUtil.downExcel(res, weiboFile, dataType);
                log.debug("下载成功！");
                return Const.SUCCESS;
            } else if (Const.PAGE_SHOW_WEIXIN.contains(dataType)) {
                heads = "站点名*;微信号*;文章链接*;优先级*;备注";
                ExcelUtil.downExcel(res, weixinFile, dataType);
                log.debug("下载成功！");
                return Const.SUCCESS;
            } else if (Const.GATHER_TYPE_TWITTER.contains(dataType)) {
                //todo 因为facebook暂定  所以暂时写成和twitter一致
                heads = "用户名称*;地址*;优先级*;备注";
                ExcelUtil.downExcel(res, twitterFile, dataType);
                log.debug("下载成功！");
                return Const.SUCCESS;
            } else if (Const.GATHER_YUAN.contains(dataType)) {
                heads = "关键词*;网站*;优先级*;备注";
                ExcelUtil.downExcel(res, yuanFile, dataType);
                log.debug("下载成功！");
                return Const.SUCCESS;
            } else if (Const.PAGE_SHOW_KEHUDUAN.equals(dataType)) {
                heads = "站点名*【站点名后增加“客户端”字样】;频道名*;优先级*;备注";
                ExcelUtil.downExcel(res, newsappFile, dataType);
                log.debug("下载成功！");
                return Const.SUCCESS;
            } else {
                ExcelUtil.downExcel(res, allFile, dataType);
                log.debug("下载成功！");
                return Const.SUCCESS;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @ApiOperation("获取采集点最新更新信息")
    @FormatResult
    @RequestMapping(value = "/getGatherInfo", method = RequestMethod.GET)
    public Object getGatherInfo(@ApiParam("采集点id") @RequestParam(value = "gatherId", required = true) String[] gatherId,
                                HttpServletRequest request) throws TRSException {
        if (ObjectUtil.isEmpty(gatherId)){
            throw new OperationException("请输入id");
        }
//        Map<String,String> map = new HashMap<>();
//        map.put("ids","["+gatherId[0]+"]");
//        String sendPost = HttpUtil.oaRelateEtl(uploadpoint, map);
        String sendPost = HttpUtil.sendPost(gatherInfo, "ids=["+gatherId[0]+"]");
        return sendPost;
    }
}
