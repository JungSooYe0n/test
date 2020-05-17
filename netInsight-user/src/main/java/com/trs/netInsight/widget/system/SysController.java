package com.trs.netInsight.widget.system;

import com.trs.dc.entity.TRSEsDatabase;
import com.trs.dc.entity.TRSEsDatabaseField;
import com.trs.dc.entity.TRSException;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.cache.RedisFactory;
import com.trs.netInsight.support.fts.model.factory.ESFactory;
import com.trs.netInsight.util.ReflectUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 系统配置Controller
 *
 * Create by yan.changjiang on 2017年11月22日
 */
@RestController
@Api(description = "系统配置")
@RequestMapping("/sys")
public class SysController {

	@FormatResult
	@RequestMapping(value = "/clear", method = RequestMethod.GET)
	@ApiOperation("清除缓存")
	public Object clearCache(@ApiParam(value = "要清除的key") @RequestParam("key") String key) {
		RedisFactory.clearRedis(key);
		return "success";
	}

	@FormatResult
	@RequestMapping(value = "/batch_clear", method = RequestMethod.GET)
	@ApiOperation("模糊清除缓存")
	public Object batchCache(@ApiParam(value = "要清除的key前缀") @RequestParam("prefix") String prefix) {
		RedisFactory.batchClearRedis(prefix);
		return "success";
	}

	@FormatResult
	@RequestMapping(value = "/db_list", method = RequestMethod.GET)
	@ApiOperation("获取全文检索库(es)库列表")
	public Object getDBList() {
		return ESFactory.getDBService().getDatabaseList();
	}

	@FormatResult
	@RequestMapping(value = "/sub_list", method = RequestMethod.GET)
	@ApiOperation("获取全文检索库(es)子库列表")
	public Object getDBSubList(@ApiParam(value = "要查询的库名") @RequestParam("db_name") String dbName)
			throws OperationException {
		try {
			return ESFactory.getDBService().getSubDatabaseList(dbName);
		} catch (TRSException e) {
			throw new OperationException(String.format("获取全文检索库(es)[%s]子库列表失败", dbName));
		}
	}

	@FormatResult
	@RequestMapping(value = "/db_detail", method = RequestMethod.GET)
	@ApiOperation("获取全文检索库(es)库详情")
	public Object getDBDetail(@ApiParam(value = "要查询的库名") @RequestParam("db_name") String dbName)
			throws OperationException {
		try {
			return ESFactory.getDBService().getDababaseInfoByName(dbName);
		} catch (TRSException e) {
			throw new OperationException(String.format("获取全文检索库(es)[%s]详情失败", dbName));
		}
	}

	@SuppressWarnings("rawtypes")
	@FormatResult
	@RequestMapping(value = "/db_field", method = RequestMethod.GET)
	@ApiOperation("显示全文检索库(es)库指定字段")
	public Object getDBField(@ApiParam(value = "要查询的库名") @RequestParam("db_name") String dbName,
			@ApiParam(value = "要查询的字段") @RequestParam("field") String... field) throws OperationException {
		try {
			List<Map> result = new ArrayList<>();
			TRSEsDatabase database = ESFactory.getDBService().getDababaseInfoByName(dbName);
			Set<TRSEsDatabaseField> fields = database.getTrsEsDatabaseFieldSet();
			for (TRSEsDatabaseField field1 : fields) {
				result.add(ReflectUtil.getFields(field1, field));
			}
			return result;
		} catch (TRSException e) {
			throw new OperationException(String.format("显示全文检索库(es)[%s]库指定字段", dbName));
		}
	}

}
