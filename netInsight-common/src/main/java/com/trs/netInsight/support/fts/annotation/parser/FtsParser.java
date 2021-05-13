package com.trs.netInsight.support.fts.annotation.parser;

import com.trs.dc.entity.TRSEsRecord;
import com.trs.hybase.client.TRSException;
import com.trs.hybase.client.TRSRecord;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.annotation.enums.FtsHybaseType;
import com.trs.netInsight.support.fts.model.result.IDocument;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import javax.management.OperationsException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 全文检索注解解析器
 * <p>
 * Created by trs on 2017/8/10.
 */
public final class FtsParser {

    /**
     * 解析检索实体中的数据库名
     *
     * @param tClass IDocument
     * @param <T>    IDocument子类
     * @return String
     */
    public static <T extends IDocument> String getDatabases(Class<T> tClass) throws TRSSearchException {
        if (!tClass.isAnnotationPresent(FtsClient.class)) {
            throw new TRSSearchException("查询实体必须被@FtsClient注解标注");
        }
        FtsClient ftsClient = tClass.getAnnotation(FtsClient.class);
        // 反射获取注解的数据库名就是indices值
        String indices = ftsClient.indices();
        if(StringUtil.isNotEmpty(indices)){
            return indices;
        }else{
            FtsHybaseType hybaseType = ftsClient.hybaseType();
            if(hybaseType.equals(FtsHybaseType.ALERT)){
                indices = Const.ALERT;
            }else if(hybaseType.equals(FtsHybaseType.ALERT_TYPE)){
                indices = Const.ALERTTYPE;
            }else if(hybaseType.equals(FtsHybaseType.TRADITIONAL)){
                indices = Const.HYBASE_NI_INDEX;
            }else if(hybaseType.equals(FtsHybaseType.WEIBO)){
                indices = Const.WEIBO;
            }else if(hybaseType.equals(FtsHybaseType.WEIXIN)){
                indices = Const.WECHAT_COMMON;
            }else if(hybaseType.equals(FtsHybaseType.OVERSEAS)){
                indices = Const.HYBASE_OVERSEAS;
            }else if(hybaseType.equals(FtsHybaseType.INSERT)){
                indices = Const.INSERT;
            }else if(hybaseType.equals(FtsHybaseType.SINAUSER)){
                indices = Const.SINAUSERS;
            } else if(hybaseType.equals(FtsHybaseType.MIX)){
                indices = Const.MIX_DATABASE;
            }else if (hybaseType.equals(FtsHybaseType.WEIBO_RSB)){
                indices = Const.WEIBO_RSB;
            }else if (hybaseType.equals(FtsHybaseType.WEIBO_HTB)){
                indices = Const.WEIBO_HTB;
            }else if (hybaseType.equals(FtsHybaseType.RANK_LIST)){
                indices = Const.DC_BANGDAN;
            }else {
                indices = Const.HYBASE_NI_INDEX;
            }
            return indices;
        }

    }

    /**
     * 解析检索实体中的查询字段
     *
     * @param tClass IDocument
     * @param <T>    IDocument子类
     * @return String[]
     */
    public static <T extends IDocument> String[] getSearchField(Class<T> tClass) throws TRSSearchException {
        List<String> list = new ArrayList<>();
        Field[] fields = tClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(FtsField.class)) {
                FtsField ftsField = field.getAnnotation(FtsField.class);
                if (StringUtil.isEmpty(ftsField.value())) {
                    throw new TRSSearchException("@FtsField value为空");
                }
                list.add(ftsField.value());
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * 解析检索实体中的高亮字段
     *
     * @param tClass IDocument
     * @param <T>    IDocument子类
     * @return String[]
     */
    public static <T extends IDocument> String[] getHighLightField(Class<T> tClass) throws TRSSearchException {
        List<String> list = new ArrayList<>();
        Field[] fields = tClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(FtsField.class)) {
                FtsField ftsField = field.getAnnotation(FtsField.class);
                if (ftsField.highLight()) {
                    list.add(ftsField.value());
                }
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public static <T extends IDocument> T toEntity(TRSEsRecord record, Class<T> tClass) throws OperationsException {
        try {
            T instance = tClass.newInstance();
            instance.setId(record.getEsId());
            Field[] fields = tClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(FtsField.class)) {
                    FtsField ftsField = field.getAnnotation(FtsField.class);
                    field.setAccessible(true);
                    String type = field.getType().getSimpleName();
                    switch (type) {
                        case "String":
                            field.set(instance, record.getString(ftsField.value()));
                            break;
                        case "Date":
                            String date = record.getString(ftsField.value());
                            if (StringUtil.isNotEmpty(date)) {
                                date = date.replaceAll("[./]", "-");
                                field.set(instance, DateUtil.stringToDate(date, DateUtil.yyyyMMdd));
                            }
                            break;
                        case "Integer":
                        case "int":
                            String i = record.getString(ftsField.value());
                            if (StringUtil.isNotEmpty(i)) {
                                field.set(instance, Integer.parseInt(i));
                            }
                            break;
                        case "Long":
                        case "long":
                            String l = record.getString(ftsField.value());
                            if (StringUtil.isNotEmpty(l)) {
                                field.set(instance, Long.parseLong(l));
                            }
                            break;
                        case "double":
                        case "Double":
                            String d = record.getString(ftsField.value());
                            if (StringUtil.isNotEmpty(d)) {
                                field.set(instance, Double.parseDouble(d));
                            }
                            break;
                        case "float":
                        case "Float":
                            String f = record.getString(ftsField.value());
                            if (StringUtil.isNotEmpty(f)) {
                                field.set(instance, Float.parseFloat(f));
                            }
                            break;
                        case "List":
                            String s = record.getString(ftsField.value());
                            if (StringUtil.isNotEmpty(s)) {
                                field.set(instance, Arrays.asList(s.split(";")));
                            }
                            break;
                        default:
                            break;
                    }
                }
            }

            return instance;
        } catch (Exception e) {
            throw new OperationsException("解析ES结果出错");
        }
    }

