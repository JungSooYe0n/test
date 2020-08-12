package com.trs.netInsight.widget.user.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.util.CollectionsUtil;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.alert.entity.AlertAccount;
import com.trs.netInsight.widget.alert.entity.AlertRule;
import com.trs.netInsight.widget.alert.entity.repository.AlertAccountRepository;
import com.trs.netInsight.widget.alert.entity.repository.AlertRuleRepository;
import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.entity.NavigationConfig;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.column.repository.IndexPageRepository;
import com.trs.netInsight.widget.column.repository.IndexTabMapperRepository;
import com.trs.netInsight.widget.column.repository.IndexTabRepository;
import com.trs.netInsight.widget.column.repository.NavigationRepository;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.entity.SpecialSubject;
import com.trs.netInsight.widget.special.entity.repository.SpecialProjectRepository;
import com.trs.netInsight.widget.special.entity.repository.SpecialSubjectRepository;
import com.trs.netInsight.widget.user.entity.DataSyncSpecial;
import com.trs.netInsight.widget.user.entity.SubGroup;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
public class HelpService {

    @Autowired
    private NavigationRepository navigationRepository;

    @Autowired
    private IndexPageRepository indexPageRepository;
    @Autowired
    private IndexTabMapperRepository indexTabMapperRepository;

    @Autowired
    private IndexTabMapperRepository tabMapperRepository;

    @Autowired
    private IndexTabRepository indexTabRepository;

    @Autowired
    private SpecialProjectRepository specialProjectRepository;

    @Autowired
    private SpecialSubjectRepository specialSubjectRepository;

    @Autowired
    private AlertAccountRepository alertAccountRepository;

    @Autowired
    private AlertRuleRepository alertRuleRepository;

    public Object save(IndexTab indexTab, boolean share) {
        IndexTab save = indexTabRepository.save(indexTab);
        // 保存映射表
        IndexTabMapper mapper = save.mapper(share);
        IndexPage indexPage = indexPageRepository.findOne(save.getParentId());
        mapper.setIndexPage(indexPage);
        mapper = tabMapperRepository.save(mapper);
        return mapper;
    }

    public void copySomePageAndTabToUserGroup(List<String> pageIds, SubGroup group) {
        //被选中机构管理员的栏目分组
        List<IndexPage> indexPageList = indexPageRepository.findByIdIn(pageIds, new Sort(Sort.Direction.DESC, "sequence"));
        //当前被同步用户分组所拥有的导航
        List<NavigationConfig> navigations = navigationRepository.findBySubGroupId(group.getId(),
                new Sort(Sort.Direction.DESC, "sequence"));

        if (indexPageList != null && indexPageList.size() > 0) {
            for (IndexPage indexPage : indexPageList) {
                if (indexPage != null) {
                    IndexPage pageCopy = indexPage.pageCopy();
                    pageCopy.setSubGroupId(group.getId());
                    pageCopy.setOrganizationId(group.getOrganizationId());
                    pageCopy.setUserId("dataSync");
                    // 此时说明该pageCopy 为 自定义导航栏下的内容
                    if (pageCopy.getTypeId() != null && pageCopy.getTypeId().length() > 0) {
                        //机构管理员的导航
                        NavigationConfig navigation = navigationRepository.findOne(pageCopy.getTypeId());
                        if(navigation!=null){
                            int sequence = navigation.getSequence();
                            for (NavigationConfig navigationTemp : navigations) {
                                // 争取匹配
                                if (navigationTemp.getSequence() == sequence) {
                                    //放入 被同步用户所属导航的id
                                    pageCopy.setTypeId(navigationTemp.getId());
                                    break;
                                }
                            }
                        }
                    }
                    IndexPage page = indexPageRepository.save(pageCopy);
                    //栏目分组所包含的栏目
                    List<IndexTabMapper> indexTabList = tabMapperRepository.findByIndexPage(indexPage);
                    if (page != null && indexTabList != null && indexTabList.size() > 0) {
                        for (IndexTabMapper indexTabMapper : indexTabList) {
                            IndexTab indexTab = indexTabMapper.getIndexTab();
                            IndexTab tabCopy = indexTab.tabCopy();
                            tabCopy.setSequence(indexTabMapper.getSequence());
                            tabCopy.setSubGroupId(group.getId());
                            tabCopy.setParentId(page.getId());
                            tabCopy.setOrganizationId(group.getOrganizationId());
                            tabCopy.setUserId("dataSync");
                            this.save(tabCopy, false);
                        }
                    }
                }
            }
        }
    }

