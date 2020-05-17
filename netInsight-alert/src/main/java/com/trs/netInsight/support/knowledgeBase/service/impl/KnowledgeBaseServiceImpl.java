package com.trs.netInsight.support.knowledgeBase.service.impl;

import com.trs.netInsight.support.knowledgeBase.entity.KnowledgeBase;
import com.trs.netInsight.support.knowledgeBase.entity.KnowledgeClassify;
import com.trs.netInsight.support.knowledgeBase.repository.KnowledgeBaseRepository;
import com.trs.netInsight.support.knowledgeBase.service.IKnowledgeBaseService;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author:拓尔思信息股份有限公司
 * @Description:  知识库 业务层接口实现
 * @Date:Created in  2020/3/9 14:53
 * @Created By yangyanyan
 */
@Service
public class KnowledgeBaseServiceImpl implements IKnowledgeBaseService {

    @Autowired
    private  KnowledgeBaseRepository knowledgeBaseRepository;


    @Override
    public void save(String keywords, KnowledgeClassify classify) {
        //false  代表 排除词
        KnowledgeBase knowledgeBase = new KnowledgeBase(keywords, classify);

        knowledgeBaseRepository.save(knowledgeBase);
    }
    @Override
    public KnowledgeBase findOne(String id) {
        return knowledgeBaseRepository.findOne(id);
    }

    @Override
    public List<KnowledgeBase> findByClassify(KnowledgeClassify classify) {
        if(ObjectUtil.isEmpty(classify)){
            return knowledgeBaseRepository.findAll();
        }
        return knowledgeBaseRepository.findByClassify(classify);//后期写上分类
    }

    @Override
    public KnowledgeBase findOneById(String id) {
        return knowledgeBaseRepository.findOne(id);
    }

    @Override
    public void deleteOne(String id) {
        knowledgeBaseRepository.delete(id);
    }

    @Override
    public KnowledgeBase updateOne(String id, String keywords,KnowledgeClassify classify) {
        KnowledgeBase knowledgeBase = knowledgeBaseRepository.findOne(id);
        knowledgeBase.setKeywords(keywords);
        knowledgeBase.setClassify(classify);
        knowledgeBaseRepository.saveAndFlush(knowledgeBase);
        return knowledgeBase;
    }
}