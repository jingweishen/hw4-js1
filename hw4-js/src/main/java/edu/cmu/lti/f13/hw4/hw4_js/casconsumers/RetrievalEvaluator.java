package edu.cmu.lti.f13.hw4.hw4_js.casconsumers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import edu.cmu.lti.f13.hw4.hw4_js.utils.*;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.f13.hw4.hw4_js.typesystems.Document;
import edu.cmu.lti.f13.hw4.hw4_js.typesystems.Token;
import edu.cmu.lti.f13.hw4.hw4_js.typesystems.Document;


public class RetrievalEvaluator extends CasConsumer_ImplBase {

	/** query id number **/
	public ArrayList<Integer> qIdList;

	/** query and text relevant values **/
	public ArrayList<Integer> relList;

	//  global maplist 
	
	public ArrayList <Map<String,Integer>>mapList;
	  
  // global query list 
	public ArrayList<Map<String,Integer>>   queryList;
	
  //global sentenceList 
	public ArrayList<String> setList;
	
	public void initialize() throws ResourceInitializationException {

		qIdList = new ArrayList<Integer>();

		relList = new ArrayList<Integer>();
    
		queryList = new ArrayList<Map<String,Integer>>();
		
		mapList  =  new ArrayList <Map<String,Integer>>();
		
		setList = new ArrayList <String> ();
	}

	/**
	 * TODO :: 1. construct the global word dictionary 2. keep the word
	 * frequency for each sentence
	 */
	@Override
	public void processCas(CAS aCas) throws ResourceProcessException {

		JCas jcas;
		try {
			jcas =aCas.getJCas();
		} catch (CASException e) {
			throw new ResourceProcessException(e);
		}

		FSIterator it = jcas.getAnnotationIndex(Document.type).iterator();
	  int ID = 0;
		if (it.hasNext()) {
			Document doc = (Document) it.next();
      
			
      
      
			//Make sure that your previous annotators have populated this in CAS
			FSList fsTokenList = doc.getTokenList();
			
			ArrayList tokenArray = Utils.fromFSListToCollection(fsTokenList, Token.class);
			Iterator iter = tokenArray.iterator();
			
		
			iter = tokenArray.iterator();
      
			Map <String,Integer> map = new HashMap<String,Integer>();
      while(iter.hasNext()){
        Token token = (Token)iter.next();
        String key = token.getText();
        int value  = token.getFrequency();
        map.put(key, value);
        
      }
			//ArrayList<Token>tokenList=Utils.fromFSListToCollection(fsTokenList, Token.class);
			int qid = doc.getQueryID();
			int rel = doc.getRelevanceValue();
								
			qIdList.add(doc.getQueryID());
			relList.add(doc.getRelevanceValue());
			mapList.add(map);
			if (rel == 99){
			  queryList.add(map);
			  setList.add(doc.getText());
			}
				
			//Do something useful here

		}

	}

	/**
	 * TODO 1. Compute Cosine Similarity and rank the retrieved sentences 2.
	 * Compute the MRR metric
	 */
	@Override
	public void collectionProcessComplete(ProcessTrace arg0)
			throws ResourceProcessException, IOException {

		super.collectionProcessComplete(arg0);
		// initialize
	  double trScore = 0.0;
		ArrayList <Double> scoreList = new ArrayList<Double>();
		ArrayList <Integer> rankList  = new ArrayList<Integer>();
		// TODO :: compute the cosine similarity measure
		
		Iterator iterQ ;
    Iterator iterR ;
    Iterator iterM ;
    Iterator iterY = queryList.iterator();
		Iterator iterS = setList.iterator();
    int ID = 0;
		while( iterY.hasNext()){
		  Map <String,Integer> queryVector = (Map <String,Integer>) iterY.next();
		  String sentence  = (String) iterS.next();
		  scoreList.clear();
      ID ++;
      System.out.println(ID);
      iterQ = qIdList.iterator();
      iterR = relList.iterator();
      iterM = mapList.iterator();
      
		  while(iterQ.hasNext() && iterR.hasNext() && iterM.hasNext()){
		    		  
		    int  qid = (Integer) iterQ.next();
		    int  rel = (Integer) iterR.next();
		    Map <String,Integer> docVector = (Map<String,Integer>) iterM.next();
		    if (qid== ID && rel <99){
	//	      double score = computeCosineSimilarity(queryVector,docVector);
		      double score = computeDiceCoefficient(queryVector,docVector);
		      System.out.println("qid="+ qid+ "rel" + rel +" score " +score);
		      scoreList.add(score);
		      if (rel == 1){
		        trScore = score;
		      }
		    }
		    	  
		  }
		  
		  Collections.sort(scoreList);
		  Collections.reverse(scoreList);
		  int rank = scoreList.indexOf(trScore) + 1;
		  System.out.println("score: " + trScore + " rank= " + rank + " rel = 1" + " qid=" + ID + " " + sentence);
		  rankList.add(rank);
		}
		
		
		// TODO :: compute the metric:: mean reciprocal rank
		Iterator itRank = rankList.iterator();
		double mrr = 0.0;
		int size = 0;
    while (itRank.hasNext()){
      size ++;
      int rank = (Integer) itRank.next();
      mrr += 1/rank;
      
    }
    mrr = mrr/size;
		System.out.println(" (MRR) Mean Reciprocal Rank ::" + mrr);
	}
    
	/**
	 * 
	 * @return cosine_similarity
	 */
	
	private double computeDiceCoefficient(Map<String, Integer> queryVector, Map <String,Integer> docVector){
	  double coeff = 0.0;
	  
	  Iterator <String> iter1 = queryVector.keySet().iterator();
    Iterator <String> iter2 = docVector.keySet().iterator();
    int length1 = 0;
    int length2 = 0;
    double enumerator =0;
    
    //get the length of docVector 
    while (iter2.hasNext()){
      length2 ++;
      String key = iter2.next();
    }
    
    //get the length of queryVector and calculate the enumerator of similarity
    while (iter1.hasNext()){
      length1 ++;
      String key = iter1.next();
      int value1 = queryVector.get(key);
      if (docVector.containsKey(key)){
        enumerator += 1;
      }
      
    }
    
    coeff = enumerator /(length1+length2-enumerator);

    return coeff;
	  
	}
	private double computeCosineSimilarity(Map<String, Integer> queryVector,
			Map<String, Integer> docVector) {
		double cosine_similarity=0.0;
		        
		// TODO :: compute cosine similarity between two sentences
		Iterator <String> iter1 = queryVector.keySet().iterator();
		Iterator <String> iter2 = docVector.keySet().iterator();
		int length1 = 0;
		int length2 = 0;
		int enumerator =0;
		
		//get the length of docVector 
		while (iter2.hasNext()){
		  length2 ++;
		  String key = iter2.next();
		}
		
		//get the length of queryVector and calculate the enumerator of similarity
		while (iter1.hasNext()){
		  length1 ++;
		  String key = iter1.next();
		  int value1 = queryVector.get(key);
		  if (docVector.containsKey(key)){
		    int value2 = docVector.get(key);
		    enumerator += (value1) * (value2);
		  }
		  
		}
		cosine_similarity = enumerator / Math.sqrt(length1 * length2 ); 
		

		return cosine_similarity;
	}

	/**
	 * 
	 * @return mrr
	 */
	private double compute_mrr() {
		double metric_mrr=0.0;

		// TODO :: compute Mean Reciprocal Rank (MRR) of the text collection
		
		return metric_mrr;
	}

}
