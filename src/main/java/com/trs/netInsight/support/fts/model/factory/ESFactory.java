package com.trs.netInsight.support.fts.model.factory;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.trs.dc.openservice.IEsDataOpenService;
import com.trs.dc.openservice.IEsDatabaseOpenService;
import com.trs.dc.openservice.IEsSearchOpenService;
import com.trs.netInsight.support.fts.constant.FTSConst;

/**
 * ES服务工厂类
 *
 * Created by leeyao on 2017/4/19.
 */
public class ESFactory {

    /**
     * 确保单例不会被系统其他代码实例化
     */
    private ESFactory() {
    }

    /**
     * 内部类维护单例，确保单例类调用时初始化
     */
    private static class SingletonHolder {
        private static ApplicationContext context = new ClassPathXmlApplicationContext(FTSConst.ES_BEAN_FILE);
    }

    /**
     * 单例获取
     *
     * @return ApplicationContext
     */
    private static ApplicationContext getApplicationContext() {
        return SingletonHolder.context;
    }

    /**
     * 获取ES检索服务类
     *
     * @return IEsSearchOpenService
     */
    public static IEsSearchOpenService getSearchService() {
        return (IEsSearchOpenService) getApplicationContext().getBean(FTSConst.ES_SEARCH_SERVICE);
    }

    /**
     * 获取ES库管理服务类
     *
     * @return IEsSearchOpenService
     */
    public static IEsDatabaseOpenService getDBService() {
        return (IEsDatabaseOpenService) getApplicationContext().getBean(FTSConst.ES_DB__SERVICE);
    }

    /**
     * 获取ES数据管理类
     *
     * @return IEsSearchOpenService
     */
    public static IEsDataOpenService getDataService() {
        return (IEsDataOpenService) getApplicationContext().getBean(FTSConst.ES_DATE_SERVICE);
    }

}


