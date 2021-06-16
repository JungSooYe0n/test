/*
 * Project: netInsight
 *
 * File Created at 2018年3月2日
 *
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.support.ckm.impl;

import com.trs.ckm.rulecat.RuleCATResultRR;
import com.trs.ckm.soap.*;
import com.trs.netInsight.support.ckm.ICkmService;
import com.trs.netInsight.support.ckm.entity.*;
import com.trs.netInsight.support.ckm.enums.PloType;
import com.trs.netInsight.support.ckm.util.CkmAnalysisUtil;
import com.trs.netInsight.support.fts.entity.FtsDocument;
import com.trs.netInsight.support.fts.entity.FtsDocumentStatus;
import com.trs.netInsight.support.fts.entity.FtsDocumentWeChat;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.StringUtil;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.trees.international.pennchinese.ChineseTreebankLanguagePack;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

/**
 * @Desc CKM服务实现类
 * @author changjiang
 * @date 2018年3月2日 下午4:50:30
 * @version
 */
@Service
public class CkmServiceImpl implements ICkmService {

	@Value("${support.ckm.host}")
	private String host;

	@Value("${support.ckm.username}")
	private String username;

	@Value("${support.ckm.password}")
	private String password;


	public static final String ANGER = "愤怒";
	public static final String DISGUST = "厌恶";
	public static final String FEAR = "恐惧";
	public static final String HAPPY = "快乐";
	public static final String SADNESS = "悲伤";
	public static final String AMAZED = "惊讶";
//	public static final String modelPath = "D:/workspace/trs/model/chineseFactored.ser";
	public static final String modelPath = "/home/trs/data/trsNetInsight/model/chineseFactored.ser";
	// "/home/trs/data/trsNetInsight/model/chineseFactored.ser";
	// "D:/workspace/trs/model/chineseFactored.ser";
	private static LexicalizedParser lp;
	private static GrammaticalStructureFactory gsf;
    static
    {
        //模型
        String models = modelPath;
        lp = LexicalizedParser.loadModel(models);
        //汉语R
        TreebankLanguagePack tlp = new ChineseTreebankLanguagePack();
        gsf = tlp.grammaticalStructureFactory();
    }

	/**
	 * 获取CKM客户端
	 *
	 * @return
	 */
	public TrsCkmSoapClient getClient() {
		TrsCkmSoapClient getClient = new TrsCkmSoapClient(host, username, password);
		return getClient;
	}

	/**
	 * 主谓宾结果与ckm分词整合
	 * @param content
	 * @return
	 */
	@Override
	public List<SegWord> SegMakeWord(String content){
	    List<SegDictWord> wordList1 = SegText(content,0);
        List<SegDictWord> wordList2 =  SegText(getMainPart(content).toString(),0);
        List<SegWord> wordList = new ArrayList<>();
        for (int i = 0; i < wordList1.size(); i++) {
            boolean isMain = false;
            for (int j = 0; j < wordList2.size(); j++) {
                if (wordList1.get(i).getword().equals(wordList2.get(j).getword())){
                    isMain = true;
                    break;
                }
            }
            wordList.add(new SegWord(wordList1.get(i).getword(),wordList1.get(i).getcate(),isMain));
        }
        for (int i = 0; i < wordList.size(); i++) {
            System.out.println("结果：" + i + ": " + wordList.get(i).getWord()+wordList.get(i).isMain());
        }
        return wordList;
    }