    public void copySomeNavigationToUserGroup(List<String> navIds, SubGroup subGroup) {
        List<NavigationConfig> adminNavigations = navigationRepository.findByIdIn(navIds);
        if (ObjectUtil.isNotEmpty(adminNavigations)){
            for (NavigationConfig adminNavigation : adminNavigations) {
                NavigationConfig navigationConfig = adminNavigation.copyNavigation();
                navigationConfig.setSubGroupId(subGroup.getId());
                //navigationConfig.setUserId(null);
                navigationConfig.setOrganizationId(subGroup.getOrganizationId());
                navigationConfig.setUserId("dataSync");
                navigationRepository.save(navigationConfig);
            }
        }
    }
    public void addSpecialSubjectAndProject(String id,SubGroup subGroup,String parentId){
        SpecialSubject subject = specialSubjectRepository.findOne(id);
        if (ObjectUtil.isNotEmpty(subject.getIndexTabMappers())){
            for (SpecialProject specialProject : subject.getIndexTabMappers()){
                SpecialProject newSpecial = specialProject.copyForSubGroup(subGroup.getId());
                newSpecial.setOrganizationId(subGroup.getOrganizationId());
                newSpecial.setUserId("dataSync");
                newSpecial.setGroupId(parentId);
                this.specialProjectRepository.save(newSpecial);
            }
        }
        if (ObjectUtil.isNotEmpty(subject.getChildrenPage())){
                for (SpecialSubject specialSubject : subject.getChildrenPage()){
                    SpecialSubject subject1 = specialSubject.pageCopy();
                    subject1.setSubGroupId(subGroup.getId());
                    subject1.setSubjectId(parentId);
                    subject1.setUserId("dataSync");
                    this.specialSubjectRepository.save(subject1);

                    addSpecialSubjectAndProject(specialSubject.getId(),subGroup,subject1.getId());
                }
        }
    }
    public void addCloumPageAndTab(String id,SubGroup subGroup,String parentId){
        IndexPage indexPage = indexPageRepository.findOne(id);
        if (ObjectUtil.isNotEmpty(indexPage.getIndexTabMappers())){
            for (IndexTabMapper indexTabMapper : indexPage.getIndexTabMappers()){
                IndexTab indexTab = indexTabMapper.getIndexTab().tabCopy();
                indexTab.setOrganizationId(subGroup.getOrganizationId());
                indexTab.setUserId("dataSync");
                indexTab.setSubGroupId(subGroup.getId());
                indexTab.setParentId(parentId);
                IndexTab save = indexTabRepository.save(indexTab);
                // 保存映射表
                IndexTabMapper mapper = save.mapper(false);
                if(StringUtil.isNotEmpty(save.getParentId())){
                    IndexPage indexPage2 = indexPageRepository.findOne(save.getParentId());
                    mapper.setIndexPage(indexPage2);
                }
                mapper.setTypeId(indexTab.getTypeId());
                mapper.setSubGroupId(subGroup.getId());
                indexTabMapperRepository.save(mapper);
            }
        }
        if (ObjectUtil.isNotEmpty(indexPage.getChildrenPage())){
            for (IndexPage indexPage1 : indexPage.getChildrenPage()){
                IndexPage indexPage2 = indexPage1.pageCopy();
                indexPage2.setSubGroupId(subGroup.getId());
                indexPage2.setParentId(parentId);
                this.indexPageRepository.save(indexPage2);
                addCloumPageAndTab(indexPage1.getId(),subGroup,indexPage2.getId());
            }
        }
    }
    public void copySomePageAndTabToUserGroupNew(String[] columnSync,String[] columnSyncLevel, SubGroup subGroup) {
        if (ObjectUtil.isNotEmpty(columnSync)){
            //同步无分组专题
//            List<IndexTab> projects = indexTabRepository.findByIdIn(Arrays.asList(columnSync));
            List<IndexTabMapper> mappers = indexTabMapperRepository.findByIdIn(Arrays.asList(columnSync));
            for (IndexTabMapper indexTabMapper : mappers){
                IndexTab indexTab = indexTabMapper.getIndexTab().tabCopy();
                indexTab.setOrganizationId(subGroup.getOrganizationId());
                indexTab.setUserId("dataSync");
                indexTab.setSubGroupId(subGroup.getId());
                IndexTab save = indexTabRepository.save(indexTab);
                // 保存映射表
                IndexTabMapper mapper = save.mapper(false);
                mapper.setSequence(indexTabMapper.getSequence());
                mapper.setTopFlag(indexTabMapper.getTopFlag());
                if(StringUtil.isNotEmpty(save.getParentId())){
                    IndexPage indexPage2 = indexPageRepository.findOne(save.getParentId());
                    mapper.setIndexPage(indexPage2);
                }
                mapper.setTypeId(indexTab.getTypeId());
                mapper.setSubGroupId(subGroup.getId());
                indexTabMapperRepository.save(mapper);
            }
        }
        if (ObjectUtil.isNotEmpty(columnSyncLevel)){
            List<IndexPage> indexPageList = indexPageRepository.findByIdIn(Arrays.asList(columnSyncLevel));
            if (ObjectUtil.isNotEmpty(indexPageList)){
                for (IndexPage indexPage : indexPageList) {
                    if (0 == indexPage.getFlag() && StringUtil.isNotEmpty(indexPage.getId())) {
                        //分组
                        IndexPage indexPage2 = indexPage.pageCopy();
                        indexPage2.setSubGroupId(subGroup.getId());
                        this.indexPageRepository.save(indexPage2);
                        addCloumPageAndTab(indexPage.getId(),subGroup,indexPage2.getId());
                    }
                }
            }
        }
    }
    public void copySomeSpecialToUserGroup(String[] specialSync, String[] specialSyncLevel, SubGroup subGroup) {
        if (ObjectUtil.isNotEmpty(specialSync)){
            //同步无分组专题
            List<SpecialProject> projects = specialProjectRepository.findByIdIn(Arrays.asList(specialSync));
            for (SpecialProject project : projects) {
                SpecialProject newSpecial = project.copyForSubGroup(subGroup.getId());
                newSpecial.setOrganizationId(subGroup.getOrganizationId());
                newSpecial.setUserId("dataSync");
                this.specialProjectRepository.save(newSpecial);
            }
        }
        if (ObjectUtil.isNotEmpty(specialSyncLevel)){
            //只有专题分组
            List<SpecialSubject> dataSyncSpecials = specialSubjectRepository.findByIdIn(Arrays.asList(specialSyncLevel));
            if (ObjectUtil.isNotEmpty(dataSyncSpecials)){
                for (SpecialSubject dataSyncSpecial : dataSyncSpecials) {
                    if (0 == dataSyncSpecial.getFlag() && StringUtil.isNotEmpty(dataSyncSpecial.getId())) {
                        SpecialSubject subject1 = dataSyncSpecial.pageCopy();
                        subject1.setSubGroupId(subGroup.getId());
                        subject1.setUserId("dataSync");
                        this.specialSubjectRepository.save(subject1);
                        //分组
                      addSpecialSubjectAndProject(dataSyncSpecial.getId(),subGroup,subject1.getId());
                    }
                }
            }
        }
     /*   //有级别
        if (StringUtil.isNotEmpty(specialSyncLevel)){
            //同步 有 分组 专题
            List<DataSyncSpecial> dataSyncSpecials = JSONArray.parseArray(specialSyncLevel, DataSyncSpecial.class);
            if (ObjectUtil.isNotEmpty(dataSyncSpecials)){
                //List<String> firstIds = new ArrayList<>();
                for (DataSyncSpecial dataSyncSpecial : dataSyncSpecials) {
                    //同步一级的
                    if (0 == dataSyncSpecial.getFlag() && StringUtil.isNotEmpty(dataSyncSpecial.getId())){
                        //一级分组id
                        //firstIds.add(dataSync.getOneId());
                        //添加一级分组
                        SpecialSubject subject = specialSubjectRepository.findOne(dataSyncSpecial.getId());
                        SpecialSubject newInstance = subject.newInstanceForSubGroup(subGroup.getId());
                        newInstance.setOrganizationId(subGroup.getOrganizationId());
                        newInstance.setUserId("dataSync");
                        SpecialSubject newSubject = this.specialSubjectRepository.save(newInstance);

                        //一级 下面 的 专题 或者 二级分组
                        List<DataSyncSpecial> childs = dataSyncSpecial.getZhuantiDetail();

                        if (ObjectUtil.isNotEmpty(childs)){
                            for (DataSyncSpecial child : childs) {
                                if (1 == child.getFlag()){
                                    //同步 一级 下 的 二级分组
                                    //添加二级分组id
                                    //添加 二级下的 分组
                                    SpecialSubject secondSub = specialSubjectRepository.findOne(child.getId());
                                    SpecialSubject newsecondSub = secondSub.newInstanceForSubGroup(subGroup.getId());
                                    newsecondSub.setOrganizationId(subGroup.getOrganizationId());
                                    newsecondSub.setSubjectId(newSubject.getId());
                                    newsecondSub.setUserId("dataSync");
                                    SpecialSubject newSubSubject = this.specialSubjectRepository.save(newsecondSub);

                                    //二级下的 专题
                                    List<DataSyncSpecial> childs1 = child.getZhuantiDetail();
                                    for (DataSyncSpecial sync : childs1) {
                                        //二级分组下的三级 专题
                                        //thirdIds.add(sync.getOneId());
                                        // 保存专题
                                        SpecialProject specialProject = specialProjectRepository.findOne(sync.getId());
                                        SpecialProject newProject = specialProject.newInstanceForSubGroup(newSubSubject.getId(),
                                                subGroup.getId());
                                        newProject.setOrganizationId(subGroup.getOrganizationId());
                                        newProject.setUserId("dataSync");
                                        this.specialProjectRepository.save(newProject);
                                    }
                                }else if (2==child.getFlag()){
                                    //三级 专题
                                    //thirdIds.add(child.getOneId());
                                    SpecialProject specialProject = this.specialProjectRepository.findOne(child.getId());
                                    SpecialProject newProject = specialProject.newInstanceForSubGroup(newSubject.getId(),
                                            subGroup.getId());
                                    newProject.setOrganizationId(subGroup.getOrganizationId());
                                    newProject.setUserId("dataSync");
                                    this.specialProjectRepository.save(newProject);
                                }
                            }
                        }
                    }
                }

            }
        }*/
    }

