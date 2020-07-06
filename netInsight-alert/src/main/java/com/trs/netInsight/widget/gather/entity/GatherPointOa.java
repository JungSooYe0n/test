package com.trs.netInsight.widget.gather.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GatherPointOa {
    private String sitename;
    private String channel;
    private String urlname;
    private String url;
    private String priority;
    private String uniqueId;
}
