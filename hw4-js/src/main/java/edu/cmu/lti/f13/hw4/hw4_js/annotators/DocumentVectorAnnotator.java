package edu.cmu.lti.f13.hw4.hw4_js.annotators;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.cas.FSList;


import edu.cmu.lti.f13.hw4.hw4_js.utils.Utils;
import edu.cmu.lti.f13.hw4.hw4_js.typesystems.Document;
import edu.cmu.lti.f13.hw4.hw4_js.typesystems.Token;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;

public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {	  	  
	  		
	  FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
		if (iter.isValid()) {
			iter.moveToNext();
			Document doc = (Document) iter.get();
			
			try {
        createTermFreqVector(jcas, doc);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
		}

	}
	/**
	 * 
	 * @param jcas
	 * @param doc
	 */
	public  String stopWords() throws IOException{
    
    BufferedReader br=new BufferedReader(new FileReader("./src/main/resources/stopwords.txt"));
    String str="";
    String r=br.readLine();

    while(r!=null){

    str+=r;
    str+=" ";

    r=br.readLine();
    
       
  }
    
    return str;
 
}
	private void createTermFreqVector(JCas jcas, Document doc) throws IOException {

		String docText = doc.getText();
	  String stopwords = stopWords();       // the word string of stop words
		ArrayList array = new ArrayList();
		String already ="";
		
		String tags = "[ \\,\\.\\?]";
		String []results = docText.split(tags);
    
    for (int i = 0; i<results.length;i++){
        String word = results[i];
       // System.out.println(word);
        word = word.toLowerCase();
        if (stopwords.indexOf(word)<0 && already.indexOf(word)<0){
          
          int count = 0;
          
          for (int j = 0;j<results.length;j++){
            if (word.equals(results[j])){
              count ++;
            }
            already = already + word + " ";
            
          }
         
          Token annotation = new Token(jcas);
          annotation.setText(word);
          annotation.setFrequency(count);
          //System.out.println(word);
          //System.out.println(count);
          array.add(annotation);
        }
        
        Utils util = new Utils();
        
        doc.setTokenList(util.fromCollectionToFSList(jcas, array));
    }
    
   
    
		//TO DO: construct a vector of tokens and update the tokenList in CAS
		
	  
		

	}

}
