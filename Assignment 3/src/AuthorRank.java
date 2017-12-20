import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

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

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class AuthorRank { 
	
	static HashMap<String, String> vertices = new HashMap<>();
	static HashMap<String, String> edges = new HashMap<>();
	static HashMap<String, Double> vertexscore = new HashMap<>();
	static DirectedSparseGraph<String, Integer> graph = new DirectedSparseGraph<String, Integer>();

	public static void main(String args[]) throws IOException
	{
	DrawGraph();
	FindPageRank();
	}
	
	//draw graph using DirectedSparseGraph
	public static void DrawGraph() throws IOException {
		String words[];
		int count=1;
		int flag=0;
		File path=new File("author_index");
		Directory index = FSDirectory.open(path);
		IndexReader reader = DirectoryReader.open(index);
		IndexSearcher searcher = new IndexSearcher(reader);
		// Get the vertices and edges from author.net file
		FileInputStream fstream = new FileInputStream("author.net");
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String Line;
		while ((Line = br.readLine()) != null)
		{
			words = Line.split("\\s");
			if(Line.contains("*Vertices") || Line.contains("*Edges") ) {
				flag++;
				continue;	
			}
			//add vertices in graph
			else if(flag==1) {
				Pattern p = Pattern.compile("\"([^\"]*)\"");
				Matcher m = p.matcher(Line);
				if (m.find()) {
					vertices.put(words[0], m.group(1));
					graph.addVertex(m.group(1));
				}		
			}
			//add edges in graph
			else if(flag==2) {
				edges.put(words[0], words[1]);
				graph.addEdge(count++,vertices.get(words[0]),vertices.get(words[1]),EdgeType.DIRECTED);
			}
		}
	}
	
	//find pagerank with priors ie top 10 authors
	public static void FindPageRank() {
		//pagerank 
		PageRank<String, Integer> ranker = new PageRank<String, Integer>(graph, 0.15);
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
			System.out.println(  ((Map.Entry<String, Float>) e).getKey()+"\t" +( (Map.Entry<String, Double >) e).getValue()+"\t");
			rank++;
			if(rank>10) {
				break;
			}
		}
	}
}


