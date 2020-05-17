package com.trs.netInsight.support.knowledgeBase.service;

import com.trs.netInsight.support.knowledgeBase.entity.KnowledgeBase;
import com.trs.netInsight.support.knowledgeBase.entity.KnowledgeClassify;

import java.util.List;

/**
 * @Author:拓尔思信息股份有限公司
 * @Description:  知识库 业务层接口
 * @Date:Created in  2020/3/9 14:51
 * @Created By yangyanyan
 */
public interface IKnowledgeBaseService {


    void save(String keywords,KnowledgeClassify classify);

    KnowledgeBase findOne(String id);

    List<KnowledgeBase> findByClassify(KnowledgeClassify classify);

    KnowledgeBase findOneById(String id);

    void deleteOne(String id);

    KnowledgeBase updateOne(String id,String keywords,KnowledgeClassify classify);

}
