//generate index code
//referred https://www.regular-expressions.info/captureall.html for parser
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class generateIndex {
	public static void main(String args[]) throws IOException
	{
	String indexpath="index/index_StandardAnalyzer";
	String docpath="corpus";
	File[] files= new File(docpath).listFiles();
	System.out.println("indexing to "+ indexpath);
	Directory dir = FSDirectory.open(Paths.get(indexpath));
	//It is default analyzer,which generates tokens and removes default set stop words.It is used when no other analyzer is specified
    Analyzer analyzer = new StandardAnalyzer();
    //Analyzer analyzer = new SimpleAnalyzer(); //This analyzer just generates tokens and does not remove any stop words.                          
    //Analyzer analyzer = new StopAnalyzer();  // This analyzer generates tokens and removes stop words like "is","the",etc.                        
    //Analyzer analyzer = new KeywordAnalyzer(); //This analyzer considers whole string as one single token.                            
    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
    iwc.setOpenMode(OpenMode.CREATE);
    IndexWriter writer = new IndexWriter(dir, iwc);
    //for each file in the folder corpus generate a new document for each <doc>
    int i=0;
    for(File file:files) 
    {	
    	 	String fileAsString = null;
    	    try {
    	    	//read the file and saving into a string
    	    InputStream is = new FileInputStream(file); 
    	    	BufferedReader buf = new BufferedReader(new InputStreamReader(is)); 
    	    	String line = buf.readLine(); 
    	    	StringBuilder sb = new StringBuilder();
    	    	while(line != null){ 
    	    		sb.append(line); 
    	    	line = buf.readLine();
    	    	}
    	    	fileAsString = sb.toString();
    	    } catch (IOException e) {
    	    }
    	    //using delimiters to separate each <doc> in a file
    	    String[] array=fileAsString.split("<DOC>");
    	    for(int j=1;j<array.length;j++)
    		{	
    	    		String s=array[j];
    	    		///create a new documnt
    			Document luceneDoc = new Document();
    			//for docno
    			//using pattern matcher to get string values between tags
    			Pattern pattern = Pattern.compile("<DOCNO>(.*?)</DOCNO>");	
	        	Matcher matcher = pattern.matcher(s);   
	        	if (matcher.find()) 
	        	{
	        	    String tag_value = matcher.group(1); //taking only group 1
	        	    luceneDoc.add(new StringField("DOCNO", tag_value ,Field.Store.YES)); 
	        	   // System.out.println("docno "+j+" " + tag_value);
	        	}
	       //for head
    			StringBuilder value = new StringBuilder();    			
	        	Pattern pattern2 = Pattern.compile("<HEAD>(.*?)</HEAD>");	
	        	Matcher matcher2 = pattern2.matcher(s);  
	        	while (matcher2.find()) 
	        	{
	        	    String head = matcher2.group(1);
	        	    value.append(head);
	        	    value.append(" ");   		  
	        	}
	        	if(matcher2.find(1))
	        	{
	        		luceneDoc.add(new TextField("HEAD", value.toString() ,Field.Store.YES)); 
	        	}
	        	//for byline
	        	StringBuilder byline = new StringBuilder();    			
	        	Pattern pattern3 = Pattern.compile("<BYLINE>(.*?)</BYLINE>");	
	        	Matcher matcher3 = pattern3.matcher(s);  
	        	while (matcher3.find())
	        	{
	        	    String head1 = matcher3.group(1);
	        	    byline.append(head1);
	        	    byline.append(" ");   		  
	        	}
	        	if(matcher3.find(1))
	        	{ 
	        	  luceneDoc.add(new TextField("BYLINE", byline.toString() ,Field.Store.YES)); 
	        	}
	        	//for dateline
	        StringBuilder dateline = new StringBuilder();    			
	        	Pattern pattern4 = Pattern.compile("<DATELINE>(.*?)</DATELINE>");	
	        	Matcher matcher4 = pattern4.matcher(s);  
	        	while (matcher4.find()) {
	        	    String head2 = matcher4.group(1);
	        	    dateline.append(head2);
	        	    dateline.append(" ");   		  
	        	}
	        	if(matcher4.find(1))
	        	{ 
	        	  luceneDoc.add(new TextField("DATELINE", dateline.toString() ,Field.Store.YES)); 
	        	}
	        	//for text
	        	StringBuilder text = new StringBuilder();
	        	Pattern pattern1 = Pattern.compile("<TEXT>(.*?)</TEXT>");
	      	Matcher matcher1 = pattern1.matcher(s);   
	      	while (matcher1.find()) {
        	    String head3 = matcher1.group(1);
        	    text.append(head3);
        	    text.append(" ");   		  
        	}
        	if(matcher1.find(1))
        	{ 
        	  luceneDoc.add(new TextField("TEXT", text.toString() ,Field.Store.YES)); 
        	}	
	   	writer.addDocument(luceneDoc);        	
    		}	
    	    i=i+1;	    
    }
    //forcemerge into one segment
	writer.forceMerge(1);
  	writer.commit();
  	writer.close(); 	
  	System.out.println("done");
	}
	}
	



