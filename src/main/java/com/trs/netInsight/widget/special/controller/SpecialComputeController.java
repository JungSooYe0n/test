package com.trs.netInsight.widget.special.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.trs.netInsight.widget.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.special.entity.SpecialExponentVO;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.service.ISpecialComputeService;
import com.trs.netInsight.widget.special.service.ISpecialProjectService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

/**
 * 专题指数分析测试接口
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年5月4日
 *
 */
@RestController
@RequestMapping("/specialCompute")
@Api(description = "专题指数分析测试接口")
public class SpecialComputeController {

	@Autowired
	private ISpecialProjectService projectService;

	@Autowired
	private ISpecialComputeService computeService;

	/**
	 * 获取本机构下所有专题
	 * 
	 * @since changjiang @ 2018年5月4日
	 * @return
	 * @Return : Object
	 */
	@FormatResult
	@RequestMapping(value = "/allSpecial", method = RequestMethod.GET)
	public Object allSpecial() {
		User loginUser = UserUtils.getUser();
		List<SpecialProject> list = null;
		if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
			list = this.projectService.findByUserId(loginUser.getId(), new Sort(Direction.DESC, "createdTime"));
		}else {
			list = this.projectService.findBySubGroupId(loginUser.getSubGroupId(),new Sort(Direction.DESC, "createdTime"));
		}
		List<Map<String, String>> data = new LinkedList<>();
		if (list != null && list.size() > 0) {
			Map<String, String> special = null;
			for (SpecialProject specialProject : list) {
				special = new HashMap<>();
				special.put("specialId", specialProject.getId());
				special.put("specialName", specialProject.getSpecialName());
				data.add(special);
			}
		}
		return data;
	}

	/**
	 * 根据专题统计对比指数
	 * 
	 * @since changjiang @ 2018年5月4日
	 * @param specialIds
	 * @param timeRanger
	 * @param orderBy
	 * @param sort
	 * @return
	 * @Return : Object
	 */
	@FormatResult
	@RequestMapping(value = "/computeSpecial", method = RequestMethod.POST)
	public Object computeSpecial(@ApiParam("专题id集合") @RequestParam(value = "specialIds") String[] specialIds,
			@ApiParam("对比时间范围,格式\"2018-05-01;2018-05-07\",默认7天") @RequestParam(value = "timeRanger", required = false) String[] timeRanger,
			@ApiParam("排序字段,默认热度") @RequestParam(value = "orderBy", required = false, defaultValue = "hotDegree") String orderBy,
			@ApiParam("正/逆序,true为正序,默认false") @RequestParam(value = "sort", required = false, defaultValue = "false") boolean sort) {

		// 参数整理
		Date begin = null;
		Date end = null;
		if (timeRanger != null && timeRanger.length == 2) {
			begin = DateUtil.stringToDate(timeRanger[0], "yyyy-MM-dd");
			end = DateUtil.stringToDate(timeRanger[1], "yyyy-MM-dd");
		}
		List<SpecialExponentVO> list = this.computeService.computeTotalByCondition(specialIds, begin, end, orderBy,
				sort);
		return list;
	}

	/**
	 * 专题指数对比走势图
	 * 
	 * @since changjiang @ 2018年5月7日
	 * @param specialIds
	 *            专题id集
	 * @param timeRanger
	 *            时间范围
	 * @return
	 * @Return : Object
	 */
	@FormatResult
	@RequestMapping(value = "/computeTrendChart", method = RequestMethod.POST)
	public Object computeTrendChart(@ApiParam("专题id集合") @RequestParam(value = "specialIds") String[] specialIds,
			@ApiParam("对比时间范围,格式\"2018-05-01,2018-05-07\",默认7天") @RequestParam(value = "timeRanger", required = false) String[] timeRanger) {

		Map<String, Object> data = new HashMap<>();
		// 参数整理
		Date begin = null;
		Date end = null;
		if (timeRanger != null && timeRanger.length == 2) {
			begin = DateUtil.stringToDate(timeRanger[0], "yyyy-MM-dd");
			end = DateUtil.stringToDate(timeRanger[1], "yyyy-MM-dd");
			data.put("timeArray", DateUtil.subDateRangeToList(timeRanger[0], timeRanger[1]));
		}
		Object chart = this.computeService.computeTrendChart(specialIds, begin, end);
		data.put("data", chart);
		return data;
	}

}
