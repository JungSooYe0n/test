package com.trs.netInsight.support.knowledgeBase.repository;

import com.trs.netInsight.support.knowledgeBase.entity.KnowledgeBase;
import com.trs.netInsight.support.knowledgeBase.entity.KnowledgeClassify;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author:拓尔思信息股份有限公司
 * @Description: 知识库 持久层
 * @Date:Created in  2020/3/9 14:48
 * @Created By yangyanyan
 */
@Repository
public interface KnowledgeBaseRepository extends PagingAndSortingRepository<KnowledgeBase, String>, JpaSpecificationExecutor<KnowledgeBase>, JpaRepository<KnowledgeBase,String> {

    List<KnowledgeBase> findByClassify(KnowledgeClassify effective);

}