    public int getSubGroupColumnCountForSubGroup(String subGroupId) {
        List<IndexTab> bySubGroupId = indexTabRepository.findBySubGroupId(subGroupId);
        if (ObjectUtil.isNotEmpty(bySubGroupId)){
            return bySubGroupId.size();
        }
        return 0;
    }

    public List<AlertAccount> findByUserId(String userId) {
        return alertAccountRepository.findByUserId(userId,new Sort(Sort.Direction.DESC, "createdTime"));
    }

    /**
     * 删除 Page
     * @param userId
     */
    @Transactional
    public void deletePageByUserId(String userId) {
        List<IndexPage> indexPages = indexPageRepository.findByUserId(userId);
        if (ObjectUtil.isNotEmpty(indexPages)){
            for (IndexPage indexPage : indexPages) {
                String indexPageId = indexPage.getId();
                // 删除栏目组及下级子栏目
                List<IndexTabMapper> mappers = findByIndexPageId(indexPageId);
                if (CollectionsUtil.isNotEmpty(mappers)) {
                    for (IndexTabMapper mapper : mappers) {

                        // 删除栏目映射关系，isMe为true的栏目关系须级联删除栏目实体
                        List<IndexTabMapper> findByIndexTab = tabMapperRepository.findByIndexTab(mapper.getIndexTab());
                        //删除所有与indexTab关联的  否则剩余关联则删除indexTab时失败
                        tabMapperRepository.delete(findByIndexTab);
                        if (mapper.isMe()) {
                            indexTabRepository.delete(mapper.getIndexTab());
                        }
                    }
                }
                // 删除栏目组
                indexPageRepository.delete(indexPageId);
            }
        }
    }

