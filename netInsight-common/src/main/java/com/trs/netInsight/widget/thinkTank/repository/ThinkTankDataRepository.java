package com.trs.netInsight.widget.thinkTank.repository;

import com.trs.netInsight.widget.thinkTank.entity.ThinkTankData;
import com.trs.netInsight.widget.thinkTank.entity.emnus.ThinkTankType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 舆情智库 持久层
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/9/23 15:48.
 * @desc
 */
@Repository
public interface ThinkTankDataRepository extends PagingAndSortingRepository<ThinkTankData,String> {

    List<ThinkTankData> findByReportTitleLike(String reportTitle, Pageable pageable);
    List<ThinkTankData> findByPicDetailNameNotAndPicDetailNameIsNotNull(String reportTitle, Pageable pageable);
    Page<ThinkTankData> findByPdfNameNot(String pdfName, Pageable pageable);
    Page<ThinkTankData> findByReportType(ThinkTankType reportType, Pageable pageable);

}
