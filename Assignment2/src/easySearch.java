import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.math.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class easySearch {
	public static void main(String args[]) throws ParseException, IOException
	{
		HashMap<String,Float> doc_length=new HashMap<String,Float>();  
		HashMap<String,Float> score=new HashMap<String,Float>();  
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("index")));
		IndexSearcher searcher = new IndexSearcher(reader);
		// Get the preprocessed query terms
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		Scanner ss = new Scanner(System.in);
	    	System.out.print("Enter query: ");
	    	String s = ss.nextLine(); 
		Query query = parser.parse(s);
		Set<Term> queryTerms = new LinkedHashSet<Term>();
		searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);
		//for each term in a query
		for (Term t : queryTerms)
		{
			float sum=0;
			File f = new File("score"+t+".txt");
    			FileOutputStream fos = new FileOutputStream(f);
    			PrintWriter out1 = new PrintWriter(fos);
		    	//k(t)
		    	int df=reader.docFreq(new Term("TEXT", t.text()));
		    	//if not in that document is query term..next term
		    	if (df == 0) 
		    	{
		      	continue;
		    	}
		    	//N
		    	int totalno_docs=reader.maxDoc();
		    	ClassicSimilarity dSimi = new ClassicSimilarity();
		   	List<LeafReaderContext> leafContexts = reader.getContext().reader().leaves();
		    	for (int i = 0; i < leafContexts.size(); i++)
		    	{
				LeafReaderContext leafContext = leafContexts.get(i);
				int startDocNo = leafContext.docBase;
				int numberOfDoc = leafContext.reader().maxDoc();
				for (int docId = 0; docId < numberOfDoc; docId++) 
				{
					float normDocLeng = dSimi.decodeNormValue(leafContext.reader().getNormValues("TEXT").get(docId));
					float docLeng = 1 / (normDocLeng * normDocLeng);
					//length(doc)
					doc_length.put(searcher.doc(docId +startDocNo).get("DOCNO"),docLeng);
				}
				System.out.println();
				PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(),"TEXT", new BytesRef(t.text()));
				if (de != null) 
				{
					while ((de.nextDoc()) != PostingsEnum.NO_MORE_DOCS) 
					{
						//c(t,doc)
						String doc_id=searcher.doc(de.docID() +startDocNo).get("DOCNO");
						if(doc_length.keySet().contains(doc_id) )
						{
							//calculate tf idf
							sum=(float) ((de.freq())/(doc_length).get(doc_id)*Math.log(1+(float)(totalno_docs/df)));
			            			//for each query term put in file relevance score
							out1.write("for term- "+t+"\tdoc id- "+doc_id+"\trelevance score is- " +sum+"\n");
						}
						if(score.keySet().contains(doc_id))
				        	{    
				        		//if already present in score hashmap then add in score  
				            		score.put(doc_id, score.get(doc_id)+sum);
				        	}
				       		else
				       		{
				            		//create a new tuple with new score
				            		score.put(doc_id, sum);
				       		}
						
					}
				}
			}
			out1.flush();
		   	out1.close();
		    	out1.close();
		}
			
		//store in a file the relevance score for query
		File f = new File("score.txt");
		FileOutputStream fos = new FileOutputStream(f);
		PrintWriter out = new PrintWriter(fos);
		for (String key:score.keySet())
		{
			out.write(key+"\t"+score.get(key)+"\n");
		}
		out.flush();
	    	out.close();
	    	out.close();	
	}			
}

			
			
			
			
			
			
			
			
			
			
			
		
