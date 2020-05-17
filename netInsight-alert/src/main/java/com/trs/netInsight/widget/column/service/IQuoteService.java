package com.trs.netInsight.widget.column.service;

import com.trs.netInsight.handler.exception.OperationException;

/**
 * 共享引用相关接口服务
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年9月20日
 *
 */
public interface IQuoteService {

	/**
	 * 添加引用
	 * 
	 * @since changjiang @ 2018年9月20日
	 * @param indexPageId
	 *            引用至栏目组id
	 * @param indexTabMapperId
	 *            栏目映射关系id
	 * @Return : void
	 */
	public void quote(String indexPageId, String indexTabMapperId);

	/**
	 * 取消引用
	 * 
	 * @since changjiang @ 2018年9月20日
	 * @param indexTabMapperId
	 *            栏目映射关系id
	 * @Return : void
	 */
	public void cancelQuote(String indexTabMapperId)throws OperationException;

}
