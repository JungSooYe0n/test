package com.trs.netInsight.widget.apply.service.impl;

import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.apply.entity.Apply;
import com.trs.netInsight.widget.apply.entity.enums.ApplyUserType;
import com.trs.netInsight.widget.apply.repository.ApplyRepository;
import com.trs.netInsight.widget.apply.service.IApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Transactional
@Service
public class ApplyServiceImpl implements IApplyService {

    @Autowired
    private ApplyRepository applyRepository;

    @Override
    public Object list(ApplyUserType applyUserType,int pageNo, int pageSize){
        Sort sort = new Sort(Sort.Direction.DESC, "createdTime");
        Pageable pageable = new PageRequest(pageNo, pageSize, sort);
        Specification<Apply> criteria = new Specification<Apply>() {
            @Override
            public Predicate toPredicate(Root<Apply> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("applyUserType"),applyUserType.toString()));

                Predicate[] pre = new Predicate[predicates.size()];
                return query.where(predicates.toArray(pre)).getRestriction();
            }
        };
        Page<Apply> applyList = applyRepository.findAll(criteria,pageable);
        return applyList;
    }

    public void updateApply(Apply apply){
        applyRepository.saveAndFlush(apply);
    }

   public Apply findOne(String id){
        return applyRepository.findOne(id);
   }


}
