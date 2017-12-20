import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
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
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class compareAlgorithms {
	static int flag=0;
	public static void main(String args[]) throws ParseException, IOException
	{	
		String fileAsString = null;
	    	try {
	    		//read the file and saving into a string
	    		InputStream is = new FileInputStream("topics.51-100"); 
	    		BufferedReader buf = new BufferedReader(new InputStreamReader(is)); 
	    		String line = buf.readLine(); 
	    		StringBuilder sb = new StringBuilder();
	    		while(line != null)
	    		{ 
	    			sb.append(line); 
	    			sb.append(" ");
	    			line = buf.readLine();
	    		}
	    		fileAsString = sb.toString();
	    		} 
	    catch (IOException e) {
	    }
		String[] array=fileAsString.split("<top>");
	    	forshortorlongquery(array,0);
	    	System.out.println("done with short query");
	    	forshortorlongquery(array,1);
	    	System.out.println("done with long query");
	}
	
	public static void forshortorlongquery(String[] array,int flag) throws IOException, ParseException
    	{
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("index")));
		IndexSearcher searcher = new IndexSearcher(reader);
    		//for short query of each model
    		if(flag==0)
    		{
    			File f = new File("DefaultShortQuery.txt");
    			FileOutputStream fos = new FileOutputStream(f);
    			PrintWriter pw = new PrintWriter(fos);
    			//for each <top> in file
    			for(int j=1;j<array.length;j++)
    			{
    				String id = null;
    				int rank=1;
    				String s=array[j];
    				String namepass1[]=null;
    				Pattern pattern1 = Pattern.compile("<num>(.*?)<dom>");	
    				Matcher matcher1 = pattern1.matcher(s);   
    				if (matcher1.find()) 
    				{
    					id = matcher1.group(1);
    					namepass1 = id.split(":");//taking only group 1
    				}
    				//take title of each top from the topics.51-100 as short query
    				Pattern pattern = Pattern.compile("<title>(.*?)<desc>");		
    				Matcher matcher = pattern.matcher(s);   
    				if (matcher.find()) 
    				{		
    					//extract title text
    					String tag_value = matcher.group(1);//taking only group 1
    					String namepass[] = tag_value.split(":"); 
    					//send to calculate relevance score
    					// Get the preprocessed query terms
					//use models to calculate relevance score.
    					searcher.setSimilarity(new ClassicSimilarity());//BM25Similarity() , DirichletSimilarity() , JelinekMercerSimilarity((float)0.7).
    					Analyzer analyzer = new StandardAnalyzer();
    					QueryParser parser = new QueryParser("TEXT", analyzer);
    					Query query = parser.parse(QueryParser.escape(namepass[1]));
    					TopDocs topDocs = searcher.search(query, 1000);
    					//get relevance score
    					ScoreDoc[] docs = topDocs.scoreDocs;
    					for (int i = 0; i < docs.length; i++) 
    					{
    						Document doc = searcher.doc(docs[i].doc);
     						pw.write((namepass1[1]+"\tQ"+(j-1)+"\t"+ doc.get("DOCNO") + "\t"+rank +"\t"+ docs[i].score+"\t"+"run-"+j+"\n"));
     						rank++;
    					}
    				}
    			}
    			pw.flush();
	   		fos.close();
	    		pw.close();
    		}
		//for long query of each model
    		else 
    		{
    			File f = new File("DefaultLongQuery.txt");
    	    		FileOutputStream fos = new FileOutputStream(f);
    	    		PrintWriter pw = new PrintWriter(fos);
    			for(int j=1;j<array.length;j++)
    			{
    				String id = null;
	    			int rank=1;
	    			String s=array[j];
	    			String namepass1[]=null;
	    			Pattern pattern1 = Pattern.compile("<num>(.*?)<dom>");	
	    			Matcher matcher1 = pattern1.matcher(s);   
	    			if (matcher1.find()) 
	    			{
	    				id = matcher1.group(1);//taking only group 1
	    				namepass1 = id.split(":");
	    			}
    				//take description of each top from the topics.51-100 as long query
	    			Pattern pattern = Pattern.compile("<desc>(.*?)<smry>");	
	    			Matcher matcher = pattern.matcher(s);   
	    			if (matcher.find()) 
	    			{
	    				String tag_value = matcher.group(1);//taking only group 1
	    				String namepass[] = tag_value.split(":");
	    				// Get the preprocessed query terms
					//use models to calculate relevance score.
	    				searcher.setSimilarity(new ClassicSimilarity());//BM25Similarity() , DirichletSimilarity() , JelinekMercerSimilarity((float)0.7).
	    				Analyzer analyzer = new StandardAnalyzer();
	    				QueryParser parser = new QueryParser("TEXT", analyzer);
	    				Query query = parser.parse(QueryParser.escape(namepass[1]));
	    				//get relevance score
	    				TopDocs topDocs = searcher.search(query, 1000);
	    				ScoreDoc[] docs = topDocs.scoreDocs;
	    				for (int i = 0; i < docs.length; i++)
	    				{
	    					Document doc = searcher.doc(docs[i].doc);
	    					pw.write((namepass1[1]+"\tQ"+(j-1)+"\t"+ doc.get("DOCNO") + "\t"+rank +"\t"+ docs[i].score+"\t"+"run-"+j+"\n"));
     						rank++;
    					}				
	    			}
    			}
    		 	pw.flush();
    		 	fos.close();
    			pw.close();
    		}
   	}
}
