package com.trs.netInsight.widget.report.service.impl;

import com.trs.jpa.utils.Criteria;
import com.trs.jpa.utils.Restrictions;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.report.entity.Favourites;
import com.trs.netInsight.widget.report.entity.repository.FavouritesRepository;
import com.trs.netInsight.widget.report.service.IFavouritesService;
import com.trs.netInsight.widget.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/8/2 10:57.
 * @desc
 */
@Service
public class FavouritesServiceImpl implements IFavouritesService {
    @Autowired
    private FavouritesRepository favouritesRepository;
    @Override
    public List<Favourites> findAll(User user) {
        String userId = user.getId();
        String subGroupId = user.getSubGroupId();
        // 检验收藏
        //原生sql
        Specification<Favourites> criteria = new Specification<Favourites>() {

            @Override
            public Predicate toPredicate(Root<Favourites> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Object> predicates = new ArrayList<>();
                if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
                    predicates.add(cb.equal(root.get("userId"),userId));
                }else {
                    predicates.add(cb.equal(root.get("subGroupId"),subGroupId));
                }
                predicates.add(cb.isNull(root.get("libraryId")));
                Predicate[] pre = new Predicate[predicates.size()];

                return query.where(predicates.toArray(pre)).getRestriction();
            }
        };
        List<Favourites> favouritesList = favouritesRepository.findAll(criteria, new Sort(Sort.Direction.DESC, "lastModifiedTime"));
        return favouritesList;
    }

    @Override
    public List<Favourites> findByUserAndSid(User user, String sid) {
        String userId = user.getId();
        String subGroupId = user.getSubGroupId();
        Criteria<Favourites> criteria = new Criteria<>();
        if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
            criteria.add(Restrictions.eq("userId", userId));
        }else {
            criteria.add(Restrictions.eq("subGroupId", subGroupId));
        }
        criteria.add(Restrictions.eq("sid", sid));
        List<Favourites> favourite = favouritesRepository.findAll(criteria);
        return favourite;
    }

    @Override
    public List<Favourites> findByUserId(String userId) {
        return favouritesRepository.findByUserId(userId);
    }

    @Override
    public void updaeAll(List<Favourites> favourites) {
        favouritesRepository.save(favourites);
        favouritesRepository.flush();
    }
}
