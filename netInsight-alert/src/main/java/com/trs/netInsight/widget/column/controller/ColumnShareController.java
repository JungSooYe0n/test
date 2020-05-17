package com.trs.netInsight.widget.column.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.batik.dom.util.HashTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trs.dc.entity.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.column.service.IIndexTabMapperService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 日常监测共享池控制器接口
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年9月18日
 *
 */
@Api(description = "日常监测共享管理接口")
@RestController
@RequestMapping("/column/share")
public class ColumnShareController {

	@Autowired
	private IIndexTabMapperService indexTabMapperService;

	/**
	 * 统计机构下或用户分组下各账号分享栏目数量
	 * 
	 * @since changjiang @ 2018年9月18日
	 * @return
	 * @throws TRSException
	 * @Return : Object
	 */
	@FormatResult
	@GetMapping("/computeShareNum")
	@ApiOperation("统计机构下各账号分享栏目数量")
	public Object computeShareNum() throws TRSException {
		List<Map<String, Object>> mapperNumber = null;
		try {
			mapperNumber = this.indexTabMapperService.computeShareMapperNumber();
		} catch (Exception e) {
			throw new TRSException(e);
		}
		return mapperNumber;
	}

	/**
	 * 根据用户id检索共享栏目类列表
	 * 
	 * @since changjiang @ 2018年9月19日
	 * @param userId
	 * @return
	 * @throws TRSException
	 * @Return : Object
	 */
	@FormatResult
	@GetMapping("/list")
	@ApiOperation("根据用户id，检索共享栏目列表")
	public Object list(@ApiParam("userId") @RequestParam("userId") String userId) throws TRSException {
		List<IndexTabMapper> mapper = null;
		try {
			mapper = this.indexTabMapperService.findByUserIdAndShare(userId, true);
		} catch (Exception e) {
			throw new TRSException(e);
		}
		return mapper;
	}

	/**
	 * 批量取消共享
	 * 
	 * @since changjiang @ 2018年9月19日
	 * @param mapperIds
	 * @return
	 * @throws TRSException
	 * @Return : Object
	 */
	@FormatResult
	@GetMapping("/unShare")
	@ApiOperation("取消共享")
	public Object unShare(@ApiParam("mapperIds") @RequestParam("mapperIds") String[] mapperIds) throws TRSException {
		try {
			this.indexTabMapperService.unShare(mapperIds);
			return "batch cancel share success!";
		} catch (Exception e) {
			throw new TRSException(e);
		}
	}

	/**
	 * 批量共享
	 * 
	 * @since changjiang @ 2018年9月19日
	 * @param mapperIds
	 * @return
	 * @throws TRSException
	 * @Return : Object
	 */
	@FormatResult
	@GetMapping("/share")
	@ApiOperation("加入共享")
	public Object share(@ApiParam("mapperIds") @RequestParam("mapperIds") String[] mapperIds) throws TRSException {
		try {
			this.indexTabMapperService.share(mapperIds);
			return "batch share success!";
		} catch (Exception e) {
			throw new TRSException(e);
		}
	}

	/**
	 * 检索当前机构管理员所有共享栏目组
	 * 
	 * @since changjiang @ 2018年9月20日
	 * @return
	 * @throws TRSException
	 * @Return : Object
	 */
	@FormatResult
	@GetMapping("/indexPage/listByOrgAdmin")
	@ApiOperation("检索当前机构管理员所有共享栏目组")
	public Object listByOrgAdmin() throws TRSException {
		List<IndexPage> shareIndexPages = null;
		try {
			shareIndexPages = this.indexTabMapperService.searchOrgAdminSharePages();
		} catch (Exception e) {
			throw new TRSException(e);
		}
		return shareIndexPages;
	}

}