    /**
     * 将TRSRecord 转换为IDocument的实体
     *
     * @param record TRSRecord
     * @param tClass IDocument
     * @param <T>    IDocument子类
     * @return String[]
     */
    public static <T extends IDocument> T toEntity(TRSRecord record, Class<T> tClass) throws TRSSearchException {
        try {
            T instance = tClass.newInstance();
            instance.setId(record.getString("IR_SID"));
            // 反射获取class中所有字段
            Field[] fields = tClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(FtsField.class)) {
                    FtsField ftsField = field.getAnnotation(FtsField.class);
                    field.setAccessible(true);
                    String type = field.getType().getSimpleName();
                    switch (type) {
                        case "String":
                            if (StringUtils.equals("uid", ftsField.value())) {
                                field.set(instance, record.getUid()); // 向对象的这个Field重新设置值
//                            } else if (ftsField.value().equals("IR_CATALOG2")) {
//                                String sensitiveType = convertType(record.getString(ftsField.value()));
//                                field.set(instance, sensitiveType); // 处理敏感分类
                            } else {
                                field.set(instance, record.getString(ftsField.value())); // 向对象的这个Field重新设置值
                            }
                            break;
                        case "Date":
                            field.set(instance, record.getDate(ftsField.value()));
                            break;
                        case "Integer":
                        case "int":
                            field.set(instance, record.getInt(ftsField.value()));
                            break;
                        case "Long":
                        case "long":
                            field.set(instance, record.getLong(ftsField.value()));
                            break;
                        case "double":
                        case "Double":
                            field.set(instance, record.getDouble(ftsField.value()));
                            break;
                        case "float":
                        case "Float":
                            field.set(instance, record.getFloat(ftsField.value()));
                            break;
                        case "List":
                            String s = record.getString(ftsField.value());
                            if (StringUtil.isNotEmpty(s)) {
                                field.set(instance, Arrays.asList(s.split(";")));
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
            return instance;
        } catch (Exception e) {
            throw new TRSSearchException("解析Hybase结果出错");
        }
    }

    private static <T> void dealFiled(Field field, FtsField ftsField, T instance, TRSRecord record) throws TRSException, IllegalAccessException {
        if (ftsField.value().equals(FtsFieldConst.FIELD_CONTENT)) {
            String dealContent = StringUtil.replaceImg(record.getString(ftsField.value()));
            field.set(instance, dealContent);
        }
    }

    /**
     * 转换敏感分类
     *
     * @param sensitiveType
     * @return
     */
    private static String convertType(String sensitiveType) {
        try {
            if (sensitiveType.contains("重点舆情")) {
                if (sensitiveType.contains(";")) {
                    String[] types = sensitiveType.split(";");
                    for (String type : types) {
                        if (type.contains("重点舆情")) {
                            sensitiveType = type;
                            break;
                        }
                    }
                }
                sensitiveType = sensitiveType.substring(sensitiveType.lastIndexOf("\\") + 1, sensitiveType.length() - 1);
            } else {
                sensitiveType = "一般舆情";
            }
        } catch (Exception e) {
            sensitiveType = "一般舆情";
        }
        return sensitiveType;
    }

}
