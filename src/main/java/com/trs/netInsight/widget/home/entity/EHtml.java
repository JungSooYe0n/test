package com.trs.netInsight.widget.home.entity;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;

/**
 *
 * Created by trs on 2017/7/7.
 */
@Data
public class EHtml {

    interface Hide{}
    public interface Show{}

    private String title;

    @JsonView(Hide.class)
    private String tit_url;
    @JsonView(Hide.class)
    private String detail_url;
    private long clicks;
    @JsonView(Hide.class)
    private String trend;
}
