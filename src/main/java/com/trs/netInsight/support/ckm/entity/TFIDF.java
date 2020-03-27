/*
 * Project: netInsight
 * 
 * File Created at 2018年3月6日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.support.ckm.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.base.Functions;
import com.google.common.collect.Ordering;
import com.trs.netInsight.support.ckm.IFeaturesExtractor;
import com.trs.netInsight.util.MathUtil;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Desc TF-IDF 加权(基于term frequency–inverse document frequency)
 * @author yan.changjiang
 * @date 2018年3月6日 下午5:37:02
 * @version 
 */
@Data
@NoArgsConstructor
public class TFIDF implements IFeaturesExtractor{
	
	/**
	 * 语料数
	 */
	private Integer corpusSize;
	
	/**
	 * 逆向文档频率
	 */
	private Map<String, Double> termsInverseDocumentFrequencies;
	
	public TFIDF(List<TextInstance> documents, int featureSize) {
		this.init(documents, featureSize);
	}
	
	/**
	 * 初始化
	 * @param docs
	 * @param featureSize
	 */
	private void init(List<TextInstance> docs, int featureSize){
		Vocabulary vocabulary = new Vocabulary();
		for (TextInstance document : docs) {
			vocabulary.addAll(document.tokens);
		}
		vocabulary.limitWords(featureSize);

		// Calculates idfs
		this.corpusSize = docs.size();
		this.termsInverseDocumentFrequencies = new HashMap<String, Double>(vocabulary.wordCount());
		for (String term : vocabulary) {
			double idf = this.idf(term, docs);
			this.termsInverseDocumentFrequencies.put(term, idf);
		}
	}
	
	
	private double tf(String term, List<String> documentTerms) {
		double tf = 0.0;

		for (String documentTerm : documentTerms) {
			if (documentTerm.equals(term)) {
				tf++;
			}
		}

		return tf;
	}
	
	private double idf(String term, List<TextInstance> documents) {
		// number of documents where term appears
		double d = 0.0;
		for (TextInstance document : documents) {
			if (document.tokens.contains(term)) {
				d++;
			}
		}

		return Math.log(this.corpusSize / (1 + d));
	}
	
	private double tfIdf(String term, List<String> documentTerms) {
		double idf = this.termsInverseDocumentFrequencies.containsKey(term) ? 
				this.termsInverseDocumentFrequencies.get(term) : Math.log(this.corpusSize);
		double tf = this.tf(term, documentTerms);
		return tf * idf;
	}
	

	@Override
	public double[] extractFeatures(List<String> documentWords) {
		double[] features = new double[this.termsInverseDocumentFrequencies.size()];

		int i = 0;
		for (String term : this.termsInverseDocumentFrequencies.keySet()) {
			features[i] = this.tfIdf(term, documentWords);
			i++;
		}

		return MathUtil.normalize(features);
	}


}

/**
 * 词汇内部类
 *
 * Create by yan.changjiang on 2018年3月6日
 */
class Vocabulary implements Iterable<String>, Serializable {

	private static final long serialVersionUID = 7827671824674205961L;

	private TreeMap<String, Integer> wordCounts = new ValueComparableMap<String, Integer>(Ordering.natural().reverse());

	private Integer size = 0;

	public Vocabulary() {
	}

	public Vocabulary(List<String> words) {
		this.addAll(words);
	}

	public void add(String word) {
		Integer actualCount = this.wordCounts.get(word);
		if (actualCount == null) {
			actualCount = 1;
		} else {
			actualCount++;
		}
		this.wordCounts.put(word, actualCount);
		this.size++;
	}

	public void addAll(List<String> words) {
		for (String word : words) {
			this.add(word);
		}
	}

	public void limitWords(Integer maxWords) {
		String lessFrequentWord;
		Integer lowestCount;
		while (this.wordCount() > maxWords) {
			lessFrequentWord = this.wordCounts.lastKey();
			lowestCount = this.wordCounts.remove(lessFrequentWord);
			this.size -= lowestCount;
		}
	}

	public Integer count(String word) {
		Integer actualCount = this.wordCounts.get(word);
		if (actualCount == null) {
			actualCount = 0;
		}
		return actualCount;
	}

	public Double frequency(String word) {
		return this.count(word).doubleValue() / this.size.doubleValue();
	}

	public Boolean contains(String word) {
		return this.wordCounts.containsKey(word);
	}

	public Integer wordCount() {
		return this.wordCounts.size();
	}

	public Integer totalCount() {
		return this.size;
	}

	@Override
	public Iterator<String> iterator() {
		return this.wordCounts.keySet().iterator();
	}

	public Set<String> wordSet() {
		return this.wordCounts.keySet();
	}

	@Override
	public String toString() {
		return "Vocabulary [size=" + size + ", wordCounts=" + wordCounts + "]";
	}

	/**
	 * <pre>
	 * See <a href=http://stackoverflow.com/questions/109383/how-to-sort-a-mapkey-value-on-the-values-in-java/1283722#comment14899161_1283722>how-to-sort-a-mapkey-value-on-the-values-in-java</a>
	 * </pre>
	 * 
	 * @param <K>
	 * @param <V>
	 */
	private static class ValueComparableMap<K extends Comparable<K>, V> extends TreeMap<K, V> {

		private static final long serialVersionUID = 1476556231893371136L;
		// A map for doing lookups on the keys for comparison so we don't get
		// infinite loops
		private final Map<K, V> valueMap;

		ValueComparableMap(final Ordering<? super V> partialValueOrdering) {
			this(partialValueOrdering, new HashMap<K, V>());
		}

		private ValueComparableMap(Ordering<? super V> partialValueOrdering, HashMap<K, V> valueMap) {
			super(partialValueOrdering // Apply the value ordering
					.onResultOf(Functions.forMap(valueMap)) // On the result of
															// getting the value
															// for the key from
															// the map
					.compound(Ordering.natural())); // as well as ensuring that
													// the keys don't get
													// clobbered
			this.valueMap = valueMap;
		}

		@Override
		public V get(Object key) {
			return this.valueMap.get(key);
		}

		@Override
		public boolean containsKey(Object key) {
			return this.valueMap.containsKey(key);
		}

		public V put(K k, V v) {
			if (valueMap.containsKey(k)) {
				// remove the key in the sorted set before adding the key again
				super.remove(k);
			}
			valueMap.put(k, v); // To get "real" unsorted values for the
								// comparator
			return super.put(k, v); // Put it in value order
		}

		@Override
		public V remove(Object key) {
			super.remove(key);
			return this.valueMap.remove(key);
		}
	}

}


/**
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年3月6日 yan.changjiang creat
 */