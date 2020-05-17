package com.trs.netInsight.widget.special.service.impl;

import com.trs.jpa.utils.Criteria;
import com.trs.jpa.utils.Restrictions;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.common.service.ICommonListService;
import com.trs.netInsight.widget.special.entity.SpecialExponent;
import com.trs.netInsight.widget.special.entity.SpecialExponentVO;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.entity.repository.SpecialExponentRepository;
import com.trs.netInsight.widget.special.service.ISpecialComputeService;
import com.trs.netInsight.widget.special.service.ISpecialProjectService;
import com.trs.netInsight.widget.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 专题分析指数计算及其相关服务实现
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年5月3日
 */
@Service
@Slf4j
public class SpecialComputeServiceImpl implements ISpecialComputeService {

    @Autowired
    private SpecialExponentRepository exponentRepository;

    @Autowired
    private ISpecialProjectService projectService;

    @Autowired
    private FullTextSearch hybase8SearchService;
    @Autowired
    private ICommonListService commonListService;

    /**
     * 线程池跑任务
     */
    private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);

    @Override
    @Transactional
    public boolean compute() throws ParseException, TRSSearchException {
        // return this.computeSpecial(null);
        return this.computeByCondition(null);
    }

    @Override
    @Transactional
    public boolean compute(String[] orgIds) throws ParseException, TRSSearchException {
        //return this.computeSpecial(orgIds);
        return this.computeByCondition(orgIds);
    }

    @Override
    public List<SpecialExponent> findAll() {
        return this.findByCondition(null, null, null, null, null, false);
    }

    @Override
    public List<SpecialExponent> findAll(String orgId) {
        return this.findByCondition(orgId, null, null, null, null, false);
    }

    @Override
    public List<SpecialExponent> findAll(String orderBy, boolean sort) {
        return this.findByCondition(null, null, null, null, orderBy, sort);
    }

    @Override
    public List<SpecialExponent> findAll(String orgId, String orderBy, boolean sort) {
        return this.findByCondition(orgId, null, null, null, orderBy, sort);
    }

    @Override
    public List<SpecialExponent> findAllByComputeDate(Date begin, Date end) {
        return this.findByCondition(null, null, begin, end, null, false);
    }

    @Override
    public List<SpecialExponent> findAllByComputeDate(String orgId, Date begin, Date end) {
        return this.findByCondition(orgId, null, begin, end, null, false);
    }

    @Override
    public List<SpecialExponent> findAllByComputeDate(Date begin, Date end, String orderBy, boolean sort) {
        return this.findByCondition(null, null, begin, end, orderBy, sort);
    }

    @Override
    public List<SpecialExponent> findAllByComputeDate(String orgId, Date begin, Date end, String orderBy,
                                                      boolean sort) {
        return this.findByCondition(orgId, null, begin, end, orderBy, sort);
    }

    @Override
    public List<SpecialExponent> findAll(String[] specialIds) {
        return this.findByCondition(null, specialIds, null, null, null, false);
    }

    @Override
    public List<SpecialExponent> findAll(String[] specialIds, String orderBy, boolean sort) {
        return this.findByCondition(null, specialIds, null, null, orderBy, sort);
    }

    @Override
    public List<SpecialExponent> findAllByComputeDate(String[] specialIds, Date begin, Date end) {
        return this.findByCondition(null, specialIds, begin, end, null, false);
    }

    @Override
    public List<SpecialExponent> findAllByComputeDate(String[] specialIds, Date begin, Date end, String orderBy,
                                                      boolean sort) {
        return this.findByCondition(null, specialIds, begin, end, orderBy, sort);
    }

    /**
     * 根据条件检索专题分析指数
     *
     * @param orgId   专题id
     * @param begin   开始计算时间
     * @param end     结束计算时间
     * @param orderBy 排序字段
     * @param sort    正/逆序,true为正序
     * @return
     * @Return : List<SpecialExponent>
     * @since changjiang @ 2018年5月3日
     */
    private List<SpecialExponent> findByCondition(String orgId, String[] specailIds, Date begin, Date end,
                                                  String orderBy, boolean sort) {
        Criteria<SpecialExponent> criteria = new Criteria<>();

        // 机构id
        if (StringUtils.isNotBlank(orgId)) {
            criteria.add(Restrictions.eq("organizationId", orgId));
        }

        // 专题id集
        if (specailIds != null && specailIds.length > 0) {
            criteria.add(Restrictions.in("specialId", Arrays.asList(specailIds)));
        }

        // 计算时间范围
        if (begin != null && end != null) {
            criteria.add(Restrictions.between("computeTime", begin, end));
        }

        // 排序
        if (StringUtils.isNotBlank(orderBy)) {
            if (sort) {
                criteria.orderByASC(orderBy);
            } else {
                criteria.orderByDESC(orderBy);
            }
        }
        return exponentRepository.findAll(criteria);

    }

    private void computeThread(final List<SpecialProject> projects, User user, final int threadNo) {
        // try {
        // 计算各专题指数
        if (threadNo == 10) {
            log.error("线程10的专题数：" + projects.size());
        }
        if (projects != null && projects.size() > 0) {

            Criteria<SpecialExponent> criteria = null;
            for (SpecialProject specialProject : projects) {
                try {
                    SpecialExponent exponent = null;
                    Date begin = new Date();
                    log.error("当前线程:[" + threadNo + "],计算专题->" + specialProject.getSpecialName());
                    // 检查该专题在当前日期是否存在数据,存在覆盖,不存在保存
                    // 更新为检查并计算前一天数据
                    criteria = new Criteria<>();
                    String nowDay = DateUtil.formatDateAfter(begin,"yyyy-MM-dd",-1);
//                        String nowDay = DateUtil.formatCurrentTime("yyyy-MM-dd");
                    criteria.add(Restrictions.eq("specialId", specialProject.getId()));
                    criteria.add(Restrictions.eq("computeTime", DateUtils.parseDate(nowDay, "yyyy-MM-dd")));
                    List<SpecialExponent> findAll = this.exponentRepository.findAll(criteria);
                    if (findAll != null && findAll.size() > 0) {
                        exponent = findAll.get(0);
                    } else {
                        exponent = new SpecialExponent();
                        exponent.setSpecialId(specialProject.getId());
                        exponent.setComputeTime(DateUtils.parseDate(nowDay, "yyyy-MM-dd"));
                    }
                    exponent.setHotDegree(this.computeHotDegree(specialProject));
                    exponent.setMetaDegree(this.computeMetaDegree(specialProject));
                    exponent.setNetizenDegree(this.computeNetizenDegree(specialProject));
                    exponent.setSpecialName(specialProject.getSpecialName());
                    exponent.setCreatedUserId(user.getId());
                    exponent.setOrganizationId(user.getOrganizationId());
                    exponent.setSubGroupId(user.getSubGroupId());
                    exponent = this.exponentRepository.saveAndFlush(exponent);
                    log.error(exponent.getSpecialName() + "指数计算成功,共用时" + (new Date().getTime() - begin.getTime()) / 1000);
                } catch (Exception e) {
                    log.error(specialProject.getSpecialName() + "指数计算失败,专题ID："+specialProject.getId(),e);
                }
            }
        }
//        } catch (Exception e) {
//            log.error("计算专题指数失败!", e);
//        }
    }

    /**
     * 根据条件计算专题指数,并将其进行持久化
     *
     * @param orgIds 机构id集
     * @return
     * @throws ParseException
     * @throws TRSSearchException
     * @Return : boolean
     * @since changjiang @ 2018年5月3日
     */
    private boolean computeByCondition(String[] orgIds) throws ParseException, TRSSearchException {
        log.error("进入底层计算服务...");
        User user = UserUtils.getUser();

        List<SpecialProject> projects = null;
        // 获取需要进行分析的专题
        if (orgIds != null && orgIds.length > 0) {
            List<SpecialProject> sonList = null;
            projects = new ArrayList<>();
            if (user != null) {
                if(user.getId() == null){
                    return false;
                }
                if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
                    sonList = this.projectService.findByUserId(user.getId(), new Sort(Sort.Direction.DESC, "createdTime"));
                }else {
                    sonList = this.projectService.findBySubGroupId(user.getSubGroupId(),new Sort(Sort.Direction.DESC, "createdTime"));
                }
            } else {
                if(orgIds[0] == null){
                    return false;
                }
                for (String orgId : orgIds) {
                    sonList = this.projectService.findByOrganizationId(orgId, new Sort(Sort.Direction.DESC, "createdTime"));
                }
            }
            projects.addAll(sonList);
        } else {
            Criteria<SpecialProject> criteria = new Criteria();
            //获取三个月以前的时间，三个月以前的专题分析不再做更新
            String startTime = DateUtil.getTimeBeforeSomeMonthForYMD(3,"yyyy-MM-dd");
            String endTime = DateUtil.formatCurrentTime("yyyy-MM-dd HH:mm:ss");
            criteria.add(Restrictions.between("createdTime",
                    DateUtils.parseDate(startTime+ " 00:00:00", "yyyy-MM-dd HH:mm:ss"), DateUtils.parseDate(endTime, "yyyy-MM-dd HH:mm:ss")));
            projects = this.projectService.findAll(criteria);
        }
        log.error("需要进行计算的专题:" + projects.size() + "个");
        // 多线程计算
        if (projects != null && projects.size() > 10) {
            final List<SpecialProject> project1 = projects.subList(0, projects.size() / 10);
            final List<SpecialProject> project2 = projects.subList(projects.size() / 10, projects.size() / 10 * 2);
            final List<SpecialProject> project3 = projects.subList(projects.size() / 10 * 2, projects.size() / 10 * 3);
            final List<SpecialProject> project4 = projects.subList(projects.size() / 10 * 3, projects.size() / 10 * 4);
            final List<SpecialProject> project5 = projects.subList(projects.size() / 10 * 4, projects.size() / 10 * 5);
            final List<SpecialProject> project6 = projects.subList(projects.size() / 10 * 5, projects.size() / 10 * 6);
            final List<SpecialProject> project7 = projects.subList(projects.size() / 10 * 6, projects.size() / 10 * 7);
            final List<SpecialProject> project8 = projects.subList(projects.size() / 10 * 7, projects.size() / 10 * 8);
            final List<SpecialProject> project9 = projects.subList(projects.size() / 10 * 8, projects.size() / 10 * 9);
            final List<SpecialProject> project10 = projects.subList(projects.size() / 10 * 9, projects.size());
            fixedThreadPool.execute(() -> computeThread(project1, user, 1));
            fixedThreadPool.execute(() -> computeThread(project2, user, 2));
            fixedThreadPool.execute(() -> computeThread(project3, user, 3));
            fixedThreadPool.execute(() -> computeThread(project4, user, 4));
            fixedThreadPool.execute(() -> computeThread(project5, user, 5));
            fixedThreadPool.execute(() -> computeThread(project6, user, 6));
            fixedThreadPool.execute(() -> computeThread(project7, user, 7));
            fixedThreadPool.execute(() -> computeThread(project8, user, 8));
            fixedThreadPool.execute(() -> computeThread(project9, user, 9));
            fixedThreadPool.execute(() -> computeThread(project10, user, 10));
        } else if (projects != null && projects.size() > 0) { // 数量无须进行线程计算
            SpecialExponent exponent = null;
            Criteria<SpecialExponent> criteria = null;
            for (SpecialProject specialProject : projects) {

                // 检查该专题在当前日期是否存在数据,存在覆盖,不存在保存
                criteria = new Criteria<>();
//                String nowDay = DateUtil.formatCurrentTime("yyyy-MM-dd");
                String nowDay = DateUtil.formatDateAfter(new Date(),"yyyy-MM-dd",-1);
                criteria.add(Restrictions.eq("specialId", specialProject.getId()));
                criteria.add(Restrictions.eq("computeTime", DateUtils.parseDate(nowDay, "yyyy-MM-dd")));
                List<SpecialExponent> findAll = this.exponentRepository.findAll(criteria);
                if (findAll != null && findAll.size() > 0) {
                    exponent = findAll.get(0);
                } else {
                    exponent = new SpecialExponent();
                    exponent.setSpecialId(specialProject.getId());
                    exponent.setComputeTime(DateUtils.parseDate(nowDay, "yyyy-MM-dd"));
                }
                exponent.setHotDegree(this.computeHotDegree(specialProject));
                exponent.setMetaDegree(this.computeMetaDegree(specialProject));
                exponent.setNetizenDegree(this.computeNetizenDegree(specialProject));
                exponent.setSpecialName(specialProject.getSpecialName());
                exponent.setCreatedUserId(user.getId());
                exponent.setOrganizationId(user.getOrganizationId());
                exponent.setSubGroupId(user.getSubGroupId());
                this.exponentRepository.saveAndFlush(exponent);
            }
        }

        return false;
    }

    private boolean computeSpecial(String[] orgIds) {
        try {

            log.error("进入底层计算服务...");
            User user = UserUtils.getUser();

            List<SpecialProject> projects = null;
            // 获取需要进行分析的专题
            if (orgIds != null && orgIds.length > 0) {
                List<SpecialProject> sonList = null;
                projects = new ArrayList<>();
                if (user != null) {
                    sonList = this.projectService.findByUserId(user.getId(), new Sort(Sort.Direction.DESC, "createdTime"));
                } else {
                    for (String orgId : orgIds) {
                        sonList = this.projectService.findByOrganizationId(orgId, new Sort(Sort.Direction.DESC, "createdTime"));
                    }
                }
                projects.addAll(sonList);
            } else {
                projects = this.projectService.findAll(new Criteria<>());
            }
            log.error("需要才进行计算的专题:" + projects.size() + "个");


            // 计算各专题指数
            if (projects != null && projects.size() > 0) {
                SpecialExponent exponent = null;
                Criteria<SpecialExponent> criteria = null;
                for (SpecialProject specialProject : projects) {
                    Date begin = new Date();

                    // 检查该专题在当前日期是否存在数据,存在覆盖,不存在保存
                    criteria = new Criteria<>();
                    String nowDay = DateUtil.formatCurrentTime("yyyy-MM-dd");
                    criteria.add(Restrictions.eq("specialId", specialProject.getId()));
                    criteria.add(Restrictions.eq("computeTime", DateUtils.parseDate(nowDay, "yyyy-MM-dd")));
                    List<SpecialExponent> findAll = this.exponentRepository.findAll(criteria);
                    if (findAll != null && findAll.size() > 0) {
                        exponent = findAll.get(0);
                    } else {
                        exponent = new SpecialExponent();
                        exponent.setSpecialId(specialProject.getId());
                        exponent.setComputeTime(DateUtils.parseDate(nowDay, "yyyy-MM-dd"));
                    }
                    exponent.setHotDegree(this.computeHotDegree(specialProject));
                    exponent.setMetaDegree(this.computeMetaDegree(specialProject));
                    exponent.setNetizenDegree(this.computeNetizenDegree(specialProject));
                    exponent.setSpecialName(specialProject.getSpecialName());
                    exponent.setCreatedUserId(user.getId());
                    exponent.setOrganizationId(user.getOrganizationId());
                    exponent.setSubGroupId(user.getSubGroupId());
                    this.exponentRepository.saveAndFlush(exponent);
                    log.error(specialProject.getSpecialName() + "指数计算成功,共用时" + (new Date().getTime() - begin.getTime()) / 1000);
                }
            }
        } catch (Exception e) {
            log.error("计算专题指数失败!", e);
        }

        return false;
    }

    /**
     * 计算该专题在指定时间范围内的指数
     *
     * @param specialId 专题id
     * @param begin     开始时间
     * @param end       结束时间
     * @return
     * @throws TRSException
     * @throws ParseException
     * @throws TRSSearchException
     * @Return : boolean
     * @since changjiang @ 2018年5月4日
     */
    private List<SpecialExponent> computeByCondition(String specialId, String begin, String end)
            throws TRSException, ParseException, TRSSearchException {

        List<SpecialExponent> exponents = new ArrayList<>();
        User user = UserUtils.getUser();

        // 获取需要进行分析的专题
        SpecialProject specialProject = this.projectService.findOne(specialId);
        List<String> days = DateUtil.subDateRangeToList(begin, end);

        // 初始化保存计算指数集合,各指数为0
        SpecialExponent exponent = null;
        Criteria<SpecialExponent> criteria = null;
        for (String day : days) {
            criteria = new Criteria<>();
            criteria.add(Restrictions.eq("specialId", specialProject.getId()));
            criteria.add(Restrictions.eq("computeTime", DateUtils.parseDate(day, "yyyy-MM-dd")));
            List<SpecialExponent> findAll = this.exponentRepository.findAll(criteria);
            if (findAll != null && findAll.size() > 0) {
                exponent = findAll.get(0);
            } else {
                exponent = new SpecialExponent();
                exponent.setSpecialId(specialProject.getId());
                exponent.setComputeTime(DateUtils.parseDate(day, "yyyy-MM-dd"));
            }
            exponent.setHotDegree(0l);
            exponent.setMetaDegree(0l);
            exponent.setNetizenDegree(0l);
            exponent.setSpecialName(specialProject.getSpecialName());
            exponent.setCreatedUserId(user.getId());
            exponent.setOrganizationId(user.getOrganizationId());
            exponent.setSubGroupId(user.getSubGroupId());
            exponents.add(exponent);
        }
        List<SpecialExponent> initList = this.exponentRepository.save(exponents);

        // 计算各指数
        initList = computeHotDegree(specialProject, days, initList);
        initList = computeNetizenDegree(specialProject, days, initList);
        initList = computeMetaDegree(specialProject, days, initList);

        // 清空返回集合
        exponents.clear();

        // 重新保存计算指数
        for (SpecialExponent specialExponent : initList) {
            SpecialExponent flush = this.exponentRepository.saveAndFlush(specialExponent);
            exponents.add(flush);
        }

        return exponents;
    }

    /**
     * 计算热度值</br>
     * 热度: 全库下指定数据来源且满足专题检索条件的所有数据总和
     *
     * @param specialProject
     * @return
     * @Return : long
     * @since changjiang @ 2018年5月3日
     */
    private long computeHotDegree(SpecialProject specialProject) {
        long number = 0l;
        // 根据source选择对应数据源
        String[] sources = {"ALL"};
        if (StringUtils.isNotBlank(specialProject.getSource())) {
            sources = specialProject.getSource().split(";");
        }
        String groupNames = computeGroupNameForHotDegree(sources);
        if (StringUtils.isNotBlank(groupNames)) {

            QueryCommonBuilder commonBuilder = specialProject.toCommonBuilder(0, Integer.MAX_VALUE, false);
            String[] database = {Const.WECHAT, Const.HYBASE_OVERSEAS, Const.HYBASE_NI_INDEX, Const.WEIBO};
            commonBuilder.setDatabase(database);
            commonBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupNames, Operator.Equal);
            String thisDay = DateUtil.formatDateAfter(new Date(),"yyyy-MM-dd",-1);
//            String thisDay = DateUtil.formatCurrentTime("yyyy-MM-dd");
            commonBuilder.filterField(FtsFieldConst.FIELD_URLDATE, thisDay, Operator.Equal);
//            number = hybase8SearchService.ftsCountCommon(commonBuilder, specialProject.isSimilar(), false,false,null);
            try {
                number = commonListService.ftsCount(commonBuilder, specialProject.isSimilar(), false,false,null);
            } catch (TRSException e) {
                e.printStackTrace();
            }
        }

        return number;
    }

    /**
     * 计算指定日期的热度值</br>
     * 热度: 全库下指定数据来源且满足专题检索条件的所有数据总和
     *
     * @param specialProject 专题
     * @param days           指定日期范围
     * @return
     * @throws TRSSearchException
     * @Return : long
     * @since changjiang @ 2018年5月4日
     */
    private List<SpecialExponent> computeHotDegree(SpecialProject specialProject, List<String> days,
                                                   List<SpecialExponent> initList) throws TRSSearchException {

        // 根据source选择对应数据源
        String[] sources = {"ALL"};
        if (StringUtils.isNotBlank(specialProject.getSource())) {
            sources = specialProject.getSource().split(";");
        }
        String groupNames = computeGroupNameForHotDegree(sources);

        if (days != null && days.size() > 0 && StringUtils.isNotBlank(groupNames)) {
            QueryCommonBuilder commonBuilder = specialProject.toCommonBuilder(0, Integer.MAX_VALUE, false);
            String[] database = {Const.WECHAT, Const.HYBASE_OVERSEAS, Const.HYBASE_NI_INDEX, Const.WEIBO};
            commonBuilder.setDatabase(database);
            commonBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupNames, Operator.Equal);
            QueryCommonBuilder countBuilder = null;
            for (String day : days) {
                long ftsCountCommon = 0l;
                for (SpecialExponent specialExponent : initList) {
                    Date thisDay = DateUtil.stringToDate(day, "yyyy-MM-dd");
                    if (thisDay.getTime() == (specialExponent.getComputeTime().getTime())) {
                        countBuilder = commonBuilder;
                        countBuilder.filterField(FtsFieldConst.FIELD_URLDATE, day, Operator.Equal);
                        System.out.println(countBuilder.asTRSL());
//                        ftsCountCommon = hybase8SearchService.ftsCountCommon(countBuilder, specialProject.isSimilar(), false,false,null);
                        try {
                            ftsCountCommon = commonListService.ftsCount(countBuilder,specialProject.isSimilar(), false,false,null);
                        } catch (TRSException e) {
                            e.printStackTrace();
                        }
                        specialExponent.setHotDegree(ftsCountCommon);
                    }
                }
            }
        }
        return initList;
    }

    /**
     * 计算网民参与度</br>
     * 网民参与度:全库中微博、微信、国内论坛、国内博客、国外论坛、FaceBook、Twitter等个人发布的信息的作者数合计(相同作者只计算一次)
     *
     * @param specialProject
     * @return
     * @throws TRSSearchException
     * @Return : long
     * @since changjiang @ 2018年5月3日
     */
    private long computeNetizenDegree(SpecialProject specialProject) throws TRSSearchException {

        long number = 0l;
        // 根据source选择对应数据源
        String[] sources = {"ALL"};
        if (StringUtils.isNotBlank(specialProject.getSource())) {
            sources = specialProject.getSource().split(";");
        }
        String groupNames = computeGroupNameForNetizenDegree(sources);
        if (StringUtils.isNotBlank(groupNames)) {
            QueryCommonBuilder commonBuilder = specialProject.toCommonBuilder(0, Integer.MAX_VALUE, false);
            QueryBuilder countWeibo = specialProject.toBuilderWeiBo(0, Integer.MAX_VALUE, false);

//			String[] database = {Const.WECHAT, Const.HYBASE_OVERSEAS, Const.HYBASE_NI_INDEX };
            String[] database = {Const.HYBASE_OVERSEAS, Const.HYBASE_NI_INDEX};
            commonBuilder.setDatabase(database);
            commonBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupNames, Operator.Equal);

            String thisDay = DateUtil.formatDateAfter(new Date(),"yyyy-MM-dd",-1);
//            String thisDay = DateUtil.formatCurrentTime("yyyy-MM-dd");
            commonBuilder.filterField(FtsFieldConst.FIELD_URLDATE, thisDay, Operator.Equal);
            GroupResult groupResult = null;
            try {
                groupResult = commonListService.categoryQuery(commonBuilder, specialProject.isSimilar(), false,false,FtsFieldConst.FIELD_AUTHORS,null);
            } catch (TRSException e) {
                e.printStackTrace();
            }
//            GroupResult groupResult = hybase8SearchService.categoryQuery(specialProject.isServer(), commonBuilder.asTRSL(),
//                    specialProject.isSimilar(), false,false, FtsFieldConst.FIELD_AUTHORS, Integer.MAX_VALUE,null, database);
            number = groupResult.size();

            // 微博用户,使用uid进行统计
            if (groupNames.contains("微博")) {
                countWeibo.filterField(FtsFieldConst.FIELD_URLDATE, thisDay, Operator.Equal);
//                groupResult = hybase8SearchService.categoryQuery(countWeibo, specialProject.isSimilar(), false,false,
//                        FtsFieldConst.FIELD_UID, null,Const.WEIBO);
                countWeibo.setDatabase(Const.WEIBO);
                try {
                    groupResult = commonListService.categoryQuery(countWeibo, specialProject.isSimilar(), false,false,FtsFieldConst.FIELD_UID,null);
                    number += groupResult.size();
                } catch (TRSException e) {
                    e.printStackTrace();
                }

            }
        }
        return number;
    }

    /**
     * 计算指定日期的网民参与度</br>
     * 网民参与度:全库中微博、微信、国内论坛、国内博客、国外论坛、FaceBook、Twitter等个人发布的信息的作者数合计(相同作者只计算一次)
     *
     * @param specialProject 专题
     * @param days           指定日期
     * @return
     * @throws TRSSearchException
     * @Return : long
     * @since changjiang @ 2018年5月4日
     */
    private List<SpecialExponent> computeNetizenDegree(SpecialProject specialProject, List<String> days,
                                                       List<SpecialExponent> initList) throws TRSSearchException {

        // 根据source选择对应数据源
        String[] sources = {"ALL"};
        if (StringUtils.isNotBlank(specialProject.getSource())) {
            sources = specialProject.getSource().split(";");
        }
        String groupNames = computeGroupNameForNetizenDegree(sources);
        if (StringUtils.isNotBlank(groupNames)) {
            boolean containsWeibo = groupNames.contains("微博");
            if (days != null && days.size() > 0) {
                QueryCommonBuilder commonBuilder = specialProject.toCommonBuilder(0, Integer.MAX_VALUE, false);
                QueryBuilder countWeibo = specialProject.toBuilderWeiBo(0, Integer.MAX_VALUE, false);

//				String[] database = { Const.WECHAT, Const.HYBASE_OVERSEAS, Const.HYBASE_NI_INDEX };
                String[] database = {Const.HYBASE_OVERSEAS, Const.HYBASE_NI_INDEX};
                commonBuilder.setDatabase(database);
                commonBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupNames, Operator.Equal);
                QueryCommonBuilder countBuilder = null;
                GroupResult groupResult = null;
                for (String day : days) {
                    for (SpecialExponent specialExponent : initList) {
                        Date thisDay = DateUtil.stringToDate(day, "yyyy-MM-dd");
                        long count = 0l;
                        if (thisDay.getTime() == specialExponent.getComputeTime().getTime()) {

                            //传统媒体及微信等使用作者进行统计
                            countBuilder = commonBuilder;
                            countBuilder.setDatabase(database);
                            countBuilder.filterField(FtsFieldConst.FIELD_URLDATE, day, Operator.Equal);
//                            groupResult = hybase8SearchService.categoryQuery(specialProject.isServer(), countBuilder.asTRSL(),
//                                    specialProject.isSimilar(), false,false, FtsFieldConst.FIELD_AUTHORS, Integer.MAX_VALUE,null,
//                                    database);
                            try {
                                groupResult = commonListService.categoryQuery(countBuilder, specialProject.isSimilar(), false,false,FtsFieldConst.FIELD_AUTHORS,null);
                            } catch (TRSException e) {
                                e.printStackTrace();
                            }
                            count += groupResult.size();

                            // 微博用户,使用uid进行统计
                            if (containsWeibo) {
                                countWeibo.filterField(FtsFieldConst.FIELD_URLDATE, day, Operator.Equal);
//                                groupResult = hybase8SearchService.categoryQuery(countWeibo, specialProject.isSimilar(), false,false,
//                                        FtsFieldConst.FIELD_UID,null, Const.WEIBO);
                                try {
                                    groupResult = commonListService.categoryQuery(countWeibo, specialProject.isSimilar(), false,false,FtsFieldConst.FIELD_UID,null,Const.GROUPNAME_WEIBO);
                                } catch (TRSException e) {
                                    e.printStackTrace();
                                }
                                count += groupResult.size();
                            }

                            specialExponent.setNetizenDegree(count);
                        }
                    }
                }
            }
        }
        return initList;
    }

    /**
     * 计算专题媒体参与度</br>
     * 媒体参与度:传统库中各媒体站点数量即为媒体参与度,大致分为:国内新闻、电子报、客户端、国外新闻、港澳台新闻等
     *
     * @param specialProject 专题
     * @return
     * @throws TRSSearchException
     * @Return : long
     * @since changjiang @ 2018年5月3日
     */
    private long computeMetaDegree(SpecialProject specialProject) throws TRSSearchException {

        long number = 0l;
        // 根据source选择对应数据源
        String[] sources = {"ALL"};
        if (StringUtils.isNotBlank(specialProject.getSource())) {
            sources = specialProject.getSource().split(";");
        }
        String groupNames = computeGroupNameForMetaDegree(sources);
        if (StringUtils.isNotBlank(groupNames)) {
            if (sources != null && sources.length > 0) {

                QueryCommonBuilder commonBuilder = specialProject.toCommonBuilder(0, Integer.MAX_VALUE, false);
                String[] database = {Const.HYBASE_NI_INDEX};
                commonBuilder.setDatabase(database);

                commonBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupNames, Operator.Equal);
                String thisDay = DateUtil.formatDateAfter(new Date(),"yyyy-MM-dd",-1);
//                String thisDay = DateUtil.formatCurrentTime("yyyy-MM-dd");
                commonBuilder.filterField(FtsFieldConst.FIELD_URLDATE, thisDay, Operator.Equal);

                commonBuilder.filterField(FtsFieldConst.FIELD_URLDATE, thisDay, Operator.Equal);
//                GroupResult groupResult = hybase8SearchService.categoryQuery(specialProject.isServer(), commonBuilder.asTRSL(),
//                        specialProject.isSimilar(), false,false, FtsFieldConst.FIELD_SITENAME, Integer.MAX_VALUE, null,database);
                GroupResult groupResult = null;
                try {
                    groupResult = commonListService.categoryQuery(commonBuilder, specialProject.isSimilar(), false,false,FtsFieldConst.FIELD_SITENAME,null);
                    number = groupResult.size();
                } catch (TRSException e) {
                    e.printStackTrace();
                }

            }
        }
        return number;
    }

    /**
     * 计算指定日期的媒体参与度
     * 媒体参与度:传统库中各媒体站点数量即为媒体参与度,大致分为:国内新闻、电子报、客户端、国外新闻、港澳台新闻等
     *
     * @param specialProject 专题
     * @param days           指定日期
     * @return
     * @throws TRSSearchException
     * @Return : long
     * @since changjiang @ 2018年5月4日
     */
    private List<SpecialExponent> computeMetaDegree(SpecialProject specialProject, List<String> days,
                                                    List<SpecialExponent> initList) throws TRSSearchException {

        // 根据source选择对应数据源
        String[] sources = {"ALL"};
        if (StringUtils.isNotBlank(specialProject.getSource())) {
            sources = specialProject.getSource().split(";");
        }
        if (sources != null && sources.length > 0) {
            String groupNames = computeGroupNameForMetaDegree(sources);
            if (StringUtils.isNotBlank(groupNames)) {
                if (days != null && days.size() > 0) {
                    QueryCommonBuilder commonBuilder = specialProject.toCommonBuilder(0, Integer.MAX_VALUE, false);
                    String[] database = {Const.HYBASE_NI_INDEX};
                    commonBuilder.setDatabase(database);
                    commonBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupNames, Operator.Equal);
                    QueryCommonBuilder countBuilder = null;
                    GroupResult groupResult = null;
                    for (String day : days) {
                        for (SpecialExponent specialExponent : initList) {
                            Date thisDay = DateUtil.stringToDate(day, "yyyy-MM-dd");
                            if (thisDay.getTime() == (specialExponent.getComputeTime().getTime())) {
                                countBuilder = commonBuilder;
                                countBuilder.filterField(FtsFieldConst.FIELD_URLDATE, day, Operator.Equal);
//                                groupResult = hybase8SearchService.categoryQuery(specialProject.isServer(), countBuilder.asTRSL(),
//                                        specialProject.isSimilar(), false,false, FtsFieldConst.FIELD_SITENAME, Integer.MAX_VALUE,
//                                       null, database);
                                try {
                                    groupResult = commonListService.categoryQuery(countBuilder, specialProject.isSimilar(), false,false,FtsFieldConst.FIELD_SITENAME,null);
                                } catch (TRSException e) {
                                    e.printStackTrace();
                                }
                                specialExponent.setMetaDegree(groupResult.size());
                            }
                        }
                    }
                }
            }
        }
        return initList;
    }

    @Override
    public List<SpecialExponent> computeBySpecialId(String specialId, Date begin, Date end)
            throws TRSException, ParseException, TRSSearchException {
        String beginDay = null;
        String endDay = null;
        String format = "yyyy-MM-dd";
        if (begin != null && end != null) {
            beginDay = DateUtil.format2String(begin, format);
            endDay = DateUtil.format2String(end, format);
        } else {
            beginDay = DateUtil.formatCurrentTime(format);
            endDay = DateUtil.formatDateAfter(end, format, 7);
        }
        return this.computeByCondition(specialId, beginDay, endDay);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SpecialExponentVO> computeTotalByCondition(String[] specialIds, Date begin, Date end, String orderBy,
                                                           boolean sort) {
        if (specialIds == null || specialIds.length <= 0) {
            return new ArrayList<>();
        }
        List<String> ids = Arrays.asList(specialIds);
        List<SpecialExponentVO> voList = new ArrayList<>();
        List<Object[]> list = this.exponentRepository.computeTotleExponentBySort(ids, begin, end);

        // object 转换为 vo对象
        if (list != null && list.size() > 0) {
            SpecialProject one = null;
            for (Object[] objs : list) {
                SpecialExponentVO vo = new SpecialExponentVO();
                vo.setSpecialId((String) objs[0]);
                one = this.projectService.findOne(vo.getSpecialId());
                vo.setSpecialName(one.getSpecialName());
                vo.setHotDegree(Long.valueOf(String.valueOf(objs[2])));
                vo.setNetizenDegree(Long.valueOf(String.valueOf(objs[3])));
                vo.setMetaDegree(Long.valueOf(String.valueOf(objs[4])));
                voList.add(vo);
            }
        }


        // 排序(jpa不支持动态排序)
        Collections.sort(voList, new Comparator<SpecialExponentVO>() {

            @Override
            public int compare(SpecialExponentVO o1, SpecialExponentVO o2) {

                if (StringUtils.isNotBlank(orderBy)) {
                    if (orderBy.equals("metaDegree")) {
                        if (sort) {
                            return (int) (o1.getMetaDegree() - o2.getMetaDegree());
                        } else {
                            return (int) (o2.getMetaDegree() - o1.getMetaDegree());
                        }
                    } else if (orderBy.equals("netizenDegree")) {

                        if (sort) {
                            return (int) (o1.getNetizenDegree() - o2.getNetizenDegree());
                        } else {
                            return (int) (o2.getNetizenDegree() - o1.getNetizenDegree());
                        }
                    } else {
                        if (sort) {
                            return (int) (o1.getHotDegree() - o2.getHotDegree());
                        } else {
                            return (int) (o2.getHotDegree() - o1.getHotDegree());
                        }
                    }
                }
                return (int) (o2.getHotDegree() - o1.getHotDegree());
            }

        });
        return voList;
    }

    @Override
    public void delete(String[] specialIds) {
        Criteria<SpecialExponent> criteria = null;
        List<SpecialExponent> findAll = null;
        if (specialIds != null && specialIds.length > 0) {
            for (String specialId : specialIds) {
                if (StringUtils.isNotBlank(specialId)) {
                    criteria = new Criteria<>();
                    criteria.add(Restrictions.eq("specialId", specialId));
                    findAll = this.exponentRepository.findAll(criteria);
                    this.exponentRepository.delete(findAll);
                }
            }
        }

    }

    @Override
    public Object computeTrendChart(String[] specialIds, Date begin, Date end) {
        List<Object> data = new ArrayList<>();
        Map<String, Object> specialData = null;// 专题集
        Map<String, Object> exponentDate = null;//指数集
        List<Object[]> convertList = null;
        if (specialIds != null && specialIds.length > 0) {
            SpecialProject project = null;
            for (String specialId : specialIds) {
                specialData = new HashMap<>();
                exponentDate = new HashMap<>();
                project = this.projectService.findOne(specialId);
                convertList = this.exponentRepository.computeExponentGroupbyTime(specialId, begin, end);
                List<Object> hotDegree = null;//时间集
                List<Object> metaDegree = null;//时间集
                List<Object> netizenDegree = null;//时间集
                if (convertList != null && convertList.size() > 0) {
                    hotDegree = new ArrayList<>();
                    metaDegree = new ArrayList<>();
                    netizenDegree = new ArrayList<>();
                    String dateStr = null;
                    Map<String, Object> keyValueHot = null;
                    Map<String, Object> keyValueNet = null;
                    Map<String, Object> keyValueMedia = null;
                    Date day = null;
                    for (Object[] objs : convertList) {
                        keyValueHot = new HashMap<>();
                        keyValueNet = new HashMap<>();
                        keyValueMedia = new HashMap<>();

                        dateStr = String.valueOf(String.valueOf(objs[1]));
                        day = DateUtil.stringToDate(dateStr, "yyyy-MM-dd");
                        dateStr = DateUtil.format2String(day, "yyyy-MM-dd");

                        keyValueHot.put("time", String.valueOf(dateStr));
                        keyValueNet.put("time", String.valueOf(dateStr));
                        keyValueMedia.put("time", String.valueOf(dateStr));

                        keyValueHot.put("number", Long.valueOf(String.valueOf(objs[2])));
                        keyValueNet.put("number", Long.valueOf(String.valueOf(objs[3])));
                        keyValueMedia.put("number", Long.valueOf(String.valueOf(objs[4])));

                        hotDegree.add(keyValueHot);
                        metaDegree.add(keyValueNet);
                        netizenDegree.add(keyValueMedia);
                    }
                    exponentDate.put("hotDegree", hotDegree);
                    exponentDate.put("metaDegree", metaDegree);
                    exponentDate.put("netizenDegree", netizenDegree);
                }
                specialData.put("specialName", project.getSpecialName());
                specialData.put("chart", exponentDate);
                data.add(specialData);
            }
        }
        return data;
    }

    /**
     * 计算媒体关注度来源
     *
     * @param sources
     * @return
     * @Return : String
     * @since changjiang @ 2018年5月10日
     */
    private String computeGroupNameForMetaDegree(String[] sources) {
        String groupName = "";
        if (sources[0].equals("ALL")) {
            groupName = "国内新闻* OR 国外新闻* ";
            return groupName;
        }
        for (String source : sources) {
            if (StringUtils.isNotBlank(source)) {
                if (source.equals("微博") || source.equals("微信") || source.equals("国内博客") || source.equals("FaceBook")
                        || source.equals("Twitter") || source.contains("论坛")) {
                    continue;
                }
                if (source.equals("境外媒体")) {
                    source = "国外新闻* OR 港澳台新闻 ";
                }

                groupName += source + " OR ";
            }
        }
        if (groupName.endsWith("OR ")) {
            groupName = groupName.substring(0, groupName.length() - 4);
        }
        return groupName;
    }

    /**
     * 计算网民关注度来源
     *
     * @param sources
     * @return
     * @Return : String
     * @since changjiang @ 2018年5月10日
     */
    private String computeGroupNameForNetizenDegree(String[] sources) {
        String groupName = "";
        if (sources[0].equals("ALL")) {
            groupName = "国内论坛 OR 国内博客 OR 国外论坛 OR 国内微信 OR FaceBook OR Twitter OR 微博 ";
            return groupName;
        }
        for (String source : sources) {
            if (StringUtils.isNotBlank(source)) {
                if (source.contains("新闻") || source.equals("境外媒体")) {
                    continue;
                }
                if (source.equals("微信")) {
                    source = "国内微信 ";
                }

                groupName += source + " OR ";
            }
        }
        if (groupName.endsWith("OR ")) {
            groupName = groupName.substring(0, groupName.length() - 4);
        }
        return groupName;
    }

    /**
     * 计算热度来源
     *
     * @param sources
     * @return
     * @Return : String
     * @since changjiang @ 2018年5月10日
     */
    private String computeGroupNameForHotDegree(String[] sources) {
        String groupName = "";
        if (sources[0].equals("ALL")) {
            groupName = "国内论坛 OR 国内博客 OR 国外论坛 OR 国内微信 OR FaceBook OR Twitter OR 微博 OR 国内新闻* OR 国内微信 OR 国外新闻* OR 港澳台新闻";
            return groupName;
        }
        for (String source : sources) {
            if (StringUtils.isNotBlank(source)) {
                if (source.equals("微信")) {
                    source = "国内微信 ";
                } else if (source.equals("境外媒体")) {
                    source = "国外新闻* OR 港澳台新闻 ";
                }

                groupName += source + " OR ";
            }
        }
        if (groupName.endsWith("OR ")) {
            groupName = groupName.substring(0, groupName.length() - 4);
        }
        return groupName;
    }



}
