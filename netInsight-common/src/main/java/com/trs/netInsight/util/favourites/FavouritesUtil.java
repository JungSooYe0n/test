package com.trs.netInsight.util.favourites;

import com.trs.netInsight.support.fts.entity.FtsDocument;
import com.trs.netInsight.support.fts.entity.FtsDocumentStatus;
import com.trs.netInsight.support.fts.entity.FtsDocumentTF;
import com.trs.netInsight.support.fts.entity.FtsDocumentWeChat;
import com.trs.netInsight.util.ObjectUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/10/23.
 * @desc
 */
public class FavouritesUtil {

    /**
     * 构建传统检索表达式 Created By Xiaoying
     */
    public static String buildSql(List<String> sidList) {
        if (ObjectUtil.isNotEmpty(sidList)) {
            StringBuilder strb = new StringBuilder();
            strb.append("IR_SID:(");
            sidList.forEach(sid -> {
                strb.append(sid).append(" OR ");
            });
            String trsl = strb.toString();
            trsl = trsl.substring(0, trsl.lastIndexOf("OR") - 1) + ")";
            return trsl;
        } else {
            return null;
        }
    }

    /**
     * 构建微博检索表达式 Created By Xiaoying
     */
    public static String buildSqlWeiBo(List<String> sidList) {
        if (ObjectUtil.isNotEmpty(sidList)) {
            StringBuilder strb = new StringBuilder();
            strb.append("IR_SID:(");
            sidList.forEach(sid -> {
                strb.append(sid).append(" OR ");
            });
            String trsl = strb.toString();
            trsl = trsl.substring(0, trsl.lastIndexOf("OR") - 1) + ")";
            return trsl;
        } else {
            return null;
        }
    }


    /**
     * 构建微信检索表达式 Created By Xiaoying
     */
    public static String buildSqlWeiXin(List<String> sidList) {
        if (ObjectUtil.isNotEmpty(sidList)) {
            StringBuilder strb = new StringBuilder();
            strb.append("IR_HKEY:(");
            sidList.forEach(sid -> {
                strb.append(sid).append(" OR ");
            });
            String trsl = strb.toString();
            trsl = trsl.substring(0, trsl.lastIndexOf("OR") - 1) + ")";
            return trsl;
        } else {
            return null;
        }
    }


    /**
     * 构建TF检索表达式
     * @param sidList
     * @return
     */
    public static String buildSqlTF(List<String> sidList) {
        if (ObjectUtil.isNotEmpty(sidList)) {
            StringBuilder strb = new StringBuilder();
            strb.append("IR_SID:(");
            sidList.forEach(sid -> {
                strb.append(sid).append(" OR ");
            });
            String trsl = strb.toString();
            trsl = trsl.substring(0, trsl.lastIndexOf("OR") - 1) + ")";
            return trsl;
        } else {
            return null;
        }
    }

    /**
     * 由于hybase搜索结果不按照查询条件的sid排列 而从mysql取出的市按照时间排列的 为了让结果按照时间排列
     */
    public static List<FtsDocument> resultByTimeTrandition(
            List<FtsDocument> result, List<String> sidList) {
        // 如果用同一个的话 会覆盖
        List<FtsDocument> resultByTime = new ArrayList<>();
        for (FtsDocument chartAnalyzeEntity : result) {
            resultByTime.add(chartAnalyzeEntity);
        }
        for (FtsDocument chartAnalyzeEntity : result) {
            int ind = sidList.indexOf(chartAnalyzeEntity.getSid());
            if (ind >= 0 && ind < result.size()) {
                resultByTime.set(ind, chartAnalyzeEntity);
            }
        }
        return resultByTime;
    }

    /**
     * 由于hybase搜索结果不按照查询条件的sid排列 而从mysql取出的市按照时间排列的 为了让结果按照时间排列
     */
    public static List<FtsDocumentStatus> resultByTimeWeiBo(
            List<FtsDocumentStatus> result, List<String> sidList) {
        // 如果用同一个的话 会覆盖
        List<FtsDocumentStatus> resultByTime = new ArrayList<>();
        for (FtsDocumentStatus chartAnalyzeEntity : result) {
            resultByTime.add(chartAnalyzeEntity);
        }
        for (FtsDocumentStatus chartAnalyzeEntity : result) {
            int ind = sidList.indexOf(chartAnalyzeEntity.getSid());
            if (ind >= 0 && ind < result.size()) {
                resultByTime.set(ind, chartAnalyzeEntity);
            }
        }
        return resultByTime;
    }
    /**
     * 由于hybase搜索结果不按照查询条件的sid排列 而从mysql取出的市按照时间排列的 为了让结果按照时间排列
     */
    public static List<FtsDocumentWeChat> resultByTimeWeiXin(
            List<FtsDocumentWeChat> result, List<String> sidList) {
        // 如果用同一个的话 会覆盖
        List<FtsDocumentWeChat> resultByTime = new ArrayList<>();
        for (FtsDocumentWeChat chartAnalyzeEntity : result) {
            resultByTime.add(chartAnalyzeEntity);
        }
        for (FtsDocumentWeChat chartAnalyzeEntity : result) {
            int ind = sidList.indexOf(chartAnalyzeEntity.getSid());
            if (ind >= 0 && ind < result.size()) {
                resultByTime.set(ind, chartAnalyzeEntity);
            }
        }
        return resultByTime;
    }
    /**
     *
     * @since changjiang @ 2018年4月25日
     * @param result
     * @param sidList
     * @return
     * @Return : List<FtsDocumentTF>
     */
    public static List<FtsDocumentTF> resultByTimeTF(List<FtsDocumentTF> result,
                                              List<String> sidList) {
        // 如果用同一个的话 会覆盖
        List<FtsDocumentTF> resultByTime = new ArrayList<>();
        for (FtsDocumentTF chartAnalyzeEntity : result) {
            resultByTime.add(chartAnalyzeEntity);
        }
        for (FtsDocumentTF chartAnalyzeEntity : result) {
            int ind = sidList.indexOf(chartAnalyzeEntity.getSid());
            if (ind >= 0 && ind < result.size()) {
                resultByTime.set(ind, chartAnalyzeEntity);
            }
        }
        return resultByTime;
    }


}
