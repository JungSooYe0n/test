package com.trs.netInsight.widget.column.entity;

import lombok.Getter;
import org.apache.poi.ss.formula.functions.T;

import java.io.Serializable;
import java.util.List;

@Getter
public class ColumnPageBean<T> implements Serializable {

    //已知数据
    private int pageNum;    //当前页,从请求那边传过来。
    private int pageSize;    //每页显示的数据条数。
    private int totalRecord;    //总的记录条数。查询数据库得到的数据

    //需要计算得来
    private int totalPage;    //总页数，通过totalRecord和pageSize计算可以得来

    //开始索引，也就是我们在数据库中要从第几行数据开始拿，有了startIndex和pageSize，
    //就知道了limit语句的两个数据，就能获得每页需要显示的数据了
    private int startIndex;

    //将每页要显示的数据放在list集合中
    private List<T> list;
    /**
     * 当前页的总记录数.
     */
    private int thisPageTotal;

    //通过pageNum，pageSize，totalRecord计算得来tatalPage和startIndex
    //构造方法中将pageNum，pageSize，totalRecord获得
    public ColumnPageBean(int pageNum,int pageSize,int totalRecord) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalRecord = totalRecord;

        //totalPage 总页数
        if(totalRecord%pageSize==0){
            //说明整除，正好每页显示pageSize条数据，没有多余一页要显示少于pageSize条数据的
            this.totalPage = totalRecord / pageSize;
        }else{
            //不整除，就要在加一页，来显示多余的数据。
            this.totalPage = totalRecord / pageSize +1;
        }
        //开始索引
        this.startIndex = (pageNum)*pageSize ;

        //如果开始索引大于总条数，则开始索引等于总条数 当前页条数=0
        if(startIndex > totalRecord){
            this.startIndex = totalRecord;
            this.thisPageTotal = 0;
        }else{
            if(pageNum+1 == totalPage){
                this.thisPageTotal = this.totalRecord -this.startIndex;
            }else{
                this.thisPageTotal=this.pageSize;
            }
        }
    }
    public ColumnPageBean(int pageNum,int pageSize,int totalRecord,List<T> list){
        this(pageNum,pageSize,totalRecord);
        this.list = list;
    }

    /**
     * 传进来的 list 全部结果，等同于totalrecord的数量，需要这个类对这个list进行分页操作，进行截取
     *
     */
    public ColumnPageBean subList(){
        if(this.thisPageTotal == 0){
            this.list = null;
            return null;
        }else{
            this.list = this.list.subList(this.startIndex,this.startIndex+this.thisPageTotal);
            return this;
        }
    }
   public List<T> getList(){
        return this.list;
   }

}
