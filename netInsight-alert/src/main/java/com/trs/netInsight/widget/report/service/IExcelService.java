package com.trs.netInsight.widget.report.service;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.report.excel.ExcelData;
import com.trs.netInsight.widget.report.entity.enums.ExportListType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 导出数据到Excel
 *
 * Created by xiaoying on 2017/12/13.
 */
public interface IExcelService {

    /**
     * 检索ids，将数据导出到excel 传统库
     * @param ids 文章主键
     * @param sort 排序方式
     * @param builder 构造表达式
     * @param head 导出头
     * @param headEnglish 导出头字段
     * @return
     * @throws Exception
     * created by xiaoying
     */
    public ByteArrayOutputStream export(String trslk,String ids,String sort,QueryBuilder builder,String head,String headEnglish,boolean handleAbstract,String groupName) throws Exception ;
    

    /**
     * 按照数量导出 传统
     * @param trslk 表达式
     * @param num 数量
     * @param sort 排序方式
     * @param sim 是否排重
     * @param server 是否转换为server
     * @param head 导出头
     * @param headEnglish 导出头字段
     * @return
     * @throws Exception
     * created by xiaoying
     */
    public ByteArrayOutputStream exportNum(String trslk,int num, String sort,boolean sim,boolean server,String head,String headEnglish,boolean handleAbstract,String groupName,boolean weight) throws Exception;
    
    public ByteArrayOutputStream exportNumTest(String trslk,int num, String sort,boolean sim,boolean server) throws Exception;
    
    /**
     * 按照数量导出  针对预警
     * @param time 时间
     * @param groupname 来源
     * @param keywords 关键词
     * @param num 导出数量
     * @param way 站内SMS 还是已发送SEND
     * @param head 导出头
     * @param headEnglish 导出头字段
     * @return
     * @throws Exception
     * created by xiaoying
     */
    public ByteArrayOutputStream exportNumForAlert(String time,String groupname,String keywords, int num,String way,String head,String headEnglish) throws Exception;

    /**
     * 获取当前要到处的预警的ids 以;分隔
     * @param time
     * @param groupname
     * @param keywords
     * @param num
     * @param way
     * @return
     * @throws Exception
     */
    public String[] getIds(String time,String groupname,String keywords, int num,String way) throws Exception;
    
    /**
     * 检索mid，将数据导出到excel 微博库
     * @param mid 文章mid
     * @param sort 排序方式
     * @param builder
     * @param head 导出头
     * @param headEnglish 导出头
     * @return
     * @throws Exception
     * CreatedBy xiaoying
     */
    public ByteArrayOutputStream exportWeiBo(String mid, String sort,QueryBuilder builder,String head,String headEnglish,String groupName) throws Exception ;
    
    /**
     * 按照数量导出  微博
     * @param trslk 表达式
     * @param num 数量
     * @param sort 排序方式
     * @param sim 是否排重
     * @param server 是否转换为server
     * @param head 导出头
     * @param headEnglish 导出头字段
     * @return
     * @throws Exception
     * CreatedBy xiaoying
     */
    public ByteArrayOutputStream exportNumWeiBo(String trslk,int num, String sort,boolean sim,boolean server,String head,String headEnglish,String groupName) throws Exception;
    
    /**
     * 检索hkey，将数据导出到excel 微信库
     * @param hkey 微信主键
     * @param sort 排序方式
     * @param builder
     * @param head 导出头
     * @param headEnglish 导出头字段
     * @return
     * @throws Exception
     * CreatedBy xiaoying
     */
    public ByteArrayOutputStream exportWeiXin(String trslk,String hkey, String sort,QueryBuilder builder,String head,String headEnglish,boolean handleAbstract,String groupName) throws Exception ;
    
    /**
     * 按照数量导出 微信
     * @param trslk 表达式
     * @param num 数量
     * @param sort 排序方式
     * @param sim 是否排重
     * @param server 是否转换为server
     * @param head 导出头
     * @param headEnglish 导出头字段
     * @return
     * @throws Exception
     * CreatedBy xiaoying
     */
    public ByteArrayOutputStream exportNumWeiXin(String trslk,int num, String sort,boolean sim,boolean server,String head,String headEnglish,boolean handleAbstract,String groupName) throws Exception;
    
    /**
     * 检索sid，将数据导出到excel 海外库
     * @param sid 文章主键
     * @param sort 排序方式
     * @param builder
     * @param head 导出头
     * @param headEnglish 导出头字段
     * @return
     * @throws Exception
     * CreatedBy xiaoying
     */
    public ByteArrayOutputStream exportOversea(String sid, String sort,QueryBuilder builder,String head,String headEnglish,String groupName) throws Exception ;
    
    /**
     * 按数量导出 海外库
     * @param trslk 表达式
     * @param num 导出数量
     * @param sort 排序方式
     * @param sim 是否排重
     * @param server 是否转换为server
     * @param head 导出头
     * @param headEnglish 导出头字段
     * @return
     * @throws Exception
     * CreatedBy xiaoying
     */
    public ByteArrayOutputStream exportNumOversea(String trslk,int num, String sort,boolean sim,boolean server,String head,String headEnglish,String groupName) throws Exception;
    
    /**
     * 导出我的收藏列表
     * @param groupName 来源
     * @param dearNum 导出数量
     * @param keywords 结果中搜索
     * @return
     * xiaoying
     */
    public ByteArrayOutputStream exportDear(String groupName,int dearNum,String keywords,String invitationCard,String forwarPrimary,String head,String headEnglish,String type,String libraryId,String time) throws Exception ;
    
    /**
     * 混合导出
     * @param id 专题或栏目id
     * @param mixid 各种主键
     * @param groupName 来源
     * @param timeBuilder
     */
    public String exportMix(String id,String mixid,String groupName,QueryBuilder timeBuilder)throws TRSException, IOException, TRSSearchException;

    public String exportAllMix(String id,String mixid, String groupName,QueryBuilder builderTime)
            throws TRSException, IOException, TRSSearchException;
    /**
     * 混合按数量导出
     * @param trslk 查询条件
     * @param num 数量
     * @return
     * @throws Exception
     */
    public ByteArrayOutputStream exportNumMix(String trslk,String mtrslk,String htrslk,int num) throws Exception;
    
    /**
     * 往excel的每一行里放数据
     * @param englishArray 导出字段
     * @param document 实体
     * @param data ExcelData
     * @param i 第几行
     * @throws TRSException
     * CreatedBy xiaoying
     */
    public void putData(String[] englishArray,Object document,ExcelData data,int i) throws TRSException;

    /**
     * 往excel的每一个工作簿中添加数据
     * @param englishArray 导出字段
     * @param document 实体
     * @param data ExcelData
     * @param i 第几行
     * @param key 工作簿的名字
     * @throws TRSException
     */
    public void putDataSheet(String[] englishArray,Object document,ExcelData data,int i,String key) throws TRSException;

    /**
     * 通过id查询数据并存储到redis中
     * @param ids 所选id
     * @param groupNames 所选id对应的数据来源
     * @param queryBuilder
     * @throws TRSException
     */
    public Object queryByIds(String ids,String groupNames,QueryBuilder queryBuilder)throws TRSException;

    public Object queryByNum(ExportListType exportListType, String trslk, String sort, Integer num, String[] source,Boolean weight)throws TRSException;

    public Object queryByNumOfOther(ExportListType exportListType, Integer num, String[] source,
                                    String time, String libraryId, String invitationCard, String forwarPrimary, String keywords,String fuzzyValueScope,String receivers) throws TRSException;

}
