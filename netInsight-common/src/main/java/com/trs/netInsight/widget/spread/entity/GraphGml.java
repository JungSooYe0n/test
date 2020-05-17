package com.trs.netInsight.widget.spread.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * .gml 文件类
 *
 * Created by ChangXiaoyang on 2017/3/9.
 */
@Getter
@NoArgsConstructor
public class GraphGml {

    private Node node;
    private Edge edge;

    public void setNode(String id, String label, String value, String source) {
        this.node = new Node(id, label, value, source);
    }

    public void setEdge(String source, String target) {
        this.edge = new Edge(source, target);
    }

    @Data
    @AllArgsConstructor
    public class Node {
        private String id;
        private String label;
        private String value;
        private String source;
    }

    @Data
    @AllArgsConstructor
    public class Edge {
        private String source;
        private String target;
    }
}
