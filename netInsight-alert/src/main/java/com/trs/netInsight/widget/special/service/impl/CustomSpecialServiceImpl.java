package com.trs.netInsight.widget.special.service.impl;

import com.trs.netInsight.support.cache.PerpetualPool;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.special.entity.CustomSpecial;
import com.trs.netInsight.widget.special.entity.enums.SearchScope;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import com.trs.netInsight.widget.special.entity.repository.CustomSpecialRepository;
import com.trs.netInsight.widget.special.service.ICustomSpecialService;
import com.trs.netInsight.widget.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

/**
 * 舆情报告 极简模式 自定义专题 业务层接口实现类
 *
 ** @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/10/10.
 */
@Service
@Slf4j
@Transactional
public class CustomSpecialServiceImpl implements ICustomSpecialService {
    @Autowired
    private CustomSpecialRepository customSpecialProjectRepository;


    @Override
    public CustomSpecial save(CustomSpecial customSpecial) {
        return customSpecialProjectRepository.save(customSpecial);
    }

    @Override
    public Object getByUser(User user) {
        if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
            return customSpecialProjectRepository.findByUserId(user.getId(),new Sort(Sort.Direction.DESC,"lastModifiedTime"));
        }else {
            return customSpecialProjectRepository.findBySubGroupId(user.getSubGroupId(),new Sort(Sort.Direction.DESC,"lastModifiedTime"));
        }
    }

    @Override
    public Object getByUserId(int pageNo, int pageSize) {
        Pageable pageable = new PageRequest(pageNo, pageSize);
        return customSpecialProjectRepository.findByUserId(UserUtils.getUser().getId(),pageable);
    }

    @Override
    public void delete(String id) {
        customSpecialProjectRepository.delete(id);
    }

    @Override
    public CustomSpecial update(String specialId, SpecialType specialType, String specialName, String anyKeyWords, String excludeWords,String excludeWeb, String trsl, SearchScope searchScope, String timeRange, String source, boolean irSimflag,boolean irSimflagAll, boolean similar, boolean weight) {
        CustomSpecial customSpecial = this.findOne(specialId);
        customSpecial.setSpecialType(specialType);
        customSpecial.setSpecialName(specialName);
        customSpecial.setAnyKeyWords(anyKeyWords);
        customSpecial.setExcludeWords(excludeWords);
        customSpecial.setExcludeWeb(excludeWeb);
        customSpecial.setTrsl(trsl);
        customSpecial.setSearchScope(searchScope);
        customSpecial.setTimeRange(timeRange);
        customSpecial.setSource(source);
        customSpecial.setIrSimflag(irSimflag);
        customSpecial.setIrSimflagAll(irSimflagAll);
        customSpecial.setSimilar(similar);
        customSpecial.setWeight(weight);
        customSpecial = customSpecialProjectRepository.save(customSpecial);
        PerpetualPool.put(specialId, DateUtil.formatCurrentTime("yyyyMMddHHmmss"));
        return customSpecial;

    }

    @Override
    public CustomSpecial findOne(String id) {
        return customSpecialProjectRepository.findOne(id);
    }

    @Override
    public List<CustomSpecial> findBySimple(SpecialType specialType) {
        return customSpecialProjectRepository.findBySpecialType(SpecialType.COMMON);
    }


}
