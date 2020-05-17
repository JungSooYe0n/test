package com.trs.netInsight.widget.column.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.column.service.IIndexTabMapperService;
import com.trs.netInsight.widget.column.service.IIndexTabService;
import com.trs.netInsight.widget.column.service.IQuoteService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 日常监测引用复制操作接口
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年9月20日
 *
 */
@RestController
@RequestMapping("/quote")
@Api(description = "日常监测引用复制操作接口")
public class QuoteController {

	@Autowired
	private IQuoteService quoteService;
	
	@Autowired
	private IIndexTabService indexTabService;
	
	@Autowired
	private IIndexTabMapperService indexTabMapperService;

	/**
	 * 添加引用
	 * 
	 * @since changjiang @ 2018年9月20日
	 * @param indexPageId
	 *            引用至栏目组id
	 * @param indexTabId
	 *            栏目id
	 * @return
	 * @throws TRSException
	 * @Return : Object
	 */
	@FormatResult
	@GetMapping("/addQuote")
	@ApiOperation("引用")
	public Object quote(@ApiParam("indexPageId") @RequestParam("indexPageId") String indexPageId,
			@ApiParam("indexTabMapperId") @RequestParam("indexTabMapperId") String indexTabMapperId)
			throws TRSException {
		try {
			quoteService.quote(indexPageId, indexTabMapperId);
		} catch (Exception e) {
			throw new TRSException(e);
		}
		return "addQuote success !";
	}

	/**
	 * 取消引用
	 * 
	 * @since changjiang @ 2018年9月20日
	 * @param indexTabMapperId
	 * @return
	 * @throws TRSException
	 * @Return : Object
	 */
	@FormatResult
	@GetMapping("/cancelQuote")
	@ApiOperation("取消引用")
	public Object cancelQuote(@ApiParam("indexTabMapperId") @RequestParam("indexTabMapperId") String indexTabMapperId)
			throws TRSException {
		try {
			quoteService.cancelQuote(indexTabMapperId);
		} catch (Exception e) {
			throw new TRSException(e);
		}
		return "cancelQuote success!";
	}

	/**
	 * 另存为共享栏目
	 * 
	 * @since changjiang @ 2018年10月12日
	 * @param indexPageId
	 * @param indexTabMapperId
	 * @return
	 * @throws TRSException
	 * @Return : Object
	 */
	@FormatResult
	@GetMapping("/toCopy")
	@ApiOperation("另存为")
	public Object toCopy(@ApiParam("indexPageId") @RequestParam("indexPageId") String indexPageId,
			@ApiParam("indexTabMapperId") @RequestParam("indexTabMapperId") String indexTabMapperId)
			throws TRSException {
		IndexTabMapper mapper = indexTabMapperService.findOne(indexTabMapperId);
		IndexTab indexTab = mapper.getIndexTab();
		IndexTab copy = indexTab.tabCopy();
		return "indexTab copy success!";
	}
}
