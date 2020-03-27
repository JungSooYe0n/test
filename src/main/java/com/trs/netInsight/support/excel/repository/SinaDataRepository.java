package com.trs.netInsight.support.excel.repository;

import com.trs.netInsight.support.excel.entity.SinaData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/11/15.
 * @desc
 */
@Repository
public interface SinaDataRepository extends JpaRepository<SinaData,String>,JpaSpecificationExecutor<SinaData> {

    //查询
    public List<SinaData> findAllByTypeAndUserId(String type,String userId);

    public SinaData findByUidAndUserIdAndType(String uid,String userId,String type);
}
