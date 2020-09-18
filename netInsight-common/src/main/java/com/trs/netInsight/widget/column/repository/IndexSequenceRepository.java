package com.trs.netInsight.widget.column.repository;

import com.trs.netInsight.widget.column.entity.IndexSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface IndexSequenceRepository extends PagingAndSortingRepository<IndexSequence, String>, JpaSpecificationExecutor<IndexSequence>,JpaRepository<IndexSequence, String> {

    public List<IndexSequence> findByParentId(String parentId);
    public List<IndexSequence> findByUserId(String userId);
    public List<IndexSequence> findBySubGroupId(String subGroupId);
    public List<IndexSequence> findByUserIdAndParentId(String userId,String parentId);
    public List<IndexSequence> findBySubGroupIdAndParentId(String subGroupId,String parentId);
    public List<IndexSequence> findByIndexId(String indexId);
    public List<IndexSequence> findByIndexTabId(String indexTabId);
    public List<IndexSequence> findByParentIdOrderBySequence(String parentId);
    public List<IndexSequence> findByIndexTabIdOrderBySequence(String indexTabId);
}
