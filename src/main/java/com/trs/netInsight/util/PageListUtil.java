package com.trs.netInsight.util;

import java.io.Serializable;
import java.util.List;

import com.trs.dev4.jdk16.dao.GroupByResult;

public class PageListUtil<T> implements Serializable {


	/**
	 * 当前页记录列表.
	 */
	private List<T> pageItems;

	/**
	 * 当前页码, 0表示第一页.
	 */
	private int pageIndex;

	/**
	 * 每页记录数.
	 */
	private int pageSize;

	/**
	 * 总记录数.
	 */
	private int totalItemCount;

	/**
	 * 当前页的总记录数.
	 */
	private int thisPageTotal;

	/**
	 * 总页数.
	 */
	private int pageTotal;

	public void setPageTotal(int pageTotal) {
		this.pageTotal = pageTotal;
	}

	/**
	 * 当前页的上一页.
	 */
	private int prevPage;

	/**
	 * 当前页的下一页.
	 */
	private int nextPage;

	/**
	 * 导航的步进. (即导航条显示多少页)
	 */
	private int step;

	/**
	 * 导航的起始页.
	 */
	private int startPage;

	/**
	 * 导航的结束页.
	 */
	private int endPage;

	/**
	 *
	 */
	private GroupByResult groupByResult;

	/**
	 * 
	 * @param pageItems
	 * @param totalItemCount
	 * @since liushen @ Jul 6, 2010
	 */
	public PageListUtil(List<T> pageItems, int totalItemCount) {
		this(pageItems, 0, 20, totalItemCount);
	}

	/**
	 * 构造分页结果集. 其中将{@link #step}设置为10.
	 * 
	 * @see #PagedList(int, int, int, List, int)
	 */
	public PageListUtil(List<T> pageItems, int pageIndex, int pageSize,
			int totalItemCount) {
		this(pageIndex, pageSize, totalItemCount, pageItems, 10);
	}

	/**
	 * @deprecated liushen@Jul 6, 2010: 应避免使用!
	 */
	@Deprecated
	public PageListUtil() {
	}

	/**
	 * 构造分页结果集.
	 * 
	 * @param pageItems
	 *            the {@link #pageItems}.
	 */
	public PageListUtil(List<T> pageItems) {
		this.pageIndex = 0;
		this.pageSize = pageItems.size();
		this.totalItemCount = pageItems.size();
		this.pageItems = pageItems;
		this.thisPageTotal = pageItems.size();
		// computePageIndex(step);
	}

	/**
	 * 构造分页结果集.
	 * 
	 * @param pageIndex
	 *            the {@link #pageIndex}.
	 * @param pageSize
	 *            the {@link #pageSize}.
	 * @param totalItemCount
	 *            the {@link #totalItemCount}.
	 * @param pageItems
	 *            the {@link #pageItems}.
	 * @param step
	 *            the {@link #step}.
	 */
	public PageListUtil(int pageIndex, int pageSize, int totalItemCount,
			List<T> pageItems, int step) {
		this.pageIndex = (pageIndex < 0) ? 0 : pageIndex;
		this.pageSize = (pageSize <= 0) ? 5 : pageSize;
		this.totalItemCount = totalItemCount;
		this.pageItems = pageItems;
		this.thisPageTotal = (pageItems == null) ? 0 : pageItems.size();

		computePageIndex(step);
	}

	/**
	 * 构造分页结果集.
	 * 
	 * @param pageIndex
	 *            the {@link #pageIndex}.
	 * @param pageSize
	 *            the {@link #pageSize}.
	 * @param totalItemCount
	 *            the {@link #totalItemCount}.
	 * @param pageItems
	 *            the {@link #pageItems}.
	 * @param step
	 *            the {@link #step}.
	 */
	public PageListUtil(int pageIndex, int pageSize, int totalItemCount,
			List<T> pageItems, int step, GroupByResult groupByResult) {
		this(pageIndex, pageSize, totalItemCount, pageItems, step);

		this.groupByResult = groupByResult;
	}

	/**
	 * 构造分页结果集.
	 * 
	 * @param pageIndex
	 *            the {@link #pageIndex}.
	 * @param pageSize
	 *            the {@link #pageSize}.
	 * @param totalItemCount
	 *            the {@link #totalItemCount}.
	 * @param pageItems
	 *            the {@link #pageItems}.
	 * @param step
	 *            the {@link #step}.
	 */
	public PageListUtil(int pageIndex, int pageSize, int totalItemCount,
			List<T> pageItems, GroupByResult groupByResult) {
		this(pageIndex, pageSize, totalItemCount, pageItems, 10);

		this.groupByResult = groupByResult;
	}

