package com.trs.netInsight.support.api.service.impl;

import com.trs.netInsight.support.api.entity.ApiFrequencyLog;
import com.trs.netInsight.support.api.entity.repository.IApiFrequencyLogRepository;
import com.trs.netInsight.support.api.service.IApiFrequencyLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ApiFrequencyLogServiceImpl implements IApiFrequencyLogService {

    @Autowired
    private IApiFrequencyLogRepository frequencyLogRepository;

    @Override
    public void recordFrequency(String  questClientId, String questOrgId, Integer questMethodCode,String questMethod){
        Date nowTime = new Date();
        Date time_day = subTime(nowTime,"day");//取时间 - 今天
        Date time_hour = subTime(nowTime,"hour");//取时间 - 当前小时
        ApiFrequencyLog afLog_allDay = this.findOneByCondition(questMethodCode.toString(),null,"day",time_day,false); //取当前天接口被调用总数
        if(afLog_allDay == null ){
            this.save(questClientId,null,questMethodCode.toString(),questMethod,"day",time_day);
        }else{
            this.update(afLog_allDay);
        }
        ApiFrequencyLog afLog_allHour = this.findOneByCondition(questMethodCode.toString(),null,"hour",time_hour,false); //取当前小时接口被调用总数
        if(afLog_allHour == null ){
            this.save(questClientId,null,questMethodCode.toString(),questMethod,"hour",time_hour);
        }else{
            this.update(afLog_allHour);
        }
        if(questOrgId != null && !"".equals(questOrgId)){
            ApiFrequencyLog afLog_nowDay = this.findOneByCondition(questMethodCode.toString(),questOrgId,"day",time_day,true); //取当前天接口被调用总数
            if(afLog_nowDay == null ){
                this.save(questClientId,questOrgId,questMethodCode.toString(),questMethod,"day",time_day);
            }else{
                this.update(afLog_nowDay);
            }
            ApiFrequencyLog afLog_nowHour = this.findOneByCondition(questMethodCode.toString(),questOrgId,"hour",time_hour,true); //取当前小时接口被调用总数
            if(afLog_nowHour == null ){
                this.save(questClientId,questOrgId,questMethodCode.toString(),questMethod,"hour",time_hour);
            }else{
                this.update(afLog_nowHour);
            }
        }
    }


    private ApiFrequencyLog save(String  questClientId, String questOrgId, String questMethodCode,String questMethod, String type, Date questTime) {
        ApiFrequencyLog log = new ApiFrequencyLog();
        log.setCreatedTime(new Date());
        log.setQuestClientId(questClientId);
        log.setQuestOrgId(questOrgId);
        log.setQuestMethod(questMethod);
        log.setQuestMethodCode(questMethodCode);
        log.setTimeType(type);
        log.setQuestFrequency(1);
        log.setQuestTime(questTime);
        ApiFrequencyLog add = frequencyLogRepository.save(log);
        return add;
    }


    private ApiFrequencyLog update(ApiFrequencyLog frequencyLog) {
        frequencyLog.setQuestFrequency(frequencyLog.getQuestFrequency()+1);
        frequencyLog.setLastModifiedTime(new Date());
        ApiFrequencyLog save = frequencyLogRepository.saveAndFlush(frequencyLog);
        return save;
    }


    private ApiFrequencyLog findOneByCondition(String methodCode, String orgId, String timeType, Date date,Boolean isOrg) {
        Specification<ApiFrequencyLog> criteria = new Specification<ApiFrequencyLog>() {
            @Override
            public Predicate toPredicate(Root<ApiFrequencyLog> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Object> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("questMethodCode"),methodCode));
                //predicates.add(cb.isNull(root.get("libraryId")));
                if(isOrg){
                    predicates.add(cb.equal(root.get("questOrgId"), orgId));
                }else{
                    predicates.add(cb.isNull(root.get("questOrgId")));
                }
                predicates.add(cb.equal(root.get("timeType"), timeType));
                predicates.add(cb.equal(root.get("questTime"), date));
                Predicate[] pre = new Predicate[predicates.size()];
                return query.where(predicates.toArray(pre)).getRestriction();
            }
        };
        List<ApiFrequencyLog> apiFrequencyLog = frequencyLogRepository.findAll(criteria);
        if(apiFrequencyLog != null && apiFrequencyLog.size() > 0){
            return apiFrequencyLog.get(0);
        }
        return null;
    }


    private static Date subTime(Date time,String type){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            if("hour".equals(type)){
                time = sdf.parse(sdf.format(time).substring(0,13)+":00:00");
            }else if("day".equals(type)){
                time = sdf.parse(sdf.format(time).substring(0,10)+" 00:00:00");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }
}
