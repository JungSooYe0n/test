package com.trs.netInsight.widget.alert.entity.repository;

import com.trs.netInsight.widget.alert.entity.AlertWebSocket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository("alertWebSocketRepository")
public interface AlertWebSocketRepository extends JpaSpecificationExecutor<AlertWebSocket>, JpaRepository<AlertWebSocket, String> {
    //根据ID查询预警信息最新的前三条
    @Query(value = "select * from alert_websocket where receive_id=:receiveid order by create_date desc limit 3",nativeQuery = true)
    public List<AlertWebSocket> selectAlertWebSocket(@Param("receiveid") String receiveid);
    //删除相关信息，保证每次存储后查出来的一定是最新的
    @Transactional
    public void deleteAlertWebSocketByReceiveid(String receiveid);

}
