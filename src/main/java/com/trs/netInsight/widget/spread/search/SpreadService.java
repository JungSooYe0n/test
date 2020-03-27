package com.trs.netInsight.widget.spread.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.support.cache.TimingCachePool;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.template.GUIDGenerator;
import com.trs.netInsight.util.MapUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.spread.entity.Document;
import com.trs.netInsight.widget.spread.entity.Edge;
import com.trs.netInsight.widget.spread.entity.Graph;
import com.trs.netInsight.widget.spread.entity.GraphMap;
import com.trs.netInsight.widget.spread.entity.Node;
import com.trs.netInsight.widget.spread.entity.SinaUser;
import com.trs.netInsight.widget.spread.util.MultiKVMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SpreadService {

	@Autowired
	private SpreadSearchService spreadSearchService;

	private static String db = Const.WEIBO;

	/**
	 * 获取原文
	 *
	 * @param url
	 *            要计算的URL
	 * @return 原文详情
	 */
	// private Document getDoc(String url) throws Exception {
	// TRSEsSearchParams params = new TRSEsSearchParams();
	// params.setQuery("IR_URLNAME:\"" + url + "\"");
	// List<Document> documentList = spreadSearchService.list(params,
	// Document.class, db);
	// if (documentList == null || documentList.size() == 0)
	// throw new Exception();
	// Document document = documentList.get(0);
	// document.setRootUrl(StringUtil.isEmpty(document.getRootUrl())
	// ? document.getCurrentUrl()
	// : document.getRootUrl());
	// document.setId(GUIDGenerator.generateName());
	//
	// return document;
	// }

	private Document getDocNew(String url) throws Exception {
		QueryBuilder builder = new QueryBuilder();
		String trsl = "IR_URLNAME:\"" + url + "\"";
		builder.filterByTRSL(trsl);
		List<Document> documentList = spreadSearchService.listNew(builder, Document.class, db);
		if (documentList == null || documentList.size() == 0) {
			throw new Exception();
		}
		Document document = documentList.get(0);
		document.setRootUrl(
				StringUtil.isEmpty(document.getRootUrl()) ? document.getCurrentUrl() : document.getRootUrl());
		document.setId(GUIDGenerator.generateName());

		return document;
	}

	/**
	 * 获取参与转发的所有用户
	 *
	 * @param document
	 *            微博文档
	 * @return LinkedMultiValueMap
	 */
	// private MultiKVMap<String, SinaUser> getAllRTTUser(Document document)
	// throws Exception {
	// TRSEsSearchParams params = new TRSEsSearchParams();
	// params.setResultSize(0, 5000);
	// params.setQuery("IR_RETWEETED_URL:\"" + document.getRootUrl() + "\"");
	// return spreadSearchService.scrollForSpread(params, db);
	// }
	private MultiKVMap<String, SinaUser> getAllRTTUserNew(Document document) throws Exception {
		QueryBuilder builder = new QueryBuilder();
		// builder.page(0, 5000);
		builder.page(0, 10000);
		String trsl = "IR_RETWEETED_URL:\"" + document.getRootUrl() + "\"";
		builder.filterByTRSL(trsl);
		return spreadSearchService.scrollForSpreadNew(builder, db);
	}

	/**
	 * 逐层取数据，直到 null
	 */
	private void generateMap(GraphMap graphMap, MultiKVMap<String, SinaUser> allUser, SinaUser fromUser, boolean first,
			int level, int num) throws Exception {

		if (level == 0) {
			return;
		}
		log.info(fromUser.getName());
		// 获取转发 sinaUser的所有用户 用户就是那些点
		String fromUserName = fromUser.getName();
		List<SinaUser> userList = allUser.remove(fromUserName);// remove方法就是把remove掉的东西返回去
																// 把remove掉的东西存起来
		// 为空直接返回
		if (first) {
			if (userList == null) {
				userList = new ArrayList<>();
			}
			List<SinaUser> remove = allUser.remove(null);
			if (remove != null) {
				userList.addAll(remove);
			}
		}
		if (userList == null || userList.size() == 0) {
			return;
		}
		int n = 0;
		for (int i = 0; i < userList.size(); i++) {
			SinaUser user = userList.remove(i);

			graphMap.addGraph(user.getId(), fromUser.getId(), user.getName(), fromUser.getName());
			if (allUser.containsKey(user.getName())) {
				// 递归取下层数据
				generateMap(graphMap, allUser, user, false, level - 1, num);
			}
			if (++n == num) {
				break;
			}
		}

	}

	/**
	 * 微博传播路径分析入口
	 *
	 * @param url
	 *            要分析的Url
	 * @param level
	 *            最多分析层级
	 * @param num
	 *            每层最多数量
	 * @return Object
	 */
	// @RequestMapping(value = "/url", method = RequestMethod.GET)
	// @ResponseBody
	@SuppressWarnings("rawtypes")
	public Object url(String url, int level, int num) throws Exception {

		String cacheKey = level + num + url;
		// Object obj = TimingCachePool.get(cacheKey);
		// if (obj != null) return obj;

		// 通过url获取原文
		// Document doc = getDoc(url);
		Document doc = getDocNew(url);
		// 获取所有参与转发的用户
		// MultiKVMap<String, SinaUser> allUser = getAllRTTUser(doc);
		MultiKVMap<String, SinaUser> allUser = getAllRTTUserNew(doc);
		allUser.sort();
		GraphMap graphMap = new GraphMap();
		SinaUser root = new SinaUser();
		// root里边是原文的东西
		root.setId(doc.getId());
		root.setName(doc.getScreenName());
		generateMap(graphMap, allUser, root, true, level, num);
		Iterator<Graph> list = graphMap.getGraph().iterator();
		List<Edge> st = new ArrayList<>();
		List<Node> node = new ArrayList<>();
		node.add(new Node(root.getId(), root.getName(), ""));
		List<Map> all = new ArrayList<>();
		while (list.hasNext()) {
			Graph graphList = list.next();
			st.add(graphList.getEdge());
			node.add(graphList.getNode());
		}
		all.add(MapUtil.putValue(new String[] { "links", "array" }, "links", st));
		all.add(MapUtil.putValue(new String[] { "node", "array" }, "node", node));

		TimingCachePool.put(cacheKey, all);
		return all;
	}
}
