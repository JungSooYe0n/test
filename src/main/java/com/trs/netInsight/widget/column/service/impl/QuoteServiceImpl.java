package com.trs.netInsight.widget.column.service.impl;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.column.service.IIndexPageService;
import com.trs.netInsight.widget.column.service.IIndexTabMapperService;
import com.trs.netInsight.widget.column.service.IQuoteService;

/**
 * 共享引用接口服务实现类
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年9月20日
 *
 */
@Service
public class QuoteServiceImpl implements IQuoteService{
	
	@Autowired
	private IIndexTabMapperService indexTabMapperService;
	
	@Autowired
	private IIndexPageService indexPageService;

	@Override
	@Transactional
	public void quote(String indexPageId, String indexTabMapperId) {
		// 获取待引用映射关系
		IndexTabMapper mapper = this.indexTabMapperService.findOne(indexTabMapperId);
		// 获取待引用栏目
		IndexTab indexTab = mapper.getIndexTab();
		// 获取引用至栏目组
		IndexPage indexPage = this.indexPageService.findOne(indexPageId);
		// 新建栏目映射关系
		IndexTabMapper quoteMapper = new IndexTabMapper();
		quoteMapper.setHide(false);
		quoteMapper.setShare(true);
		quoteMapper.setTabWidth(Integer.valueOf(mapper.getTabWidth()));
		quoteMapper.setIndexPage(indexPage);
		quoteMapper.setIndexTab(indexTab);
		quoteMapper.setMe(false);
		long index = this.indexTabMapperService.countByIndexPage(indexPage);
		quoteMapper.setSequence((int)index + 1);
		this.indexTabMapperService.save(quoteMapper);
	}

	@Override
	public void cancelQuote(String indexTabMapperId) {
		indexTabMapperService.delete(indexTabMapperId);
	}

}
