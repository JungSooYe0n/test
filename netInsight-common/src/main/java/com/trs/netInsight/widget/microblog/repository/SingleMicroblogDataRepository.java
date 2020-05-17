package com.trs.netInsight.widget.microblog.repository;

import com.trs.netInsight.widget.microblog.entity.SingleMicroblogData;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 单条微博分析数据存储持久层
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/2/13.
 * @desc
 */
@Repository
public interface SingleMicroblogDataRepository extends MongoRepository<SingleMicroblogData,String> {
    public List<SingleMicroblogData> findAllByUserIdAndCurrentUrl(String userId, String currentUrl);
    public List<SingleMicroblogData> findAllByUserIdAndCurrentUrlAndRandom(String userId, String currentUrl, String random);
    public List<SingleMicroblogData> findAllBySubGroupIdAndCurrentUrlAndRandom(String subGroupId, String currentUrl, String random);

    public List<SingleMicroblogData> findByUserIdAndCurrentUrlAndNameAndRandom(String userId, String currentUrl, String name, String random);
    public List<SingleMicroblogData> findBySubGroupIdAndCurrentUrlAndNameAndRandom(String subGroupId, String currentUrl, String name, String random);
    public List<SingleMicroblogData> findByUserIdAndCurrentUrlAndName(String userId, String currentUrl, String name);
    public List<SingleMicroblogData> findBySubGroupIdAndCurrentUrlAndName(String subGroupId, String currentUrl, String name);

    public List<SingleMicroblogData> findAllByUserIdAndName(String userId, String name, Sort sort);
    public List<SingleMicroblogData> findAllBySubGroupIdAndName(String subGroupId, String name, Sort sort);




    public List<SingleMicroblogData> findByUserIdAndNameAndState(String uerId, String name, String state, Sort sort);
    public List<SingleMicroblogData> findBySubGroupIdAndNameAndState(String subGroupId, String name, String state, Sort sort);

    public List<SingleMicroblogData> findByUserId(String userId);

}

