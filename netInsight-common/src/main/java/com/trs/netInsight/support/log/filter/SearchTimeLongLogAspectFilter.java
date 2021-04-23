package com.trs.netInsight.support.log.filter;

import com.trs.netInsight.support.kafka.util.KafkaUtil;
import com.trs.netInsight.support.log.entity.SearchTimeLongLog;
import com.trs.netInsight.support.log.entity.enums.SearchLogType;
import com.trs.netInsight.support.log.handler.SearchLog;
import com.trs.netInsight.util.DateUtil;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author 马加鹏
 * @date 2021/4/22 9:19
 */
@Aspect
@Order(-90)
@Component
@Slf4j
public class SearchTimeLongLogAspectFilter {

    @Qualifier("organizationRepository")
    @Autowired
    private OrganizationRepository organizationRepository;

    /**
     * 启动线程池来向Kafka进行日志存入操作
     */
    ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Around(value = "@annotation(searchLog)")
    public Object doAround(ProceedingJoinPoint pjp, SearchLog searchLog) {
        //获取HttpServletRequest请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        //类型
        SearchLogType searchLogType = searchLog.searchLogType();

        Object proceed = null;
        // hybase查询的语句
        String trsl = null;
        String modelName = null;
        String timeRange = null;

        try {
            proceed = pjp.proceed();
            String[] strings = getStrings(request);
            trsl = strings[0];
            modelName = strings[1];
            timeRange = strings[2];
            String[] times = DateUtil.formatTimeRange(timeRange);
            int searchTime = DateUtil.getBetweenDays(times[0], times[1]);
//            if (searchTime < 30 * 3) {//小于90天不记录日志
//                return proceed;
//            }
            User currentUser = UserUtils.getUser();
            String organizationName = null;
            if (StringUtil.isNotEmpty(currentUser.getOrganizationId())) {
                Organization organization = organizationRepository.findOne(currentUser.getOrganizationId());
                if (ObjectUtil.isNotEmpty(organization)) {
                    organizationName = organization.getOrganizationName();
                }
            }
            SearchTimeLongLog searchTimeLongLog = new SearchTimeLongLog(organizationName, searchLogType.getValue(), modelName, trsl, timeRange, searchTime);
            addUserInfo(searchTimeLongLog);
            Task task = new Task(searchTimeLongLog);
            executorService.execute(task);
            return proceed;
        } catch (Throwable throwable) {
            return throwable;
        }
    }

    private void addUserInfo(SearchTimeLongLog searchTimeLongLog){

        User user = UserUtils.getUser();
        if(StringUtils.isNotBlank(user.getId())){
            searchTimeLongLog.setCreatedUserId(user.getId());
        }
        if(StringUtils.isNotBlank(user.getUserName())){
            searchTimeLongLog.setCreatedUserName(user.getUserName());
        }
        if(StringUtils.isNotBlank(user.getDisplayName())){
            searchTimeLongLog.setDisplayName(user.getDisplayName());
        }
        if (StringUtils.isNotBlank(user.getOrganizationId())) {
            searchTimeLongLog.setOrganizationId(user.getOrganizationId());
        }
        if (StringUtils.isNotBlank(user.getSubGroupId())){
            searchTimeLongLog.setSubGroupId(user.getSubGroupId());
        }
    }

    /**
     * 从request中获取查询hybase的trsl表达式、栏目名称或者专题名称、检索时间范围、检索时间范围差
     * @param request
     * @return
     */
    private String[] getStrings(HttpServletRequest request) {
        Object trslObj = request.getAttribute("search_time_long_trsl");
        Object modelNameObj = request.getAttribute("model_name_123");
        Object timeRangeObj = request.getAttribute("timeRange_123");
        String strs[] = new String[3];
        if (trslObj instanceof String) {
            request.removeAttribute("search_time_long_trsl");
            strs[0] = trslObj.toString();
        }
        if (modelNameObj instanceof String) {
            request.removeAttribute("model_name_123");
            strs[1] = modelNameObj.toString();
        }
        if (timeRangeObj instanceof String) {
            request.removeAttribute("timeRange_123");
            strs[2] = timeRangeObj.toString();
        }
        return strs;
    }

}

class Task implements Runnable {

    private SearchTimeLongLog searchTimeLongLog;

    public Task() {
    }

    public Task(SearchTimeLongLog searchTimeLongLog) {
        this.searchTimeLongLog = searchTimeLongLog;
    }

    @Override
    public void run() {
        KafkaUtil.send(searchTimeLongLog);
    }
}
