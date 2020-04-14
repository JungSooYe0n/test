package com.trs.netInsight.support.log.service.impl;

import com.trs.jpa.utils.Criteria;
import com.trs.jpa.utils.Restrictions;
import com.trs.netInsight.support.log.entity.LoginFrequencyLog;
import com.trs.netInsight.support.log.repository.LoginFrequencyLogRepository;
import com.trs.netInsight.support.log.service.ILoginFrequencyLogService;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import com.trs.netInsight.widget.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class LoginFrequencyLogServiceImpl implements ILoginFrequencyLogService {
    @Autowired
    private LoginFrequencyLogRepository loginFrequencyLogRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public void save(Integer num,String  id){
        User user = userRepository.findOne(id);
        Sort sort = new Sort(Sort.Direction.ASC,"createdTime");
        Criteria<LoginFrequencyLog> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("createdUserId",user.getId()));
        criteria.add(Restrictions.between("createdTime",getTimeOO(null,0),getTimeOO(null,-1)));
        List<LoginFrequencyLog> list = loginFrequencyLogRepository.findAll(criteria,sort);
        if(list != null && list.size() >0){
            //不管数据库里面怎么存的，以redis中的为准
            LoginFrequencyLog log = list.get(0);
            list.remove(0);
            if(list.size() >0){
                loginFrequencyLogRepository.delete(list);
            }
            log.setLoginNum(num);
            loginFrequencyLogRepository.saveAndFlush(log);

        }else{
            LoginFrequencyLog log = new LoginFrequencyLog();
            //方便历史数据
            log.setLoginNum(num);
            log.setOperationUserName(user.getUserName());
            loginFrequencyLogRepository.saveAndFlush(log);
        }
        Integer loginCount = loginFrequencyLogRepository.countOrganizationLoginNum(user.getOrganizationId());
        organizationRepository.updateOrganizationLoginCount(loginCount,user.getOrganizationId());


    }

    /**
     * 获取时间的 00:00:00
     * 没传时间则默认为 今天
     * @param date
     * @param beforeDay 之前几天  往前推时间，写正式数就是往前推 ，写负数，往后推
     * @return
     */
    private Date getTimeOO(Date date,Integer beforeDay){
        Calendar calendar = Calendar.getInstance();
        if(date == null){
            calendar.setTime(new Date());
        }else{
            calendar.setTime(date);
        }
        if(beforeDay != 0){
            calendar.set(Calendar.DAY_OF_MONTH,calendar.get(Calendar.DAY_OF_MONTH)-beforeDay);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date zero = calendar.getTime();
        return zero;
    }

}
