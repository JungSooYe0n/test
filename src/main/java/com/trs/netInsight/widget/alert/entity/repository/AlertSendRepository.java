package com.trs.netInsight.widget.alert.entity.repository;

import com.trs.netInsight.widget.alert.entity.AlertSend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * app(目前针对app)预警推送提示业务层接口
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/4/22 17:22.
 * @desc
 */
@Repository
public interface AlertSendRepository extends JpaRepository<AlertSend,String> {
}
