package com.trs.netInsight.widget.analysis.factory;

import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.widget.analysis.enums.SpecialChartType;
import lombok.extern.slf4j.Slf4j;

/**
 * 专题分析图表构造工厂
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2020/1/7 17:34.
 * @desc
 */
@Slf4j
public class SpecialChartFactory {

    /**
     * @Desc : 根据图表类型构造对应图表类
     * @param typeCodeArray
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @Return : AbstractSpecialChart
     */
    @SuppressWarnings("unchecked")
    public static AbstractSpecialChart createSpecialChart(String[] typeCodeArray) {
        try {
            if (ObjectUtil.isNotEmpty(typeCodeArray)) {
                String typeCode = "";
                if (typeCodeArray.length > 1 || typeCodeArray[0].equals(ColumnConst.LIST_STATUS_COMMON) || typeCodeArray[0].equals(ColumnConst.LIST_WECHAT_COMMON)
                        || typeCodeArray[0].equals(ColumnConst.LIST_TWITTER) || typeCodeArray[0].equals(ColumnConst.LIST_FaceBook)) {
                    typeCode = ColumnConst.LIST_NO_SIM;
                }else {
                    typeCode = typeCodeArray[0];
                }
                SpecialChartType type = chooseType(typeCode);
                Class<AbstractSpecialChart> forName;
                forName = (Class<AbstractSpecialChart>) Class.forName(type.getResource());
                AbstractSpecialChart newInstance = forName.newInstance();
                return newInstance;
            }
        } catch (Exception e) {
            log.error("created specialChart error", e);
        }
        return null;
    }

    /**
     * @Desc : 根据图表类型选择对应的枚举类
     * @param typeCode
     * @return
     * @Return : SpecialChartType
     */
    private static SpecialChartType chooseType(String typeCode) {
        for (SpecialChartType chartTybe : SpecialChartType.values()) {
            if (chartTybe.getTypeCode().equals(typeCode)) {
                return chartTybe;
            }
        }
        return null;
    }

}
