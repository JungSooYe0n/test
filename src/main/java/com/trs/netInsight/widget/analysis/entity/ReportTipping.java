package com.trs.netInsight.widget.analysis.entity;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.model.result.IDocument;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 引爆点结果封装
 *
 * Created by xqj
 */
@Setter
@Getter
@NoArgsConstructor
@FtsClient(indices = Const.ES_INDEX_WEIBO)
public class ReportTipping extends IDocument {

	@FtsField("IR_SCREEN_NAME")
	private String screenName;

	@FtsField("IR_RTTCOUNT")
	private String rttCount;

	private String imageUrl = "http://119.254.92.55:8000/logo/l8rmua.jpg";

}
