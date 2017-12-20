import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;

import org.apache.commons.collections15.Transformer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;


public class AuthorRankwithQuery {
	
	static HashMap<String, Double> priors = new HashMap<>();
	static DirectedSparseGraph<String, Integer> graph = new DirectedSparseGraph<String, Integer>();
	static HashMap<String, String> vertices = new HashMap<>();
	static HashMap<String, String> edges = new HashMap<>();
	static HashMap<String, Double> vertexscore = new HashMap<>();	
	
	public static void main(String args[]) throws IOException, ParseException {
		Scanner ss = new Scanner(System.in);
		System.out.print("Enter query: ");
		String s = ss.nextLine();
		CalculatePriors(s);
		DrawGraph();
		FindPageRank();	   
	}

	//calculates prior probability
	public static void CalculatePriors(String s) throws IOException, ParseException
	{
		File path=new File("author_index");
		Directory index = FSDirectory.open(path);
		IndexReader reader = DirectoryReader.open(index);
		IndexSearcher searcher = new IndexSearcher(reader);	
		searcher.setSimilarity(new BM25Similarity());
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("content", analyzer);
		Query query = parser.parse(QueryParser.escape(s));
		TopDocs topDocs = searcher.search(query,300);
		ScoreDoc[] docs = topDocs.scoreDocs;
		double SumOFpriors=0.0;
		int rank1=1;
		//find relevance score
		for (int i = 0; i < docs.length; i++) 
		{
			Document doc = searcher.doc(docs[i].doc);
			double Score=docs[i].score;
			SumOFpriors+=Score;
			String authorid=doc.get("authorid");
			if(priors.keySet().contains(authorid) )
				priors.put(authorid, priors.get(authorid)+Score);
			else
            		priors.put(authorid, Score);
			rank1++;
		}
		for (String key : priors.keySet()) 
			priors.put(key, priors.get(key)/SumOFpriors);
}

	//draw graph using DirectedSparseGraph
	public static void DrawGraph() throws IOException 
	{
		String words[];
		int count=0;
		int flag=0;
		Double k;
		// Get the vertices and edges from author.net file
		FileInputStream fstream = new FileInputStream("author.net");
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String Line;
		while ((Line = br.readLine()) != null)
		{
			words = Line.split("\\s");
			if(Line.contains("*Vertices") || Line.contains("*Edges") ) 
			{
				flag++;
				continue;	
			}
			//add vertices in graph
			else if(flag==1) 
			{
				Pattern p = Pattern.compile("\"([^\"]*)\"");
				Matcher m = p.matcher(Line);
				if (m.find()) 
				{
					vertices.put(words[0], (m.group(1)));
					graph.addVertex(m.group(1));
				}
			}
			//add edges in graph
			else if(flag==2) 
			{
				edges.put(words[0], words[1]);	
				graph.addEdge(count++,vertices.get(words[0]),vertices.get(words[1]),EdgeType.DIRECTED);
			}
		}
	}
	
	//find pagerank with priors ie top 10 authors
	public static void FindPageRank()
	{
		Transformer<String, Double> priorsmap = new Transformer<String, Double>()
		{            
			@Override
			public Double transform(String v) 
			{   
				if(priors.containsKey(v))
					return priors.get(v);
				else 	
					return (double)0.0;  
			}           
		};
		//pagerank 
		PageRankWithPriors<String, Integer> ranker = new PageRankWithPriors<String, Integer>(graph,priorsmap, 0.15);
		ranker.evaluate();
		for (String v : graph.getVertices()) {
			vertexscore.put(v, ranker.getVertexScore(v));
		}
		//sort score in descending order
		Object[] a = vertexscore.entrySet().toArray();
		Arrays.sort(a, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Map.Entry<String,Double>) o2).getValue()
						.compareTo(((Map.Entry<String, Double>) o1).getValue());
			}
		});
		int rank=1;
		//print page rank score
		System.out.println("Author ID\tAuthor Rank Score");
		for (Object e : a) 
		{
			String k1=((Map.Entry<String, Double>) e).getKey();
			System.out.println(  ((Map.Entry<String, Float>) e).getKey()+"\t"+( (Map.Entry<String, Double >) e).getValue()+"\t");
			rank++;
			if(rank>10) {
				break;
			}
		}
	}
}

