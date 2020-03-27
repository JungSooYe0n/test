package com.trs.netInsight.widget.spread.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
@Setter
@Getter
public class Node {
	 private String id;
     private String label;
     private String source;
}
