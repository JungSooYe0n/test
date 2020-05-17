package com.trs.netInsight.widget.microblog.service.impl;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.microblog.entity.SingleMicroblogData;
import com.trs.netInsight.widget.microblog.repository.SingleMicroblogDataRepository;
import com.trs.netInsight.widget.microblog.service.ISingleMicroblogDataService;
import com.trs.netInsight.widget.microblog.service.ISingleMicroblogService;
import com.trs.netInsight.widget.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 单条微博分析数据业务层实现
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/2/13.
 */
@Service
public class SingleMicroblogDataServiceImpl implements ISingleMicroblogDataService {

    @Autowired
    private SingleMicroblogDataRepository singleMicroblogDataRepository;

    @Autowired
    private ISingleMicroblogService singleMicroblogService;

    @Override
    public SingleMicroblogData findOne(String id) {
        return singleMicroblogDataRepository.findOne(id);
    }

    @Override
    public void remove(String id) {
        SingleMicroblogData microblogData = singleMicroblogDataRepository.findOne(id);
        if (ObjectUtil.isNotEmpty(microblogData)){
            singleMicroblogDataRepository.delete(singleMicroblogDataRepository.findAllByUserIdAndCurrentUrl(microblogData.getUserId(),microblogData.getCurrentUrl()));
        }
       // singleMicroblogDataRepository.delete(id);
    }

    @Override
    public void delete(String id) {
        singleMicroblogDataRepository.delete(id);
    }

    @Override
    public SingleMicroblogData insert(SingleMicroblogData singleMicroblogData) {
        return singleMicroblogDataRepository.insert(singleMicroblogData);
    }

    @Override
    public SingleMicroblogData save(SingleMicroblogData singleMicroblogData) {
        return singleMicroblogDataRepository.save(singleMicroblogData);
    }

    @Override
    public SingleMicroblogData findSMDBySth(User user,String currentUrl,String name,String random) {
        List<SingleMicroblogData> singleMicroblogData = null;
        if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
            singleMicroblogData = singleMicroblogDataRepository.findByUserIdAndCurrentUrlAndNameAndRandom(user.getId(), currentUrl, name,random);
        }else {
            singleMicroblogData = singleMicroblogDataRepository.findBySubGroupIdAndCurrentUrlAndNameAndRandom(user.getSubGroupId(), currentUrl, name,random);
        }
        if (ObjectUtil.isEmpty(singleMicroblogData)){
            return null;
        }
        return singleMicroblogData.get(0);
    }

    @Override
    public SingleMicroblogData findSMDBySthNoRandom(User user, String currentUrl, String name) {
        List<SingleMicroblogData> singleMicroblogData = null;
        if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
           singleMicroblogData = singleMicroblogDataRepository.findByUserIdAndCurrentUrlAndName(user.getId(), currentUrl, name);
        }else {
            singleMicroblogData = singleMicroblogDataRepository.findBySubGroupIdAndCurrentUrlAndName(user.getSubGroupId(), currentUrl, name);
        }
        if (ObjectUtil.isEmpty(singleMicroblogData)){
            return null;
        }
        return singleMicroblogData.get(0);
    }

    @Override
    public List<SingleMicroblogData> findAllSMD(String userId, String currentUrl) {

        return singleMicroblogDataRepository.findAllByUserIdAndCurrentUrl(userId,currentUrl);
    }

    @Override
    public List<SingleMicroblogData> findAllSMDWithRandom(User user, String currentUrl, String random) {
        if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
            return singleMicroblogDataRepository.findAllByUserIdAndCurrentUrlAndRandom(user.getId(),currentUrl,random);
        }else {
            return singleMicroblogDataRepository.findAllBySubGroupIdAndCurrentUrlAndRandom(user.getSubGroupId(),currentUrl,random);

        }
    }

    @Override
    public List<SingleMicroblogData> findAll(User user, String name, Sort sort) throws TRSException {
        if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
            List<SingleMicroblogData> states = singleMicroblogDataRepository.findByUserIdAndNameAndState(user.getId(),name, "分析中",sort);
            if (ObjectUtil.isEmpty(states) && states.size() ==0){
                List<SingleMicroblogData> statesList = singleMicroblogDataRepository.findByUserIdAndNameAndState(user.getId(),name, "正在排队",sort);
                if (ObjectUtil.isNotEmpty(statesList)){
                    SingleMicroblogData microblogData = statesList.get(statesList.size()-1);
                    microblogData.setState("分析中");
                    singleMicroblogDataRepository.save(microblogData);
                    singleMicroblogService.dataAnalysis(microblogData.getOriginalUrl(),microblogData.getCurrentUrl(),microblogData.getRandom());
                }
            }
            return singleMicroblogDataRepository.findAllByUserIdAndName(user.getId(),name,sort);
        }else {
            List<SingleMicroblogData> states = singleMicroblogDataRepository.findBySubGroupIdAndNameAndState(user.getSubGroupId(),name, "分析中",sort);
            if (ObjectUtil.isEmpty(states) && states.size() ==0){
                List<SingleMicroblogData> statesList = singleMicroblogDataRepository.findBySubGroupIdAndNameAndState(user.getSubGroupId(),name, "正在排队",sort);
                if (ObjectUtil.isNotEmpty(statesList)){
                    SingleMicroblogData microblogData = statesList.get(statesList.size()-1);
                    microblogData.setState("分析中");
                    singleMicroblogDataRepository.save(microblogData);
                    singleMicroblogService.dataAnalysis(microblogData.getOriginalUrl(),microblogData.getCurrentUrl(),microblogData.getRandom());
                }
            }
            return singleMicroblogDataRepository.findAllBySubGroupIdAndName(user.getSubGroupId(),name,sort);
        }

    }

    @Override
    public List<SingleMicroblogData> findStates(User user,String name, String state) {
        Sort sort = new Sort(Sort.Direction.DESC, "createdTime");
        if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
            return singleMicroblogDataRepository.findByUserIdAndNameAndState(user.getId(),name,state,sort);
        }else {
            return singleMicroblogDataRepository.findBySubGroupIdAndNameAndState(user.getSubGroupId(),name,state,sort);
        }
    }

    @Override
    public List<SingleMicroblogData> findByUserId(String userId) {
        return singleMicroblogDataRepository.findByUserId(userId);
    }

    @Override
    public void updateAll(List<SingleMicroblogData> singleMicroblogData) {
        for (SingleMicroblogData singleMicroblogDatum : singleMicroblogData) {
            singleMicroblogDataRepository.save(singleMicroblogDatum);

        }
    }
}