	/**
	 * ckm分词以及优化
	 * @param sContent
	 * @param iOption 默认为0
	 * @return
	 */
	public List<SegDictWord> SegText(String sContent,int iOption){
		if (StringUtil.isNotEmpty(sContent)) {
			TrsCkmSoapClient getClient = this.getClient();
			try {
				SegDictWord[] segResult = getClient.SegText(sContent, 0);
				List<SegDictWord> result = new ArrayList<>();
				if (segResult != null) {
					int n = 0;//作为填入新词组就不能重复填入的判断标志
					for (int i = 0; i < segResult.length; i++) {
						System.out.println("分词：" + i + ": " + segResult[i].getword() + " : " + segResult[i].getcate());

						if (i > n || n == 0) {

							if (segResult[i].getcate().contains("nr")) {
								if (segResult.length > (i + 2)) {
									if (segResult[i + 2].getcate().equals("n") || segResult[i + 2].getcate().equals("v") || segResult[i + 2].getcate().contains("nr") || segResult[i + 2].getcate().equals("t") || segResult[i + 2].getcate().equals("w") || segResult[i + 2].getword().length() >= 2) {
										result.add(new SegDictWord(segResult[i].getword() + segResult[i + 1].getword(), "n"));
										n = i + 1;
									} else {
                                            result.add(new SegDictWord(segResult[i].getword() + segResult[i + 1].getword() + segResult[i + 2].getword(), "n"));
                                            n = i + 2;
									}
								}
								if (segResult.length == (i + 2)) {
									//下一个词非 动词名词姓名
									if (!(segResult[i + 1].getcate().equals("n") || segResult[i + 1].getcate().equals("v") || segResult[i + 1].getcate().contains("nr") || segResult[i + 1].getcate().equals("w"))) {
										result.add(new SegDictWord(segResult[i].getword() + segResult[i + 1].getword(), "n"));
										n = i + 1;
									}

								}
							}else if (segResult[i].getcate().contains("Ng") && !segResult[i].getcate().contains("p") && !segResult[i].getcate().contains("a")){
                                    if (segResult.length > (i + 2)) {
                                        if (segResult[i + 2].getcate().equals("n") || segResult[i + 2].getcate().equals("v") || segResult[i + 2].getcate().contains("nr") || segResult[i + 2].getcate().equals("t") || segResult[i + 2].getcate().equals("w") || segResult[i + 2].getword().length() >= 2) {
                                            result.add(new SegDictWord(segResult[i].getword() + segResult[i + 1].getword(), "n"));
                                            n = i + 1;
                                        } else {
                                                result.add(new SegDictWord(segResult[i].getword() + segResult[i + 1].getword() + segResult[i + 2].getword(), "n"));
                                                n = i + 2;
                                        }
                                    }
                                    if (segResult.length == (i + 2)) {
                                        //下一个词非 动词名词姓名
                                        if (!(segResult[i + 1].getcate().equals("n") || segResult[i + 1].getcate().equals("v") || segResult[i + 1].getcate().contains("nr") || segResult[i + 1].getcate().equals("w"))) {
                                            result.add(new SegDictWord(segResult[i].getword() + segResult[i + 1].getword(), "n"));
                                            n = i + 1;
                                        }

                                    }
                            }else if (segResult[i].getcate().contains("n") || segResult[i].getcate().equals("v") || segResult[i].getcate().equals("ns") || segResult[i].getcate().equals("nt") || segResult[i].getcate().equals("nz") || segResult[i].getcate().equals("t") || segResult[i].getcate().equals("a") || segResult[i].getcate().equals("i") || segResult[i].getcate().equals("l") || segResult[i].getcate().equals("b")) {
								if(!(i != 0 && segResult.length > (i + 1)&& segResult[i].getcate().contains("p") && segResult[i-1].getcate().contains("n") && segResult[i+1].getcate().contains("n"))){
									//排除掉 一些既是n 又是p 的连词  比如：“和”  把前后都是名词的连词排除
                                    result.add(new SegDictWord(segResult[i].getword(), segResult[i].getcate()));
									continue;
								}

							}else if(segResult[i].getcate().contains("m")){
								if(segResult.length > (i + 5) && segResult[i+2].getcate().contains("m") && segResult[i+4].getcate().contains("m") && (segResult[i+1].getword().contains("年") || segResult[i+3].getword().contains("年"))){
									//年月日的情况
									n = i + 5;
									result.add(new SegDictWord(segResult[i].getword() + segResult[i + 1].getword()+segResult[i + 2].getword()+segResult[i + 3].getword()+segResult[i + 4].getword()+segResult[i + 5].getword(), "t"));
								}else if (segResult.length > (i + 3) && segResult[i+2].getcate().contains("m") && (segResult[i+1].getword().contains("月") || segResult[i+3].getword().contains("月"))){
									//年月
									n = i + 3;
									result.add(new SegDictWord(segResult[i].getword() + segResult[i + 1].getword()+segResult[i + 2].getword()+segResult[i + 3].getword(),"t"));
								}else if (segResult.length > (i + 2) && (segResult[i+1].getcate().contains("m") || segResult[i+1].getcate().contains("q")) && segResult[i+2].getcate().contains("q")){
									//对数词 后跟数词 或者 直接跟量词的情况处理  直接联合起来 例如：第一 场   三 百万  七 千万 袋
									result.add(new SegDictWord(segResult[i].getword() + segResult[i + 1].getword() + segResult[i + 2].getword(), "q"));
									n = i + 2;
								}else if (segResult.length > (i + 1) && (segResult[i+1].getcate().contains("m") || segResult[i+1].getcate().contains("q") || segResult[i+1].getcate().contains("n"))){
									result.add(new SegDictWord(segResult[i].getword() + segResult[i + 1].getword(), "m"));
									n = i + 1;
								}else {
										result.add(new SegDictWord(segResult[i].getword(), segResult[i].getcate()));
								}
							} else if (segResult[i].getcate().equals("w")){

							}else {
								if (segResult.length > (i + 1) && segResult[i].getword().length() == segResult[i + 1].getword().length() && segResult[i].getcate().equals(segResult[i + 1].getcate())) {
									result.add(new SegDictWord(segResult[i].getword() + segResult[i + 1].getword(), "n"));
									n = i + 1;
								}
							}
						}
					}
					return result;
				}
			} catch (CkmSoapException e) {
				e.printStackTrace();
			}
		}
	return null;
}
	/**
	 * 获取句子的主谓宾
	 *
	 * @param sentence 问题
	 * @return 问题结构
	 */
	public MainPart getMainPart(String sentence)
	{
		// 去掉不可见字符
		sentence = sentence.replace("\\s+", "");
		// 分词，用空格隔开
		List<edu.stanford.nlp.ling.Word> wordList = seg(sentence);
		return getMainPart(wordList);
	}
	/**
	 * 分词
	 *
	 * @param sentence 句子
	 * @return 分词结果
	 */
	private List<edu.stanford.nlp.ling.Word> seg(String sentence)
	{
		//分词
		List<edu.stanford.nlp.ling.Word> wordList = new LinkedList<>();
//		List<Term> terms = HanLP.segment(sentence);
		List<SegDictWord> wordListseg = SegMainText(sentence,0);
		StringBuffer sbLogInfo = new StringBuffer();
        for (SegDictWord tword : wordListseg) {
            edu.stanford.nlp.ling.Word word = new edu.stanford.nlp.ling.Word(tword.getword());
            wordList.add(word);
            sbLogInfo.append(word);
            sbLogInfo.append(' ');
        }
//		for (Term term : terms)
//		{
//			edu.stanford.nlp.ling.Word word = new edu.stanford.nlp.ling.Word(term.word);
////			wordList.add(word);
//			sbLogInfo.append(word);
//			sbLogInfo.append(' ');
//		}
		System.out.printf("分词结果为：" + sbLogInfo);
		return wordList;
	}
	/**
	 * 获取句子的主谓宾
	 *
	 * @param words    HashWord列表
	 * @return 问题结构
	 */
	public static MainPart getMainPart(List<edu.stanford.nlp.ling.Word> words)
	{
		MainPart mainPart = new MainPart();
		if (words == null || words.size() == 0) return mainPart;
		Tree tree = lp.apply(words);
		// 根据整个句子的语法类型来采用不同的策略提取主干
		switch (tree.firstChild().label().toString())
		{
			case "NP":
				// 名词短语，认为只有主语，将所有短NP拼起来作为主语即可
				mainPart = getNPPhraseMainPart(tree);
				break;
			default:
				GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
				Collection<TypedDependency> tdls = gs.typedDependenciesCCprocessed(true);
				TreeGraphNode rootNode = getRootNode(tdls);
				if (rootNode == null)
				{
					return getNPPhraseMainPart(tree);
				}
				mainPart = new MainPart(rootNode);
				for (TypedDependency td : tdls)
				{
					// 依存关系的出发节点，依存关系，以及结束节点
					TreeGraphNode gov = td.gov();
					GrammaticalRelation reln = td.reln();
					String shortName = reln.getShortName();
					TreeGraphNode dep = td.dep();
					if (gov == rootNode)
					{
						switch (shortName)
						{
							case "nsubjpass":
							case "dobj":
							case "attr":
								mainPart.object = dep;
								break;
							case "nsubj":
							case "top":
								mainPart.subject = dep;
								break;
						}
					}
					if (mainPart.object != null && mainPart.subject != null)
					{
						break;
					}
				}
				// 尝试合并主语和谓语中的名词性短语
				combineNN(tdls, mainPart.subject);
				combineNN(tdls, mainPart.object);
				if (!mainPart.isDone()) mainPart.done();
		}

		return mainPart;
	}
	private static TreeGraphNode getRootNode(Collection<TypedDependency> tdls)
	{
		for (TypedDependency td : tdls)
		{
			if (td.reln() == GrammaticalRelation.ROOT)
			{
				return td.dep();
			}
		}

		return null;
	}

