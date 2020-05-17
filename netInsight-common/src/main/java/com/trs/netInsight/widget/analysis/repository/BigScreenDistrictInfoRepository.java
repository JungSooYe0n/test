package com.trs.netInsight.widget.analysis.repository;

import com.trs.netInsight.widget.analysis.entity.BigScreenDistrictInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 大屏 地域信息 持久层
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/12/19.
 * @desc
 */
@Repository
public interface BigScreenDistrictInfoRepository extends JpaRepository<BigScreenDistrictInfo, String> {
}
