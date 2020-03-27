package com.trs.netInsight.widget.analysis.entity;

import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.annotation.enums.FtsHybaseType;
import com.trs.netInsight.support.fts.model.result.IDocument;
import com.trs.netInsight.util.StringUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * 传播返回实体
 *
 * Create by yan.changjiang on 2017年11月21日
 */
@Setter
@Getter
@NoArgsConstructor
@FtsClient(hybaseType = FtsHybaseType.WEIBO)
public class SpreadEntity extends IDocument {

	@FtsField("IR_URLTIME")
	private Date createdAt;

	@FtsField("IR_URLNAME")
	private String url;

	@FtsField("IR_SCREEN_NAME")
	private String screenName;

	@FtsField("IR_RETWEETED_URL")
	private String rUrl;

	@FtsField("IR_RETWEETED_FROM")
	private List<String> retFrom;
	
	@FtsField("IR_SID")
	private String sid;
	
	@FtsField("MD5TAG")
	private String md5;
	
	private String trslk;

	private int retFr1asom;
	private long ret2Frasom;
	private Integer retFrasom;
	private Long retFra1som;

	private boolean retFrasasom;
	private Boolean retFdsra1som;

	public String baseUrl() {
		return StringUtil.isEmpty(this.rUrl) ? this.url : this.rUrl;
	}

}
