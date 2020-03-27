package com.trs.netInsight.support.appApi.entity;

import com.trs.netInsight.support.appApi.utils.constance.ApiFrequencyConst;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * api请求频率表<br>
 * 所有ApiClient均使用默认频率,特殊Api频率单独维护一条数据,用程序或Sql进行区分
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年7月2日
 *
 */
@Entity
@Table(name = "app_api_frequency")
@Getter
@Setter
public class AppApiFrequency extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8163953870727596465L;

	/**
	 * 自定义clientId
	 */
	@Column(name = "client_id")
	private String clientId;

	/**
	 * api 代码
	 */
	@Column(name = "code")
	private int code;

	/**
	 * api名称
	 */
	@Column(name = "name")
	private String name;

	/**
	 * 低频,格式('maxM;maxH')<br>
	 * maxH:每小时调用量阈值,默认每小时最大调用10次<br>
	 * maxD:每天调用量阈值,默认每天最大嗲用100次
	 */
	@Column(name = "frequency_low")
	private String frequencyLow = "10;100";

	/**
	 * 中频,格式('maxM;maxH')<br>
	 * maxH:每小时调用量阈值,默认每小时最大调用30次<br>
	 * maxD:每天调用量阈值,默认每天最大嗲用300次
	 */
	@Column(name = "frequency_common")
	private String frequencyCommon = "30;300";

	/**
	 * 高频,格式('maxM;maxH')<br>
	 * maxH:每小时调用量阈值,默认每小时最大调用60次<br>
	 * maxD:每天调用量阈值,默认每天最大嗲用1000次
	 */
	@Column(name = "frequency_high")
	private String frequencyHigh = "60;1000";

	/**
	 * 自定义频率
	 */
	@Column(name = "frequency_custom")
	private String frequencyCustom;

	/**
	 * 选择频率
	 * 
	 * @since changjiang @ 2018年7月4日
	 * @param level
	 * @Return : void
	 */
	public void chooseLevel(String level) {
		if (StringUtils.isNotBlank(level) && StringUtils.isBlank(frequencyCustom)) {
			switch (level) {
			case ApiFrequencyConst.LEVEL_LOW:
				this.frequencyCustom = frequencyLow;
				break;
			case ApiFrequencyConst.LEVEL_HIGH:
				this.frequencyCustom = frequencyHigh;
				break;
			case ApiFrequencyConst.LEVEL_COMMON:
				this.frequencyCustom = frequencyCommon;
			}
		}
	}

}
