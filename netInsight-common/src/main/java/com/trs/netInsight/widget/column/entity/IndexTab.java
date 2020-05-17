
package com.trs.netInsight.widget.column.entity;

import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * 三级栏目（图）实体类
 * 
 * @author xiaoying
 *
 */
@Table(name = "index_tab")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IndexTab extends BaseEntity implements Cloneable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5624243370102578700L;

	@Column(name = "name")
	private String name;// 三级栏目（图）名

	@Column(name = "trsl", columnDefinition = "TEXT")
	private String trsl;// trs表达式

	@Column(name = "xy_trsl", columnDefinition = "TEXT")
	private String xyTrsl;// x轴y轴 表达式

	// private ColumnType type;//图表类型
	@Column(name = "type")
	private String type;// 图表类型
	
	/**
	 * 分类对比类型
	 */
	@Column(name = "contrast")
	private String contrast;

	@Column(name = "tradition")
	private String tradition;// 传统来源细分

	@Column(name = "excludeWeb")
	private String excludeWeb;// 排除网站
	

	@Column(name = "parent_id")
	private String parentId;// 二级栏目的id

	@Column(name = "sequence")
	private int sequence;

	@Column(name = "max_size")
	private int maxSize;// 最大条数

	@Column(name = "time_range")
	private String timeRange;// 发布时间范围

	@Column(name = "time_recent")
	private String timeRecent;// 最近发布时间

	@Column(name = "hide")
	private boolean hide;

	@Column(name = "status_trsl", columnDefinition = "TEXT")
	private String statusTrsl;// 微博检索表达式

	@Column(name = "we_chat_trsl", columnDefinition = "TEXT")
	private String weChatTrsl;// 微信检索表达式

	/**
	 * 检索关键词
	 */
	@Column(name = "key_word", columnDefinition = "TEXT")
	private String keyWord;
	
	/**
	 * 排除词
	 */
	@Column(name = "exclude_words", columnDefinition = "TEXT")
	private String excludeWords;

	/**
	 * 检索关键词位置 0：标题 1：标题+正文
	 */
	@Column(name = "key_word_index", columnDefinition = "TEXT")
	private String keyWordIndex;

	/**
	 * xy轴检索关键词 exp:北京=xx;南京=xx;
	 */
	@Column(name = "xy_key_word", columnDefinition = "TEXT")
	private String xyKeyWord;

	/**
	 * xy轴检索关键词位置
	 */
	@Column(name = "xy_key_kord_index", columnDefinition = "TEXT")
	private String xyKeyWordIndex;

	/**
	 * 保存栏目类型,多值,中间以';'分隔
	 */
	@Column(name = "group_name", columnDefinition = "TEXT")
	private String groupName;
	/**
	 * 是否排重
	 */
	@Column(name = "similar")
	private boolean similar;
	
	/**
     * 按urlname排重  0排 1不排  true排 false不排
     */
    @Column(name = "ir_simflag")
    private boolean irSimflag = false;

	/**
	 * 跨数据源排重
	 */
	@Column(name = "ir_simflag_all")
	private boolean irSimflagAll = false;
    /**
	 * 是否按照权重查找
	 */
	@Column(name = "weight")
	private boolean weight = false;

	@Column(name = "server")
	private boolean server = false;
	
	/**
	 * 50为半栏 100为通栏
	 */
	@Column(name = "tab_width")
	private int tabWidth = 50;
	
	public String getTabWidth(){
		return String.valueOf(this.tabWidth);
	}
	
	/**
	 * 一级栏目名
	 */
	@Transient
	private String oneName;

	/**
	 * 此字段专 为 专报、极简报告而设
	 */
	@Transient
	private String notSids;

	public String[]  getType(){
		if (StringUtil.isNotEmpty(this.type)){
			return this.type.trim().split(";");
		}
		return null;
	}
	public String[]  getTradition(){
		if (StringUtil.isNotEmpty(this.tradition)){
			return tradition.trim().split(";");
		}
		return null;
	}
	public String[]  getExcludeWeb(){
		if (StringUtil.isNotEmpty(this.excludeWeb)){
			return excludeWeb.trim().split(";|；");
		}
		return null;
	}


	public IndexTab(String name, String trsl,String keyWord,String excludeWords,String keyWordIndex, String xyTrsl, String type, String parentId, String groupName,
			int sequence, int maxSize, String timeRange, String timeRecent,boolean similar,boolean irSimflag,boolean weight,boolean server,boolean irSimflagAll) {
		this.name = name;
		this.trsl = trsl;
		this.keyWord = keyWord;
		this.excludeWords = excludeWords;
		this.keyWordIndex = keyWordIndex;
		this.xyTrsl = xyTrsl;
		this.type = type;
		this.parentId = parentId;
		this.groupName = groupName;
		this.sequence = sequence;
		this.maxSize = maxSize;
		this.timeRange = timeRange;
		this.timeRecent = timeRecent;
		this.similar = similar;
		this.irSimflag = irSimflag;
		// 默认不隐藏
		this.hide = false;
		this.weight = weight;
		this.server = server;
		this.irSimflagAll = irSimflagAll;
	}
	
	public IndexTab(String name, String trsl,String statusTrsl,String weChatTrsl,String keyWord,String excludeWords,String keyWordIndex, String xyTrsl, String type, String parentId, String groupName,
			int sequence, int maxSize, String timeRange, String timeRecent,boolean similar,boolean irSimflag,boolean weight,boolean server,boolean irSimflagAll) {
		this.name = name;
		this.trsl = trsl;
		this.statusTrsl = statusTrsl;
		this.weChatTrsl = weChatTrsl;
		this.keyWord = keyWord;
		this.excludeWords = excludeWords;
		this.keyWordIndex = keyWordIndex;
		this.xyTrsl = xyTrsl;
		this.type = type;
		this.parentId = parentId;
		this.groupName = groupName;
		this.sequence = sequence;
		this.maxSize = maxSize;
		this.timeRange = timeRange;
		this.timeRecent = timeRecent;
		this.similar = similar;
		this.irSimflag = irSimflag;
		// 默认不隐藏
		this.hide = false;
		this.weight = weight;
		this.server = server;
		this.irSimflagAll = irSimflagAll;
	}

	/**
	 * 复制tab，谨慎使用，会更新持久态对象
	 * @date Created at 2018年3月22日  上午11:04:55
	 * @Author 谷泽昊
	 * @return
	 */
	@Override  
    public IndexTab clone() {  
		IndexTab indexTab = null;  
        try{  
        	indexTab = (IndexTab)super.clone();  
        }catch(CloneNotSupportedException e) {  
            e.printStackTrace();  
        }  
        return indexTab;  
    }  
	
	/**
	 * 复制栏目
	 * @since changjiang @ 2018年9月13日
	 * @return
	 * @Return : IndexTab
	 */
	public IndexTab tabCopy(){
		IndexTab indexTab=new IndexTab(name, trsl, xyTrsl, type,contrast, tradition, excludeWeb, parentId, sequence, 
				maxSize, timeRange, timeRecent, hide, statusTrsl, weChatTrsl, keyWord,
				excludeWords, keyWordIndex, xyKeyWord, xyKeyWordIndex, groupName,similar,irSimflag,irSimflagAll,weight,server,tabWidth,oneName,notSids);
		return indexTab;
	}
	
	/**
	 * 映射关联
	 * @since changjiang @ 2018年9月18日
	 * @return
	 * @Return : IndexTabMapper
	 */
	public IndexTabMapper mapper(){
		IndexTabMapper mapper = new IndexTabMapper();
		mapper.setHide(this.hide);
		mapper.setIndexTab(this);
		mapper.setMe(true);
		mapper.setSequence(this.sequence);
		mapper.setTabWidth(this.tabWidth);
		return mapper;
	}
	
	/**
	 * 映射关联
	 * @since changjiang @ 2018年10月9日
	 * @param share
	 * @return
	 * @Return : IndexTabMapper
	 */
	public IndexTabMapper mapper(boolean share){
		IndexTabMapper mapper = new IndexTabMapper();
		mapper.setHide(this.hide);
		mapper.setUserId(super.getUserId());
		mapper.setIndexTab(this);
		mapper.setMe(true);
		mapper.setSequence(this.sequence);
		mapper.setTabWidth(this.tabWidth);
		mapper.setShare(share);
		return mapper;
	}
	
	/**
	 * 持久态对象转为瞬时状态
	 * @since changjiang @ 2018年10月15日
	 * @return
	 * @Return : IndexTab
	 */
	public void clear(){
		super.setId(null);
		super.setCreatedTime(null);
		super.setCreatedUserId(null);
		super.setUserAccount(null);
		super.setUserId(null);
	}

}
