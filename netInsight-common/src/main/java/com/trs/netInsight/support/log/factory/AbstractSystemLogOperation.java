package com.trs.netInsight.support.log.factory;

import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.widget.column.repository.IndexPageRepository;
import com.trs.netInsight.widget.column.repository.IndexTabMapperRepository;
import com.trs.netInsight.widget.column.repository.NavigationRepository;
import com.trs.netInsight.widget.special.entity.repository.SpecialProjectRepository;
import com.trs.netInsight.widget.special.entity.repository.SpecialSubjectRepository;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日志操作检索抽象类
 * 
 * @Type AbstractSystemLog.java
 * @Desc
 * @author 北京拓尔思信息技术股份有限公司
 * @author 谷泽昊
 * @date 2018年11月7日 上午10:37:45
 * @version
 */
public abstract class AbstractSystemLogOperation {

	protected SpecialProjectRepository specialProjectService;
	protected IndexTabMapperRepository indexTabMapperService;
	protected IndexPageRepository indexPageService;
	protected NavigationRepository navigationService;
	protected SpecialSubjectRepository specialSubjectService;
	/**
	 * 初始化
	 * 
	 * @date Created at 2018年11月7日 下午5:44:03
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param specialProjectService
	 * @param indexTabMapperService
	 * @param indexPageService
	 * @param navigationService
	 */
	public void init(SpecialProjectRepository specialProjectService, IndexTabMapperRepository indexTabMapperService,
					 IndexPageRepository indexPageService, NavigationRepository navigationService, SpecialSubjectRepository specialSubjectService) {
		this.specialProjectService = specialProjectService;
		this.indexTabMapperService = indexTabMapperService;
		this.indexPageService = indexPageService;
		this.navigationService = navigationService;
		this.specialSubjectService = specialSubjectService;
	}

	/**
	 * 获取操作
	 * 
	 * @date Created at 2018年11月7日 下午5:39:28
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param parameterMap
	 * @param operationPosition
	 * @return
	 */
	public abstract String getOperationPosition(Map<String, String[]> parameterMap, String operationPosition,SystemLogOperation systemLogOperation);

	/**
	 * 获取key中指定参数，比如 1234${name}5678,则获取参数中name的值
	 * 
	 * @date Created at 2018年7月27日 下午4:28:17
	 * @Author 谷泽昊
	 * @param key
	 * @param map
	 * @return
	 */
	protected String getValueByParamAndKey(String key, Map<String, String[]> map) {
		String name = getKey(key);
		if (StringUtils.isNotBlank(name)) {
			String temp = name.substring(2);
			temp = temp.substring(0, temp.length() - 1);
			return map.get(temp)[0];
		}
		return null;
	}

	/**
	 * 找到指定的key，比如 1234${name}5678,则获取到${name}
	 * 
	 * @date Created at 2018年11月7日 下午6:06:26
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param key
	 * @return
	 */
	protected String getKey(String key) {
		if (!key.contains("$")) {
			return null;
		}
		String regexp = "\\$\\{[^\\}]+\\}";
		Pattern pattern = Pattern.compile(regexp);
		Matcher matcher = pattern.matcher(key);
		List<String> names = new ArrayList<String>();
		try {
			while (matcher.find()) {
				names.add(matcher.group());
			}
			if (names != null && names.size() > 0) {
				return names.get(0);
			}

		} catch (Exception e) {
		}
		return null;
	}
}
