package com.trs.netInsight.widget.spread.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Graph实体类
 *
 * Created by ChangXiaoyang on 2017/3/11.
 */
public class GraphMap implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = -4654050659170429632L;

//	private Node root;

    private List<Graph> graph;

    public int size() {
        return this.graph.size();
    }

    public GraphMap(){
        this.graph = new ArrayList<>();
    }

    public void addGraph( String uuid, String fromId, String screenName, String fromName
                         ) {//好像就这有用
        this.graph.add(new Graph(
                new Node(uuid, screenName,  fromName),
                new Edge(uuid, fromId)));
    	/*Map map=new HashMap<>();
    	this.graph.add((Graph) map.put(new Node(uuid, screenName,  fromName),
                new Edge(uuid, fromId)))
        ;*/
    }

    public List<Graph> getGraph() {
        return graph;
    }

   /* @AllArgsConstructor
    public class Graph {

        private Node node;
        private Edge edge;
//        Map map=new HashMap<>();

    }*/

 /*   @Data
    @AllArgsConstructor
    public class Node {
        public String id;
        public String label;
        public String source;
    }

    @Data
    @AllArgsConstructor
    public class Edge {
        public String source;
        public String target;
    }*/
}