    public List<IndexTabMapper> findByIndexPageId(String indexPageId) {
        IndexPage indexPage = indexPageRepository.findOne(indexPageId);
        return tabMapperRepository.findByIndexPage(indexPage);
    }

    /**
     * 删除 Mapper
     * @param userId
     * @throws TRSException
     */
    public void deleteMapperByUserId(String userId) throws TRSException {
        List<IndexTabMapper> indexTabMappers = tabMapperRepository.findByUserId(userId);
        if (ObjectUtil.isNotEmpty(indexTabMappers)){
            for (IndexTabMapper indexTabMapper : indexTabMappers) {
                this.deleteMapper(indexTabMapper.getId());
            }
//			tabMapperRepository.delete(indexTabMappers);
//			tabMapperRepository.flush();
        }
    }
    @javax.transaction.Transactional
    public void deleteMapper(String indexMapperId) throws TRSException {
        try {
            if (StringUtils.isNotBlank(indexMapperId)) {
                // 多个删除用;分割
                String[] idsplit = indexMapperId.split(";");
                for (String ids : idsplit) {
                    IndexTabMapper mapper = tabMapperRepository.findOne(ids);
                    // 修改顺序
                    if (mapper != null && mapper.getIndexPage() != null) {
                        List<IndexTabMapper> mappers = tabMapperRepository.findByIndexPage(mapper.getIndexPage());
                        if (CollectionsUtil.isNotEmpty(mappers)) {
                            for (IndexTabMapper indexTabMapper : mappers) {
                                if (indexTabMapper.getSequence() > mapper.getSequence()) {
                                    indexTabMapper.setSequence(indexTabMapper.getSequence() - 1);
                                }
                            }
                        }
                        // 考虑加锁 一个一个存
                        tabMapperRepository.save(mappers);
                        if (mapper.isMe()) {
                            // 删除所有相关的mapper
                            List<IndexTabMapper> aboutMapper = tabMapperRepository.findByIndexTab(mapper.getIndexTab());
                            tabMapperRepository.delete(aboutMapper);
                            indexTabRepository.delete(mapper.getIndexTab().getId());
                        } else {
                            // 引用栏目只删除本身
                            tabMapperRepository.delete(mapper);
                        }
                    }
                }

            }
        } catch (Exception e) {
            throw new TRSException(e);
        }
    }

