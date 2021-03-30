package com.trs.netInsight.support.log.repository;

import com.trs.netInsight.support.log.entity.RequestTimeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RequestTimeLogRepository extends PagingAndSortingRepository<RequestTimeLog, String>, JpaSpecificationExecutor<RequestTimeLog>,JpaRepository<RequestTimeLog,String> {

    //查询数量 根据机构查
    @Query(value = "SELECT tab_name as item, count(a.id) num " +//distinct 这里我进行了一个去重的操作 这个可以忽略,业务需求
            "FROM request_time_log a " +
            "WHERE `organization_id` IN (:orgIds) " +
            "AND a.created_time >= date_format((:dateStr),'%Y-%m-%d %H:%M:%S') group by tab_name limit 10", nativeQuery = true)
    Object[] itemPer(@Param("orgIds") List<String> orgIds, @Param("dateStr")String dateStr);

    //查询数量 根据机构查
    @Query(value = "SELECT tab_name as item, count(a.id) num " +//distinct 这里我进行了一个去重的操作 这个可以忽略,业务需求
            "FROM request_time_log a " +
            "WHERE `createduser_id` IN (:userIds)" +
            "AND a.created_time >= date_format((:dateStr),'%Y-%m-%d %H:%M:%S') group by tab_name limit 10", nativeQuery = true)
    Object[] itemPerByUserId(@Param("userIds")List<String> userIds, @Param("dateStr")String dateStr);

    //查询数量 根据机构查
    @Query(value = "SELECT tab_name as item, count(a.id) num " +//distinct 这里我进行了一个去重的操作 这个可以忽略,业务需求
            "FROM request_time_log a " +
            "WHERE `organization_id` IN (:orgIds) " +
            "AND `operation` like (:item)" +
            "AND a.created_time >= date_format((:dateStr),'%Y-%m-%d %H:%M:%S') group by tab_name limit 10", nativeQuery = true)
    Object[] itemPer(@Param("orgIds") List<String> orgIds, @Param("dateStr")String dateStr,@Param("item")String item);

    //查询数量 根据机构查
    @Query(value = "SELECT tab_name as item, count(a.id) num " +//distinct 这里我进行了一个去重的操作 这个可以忽略,业务需求
            "FROM request_time_log a " +
            "WHERE `createduser_id` IN (:userIds) " +
            "AND `operation` like (:item)" +
            "AND a.created_time >= date_format((:dateStr),'%Y-%m-%d %H:%M:%S') group by tab_name limit 10", nativeQuery = true)
    Object[] itemPerByUserId(@Param("userIds")List<String> userIds, @Param("dateStr")String dateStr,@Param("item")String item);

    //查询数量 根据机构查
    @Query(value = "SELECT * " +//distinct 这里我进行了一个去重的操作 这个可以忽略,业务需求
            "FROM request_time_log a " +
            "WHERE `organization_id` IN (:orgIds) " +
            "AND `operation` like (:item)" +
            "AND a.created_time >= date_format((:dateStr),'%Y-%m-%d %H:%M:%S')", nativeQuery = true)
    List<RequestTimeLog> topTenMoudle(@Param("orgIds") List<String> orgIds, @Param("dateStr")String dateStr,@Param("item")String item);

    //查询数量 根据机构查
    @Query(value = "SELECT * " +//distinct 这里我进行了一个去重的操作 这个可以忽略,业务需求
            "FROM request_time_log a " +
            "WHERE `createduser_id` IN (:userIds) " +
            "AND `operation` like (:item)" +
            "AND a.created_time >= date_format((:dateStr),'%Y-%m-%d %H:%M:%S')", nativeQuery = true)
    List<RequestTimeLog> topTenMoudleUserId(@Param("userIds")List<String> userIds, @Param("dateStr")String dateStr,@Param("item")String item);
}
