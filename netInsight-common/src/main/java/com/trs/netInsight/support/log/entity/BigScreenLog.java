package com.trs.netInsight.support.log.entity;

import com.trs.netInsight.util.DateUtil;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 大屏点击日志记录
 * @author lilyy
 * @date 2020/2/20 10:11
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bigscreen_log")
public class BigScreenLog implements Serializable {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "`id`")
    private String id;

    /**
     * 对象创建时间
     */
    @Column(name = "`created_time`", updatable = true)
    private Date createdTime;

    /**
     * 对象创建日期
     */
    @Column(name = "`created_date`")
    private String createdDate;

    @PrePersist
    protected void onCreate() {
        createdTime = new Date();
        createdDate = DateUtil.getNowDate();
    }
    /**
     * 访问者ip
     */
    @Column(name = "`request_ip`")
    private String requestIp;

    /***
     * 操作客户端信息(operation system infomation)
     */
    @Column(name = "`os_info`")
    private String osInfo;

    /***
     * 操作浏览器信息
     */
    @Column(name = "`browser_info`")
    private String browserInfo;

    /***
     * sessionId
     */
    @Column(name = "`session_id`")
    private String sessionId;

    /**
     * 请求uri
     */
    @Column(name = "`descript`")
    private String descript;


    public BigScreenLog(Date createdTime,String createdDate,String requestIp,String osInfo,
                        String browserInfo,String sessionId,String descript){
        this.createdTime = createdTime;
        this.createdDate = createdDate;
        this.requestIp = requestIp;
        this.osInfo = osInfo;
        this.browserInfo = browserInfo;
        this.sessionId = sessionId;
        this.descript = descript;
    }



}
