package com.trs.netInsight.widget.analysis.entity;

import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.annotation.enums.FtsHybaseType;
import com.trs.netInsight.support.fts.model.result.IDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 引爆点结果封装
 *
 * Create by yan.changjiang on 2017年11月21日
 */
@Setter
@Getter
@NoArgsConstructor
@FtsClient(hybaseType = FtsHybaseType.WEIBO)
public class TippingPoint extends IDocument {

	@FtsField("IR_SCREEN_NAME")
	private String screenName;

	@FtsField("IR_RTTCOUNT")
	private long rttCount;

	/*@FtsField("IR_CREATED_AT")
	private String createdAt;*/
	
	@FtsField("IR_SID")
	private String sid;
	
	@FtsField("MD5TAG")
	private String md5;
	
	private String trslk;


	private String imageUrl = "http://119.254.92.55:8000/logo/l8rmua.jpg";

}