	/**
	 * 计算页码导航的各个值.
	 * 
	 * @param stepValue
	 *            页码导航显示多少页.
	 */
	private void computePageIndex(int stepValue) {
		if (totalItemCount <= 0) {
			pageTotal = 0;
		} else {
			pageTotal = (totalItemCount / pageSize)
					+ ((totalItemCount % pageSize == 0) ? 0 : 1);
		}
		prevPage = (pageIndex == 0) ? 0 : pageIndex - 1;
		nextPage = (pageIndex >= pageTotal - 1) ? pageTotal - 1 : pageIndex + 1;
		step = stepValue;
		startPage = (pageIndex / step) * step;
		endPage = (startPage + step >= pageTotal) ? pageTotal - 1 : startPage
				+ step;
	}

	/**
	 * 返回当前页的第index条记录.
	 */
	public T get(int index) {
		return pageItems.get(index);
	}

	/**
	 * @return the list of items for this page
	 */
	public List<T> getPageItems() {
		return pageItems;
	}

	/**
	 * @return total count of items
	 */
	public int getTotalItemCount() {
		return totalItemCount;
	}

	public void setTotalItemCount(int totalItemCount) {
		this.totalItemCount = totalItemCount;
	}

	public void setThisPageTotal(int thisPageTotal) {
		this.thisPageTotal = thisPageTotal;
	}

	/**
	 * @return total count of pages
	 */
	public int getTotalPageCount() {
		return getPageTotal();
	}
 
	public void setTotalPageCount(int totalPageCount){
		this.pageTotal = totalPageCount;
	}
	/**
	 * @return Returns the pageTotal.
	 */
	public int getPageTotal() {
		return pageTotal;
	}

	/**
	 * 返回第一页(首页)的页码.
	 */
	// 原方法名为getFirstPage，但使用JSON时会出现异常，故此改名。具体异常信息：org.codehaus.jackson.map.JsonMappingException:
	// Conflicting getter definitions for property "firstPage":
	// com.trs.dev4.jdk16.dao.PagedList#getFirstPage(0 params) vs
	// com.trs.dev4.jdk16.dao.PagedList#isFirstPage(0 params)
	public int getFirstPageNo() {
		return 0;
	}

	/**
	 * 返回最后一页(末页)的页码.
	 */
	public int getLastPageNo() {
		return pageTotal - 1;
	}

	/**
	 * @return true if this is the first page
	 */
	public boolean isFirstPage() {
		return isFirstPage(getPageIndex());
	}

	/**
	 * @return true if this is the last page
	 */
	public boolean isLastPage() {
		return isLastPage(getPageIndex());
	}

	/**
	 * @param page
	 * @return true if the page is the first page
	 */
	public boolean isFirstPage(int page) {
		return page <= 0;
	}

	/**
	 * @param page
	 * @return true if the page is the last page
	 */
	public boolean isLastPage(int page) {
		return page >= getTotalPageCount() - 1;
	}

	/**
	 * @return the pageIndex
	 */
	public int getPageIndex() {
		return pageIndex;
	}

	/**
	 * @return the pageSize
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * @return the thisPageTotal.
	 */
	public int getThisPageTotal() {
		return thisPageTotal;
	}

	/**
	 * @return step
	 */
	public int getStep() {
		return step;
	}

	/**
	 * @return startPage
	 */
	public int getStartPage() {
		return startPage;
	}

	/**
	 * @return endPage
	 */
	public int getEndPage() {
		return endPage;
	}

	/**
	 * @return prevPage
	 */
	public int getPrevPage() {
		return prevPage;
	}

	/**
	 * @return nextPage
	 */
	public int getNextPage() {
		return nextPage;
	}

	/**
	 * @see java.lang.Object#toString()
	 * @creator liushen @ Jan 27, 2010
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PagedList [pageIndex=").append(pageIndex);
		builder.append(", total=").append(totalItemCount);
		builder.append(", thisPageTotal=").append(thisPageTotal);
		if (pageItems != null) {
			builder.append("; pageItems=").append(pageItems);
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @return the {@link #groupByResult}
	 */
	public GroupByResult getGroupByResult() {
		return groupByResult;
	}

	/**
	 * @param groupByResult
	 *            the {@link #groupByResult} to set
	 */
	public void setGroupByResult(GroupByResult groupByResult) {
		this.groupByResult = groupByResult;
	}

	/**
	 * 获取当前结果集的记录数
	 * 
	 * @return
	 * @since fangxiang @ Nov 14, 2010
	 */
	public int size() {
		return (pageItems == null) ? 0 : pageItems.size();
	}

	/**
	 * 获取当前结果集的开始记录数
	 * 
	 * @since fangxiang @ 2011-8-24
	 */
	public int getStartIndex() {
		return ((pageIndex > 1 ? pageIndex : 1) - 1) * this.pageSize + 1;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public void setPrevPage(int prevPage) {
		this.prevPage = prevPage;
	}

	public void setNextPage(int nextPage) {
		this.nextPage = nextPage;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public void setStartPage(int startPage) {
		this.startPage = startPage;
	}

	public void setEndPage(int endPage) {
		this.endPage = endPage;
	}

	/**
	 * 更新结果集，供子类使用
	 * 
	 * @since fangxiang @ 2011-8-24
	 */
	public void setPageItems(List<T> pageItems) {
		this.pageItems = pageItems;
	}

}
