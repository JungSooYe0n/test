package com.trs.netInsight.widget.spread.entity;

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
 * 新浪微博用户
 *
 * Created by ChangXiaoyang on 2017/3/9.
 */
@Setter
@Getter
@NoArgsConstructor
@FtsClient(hybaseType = FtsHybaseType.WEIBO)
public class SinaUser extends IDocument {
	
	@FtsField("IR_SID")
	private String sid;

	@FtsField("IR_RETWEETED_SCREEN_NAME")
    private List<String> fromScreenName;

	@FtsField("IR_SCREEN_NAME")
    private String name;

    @FtsField("IR_RETWEETED_URL")
    private String rUrl;

    @FtsField("IR_URLNAME")
    private String url;

    @FtsField("IR_CONTENT")
    private String content;

    @FtsField("IR_UID")
    private String uid;

    @FtsField("IR_MID")
    private String mid;

    @FtsField("IR_URLTIME")
    private Date createdAt;
    
    @FtsField("MD5TAG")
    private String md5;
    
    private String trslk;

    /**
     *截取微博内容的 30个字
     * @return
     */
    /*public String getContent(){

    }*/

    private List<SinaUser> children;

    @Override
    public String toString() {
        return name;
    }

    public String baseUrl() {
        return StringUtil.isEmpty(this.rUrl) ? this.url : this.rUrl;
    }
}