	/**
	 * 合并名词性短语为一个节点
	 * @param tdls 依存关系集合
	 * @param target 目标节点
	 */
	private static void combineNN(Collection<TypedDependency> tdls, TreeGraphNode target)
	{
		if (target == null) return;
		for (TypedDependency td : tdls)
		{
			// 依存关系的出发节点，依存关系，以及结束节点
			TreeGraphNode gov = td.gov();
			GrammaticalRelation reln = td.reln();
			String shortName = reln.getShortName();
			TreeGraphNode dep = td.dep();
			if (gov == target)
			{
				switch (shortName)
				{
					case "nn":
						target.setValue(dep.toString("value") + target.value());
						return;
				}
			}
		}
	}
	private static MainPart getNPPhraseMainPart(Tree tree)
	{
		MainPart mainPart = new MainPart();
		StringBuilder sbResult = new StringBuilder();
		List<String> phraseList = getPhraseList("NP", tree);
		for (String phrase : phraseList)
		{
			sbResult.append(phrase);
		}
		mainPart.result = sbResult.toString();
		return mainPart;
	}
	private static List<String> getPhraseList(String type, Tree tree)
	{
		List<String> phraseList = new LinkedList<String>();
		for (Tree subtree : tree)
		{
			if(subtree.isPrePreTerminal() && subtree.label().value().equals(type))
			{
				StringBuilder sbResult = new StringBuilder();
				for (Tree leaf : subtree.getLeaves())
				{
					sbResult.append(leaf.value());
				}
				phraseList.add(sbResult.toString());
			}
		}
		return phraseList;
	}
	@Override
	public Map<String, Integer> statisticsEntity(String words, int topN, int entityType) throws CkmSoapException {
		Map<String, Integer> sameType = new LinkedHashMap<String, Integer>();
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		TrsCkmSoapClient getClient = this.getClient();
		PLOResult[] results = getClient.PLOText("<![CDATA[" + words + "]]>", 0xFFFF);
		if (results != null) {
			for (PLOResult result : results) {
				if (result.gettype() == entityType) {
					if (!sameType.containsKey(result.getword())) {
						sameType.put(result.getword(), 1);
					} else {
						int num = sameType.get(result.getword());
						sameType.remove(result.getword());
						sameType.put(result.getword(), num + 1);
					}
				}
			}
		}

		List<Entry<String, Integer>> entryList = new ArrayList<Entry<String, Integer>>(sameType.entrySet());
		Collections.sort(entryList, new Comparator<Map.Entry<String, Integer>>() {
			@Override
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return (o2.getValue() - o1.getValue());
			}
		});
		for (int i = 0; i < entryList.size() && i < topN; i++) {
			Entry<String, Integer> entry = entryList.get(i);
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	@Override
	public List<CluClsInfo> cluster(List<FtsDocument> docs, int clusterType) throws CkmSoapException {

		if (docs == null || docs.isEmpty()) {
			return Collections.emptyList();
		}

		TrsCkmSoapClient client = this.getClient();

		// 转换数据类型
		List<CluDocInfo> cluInfos = new ArrayList<CluDocInfo>(docs.size());
		for (FtsDocument doc : docs) {
			if (doc == null) {
				continue;
			}
			CluDocInfo info = new CluDocInfo();
			if (clusterType == 1) {
				info.setcontent(doc.getTitle());
			} else if (clusterType == 2) {
				info.setcontent(doc.getAbstracts());
			} else {
				info.setcontent(doc.getContent());
			}
			info.setdocdate(DateUtil.format2String(doc.getUrlTime(), DateUtil.yyyyMMdd3));
			info.setdocid(doc.getSid());
			cluInfos.add(info);
		}

		// 聚类参数
		CluParam param = new CluParam();
		param.setdocsimv(0);
		param.setclusimv(0);
		param.setminnum(1);
		param.setmincls(1);
		param.setmaxcls(docs.size());

		CluClsGraph result = client.ClusterDocGraph(cluInfos.toArray(new CluDocInfo[] {}), param, 500, 500, 4096);
		CluClsInfo[] clsInfos = result.getclslist();
		return Arrays.asList(clsInfos);
	}

	@Override
	public List<CluClsInfo> clusterStatus(List<FtsDocumentStatus> docs, int clusterType) throws CkmSoapException {
		if (docs == null || docs.isEmpty()) {
			return Collections.emptyList();
		}

		TrsCkmSoapClient client = this.getClient();

		// 转换数据类型
		List<CluDocInfo> cluInfos = new ArrayList<CluDocInfo>(docs.size());
		for (FtsDocumentStatus doc : docs) {
			if (doc == null) {
				continue;
			}
			CluDocInfo info = new CluDocInfo();
			info.setcontent(doc.getStatusContent());
			info.setdocdate(DateUtil.format2String(doc.getCreatedAt(), DateUtil.yyyyMMdd3));
			info.setdocid(doc.getSid());
			cluInfos.add(info);
		}

		// 聚类参数
		CluParam param = new CluParam();
		param.setdocsimv(0);
		param.setclusimv(0);
		param.setminnum(1);
		param.setmincls(1);
		param.setmaxcls(docs.size());

		CluClsGraph result = client.ClusterDocGraph(cluInfos.toArray(new CluDocInfo[] {}), param, 500, 500, 4096);
		CluClsInfo[] clsInfos = result.getclslist();
		/*
		 * for (CluClsInfo clsInfo : clsInfos) { clsInfo. }
		 */
		return Arrays.asList(clsInfos);
	}

	@Override
	public List<CluClsInfo> clusterWeChat(List<FtsDocumentWeChat> docs, int clusterType) throws CkmSoapException {
		if (docs == null || docs.isEmpty()) {
			return Collections.emptyList();
		}

		TrsCkmSoapClient client = this.getClient();

		// 转换数据类型
		List<CluDocInfo> cluInfos = new ArrayList<CluDocInfo>(docs.size());
		for (FtsDocumentWeChat doc : docs) {
			if (doc == null) {
				continue;
			}
			CluDocInfo info = new CluDocInfo();
			if (clusterType == 1) {
				info.setcontent(doc.getUrlTitle());
			} else {
				info.setcontent(doc.getContent());
			}
			info.setdocdate(DateUtil.format2String(doc.getUrlTime(), DateUtil.yyyyMMdd3));
			info.setdocid(doc.getSid());
			cluInfos.add(info);
		}

		// 聚类参数
		CluParam param = new CluParam();
		param.setdocsimv(0);
		param.setclusimv(0);
		param.setminnum(1);
		param.setmincls(1);
		param.setmaxcls(docs.size());

		CluClsGraph result = client.ClusterDocGraph(cluInfos.toArray(new CluDocInfo[] {}), param, 500, 500, 4096);
		CluClsInfo[] clsInfos = result.getclslist();
		return Arrays.asList(clsInfos);
	}

	@Override
	public ClusterInfo[] clusterByKMeans(List<IdText> corpus, int featureSize, int k) throws CkmSoapException {

		if (corpus == null) {
			return null;
		}
		List<TextInstance> texts = new ArrayList<TextInstance>();
		for (IdText item : corpus) {

			List<String> tokens = new ArrayList<String>();
			for (Word w : segment(item.getText())) {
				tokens.add(w.getword());
			}
			TextInstance instance = new TextInstance(item.getId(), tokens);
			texts.add(instance);
		}
		double[][] data = new double[texts.size()][];
		TFIDF tf = new TFIDF(texts, featureSize);
		for (int i = 0; i < texts.size(); i++) {
			data[i] = tf.extractFeatures(texts.get(i).tokens);
		}
		KMeans cluster = new KMeans(data, k);
		return cluster.cluster();
	}

	@Override
	public Map<String, Set<String>> ploText(String content, int maxKeyword) throws CkmSoapException {
		if (StringUtils.isBlank(content)) {
			return null;
		}
		Map<String, Set<String>> result = new HashMap<>();
		Set<String> peoples = new HashSet<>();
		Set<String> areas = new HashSet<>();
		Set<String> units = new HashSet<>();
		Set<String> keywords = new HashSet<>();

		TrsCkmSoapClient client = this.getClient();

		// 处理实体词
		PLOResult[] results = client.PLOText("<![CDATA[" + content + "]]>", 1);
		if (results != null) {
			for (int i = 0; i < results.length; i++) {
				String word = results[i].getword();

				int type = results[i].gettype();
				if (type == 1001) {
					peoples.add(word);
				} else if (type == 1002) {
					areas.add(word);
				} else if (type == 1003) {
					units.add(word);
				}
			}
		}

		// 处理关键词,处理两遍,先根据摘要处理,如果不满足需求,继续从正文提取
		ABSResult resultAbs = client.ABSext(content, new ABSHold(maxKeyword, 10));
		String wordlistStr = resultAbs.getwordlist();
		if (StringUtils.isNotEmpty(wordlistStr)) {
			String[] keywordArray = wordlistStr.split(";");
			for (String word : keywordArray) {
				keywords.add(word);
			}
		}
		if (keywords.size() < 3) {
			SegDictWord[] segText = client.PLOSegText(content);
			if (segText != null) {
				for (SegDictWord segDictWord : segText) {
					String word = segDictWord.getword();
					if (word.length() < 2) {
						continue;
					}
					boolean ifinclude = false;
					for (String value : keywords) {
						if (value.indexOf(word) >= 0 || (word.indexOf(value) >= 0)) {
							ifinclude = true;
						}
						if (ifinclude) {
							continue;
						}
						keywords.add(word);
						if (keywords.size() >= maxKeyword) {
							break;
						}
					}
				}
			}
		}
		result.put("people", peoples);
		result.put("area", areas);
		result.put("unit", units);
		result.put("keyword", keywords);
		return result;
	}

	@Override
	public List<Word> segment(String content) throws CkmSoapException {
		if (StringUtils.isBlank(content)) {
			return Collections.emptyList();
		}

		TrsCkmSoapClient client = this.getClient();
		SegDictWord[] words = client.PLOSegText(content);
		List<Word> results = new ArrayList<>();

		for (SegDictWord word : words) {
			results.add(new Word(word));
		}
		words = null;
		return results;
	}

    /**
     * 主谓宾分词使用
     * @param sContent
     * @param iOption
     * @return
     */
    public List<SegDictWord> SegMainText(String sContent,int iOption){
        if (StringUtil.isNotEmpty(sContent)) {
            TrsCkmSoapClient getClient = this.getClient();
            try {
                SegDictWord[] segResult = getClient.SegText(sContent, 0);
                List<SegDictWord> result = new ArrayList<>();
                if (segResult != null) {
                    int n = 0;//作为填入新词组就不能重复填入的判断标志
                    for (int i = 0; i < segResult.length; i++) {
                        if (i > n || n == 0) {
                            if (segResult[i].getcate().contains("nr")) {
                                if (segResult.length > (i + 2)) {
                                    if (segResult[i + 2].getcate().equals("n") || segResult[i + 2].getcate().equals("v") || segResult[i + 2].getcate().contains("nr") || segResult[i + 2].getcate().equals("t") || segResult[i + 2].getcate().equals("w") || segResult[i + 2].getword().length() >= 2) {
                                        result.add(new SegDictWord(segResult[i].getword() + segResult[i + 1].getword(), "n"));
                                        n = i + 1;
                                    } else {
                                        result.add(new SegDictWord(segResult[i].getword() + segResult[i + 1].getword() + segResult[i + 2].getword(), "n"));
                                        n = i + 2;
                                    }
                                }
                                if (segResult.length == (i + 2)) {
                                    //下一个词非 动词名词姓名
                                    if (!(segResult[i + 1].getcate().equals("n") || segResult[i + 1].getcate().equals("v") || segResult[i + 1].getcate().contains("nr") || segResult[i + 1].getcate().equals("w"))) {
                                        result.add(new SegDictWord(segResult[i].getword() + segResult[i + 1].getword(), "n"));
                                        n = i + 1;
                                    }

                                }
                            }else if (segResult[i].getcate().contains("Ng") && !segResult[i].getcate().contains("p") && !segResult[i].getcate().contains("a")){
                                if (segResult.length > (i + 2)) {
                                    if (segResult[i + 2].getcate().equals("n") || segResult[i + 2].getcate().equals("v") || segResult[i + 2].getcate().contains("nr") || segResult[i + 2].getcate().equals("t") || segResult[i + 2].getcate().equals("w") || segResult[i + 2].getword().length() >= 2) {
                                        result.add(new SegDictWord(segResult[i].getword() + segResult[i + 1].getword(), "n"));
                                        n = i + 1;
                                    } else {
                                        result.add(new SegDictWord(segResult[i].getword() + segResult[i + 1].getword() + segResult[i + 2].getword(), "n"));
                                        n = i + 2;
                                    }
                                }
                                if (segResult.length == (i + 2)) {
                                    //下一个词非 动词名词姓名
                                    if (!(segResult[i + 1].getcate().equals("n") || segResult[i + 1].getcate().equals("v") || segResult[i + 1].getcate().contains("nr") || segResult[i + 1].getcate().equals("w"))) {
                                        result.add(new SegDictWord(segResult[i].getword() + segResult[i + 1].getword(), "n"));
                                        n = i + 1;
                                    }

                                }
                            }else if (segResult[i].getcate().contains("n") || segResult[i].getcate().equals("v") || segResult[i].getcate().equals("ns") || segResult[i].getcate().equals("nt") || segResult[i].getcate().equals("nz") || segResult[i].getcate().equals("t") || segResult[i].getcate().equals("a") || segResult[i].getcate().equals("i") || segResult[i].getcate().equals("l") || segResult[i].getcate().equals("b")) {
                                if(!(i != 0 && segResult.length > (i + 1)&& segResult[i].getcate().contains("p") && segResult[i-1].getcate().contains("n") && segResult[i+1].getcate().contains("n"))){
                                    //排除掉 一些既是n 又是p 的连词  比如：“和”  把前后都是名词的连词排除
                                    result.add(new SegDictWord(segResult[i].getword(), segResult[i].getcate()));
                                    continue;
                                }

                            }else if(segResult[i].getcate().contains("m")){
                                if(segResult.length > (i + 5) && segResult[i+2].getcate().contains("m") && segResult[i+4].getcate().contains("m") && (segResult[i+1].getword().contains("年") || segResult[i+3].getword().contains("年"))){
                                    //年月日的情况
                                    n = i + 5;
                                    result.add(new SegDictWord(segResult[i].getword() + segResult[i + 1].getword()+segResult[i + 2].getword()+segResult[i + 3].getword()+segResult[i + 4].getword()+segResult[i + 5].getword(), "t"));
                                }else if (segResult.length > (i + 3) && segResult[i+2].getcate().contains("m") && (segResult[i+1].getword().contains("月") || segResult[i+3].getword().contains("月"))){
                                    //年月
                                    n = i + 3;
                                    result.add(new SegDictWord(segResult[i].getword() + segResult[i + 1].getword()+segResult[i + 2].getword()+segResult[i + 3].getword(),"t"));
                                }else if (segResult.length > (i + 2) && (segResult[i+1].getcate().contains("m") || segResult[i+1].getcate().contains("q")) && segResult[i+2].getcate().contains("q")){
                                    //对数词 后跟数词 或者 直接跟量词的情况处理  直接联合起来 例如：第一 场   三 百万  七 千万 袋
                                    result.add(new SegDictWord(segResult[i].getword() + segResult[i + 1].getword() + segResult[i + 2].getword(), "q"));
                                    n = i + 2;
                                }else if (segResult.length > (i + 1) && (segResult[i+1].getcate().contains("m") || segResult[i+1].getcate().contains("q") || segResult[i+1].getcate().contains("n"))){
                                    result.add(new SegDictWord(segResult[i].getword() + segResult[i + 1].getword(), "m"));
                                    n = i + 1;
                                }else {
                                    result.add(new SegDictWord(segResult[i].getword(), segResult[i].getcate()));
                                }
                            } else {
                                if (segResult.length > (i + 1) && segResult[i].getword().length() == segResult[i + 1].getword().length() && segResult[i].getcate().equals(segResult[i + 1].getcate())) {
                                    result.add(new SegDictWord(segResult[i].getword() + segResult[i + 1].getword(), "n"));
                                    n = i + 1;
                                }else {
                                    result.add(new SegDictWord(segResult[i].getword(), segResult[i].getcate()));
                                }
                            }
                        }
                    }
                    return result;
                }
            } catch (CkmSoapException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
	@Override
	public List<AbsTheme> theme(String content, int topN) throws CkmSoapException {

		if (StringUtils.isBlank(content)) {
			return Collections.emptyList();
		}
		TrsCkmSoapClient client = this.getClient();
		AbsTheme[] result = client.GetAbsThemeList(content, topN);

		return Arrays.asList(result);
	}

	@Override
	public List<AbsTheme> theme(List<String> contents) throws CkmSoapException {
		if (contents != null) {
			StringBuffer sb = new StringBuffer();
			for (String word : contents) {
				sb.append(word).append("\n");
			}
			return this.theme(sb.toString(), contents.size() * 3);
		}
		return null;
	}

	@Override
	public List<List<AnalysisValue>> getSimDatas(Map<String, SimData> corpus, float ration) throws CkmSoapException {
		String modeName = CkmModeConst.HotMessage;
		boolean createSuccess = this.createModList(modeName, corpus);

		if (!createSuccess) {
			throw new CkmSoapException("create ckm mode error :" + modeName);
		}
		// 计算文本与文本之间的相似度
		List<List<AnalysisValue>> simDatas = this.getSimDatas(modeName, corpus, ration);
		// 删除模板
		this.deletMod(modeName);
		// 处理相似结果集
		simDatas = CkmAnalysisUtil.simDataGroup(simDatas);
		return simDatas;
	}

	@Override
	public Map<String, Integer> classify(String mod, String text) throws CkmSoapException {
		Map<String, Integer> map = new LinkedHashMap<String, Integer>();
		TrsCkmSoapClient client = this.getClient();
		CATRevDetail[] details = client.CATClassifyText(mod, text);
		if (details != null) {
			for (CATRevDetail detail : details) {
				String name = detail.getCATName();
				int value = (int) (detail.getv() * 10);
				map.put(name, value);
			}
		}
		return map;
	}

	/**
	 * 根据热点相似语料创建对应模板,如果已经存在该模板,直接返回true
	 *
	 * @param modeName
	 *            模板名称
	 * @param simData
	 *            语料
	 * @return
	 */
	private boolean createModList(String modeName, Map<String, SimData> simData) throws CkmSoapException {

		TrsCkmSoapClient client = this.getClient();
		// 获取该ckm服务下所有模板
		String[] modelList = client.getModelList(Constants.MODEL_TYPE_SIM_CURRENT);
		if (modelList != null) {
			for (String model : modelList) {
				if (model.equals(modeName)) {
					return true;
				}
			}
		}
		// 创建模板
		int simCreateModel = client.SimCreateModel(modeName);
		if (simCreateModel != 0) {
			throw new CkmSoapException("create ckm mode error:" + modeName);
		}
		for (Map.Entry<String, SimData> entry : simData.entrySet()) {
			SimData data = entry.getValue();
			// 添加相似度记录
			int index = client.SimUpdateIndex(modeName, data.getMsgId(), data.getContent());
			if (index != 0) {
				throw new CkmSoapException(
						"Add sim record error:" + modeName + "|" + data.getMsgId() + "|" + simData.size());
			}
		}
		return false;
	}

	/**
	 * 计算文本之前的相关度
	 *
	 * @param modeName
	 *            模板名称
	 * @param corpus
	 *            语料
	 * @param ratio
	 *            相似度阈值
	 * @return
	 * @throws CkmSoapException
	 */
	private List<List<AnalysisValue>> getSimDatas(String modeName, Map<String, SimData> corpus, float ratio)
			throws CkmSoapException {
		List<List<AnalysisValue>> simData = new ArrayList<List<AnalysisValue>>();

		TrsCkmSoapClient client = this.getClient();
		for (Map.Entry<String, SimData> entry : corpus.entrySet()) {
			SimData data = entry.getValue();
			String content = data.getContent();
			if (StringUtils.isBlank(content)) {
				continue;
			}
			RevHold revHold = new RevHold(0, ratio);
			RevDetail[] details = client.SimRetrieveByText(modeName, revHold, content);
			if (details != null) {
				List<AnalysisValue> list = new ArrayList<AnalysisValue>();
				for (RevDetail detail : details) {
					AnalysisValue bean = new AnalysisValue();
					bean.setMsgId(detail.getid());
					bean.setSimv(detail.getsimv());
					list.add(bean);
				}
				simData.add(list);
			}
		}

		return simData;
	}

	/**
	 * 根据模板名称删除模板
	 *
	 * @param modeName
	 * @return
	 * @throws CkmSoapException
	 */
	private boolean deletMod(String modeName) throws CkmSoapException {
		TrsCkmSoapClient client = this.getClient();
		int simDropModel = client.SimDropModel(modeName);
		if (simDropModel != 0) {
			return false;
		}
		return true;
	}

	@Override
	public String simMD5GenerateTheme(String content) throws CkmSoapException {
		TrsCkmSoapClient client = this.getClient();
		SimMD5Result _md5ret = client.SimMD5GenerateTheme(content);
		return _md5ret.getdigest();
	}

	@Override
	public Map<String, Set<String>> ploTextDetail(String content, int maxKeyword, String ploType)
			throws CkmSoapException {
		if (StringUtils.isBlank(content)) {
			return null;
		}
		Map<String, Set<String>> result = new HashMap<>();
		Set<String> peoples = new HashSet<>();
		Set<String> areas = new HashSet<>();
		Set<String> units = new HashSet<>();
		Set<String> keywords = new HashSet<>();
		int ploItem = 0;
		String typeUpperCase = ploType.toUpperCase();
		TrsCkmSoapClient client = this.getClient();

		switch (typeUpperCase){
			case "NAME_AREA_ORGANIZATION":
				ploItem = PloType.NAME_AREA_ORGANIZATION.getCode();
				break;
			case "NUMBER":
				ploItem = PloType.NUMBER.getCode();
				break;
			case "CASE":
				ploItem = PloType.CASE.getCode();
				break;
			case "HOUSE":
				ploItem = PloType.HOUSE.getCode();
				break;
			default:
				ploItem = PloType.ALL.getCode();
				break;
		}


//		if ("NAME_AREA_ORGANIZATION".equals(typeUpperCase)) {
//			// 抽取人名/地名/机构名
//			ploItem = PloType.NAME_AREA_ORGANIZATION.getCode();
//		} else if ("NUMBER".equals(typeUpperCase)) {
//			// 抽取数字——时间、MSN、email、QQ、车牌、护照号、身份证号、电话号码等有意义的数字信息
//			ploItem = PloType.NUMBER.getCode();
//		} else if ("CASE".equals(typeUpperCase)) {
//			// 抽取案件
//			ploItem = PloType.CASE.getCode();
//		} else if ("HOUSE".equals(typeUpperCase)) {
//			// 抽取房屋相关
//			ploItem = PloType.HOUSE.getCode();
//		} else {
//			// 抽取所有实体信息
//			ploItem = PloType.ALL.getCode();
//		}
		// 处理实体词
		PLOResult[] results = client.PLOText("<![CDATA[" + content + "]]>", ploItem);
		if (results != null) {
			for (int i = 0; i < results.length; i++) {
				String word = results[i].getword();

				int type = results[i].gettype();
				if (type == PloType.NAME.getCode()) {
					peoples.add(word);
				} else if (type == PloType.NETHERLANGS.getCode()) {
					areas.add(word);
				} else if (type == PloType.ORGANIZATION.getCode()) {
					units.add(word);
				}
			}
		}

		// 处理关键词,处理两遍,先根据摘要处理,如果不满足需求,继续从正文提取
		// 自动摘要 ---- ABSext(内容, new ABSHold(需要返回的摘要词个数, 限制返回的摘要词个数))
		ABSResult resultAbs = client.ABSext(content, new ABSHold(maxKeyword, 20));
		// 获取摘要词列表
		String wordlistStr = resultAbs.getwordlist();
		if (StringUtils.isNotEmpty(wordlistStr)) {
			String[] keywordArray = wordlistStr.split(";");
			for (String word : keywordArray) {
				keywords.add(word);
			}
		}
		if (keywords.size() < 3) {
			SegDictWord[] segText = client.PLOSegText(content);
			if (segText != null) {
				for (SegDictWord segDictWord : segText) {
					String word = segDictWord.getword();
					if (word.length() < 2) {
						continue;
					}
					boolean ifinclude = false;
					for (String value : keywords) {
						if (value.indexOf(word) >= 0 || (word.indexOf(value) >= 0)) {
							ifinclude = true;
						}
						if (ifinclude) {
							continue;
						}
						keywords.add(word);
						if (keywords.size() >= maxKeyword) {
							break;
						}
					}
				}
			}
		}
		result.put("people", peoples);
		result.put("area", areas);
		result.put("unit", units);
		result.put("keyword", keywords);
		return result;
	}

	@SuppressWarnings("unused")
	@Override
	public Map<String, Integer> emotionDivide(String content) throws CkmSoapException {
		//API要求固定字段
		String field = "正文" ;
		//模板名，可视情况而定
		String catModelName = "emotion2";
		TrsCkmSoapClient client = this.getClient();

		//最后装结果的map
		Map<String, Integer> afterCalculateMap = new HashMap<String, Integer>();
		//分词接口，该接口只分词，不做排重处理，故往后调用还可统计词频
		SegDictWord[] segText = client.PLOSegText(StringUtil.replaceImg(content));
		if(segText!= null && segText.length>0){
			List<String> wordList = new ArrayList<String>();
			for (SegDictWord segDictWord : segText) {
				//除去了所有标点符号
				String word = segDictWord.getword().replaceAll( "[\\p{P}+~$`^=|<>～｀＄＾＋＝｜＜＞￥×]" , "");
				if(StringUtil.isNotEmpty(word)){
					wordList.add(word);
				}
			}
//			StringBuilder builder = new StringBuilder();
			String builder = "";
			Iterator<String> iterator = wordList.iterator();
			while(iterator.hasNext()){
				builder += iterator.next();
			}

			RuleCATField[] ruleFields = new RuleCATField[1];
			ruleFields[0] = new RuleCATField(builder, field);
			if(StringUtil.isNotEmpty(builder)){
				RuleCATResultRR[] res = client.RuleCATClassifyTextRR(catModelName, ruleFields);
				if(res != null){
					int totalScore = 0;
					for(int i = 0; i< res.length; i++){
						totalScore += res[i].getMatchwordcount();
					}
					//初始化各项分数总和
					int angerScore = 0;
					int disgustScore = 0;
					int fearScore = 0;
					int happyScore = 0;
					int sadnessScore = 0;
					int amazedScore = 0;

					//初始化各项分数百分比
					int angerPercent = 0;
					int disgustPercent = 0;
					int fearPercent = 0;
					int happyPercent = 0;
					int sadnessPercent = 0;
					int amazedPercent = 0;

					for(int i = 0; i < res.length; i++){
//						System.out.println(res[i].getMatchword()+"\r");
//						System.out.println(res[i].getMatchwordcount()+"\r");
//						System.out.println(res[i].getLabel()+"\r");

						//获得各项情绪的独自分数并计算百分比
						String emotionType = res[i].getLabel();
						//分类别计算分数
						if(ANGER.equals(emotionType)){
							angerScore += res[i].getMatchwordcount();
							//想直接计算出百分比不可行，因为先要循环获得各项自己的分数相加，直接计算是拿不到各项总分的
//							angerPercent = new BigDecimal((float)res[i].getMatchwordcount()/totalScore).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
						}
						if(DISGUST.equals(emotionType)){
							disgustScore += res[i].getMatchwordcount();
						}
						if(FEAR.equals(emotionType)){
							fearScore += res[i].getMatchwordcount();
						}
						if(HAPPY.equals(emotionType)){
							happyScore += res[i].getMatchwordcount();
						}
						if(SADNESS.equals(emotionType)){
							sadnessScore += res[i].getMatchwordcount();
						}
						if(AMAZED.equals(emotionType)){
							amazedScore += res[i].getMatchwordcount();
						}

					}
					//throws ParseException?
					//因为返回结果res[i].getMatchwordcount()属于int类型，所以暂时不用BigDecimal来初始化相关的分数，所以此处的计算也没有用BigDecimal中的除法（.divide）等
					angerPercent = (int) new BigDecimal((float)angerScore/totalScore*5).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
					disgustPercent = (int) new BigDecimal((float)disgustScore/totalScore*5).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
					fearPercent = (int) new BigDecimal((float)fearScore/totalScore*5).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
					happyPercent = (int) new BigDecimal((float)happyScore/totalScore*5).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
					sadnessPercent = (int) new BigDecimal((float)sadnessScore/totalScore*5).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
					amazedPercent = (int) new BigDecimal((float)amazedScore/totalScore*5).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

					afterCalculateMap.put(ANGER, angerPercent);
					afterCalculateMap.put(DISGUST, disgustPercent);
					afterCalculateMap.put(FEAR, fearPercent);
					afterCalculateMap.put(HAPPY, happyPercent);
					afterCalculateMap.put(SADNESS, sadnessPercent);
					afterCalculateMap.put(AMAZED, amazedPercent);
					return afterCalculateMap;
				}
			}
		}

		return null;
	}

}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * <font color='red'>入坑需谨慎,且行且珍惜</font>
 * -------------------------------------------------------------------------
 * 2018年3月2日 changjiang creat
 */
