package com.trs.netInsight.widget.spread.search;

import com.trs.dc.entity.TRSEsRecord;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.model.result.IDocument;
import com.trs.netInsight.support.template.GUIDGenerator;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.spread.entity.SinaUser;
import com.trs.netInsight.widget.spread.util.MultiKVMap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * ES检索服务
 */
@Slf4j
@Service
public class SpreadSearchService {

	@Autowired
	private FullTextSearch hybase8SearchService;
    /**
     * 获取ESClient IEsSearchOpenService
     *
     * @return IEsSearchOpenService
     */
//    private IEsSearchOpenService getSearchService(){
//        return ESClient.getClient().getSearchService();
//    }

    /**
     * 检索获取传播用户
     *
     * @param params TRSEsSearchParams
     * @param indices 库名
     * @return LinkedMultiValueMap
     */
//    public MultiKVMap<String, SinaUser> scrollForSpread(TRSEsSearchParams params, String ...indices)  throws Exception {
//        MultiKVMap<String, SinaUser> resultMap = new MultiKVMap<>();
//        params.setResultFields(getAnnotationField(SinaUser.class));
//        TRSEsRecordSet recordSet = getSearchService().scan(params, indices);
//        long total = 0L;
//        while (recordSet.getResultSize() > 0) {
//            for (TRSEsRecord record : recordSet.getResultSet()) {
//                SinaUser user = toDoc(record, SinaUser.class);
//                if (!StringUtil.isEmpty(user.getScreenName())
//                        && !user.getScreenName().equals(user.getFromScreenName()))
//                    resultMap.add(user.getFromScreenName(), user.getScreenName(), user);
//            }
//            recordSet = getSearchService().scanNextPage(recordSet.getScrollId());
//        }
//        log.info(params.getQuery() + "total: " + total);
//        return resultMap;
//    }
    /**
     * 检索获取传播用户
     *
     * @param indices 库名
     * @return LinkedMultiValueMap
     * @throws TRSException 
     * @throws TRSSearchException 
     */
    public MultiKVMap<String, SinaUser> scrollForSpreadNew(QueryBuilder builder, String indices) throws TRSSearchException, TRSException  {
        MultiKVMap<String, SinaUser> resultMap = new MultiKVMap<>();
//        params.setResultFields(getAnnotationField(SinaUser.class));
//        TRSEsRecordSet recordSet = getSearchService().scan(params, indices);
        builder.setDatabase(indices);
        List<SinaUser> ftsQuery = hybase8SearchService.ftsQuery(builder, SinaUser.class,true,false,false,null);
        log.info(builder.asTRSL());
//        long total = 0L;
//        while (ftsQuery.size() > 0) {
            for (SinaUser user : ftsQuery) {
//                SinaUser user = toDoc(record, SinaUser.class);
                if (!StringUtil.isEmpty(user.getName())
                        && !user.getName().equals(user.getFromScreenName())){
                	if(ObjectUtil.isNotEmpty(user.getFromScreenName())){
                		resultMap.add(user.getFromScreenName().get(0), user.getName(), user);
                	}
//                	else {
//                		resultMap.add(user.getFromScreenName()+"", user.getScreenName(), user);
//					}
                }
                    
            }
//            if(builder.getPageNo()<1){
//            	 builder.setPageNo(builder.getPageNo()+1);
//                 ftsQuery = hybase8SearchService.ftsQuery(builder, SinaUser.class);
//            }else {
//            	 return resultMap;
//			}
//        }
//        log.info(params.getQuery() + "total: " + total);
        return resultMap;
    }

    /**
     * list检索
     *
     * @return List结果集
     * @throws TRSException 
     * @throws TRSSearchException 
     */
//    public <T extends IDocument> List<T> list(TRSEsSearchParams params, Class<T> clazz, String ...indices) throws Exception {
//        params.setResultFields(getAnnotationField(clazz));
//        TRSEsRecordSet recordSet = getSearchService().querySearch(params, indices);
//        if (recordSet.getNumFound() == 0)
//            return null;
//        List<TRSEsRecord> resultSet = recordSet.getResultSet();
//        List<T> resultList = new ArrayList<T>();
//        for (TRSEsRecord record : resultSet)
//            resultList.add(toDoc(record, clazz));
//        return resultList;
//    }
    public <T extends IDocument> List<T> listNew(QueryBuilder builder, Class<T> clazz, String indices) throws TRSSearchException, TRSException  {
        builder.setDatabase(indices);
        List<T> ftsQuery = hybase8SearchService.ftsQuery(builder, clazz,true,false,false,null);
        return ftsQuery;
    }
    @SuppressWarnings("unused")
	private <T extends IDocument> T toDoc(TRSEsRecord record, Class<T> clazz) throws Exception {
        T instance = clazz.newInstance();
        instance.setId(GUIDGenerator.generateName());
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(FtsField.class)) {
                String esField = field.getAnnotation(FtsField.class).value();
                for (String name : esField.split(";")) {
                    String value = record.getString(name);
                    if (!StringUtil.isEmpty(value)) {
                        Method method = clazz.getMethod(toSet(field.getName()), String.class);
                        method.invoke(instance, value);
                    }
                }
            }
        }
        return instance;
    }
    @SuppressWarnings("unused")
	private <T extends IDocument> T toDocNew(SinaUser record, Class<T> clazz) throws Exception {
        T instance = clazz.newInstance();
        instance.setId(GUIDGenerator.generateName());
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(FtsField.class)) {
                String esField = field.getAnnotation(FtsField.class).value();
                for (String name : esField.split(";")) {
//                    String value = record.getString(name);
                	String screenName=record.getName();
                	String fromScreen=record.getFromScreenName().get(0);
                	String value=name;
                    if (!StringUtil.isEmpty(value)) {
                        Method method = clazz.getMethod(toSet(field.getName()), String.class);
                        method.invoke(instance, value);
                    }
                }
            }
        }
        return instance;
    }

    /**
     * 获取注解标识的字段
     *
     * @param clazz ? extends BaseESEntity
     * @return List
     */
    @SuppressWarnings("unused")
	private static String[] getAnnotationField(Class<? extends IDocument> clazz) throws Exception {
        Field[] fields = clazz.getDeclaredFields();
        List<String> resultList = new ArrayList<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(FtsField.class)) {
                String[] f = field.getAnnotation(FtsField.class).value().split(";");
                resultList.addAll(Arrays.asList(f));
            }
        }
        String[] result = new String[resultList.size()];
        return resultList.toArray(result);

    }

    private static String toSet(String str){
        char[] charArray = str.toCharArray();
        charArray[0] = (char)(charArray[0] +'A' -'a');
        return "set" + String.valueOf(charArray);
    }

}
