package com.trs.netInsight.widget.spread.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
@Setter
@Getter
public class Edge {
	 public String source;
     public String target;
}