    /**
     * 删除 Subject
     *
     */
    public void deleteSubjectByUserId(String userId) {
        List<SpecialSubject> specialSubjects = specialSubjectRepository.findByUserId(userId);
        if (ObjectUtil.isNotEmpty(specialSubjects)){
            specialSubjectRepository.delete(specialSubjects);
            specialSubjectRepository.flush();
        }
    }

    /**
     * 删除 Project
     */
    public void deleteProjectByUserId(String userId) {
        List<SpecialProject> specialProjects = specialProjectRepository.findByUserId(userId);
        if (ObjectUtil.isNotEmpty(specialProjects)){
            specialProjectRepository.delete(specialProjects);
            specialProjectRepository.flush();
        }
    }

    /**
     * 删除 预警规则
     */
    public void deleteAlertRuleByUserId(String userId){
        List<AlertRule> alertRules = alertRuleRepository.findByUserId(userId);
        if (ObjectUtil.isNotEmpty(alertRules)){
            alertRuleRepository.delete(alertRules);
            alertRuleRepository.flush();
        }
    }

    /**
     * 删除预警账号
     * @param userId
     * @return
     */
    public List<AlertAccount> findAlertAccountByUserId(String userId) {
        return alertAccountRepository.findByUserId(userId,new Sort(Sort.Direction.DESC, "createdTime"));
    }

    public void deleteAlertAccount(List<AlertAccount> list) {
        alertAccountRepository.delete(list);
        alertAccountRepository.flush();
    }




}
